package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

import util.Utilities;

public class Client {

	private PrintStream clientOutputPrintWriter;
	private BufferedReader serverResponseReader;
	private Socket clientSocket;
	private String ID, password, hostname, port;

	public Client(Socket socket) {
		clientSocket = socket;
		try {
			clientOutputPrintWriter = new PrintStream(
					clientSocket.getOutputStream());
			serverResponseReader = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setPort(String port) {
		this.port = port;
	}

	public int getPort() {
		return Integer.parseInt(port);
	}

	public void setHostName(String hostname) {
		this.hostname = hostname;
	}

	public String getHostName() {
		return this.hostname;
	}

	public void setID(String ID) {
		this.ID = ID;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getID() {
		return this.ID;
	}

	public boolean changeHost(String hostname, String port) {
		try {
			Utilities.cleanResources(clientOutputPrintWriter, clientSocket,
					serverResponseReader);
			clientSocket = new Socket(hostname, Integer.parseInt(port));
			clientOutputPrintWriter = new PrintStream(
					clientSocket.getOutputStream());
			serverResponseReader = new BufferedReader(new InputStreamReader(
					clientSocket.getInputStream()));
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	// getting Response for logging in or signing up
	public String getResponseFromServer(String type, String ID,
			String password, String name) {
		if (type.equals("signup"))
			clientOutputPrintWriter.println(type + "#" + ID + "#" + password
					+ "#" + name);
		else
			clientOutputPrintWriter.println(type + "#" + ID + "#" + password);
		while (true) {
			try {
				if (serverResponseReader.ready()) {
					return serverResponseReader.readLine();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void disconnectMe() {
		// Disconnect and Close Every thing
		clientOutputPrintWriter.println("disconnect");
		try {
			clientOutputPrintWriter.close();
			serverResponseReader.close();
			closeSocket();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void closeSocket() {
		new Thread() {
			public void run() {
				synchronized (clientSocket) {
					try {
						clientSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	// From#To#Message
	public void forwardMessageTo(String to, String message) {
		if (message.equals("BYE") || message.equals("QUIT")) {
			disconnectMe();
			return;
		}
		// messages can't contain # "ra5ama :P"
		message = message.replace('#', '*');
		clientOutputPrintWriter.println(to + "#" + message);
	}

	public BufferedReader getServerResponseReader() {
		return this.serverResponseReader;
	}

	
}
