package com.rfc.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

import au.com.bytecode.opencsv.CSVReader;

public class Compare {
	private static final Logger log = Logger.getLogger(Compare.class);
	
	private String[] priceColArr = {"Spot Price", "Strike Price"};
	private String[] specialSheetArr = {"T_Bill" , "Interbank_Curve" , "OIS_Curve", "Treasury_Curve" , "Corporate_Curve" , "Depo_Curve" , "Repo_Curve" , "Currency_Swap_Curve" , "Forward_Curve" , "FRA_Curve" , "Index_Growth" , "Index_Volatility" , "Exchange_Rate" , "FX_Converter" , "FX_Vol_Moneyness_Term" , "Bond_Vol" , "CF_Vol" , "Equity_Vol" , "Swaption_Vol"};
	private String[] excludeSheetArr = {""};
	
	private String[] optionColArr = {"Type", "Currency", "Maturity Date", "Volatility", "Volatility Surface", "RiskMetrics Map Procedure", "Discount Curve", "*Theoretical Model", "*Market Model", "Settlement Type", "Settlement Procedure", "Volatility Type"};
	
	public void startCompare(String input_path, List<String> edmList, List<String> fbList, String evalDate){
		Map<String, Map<String, Map<String, Map<String, String>>>> edmFileMap = new HashMap<String, Map<String, Map<String, Map<String, String>>>>();
		Map<String, Map<String, Map<String, Map<String, String>>>> fbFileMap = new HashMap<String, Map<String, Map<String, Map<String, String>>>>();

		try{
			System.out.println("Gathering EDM data...");
			log.info("Gathering EDM data...");
			gatherData(input_path+"EDM/", edmList, edmFileMap);

			System.out.println("Gathering FB data...");
			log.info("Gathering FB data...");
			gatherData(input_path+"FB/", fbList, fbFileMap);
			
			System.out.println("Start comparing...");
			log.info("Start comparing...");
			Map<String, Map<String, Map<String, Map<String, String>>>> fileMap = compareData(edmFileMap, fbFileMap);
			writeExcel(fileMap, evalDate);
			System.out.println("DONE");
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void createHeader(HSSFRow hRow){
		hRow.createCell(0).setCellValue("File Name");
		hRow.createCell(1).setCellValue("Sheet Name");
		hRow.createCell(2).setCellValue("Risk Factor ID");
		hRow.createCell(3).setCellValue("Column Name");
		hRow.createCell(4).setCellValue("Rule type");
		hRow.createCell(5).setCellValue("Rule limit");
		hRow.createCell(6).setCellValue("EDM Value");
		hRow.createCell(7).setCellValue("Fubon Value");
		hRow.createCell(8).setCellValue("Diff/Ratio");
	}
	
	public void writeExcel(Map<String, Map<String, Map<String, Map<String, String>>>> fileMap, String evalDate){
		try{
			HSSFWorkbook workbook = new HSSFWorkbook();
			Integer sheetNum = 1;
			HSSFSheet sheet = workbook.createSheet("RF Compare Result (" +sheetNum+")");

			//Create header
			HSSFRow hRow = sheet.createRow(0);
			createHeader(hRow);
			
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

							if(rowIdx > 65535){
								sheetNum++;
								rowIdx = 1;
								sheet = workbook.createSheet("RF Compare Result (" +sheetNum+")");
								hRow = sheet.createRow(0);
								createHeader(hRow);
							}
							
							try{
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
							}catch(Exception e){
								e.printStackTrace();
							}
							
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
        
		for(String filename : fbFileMap.keySet()){
			Map<String, Map<String, Map<String, String>>> edmSheetMap = edmFileMap.get(filename);
			Map<String, Map<String, Map<String, String>>> fbSheetMap = fbFileMap.get(filename);
			
			if(edmSheetMap != null && fbSheetMap != null){
				Map<String, Map<String, Map<String, String>>> sheetMap = new HashMap<String, Map<String, Map<String, String>>>();
				for(String sheetName : fbSheetMap.keySet()){
					if(sheetName.equals("Equity_Vol")){
						System.out.println();
					}
					String rule = getRule(sheetName.trim());
					Integer ruleType = 0;
					Double ruleLimit = 0.0;
					
					if(rule != null && !"".equals(rule)){
						ruleType = Integer.valueOf(rule.split(",")[0]);
						ruleLimit = Double.valueOf(rule.split(",")[1]);
					}
					
					String ruleTypeStr = "Equal";
					if(ruleType == 1) ruleTypeStr = "Difference";
					if(ruleType == 2) ruleTypeStr = "Ratio";
					
					Map<String, Map<String, String>> edmIdentMap = edmSheetMap.get(sheetName);
					Map<String, Map<String, String>> fbIdentMap = fbSheetMap.get(sheetName);
					
					if(fbIdentMap != null){
						Map<String, Map<String, String>> identMap = new HashMap<String, Map<String, String>>();
						for(String identifier : fbIdentMap.keySet()){
							if(identifier.equals("1419 Vol")){
								System.out.println();
							}
							
							Map<String, String> edmColMap = edmIdentMap.get(identifier);
							Map<String, String> fbColMap = fbIdentMap.get(identifier);
							
							if(fbColMap != null){
								Map<String, String> colMap = new HashMap<String, String>();
								if(edmColMap == null){
									colMap.put("", ruleTypeStr + "," + ruleLimit + ",,"+identifier+",Risk Factor ID not found");
								} else {
									for(String colName : fbColMap.keySet()){
										String edmVal = edmColMap.get(colName);
										String fbVal = fbColMap.get(colName);
										
										if(fbVal!=null && !"".equals(fbVal)){
	
											if(edmVal == null || "".equals(edmVal)){
												colMap.put(colName, ruleTypeStr + "," + ruleLimit + ",,"+fbVal+",Value empty");
											} else {
												try{
													if(Arrays.asList(priceColArr).contains(colName.trim()) || colName.trim().contains("&")) {
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
															colMap.put(colName, ruleTypeStr + "," + ruleLimit + "," + edmColMap.get(colName)+","+fbColMap.get(colName)+",Value empty");
														}
													} else {
														if(colName.trim().toLowerCase().contains("procedure parameter")){
															String differ = compareProcedureParameter(edmVal, fbVal);
															if(!differ.equals("")) colMap.put(colName, "Equal,N/A" + "," + edmVal.replace(",", "|")+","+fbVal.replace(",", "|")+","+differ);
														} else{
															if(!edmVal.equals(fbVal)) colMap.put(colName, "Equal,N/A" + "," + edmVal+","+fbVal+",Value inconsistent");
														}
													}
												}catch(Exception e){
													System.out.println("ERROR");
												}
											}
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
	
	public String compareProcedureParameter(String edmVal, String fbVal){
		String differ = "";
		
		List<String> edmList = new ArrayList<String>();
		List<String> fbList = new ArrayList<String>();
		
		String[] edmArr = edmVal.split(",");
		String[] fbArr = fbVal.split(",");
		
		for(String edm : edmArr) edmList.add(edm.trim());
		for(String fb : fbArr) fbList.add(fb.trim());
		
		for(String fb : fbList){
			if(!edmList.contains(fb)){
				differ += fb + " | ";
			}
		}
		
		if(!differ.equals("")) differ = differ.substring(0, differ.lastIndexOf("|"));
		return differ;
	}
	
	public void gatherData(String path, List<String> list, Map<String, Map<String, Map<String, Map<String, String>>>> fileMap){
		for(String filename : list){
			if(filename.contains(".xls")){
				try{
					InputStream is = new FileInputStream(path+filename);
					HSSFWorkbook wb = new HSSFWorkbook(is);
	
					Map<String, Map<String, Map<String, String>>> sheetMap = new HashMap<String, Map<String, Map<String, String>>>();
					for(int i=0 ; i<wb.getNumberOfSheets() ; i++){
						HSSFSheet sheet = wb.getSheetAt(i);
						String sheetName = sheet.getSheetName();

						if(!Arrays.asList(excludeSheetArr).contains(sheetName.trim())){
							log.info("Sheet: " + sheetName);
							Integer nameCol = 0;
							if(Arrays.asList(specialSheetArr).contains(sheetName)) nameCol = 1;
	
							Map<String, Map<String, String>> identMap = new HashMap<String, Map<String, String>>();
							for (int k=0 ; k<sheet.getLastRowNum()+1 ; k++){
								if(k>0){
									HSSFRow row = sheet.getRow(k);
									if(row != null && !isRowEmpty(row)){
										try{
											Map<String, String> colMap = new HashMap<String, String>();
											for(int j=0 ; j<row.getLastCellNum() ; j++){
												if(!sheetName.toLowerCase().contains("index_growth") && !sheetName.toLowerCase().contains("vol")){
													try{
														gatherNormalSheet(sheet, sheetName, row, nameCol, j, identMap, colMap);
													}catch(Exception e){
														log.error("Gathering normal sheet error", e);
														e.printStackTrace();
													}
												} else {
													try{
														gatherVolSheet(sheet, sheetName, row, nameCol, j, k, identMap, colMap);
													}catch(Exception e){
														log.error("Gathering vol sheet error", e);
														e.printStackTrace();
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
					is.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			} else if(filename.contains(".csv")){
				log.info("Sheet: " + filename);
				try{
					CSVReader reader = new CSVReader( new InputStreamReader(new FileInputStream(path+filename), "Big5"));
					List<String[]> dataArrList = reader.readAll();
					String[] hDataArr = dataArrList.get(0);
					
					Map<String, Map<String, Map<String, String>>> sheetMap = new HashMap<String, Map<String, Map<String, String>>>();
					Map<String, Map<String, String>> identMap = new HashMap<String, Map<String, String>>();
					for(int i=0 ; i<dataArrList.size() ; i++){
						if(i>0){
							String[] dataArr = dataArrList.get(i);
							String identifier = dataArr[0];
							if(!filename.contains("CorpBondSTATIC") && !filename.contains("GovtBondStatSpot")) identifier = dataArr[2];
							
							Map<String, String> colMap = new HashMap<String, String>();
							for(int idx=0 ; idx < dataArr.length ; idx++){
								try{
									String colName = null;
									try{colName = hDataArr[idx];}catch(Exception e){}
									if(colName != null){
//										if(Arrays.asList(posColArr).contains(colName.trim())){
											String value = dataArr[idx];
											if(value != null && !"".equals(value)){
												try{
													Date date = new TimeUtil().getDate(value, "yyyy/MM/dd");
													if(date != null) value = new TimeUtil().getDateFormat(date, "yyyy/MM/dd");
												}catch(Exception e){}
												
												colMap.put(colName, value);
												identMap.put(identifier, colMap);
											}
//										}
									}
								}catch(Exception e){
									e.printStackTrace();
								}
							}
						}
					}
					sheetMap.put(filename, identMap);
					fileMap.put(filename, sheetMap);
					reader.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
			
	}
	
	public void gatherNormalSheet(HSSFSheet sheet, String sheetName, HSSFRow row, Integer nameCol, Integer j, Map<String, Map<String, String>> identMap, Map<String, String> colMap){
		HSSFCell identCell = row.getCell(nameCol);
		if(identCell != null){
			String identifier = checkCellValue(identCell);
			
			HSSFCell cellCol = sheet.getRow(0).getCell(j);
			if(cellCol != null){
				String columnName = checkCellValue(cellCol);
				
				HSSFCell cell = row.getCell(j);
				if(cell != null){
					String value = checkCellValue(cell);
					
					if(value != null && !"".equals(value)){
						if(sheetName.equals("BondFuture_Option") || sheetName.equals("EURUSDFUT_Option")){
							if(Arrays.asList(optionColArr).contains(columnName.trim())){
								String underlying = getOptionKey(row, 7);
								String maturity = getOptionKey(row, 8);
								String putCall = getOptionKey(row, 10);
								
								String optIdentifier = underlying+ " " +maturity+ " " +putCall;
								if(identMap.get(optIdentifier) == null || identMap.get(optIdentifier).get(columnName) == null){
									colMap.put(columnName, value);
									identMap.put(optIdentifier, colMap);
								}
							}
						} else {
							colMap.put(columnName, value);
							identMap.put(identifier, colMap);
						}
					}
				}
			}
		}
	
	}
	
	public void gatherVolSheet(HSSFSheet sheet, String sheetName, HSSFRow row, Integer nameCol, Integer j, Integer k, Map<String, Map<String, String>> identMap, Map<String, String> colMap){

		if(sheetName.equals("Equity_Vol")){
			System.out.println();
		}
		
		String volName = null;
		try{volName = row.getCell(nameCol).toString();}catch(Exception e){}
		
		if(volName != null &&!"".equals(volName)){
			if(volName.equals("1419 Vol")){
				System.out.println();
			}
			
			HSSFCell topCellCol = sheet.getRow(0).getCell(j);
			if(topCellCol != null){
				String topColumnName = checkCellValue(topCellCol);
				
				if(!"".equals(topColumnName) && topColumnName != null){
					if(topColumnName.toLowerCase().equals("surface")){
						Integer totalVolCol = 17;
						Integer loopSize = 1;
						if(sheetName.toLowerCase().contains("swaption_vol")) loopSize = getRowNum(volName);
						
						for(int idx=0 ; idx<loopSize ; idx++){
							Integer rowNum = idx+k+1;
							HSSFCell cellRow = sheet.getRow(rowNum).getCell(j);
							String rowName = "";
							try{
								rowName = checkCellValue(cellRow);
							}catch(Exception e){}
							
							for(int volColIdx=0 ; volColIdx<totalVolCol ; volColIdx++){
								Integer colNum = volColIdx+j+1;
								HSSFCell cellCol = sheet.getRow(k).getCell(colNum);
								String columnName = "";
								try{
									columnName = checkCellValue(cellCol);
								}catch(Exception e){}
								
								HSSFCell cellVal = sheet.getRow(rowNum).getCell(colNum);
								if(cellVal != null){
									String value = checkCellValue(cellVal);

									if(value != null && !"".equals(value)){
										colMap.put(rowName+"&"+columnName, value);
										identMap.put(volName, colMap);
									}
								}
							}
						}
					} else {
						HSSFCell cellVal = row.getCell(j);
						if(cellVal != null){
							String value = checkCellValue(cellVal);
							
							if(value != null && !"".equals(value)){
								colMap.put(topColumnName, value);
								identMap.put(volName, colMap);
							}
						}
					}
				}
			}
		}
	
	}

	public String checkCellValue(Cell cell) {
		String result = "";
		try {
			
			switch (cell.getCellType()) {
	            case Cell.CELL_TYPE_STRING:
	                result = cell.getStringCellValue();
	                break;
	            case Cell.CELL_TYPE_NUMERIC:
	                if (DateUtil.isCellDateFormatted(cell)) {
	                	Date date = cell.getDateCellValue();
	                	result = new TimeUtil().getDateFormat(date, "yyyy/MM/dd");
	                } else {
	                	DecimalFormat df = new DecimalFormat("#.##########");
	                	Double d = cell.getNumericCellValue();
	                	result = df.format(d);
	                }
	                break;
	            case Cell.CELL_TYPE_BOOLEAN:
	            	cell.setCellType(Cell.CELL_TYPE_STRING);
	            	result = cell.getStringCellValue();
	                break;
	            case Cell.CELL_TYPE_FORMULA:
	            	cell.setCellType(Cell.CELL_TYPE_STRING);
	            	result = cell.getStringCellValue();
	                break;
	            default:
                // some code
			}
		} catch(Exception e) {
		
		}
		
		return result;
	}
	
	public String getOptionKey(HSSFRow row, Integer cellnum){
		String value = "";
		HSSFCell cell = row.getCell(cellnum);
		if(cell.getCellType() == Cell.CELL_TYPE_NUMERIC){
		    if (HSSFDateUtil.isCellDateFormatted(cell)) {
		    	Date date = cell.getDateCellValue();
		    	value = new TimeUtil().getDateFormat(date, "yyyy/MM/dd");
		    }
		}else{
			cell.setCellType(Cell.CELL_TYPE_STRING);
			value = cell.getStringCellValue();
			try{
				Date date = new TimeUtil().getDate(value, "yyyy/MM/dd");
				if(date != null) value = new TimeUtil().getDateFormat(date, "yyyy/MM/dd");
			}catch(Exception e){}
		}
		
		return value;
	}
	
	public static boolean isRowEmpty(Row row) {
	    for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
	        Cell cell = row.getCell(c);
	        if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK)
	            return false;
	    }
	    return true;
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
			
			case "CorpBondSTATIC":
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
