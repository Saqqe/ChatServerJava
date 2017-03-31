package server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.HashSet;

/**
 * A multi threaded chat room server. When a client connects the server requests
 * a screen name by sending the client the text "SUBMITNAME", and keeps
 * requesting a name until a unique one is received. After a client submits a
 * unique name, the server acknowledges with "NAMEACCEPTED". Then all messages
 * from that client will be broadcast to all other clients that have submitted a
 * unique screen name. The broadcast messages are prefixed with "MESSAGE ".
 *
 * Because this is just a teaching example to illustrate a simple chat server,
 * there are a few features that have been left out. Two are very useful and
 * belong in production code:
 *
 * 1. The protocol should be enhanced so that the client can send clean
 * disconnect messages to the server.
 *
 * 2. The server should do some logging.
 */
public class ChatServer {

	/**
	 * The port that the server listens on.
	 */
	private static final int PORT = 9001;

	/**
	 * This hashMap will contain the userName and PrintWrites to them.
	 */
	protected static HashMap<String, PrintWriter> clientMap = new HashMap<String, PrintWriter>();

	/**
	 * The application main method, which just listens on a port and spawns
	 * handler threads.
	 * 
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		ServerSocket listener = new ServerSocket(PORT);
		System.out.println("The chat server is running on port: " + PORT);
		try {
			while (true) {
				new ClientThread(listener.accept()).start();
			}
		} finally {
			listener.close();
		}
	}// End of main
}// End of class