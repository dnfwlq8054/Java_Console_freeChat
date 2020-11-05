package ServerSocket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Calendar;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.concurrent.locks.ReentrantLock;

public class AdminManagements implements Table{
	private AdminThread admin;
	
	public void threadIn(Socket socket, String id, String pw) throws IOException{
		admin = new AdminThread(socket, id, pw);
		AdminManagements.adminTable.add(admin);
		admin.start();	//������ ������ ����
	}
}

class AdminThread extends Thread{
	final DataInputStream in;		//�Է� ��Ʈ��
	final DataOutputStream out;	//��� ��Ʈ��
	private String msg, command;	//�޽����� �޽���ó�� ��ä
	private StringTokenizer st;	
	
	Socket socket;			//����
	String name;		//������ ID
	String password;	//������ PW
	
	private final ReentrantLock key = new ReentrantLock();	//key����
	
	AdminThread(Socket socket, String id, String pw) throws IOException{	//��Ʈ�� �ʱ�ȭ
		this.socket = socket;
		this.name = id;
		this.password = pw;
		this.in = new DataInputStream(socket.getInputStream());
		this.out = new DataOutputStream(socket.getOutputStream());
		MemberAdmin.hashTable.get(id).online = true; //���������� ǥ��
	}	
	
	void showHelp() throws IOException{		//����
		String[] help = {
				"1. ID������ '/id ����� ���̵�' �Է��ϸ� �˴ϴ�.",
				"2. Password������ '/pw ������ Password'�� �Է��Ͻø� �˴ϴ�.",
				"3. ����� ����� '/list'�� �Է��ϸ� �˴ϴ�.",
				"4. �ӼӸ��� '/w [ID] [�� ��]'�� �Է��Ͻø� �˴ϴ�.",
				"5. �߹��� '/kick [ID]'�� �Է��Ͻø� �˴ϴ�.",
				"6. ������ ��û �޽����� �������� '/server [�� ��]�� �Է��Ͻø� �˴ϴ�.",
				"7. �� ����Ȯ���� '/myinfo'�� �Է��Ͻø� �˴ϴ�.",
				"8. ��� �ϴ� ����� '/dnd [ID] �� �Է��Ͻø� (30)�ʰ� ������°� �˴ϴ�.",
				"9. ���� �ð��� Ȯ���ϴ� ����� '/time'�� �Է��Ͻø� �˴ϴ�.",
				"10. ä�ù� ������ '/exit'�� �Է��Ͻø� ���ϴ�."
		};
		
		for(int i = 0; i < help.length; i++)
			out.writeUTF(help[i]);
	}	//showHelp()
	
	void kick(String id) throws IOException{		//�Ϲ� ������ �߹�

		try{
			Iterator itr = ClientManagements.clientTable.iterator();
					
			while(itr.hasNext()){
				ClientThread t = (ClientThread)itr.next();					
				
				if(id.equals(t.name)){	//�ش���̵� ����!!
					out.writeUTF("���� ����!!");
					t.out.writeUTF("������߽��ϴ�.!!");
					t.socket.close(); 
					return;
				}	//if()
			}	//while()
			out.writeUTF("�ش� ���̵� �����ϴ�....");
		}	//try()
		catch(IOException e){
			System.out.println("kick��� ����..");
			out.writeUTF("���̵� �ߺ��˻� ����..");
		}
	}	//kick()
	
	void showMyInfo() throws IOException{		//���� ���� ����
		AdminInfo e = MemberAdmin.hashTable.get(name);
		out.writeUTF("=====�� ����=====");
		out.writeUTF("���̵� : " + e.id);
		out.writeUTF("��й�ȣ : " + e.password);
		out.writeUTF("�̸� : " + e.name);
		out.writeUTF("==============");
	}	//showMyInfo()
	
