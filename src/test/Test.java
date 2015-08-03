package test;


public class Test {

	public static void main(String[] args) {
		String str = "12.42423 SMP Actual/360";
		str = str.replaceAll("[^\\d.]", "");
		
		System.out.println(str);
	}

}
