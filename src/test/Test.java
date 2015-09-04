package test;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;


public class Test {

	public static void main(String[] args) throws IOException {
		File file = new File("C:/ar/output/log/");
		FileUtils.deleteDirectory(file);
	}

}
