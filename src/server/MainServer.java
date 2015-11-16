package server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

public class MainServer {
	private static ArrayList<Integer> uniqueIdentifiers = new ArrayList<Integer>();
	private static int MaxNumOfServers = 4;
	private static final int portNumber = 1234;// This port will be constant for
												// all the other server to
												// connect to it
	private static ArrayList<ConnectedServer> connectedServers;
	private ServerSocket MainServerSocket;
	private static HashMap<Integer, ArrayList<String>> getOnlineListByServerID;
	private static HashMap<String, String[]> dataBase;
	private Socket tempSocket;

	public MainServer() {
		uniqueIdentifiers = new ArrayList<Integer>();
		for (int i = 0; i < MaxNumOfServers; i++) {
			uniqueIdentifiers.add(i + 1);
		}
		getOnlineListByServerID = new HashMap<Integer, ArrayList<String>>();
		try {
			MainServerSocket = new ServerSocket(portNumber);
			System.out.println("The Main Server is Running on port: "
					+ portNumber);
			loadUsers();
			displayDataBase();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		MainServer.connectedServers = new ArrayList<ConnectedServer>();
		while (true) {
			try {
				tempSocket = MainServerSocket.accept();
				PrintWriter tempPriner = new PrintWriter(
						tempSocket.getOutputStream(), true);
				if (uniqueIdentifiers.size() == 0) {
					tempPriner.println("main server is full");
				} else {
					int serverID = uniqueIdentifiers.remove(0);
					getOnlineListByServerID.put(serverID,
							new ArrayList<String>());
					String onlineUsers = "";
					for (int i = 0; i < MaxNumOfServers; i++) {
						if (getOnlineListByServerID.containsKey(i + 1)) {
							ArrayList<String> list = getOnlineListByServerID
									.get(i + 1);
							for (String user : list) {
								onlineUsers += user + "#";
							}
						}
					}
					tempPriner.println(serverID);
					Socket serverRegSocket = MainServerSocket.accept();
					tempPriner.println(onlineUsers);
					ConnectedServer server = new ConnectedServer(tempSocket,
							serverRegSocket, serverID);
					connectedServers.add(server);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// loads the saved hash map object from database.ser
	private void loadUsers() {
		// deserialize the database.ser file
		try (InputStream file = new FileInputStream("database.ser");
				InputStream buffer = new BufferedInputStream(file);) {
			if (buffer.available() <= 0) {
				dataBase = new HashMap<String, String[]>();
				dataBase.put("test123", new String[] { "1234", "test" });
				save();
				return;
			}
			ObjectInput input = new ObjectInputStream(buffer);
			// deserialize the map
			@SuppressWarnings("unchecked")
			HashMap<String, String[]> temp = (HashMap<String, String[]>) input
					.readObject();
			dataBase = temp;
			input.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private void clearDataBase() {
		dataBase = new HashMap<String, String[]>();
		save();
		loadUsers();
	}

	public void insertIntoDataBase(String uid, String password, String name) {
		dataBase.put(uid, new String[] { password, name });
		save();
	}

	private void displayDataBase() {
		Set<String> set = dataBase.keySet();
		Object[] uids = set.toArray();
		System.out.println("Registered Users in DataBase:");
		System.out.println("-----------------");
		for (int i = 0; i < uids.length; i++) {
			String password = dataBase.get(uids[i])[0];
			String name = dataBase.get(uids[i])[1];
			System.out.printf(
					"%d. uid: <%s>     password: <%s>     name: <%s>\n",
					(i + 1), uids[i], password, name);
		}
		System.out.println("-----------------");
	}

	// serialize the map
	private void save() {
		try (OutputStream file = new FileOutputStream("database.ser");
				OutputStream buffer = new BufferedOutputStream(file);
				ObjectOutput output = new ObjectOutputStream(buffer);) {
			output.writeObject(dataBase);
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	class ConnectedServer implements Runnable {
		private BufferedReader inFromSocketReader, regReader;
		private PrintWriter outFromSocketPrinter, regPrinter;
		private Socket connectedServerSocket, registrationSocket;
		private Thread readingFromServerThread, regThread;
		private int ServerID;

		public ConnectedServer(Socket socket, Socket regSocket, int ID) {
			try {
				connectedServerSocket = socket;
				registrationSocket = regSocket;
				inFromSocketReader = new BufferedReader(new InputStreamReader(
						connectedServerSocket.getInputStream()));
				regReader = new BufferedReader(new InputStreamReader(
						registrationSocket.getInputStream()));
				regPrinter = new PrintWriter(regSocket.getOutputStream(), true);
				outFromSocketPrinter = new PrintWriter(
						connectedServerSocket.getOutputStream(), true);
				this.ServerID = ID;
				System.out.println("Server " + ID
						+ " is now Connected to the main server");
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			//
			readingFromServerThread = new Thread(this);
			regThread = new Thread(this);
			readingFromServerThread.start();
			regThread.start();
		}

		private void processLoginOrSignUpRequest(String[] message) {
			try {
				if (message[0].equals("login")) {
					String ID = message[1];
					String ServerID = message[3];
					if (!dataBase.containsKey(ID)) {
						regPrinter.println("Incorrect id or password");
						return;
					} else {
						for (ConnectedServer server : connectedServers) {
							if (getOnlineListByServerID
									.containsKey(server.ServerID)
									&& getOnlineListByServerID.get(
											server.ServerID).contains(ID)) {
								regPrinter.println("logged in");
								return;
							}
						}
					}
					if (!dataBase.get(ID)[0].equals(message[2])) {
						regPrinter.println("Incorrect id or password");
						return;
					}
					// here the client has logged in so his ID must be stored
					getOnlineListByServerID.get(Integer.parseInt(ServerID))
							.add(ID);
					sendToAll("joined#" + ID);
					regPrinter.println("ok");
					return;
				} else {
					// signup
					if (dataBase.containsKey(message[1])) {
						regPrinter.println("Incorrect id or password");
					} else {
						insertIntoDataBase(message[1], message[2], message[3]);
						regPrinter.println("ok");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		@Override
		public void run() {
			if (Thread.currentThread() == readingFromServerThread) {
				String message;
				while (true) {
					try {
						message = inFromSocketReader.readLine();
						if (message == null)
							continue;
						String content[] = message.split("#");
						if (content[0].equals("joined")) {
							getOnlineListByServerID.get(this.ServerID).add(
									content[1]);
							sendToAll("joined#" + content[1]);
						} else if (content[0].equals("disconnect")) {
							// this message is of the form "disconnect#" +
							// senderID
							getOnlineListByServerID.get(this.ServerID).remove(
									content[1]);
							sendToAll("disconnect#" + content[1]);
						} else if (content[0].equals("All")) {
							sendToAll("All#" + content[1] + "#" + content[2]);
						} else {
							// private message
							String senderID = content[0];
							String toID = content[1];
							sendTo(toID, senderID + "#" + toID + "#"
									+ content[2]);
						}

					} catch (Exception e) {
						if (getOnlineListByServerID.containsKey(ServerID)) {
							ArrayList<String> list = getOnlineListByServerID
									.remove(ServerID);
							System.out.println("Server " + ServerID
									+ " has Disconnected from the main server");
							connectedServers.remove(this);
							uniqueIdentifiers.add(this.ServerID); // to reuse
							// again
							Collections.sort(uniqueIdentifiers);
							for (String user : list) {
								sendToAll("disconnect#" + user);
							}
							break;
						}
					}
				}
			} else if (Thread.currentThread() == regThread) {
				String message;
				while (true) {
					try {
						message = regReader.readLine();
						if (message == null)
							continue;
						processLoginOrSignUpRequest(message.split("#"));
					} catch (Exception e) {
						if (getOnlineListByServerID.containsKey(ServerID)) {
							ArrayList<String> list = getOnlineListByServerID
									.remove(ServerID);
							System.out.println("Server " + ServerID
									+ " has Disconnected from the main server");
							connectedServers.remove(this);
							uniqueIdentifiers.add(this.ServerID); // to reuse
							// again
							Collections.sort(uniqueIdentifiers);
							for (String user : list) {
								sendToAll("disconnect#" + user);
							}
							break;
						}
					}
				}
			}

		}

		private void sendTo(String toID, String message) {
			for (ConnectedServer server : connectedServers) {
				if (getOnlineListByServerID.get(server.ServerID).contains(toID)) {
					server.outFromSocketPrinter.println(message);
				}
			}
		}

		private void sendToAll(String message) {
			for (ConnectedServer server : connectedServers) {
				server.outFromSocketPrinter.println(message);
			}
		}
	}

	public static void main(String[] args) {
		new MainServer();
	}
}
