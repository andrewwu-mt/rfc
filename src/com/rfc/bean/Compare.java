package com.rfc.bean;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class Compare {
	
	public void compareFile(String input_path, List<String> edmList, List<String> fbList){
		try{
			Map<String, Map<String, List<String>>> edmMap = new HashMap<String, Map<String, List<String>>>();
			Map<String, Map<String, List<String>>> fbMap = new HashMap<String, Map<String, List<String>>>();
			Map<String, List<String>> edmSheetMap = new HashMap<String, List<String>>();
			Map<String, List<String>> fbSheetMap = new HashMap<String, List<String>>();
			
			gatherData(input_path+"EDM/", edmList, edmSheetMap, edmMap);
			gatherData(input_path+"FB/", fbList, fbSheetMap, fbMap);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	
	public void gatherData(String path, List<String> list, Map<String, List<String>> sheetMap, Map<String, Map<String, List<String>>> map){
		for(String filename : list){
			try{
				InputStream is = new FileInputStream(path+filename);
				HSSFWorkbook wb = new HSSFWorkbook(is);
				
				for(int i=0 ; i<wb.getNumberOfSheets() ; i++){
					HSSFSheet sheet = wb.getSheetAt(i);
					String sheetName = sheet.getSheetName();
					Integer[] intArr = getColumnNumber(sheetName.trim());
					if(intArr != null){
						Integer nameCol = 0;
						if(sheetName.trim().equals("T_Bill") || 
							sheetName.trim().equals("FX_Vol_Moneyness_Term") || 
							sheetName.trim().equals("Bond_Vol") || 
							sheetName.trim().equals("Equity_Vol") || 
							sheetName.trim().equals("Swaption_Vol")) nameCol = 1;
		
						List<String> cellList = new ArrayList<String>();
						
						for (int k=0 ; k<sheet.getLastRowNum()+1 ; k++){
							if(k>0){
								try{
									HSSFRow row = sheet.getRow(k);
									
									for(int j=0 ; j<row.getLastCellNum() ; j++){
										List<Integer> intList = Arrays.asList(intArr);
										
										if(intList.contains(j)){
											if(!sheetName.toLowerCase().contains("vol")){
												String columnName = sheet.getRow(0).getCell(j).getStringCellValue();
												HSSFCell cell = row.getCell(j);
												String value = cell.getStringCellValue();
												if(value != null && !"".equals(value)){
													String identifier = row.getCell(nameCol).toString();
													String result = identifier + "," + columnName + "," + value;
													cellList.add(result);
												}
											} else {
												if(!sheetName.toLowerCase().contains("swaption_vol")){
													if(k % 2 != 0){
														String identifier = row.getCell(nameCol).toString();
														HSSFCell cell = row.getCell(j);
														String columnName = cell.getStringCellValue();
														String value = sheet.getRow(k+1).getCell(j).getStringCellValue();
														if(value != null && !"".equals(value)){
															String result = identifier + "," + columnName + "," + value;
															cellList.add(result);
														}
													}
												}else{
													if(k == 1){
														String identifier = row.getCell(k).toString();
														for(int idx=0 ; idx<7 ; idx++){
															Integer rowNum = idx+k+1;
															String rowName = sheet.getRow(rowNum).getCell(15).getStringCellValue();
															String columnName = sheet.getRow(k).getCell(j).getStringCellValue();
															String value = sheet.getRow(rowNum).getCell(j).getStringCellValue();
															if(value != null && !"".equals(value)){
																String result = identifier + "," + rowName+"&"+columnName + "," + value;
																cellList.add(result);
															}
															
														}
													}
													if(k == 9){
														String identifier = row.getCell(k).toString();
														for(int idx=0 ; idx<14 ; idx++){
															Integer rowNum = idx+k+1;
															String rowName = sheet.getRow(rowNum).getCell(15).getStringCellValue();
															String columnName = sheet.getRow(k).getCell(j).getStringCellValue();
															String value = sheet.getRow(rowNum).getCell(j).getStringCellValue();
															if(value != null && !"".equals(value)){
																String result = identifier + "," + rowName+"&"+columnName + "," + value;
																cellList.add(result);
															}
															
														}
													}
													if(k == 24){
														String identifier = row.getCell(k).toString();
														for(int idx=0 ; idx<14 ; idx++){
															Integer rowNum = idx+k+1;
															String rowName = sheet.getRow(rowNum).getCell(15).getStringCellValue();
															String columnName = sheet.getRow(k).getCell(j).getStringCellValue();
															String value = sheet.getRow(rowNum).getCell(j).getStringCellValue();
															if(value != null && !"".equals(value)){
																String result = identifier + "," + rowName+"&"+columnName + "," + value;
																cellList.add(result);
															}
															
														}
													}
													if(k == 39){
														String identifier = row.getCell(k).toString();
														for(int idx=0 ; idx<7 ; idx++){
															Integer rowNum = idx+k+1;
															String rowName = sheet.getRow(rowNum).getCell(15).getStringCellValue();
															String columnName = sheet.getRow(k).getCell(j).getStringCellValue();
															String value = sheet.getRow(rowNum).getCell(j).getStringCellValue();
															if(value != null && !"".equals(value)){
																String result = identifier + "," + rowName+"&"+columnName + "," + value;
																cellList.add(result);
															}
															
														}
													}
													if(k == 47){
														String identifier = row.getCell(k).toString();
														for(int idx=0 ; idx<7 ; idx++){
															Integer rowNum = idx+k+1;
															String rowName = sheet.getRow(rowNum).getCell(15).getStringCellValue();
															String columnName = sheet.getRow(k).getCell(j).getStringCellValue();
															String value = sheet.getRow(rowNum).getCell(j).getStringCellValue();
															if(value != null && !"".equals(value)){
																String result = identifier + "," + rowName+"&"+columnName + "," + value;
																cellList.add(result);
															}
															
														}
													}
													
												}
											}
										}
									}
								}catch(Exception e){
									e.printStackTrace();
								}
							}
						}
						
						sheetMap.put(sheetName, cellList); //Save data per sheet
					}
				}
				
				map.put(filename, sheetMap);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
			
	}
	
	
	public Integer[] getColumnNumber(String sheetName){ 
		Integer[] intArr = null;
		switch(sheetName){
			case "Constant_Term_Bullet_Bond":
				intArr = new Integer[] {5,6};
				
			break;

			case "Constant_Term_Swap_Fixed_Leg":
				intArr = new Integer[] {7,8};
				
			break;

			case "Bond_For_Future":
				intArr = new Integer[] {14,16};
				
			break;

			case "Fixed_Rate_Bond":
				intArr = new Integer[] {12,14};
				
			break;

			case "T_Bill":
				intArr = new Integer[] {14};
				
			break;

			case "Constant_Term_Forex_Forward":
				intArr = new Integer[] {5};
				
			break;

			case "Constant_Term_FRA":
				intArr = new Integer[] {5};
				
			break;

			case "IRFuture":
				intArr = new Integer[] {10};
				
			break;

			case "Market_Index":
				intArr = new Integer[] {4};
				
			break;

			case "BondFuture":
				intArr = new Integer[] {9};
				
			break;

			case "Foreign_Exchange":
				intArr = new Integer[] {7};
				
			break;

			case "FX_Vol_Moneyness_Term":
				intArr = new Integer[] {16,17,18,19,20,21,22,23};
				
			break;

			case "Bond_Vol":
				intArr = new Integer[] {16,17,18,19,20};
				
			break;

			case "CF_Vol":
				intArr = new Integer[] {14,15,16,17,18,19,20};
				
			break;

			case "Equity_Vol":
				intArr = new Integer[] {14};
				
			break;

			case "Swaption_Vol":
				intArr = new Integer[] {16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32};
				
			break;
		}
		
		return intArr;
	}
	
	public String getRule(String sheetName){ 
		String rule = "";
		
		switch(sheetName){
			case "Constant_Term_Bullet_Bond":
				rule = "1,0.001";
				
			break;

			case "Constant_Term_Swap_Fixed_Leg":
				rule = "1,0.001";
				
			break;

			case "Bond_For_Future":
				rule = "2,1";
				
			break;

			case "Fixed_Rate_Bond":
				rule = "2,1";
				
			break;

			case "T_Bill":
				rule = "2,1";
				
			break;

			case "Constant_Term_Forex_Forward":
				rule = "2,1";
				
			break;

			case "Constant_Term_FRA":
				rule = "1,0.001";
				
			break;

			case "IRFuture":
				rule = "2,1";
				
			break;

			case "Market_Index":
				rule = "2,1";
				
			break;

			case "BondFuture":
				rule = "2,1";
				
			break;

			case "Foreign_Exchange":
				rule = "2,0.1";
				
			break;

			case "FX_Vol_Moneyness_Term":
				rule = "2,10";
				
			break;

			case "Bond_Vol":
				rule = "2,10";
				
			break;

			case "CF_Vol":
				rule = "2,10";
				
			break;

			case "Equity_Vol":
				rule = "2,10";
				
			break;

			case "Swaption_Vol":
				rule = "2,10";
				
			break;
		}
		
		return rule;
	}
}
