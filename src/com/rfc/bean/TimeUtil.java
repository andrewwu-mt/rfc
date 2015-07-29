package com.rfc.bean;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class TimeUtil {
	
	private final String[] simpleFormat = {
		"%D,yyyyMMdd",
		"%Y,yyyy",
		"%M,MM",
		"%B,dd",
		"%H,HH",
		"%m,mm",
		"%S,ss",
		"%T,HHmmss",
		"%d,dd"
	};
	
	private SimpleDateFormat df;
	
	public String replaceStringTime(String str) {
		Date date = new Date();
		for(int i = 0; i < simpleFormat.length; i++) {
			String[] reStr = simpleFormat[i].split(",");
			df = new SimpleDateFormat(reStr[1]);
			str = str.replace(reStr[0], df.format(date));
		}
		return str;
	}
	
	public String removeTimeCode(String str) {
		for(int i = 0; i < simpleFormat.length; i++) {
			String[] reStr = simpleFormat[i].split(",");
			str = str.replace(reStr[0], "");
		}
		return str;
	}
	
	public String getDateFormat(String yyyyMMdd, String format) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
		SimpleDateFormat df2 = new SimpleDateFormat(format);
		Date date = new Date();
		try {
			date = df.parse(yyyyMMdd);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return df2.format(date);
	}
	
	public String getDateFormat(String yyyyMMdd, String inputFormat, String outputFormat) {
		SimpleDateFormat df = new SimpleDateFormat(inputFormat);
		SimpleDateFormat df2 = new SimpleDateFormat(outputFormat);
		Date date = new Date();
		try {
			date = df.parse(yyyyMMdd);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return df2.format(date);
	}
	
	public String getDateFormat(Long yyyyMMdd, String format) {
		SimpleDateFormat df2 = new SimpleDateFormat(format);
		Date date = new Date(yyyyMMdd);
		return df2.format(date);
	}
	
	public String getDateFormat(Date yyyyMMdd, String format) {
		SimpleDateFormat df2 = new SimpleDateFormat(format);
		return df2.format(yyyyMMdd);
	}
	
	public Date getTradeDateFormat(String stringTime, Map<String, String> propMap) {
		String[] dformat = propMap.get("trade_date_format").replaceAll(" ", "").split(",");
		Date date = new Date();
		for(int i = 0; i < dformat.length; i++) {
			if(checkFormat(stringTime, dformat[i])) {
				SimpleDateFormat df = new SimpleDateFormat(dformat[i]);
				try {
					date = df.parse(stringTime);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
				}
				break;
			}
		}
		return date;
	}
	
	private boolean checkFormat(String stringTime, String format) {
		boolean isResult = false;
		SimpleDateFormat df = new SimpleDateFormat(format);
		Date date = new Date();
		try {
			date = df.parse(stringTime);
			isResult = true;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			return false;
		}
		return isResult;
	}
	
	public Date getDate(String stringTime, String format) {
		Date date = new Date();
		if(checkFormat(stringTime, format)) {
			SimpleDateFormat df = new SimpleDateFormat(format);
			try {
				date = df.parse(stringTime);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
			}
			return date;
		} else {
			return null;
		}
	}
	
	public Date getDate2(String stringTime, String format) throws ParseException {
		SimpleDateFormat df = new SimpleDateFormat(format);
		return df.parse(stringTime);
	}
	
	public String getHour(Date date) {
		df = new SimpleDateFormat("HH");
		return df.format(date);
	}
	
	public String getMinute(Date date) {
		df = new SimpleDateFormat("mm");
		return df.format(date);
	}
	
	public String getHour(Long time) {
		df = new SimpleDateFormat("HH");
		return df.format(time);
	}
	
	public String getMinute(Long time) {
		df = new SimpleDateFormat("mm");
		return df.format(time);
	}
	
	public Integer calcDateCount(String time1, String time2, String format) {
		
		Long t1 = null;
		Long t2 = null;
		try {
			t1 = getDate2(time1, format).getTime();
			t2 = getDate2(time2, format).getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Long t = null;
		if(t1 != null && t2 != null) {
			t = t1 - t2;
			long time = 1000 * 3600 * 24;
			
			t = t / time;
		}
		
		return t.intValue();
	}
	
	public boolean checkWeekAndDate(Integer checkWeekOfMonth, Integer checkDayOfWeek, String dateTime, String format) {
		DateFormat df = new SimpleDateFormat(format);
		
		Date date = new Date();
		
		try {
			date = df.parse(dateTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		Calendar c = Calendar.getInstance();
		
		c.setTime(date);
		
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
		int weekOfMonth = c.get(Calendar.WEEK_OF_MONTH);
		
		//System.out.println("Week: " + weekOfMonth + " Day: " + dayOfWeek);
		
		if(dayOfWeek == checkDayOfWeek + 1 && weekOfMonth == checkWeekOfMonth) {
			return true;
		}
		
		return false;
	}
	
	public String getDayByYearAndMonth(String dateTime, Integer week, Integer day, String format, String outFormat) {
		String[] days = new String[5];
		
		DateFormat df = new SimpleDateFormat(format);
		
		Date date = new Date();
		
		try {
			date = df.parse(dateTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		String tempDate = new TimeUtil().getDateFormat(date, outFormat);
		
		Integer chkMM = getDateMonthInt(tempDate);
		
		Integer idx = 0;
		
		for(int i = 1; i <= 5; i++) {
			
			String result = getDayByYearAndMonth2(dateTime, i, day, format, outFormat);
			
			Integer mm = getDateMonthInt(result);
			
			if(chkMM.equals(mm)) {
				days[idx] = result;
				idx++;
			}
		}
		
		return days[day - 1];
	}
	
	public Integer getDateMonthInt(String time) {
		String mm = time.substring(time.indexOf("/")+1, time.lastIndexOf("/"));
		//System.out.println(mDate);
		return Integer.valueOf(mm);
	}
	
	public String getDayByYearAndMonth2(String dateTime, Integer week, Integer day, String format, String outFormat) {
		DateFormat df = new SimpleDateFormat(format);
		
		Date date = new Date();
		
		try {
			date = df.parse(dateTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		//System.out.println(date);
		
		Calendar c = Calendar.getInstance();

		c.setTime(date);
		
		c.set(Calendar.WEEK_OF_MONTH, week); 
		c.set(Calendar.DAY_OF_WEEK, day + 1);
		
		date = c.getTime();
		
		df = new SimpleDateFormat(outFormat);
		
		return df.format(date);
	}
}
