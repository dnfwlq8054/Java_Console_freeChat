package ServerSocket;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class ChatWrite {	//ä�� ��� ���� Ŭ����
	static private BufferedWriter bw = null;
	static private int fDate, lDate;	// ��¥ �� ����
	
	static boolean check() throws IOException{
		fDate = NowCalendar.dateSum();
		
		if(fDate > lDate){	//���� ��¥�� ���� ��¥���� ũ�ٸ�
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
