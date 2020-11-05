package ServerSocket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class ServerMenu implements Table{
	private Socket socket;
	private AccessMenu accessMenu;
	
	public void init(Socket socket) throws IOException{		//�޴� �ʱ�ȭ
		this.socket = socket;
	}	//init()
	
	public void access(Socket socket) throws IOException{
			client.threadIn(socket);		//�Ϲ����� ������ ����
			return;
	}
	
	public void access(Socket socket, String id, String pw) throws IOException{
			admin.threadIn(socket, id, pw);		//������ ������ ����
			return;
	}	//menu()
	
	public void start() throws IOException{		//�α��� �Ŵ� ��ŸƮ
		accessMenu = new AccessMenu(socket);
		accessMenu.start();
	}
}

class AccessMenu extends Thread{		//ȸ�������ϴ� ������
	private DataOutputStream out;	
	private DataInputStream in;
	private Socket socket;
	private String choice;
	private ServerMenu serverMenu;
	private final ReentrantLock key = new ReentrantLock();	//key����
	private ServerInput sv = new ServerInput();	//������ ������ �����ϱ� ���ؼ�
	
	AccessMenu(Socket socket) throws IOException{
		this.serverMenu = new ServerMenu();
		this.socket = socket;
		this.out = new DataOutputStream(socket.getOutputStream());
		this.in = new DataInputStream(socket.getInputStream());
	}	//AccessMenu Constructor
	
	@Override
	public void run(){	
		Random random = new Random();		//������ȣ�� ���� ����Ŭ����
		String id=null, password=null, name=null, district=null;
		int authen;
		String[] menu = {
				"1. ������ ���",
				"2. �Ϲ� ���",
				"3. ����"
		};
		
		while(true){
			try {
				for(int i = 0; i < menu.length; i++)
					out.writeUTF(menu[i]);
				choice = in.readUTF();
				
				if(choice.equals("1")){
					out.writeUTF("1. �α���");
					out.writeUTF("2. ȸ������");
					out.writeUTF("3. ���ư���");
					choice = in.readUTF();
					
					if(choice.equals("1")){
						out.writeUTF("���̵�  : ");
						id = in.readUTF();
						out.writeUTF("��й�ȣ  : ");
						password = in.readUTF();
						
						if(MemberAdmin.hashTable.containsKey(id)){		//id�˻�
							if(password.equals(MemberAdmin.hashTable.get(id).password)){	//password�˻�
								if(MemberAdmin.hashTable.get(id).online){	//�ش� ID�� ���������� �ƴ��� �˻�
									out.writeUTF("���� �ش� ID�� �α��� ���Դϴ�..");
									continue;
								}
								else{
									serverMenu.access(socket, id, password);
									return;
								}
							}
							else{
								System.out.println("2");
								out.writeUTF("���̵� Ȥ�� ��й�ȣ Ʋ��!!");
							}
						}
						else
							out.writeUTF("���̵� Ȥ�� ��й�ȣ Ʋ��!!");
						
					}else if(choice.equals("2")){		//ȸ������ 
						out.writeUTF("***ȸ�� ����***");
						out.writeUTF("���̵� �Է�");
						id = in.readUTF();
						out.writeUTF("���̵� �ߺ��˻���...");
						
						key.lock();		//ID�˻簡 ���ÿ� �̷����� �ߺ��� �� �ֱ� ������ lock()�޼ҵ� ȣ��
						if(MemberAdmin.hashTable.containsKey(id)){
							out.writeUTF("�ߺ��� ���̵� ����!!..");
							out.writeUTF("�ٽ� �Է��ϼ���..");
							continue;
						}
						key.unlock();
						
						out.writeUTF("��й�ȣ �Է�");
						password = in.readUTF();
						out.writeUTF("�̸� �Է� : ");
						name = in.readUTF();
						out.writeUTF("������ȣ �Է�");
						
						authen = random.nextInt(90000) + 10000;	//������ȣ ����
						System.out.println(id + "�� ���� ������ȣ : " + authen);
						district = "" + authen;		//������ȣ String���� �ٲٴ� �۾�
						
						for(int i = 1; i <=3; i++){
							if(district.equals(in.readUTF())){
								out.writeUTF("������ȣ �Է¼���!!");
								MemberAdmin.hashTable.put(id, new AdminInfo(id, password, name));
								sv.saveInfo();	//������ ����
								break;
							}
							else
								out.writeUTF("�ٽ� �Է��ϼ���..." + i + "/3");
							if(i == 3)
								out.writeUTF("���� ����..");
						}//for()
					}//ȸ������ if
					else if(choice.equals("3"))
						continue;
				}// �ʱ�޴� 1������  if
				else if(choice.equals("2")){	//�Ϲ����� ����
					serverMenu.access(socket);
					return;
				}
				else if(choice.equals("3")){
					try{ this.socket.close(); }
					catch(IOException e){}
					System.out.println(socket.getInetAddress() + "�� �޴�����â���� �����߽��ϴ�.");
					break;
				}
				
				else
					out.writeUTF("�ٽ� �Է��ϼ���...");
				
			}	//try() 
			catch (IOException e) { 
					System.out.println(socket.getInetAddress() + "�� �������ϴ�."); 
					return;
			}
		}	//while()
	}	//run()
}	//Input class