package ServerSocket;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Scanner;
import java.util.StringTokenizer;

class SocketAccept extends Thread{		//Ŭ���̾�Ʈ ���� �ޱ�
	private ServerMenu server = new ServerMenu();
	// ClientManagements client = new ClientManagements();
	
	public void run(){
		ServerSocket serverSocket = null;	//���� ����
		try{
			serverSocket = new ServerSocket(9000);	//���Ͽ� ��Ʈ��ȣ �ο�(bind)
			System.out.println("������ �غ�Ǿ����ϴ�.");
			System.out.println("������ ���Ƿ��� '/help'�� �Է����ּ���.");
			
			while(true){
				//	accept�Լ��� ������ �ö����� ������ ����Ѵ�.
				Socket socket = serverSocket.accept();	//���� ���� ���� �Լ�(accept)
				System.out.println(socket.getInetAddress() + "�κ��� ��û�� ���Խ��ϴ�.");
		
				//	client.threadIn(socket);
				server.init(socket);
				server.start();
			}
		}
		catch(IOException e){
			System.out.println("������ �ϳ��� �����մϴ�..");
			System.exit(-1);
		}	//catch()
	}	//run()
}	//SocetAccept class

class ServerInput extends Thread implements Table{	//�������� ó���� �� �ִ� ���
	private Scanner sc = new Scanner(System.in);
	private StringTokenizer st;	
	private String tok;
	
	void showHelp() {		//����
		String[] help = {
				"1. ȸ�������� ������ '/mbinfo'�� �Է��ϸ� �˴ϴ�.",
				"2. ȸ������ ������ '/del [ID]'�Դϴ�.",
				"3. ������ ����� '/list'�� �Է��ϸ� �˴ϴ�.",
				"4. �������� �̸��� �����Ϸ��� '/rename [������ ID] [�Ƹ�]�� �Է��Ͻø� �˴ϴ�.",
				"5. ��������� '/poweroff'�� �Է��Ͻø� ���ϴ�."
		};
		
		for(int i = 0; i < help.length; i++)
			System.out.println(help[i]);
	}	//showHelp()
	
	void saveInfo(){	//���� ������ �ش� ����� ����
		try{
			ObjectOutputStream out = new ObjectOutputStream(
					new FileOutputStream(new File("..\\AdminRegister\\AdminInfo.ser")));
			
			Enumeration<AdminInfo> en = MemberAdmin.hashTable.elements();
			
			while(en.hasMoreElements()){
				AdminInfo e = (AdminInfo)en.nextElement();
				out.writeObject(e);
			}
			out.close();
		}	//try
		catch(IOException e){ e.printStackTrace(); }
	} //saveInfo
	
	void showList() {	//�����ڵ��� ID����Ʈ �����ִ� ���
			int count=0;
			Iterator itr = ClientManagements.clientTable.iterator();
			Iterator itr2 = AdminManagements.adminTable.iterator();
			
			System.out.println("====���� ������ ������ ����Ʈ=====");
			while(itr2.hasNext()){
				AdminThread t1 = (AdminThread)itr2.next();
					System.out.println((++count) + " : " + t1.name);
			}
			
			count=0;
			System.out.println("====���� ���� ������ ����Ʈ=====");
			while(itr.hasNext()){
				ClientThread t = (ClientThread)itr.next();
				System.out.println((++count) + " : " + t.name);
			}	//while()
	}	//showList()
	
	void memberInfo(){		//����� �����ڵ鿡 ���� ���� ���
		Enumeration<AdminInfo> en = MemberAdmin.hashTable.elements();
		System.out.println("====������ ����====");
		while(en.hasMoreElements()){
			AdminInfo e = (AdminInfo)en.nextElement();
			System.out.println("==============");
			System.out.println("�̸� : " + e.name);
			System.out.println("id : " + e.id);
			System.out.println("password : " + e.password);
			System.out.println("==============");	
		}
	}	//memberInfo
	
	void deleteAdmin(String id){	//������ ���� ����
		if(MemberAdmin.hashTable.containsKey(id)){
			MemberAdmin.hashTable.remove(id);
			System.out.println("���� �Ϸ�!!");
			saveInfo();
		}
		else
			System.out.println("ã�� id���� �����ϴ�...");
		
	}//deletAdmin()
	
	void showIP_List(){		//���� �������� �������� ip�ּ� ���
		Iterator itr = ClientManagements.clientTable.iterator();
		Iterator itr2 = AdminManagements.adminTable.iterator();
		
		System.out.println("==========������ IP���==========");
		while(itr2.hasNext()){
			AdminThread t1 = (AdminThread)itr2.next();
			System.out.println(t1.name + "�� IP : " + t1.socket.getInetAddress());
		}
		
		System.out.println("=========�Ϲ� ���� IP���=========");
		while(itr.hasNext()){
				ClientThread t = (ClientThread)itr.next();
				System.out.println(t.name + "�� IP : " + t.socket.getInetAddress());
		}
		System.out.println("============================");
	}
	
	@Override
	public void run(){
		String in, id;
		
		while(true){
			in = sc.nextLine();
			if(in.length() == 0)	//����Ű ���ӹ���
				continue;
			
			st = new StringTokenizer(in," ");
			tok = st.nextToken();
			
			if(tok.equals("/help"))	//����
				showHelp();
			
			else if(tok.equals("/mbinfo"))	//�����ڵ� ȸ������
				memberInfo();
			
			else if(tok.equals("/list"))	//������������ ����
				showList();
			
			else if(tok.equals("/del")){	//������ ȸ������ ����
				if(!st.hasMoreTokens()){	//id�� ��ù�� ���
					System.out.println("id�� �Է��ϼ���...");
					continue;
				}
				deleteAdmin(st.nextToken());
			}
			
			else if(tok.equals("/ip"))	//�������� �������� ip�ּ� Ȯ��
				showIP_List();
			
			else if(tok.equals("/rename")){		//������ ȸ������ �̸� �ٲ��ֱ�
				if(!st.hasMoreTokens()){		//id�� ��ù�� ���
					System.out.println("�ش� id�� �Է��ϼ���...");
					continue;
				}
				else{
					tok = st.nextToken();
					if(MemberAdmin.hashTable.containsKey(tok)){
						if(!st.hasMoreTokens())	//�̸��� ��ù�� ���
							System.out.println("�ٲ��̸��� ��Ȯ�� �Է��� �ּ���...");
						else{
							MemberAdmin.hashTable.get(tok).name = st.nextToken();
							System.out.println("�̸� �ٲٱ� ����!!");
						}
					}
					else
						System.out.println("�ش� ID�� �����ϴ�...");
				}
			}	//rename
				
			else if(tok.equals("/poweroff")){	//���� ����
				saveInfo();
				try {
					ChatLog.writeLog("=======ä������======");
					ChatWrite.writeFile("======���� �ٿ�=====");
				} catch (IOException e) { }
				ChatLog.closeLog();
				ChatWrite.closeWrite();
				System.out.println("������ ����..");
				System.out.println("���� ����..");
				System.exit(1);
			}
		}
	}	//run()
}// ServerInput class

//************************** main *************************//
public class MainServer {
	
	public static void main(String[] args) {
		System.out.println("���� �غ����Դϴ�. ��ø� ��ٷ��ּ���..");
		new MemberAdmin();		//���� �о����
		SocketAccept socketAccept = new SocketAccept();
		ServerInput serverInput = new ServerInput();
		socketAccept.start();	//Ŭ���̾�Ʈ ������ �޴� ������ ����
		serverInput.start();	//���� ����� �� �� �ִ� ������ ����
		return;
		
	}	//main()
}	//MainServer