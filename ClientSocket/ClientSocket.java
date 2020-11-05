package ClientSocket;

import java.util.*;

interface Keyborad{		//�Է¹ޱ� ���ϰ� �ϱ� ���ؼ� Scanner �������̽� ����
	static Scanner In = new Scanner(System.in);
}

class MainMenu implements Keyborad{
	static private String ipAddress;
	static private int portNum;
	static private SocketAccept socketAccept;
	
	static void menu(){			//�ʱ� ȭ�� �Ŵ�
		System.out.println("1. ä�ü��� �����ϱ�");
		System.out.println("2. ������");
	}	//menu()
	
	static void chatMenu(){			//ä�� ���� �����ϱ� ���� �Ŵ�
		System.out.println("���� ip�ּ� �Է� : ");
		ipAddress = Keyborad.In.nextLine();	//ip�ּ� �Է�
		System.out.println("��Ʈ��ȣ �Է� : ");
		portNum = Keyborad.In.nextInt();	//��Ʈ��ȣ �Է�
		Keyborad.In.nextLine();
		socketAccept = new SocketAccept(ipAddress, portNum);	//ip�ּҿ� port��ȣ�� ����
		socketAccept.socketCreate();
	}	//chatMenu()
}	//MainMenu

class MenuChoiceException extends Exception{	//menu ����ó�� Ŭ���� ����
	private int choice;
	
	MenuChoiceException(int choice){
		System.out.println("�߸��� �����Դϴ�...");
		this.choice = choice;
	}	//MenuChoiceException Constructor

	void showError(){
		System.out.println(choice + "�� �ش��ϴ� ������ �������� �ʽ��ϴ�.");
	}	//showError()
} //MenuChoiceException

public class ClientSocket implements Keyborad{
	public static void main(String[] args){
		
		while(true){
			MainMenu.menu();
			int choice = Keyborad.In.nextInt();
			Keyborad.In.nextLine();
		
			try{
				if(choice > 2 || choice < 1)	//1���� �۰ų� 2���� ũ�� ����ó�� �߻�
					throw new MenuChoiceException(choice);
				
				switch(choice)		//�޴� ����
				{
				case 1:
					MainMenu.chatMenu();
					return;
				case 2:
					System.out.println("���α׷� ����...");
					return;
				}
			}
			catch(MenuChoiceException e){
				e.showError();
				System.out.println("�޴� ������ ó������ �ٽ� �����մϴ�.");
			}	//try()
		}	//while()	
	}	//main()
}	//ClientSocket