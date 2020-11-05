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
	
	public void init(Socket socket) throws IOException{		//메뉴 초기화
		this.socket = socket;
	}	//init()
	
	public void access(Socket socket) throws IOException{
			client.threadIn(socket);		//일반유저 쓰레드 생성
			return;
	}
	
	public void access(Socket socket, String id, String pw) throws IOException{
			admin.threadIn(socket, id, pw);		//관리자 쓰레드 생성
			return;
	}	//menu()
	
	public void start() throws IOException{		//로그인 매뉴 스타트
		accessMenu = new AccessMenu(socket);
		accessMenu.start();
	}
}

class AccessMenu extends Thread{		//회원가입하는 쓰레드
	private DataOutputStream out;	
	private DataInputStream in;
	private Socket socket;
	private String choice;
	private ServerMenu serverMenu;
	private final ReentrantLock key = new ReentrantLock();	//key생성
	private ServerInput sv = new ServerInput();	//관리자 정보를 저장하기 위해서
	
	AccessMenu(Socket socket) throws IOException{
		this.serverMenu = new ServerMenu();
		this.socket = socket;
		this.out = new DataOutputStream(socket.getOutputStream());
		this.in = new DataInputStream(socket.getInputStream());
	}	//AccessMenu Constructor
	
	@Override
	public void run(){	
		Random random = new Random();		//인증번호를 위한 랜덤클레스
		String id=null, password=null, name=null, district=null;
		int authen;
		String[] menu = {
				"1. 관리자 모드",
				"2. 일반 모드",
				"3. 종료"
		};
		
		while(true){
			try {
				for(int i = 0; i < menu.length; i++)
					out.writeUTF(menu[i]);
				choice = in.readUTF();
				
				if(choice.equals("1")){
					out.writeUTF("1. 로그인");
					out.writeUTF("2. 회원가입");
					out.writeUTF("3. 돌아가기");
					choice = in.readUTF();
					
					if(choice.equals("1")){
						out.writeUTF("아이디  : ");
						id = in.readUTF();
						out.writeUTF("비밀번호  : ");
						password = in.readUTF();
						
						if(MemberAdmin.hashTable.containsKey(id)){		//id검사
							if(password.equals(MemberAdmin.hashTable.get(id).password)){	//password검사
								if(MemberAdmin.hashTable.get(id).online){	//해당 ID가 접속중인지 아닌지 검사
									out.writeUTF("현재 해당 ID는 로그인 중입니다..");
									continue;
								}
								else{
									serverMenu.access(socket, id, password);
									return;
								}
							}
							else{
								System.out.println("2");
								out.writeUTF("아이디 혹은 비밀번호 틀림!!");
							}
						}
						else
							out.writeUTF("아이디 혹은 비밀번호 틀림!!");
						
					}else if(choice.equals("2")){		//회원가입 
						out.writeUTF("***회원 가입***");
						out.writeUTF("아이디 입력");
						id = in.readUTF();
						out.writeUTF("아이디 중복검사중...");
						
						key.lock();		//ID검사가 동시에 이뤄지면 중복될 수 있기 때문에 lock()메소드 호출
						if(MemberAdmin.hashTable.containsKey(id)){
							out.writeUTF("중복된 아이디가 있음!!..");
							out.writeUTF("다시 입력하세요..");
							continue;
						}
						key.unlock();
						
						out.writeUTF("비밀번호 입력");
						password = in.readUTF();
						out.writeUTF("이름 입력 : ");
						name = in.readUTF();
						out.writeUTF("인증번호 입력");
						
						authen = random.nextInt(90000) + 10000;	//인증번호 생성
						System.out.println(id + "의 대한 인증번호 : " + authen);
						district = "" + authen;		//인증번호 String으로 바꾸는 작업
						
						for(int i = 1; i <=3; i++){
							if(district.equals(in.readUTF())){
								out.writeUTF("인증번호 입력성공!!");
								MemberAdmin.hashTable.put(id, new AdminInfo(id, password, name));
								sv.saveInfo();	//계정들 저장
								break;
							}
							else
								out.writeUTF("다시 입력하세요..." + i + "/3");
							if(i == 3)
								out.writeUTF("인증 실패..");
						}//for()
					}//회원가입 if
					else if(choice.equals("3"))
						continue;
				}// 초기메뉴 1번선택  if
				else if(choice.equals("2")){	//일반유저 접속
					serverMenu.access(socket);
					return;
				}
				else if(choice.equals("3")){
					try{ this.socket.close(); }
					catch(IOException e){}
					System.out.println(socket.getInetAddress() + "이 메뉴선택창에서 종료했습니다.");
					break;
				}
				
				else
					out.writeUTF("다시 입력하세요...");
				
			}	//try() 
			catch (IOException e) { 
					System.out.println(socket.getInetAddress() + "이 나갔습니다."); 
					return;
			}
		}	//while()
	}	//run()
}	//Input class