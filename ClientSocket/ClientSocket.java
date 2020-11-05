package ClientSocket;

import java.util.*;

interface Keyborad{		//입력받기 편하게 하기 위해서 Scanner 인터페이스 선언
	static Scanner In = new Scanner(System.in);
}

class MainMenu implements Keyborad{
	static private String ipAddress;
	static private int portNum;
	static private SocketAccept socketAccept;
	
	static void menu(){			//초기 화면 매뉴
		System.out.println("1. 채팅서버 접속하기");
		System.out.println("2. 나가기");
	}	//menu()
	
	static void chatMenu(){			//채팅 서버 접속하기 위한 매뉴
		System.out.println("서버 ip주소 입력 : ");
		ipAddress = Keyborad.In.nextLine();	//ip주소 입력
		System.out.println("포트번호 입력 : ");
		portNum = Keyborad.In.nextInt();	//포트번호 입력
		Keyborad.In.nextLine();
		socketAccept = new SocketAccept(ipAddress, portNum);	//ip주소와 port번호를 전달
		socketAccept.socketCreate();
	}	//chatMenu()
}	//MainMenu

class MenuChoiceException extends Exception{	//menu 예외처리 클래스 생성
	private int choice;
	
	MenuChoiceException(int choice){
		System.out.println("잘못된 선택입니다...");
		this.choice = choice;
	}	//MenuChoiceException Constructor

	void showError(){
		System.out.println(choice + "에 해당하는 선택은 존재하지 않습니다.");
	}	//showError()
} //MenuChoiceException

public class ClientSocket implements Keyborad{
	public static void main(String[] args){
		
		while(true){
			MainMenu.menu();
			int choice = Keyborad.In.nextInt();
			Keyborad.In.nextLine();
		
			try{
				if(choice > 2 || choice < 1)	//1보다 작거나 2보다 크면 예외처리 발생
					throw new MenuChoiceException(choice);
				
				switch(choice)		//메뉴 선택
				{
				case 1:
					MainMenu.chatMenu();
					return;
				case 2:
					System.out.println("프로그램 종료...");
					return;
				}
			}
			catch(MenuChoiceException e){
				e.showError();
				System.out.println("메뉴 선택을 처음부터 다시 진행합니다.");
			}	//try()
		}	//while()	
	}	//main()
}	//ClientSocket