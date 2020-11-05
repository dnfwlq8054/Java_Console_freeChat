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
		admin.start();	//관리자 쓰레드 시작
	}
}

class AdminThread extends Thread{
	final DataInputStream in;		//입력 스트림
	final DataOutputStream out;	//출력 스트림
	private String msg, command;	//메시지와 메시지처리 객채
	private StringTokenizer st;	
	
	Socket socket;			//소켓
	String name;		//관리자 ID
	String password;	//관리자 PW
	
	private final ReentrantLock key = new ReentrantLock();	//key생성
	
	AdminThread(Socket socket, String id, String pw) throws IOException{	//스트림 초기화
		this.socket = socket;
		this.name = id;
		this.password = pw;
		this.in = new DataInputStream(socket.getInputStream());
		this.out = new DataOutputStream(socket.getOutputStream());
		MemberAdmin.hashTable.get(id).online = true; //접속중임을 표시
	}	
	
	void showHelp() throws IOException{		//도움말
		String[] help = {
				"1. ID변경은 '/id 사용할 아이디' 입력하면 됩니다.",
				"2. Password변경은 '/pw 변경할 Password'를 입력하시면 됩니다.",
				"3. 사용자 목록은 '/list'를 입력하면 됩니다.",
				"4. 귓속말은 '/w [ID] [할 말]'을 입력하시면 됩니다.",
				"5. 추방은 '/kick [ID]'를 입력하시면 됩니다.",
				"6. 서버로 요청 메시지를 보낼려면 '/server [할 말]을 입력하시면 됩니다.",
				"7. 내 정보확인은 '/myinfo'를 입력하시면 됩니다.",
				"8. 벙어리 하는 방법은 '/dnd [ID] 를 입력하시면 (30)초간 벙어리상태가 됩니다.",
				"9. 현재 시간을 확인하는 방법은 '/time'을 입력하시면 됩니다.",
				"10. 채팅방 퇴장은 '/exit'을 입력하시면 딥니다."
		};
		
		for(int i = 0; i < help.length; i++)
			out.writeUTF(help[i]);
	}	//showHelp()
	
	void kick(String id) throws IOException{		//일반 유저들 추방

		try{
			Iterator itr = ClientManagements.clientTable.iterator();
					
			while(itr.hasNext()){
				ClientThread t = (ClientThread)itr.next();					
				
				if(id.equals(t.name)){	//해당아이디 강퇴!!
					out.writeUTF("강퇴 성공!!");
					t.out.writeUTF("강퇴당했습니다.!!");
					t.socket.close(); 
					return;
				}	//if()
			}	//while()
			out.writeUTF("해당 아이디가 없습니다....");
		}	//try()
		catch(IOException e){
			System.out.println("kick명령 실패..");
			out.writeUTF("아이디 중복검사 실패..");
		}
	}	//kick()
	
	void showMyInfo() throws IOException{		//나의 정보 보기
		AdminInfo e = MemberAdmin.hashTable.get(name);
		out.writeUTF("=====내 정보=====");
		out.writeUTF("아이디 : " + e.id);
		out.writeUTF("비밀번호 : " + e.password);
		out.writeUTF("이름 : " + e.name);
		out.writeUTF("==============");
	}	//showMyInfo()
	
