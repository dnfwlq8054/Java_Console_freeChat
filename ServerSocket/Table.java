package ServerSocket;

import java.util.Hashtable;
import java.util.Vector;

public interface Table {	//�Ҵ��� �������� ������ ��� ��ä, ����, �ؽ� ���̺� ����
		ClientManagements client = new ClientManagements();		
		AdminManagements admin = new AdminManagements();
		Vector<AdminThread> adminTable = new Vector<AdminThread>(5);
		Vector<ClientThread> clientTable = new Vector<ClientThread>(20);
		Hashtable<String, AdminInfo> hashTable = new Hashtable<String, AdminInfo>(5);
	}
