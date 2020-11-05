package ServerSocket;

import java.util.Calendar;

public class NowCalendar {
	static Calendar c;
	
	static String nowDate(){	//오늘 날짜 리턴
		c = Calendar.getInstance();
		String date = c.get(Calendar.YEAR) + "-";
		
		int month = (c.get(Calendar.MONTH)+1); 
		int day = c.get(Calendar.DATE);
		
		if(month < 10)
			date+= "0" + month;
		else
			date+= "-" + month;
		
		if(day < 10)
			date+= "-0" + day;
		else
			date+= "-" + day;
		return date;
	}
	
	static int dateSum(){	//날짜들의 합을 리턴
		c = Calendar.getInstance();
		int date = (c.get(Calendar.YEAR) + c.get(Calendar.MONTH) + 
				c.get(Calendar.DATE));
		
		return date;
	}
	
	static String nowTime(){	//현재 시간을 리턴
		c = Calendar.getInstance();
		String time = c.get(Calendar.HOUR_OF_DAY) + "시 "
				+ "";
		
		int minute = c.get(Calendar.MINUTE); 
		int second = c.get(Calendar.SECOND);
		
		if(minute < 10)
			time+= "0" + minute + "분 ";
		else
			time+= minute + "분 ";
		
		if(second < 10)
			time+= "0"+ second + "초";
		else
			time+= second + "초";
		
		return time;
	}
}