	void searchId(String id) throws IOException{	//ID중복검사
		out.writeUTF("ID중복 검색중....");	
		String beforeId, log;	//beforeId : 기존 ID저장 변수
		key.lock();	//쓰레드들이 동시 참조 시 아이디 중복이 될 확률이 있기 때문에 ReentrantLock클래스의 lock()메소드로 동시 참조를 막음
		try{
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
			beforeId = this.name;
			this.name = id;		//중복 검사가 완료되면 ID변경
			MemberAdmin.hashTable.get(beforeId).id = id;
			out.writeUTF("아이디 변경 완료...");
			log = "[관리자] \"" + beforeId + "\"(" + socket.getInetAddress() + ") 의 아이디가 "
					+ this.name + "으로 바뀌었습니다.";
			try { ChatLog.writeLog(log); } catch (IOException e) { System.out.println("Log파일 저장 에러(ID)");}
			out.writeUTF("*로그인 시 변경된 id로 로그인 하시기 바랍니다...");
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
				if(t1.equals(this))
					out.writeUTF((++count) + " : " + t1.name + "<---이 ID가 나");
				else
					out.writeUTF((++count) + " : " + t1.name);
			}
			
			count=0;
			out.writeUTF("====현재 유저 접속자 리스트=====");
			while(itr.hasNext()){
				ClientThread t = (ClientThread)itr.next();
				out.writeUTF((++count) + " : " + t.name);
			}	//while()
		}	//try()	
		catch(IOException e){
			System.out.println("리스트명령 오류!!");
			out.writeUTF("리스트 명령 오류!!");
		}	//catch()
	}	//list()
	
	void sendToAll(String msg) throws IOException{		//받은메시지를 모든 클라이언트들에게 뿌려주기
		//Iterator 인터페이스를 이용해서 HashSet에 있는 자료검색을 한다.
		Iterator itr = ClientManagements.clientTable.iterator();
		Iterator itr2 = AdminManagements.adminTable.iterator();
		while(itr2.hasNext()){		//관리자들한태 보내는 메시지
			AdminThread t1 = (AdminThread)itr2.next();
			try {
				if(t1.equals(this))	//자신이 친 채팅은 다시받을 필요가 없기때문에
					continue;
				else
					t1.out.writeUTF(this.name +  "(Admin) : " + msg);
				
			} catch (IOException e) { e.printStackTrace(); }
		}	//while()
		
		while(itr.hasNext()){	//일반 유저들에게 보내는 메시지
			try {
				ClientThread t = (ClientThread)itr.next();
					t.out.writeUTF(this.name +  "(Admin) : " + msg);
					t.out.flush();
			}
			catch (IOException e) {}
		}	//while()
	}	//sendToAll()
	
	void send(String id, String msg) throws IOException{	//귓속말
		try{
			Iterator itr = ClientManagements.clientTable.iterator();
			Iterator itr2 = AdminManagements.adminTable.iterator();
			
			while(itr2.hasNext()){
				AdminThread t1 = (AdminThread)itr2.next();	
				
				if(id.equals(t1.name)){	//입력한 아이디가 같으면 관리자에게 메시지 전달
					ChatWrite.writeFile(this.name + " >>>>> " + t1.name + msg + "(귓속말)");
					t1.out.writeUTF(this.name + "(Admin)에게 온 귓속말  : " + msg);
					return;
				}	//if()
			}	//while()
			
			while(itr.hasNext()){
				ClientThread t = (ClientThread)itr.next();					
				
				if(id.equals(t.name)){	//입력한 아이디가 같으면 일반유저 에게 메시지 전달
					ChatWrite.writeFile(this.name + " >>>>> " + t.name + msg + "(귓속말)");
					t.out.writeUTF(this.name + "(Admin)에게 온 귓속말  : " + msg);
					return;
				}	//if()
			}	//while()
			out.writeUTF("해당 아이디가 없습니다...");
		}	//try()
		catch(IOException e){
			System.out.println("send명령 실패..");
			out.writeUTF("귓속말 실패..");
		}
	}	//send()
	
	void serverMsg(String msg){		//서버로 메시지 전송
		System.out.println(this.name + "님이 보낸 메시지 입니다. :" + msg);
	}
	
	void changePw(String pw) throws IOException{	//비밀번호 변경
		MemberAdmin.hashTable.get(name).password = pw;
		out.writeUTF("비밀번호 변경 완료!!");
	}	//changePw()
	
	void realTime() throws IOException{		//현재 시간 알려주는 메소드
		String date = NowCalendar.nowDate();
		String time = NowCalendar.nowTime();
		
		out.writeUTF("오늘 날짜 : " + date);	
		out.writeUTF("현재 시간 : " + time);
	}
	
	void dnd(String id) throws IOException{	//벙어리 메소드
		Iterator itr = ClientManagements.clientTable.iterator();
		
		while(itr.hasNext()){
			ClientThread t = (ClientThread)itr.next();					
			
			if(id.equals(t.name)){	//벙어리
				t.tolkOn = false;
				this.out.writeUTF("벙어리 성공!!");
				sendToAll(name + "(관리자)에 의해  " + t.name + "(사용자)는 벙어리 상태가 됩니다.(30초)");
				return;
			}	//if()
		}	//while()
		out.writeUTF("해당 아이디가 없습니다...");
	}
	
	void exit() {	//접속종료 메소드
		try {
			Iterator itr = ClientManagements.clientTable.iterator();
			Iterator itr2 = AdminManagements.adminTable.iterator();
			
			while(itr2.hasNext()){		//관리자들한태 뿌리기
				AdminThread t1 = (AdminThread)itr2.next();
				
				if(t1.equals(this))	//자신이 나갔다는 정보를 받을 필요가 없기 때문에
					continue;
				else
					t1.out.writeUTF(this.name + "(관리자)님이 퇴장했습니다...");
				
			}
			
			while(itr.hasNext()){		//유저들한태 뿌리기
					ClientThread t = (ClientThread)itr.next();
						t.out.writeUTF(this.name + "(관리자)님이 퇴장했습니다...");
			}
		} catch (IOException e) { System.out.println("퇴장 기능 오류..!"); }
	}	//exit()
	
	@Override
	public void run(){
		String log;
		
		try {
			log = socket.getInetAddress() + 
					" " + this.name + "[관리자]가 접속했습니다...";
			System.out.println(log);
			ChatLog.writeLog(log);
			
			System.out.println("현재 관리자 접속 수는 : " + AdminManagements.adminTable.size()
			+ "명 입니다.");
			System.out.println("현재 일반 접속자 수는 : " + ClientManagements.clientTable.size()
			+ "명 입니다.");
			out.writeUTF("관리자 모드에 접속했습니다...");
			out.writeUTF("도움말은 '/help' 를 입력하세요...");
			
			while(true){
				msg = in.readUTF();
				st = new StringTokenizer(msg," ");
				command = st.nextToken();
				
				if(command.equals("/help"))	//도움말 출력
					showHelp();
				
				else if(command.equals("/kick"))	//일반유저 추방
					kick(st.nextToken());
				
				else if(command.equals("/list"))	//유저 목록
					list();
				
				else if(command.equals("/id")){		//id변경
					if(!st.hasMoreTokens()){		//id를 안첫을 경우
						this.out.writeUTF("id를 입력하세요...");
						continue;
					}
					else
						searchId(st.nextToken());
				}	//searchId
				
				else if(command.equals("/myinfo"))		//해당 관리자 정보 출력
					showMyInfo();
				
				else if(command.equals("/server")){		//서버로 메시지 전송
					if(!st.hasMoreTokens()){	//할말을 안첫을 경우
						this.out.writeUTF("[할 말]을 입력하세요...");
						continue;
					}
					else
						serverMsg(st.nextToken(""));
				}
				
				else if(command.equals("/dnd")){	//벙어리
					if(!st.hasMoreTokens()){	//id를 안첫을 경우
						this.out.writeUTF("ID를 입력하세요.");
						continue;
					}
					else
						dnd(st.nextToken());
				}
				
				else if(command.equals("/w")){	//귓속말
					if(!st.hasMoreTokens()){	//id를 안첫을 경우
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
				}	//send()
				
				else if(command.equals("/pw")){		//비밀번호 변경
					if(!st.hasMoreTokens()){
						this.out.writeUTF("password를 입력하세요...");
						continue;
					}
					else
						changePw(st.nextToken());
				}	//changePw
				
				else if(command.equals("/time"))	//현재시간
					realTime();
				
				else if(command.equals("/exit"))	//접속종료
					break;
				
				else{		//대화내용 뿌리기
					ChatWrite.writeFile("(관리자)[" + this.name + "] " + msg);
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
			log = "[관리자] \"" + name + "\"(" + socket.getInetAddress() + ") 가 나갔습니다.";
			System.out.println(log);
			try { ChatLog.writeLog(log); } catch (IOException e) { System.out.println("Log파일 저장 에러");}
			System.out.println("현재 관리자 수는 : " + AdminManagements.adminTable.size() + "명 입니다.");
			System.out.println("현재 접속자 수는 : " + ClientManagements.clientTable.size()
								+ "명 입니다.");
		}
	}	//run()
}	//AdminManagement class
