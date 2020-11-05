package ServerSocket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class ClientManagements implements Table{
	//���������� �����ؼ� �����尣�� ������ �����ϵ��� ��.
	private ClientThread chatThread;

	public void threadIn(Socket socket) throws IOException {	
		chatThread = new ClientThread(socket);
		ClientManagements.clientTable.add(chatThread);	//���Ϳ� chatThread �߰�
		chatThread.start(); //������ ����
	}	//threadIn()
}	//ClientManagements

class ClientThread extends Thread {
	static private int num = 1;		//������ ��ȣ
	String name = "user" + (num++);	//�ʱ��̸� ����
	private String msg, command;	//�޽����� �޽���ó�� ��ä
	final Socket socket;			//����
	final DataInputStream in;		//�Է� ��Ʈ��
	final DataOutputStream out;	//��� ��Ʈ��
	private final ReentrantLock key = new ReentrantLock();	//key����
	private StringTokenizer st;		//�޽��� token ��ä
	boolean tolkOn = true;	//��� ����
	
	ClientThread(Socket socket) throws IOException{	//��Ʈ�� �ʱ�ȭ
		this.socket = socket;
		this.in = new DataInputStream(socket.getInputStream());
		this.out = new DataOutputStream(socket.getOutputStream());
	}	//ClientTHread Constructor
	
	void sendToAll(String msg) {		//�����޽����� ��� Ŭ���̾�Ʈ�鿡�� �ѷ��ֱ�
		//Iterator �������̽��� �̿��ؼ� HashSet�� �ִ� �ڷ�˻��� �Ѵ�.
		Iterator itr = ClientManagements.clientTable.iterator();
		Iterator itr2 = AdminManagements.adminTable.iterator();
		
		while(itr2.hasNext()){		//�����ڿ��� �޽��� ����
			AdminThread t1 = (AdminThread)itr2.next();
			try {
				t1.out.writeUTF(this.name + " : " + msg);
			} 
			catch (IOException e) { 
				e.printStackTrace(); 
				System.out.println("�޽��� �۽� ����..!");
			}
		}
		
		while(itr.hasNext()){		//Ŭ���̾�Ʈ���� �޽��� ����
			try{
				ClientThread t = (ClientThread)itr.next();
				if(t.equals(this))	//�ڽ��� ģ ä���� �ٽù��� �ʿ䰡 ���⶧����
					continue;
				else
					t.out.writeUTF(this.name + " : " + msg);
			}
			catch(IOException e){ 
				e.printStackTrace(); 
				System.out.println("�޽��� �۽� ����..!");
			}
		}	//while()
	}	//sendToAll()
	
	void showHelp() throws IOException{		//����
		String[] help = {
				"1. ID������ '/id ����� ���̵�' �Է��ϸ� �˴ϴ�.",
				"2. ����� ����� '/list'�� �Է��ϸ� �˴ϴ�.",
				"3. �ӼӸ��� '/w �������̵� �Ҹ�'�� �Է��Ͻø� �˴ϴ�.",
				"4. ���� �ð��� Ȯ���ϴ� ����� '/time'�� �Է��Ͻø� �˴ϴ�.",
				"5. ä�ù� ������ '/exit'�� �Է��Ͻø� ���ϴ�.",
		};
		
		for(int i = 0; i < help.length; i++)
			out.writeUTF(help[i]);
	}	//showHelp()
	
