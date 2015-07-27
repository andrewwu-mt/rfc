package com.rfc.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class Compare {
	
	public void startCompare(String input_path, List<String> edmList, List<String> fbList, String evalDate){
		Map<String, Map<String, Map<String, Map<String, String>>>> edmFileMap = new HashMap<String, Map<String, Map<String, Map<String, String>>>>();
		Map<String, Map<String, Map<String, Map<String, String>>>> fbFileMap = new HashMap<String, Map<String, Map<String, Map<String, String>>>>();

		try{
			gatherData(input_path+"EDM/", edmList, edmFileMap);
			gatherData(input_path+"FB/", fbList, fbFileMap);
			
			Map<String, Map<String, Map<String, Map<String, String>>>> fileMap = compareData(edmFileMap, fbFileMap);
			writeExcel(fileMap, evalDate);
			System.out.println("DONE");
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void writeExcel(Map<String, Map<String, Map<String, Map<String, String>>>> fileMap, String evalDate){
		try{
			Workbook workbook = new HSSFWorkbook();
			Sheet sheet = workbook.createSheet("RF Compare Result");
			
			//Create header
			Row hRow = sheet.createRow(0);
			hRow.createCell(0).setCellValue("File Name");
			hRow.createCell(1).setCellValue("Sheet Name");
			hRow.createCell(2).setCellValue("Risk Factor ID");
			hRow.createCell(3).setCellValue("Column Name");
			hRow.createCell(4).setCellValue("Rule type");
			hRow.createCell(5).setCellValue("Rule limit");
			hRow.createCell(6).setCellValue("EDM Value");
			hRow.createCell(7).setCellValue("Fubon Value");
			hRow.createCell(8).setCellValue("Diff/Ratio");
			
			//Create content
			Integer rowIdx = 1;
			for(String filename : fileMap.keySet()){
				Map<String, Map<String, Map<String, String>>> sheetMap = fileMap.get(filename);
				
				for(String sheetName : sheetMap.keySet()){
					Map<String, Map<String, String>> identMap = sheetMap.get(sheetName);
					
					for(String identifier : identMap.keySet()){
						Map<String, String> colMap = identMap.get(identifier);
						
						for(String colName : colMap.keySet()){
							String value = colMap.get(colName);
							String[] valArr = value.split(",");
							String ruleTypeStr = valArr[0];
							String ruleLimit = valArr[1];
							String edmVal = valArr[2];
							String fbVal = valArr[3];
							String reason = valArr[4];
	
							Row row = sheet.createRow(rowIdx);
							row.createCell(0).setCellValue(filename);
							row.createCell(1).setCellValue(sheetName);
							row.createCell(2).setCellValue(identifier);
							row.createCell(3).setCellValue(colName);
							row.createCell(4).setCellValue(ruleTypeStr);
							row.createCell(5).setCellValue(ruleLimit);
							row.createCell(6).setCellValue(edmVal);
							row.createCell(7).setCellValue(fbVal);
							row.createCell(8).setCellValue(reason);
							
							rowIdx++;
						}
					}
				}
			}
	
			String output_path = new LoadProperties().load("output.path");
			checkFolder(output_path);
			File tempFile = new File(output_path + "result_"+ evalDate +".xls");
			FileOutputStream fileOut = new FileOutputStream(tempFile);
			workbook.write(fileOut);
			fileOut.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void checkFolder(String output_path){
		File outputPath = new File(output_path);
		if(!outputPath.exists()){
			outputPath.mkdirs();
		}
	}
	
	public Map<String, Map<String, Map<String, Map<String, String>>>> compareData(Map<String, Map<String, Map<String, Map<String, String>>>> edmFileMap, Map<String, Map<String, Map<String, Map<String, String>>>> fbFileMap){
		Map<String, Map<String, Map<String, Map<String, String>>>> fileMap = new HashMap<String, Map<String, Map<String, Map<String, String>>>>();
		DecimalFormat df = new DecimalFormat("#.#");
        df.setMaximumFractionDigits(8);
        
		for(String filename : edmFileMap.keySet()){
			Map<String, Map<String, Map<String, String>>> edmSheetMap = edmFileMap.get(filename);
			Map<String, Map<String, Map<String, String>>> fbSheetMap = fbFileMap.get(filename);
			
			if(edmSheetMap != null && fbSheetMap != null){
				Map<String, Map<String, Map<String, String>>> sheetMap = new HashMap<String, Map<String, Map<String, String>>>();
				for(String sheetName : edmSheetMap.keySet()){
					String rule = getRule(sheetName.trim());
					Integer ruleType = Integer.valueOf(rule.split(",")[0]);
					Double ruleLimit = Double.valueOf(rule.split(",")[1]);
					String ruleTypeStr = "Diff";
					if(ruleType == 2) ruleTypeStr = "Ratio";
					
					Map<String, Map<String, String>> edmIdentMap = edmSheetMap.get(sheetName);
					Map<String, Map<String, String>> fbIdentMap = fbSheetMap.get(sheetName);
					
					if(edmIdentMap != null && fbIdentMap != null){
						Map<String, Map<String, String>> identMap = new HashMap<String, Map<String, String>>();
						for(String identifier : edmIdentMap.keySet()){
							Map<String, String> edmColMap = edmIdentMap.get(identifier);
							Map<String, String> fbColMap = fbIdentMap.get(identifier);
							
							if(edmColMap != null && fbColMap != null){
								Map<String, String> colMap = new HashMap<String, String>();
								for(String colName : edmColMap.keySet()){
									String edmVal = edmColMap.get(colName);
									String fbVal = fbColMap.get(colName);
									
									if((edmVal!=null && !"".equals(edmVal)) && (fbVal!=null && !"".equals(fbVal))){
										try{
											if(colName.trim().equals("Coupon Rate")){
												if(!edmVal.equals(fbVal)) colMap.put(colName, ruleTypeStr + "," + ruleLimit + "," + edmVal+","+fbVal+",Not same");
											} else {
												if(edmVal.contains(" ")){
													edmVal = edmVal.split(" ")[0];
													fbVal = fbVal.split(" ")[0];
												}
												
												try{
													Double edm = Double.valueOf(edmVal);
													Double fb = Double.valueOf(fbVal);
													if(ruleType == 1){
														Double diff = Math.abs(fb-edm);
														if(diff > ruleLimit) colMap.put(colName, ruleTypeStr + "," + ruleLimit + "," + df.format(edm)+","+df.format(fb)+","+diff);
													} else {
														Double diff = (Math.abs(fb-edm) / fb) * 100;
														if(diff > ruleLimit) colMap.put(colName, ruleTypeStr + "," + ruleLimit + "," + df.format(edm)+","+df.format(fb)+","+diff+"%");
													}
												}catch(Exception e){
													colMap.put(colName, ruleTypeStr + "," + ruleLimit + "," + edmColMap.get(colName)+","+fbColMap.get(colName)+",Empty Value");
												}
											}
										}catch(Exception e){
											e.printStackTrace();
										}
									}
									
									
								}
								if(colMap.size() != 0) identMap.put(identifier, colMap);
							}
						}
						if(identMap.size() != 0) sheetMap.put(sheetName, identMap);
					}
				}
				if(sheetMap.size() != 0) fileMap.put(filename, sheetMap);
			}
		}
		
		return fileMap;
	}
	
	public void gatherData(String path, List<String> list, Map<String, Map<String, Map<String, Map<String, String>>>> fileMap){
		for(String filename : list){
			try{
				InputStream is = new FileInputStream(path+filename);
				HSSFWorkbook wb = new HSSFWorkbook(is);

				Map<String, Map<String, Map<String, String>>> sheetMap = new HashMap<String, Map<String, Map<String, String>>>();
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

						Map<String, Map<String, String>> identMap = new HashMap<String, Map<String, String>>();
						for (int k=0 ; k<sheet.getLastRowNum()+1 ; k++){
							
							if(k>0){
								HSSFRow row = sheet.getRow(k);
								if(row != null){
									try{
										Map<String, String> colMap = new HashMap<String, String>();
										for(int j=0 ; j<row.getLastCellNum() ; j++){
											List<Integer> intList = Arrays.asList(intArr);
											
											if(intList.contains(j)){
												if(!sheetName.toLowerCase().contains("vol")){
													HSSFCell cellCol = sheet.getRow(0).getCell(j);
													cellCol.setCellType(Cell.CELL_TYPE_STRING);
													String columnName = cellCol.getStringCellValue();
													
													HSSFCell cell = row.getCell(j);
													cell.setCellType(Cell.CELL_TYPE_STRING);
													String value = cell.getStringCellValue();
													if(value != null && !"".equals(value)){
														String identifier = row.getCell(nameCol).toString();
														colMap.put(columnName, value);
														identMap.put(identifier, colMap);
													}
												} else {
													if(!sheetName.toLowerCase().contains("swaption_vol")){
														if(k % 2 != 0){
															try{
																String identifier = row.getCell(nameCol).toString();
																HSSFCell cellCol = row.getCell(j);
																cellCol.setCellType(Cell.CELL_TYPE_STRING);
																String columnName = cellCol.getStringCellValue();
																
																HSSFCell cellVal = sheet.getRow(k+1).getCell(j);
																cellVal.setCellType(Cell.CELL_TYPE_STRING);
																String value = cellVal.getStringCellValue();
																if(value != null && !"".equals(value)){
																	colMap.put(columnName, value);
																	identMap.put(identifier, colMap);
																}
															}catch(Exception e){
																e.printStackTrace();
															}
														}
													}else{
														try{
															String volName = null;
															try{volName = row.getCell(nameCol).toString();}catch(Exception e){}
															
															if(volName != null){
																Integer loopSize = getRowNum(volName);
																for(int idx=0 ; idx<loopSize ; idx++){
																	Integer rowNum = idx+k+1;
																	HSSFCell cellRow = sheet.getRow(rowNum).getCell(15);
																	cellRow.setCellType(Cell.CELL_TYPE_STRING);
																	String rowName = cellRow.getStringCellValue();
	
																	HSSFCell cellCol = sheet.getRow(k).getCell(j);
																	cellCol.setCellType(Cell.CELL_TYPE_STRING);
																	String columnName = cellCol.getStringCellValue();
																	
																	HSSFCell cellVal = sheet.getRow(rowNum).getCell(j);
																	cellVal.setCellType(Cell.CELL_TYPE_STRING);
																	String value = cellVal.getStringCellValue();
																	if(value != null && !"".equals(value)){
																		colMap.put(rowName+"&"+columnName, value);
																		identMap.put(volName, colMap);
																	}
																}
															}
														}catch(Exception e){
															e.printStackTrace();
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
						}
						sheetMap.put(sheetName, identMap);
					}
				}
				fileMap.put(filename, sheetMap);
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

	public Integer getRowNum(String volName){ 
		Integer rowNum = null;
		
		switch(volName){
			case "AUD SwaptionVol":
				rowNum = 7;
			break;

			case "EUR SwaptionVol":
				rowNum = 14;
			break;

			case "GBP SwaptionVol":
				rowNum = 14;
			break;

			case "TWD SwaptionVol":
				rowNum = 7;
			break;

			case "USD SwaptionVol":
				rowNum = 7;
			break;

		}
		
		return rowNum;
	}
}
