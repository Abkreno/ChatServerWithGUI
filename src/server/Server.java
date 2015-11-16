package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import util.Utilities;

public class Server implements Runnable {
	private static final int mainServerPort = 1234;
	private int ServerUniqueID, portNumber;
	private String hostName;
	private ServerSocket welcomingSocket;
	private Socket serverSocket, mainServerSocket, registrationSocket;
	private static ArrayList<ConnectedClient> connectedClients;
	private static ArrayList<String> ConnectedClientsInMainServer;
	private BufferedReader serverStdin, inFromMainServer, regReader;
	private static PrintWriter outToMainServer, regPrinter;
	private boolean running = false;
	private Thread acceptClients, readFromMain;

	public Server() {
		connectedClients = new ArrayList<ConnectedClient>();
		ConnectedClientsInMainServer = new ArrayList<String>();
		this.launchServer();
	}

	private boolean connectToMainServer() {
		try {
			mainServerSocket = new Socket(hostName, mainServerPort);
			inFromMainServer = new BufferedReader(new InputStreamReader(
					mainServerSocket.getInputStream()));
			outToMainServer = new PrintWriter(
					mainServerSocket.getOutputStream(), true);
			String inFromMain = inFromMainServer.readLine();
			if (inFromMain.equals("main server is full")) {
				System.err.println("The Main Server is full!");
				return false;
			}
			registrationSocket = new Socket(hostName, mainServerPort);
			regReader = new BufferedReader(new InputStreamReader(
					registrationSocket.getInputStream()));
			regPrinter = new PrintWriter(registrationSocket.getOutputStream(),
					true);
			ServerUniqueID = Integer.parseInt(inFromMain);
			String onlineUsers[] = inFromMainServer.readLine().split("#");
			for (int i = 0; i < onlineUsers.length; i++) {
				ConnectedClientsInMainServer.add(onlineUsers[i]);
			}
			running = true; // to let us know that we are now connected to the
							// main server
		} catch (Exception e1) {
			System.err.println("The Main Server is now offline!");
			return false;
		}
		return true;
	}

