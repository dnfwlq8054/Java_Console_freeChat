package ClientSocket;

import java.io.*;
import java.util.*;
import java.net.*;

public class SocketAccept {
	private String serverIp;
	private int port;

	public SocketAccept(String ipAddress, int portNum){
		serverIp = ipAddress;
		port = portNum;
	}	//SocketAccept Constructor
	
	public void socketCreate(){
		try{
			System.out.println("������ �������Դϴ�....");
			Socket socket = new Socket();	//���� ����	
			SocketAddress socketAddress = new InetSocketAddress(serverIp,port);	//���� ���ӿ� �ʿ��� ������ ����
			socket.connect(socketAddress, 5000);	//���������� ������ �� 5�ʰ� ������ ����ó��
			
			Sender send = new Sender(socket);
			Receiver receive = new Receiver(socket);
			
			send.start();	//�۽� ������ ����
			receive.start();	//���� ������ ����
			return;
		}	//try()
		catch(IOException e){
			System.out.println("������ �������� ���߽��ϴ�....");
		}	//catch()
	}	//socketCreate()
}	//SocketAccept

class Sender extends Thread implements Keyborad{	//�޽��� �۽�
	private Socket socket;		
	private DataOutputStream out;	
	private String chat;	//ä�� ����
	
	Sender(Socket socket){
		this.socket = socket;
		try {
			this.out = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {}
	}	//Sender Constructor
	
	@Override
	public void run(){	//�۽� ������ run()
		try {
			while(out != null){
				chat = Keyborad.In.nextLine();					
					
				if(chat.length() == 0)	//����Ű ���ӹ���
					continue;
				else
					out.writeUTF(chat);	//�Է��� ä���� ������ �۽�
			}	//while()
		}
		catch (IOException e) {System.out.println("�޽��� ���� ����!");}
	}//run()
}//Sender

class Receiver extends Thread{	//�Ž��� ���� 
	private Socket socket;
	private DataInputStream in;
	
	Receiver(Socket socket){	
		this.socket = socket;
		try{
			this.in = new DataInputStream(socket.getInputStream());
		} catch(IOException e) {}
	}//Receiver Constructor
	
	@Override
	public void run(){		//���� ������ run()
		try {
			while(in != null)		//���ŵ� ������ ��� �˻��Ѵ�.
				System.out.println(in.readUTF());
			
		} 
		catch (IOException e) {	//���� ������ �������� ä�����α׷� ����
			System.out.println("ä�����α׷� ����..");
			System.exit(1);
		}
	}//run()
}// Receiver