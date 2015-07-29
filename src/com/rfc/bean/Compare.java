package com.rfc.bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import au.com.bytecode.opencsv.CSVReader;

public class Compare {
	private static final Logger log = Logger.getLogger(Compare.class);
	
	private String[] sheetArr = {"Constant_Term_Bullet_Bond", "Constant_Term_Swap_Fixed_Leg", "Bond_For_Future", "Fixed_Rate_Bond", "T_Bill", "Constant_Term_Forex_Forward", "Constant_Term_FRA", "Curve_Index ", "Interbank_Curve", "OIS_Curve", "Treasury_Curve", "Corporate_Curve", "Depo_Curve", "Repo_Curve", "Currency_Swap_Curve", "Forward_Curve", "FRA_Curve", "Index_Growth", "IRFuture", "Market_Index", "BondFuture", "Index_Volatility", "Foreign_Exchange", "Exchange_Rate", "FX_Converter", "FX_Vol_Moneyness_Term", "Bond_Vol", "CF_Vol", "Equity_Vol", "Swaption_Vol"};
	private String[] curArr = {"AED","AFN","ALL","AMD","ANG","AOA","ARS","AUD","AWG","AZN","BAM","BBD","BDT","BGN","BHD","BIF","BMD","BND","BOB","BRL","BSD","BTN","BWP","BYR","BZD","CAD","CDF","CHF","CLP","CNY","COP","CRC","CUC","CUP","CVE","CZK","DJF","DKK","DOP","DZD","EGP","ERN","ETB","EUR","FJD","FKP","GBP","GEL","GGP","GHS","GIP","GMD","GNF","GTQ","GYD","HKD","HNL","HRK","HTG","HUF","IDR","ILS","IMP","INR","IQD","IRR","ISK","JEP","JMD","JOD","JPY","KES","KGS","KHR","KMF","KPW","KRW","KWD","KYD","KZT","LAK","LBP","LKR","LRD","LSL","LTL","LYD","MAD","MDL","MGA","MKD","MMK","MNT","MOP","MRO","MUR","MVR","MWK","MXN","MYR","MZN","NAD","NGN","NIO","NOK","NPR","NZD","OMR","PAB","PEN","PGK","PHP","PKR","PLN","PYG","QAR","RON","RSD","RUB","RWF","SAR","SBD","SCR","SDG","SEK","SGD","SHP","SLL","SOS","SPL","SRD","STD","SVC","SYP","SZL","THB","TJS","TMT","TND","TOP","TRY","TTD","TVD","TWD","TZS","UAH","UGX","USD","UYU","UZS","VEF","VND","VUV","WST","XAF","XCD","XDR","XOF","XPF","YER","ZAR","ZMW","ZWD","ANY","GBp","ZAc"};
	private String[] colArr = {"ASE Transfer", "Business Day Rule", "Cashflow Output Currency", "Contract Size", "Currency", "Curve Unit", "Discount Curve", "Exchange Foreign Curve", "ExchangeCurve", "Foreign Curve", "Issue Date", "Maturity Date", "Net Basis", "Notional", "Procedure Parameter", "Repo Curve", "RiskMetrics Link", "Sliding Start Rule", "Sliding Term", "Strike Price", "Term", "Trade Day Rule", "Underlying", "Underlying Curve Index", "Underlying Maturity Date", "Unit", "Variable Notional", "UnifiedProductCode", "Cpn Typ", "Cpn", "Cpn Freq", "Refix Freq", "Cpn Crncy", "Crncy", "Day Cnt Des", "Issue Dt", "Maturity", "Callable"};
	private String[] priceArr = {"Spot Price"};
	private String[] specialSheetArr = {"T_Bill" , "Interbank_Curve" , "OIS_Curve", "Treasury_Curve" , "Corporate_Curve" , "Depo_Curve" , "Repo_Curve" , "Currency_Swap_Curve" , "Forward_Curve" , "FRA_Curve" , "Index_Growth" , "Index_Volatility" , "Exchange_Rate" , "FX_Converter" , "FX_Vol_Moneyness_Term" , "Bond_Vol" , "Equity_Vol" , "Swaption_Vol"};
	
	private String[] posColArr = {"UnifiedProductCode", "Cpn Typ", "Cpn", "Cpn Freq", "Refix Freq", "Cpn Crncy", "Crncy", "Day Cnt Des", "Issue Dt", "Maturity", "Callable"};
	
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
        