	public void launchServer() {
		try {
			// Server standard input to get info (i.e port number ... )
			serverStdin = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Please Enter the port number :");
			portNumber = Integer.parseInt(serverStdin.readLine());
			while (portNumber == mainServerPort) {
				System.out
						.println("This port can only be used by the main server, please enter another port: ");
				portNumber = Integer.parseInt(serverStdin.readLine());
			}
			welcomingSocket = new ServerSocket(portNumber);
			System.out.println("Please Enter the host name :");
			// hostName = serverStdin.readLine();
			hostName = "localhost";
			if (!connectToMainServer()) {
				return;
			}
			System.out.println("Server running on port: " + portNumber);
			if (running) {
				acceptClients = new Thread(this);
				readFromMain = new Thread(this);
				acceptClients.start();
				readFromMain.start();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		if (Thread.currentThread() == acceptClients) {
			while (running) {
				try {
					serverSocket = welcomingSocket.accept();
					String ID = processLoginOrSignUpRequest(
							new BufferedReader(new InputStreamReader(
									serverSocket.getInputStream())),
							new PrintWriter(serverSocket.getOutputStream(),
									true));
					if (ID == null) {
						// this means that the connected client has disconnected
						continue;
					}
					ConnectedClient connectedClient = new ConnectedClient(
							serverSocket, ID);
					connectedClients.add(connectedClient);
				} catch (IOException e) {
					e.printStackTrace();
					running = false;
					break;
				}
			}
		} else if (Thread.currentThread() == readFromMain) {
			while (running) {
				try {
					String[] message = inFromMainServer.readLine().split("#");
					if (message[0].equals("joined")) {
						// to announce that someone has logged in
						ConnectedClientsInMainServer.add(message[1]);
						sendMessage("joined#" + message[1]);
						continue;
					} else if (message[0].equals("All")) {
						sendMessage(message[1] + "#All#" + message[2]);
					} else if (message.length == 3) {
						// private message to me
						String senderID = message[0];
						String recieverID = message[1];
						String content = message[2];
						for (ConnectedClient c : connectedClients) {
							if (c.ID.equals(recieverID)) {
								c.showMessage(senderID + "#" + recieverID + "#"
										+ content);
							}
						}
					} else {
						ConnectedClientsInMainServer.remove(message[1]);
						sendMessage("disconnect#" + message[1]);
					}

				} catch (IOException e) {
					running = false;
					System.err.println("Main Server has Disconnected ");
					break;
				}

			}
		}
	}

	// processing the first message which could be login request or signup
	// request
	private String processLoginOrSignUpRequest(
			BufferedReader inFromSocketReader, PrintWriter outFromSocketPrinter) {
		try {
			String in = inFromSocketReader.readLine();
			if (in == null) {
				// try again
				return processLoginOrSignUpRequest(inFromSocketReader,
						outFromSocketPrinter);
			}
			String[] m = in.split("#");

			if (m[0].equals("disconnect")) {
				return null;
			}
			String type = m[0];
			String ID = m[1];
			String password = m[2];
			if (type.equals("login")) {
				regPrinter.println("login#" + ID + "#" + password + "#"
						+ ServerUniqueID);
				String message = regReader.readLine();
				if (message.equals("ok")) {
					String onlineUsers = "";
					for (String clientID : ConnectedClientsInMainServer) {
						onlineUsers += "#" + clientID;
					}
					outFromSocketPrinter.println(onlineUsers);
					// sendMessage("joined#" + ID); // to announce that u
					// logged in .. has been handeled in the main server
					return ID;
				} else {
					outFromSocketPrinter.println(message);
					processLoginOrSignUpRequest(inFromSocketReader,
							outFromSocketPrinter);
				}
			} else if (type.equals("signup")) {
				String name = m[3];
				regPrinter
						.println("signup#" + ID + "#" + password + "#" + name);
				String message = regReader.readLine();
				if (!message.equals("ok")) {
					outFromSocketPrinter.println("This ID is already in use!");
					processLoginOrSignUpRequest(inFromSocketReader,
							outFromSocketPrinter);
				}
				outFromSocketPrinter.println("ok");
				processLoginOrSignUpRequest(inFromSocketReader,
						outFromSocketPrinter);// to get the login request
												// now
			}
		} catch (SocketException e) {
			return null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void sendMessage(String message) {
		for (ConnectedClient client : connectedClients) {
			client.showMessage(message);
		}
	}

	class ConnectedClient implements Runnable {

		private Socket toClientSocket;
		private BufferedReader inFromSocketReader;
		private PrintWriter outFromSocketPrinter;

		private String ID;
		private boolean running = false;
		private Thread readingFromClientThread;

		public ConnectedClient(Socket socket, String ID) {
			this.toClientSocket = socket;
			this.ID = ID;
			try {
				inFromSocketReader = new BufferedReader(new InputStreamReader(
						toClientSocket.getInputStream()));
				outFromSocketPrinter = new PrintWriter(
						toClientSocket.getOutputStream(), true);

				// Processing the first message which is either login request or
				// sign up request

				// Initializing and running the threads

				running = true;
				readingFromClientThread = new Thread(this);
				readingFromClientThread.start();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public String getID() {
			return this.ID;
		}

		/**
		 * Sending a message to <strong>this</strong> particular
		 * {@code connectedClient}
		 * 
		 * @param message
		 *            the message to be sent
		 */
		private void showMessage(String message) {
			if (!toClientSocket.isConnected()) {
				killMe();
				return;
			}
			outFromSocketPrinter.println(message);
		}

		/**
		 * Sending a message to all the clients connected to this server
		 * 
		 * @param message
		 *            the message to be sent
		 */
		// private void sendMessage(String message) {
		// for (ConnectedClient client : connectedClients) {
		// client.showMessage(message);
		// }
		// }

		// message in for ID#message
		private void sendMessage(String toID, String message) {
			if (toID.equals(this.ID)) {
				showMessage("Sending a -private- message to yourself O.o ?");
			}

			for (ConnectedClient client : connectedClients) {
				if (client.ID.equals(toID)) {
					client.showMessage(message);
					return;
				}
			}
			// not found here send it to the main server to handle it
			outToMainServer.println(message);
		}

		@Override
		public void run() {
			if (running && Thread.currentThread() == readingFromClientThread) {
				String fromClient = null;
				while (running) {
					try {
						fromClient = inFromSocketReader.readLine();
						if (fromClient == null)
							continue;
						String content[] = fromClient.split("#");
						// If the user want to logout
						if ((content[0].equals("disconnect"))) {
							connectedClients.remove(this);
							// sendMessage("disconnect#" + this.ID);
							outToMainServer.println("disconnect#" + this.ID);
							break;
						}
						if (content.length != 2)
							continue;
						String toID = content[0];
						String message = content[1];
						System.out.println("message from " + ID + " to " + toID
								+ " : " + message);
						// If the user want to know who is connected
						if (toID.equals("All")) {
							// If the user is sending a message to all
							outToMainServer
									.println("All#" + ID + "#" + message);
						} else {
							// private message
							sendMessage(toID, ID + "#" + toID + "#" + message);
						}
					} catch (SocketException e) {
						System.err.println("Socket Problem!");
						e.printStackTrace();
						running = false;
						killMe();
						break;
					} catch (IOException e) {
						e.printStackTrace();
						running = false;
						killMe();
						break;
					}
				}
			}
		}

		// Kill Em' :|
		private void killMe() {
			Utilities.cleanResources(inFromSocketReader, outFromSocketPrinter,
					toClientSocket);
		}
	}

	public static void main(String[] args) {
		new Server();
	}

}
