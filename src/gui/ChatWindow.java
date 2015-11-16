package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

import util.Utilities;
import client.Client;

public class ChatWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private OnlineList onlineList;
	private JMenuBar menuBar;
	private JMenu mnFile;
	private JMenuItem mntmOnlineUsers;
	private JMenuItem mntmExit;
	private JInternalFrame internalFrame;
	private JTextArea history;
	private DefaultCaret caret;
	private JTextField txtMessage;

	private Thread clientReadingThread;
	private BufferedReader serverResponseReader;
	private boolean running = false;

	private static Client client;
	private HashSet<String> id_chatRooms;
	private ArrayList<ChatRoom> chatRooms;

	public ChatWindow(String ID, LinkedList<String> online, Client client) {
		setForeground(SystemColor.controlDkShadow);
		ChatWindow.client = client;
		id_chatRooms = new HashSet<String>();
		chatRooms = new ArrayList<ChatRoom>();
		createWindow();
		for (int i = 0; i < online.size(); i++) {
			if (online.get(i).toString().equals(ID)
					|| onlineList.contains(online.get(i).toString()))
				continue;
			onlineList.addUser(online.get(i).toString());
		}
		setVisible(true);
		serverResponseReader = client.getServerResponseReader();
		startRecieving();
	}

	public void startRecieving() {
		clientReadingThread = new Thread("") {
			public void run() {
				try {
					if (Thread.currentThread() == clientReadingThread) {
						String messageFromServer;
						while (running) {
							messageFromServer = serverResponseReader.readLine();
							if (messageFromServer == null)
								continue;
							if (messageFromServer.equals("MainDisconnected")) {
								closeWindow();
								return;
							}
							processMessage(messageFromServer);
						}
					}
				} catch (Exception e) {

				} finally {
					Utilities.cleanResources(serverResponseReader);
					System.exit(0); // Terminating !!
				}

			}
		};
		running = true;
		clientReadingThread.start();
	}

	private void closeWindow() {
		if (client != null)
			client.disconnectMe();
		running = false;
		final JPanel panel = new JPanel();
		panel.setSize(230, 200);
		panel.setForeground(Color.red);
		JOptionPane.showMessageDialog(panel,
				"The Main Server has disconnected! please try again later",
				"Server", JOptionPane.WARNING_MESSAGE);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		setVisible(false);
		System.exit(0);
	}

	private void createWindow() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (client != null)
					client.disconnectMe();
				running = false;
			}

			public void windowClosed(WindowEvent e) {
				if (client != null)
					client.disconnectMe();
				running = false;
			}
		});
		setSize(450, 306);
		setLocationRelativeTo(null);
		setResizable(false);
		contentPane = new JPanel();
		contentPane.setBackground(SystemColor.textHighlight);
		setContentPane(contentPane);
		contentPane.setLayout(null);
		internalFrame = new JInternalFrame("Online");
		internalFrame.setClosable(true);
		internalFrame
				.setToolTipText("Online People! \r\nDouble Click on any of them to start a conversation");
		internalFrame
				.setFrameIcon(new ImageIcon(
						ChatWindow.class
								.getResource("/com/sun/java/swing/plaf/windows/icons/DetailsView.gif")));
		internalFrame.setBorder(null);
		internalFrame.setEnabled(false);
		internalFrame.setBounds(335, 0, 109, 251);
		final Dimension internalFrameDimension = internalFrame.getSize();
		final Point internalFrameLocation = internalFrame.getLocation();
		// To make the internalFrame unmovable
		internalFrame.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				internalFrame.setSize(internalFrameDimension);
			}

			public void componentMoved(ComponentEvent e) {
				internalFrame.setLocation(internalFrameLocation);
			}
		});
		onlineList = new OnlineList();
		onlineList.list.setBackground(SystemColor.windowText);
		onlineList.list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		onlineList.list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && !e.isConsumed()) {
					String ID = onlineList.getSelectedUserID();
					startNewChat(ID);
				}
			}
		});
		internalFrame.setContentPane(onlineList);
		contentPane.add(internalFrame);

		internalFrame.setVisible(true);
		createMenu();
		createAllChat();
	}

	private void createAllChat() {
		history = new JTextArea();
		history.setBackground(SystemColor.inactiveCaption);
		history.setFont(new Font("Courier New", Font.PLAIN, 12));
		history.setEditable(false);
		JScrollPane scroll = new JScrollPane(history);
		scroll.setBounds(3, 3, 327, 220);
		contentPane.add(scroll);
		caret = (DefaultCaret) history.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		txtMessage = new JTextField();
		txtMessage.setBounds(3, 225, 227, 26);
		txtMessage.setColumns(10);
		txtMessage.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					client.forwardMessageTo("All", txtMessage.getText());
					txtMessage.setText("");
				}
			}
		});
		contentPane.add(txtMessage);

		JButton SendToAll = new JButton("SendToAll");
		SendToAll.setFocusable(false);
		SendToAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				client.forwardMessageTo("All", txtMessage.getText());
				txtMessage.setText("");
			}
		});
		SendToAll.setBackground(SystemColor.menu);
		SendToAll.setBounds(231, 225, 99, 24);
		contentPane.add(SendToAll);
		txtMessage.requestFocusInWindow();
	}

	public void console(String message) {
		history.append(message + '\n');
		txtMessage.setText("");
	}

	public void createMenu() {
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		mnFile = new JMenu("File");
		menuBar.add(mnFile);

		mntmOnlineUsers = new JMenuItem("Online Users");
		mntmOnlineUsers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				internalFrame.setVisible(!internalFrame.isVisible());
			}
		});
		mnFile.add(mntmOnlineUsers);

		JMenuItem mtnmClearLog = new JMenuItem("Clear");
		mtnmClearLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				history.setText("");
			}
		});
		mnFile.add(mtnmClearLog);

		mntmExit = new JMenuItem("Log Out");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
				client.disconnectMe();
			}
		});
		mnFile.add(mntmExit);
	}

	public void startNewChat(final String toID) {
		if (toID.length() < 4)
			return;
		if (id_chatRooms.contains(toID))
			return;
		final ChatRoom frame = new ChatRoom(client.getID(), toID);
		id_chatRooms.add(toID);
		chatRooms.add(frame);
		setVisible(true);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				id_chatRooms.remove(toID);
				chatRooms.remove(frame);
			}

			public void windowClosed(WindowEvent e) {
				id_chatRooms.remove(toID);
				chatRooms.remove(frame);
			}
		});

	}

	public void processMessage(String messageFromServer) {
		String[] content = messageFromServer.split("#");
		if (content[0].equals("disconnect")) {
			String ID = content[1];
			if (ID.equals(client.getID()))
				return;
			onlineList.removeUser(ID);
			console("User " + ID + " diconnected from the server.");
		} else if (content[0].equals("joined")) {
			String ID = content[1];
			if (ID.equals(client.getID()) || onlineList.contains(ID))
				return;
			onlineList.addUser(ID);
			console("User " + ID + " joined the server.");
			consoleToAllChatRooms("User " + ID + " joined the server.");
		} else if (content.length != 3) {
			console(content[0]);
			consoleToAllChatRooms(content[0]);
		} else {
			String senderID = content[0];
			String recieverID = content[1];
			String message = content[2];
			if (recieverID.equals("All"))
				console(senderID + ": " + message);
			else
				consoleTo(senderID, message);
		}
	}

	private void consoleTo(String toID, String message) {
		if (!id_chatRooms.contains(toID)) {
			startNewChat(toID);
		}
		for (int i = 0; i < chatRooms.size(); i++) {
			if (chatRooms.get(i).roomMateID.equals(toID)) {
				chatRooms.get(i).console(toID + ": " + message);
				break;
			}
		}
	}

	private void consoleToAllChatRooms(String message) {
		for (int i = 0; i < chatRooms.size(); i++) {
			chatRooms.get(i).console(message);
		}
	}

	class ChatRoom extends JFrame {
		private static final long serialVersionUID = 1L;
		private JPanel contentPane;
		private JTextField txtMessage;
		private JTextArea chatHistory;
		private DefaultCaret caret;
		private JMenuBar menuBar;
		private JMenu mnFile;
		private JMenuItem mntmOnlineUsers;
		private JMenuItem mntmExit;

		private String roomMateID;
		private String ID;

		public ChatRoom(String ID, String roomMateID) {
			this.ID = ID;
			this.roomMateID = roomMateID;
			setTitle(roomMateID);
			createWindow();
			setVisible(true);
		}

		private void createWindow() {
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setSize(550, 350);
			setLocationRelativeTo(null);

			contentPane = new JPanel();
			contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			setContentPane(contentPane);

			GridBagLayout gbl_contentPane = new GridBagLayout();
			gbl_contentPane.columnWidths = new int[] { 28, 815, 30, 7 }; // SUM
																			// =
																			// 880
			gbl_contentPane.rowHeights = new int[] { 25, 485, 40 }; // SUM = 550
			contentPane.setLayout(gbl_contentPane);

			chatHistory = new JTextArea();
			chatHistory.setEditable(false);
			chatHistory.setFont(new Font("Courier New", Font.PLAIN, 12));
			JScrollPane scroll = new JScrollPane(chatHistory);
			caret = (DefaultCaret) chatHistory.getCaret();
			caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
			GridBagConstraints scrollConstraints = new GridBagConstraints();
			scrollConstraints.insets = new Insets(0, 0, 5, 5);
			scrollConstraints.fill = GridBagConstraints.BOTH;
			scrollConstraints.gridx = 0;
			scrollConstraints.gridy = 0;
			scrollConstraints.gridwidth = 3;
			scrollConstraints.gridheight = 2;
			scrollConstraints.weightx = 1;
			scrollConstraints.weighty = 1;
			scrollConstraints.insets = new Insets(0, 5, 0, 0);
			contentPane.add(scroll, scrollConstraints);

			txtMessage = new JTextField();
			txtMessage.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ENTER) {
						client.forwardMessageTo(roomMateID,
								txtMessage.getText());
						if (!ID.equals(roomMateID))
							console(ID + ": " + txtMessage.getText());
					}
				}
			});
			GridBagConstraints gbc_txtMessage = new GridBagConstraints();
			gbc_txtMessage.insets = new Insets(0, 0, 0, 5);
			gbc_txtMessage.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtMessage.gridx = 0;
			gbc_txtMessage.gridy = 2;
			gbc_txtMessage.gridwidth = 2;
			gbc_txtMessage.weightx = 1;
			gbc_txtMessage.weighty = 0;
			contentPane.add(txtMessage, gbc_txtMessage);
			txtMessage.setColumns(10);

			JButton btnSend = new JButton("Send");
			btnSend.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					client.forwardMessageTo(roomMateID, txtMessage.getText());
					if (!ID.equals(roomMateID))
						console(ID + ": " + txtMessage.getText());
				}
			});
			GridBagConstraints gbc_btnSend = new GridBagConstraints();
			gbc_btnSend.insets = new Insets(0, 0, 0, 5);
			gbc_btnSend.gridx = 2;
			gbc_btnSend.gridy = 2;
			gbc_btnSend.weightx = 0;
			gbc_btnSend.weighty = 0;
			contentPane.add(btnSend, gbc_btnSend);

			addMenu();

			txtMessage.requestFocusInWindow();
		}

		public void addMenu() {
			menuBar = new JMenuBar();
			setJMenuBar(menuBar);

			mnFile = new JMenu("File");
			menuBar.add(mnFile);

			mntmOnlineUsers = new JMenuItem("Clear Window");
			mntmOnlineUsers.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					chatHistory.setText("");
				}
			});
			mnFile.add(mntmOnlineUsers);

			mntmExit = new JMenuItem("Exit");
			mntmExit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});

			mnFile.add(mntmExit);
		}

		public void console(String message) {
			chatHistory.append(message + '\n');
			txtMessage.setText("");
		}

	}
}
