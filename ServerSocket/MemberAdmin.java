package ServerSocket;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Hashtable;

public class MemberAdmin implements Table{
	
	public MemberAdmin(){
		System.out.println("회원정보 읽는중...");
		readFile();
	}
	
	public void readFile(){	//AdminInfo.ser파일을 읽어오는 메소드
		try	{
			ObjectInputStream in = new ObjectInputStream(
					new FileInputStream(new File("..\\AdminRegister\\AdminInfo.ser")));
			while(true)	{
				AdminInfo e = (AdminInfo)in.readObject();
				if(e == null)
					break;
				e.online = false;
				hashTable.put(e.id, e);
			}	//while()		
			in.close();
		}
		catch(IOException e) {}
		catch(ClassNotFoundException e)	{ return; }
		System.out.println("회원정보 읽기 완료!!");
	}	//readFile()
}	//MemberAdmin class

class AdminInfo implements Serializable{	//관리자 정보를 담은 클래스
	String id;	//관리자 id
	String password;	//관리자 pw
	String name;	//관리자 이름
	boolean online = false;	//관리자가 접속중인지 접속중이지 않은지 판단하는 변수
	
	AdminInfo(String id, String password, String name){
		this.id = id;
		this.password = password;
		this.name = name;
	}
}