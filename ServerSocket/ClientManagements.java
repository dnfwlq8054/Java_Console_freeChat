package ServerSocket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class ClientManagements implements Table{
	//전역변수로 선언해서 쓰래드간의 공유가 가능하도록 함.
	private ClientThread chatThread;

	public void threadIn(Socket socket) throws IOException {	
		chatThread = new ClientThread(socket);
		ClientManagements.clientTable.add(chatThread);	//백터에 chatThread 추가
		chatThread.start(); //쓰레드 시작
	}	//threadIn()
}	//ClientManagements

class ClientThread extends Thread {
	static private int num = 1;		//유저의 번호
	String name = "user" + (num++);	//초기이름 설정
	private String msg, command;	//메시지와 메시지처리 객채
	final Socket socket;			//소켓
	final DataInputStream in;		//입력 스트림
	final DataOutputStream out;	//출력 스트림
	private final ReentrantLock key = new ReentrantLock();	//key생성
	private StringTokenizer st;		//메시지 token 객채
	boolean tolkOn = true;	//벙어리 여부
	
	ClientThread(Socket socket) throws IOException{	//스트림 초기화
		this.socket = socket;
		this.in = new DataInputStream(socket.getInputStream());
		this.out = new DataOutputStream(socket.getOutputStream());
	}	//ClientTHread Constructor
	
	void sendToAll(String msg) {		//받은메시지를 모든 클라이언트들에게 뿌려주기
		//Iterator 인터페이스를 이용해서 HashSet에 있는 자료검색을 한다.
		Iterator itr = ClientManagements.clientTable.iterator();
		Iterator itr2 = AdminManagements.adminTable.iterator();
		
		while(itr2.hasNext()){		//관리자에게 메시지 전송
			AdminThread t1 = (AdminThread)itr2.next();
			try {
				t1.out.writeUTF(this.name + " : " + msg);
			} 
			catch (IOException e) { 
				e.printStackTrace(); 
				System.out.println("메시지 송신 오류..!");
			}
		}
		
		while(itr.hasNext()){		//클라이언트에게 메시지 전송
			try{
				ClientThread t = (ClientThread)itr.next();
				if(t.equals(this))	//자신이 친 채팅은 다시받을 필요가 없기때문에
					continue;
				else
					t.out.writeUTF(this.name + " : " + msg);
			}
			catch(IOException e){ 
				e.printStackTrace(); 
				System.out.println("메시지 송신 오류..!");
			}
		}	//while()
	}	//sendToAll()
	
	void showHelp() throws IOException{		//도움말
		String[] help = {
				"1. ID변경은 '/id 사용할 아이디' 입력하면 됩니다.",
				"2. 사용자 목록은 '/list'를 입력하면 됩니다.",
				"3. 귓속말은 '/w 유저아이디 할말'을 입력하시면 됩니다.",
				"4. 현재 시간을 확인하는 방법은 '/time'을 입력하시면 됩니다.",
				"5. 채팅방 퇴장은 '/exit'을 입력하시면 딥니다.",
		};
		
		for(int i = 0; i < help.length; i++)
			out.writeUTF(help[i]);
	}	//showHelp()
	
	void searchId(String id) throws IOException{	//ID중복검사
		out.writeUTF("ID중복 검색중....");	
		String adminId = "admin";
		String beforeId, log;
		
		if(id.contains(adminId)){
			out.writeUTF("admin아이디는 사용하실 수 없습니다..");
			return;
		}
		
		key.lock();	//쓰레드들이 동시 참조 시 아이디 중복이 될 확률이 있기 때문에 ReentrantLock클래스의 lock()메소드로 동시 참조를 막음
		try{
			beforeId = this.name;
			
			Iterator itr = ClientManagements.clientTable.iterator();
			Iterator itr2 = AdminManagements.adminTable.iterator();
			
			while(itr2.hasNext()){	//관리자 아이디 검색
				AdminThread t1 = (AdminThread)itr2.next();		
				
				if(id.equals(t1.name)){	//아이디가 같으면 리털
					out.writeUTF("중복되는 아이디가 있습니다...");
					return;		
				}	//if()
			}	//while()
			
			while(itr.hasNext()){	//유저 아이디 검색
				ClientThread t = (ClientThread)itr.next();					
				
				if(id.equals(t.name)){	//아이디가 같으면 리털
					out.writeUTF("중복되는 아이디가 있습니다...");
					return;
				}	//if()
			}	//while()
			
			out.writeUTF("아이디 변경 완료...");
			this.name = id;		//중복 검사가 완료되면 ID변경
			log = "[일반유저] \"" + beforeId + "\"(" + socket.getInetAddress() + ") 의 아이디가 "
					+ this.name + "으로 바뀌었습니다.";
			try { ChatLog.writeLog(log); } catch (IOException e) { System.out.println("Log파일 저장 에러(ID)");}
		}	//try()
		catch(IOException e){
			System.out.println("search명령 실패..");
			out.writeUTF("아이디 중복검사 실패..");
		}
		finally{ key.unlock(); }	//lock 풀기
	}	//search()
	
	void list() throws IOException{	//접속자들의 ID리스트 보여주는 명령
		try{
			int count=0;
			Iterator itr = ClientManagements.clientTable.iterator();
			Iterator itr2 = AdminManagements.adminTable.iterator();
			
			out.writeUTF("====현재 관리자 접속자 리스트=====");
			while(itr2.hasNext()){
				AdminThread t1 = (AdminThread)itr2.next();
				out.writeUTF((++count) + " : " + t1.name);
			}
			
			count=0;
			out.writeUTF("====현재 유저 접속자 리스트=====");
			while(itr.hasNext()){
				ClientThread t = (ClientThread)itr.next();
				if(t.equals(this))
					out.writeUTF((++count) + " : " + t.name + "<---이 ID가 나");
				else
					out.writeUTF((++count) + " : " + t.name);
			}	//while()
		}	//try()	
		catch(IOException e){
			System.out.println("리스트명령 오류!!");
			out.writeUTF("리스트 명령 오류!!");
		}	//catch()
	}	//list()
	
