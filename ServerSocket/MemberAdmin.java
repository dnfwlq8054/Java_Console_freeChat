package ServerSocket;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Hashtable;

public class MemberAdmin implements Table{
	
	public MemberAdmin(){
		System.out.println("ȸ������ �д���...");
		readFile();
	}
	
	public void readFile(){	//AdminInfo.ser������ �о���� �޼ҵ�
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
		System.out.println("ȸ������ �б� �Ϸ�!!");
	}	//readFile()
}	//MemberAdmin class

class AdminInfo implements Serializable{	//������ ������ ���� Ŭ����
	String id;	//������ id
	String password;	//������ pw
	String name;	//������ �̸�
	boolean online = false;	//�����ڰ� ���������� ���������� ������ �Ǵ��ϴ� ����
	
	AdminInfo(String id, String password, String name){
		this.id = id;
		this.password = password;
		this.name = name;
	}
}