	void searchId(String id) throws IOException{	//ID�ߺ��˻�
		out.writeUTF("ID�ߺ� �˻���....");	
		String adminId = "admin";
		String beforeId, log;
		
		if(id.contains(adminId)){
			out.writeUTF("admin���̵�� ����Ͻ� �� �����ϴ�..");
			return;
		}
		
		key.lock();	//��������� ���� ���� �� ���̵� �ߺ��� �� Ȯ���� �ֱ� ������ ReentrantLockŬ������ lock()�޼ҵ�� ���� ������ ����
		try{
			beforeId = this.name;
			
			Iterator itr = ClientManagements.clientTable.iterator();
			Iterator itr2 = AdminManagements.adminTable.iterator();
			
			while(itr2.hasNext()){	//������ ���̵� �˻�
				AdminThread t1 = (AdminThread)itr2.next();		
				
				if(id.equals(t1.name)){	//���̵� ������ ����
					out.writeUTF("�ߺ��Ǵ� ���̵� �ֽ��ϴ�...");
					return;		
				}	//if()
			}	//while()
			
			while(itr.hasNext()){	//���� ���̵� �˻�
				ClientThread t = (ClientThread)itr.next();					
				
				if(id.equals(t.name)){	//���̵� ������ ����
					out.writeUTF("�ߺ��Ǵ� ���̵� �ֽ��ϴ�...");
					return;
				}	//if()
			}	//while()
			
			out.writeUTF("���̵� ���� �Ϸ�...");
			this.name = id;		//�ߺ� �˻簡 �Ϸ�Ǹ� ID����
			log = "[�Ϲ�����] \"" + beforeId + "\"(" + socket.getInetAddress() + ") �� ���̵� "
					+ this.name + "���� �ٲ�����ϴ�.";
			try { ChatLog.writeLog(log); } catch (IOException e) { System.out.println("Log���� ���� ����(ID)");}
		}	//try()
		catch(IOException e){
			System.out.println("search��� ����..");
			out.writeUTF("���̵� �ߺ��˻� ����..");
		}
		finally{ key.unlock(); }	//lock Ǯ��
	}	//search()
	
	void list() throws IOException{	//�����ڵ��� ID����Ʈ �����ִ� ���
		try{
			int count=0;
			Iterator itr = ClientManagements.clientTable.iterator();
			Iterator itr2 = AdminManagements.adminTable.iterator();
			
			out.writeUTF("====���� ������ ������ ����Ʈ=====");
			while(itr2.hasNext()){
				AdminThread t1 = (AdminThread)itr2.next();
				out.writeUTF((++count) + " : " + t1.name);
			}
			
			count=0;
			out.writeUTF("====���� ���� ������ ����Ʈ=====");
			while(itr.hasNext()){
				ClientThread t = (ClientThread)itr.next();
				if(t.equals(this))
					out.writeUTF((++count) + " : " + t.name + "<---�� ID�� ��");
				else
					out.writeUTF((++count) + " : " + t.name);
			}	//while()
		}	//try()	
		catch(IOException e){
			System.out.println("����Ʈ��� ����!!");
			out.writeUTF("����Ʈ ��� ����!!");
		}	//catch()
	}	//list()
	
	void send(String id, String msg) throws IOException{	//�ӼӸ�
		try{
			Iterator itr = ClientManagements.clientTable.iterator();
			Iterator itr2 = AdminManagements.adminTable.iterator();
			
			while(itr.hasNext()){
				AdminThread t1 = (AdminThread)itr2.next();	
				
				if(id.equals(t1.name)){	//�Է��� ���̵� ������ �����ڿ��� �޽��� ����
					ChatWrite.writeFile(this.name + " >>>>> " + t1.name + msg + "(�ӼӸ�)");
					t1.out.writeUTF(this.name + "���� �� �ӼӸ� :" + msg);
					return;
				}	//if()
			}	//while()
			
			while(itr.hasNext()){
				ClientThread t = (ClientThread)itr.next();					
				
				if(id.equals(t.name)){	//�Է��� ���̵� ������ �Ϲ� �������� �޽��� ����
					ChatWrite.writeFile(this.name + " >>>>> " + t.name + msg + "(�ӼӸ�)");
					t.out.writeUTF(this.name + "���� �� �ӼӸ� :" + msg);
					return;
				}	//if()
			}	//while()
			out.writeUTF("�ش� ���̵� �����ϴ�...");
		}	//try()
		catch(IOException e){
			System.out.println("send��� ����..");
			out.writeUTF("�ӼӸ� ����..");
		}
	}
	
	void realTime() throws IOException{		//���� �ð� �����ֱ�
		String date = NowCalendar.nowDate();
		String time = NowCalendar.nowTime();
		
		out.writeUTF("���� ��¥ : " + date);	
		out.writeUTF("���� �ð� : " + time);
	}
	
