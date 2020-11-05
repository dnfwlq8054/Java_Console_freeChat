package ServerSocket;

import java.util.Hashtable;
import java.util.Vector;

public interface Table {	//할당할 쓰래드의 정보를 담는 객채, 백터, 해쉬 테이블 선언
		ClientManagements client = new ClientManagements();		
		AdminManagements admin = new AdminManagements();
		Vector<AdminThread> adminTable = new Vector<AdminThread>(5);
		Vector<ClientThread> clientTable = new Vector<ClientThread>(20);
		Hashtable<String, AdminInfo> hashTable = new Hashtable<String, AdminInfo>(5);
	}
