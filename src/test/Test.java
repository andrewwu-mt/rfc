package test;

import java.util.HashMap;
import java.util.Map;


public class Test {

	public static void main(String[] args) {
		Map<String, Map<String, String>> map1 = new HashMap<String, Map<String, String>>();
		Map<String, String> map2 = new HashMap<String, String>();
		
		if(map1.get("test") == null || map1.get("test").get("test") == null){
			System.out.println("Empty");
		}
	}

}