	void searchId(String id) throws IOException{	//ID�ߺ��˻�
		out.writeUTF("ID�ߺ� �˻���....");	
		String beforeId, log;	//beforeId : ���� ID���� ����
		key.lock();	//��������� ���� ���� �� ���̵� �ߺ��� �� Ȯ���� �ֱ� ������ ReentrantLockŬ������ lock()�޼ҵ�� ���� ������ ����
		try{
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
			beforeId = this.name;
			this.name = id;		//�ߺ� �˻簡 �Ϸ�Ǹ� ID����
			MemberAdmin.hashTable.get(beforeId).id = id;
			out.writeUTF("���̵� ���� �Ϸ�...");
			log = "[������] \"" + beforeId + "\"(" + socket.getInetAddress() + ") �� ���̵� "
					+ this.name + "���� �ٲ�����ϴ�.";
			try { ChatLog.writeLog(log); } catch (IOException e) { System.out.println("Log���� ���� ����(ID)");}
			out.writeUTF("*�α��� �� ����� id�� �α��� �Ͻñ� �ٶ��ϴ�...");
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
				if(t1.equals(this))
					out.writeUTF((++count) + " : " + t1.name + "<---�� ID�� ��");
				else
					out.writeUTF((++count) + " : " + t1.name);
			}
			
			count=0;
			out.writeUTF("====���� ���� ������ ����Ʈ=====");
			while(itr.hasNext()){
				ClientThread t = (ClientThread)itr.next();
				out.writeUTF((++count) + " : " + t.name);
			}	//while()
		}	//try()	
		catch(IOException e){
			System.out.println("����Ʈ��� ����!!");
			out.writeUTF("����Ʈ ��� ����!!");
		}	//catch()
	}	//list()
	
	void sendToAll(String msg) throws IOException{		//�����޽����� ��� Ŭ���̾�Ʈ�鿡�� �ѷ��ֱ�
		//Iterator �������̽��� �̿��ؼ� HashSet�� �ִ� �ڷ�˻��� �Ѵ�.
		Iterator itr = ClientManagements.clientTable.iterator();
		Iterator itr2 = AdminManagements.adminTable.iterator();
		while(itr2.hasNext()){		//�����ڵ����� ������ �޽���
			AdminThread t1 = (AdminThread)itr2.next();
			try {
				if(t1.equals(this))	//�ڽ��� ģ ä���� �ٽù��� �ʿ䰡 ���⶧����
					continue;
				else
					t1.out.writeUTF(this.name +  "(Admin) : " + msg);
				
			} catch (IOException e) { e.printStackTrace(); }
		}	//while()
		
		while(itr.hasNext()){	//�Ϲ� �����鿡�� ������ �޽���
			try {
				ClientThread t = (ClientThread)itr.next();
					t.out.writeUTF(this.name +  "(Admin) : " + msg);
					t.out.flush();
			}
			catch (IOException e) {}
		}	//while()
	}	//sendToAll()
	
	void send(String id, String msg) throws IOException{	//�ӼӸ�
		try{
			Iterator itr = ClientManagements.clientTable.iterator();
			Iterator itr2 = AdminManagements.adminTable.iterator();
			
			while(itr2.hasNext()){
				AdminThread t1 = (AdminThread)itr2.next();	
				
				if(id.equals(t1.name)){	//�Է��� ���̵� ������ �����ڿ��� �޽��� ����
					ChatWrite.writeFile(this.name + " >>>>> " + t1.name + msg + "(�ӼӸ�)");
					t1.out.writeUTF(this.name + "(Admin)���� �� �ӼӸ�  : " + msg);
					return;
				}	//if()
			}	//while()
			
			while(itr.hasNext()){
				ClientThread t = (ClientThread)itr.next();					
				
				if(id.equals(t.name)){	//�Է��� ���̵� ������ �Ϲ����� ���� �޽��� ����
					ChatWrite.writeFile(this.name + " >>>>> " + t.name + msg + "(�ӼӸ�)");
					t.out.writeUTF(this.name + "(Admin)���� �� �ӼӸ�  : " + msg);
					return;
				}	//if()
			}	//while()
			out.writeUTF("�ش� ���̵� �����ϴ�...");
		}	//try()
		catch(IOException e){
			System.out.println("send��� ����..");
			out.writeUTF("�ӼӸ� ����..");
		}
	}	//send()
	
	void serverMsg(String msg){		//������ �޽��� ����
		System.out.println(this.name + "���� ���� �޽��� �Դϴ�. :" + msg);
	}
	
	void changePw(String pw) throws IOException{	//��й�ȣ ����
		MemberAdmin.hashTable.get(name).password = pw;
		out.writeUTF("��й�ȣ ���� �Ϸ�!!");
	}	//changePw()
	
	void realTime() throws IOException{		//���� �ð� �˷��ִ� �޼ҵ�
		String date = NowCalendar.nowDate();
		String time = NowCalendar.nowTime();
		
		out.writeUTF("���� ��¥ : " + date);	
		out.writeUTF("���� �ð� : " + time);
	}
	
	void dnd(String id) throws IOException{	//��� �޼ҵ�
		Iterator itr = ClientManagements.clientTable.iterator();
		
		while(itr.hasNext()){
			ClientThread t = (ClientThread)itr.next();					
			
			if(id.equals(t.name)){	//���
				t.tolkOn = false;
				this.out.writeUTF("��� ����!!");
				sendToAll(name + "(������)�� ����  " + t.name + "(�����)�� ��� ���°� �˴ϴ�.(30��)");
				return;
			}	//if()
		}	//while()
		out.writeUTF("�ش� ���̵� �����ϴ�...");
	}
	
	void exit() {	//�������� �޼ҵ�
		try {
			Iterator itr = ClientManagements.clientTable.iterator();
			Iterator itr2 = AdminManagements.adminTable.iterator();
			
			while(itr2.hasNext()){		//�����ڵ����� �Ѹ���
				AdminThread t1 = (AdminThread)itr2.next();
				
				if(t1.equals(this))	//�ڽ��� �����ٴ� ������ ���� �ʿ䰡 ���� ������
					continue;
				else
					t1.out.writeUTF(this.name + "(������)���� �����߽��ϴ�...");
				
			}
			
			while(itr.hasNext()){		//���������� �Ѹ���
					ClientThread t = (ClientThread)itr.next();
						t.out.writeUTF(this.name + "(������)���� �����߽��ϴ�...");
			}
		} catch (IOException e) { System.out.println("���� ��� ����..!"); }
	}	//exit()
	
	@Override
	public void run(){
		String log;
		
		try {
			log = socket.getInetAddress() + 
					" " + this.name + "[������]�� �����߽��ϴ�...";
			System.out.println(log);
			ChatLog.writeLog(log);
			
			System.out.println("���� ������ ���� ���� : " + AdminManagements.adminTable.size()
			+ "�� �Դϴ�.");
			System.out.println("���� �Ϲ� ������ ���� : " + ClientManagements.clientTable.size()
			+ "�� �Դϴ�.");
			out.writeUTF("������ ��忡 �����߽��ϴ�...");
			out.writeUTF("������ '/help' �� �Է��ϼ���...");
			
			while(true){
				msg = in.readUTF();
				st = new StringTokenizer(msg," ");
				command = st.nextToken();
				
				if(command.equals("/help"))	//���� ���
					showHelp();
				
				else if(command.equals("/kick"))	//�Ϲ����� �߹�
					kick(st.nextToken());
				
				else if(command.equals("/list"))	//���� ���
					list();
				
				else if(command.equals("/id")){		//id����
					if(!st.hasMoreTokens()){		//id�� ��ù�� ���
						this.out.writeUTF("id�� �Է��ϼ���...");
						continue;
					}
					else
						searchId(st.nextToken());
				}	//searchId
				
				else if(command.equals("/myinfo"))		//�ش� ������ ���� ���
					showMyInfo();
				
				else if(command.equals("/server")){		//������ �޽��� ����
					if(!st.hasMoreTokens()){	//�Ҹ��� ��ù�� ���
						this.out.writeUTF("[�� ��]�� �Է��ϼ���...");
						continue;
					}
					else
						serverMsg(st.nextToken(""));
				}
				
				else if(command.equals("/dnd")){	//���
					if(!st.hasMoreTokens()){	//id�� ��ù�� ���
						this.out.writeUTF("ID�� �Է��ϼ���.");
						continue;
					}
					else
						dnd(st.nextToken());
				}
				
				else if(command.equals("/w")){	//�ӼӸ�
					if(!st.hasMoreTokens()){	//id�� ��ù�� ���
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
				}	//send()
				
				else if(command.equals("/pw")){		//��й�ȣ ����
					if(!st.hasMoreTokens()){
						this.out.writeUTF("password�� �Է��ϼ���...");
						continue;
					}
					else
						changePw(st.nextToken());
				}	//changePw
				
				else if(command.equals("/time"))	//����ð�
					realTime();
				
				else if(command.equals("/exit"))	//��������
					break;
				
				else{		//��ȭ���� �Ѹ���
					ChatWrite.writeFile("(������)[" + this.name + "] " + msg);
					sendToAll(msg);
				}
			}	//while()
		} 	//try
		catch (IOException e) {} 
		finally{
			exit();
			AdminManagements.adminTable.remove(this);
			MemberAdmin.hashTable.get(name).online = false;
			try{ this.socket.close(); }
			catch(IOException e){}
			log = "[������] \"" + name + "\"(" + socket.getInetAddress() + ") �� �������ϴ�.";
			System.out.println(log);
			try { ChatLog.writeLog(log); } catch (IOException e) { System.out.println("Log���� ���� ����");}
			System.out.println("���� ������ ���� : " + AdminManagements.adminTable.size() + "�� �Դϴ�.");
			System.out.println("���� ������ ���� : " + ClientManagements.clientTable.size()
								+ "�� �Դϴ�.");
		}
	}	//run()
}	//AdminManagement class
