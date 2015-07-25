package com.rfc.action;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.opensymphony.xwork2.ActionSupport;
import com.rfc.bean.Compare;
import com.rfc.bean.UnZip;

public class CompareAction extends ActionSupport{
	private static final Logger log = Logger.getLogger(CompareAction.class);

	//upload file
	private List<File> fileUpload = new ArrayList<File>();
	private List<String> fileUploadContentType = new ArrayList<String>();
	private List<String> fileUploadFileName = new ArrayList<String>();
	
	public void checkFolder(String output_path){
		File outputPath = new File(output_path);
		if(!outputPath.exists()){
			outputPath.mkdirs();
		}
	}
	
	public String compareFile() {
		try {
			List<String> edmList = new ArrayList<String>();
			List<String> fbList = new ArrayList<String>();
			String input_path = "C:/rfc/upload/";
			
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
			
			new Compare().startCompare(input_path, edmList, fbList); //Start comparing
			
			
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

}
