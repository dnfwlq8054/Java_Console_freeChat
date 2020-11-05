package ServerSocket;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class ChatLog {		//���� ���ӽ� log��� ����
	static private BufferedWriter log = null;
	static private int fDate, lDate;	//��¥ �� ����
	
	static boolean check() throws IOException{
		fDate = NowCalendar.dateSum();
		
		if(fDate > lDate){	//���� ��¥�� ���� ��¥���� ũ�ٸ�
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
