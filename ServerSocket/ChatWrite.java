package ServerSocket;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class ChatWrite {	//채팅 기록 저장 클래스
	static private BufferedWriter bw = null;
	static private int fDate, lDate;	// 날짜 비교 변수
	
	static boolean check() throws IOException{
		fDate = NowCalendar.dateSum();
		
		if(fDate > lDate){	//지금 날짜가 전애 날짜보다 크다면
			lDate = fDate;
			return true;
		}
		else 
			return false;
	}
		
	static void writeFile(String msg) throws IOException{
		try{
			if(check()){ 
				bw = new BufferedWriter(new FileWriter("..\\ChatContents\\" + 
											NowCalendar.nowDate() + " " +
											NowCalendar.nowTime() + "~.txt"));
			}
		
			bw.write(NowCalendar.nowTime() + " : " + msg);
			bw.newLine();
			bw.flush();
		}
		catch(IOException e) { bw.close(); }
	}
	
	static void closeWrite(){
		try {
			bw.close();
		} catch (IOException e) { e.printStackTrace(); }
	}
}
