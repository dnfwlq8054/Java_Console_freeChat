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

class SocketAccept extends Thread{		//클라이어트 접속 받기
	private ServerMenu server = new ServerMenu();
	// ClientManagements client = new ClientManagements();
	
	public void run(){
		ServerSocket serverSocket = null;	//소켓 생성
		try{
			serverSocket = new ServerSocket(9000);	//소켓에 포트번호 부여(bind)
			System.out.println("서버가 준비되었습니다.");
			System.out.println("도움말을 보실려면 '/help'를 입력해주세요.");
			
			while(true){
				//	accept함수는 접속이 올때까지 무한정 대기한다.
				Socket socket = serverSocket.accept();	//서버 소켓 연결 함수(accept)
				System.out.println(socket.getInetAddress() + "로부터 요청이 들어왔습니다.");
		
				//	client.threadIn(socket);
				server.init(socket);
				server.start();
			}
		}
		catch(IOException e){
			System.out.println("서버는 하나만 동작합니다..");
			System.exit(-1);
		}	//catch()
	}	//run()
}	//SocetAccept class

class ServerInput extends Thread implements Table{	//서버에서 처리할 수 있는 기능
	private Scanner sc = new Scanner(System.in);
	private StringTokenizer st;	
	private String tok;
	
	void showHelp() {		//도움말
		String[] help = {
				"1. 회원정보를 볼려면 '/mbinfo'를 입력하면 됩니다.",
				"2. 회원정보 삭제는 '/del [ID]'입니다.",
				"3. 접속자 목록은 '/list'를 입력하면 됩니다.",
				"4. 관리자의 이름을 변경하려면 '/rename [관리자 ID] [아름]을 입력하시면 됩니다.",
				"5. 서버종료는 '/poweroff'를 입력하시면 딥니다."
		};
		
		for(int i = 0; i < help.length; i++)
			System.out.println(help[i]);
	}	//showHelp()
	
	void saveInfo(){	//현재 폴더에 해당 내용들 저장
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
	
	void showList() {	//접속자들의 ID리스트 보여주는 명령
			int count=0;
			Iterator itr = ClientManagements.clientTable.iterator();
			Iterator itr2 = AdminManagements.adminTable.iterator();
			
			System.out.println("====현재 관리자 접속자 리스트=====");
			while(itr2.hasNext()){
				AdminThread t1 = (AdminThread)itr2.next();
					System.out.println((++count) + " : " + t1.name);
			}
			
			count=0;
			System.out.println("====현재 유저 접속자 리스트=====");
			while(itr.hasNext()){
				ClientThread t = (ClientThread)itr.next();
				System.out.println((++count) + " : " + t.name);
			}	//while()
	}	//showList()
	
	void memberInfo(){		//저장된 관리자들에 대한 내용 출력
		Enumeration<AdminInfo> en = MemberAdmin.hashTable.elements();
		System.out.println("====관리자 정보====");
		while(en.hasMoreElements()){
			AdminInfo e = (AdminInfo)en.nextElement();
			System.out.println("==============");
			System.out.println("이름 : " + e.name);
			System.out.println("id : " + e.id);
			System.out.println("password : " + e.password);
			System.out.println("==============");	
		}
	}	//memberInfo
	
	void deleteAdmin(String id){	//관리자 정보 삭제
		if(MemberAdmin.hashTable.containsKey(id)){
			MemberAdmin.hashTable.remove(id);
			System.out.println("삭제 완료!!");
			saveInfo();
		}
		else
			System.out.println("찾는 id값이 없습니다...");
		
	}//deletAdmin()
	
	void showIP_List(){		//현재 접속중인 유저들의 ip주소 출력
		Iterator itr = ClientManagements.clientTable.iterator();
		Iterator itr2 = AdminManagements.adminTable.iterator();
		
		System.out.println("==========관리자 IP목록==========");
		while(itr2.hasNext()){
			AdminThread t1 = (AdminThread)itr2.next();
			System.out.println(t1.name + "의 IP : " + t1.socket.getInetAddress());
		}
		
		System.out.println("=========일반 유저 IP목록=========");
		while(itr.hasNext()){
				ClientThread t = (ClientThread)itr.next();
				System.out.println(t.name + "의 IP : " + t.socket.getInetAddress());
		}
		System.out.println("============================");
	}
	
	@Override
	public void run(){
		String in, id;
		
		while(true){
			in = sc.nextLine();
			if(in.length() == 0)	//엔터키 연속방지
				continue;
			
			st = new StringTokenizer(in," ");
			tok = st.nextToken();
			
			if(tok.equals("/help"))	//도움말
				showHelp();
			
			else if(tok.equals("/mbinfo"))	//관리자들 회원정보
				memberInfo();
			
			else if(tok.equals("/list"))	//접속유저들의 정보
				showList();
			
			else if(tok.equals("/del")){	//관리자 회원정보 삭제
				if(!st.hasMoreTokens()){	//id를 안첫을 경우
					System.out.println("id를 입력하세요...");
					continue;
				}
				deleteAdmin(st.nextToken());
			}
			
			else if(tok.equals("/ip"))	//접속중인 유저들의 ip주소 확인
				showIP_List();
			
			else if(tok.equals("/rename")){		//관리자 회원정보 이름 바꿔주기
				if(!st.hasMoreTokens()){		//id를 안첫을 경우
					System.out.println("해당 id를 입력하세요...");
					continue;
				}
				else{
					tok = st.nextToken();
					if(MemberAdmin.hashTable.containsKey(tok)){
						if(!st.hasMoreTokens())	//이름을 안첫을 경우
							System.out.println("바꿀이름을 정확히 입력해 주세요...");
						else{
							MemberAdmin.hashTable.get(tok).name = st.nextToken();
							System.out.println("이름 바꾸기 성공!!");
						}
					}
					else
						System.out.println("해당 ID가 없습니다...");
				}
			}	//rename
				
			else if(tok.equals("/poweroff")){	//서버 종료
				saveInfo();
				try {
					ChatLog.writeLog("=======채팅종료======");
					ChatWrite.writeFile("======서버 다운=====");
				} catch (IOException e) { }
				ChatLog.closeLog();
				ChatWrite.closeWrite();
				System.out.println("데이터 저장..");
				System.out.println("서버 종료..");
				System.exit(1);
			}
		}
	}	//run()
}// ServerInput class

//************************** main *************************//
public class MainServer {
	
	public static void main(String[] args) {
		System.out.println("서버 준비중입니다. 잠시만 기다려주세요..");
		new MemberAdmin();		//파일 읽어오기
		SocketAccept socketAccept = new SocketAccept();
		ServerInput serverInput = new ServerInput();
		socketAccept.start();	//클라이언트 접속을 받는 쓰레드 시작
		serverInput.start();	//서버 기능을 할 수 있는 쓰래드 시작
		return;
		
	}	//main()
}	//MainServer