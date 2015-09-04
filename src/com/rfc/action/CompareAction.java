package com.rfc.action;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.opensymphony.xwork2.ActionSupport;
import com.rfc.bean.Compare;
import com.rfc.bean.LoadProperties;
import com.rfc.bean.UnZip;

public class CompareAction extends ActionSupport{
	private static final Logger log = Logger.getLogger(CompareAction.class);

	//upload file
	private List<File> fileUpload = new ArrayList<File>();
	private List<String> fileUploadContentType = new ArrayList<String>();
	private List<String> fileUploadFileName = new ArrayList<String>();
	
	private String evalDate;
	private String name;
	
	public void checkFolder(String output_path){
		File outputPath = new File(output_path);
		if(!outputPath.exists()){
			outputPath.mkdirs();
		}
	}
	
	public String deleteFile(){
		try{
			String output_path = new LoadProperties().load("output.path");
			File file = new File(output_path, name);
			FileUtils.deleteQuietly(file);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return "successdelete";
	}
	
	public String compareFile() {
		try {
			List<String> edmList = new ArrayList<String>();
			List<String> fbList = new ArrayList<String>();
			String input_path = new LoadProperties().load("upload.path");
			
			for(int i=0 ; i<fileUpload.size() ; i++){
				String dataType = "EDM/";
				if(i == 1) dataType = "FB/";
				
				checkFolder(input_path);
				String filename = fileUploadFileName.get(i);
				
				File file = new File(input_path, filename);
				FileUtils.copyFile(fileUpload.get(i), file);
				UnZip uz = new UnZip();
				List<String> filenameList = uz.unZipIt(input_path + filename, input_path+dataType);
				if(i==1){
					fbList.addAll(filenameList);
				} else {
					edmList.addAll(filenameList);
				}
				
				FileUtils.deleteQuietly(file); //Delete zip file
			}
			
			new Compare().startCompare(input_path, edmList, fbList, evalDate); //Start comparing

			//Delete uploaded files
			File pathFolder = new File(input_path);
			FileUtils.deleteDirectory(pathFolder);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return INPUT;
		}
		return SUCCESS;
	}

	public List<File> getFileUpload() {
		return fileUpload;
	}

	public void setFileUpload(List<File> fileUpload) {
		this.fileUpload = fileUpload;
	}

	public List<String> getFileUploadContentType() {
		return fileUploadContentType;
	}

	public void setFileUploadContentType(List<String> fileUploadContentType) {
		this.fileUploadContentType = fileUploadContentType;
	}

	public List<String> getFileUploadFileName() {
		return fileUploadFileName;
	}

	public void setFileUploadFileName(List<String> fileUploadFileName) {
		this.fileUploadFileName = fileUploadFileName;
	}

	public String getEvalDate() {
		return evalDate;
	}

	public void setEvalDate(String evalDate) {
		this.evalDate = evalDate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