	void tolkOnOff(){	//����� Ǯ���� Ÿ�� ���� �޼ҵ�
		Timer timer = new Timer();
		TimerTask m_task = new TimerTask(){	//��ϸӽ� Ŭ������ �ٷ� ���� �߽��ϴ�.
		
			public void run(){
				tolkOn = true;
				timer.cancel();
			}
		};
		timer.schedule(m_task, 30000);	//30�� �� Ÿ�̸� ����
	}
	
	void exit() {	//ä�� ���� 
		try {
			Iterator itr = ClientManagements.clientTable.iterator();
			Iterator itr2 = AdminManagements.adminTable.iterator();
			
			while(itr2.hasNext()){
				AdminThread t1 = (AdminThread)itr2.next();
					t1.out.writeUTF(this.name + "���� �����߽��ϴ�...");
			}
			
			while(itr.hasNext()){
					ClientThread t = (ClientThread)itr.next();
					if(t.equals(this))	//�ڽ��� ģ ä���� �ٽù��� �ʿ䰡 ���⶧����
						continue;
					else
						t.out.writeUTF(this.name + "���� �����߽��ϴ�...");
			}
		} catch (IOException e) { System.out.println("���� ��� ����..!"); 	}
	}
	
	@Override
	public void run(){
		String log;	//�Է¹��� ���� üũ
	
		try {
			log = socket.getInetAddress() + 
					" " + this.name + "[�Ϲ�����]�� �����߽��ϴ�...";
			System.out.println(log);
			ChatLog.writeLog(log);
			System.out.println("���� ������ ���� ���� : " + AdminManagements.adminTable.size()
			+ "�� �Դϴ�.");
			System.out.println("���� �Ϲ� ������ ���� : " + ClientManagements.clientTable.size()
								+ "�� �Դϴ�.");
			out.writeUTF("ä�ü����� �����ϼŽ��ϴ�...");
			out.writeUTF("������ '/help' �� �Է��ϼ���...");
			
			while(true){
				msg = in.readUTF();
				 
				if(!tolkOn){	//��� �˻�
					this.out.writeUTF("������ ��� �����Դϴ�.!!");
					tolkOnOff();
					continue;
				}
				st = new StringTokenizer(msg," ");
				command = st.nextToken();

				if(command.equals("/help"))		//����
					showHelp();
				
				else if(command.equals("/id")){		//id����
					if(!st.hasMoreTokens()){	//id�� ��ù�� ���
						this.out.writeUTF("id�� �Է��ϼ���...");
						continue;
					}
					else
						searchId(st.nextToken());
				} //searchId
				
				else if(command.equals("/list"))	//���� ���
					list();
				
				else if(command.equals("/time"))	//���� �ð� ���
					realTime();
				
				else if(command.equals("/w")){	//�ӼӸ�
					if(!st.hasMoreTokens()){	//id�� ��ú�� ���
						this.out.writeUTF("[id] [�� ��]�� �Է��ϼ���...");
						continue;
					}
					else{
						command = st.nextToken();
						if(!st.hasMoreTokens()){	//id�� ù�´� �Ҹ��� ��ù�� ���
							out.writeUTF("[id] [�� ��]�� �Է��ϼ���...");
							continue;
						}
						else
							send(command, st.nextToken(""));
					}
				}	//send
				
				else if(command.equals("/exit"))	//����
					break;
				
				else{		//��ȭ���� �Ѹ���
					ChatWrite.writeFile("[" + name + "] " + msg);
					sendToAll(msg);
				}
			}	//while()
		} 	//try
		catch (IOException e) {} 
		finally{
			exit();
			ClientManagements.clientTable.remove(this);
			try { this.socket.close(); } 
			catch (IOException e) {	System.out.println("�������� ����.."); }
			log = "[�Ϲ�����] \"" + name + "\"(" + socket.getInetAddress() + ") �� �������ϴ�.";
			System.out.println(log);
			try { ChatLog.writeLog(log); } catch (IOException e) { System.out.println("Log���� ���� ����");}
			System.out.println("���� ������ ���� : " + AdminManagements.adminTable.size() + "�� �Դϴ�.");
			System.out.println("���� ������ ���� : " + ClientManagements.clientTable.size()
								+ "�� �Դϴ�.");
		}	//finally()
	}	//run()
}	//ChatThread

