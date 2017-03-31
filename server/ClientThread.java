package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class ClientThread extends Thread {
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	private String userName;
	private long WAIT_TWO_SEC = 2000; // milliseconds

	private String UserNameREQ_TAG = "SUBMIT_NAME";
	private String UserNameCFM_TAG = "USERNAME_ACCEPTED";
	private String UserNameREJ_TAG = "USERNAME_NOT_ACCEPTED";

	private String GetOnlineList_API = "GETONLINELIST";

	public ClientThread(Socket socket) {
		this.socket = socket;
	}// End of constructor

	@Override
	public void run() {
		super.run();
		System.out.println("A Client joined the server");
		try {
			// Create character streams for the socket.
			this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.out = new PrintWriter(socket.getOutputStream(), true);
			sendToClient(UserNameREQ_TAG);

			/**
			 * This while loop verifies the client.
			 * Client accepted if the userName is unique(NOT in our ChatServer.clientMap HashMap).
			 */
			while (true) {
				userName = readFromClient();
				if (userName != null) {
					userName = userName.toLowerCase();
					System.out.println("useName: " + userName);
					synchronized (ChatServer.clientMap) {
						if (ChatServer.clientMap.containsKey(userName) == false) {
							ChatServer.clientMap.put(userName, this.out);
							System.out.println("Username: " + userName + ", joined the server");
							currentOnlineList();
							currentOnlineListToClient();
							sendToClient(UserNameCFM_TAG);
							break;
						} else {
							System.out.println("Username: " + userName + ", allready exist. REJ sent to client.");
							this.userName = null;
							sendToClient(UserNameREJ_TAG);
						}
					} // End of synchronized
				}//End of if(userName != null)
				else{
					System.out.println("Username was NULL.");
					sendToClient(UserNameREJ_TAG);
				}
			} // End of while(true)
			
			/** Client Ready **/
			
			//Start multicast
			while(true){
				sendToAllClients(readFromClient());
			}
			
		} // End of try
		catch (Exception e) {
			// TODO: handle exception
		} // End of catch
		finally {
			removeClient();
		} // End of finally
	}// End of run

	private void clientCommandHandler() throws IOException {
		String apiCall = readFromClient();
	}

	private String readFromClient() throws IOException {
		String temp = this.in.readLine().trim();
		if (temp.isEmpty()){
			return null;
		}
		else{
			return temp;
		}
	}// End of readFromClient

	private void sendToClient(String mString) {
		this.out.println(mString);
	}// End of SendToClient

	private void sendToAllClients(String mString) {
		if(mString != null){
			System.out.println("*** Conversation start ***");
			synchronized (ChatServer.clientMap) {
				for(Map.Entry<String, PrintWriter> entry : ChatServer.clientMap.entrySet()) {
					String name = entry.getKey();
					PrintWriter out = entry.getValue();
					if(this.out != out){
						//out.println(mString);
						System.out.println(this.userName + " sent \"" + mString + "\" to " + name);
					}
				}
			}
			System.out.println("*** Conversation end ***\n");
		}
	}// End of sendToAllClients
	
	private void currentOnlineListToClient() {
		synchronized (ChatServer.clientMap) {
			for(Map.Entry<String, PrintWriter> entry : ChatServer.clientMap.entrySet()) {
			    String key = entry.getKey();
			    //PrintWriter out = entry.getValue();
			    sendToClient(key);
			}
		}
	}//End of currentOnlineListToClient


	private void currentOnlineList() {
		System.out.println("In currentOnlineList: ");
		synchronized (ChatServer.clientMap) {
			for(Map.Entry<String, PrintWriter> entry : ChatServer.clientMap.entrySet()) {
			    String key = entry.getKey();
			    System.out.println(key);
			}
		}
	}//End of currentOnlineList

	private synchronized void removeClient(){
		synchronized (ChatServer.clientMap) {
			if (ChatServer.clientMap.containsKey(this.userName) == true) {
				ChatServer.clientMap.remove(this.userName);
				System.out.println(this.userName + ", left the server");
				this.userName = null;
				try {
					this.in.close();
					this.out.close();
					this.socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}// End of if
			currentOnlineList();
		} // End of synchronized
	}//End of removeClient

}// End of class