	void send(String id, String msg) throws IOException{	//귓속말
		try{
			Iterator itr = ClientManagements.clientTable.iterator();
			Iterator itr2 = AdminManagements.adminTable.iterator();
			
			while(itr.hasNext()){
				AdminThread t1 = (AdminThread)itr2.next();	
				
				if(id.equals(t1.name)){	//입력한 아이디가 같으면 관리자에게 메시지 전달
					ChatWrite.writeFile(this.name + " >>>>> " + t1.name + msg + "(귓속말)");
					t1.out.writeUTF(this.name + "에게 온 귓속말 :" + msg);
					return;
				}	//if()
			}	//while()
			
			while(itr.hasNext()){
				ClientThread t = (ClientThread)itr.next();					
				
				if(id.equals(t.name)){	//입력한 아이디가 같으면 일반 유저에게 메시지 전달
					ChatWrite.writeFile(this.name + " >>>>> " + t.name + msg + "(귓속말)");
					t.out.writeUTF(this.name + "에게 온 귓속말 :" + msg);
					return;
				}	//if()
			}	//while()
			out.writeUTF("해당 아이디가 없습니다...");
		}	//try()
		catch(IOException e){
			System.out.println("send명령 실패..");
			out.writeUTF("귓속말 실패..");
		}
	}
	
	void realTime() throws IOException{		//현재 시간 보여주기
		String date = NowCalendar.nowDate();
		String time = NowCalendar.nowTime();
		
		out.writeUTF("오늘 날짜 : " + date);	
		out.writeUTF("현재 시간 : " + time);
	}
	
	void tolkOnOff(){	//벙어리가 풀리는 타임 제는 메소드
		Timer timer = new Timer();
		TimerTask m_task = new TimerTask(){	//어나니머스 클래스로 바로 정의 했습니다.
		
			public void run(){
				tolkOn = true;
				timer.cancel();
			}
		};
		timer.schedule(m_task, 30000);	//30초 후 타이머 종료
	}
	
	void exit() {	//채팅 종료 
		try {
			Iterator itr = ClientManagements.clientTable.iterator();
			Iterator itr2 = AdminManagements.adminTable.iterator();
			
			while(itr2.hasNext()){
				AdminThread t1 = (AdminThread)itr2.next();
					t1.out.writeUTF(this.name + "님이 퇴장했습니다...");
			}
			
			while(itr.hasNext()){
					ClientThread t = (ClientThread)itr.next();
					if(t.equals(this))	//자신이 친 채팅은 다시받을 필요가 없기때문에
						continue;
					else
						t.out.writeUTF(this.name + "님이 퇴장했습니다...");
			}
		} catch (IOException e) { System.out.println("퇴장 기능 오류..!"); 	}
	}
	
	@Override
	public void run(){
		String log;	//입력받은 문자 체크
	
		try {
			log = socket.getInetAddress() + 
					" " + this.name + "[일반유저]가 접속했습니다...";
			System.out.println(log);
			ChatLog.writeLog(log);
			System.out.println("현재 관리자 접속 수는 : " + AdminManagements.adminTable.size()
			+ "명 입니다.");
			System.out.println("현재 일반 접속자 수는 : " + ClientManagements.clientTable.size()
								+ "명 입니다.");
			out.writeUTF("채팅서버에 접속하셔습니다...");
			out.writeUTF("도움말은 '/help' 를 입력하세요...");
			
			while(true){
				msg = in.readUTF();
				 
				if(!tolkOn){	//벙어리 검사
					this.out.writeUTF("지금은 벙어리 상태입니다.!!");
					tolkOnOff();
					continue;
				}
				st = new StringTokenizer(msg," ");
				command = st.nextToken();

				if(command.equals("/help"))		//도움말
					showHelp();
				
				else if(command.equals("/id")){		//id변경
					if(!st.hasMoreTokens()){	//id를 안첫을 경우
						this.out.writeUTF("id를 입력하세요...");
						continue;
					}
					else
						searchId(st.nextToken());
				} //searchId
				
				else if(command.equals("/list"))	//유저 목록
					list();
				
				else if(command.equals("/time"))	//현재 시간 출력
					realTime();
				
				else if(command.equals("/w")){	//귓속말
					if(!st.hasMoreTokens()){	//id를 안첬을 경우
						this.out.writeUTF("[id] [할 말]을 입력하세요...");
						continue;
					}
					else{
						command = st.nextToken();
						if(!st.hasMoreTokens()){	//id는 첫는대 할말을 안첫을 경우
							out.writeUTF("[id] [할 말]을 입력하세요...");
							continue;
						}
						else
							send(command, st.nextToken(""));
					}
				}	//send
				
				else if(command.equals("/exit"))	//종료
					break;
				
				else{		//대화내용 뿌리기
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
			catch (IOException e) {	System.out.println("접속종료 실패.."); }
			log = "[일반유저] \"" + name + "\"(" + socket.getInetAddress() + ") 가 나갔습니다.";
			System.out.println(log);
			try { ChatLog.writeLog(log); } catch (IOException e) { System.out.println("Log파일 저장 에러");}
			System.out.println("현재 관리자 수는 : " + AdminManagements.adminTable.size() + "명 입니다.");
			System.out.println("현재 접속자 수는 : " + ClientManagements.clientTable.size()
								+ "명 입니다.");
		}	//finally()
	}	//run()
}	//ChatThread