		for(String filename : edmFileMap.keySet()){
			Map<String, Map<String, Map<String, String>>> edmSheetMap = edmFileMap.get(filename);
			Map<String, Map<String, Map<String, String>>> fbSheetMap = fbFileMap.get(filename);
			
			if(edmSheetMap != null && fbSheetMap != null){
				Map<String, Map<String, Map<String, String>>> sheetMap = new HashMap<String, Map<String, Map<String, String>>>();
				for(String sheetName : edmSheetMap.keySet()){
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
											if(Arrays.asList(colArr).contains(colName.trim())){
												if(colName.trim().toLowerCase().contains("procedure parameter")){
													String differ = compareProcedureParameter(edmVal, fbVal);
													if(!differ.equals("")) colMap.put(colName, "Equal,N/A" + "," + edmVal+","+fbVal+","+differ);
												} else{
													if(!edmVal.equals(fbVal)) colMap.put(colName, "Equal,N/A" + "," + edmVal+","+fbVal+",Value inconsistent");
												}
											} else if(Arrays.asList(priceArr).contains(colName.trim())) {
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
						
						if(Arrays.asList(sheetArr).contains(sheetName.trim())){
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
													HSSFCell identCell = row.getCell(nameCol);
													if(identCell != null){
														identCell.setCellType(Cell.CELL_TYPE_STRING);
														String identifier = identCell.getStringCellValue();
														
														HSSFCell cellCol = sheet.getRow(0).getCell(j);
														if(cellCol != null){
															cellCol.setCellType(Cell.CELL_TYPE_STRING);
															String columnName = cellCol.getStringCellValue();
															
															HSSFCell cell = row.getCell(j);
															if(cell != null){
																cell.setCellType(Cell.CELL_TYPE_STRING);
																String value = cell.getStringCellValue();
																if(value != null && !"".equals(value)){
																	colMap.put(columnName, value);
																	identMap.put(identifier, colMap);
																}
															}
														}
													}
												} else {
													try{
														String volName = null;
														try{volName = row.getCell(nameCol).toString();}catch(Exception e){}
														
														if(volName != null &&!"".equals(volName)){
															HSSFCell topCellCol = sheet.getRow(0).getCell(j);
															if(topCellCol != null){
																topCellCol.setCellType(Cell.CELL_TYPE_STRING);
																String topColumnName = topCellCol.getStringCellValue();
																
																if(topColumnName.toLowerCase().equals("surface")){
																	Integer totalVolCol = 17;
																	Integer loopSize = 1;
																	if(sheetName.toLowerCase().contains("swaption_vol")) loopSize = getRowNum(volName);
																	
																	for(int idx=0 ; idx<loopSize ; idx++){
																		Integer rowNum = idx+k+1;
																		HSSFCell cellRow = sheet.getRow(rowNum).getCell(j);
																		String rowName = "";
																		try{
																			cellRow.setCellType(Cell.CELL_TYPE_STRING);
																			rowName = cellRow.getStringCellValue();
																		}catch(Exception e){}
																		
																		for(int volColIdx=0 ; volColIdx<totalVolCol ; volColIdx++){
																			Integer colNum = volColIdx+j+1;
																			HSSFCell cellCol = sheet.getRow(k).getCell(colNum);
																			String columnName = "";
																			try{
																				cellCol.setCellType(Cell.CELL_TYPE_STRING);
																				columnName = cellCol.getStringCellValue();
																			}catch(Exception e){}
																			
																			HSSFCell cellVal = sheet.getRow(rowNum).getCell(colNum);
																			if(cellVal != null){
																				cellVal.setCellType(Cell.CELL_TYPE_STRING);
																				String value = cellVal.getStringCellValue();
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
																		cellVal.setCellType(Cell.CELL_TYPE_STRING);
																		String value = cellVal.getStringCellValue();
																		if(value != null && !"".equals(value)){
																			colMap.put(topColumnName, value);
																			identMap.put(volName, colMap);
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
			} else if(filename.contains(".csv")){
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
							
							Map<String, String> colMap = new HashMap<String, String>();
							for(int idx=0 ; idx < dataArr.length ; idx++){
								String colname = hDataArr[idx];
								if(Arrays.asList(posColArr).contains(colname.trim())){
									String value = dataArr[idx];
									if(value != null && !"".equals(value)){
										colMap.put(colname, value);
										identMap.put(identifier, colMap);
									}
								}
							}
						}
					}
					sheetMap.put(filename, identMap);
					fileMap.put(filename, sheetMap);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
			
	}
	
	public static boolean isRowEmpty(Row row) {
	    for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
	        Cell cell = row.getCell(c);
	        if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK)
	            return false;
	    }
	    return true;
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
			
			case "CorpBondSTATIC":
				intArr = new Integer[] {4,5};	
				
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
