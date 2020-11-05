package ServerSocket;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class ChatLog {		//서버 접속시 log기록 저장
	static private BufferedWriter log = null;
	static private int fDate, lDate;	//날짜 비교 변수
	
	static boolean check() throws IOException{
		fDate = NowCalendar.dateSum();
		
		if(fDate > lDate){	//현재 날짜가 전에 날짜보다 크다면
			lDate = fDate;
			return true;
		}
		else 
			return false;
	}
	
	static void writeLog(String msg) throws IOException{
		try{
			if(check()){
				log = new BufferedWriter(new FileWriter("..\\Log\\" + 
											NowCalendar.nowDate() + " " +
											NowCalendar.nowTime() + "~.txt"));
			}
		
			log.write(NowCalendar.nowTime() + " : " + msg);
			log.newLine();
			log.flush();
		}
		catch(IOException e) { log.close(); }
	}
	
	static void closeLog() {
		try {
			log.close();
		} catch (IOException e) { e.printStackTrace(); }
	}
}
