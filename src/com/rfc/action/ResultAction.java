package com.rfc.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionSupport;
import com.rfc.bean.LoadProperties;

public class ResultAction extends ActionSupport{

	private String fileName;
	private InputStream downloadFile;
	private Long contentLength;
	
	public String allRecords(){
		HttpServletRequest request = ServletActionContext.getRequest();
		
		String output_path = new LoadProperties().load("output.path");
		File folder = new File(output_path);
		File[] listOfFiles = folder.listFiles();
		
		List<String> filenameList = new ArrayList<String>();
	    for (int i = 0; i < listOfFiles.length; i++) {
	    	filenameList.add(listOfFiles[i].getName());
	    }
		
	    request.setAttribute("filenameList", filenameList);
		return SUCCESS;
	} 
	

	public String downloadFile() {
		try {
			String output_path = new LoadProperties().load("output.path");
			File fileToDownload = new File(output_path + fileName);
			downloadFile = new FileInputStream(fileToDownload);
	        contentLength = fileToDownload.length();
	        
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return SUCCESS;
	}


	public String getFileName() {
		return fileName;
	}


	public void setFileName(String fileName) {
		this.fileName = fileName;
	}


	public InputStream getDownloadFile() {
		return downloadFile;
	}

	public void setDownloadFile(InputStream downloadFile) {
		this.downloadFile = downloadFile;
	}
	
}
