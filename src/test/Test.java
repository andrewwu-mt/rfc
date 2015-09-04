package test;

import java.io.File;

import org.apache.commons.io.FileUtils;


public class Test {

	public static void main(String[] args) {
		File file = new File("C:/rfc/upload/FB/");
		try{
			FileUtils.deleteDirectory(file);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
