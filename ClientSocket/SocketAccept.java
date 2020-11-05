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
			System.out.println("서버에 연결중입니다....");
			Socket socket = new Socket();	//소켓 생성	
			SocketAddress socketAddress = new InetSocketAddress(serverIp,port);	//서버 접속에 필요한 정보를 저장
			socket.connect(socketAddress, 5000);	//서버연결을 시작한 후 5초가 지나면 예외처리
			
			Sender send = new Sender(socket);
			Receiver receive = new Receiver(socket);
			
			send.start();	//송신 쓰래드 시작
			receive.start();	//수신 쓰래드 시작
			return;
		}	//try()
		catch(IOException e){
			System.out.println("서버에 접속하지 못했습니다....");
		}	//catch()
	}	//socketCreate()
}	//SocketAccept

class Sender extends Thread implements Keyborad{	//메시지 송신
	private Socket socket;		
	private DataOutputStream out;	
	private String chat;	//채팅 저장
	
	Sender(Socket socket){
		this.socket = socket;
		try {
			this.out = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {}
	}	//Sender Constructor
	
	@Override
	public void run(){	//송신 쓰래드 run()
		try {
			while(out != null){
				chat = Keyborad.In.nextLine();					
					
				if(chat.length() == 0)	//엔터키 연속방지
					continue;
				else
					out.writeUTF(chat);	//입력한 채팅을 서버로 송신
			}	//while()
		}
		catch (IOException e) {System.out.println("메시지 전송 실패!");}
	}//run()
}//Sender

class Receiver extends Thread{	//매시지 수신 
	private Socket socket;
	private DataInputStream in;
	
	Receiver(Socket socket){	
		this.socket = socket;
		try{
			this.in = new DataInputStream(socket.getInputStream());
		} catch(IOException e) {}
	}//Receiver Constructor
	
	@Override
	public void run(){		//수신 쓰래드 run()
		try {
			while(in != null)		//수신된 내용을 계속 검사한다.
				System.out.println(in.readUTF());
			
		} 
		catch (IOException e) {	//소켓 연결이 끊어지면 채팅프로그램 종료
			System.out.println("채팅프로그램 종료..");
			System.exit(1);
		}
	}//run()
}// Receiver