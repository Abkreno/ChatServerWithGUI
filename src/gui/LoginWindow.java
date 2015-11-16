package gui;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.Socket;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;

import client.Client;

public class LoginWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private JTextField tf_uid;
	private JTextField tf_localhost;
	private JTextField tf_port;
	private JPasswordField pf;
	private JLabel lbl_verdict;

	private SignUpWindow signUpWindow;
	private String myHost = "localhost", myPort = "6000";
	private Client manager;
	private boolean logged = false;

	public LoginWindow() {
		createWindow();
		this.manager = null;
	}

	private void createWindow() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		setTitle("Cool Chat");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 229, 379);
		setLocationRelativeTo(null);
		getContentPane().setLayout(null);
		setResizable(false);
		JLabel lbl_uid = new JLabel("User-ID");
		lbl_uid.setBounds(83, 11, 46, 14);
		getContentPane().add(lbl_uid);

		tf_uid = new JTextField();
		tf_uid.setBounds(37, 28, 139, 23);
		getContentPane().add(tf_uid);
		tf_uid.setColumns(10);

		JLabel lbl_pw = new JLabel("Password");
		lbl_pw.setBounds(83, 62, 46, 14);
		getContentPane().add(lbl_pw);

		pf = new JPasswordField();
		pf.setBounds(37, 76, 139, 23);
		getContentPane().add(pf);

		lbl_verdict = new JLabel("");
		lbl_verdict.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lbl_verdict.setForeground(Color.RED);
		lbl_verdict.setBounds(38, 138, 138, 14);
		getContentPane().add(lbl_verdict);

		JButton btn_Login = new JButton("Login");
		btn_Login.setFocusable(false);
		btn_Login.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String ID = tf_uid.getText();
				@SuppressWarnings("deprecation")
				String password = pf.getText();
				String hostname = tf_localhost.getText();
				String port = tf_port.getText();
				login(ID, password, hostname, port);
			}
		});

		pf.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					String ID = tf_uid.getText();
					@SuppressWarnings("deprecation")
					String password = pf.getText();
					String hostname = tf_localhost.getText();
					String port = tf_port.getText();
					login(ID, password, hostname, port);
				}
			}
		});

		JLabel lblServer = new JLabel("=========   Server  =========");
		lblServer.setBounds(8, 163, 197, 14);
		getContentPane().add(lblServer);
		btn_Login.setBounds(61, 110, 89, 23);
		getContentPane().add(btn_Login);

		JLabel lblHostName = new JLabel("Host Name");
		lblHostName.setBounds(80, 193, 53, 14);
		getContentPane().add(lblHostName);

		tf_localhost = new JTextField();
		tf_localhost.setText("localhost");
		tf_localhost.setColumns(10);
		tf_localhost.setBounds(37, 210, 139, 23);
		getContentPane().add(tf_localhost);

		JLabel lblPort = new JLabel("Port");
		lblPort.setBounds(93, 244, 27, 14);
		getContentPane().add(lblPort);

		tf_port = new JTextField();
		tf_port.setText("6000");
		tf_port.setColumns(10);
		tf_port.setBounds(37, 257, 139, 23);
		getContentPane().add(tf_port);

		JLabel lblDontHaveId = new JLabel("Don't have ID ?");
		lblDontHaveId.setBackground(SystemColor.activeCaption);
		lblDontHaveId.setBounds(16, 311, 82, 14);
		getContentPane().add(lblDontHaveId);

		JButton btn_SignUp = new JButton("Sign Up");
		btn_SignUp.setBackground(SystemColor.inactiveCaptionText);
		btn_SignUp.setForeground(new Color(0, 0, 205));
		btn_SignUp.setFocusable(false);
		btn_SignUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (signUpWindow == null || !signUpWindow.isVisible()) {
					String hostname = tf_localhost.getText();
					String port = tf_port.getText();
					signUp(hostname, port);
				}
			}
		});

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (manager != null)
					manager.disconnectMe();
			}
		});

		btn_SignUp.setBounds(114, 307, 82, 23);
		getContentPane().add(btn_SignUp);
	}

	public void login(final String ID, String password, String hostname,
			String port) {
		if (logged) {
			logged = false;
			manager.disconnectMe();
			manager = null;
		}
		lbl_verdict.setText("");
		if (ID.length() == 0) {
			lbl_verdict.setText("      Enter Your ID!");
			return;
		}
		if (password.length() == 0) {
			lbl_verdict.setText("  Enter Your Password!");
			return;
		}
		if (hostname.length() == 0) {
			lbl_verdict.setText(" Enter the host name!");
			return;
		}
		if (port.length() == 0) {
			lbl_verdict.setText("Enter the port number!");
			return;
		} else {
			for (int i = 0; i < port.length(); i++) {
				if (!(port.charAt(0) >= '0' && port.charAt(i) <= '9')) {
					lbl_verdict.setText("  Invalid port number!");
					return;
				}
			}
		}

		// make sure the manager is initialized first
		if (!setManager(hostname, port)) {
			// failed to join the server
			lbl_verdict.setText("    Connection failed!");
			return;
		}
		// getting Response for logging in or signing up
		String request = manager.getResponseFromServer("login", ID, password,
				"");
		if (request.equals("Incorrect id or password")) {
			lbl_verdict.setText("Invalid ID or Password!");
		} else if (request.equals("logged in")) {
			lbl_verdict.setText("Already Logged in!");
		} else {
			String online[] = request.split("#");
			final LinkedList<String> onlineList = new LinkedList<String>();
			for (int i = 0; i < online.length; i++) {
				if (!onlineList.contains(online[i]) && online[i].length() >= 4)
					onlineList.add(online[i]);
			}
			manager.setID(ID);
			manager.setPassword(password);
			manager.setHostName(hostname);
			manager.setPort(port);
			logged = true;
			setVisible(false);
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						ChatWindow w = new ChatWindow(ID, onlineList, manager);
						w.setVisible(true);

					} catch (Exception e) {
						e.printStackTrace();

					}
				}
			});
		}

	}

	public void signUp(String hostname, String port) {
		if (hostname.length() == 0) {
			lbl_verdict.setText(" Enter the host name!");
			return;
		}
		if (port.length() == 0) {
			lbl_verdict.setText("Enter the port number!");
			return;
		} else {
			for (int i = 0; i < port.length(); i++) {
				if (!(port.charAt(0) >= '0' && port.charAt(i) <= '9')) {
					lbl_verdict.setText("  Invalid port number!");
					return;
				}
			}
		}
		// make sure the manager is initialized first
		if (setManager(hostname, port)) {
			signUpWindow = new SignUpWindow(manager);
			this.setVisible(false);
			signUpWindow.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					signUpWindow = null;
					setVisible(true);
				}

				public void windowClosed(WindowEvent e) {
					signUpWindow = null;
					setVisible(true);
				}
			});
		} else {
			// failed to join the server
			final JPanel panel = new JPanel();
			panel.setSize(200, 200);
			panel.setForeground(Color.red);
			JOptionPane.showMessageDialog(panel,
					"The Server is now offline, please try again later",
					"Server", JOptionPane.WARNING_MESSAGE);

		}
	}

	private boolean setManager(String hostname, String port) {
		if (manager == null) {
			if (!port.equals(myPort) || !hostname.equals(myHost)) {
				myPort = port;
				myHost = hostname;
			}
			try {
				Socket s = new Socket(myHost, Integer.parseInt(myPort));
				manager = new Client(s);
			} catch (Exception e) {
				return false;
			}
		} else if (!port.equals(myPort) || !hostname.equals(myHost)) {
			myPort = port;
			myHost = hostname;
			boolean good = manager.changeHost(hostname, port);
			if (!good) {
				manager = null;
				return false;
			}
		}
		return true;
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoginWindow frame = new LoginWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}
}
