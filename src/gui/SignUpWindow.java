package gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import client.Client;

public class SignUpWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JPasswordField pf_password;
	private JTextField tf_Name;
	private JTextField tf_ID;
	private JLabel lbl_ID2;
	private JPasswordField pf_passwordConfirm;
	private JLabel lbl_name;
	private JLabel lbl_ID;
	private JLabel lbl_pass1;
	private JLabel lbl_pass2;
	private JButton btn_signUp;

	private Client windowSocket;
	private boolean Regitered = false;
	private JLabel done;

	public SignUpWindow(Client socket) {
		windowSocket = socket;
		createWindow();
	}

	private void createWindow() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		setResizable(false);
		setTitle("Sign Up");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 600, 440);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		Font myFont = new Font("TYPE1_FONT", Font.TYPE1_FONT, 15);

		tf_Name = new JTextField();
		tf_Name.setColumns(10);
		tf_Name.setBounds(45, 52, 190, 31);
		tf_Name.setFont(myFont);
		contentPane.add(tf_Name);

		tf_ID = new JTextField();
		tf_ID.setFont(new Font("Dialog", Font.BOLD, 15));
		tf_ID.setColumns(10);
		tf_ID.setBounds(45, 135, 390, 31);
		contentPane.add(tf_ID);

		pf_password = new JPasswordField();
		pf_password.setBounds(46, 218, 388, 31);
		contentPane.add(pf_password);

		pf_passwordConfirm = new JPasswordField();
		pf_passwordConfirm.setBounds(46, 301, 388, 31);
		contentPane.add(pf_passwordConfirm);

		JLabel lblName = new JLabel("Name *");
		lblName.setBounds(45, 19, 66, 31);
		contentPane.add(lblName);

		lbl_ID2 = new JLabel("User ID *");
		lbl_ID2.setBounds(45, 102, 66, 31);
		contentPane.add(lbl_ID2);

		JLabel lblPassword = new JLabel("Password *");
		lblPassword.setBounds(45, 185, 66, 31);
		contentPane.add(lblPassword);

		JLabel lblReenterPassword = new JLabel("Re-Enter Password *");
		lblReenterPassword.setBounds(45, 268, 124, 31);
		contentPane.add(lblReenterPassword);

		btn_signUp = new JButton("Sign Up");
		btn_signUp.setToolTipText("Sign Up!");
		btn_signUp.setFont(new Font("Calibri", Font.PLAIN, 18));
		btn_signUp.setForeground(new Color(0, 0, 0));
		btn_signUp.setFocusable(false);
		btn_signUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!Regitered) {
					String name = tf_Name.getText();
					String ID = tf_ID.getText();
					@SuppressWarnings("deprecation")
					String password = pf_password.getText();
					@SuppressWarnings("deprecation")
					String confirmed = pf_passwordConfirm.getText();

					checkValidUser(name, ID, password, confirmed);
				} else {
					dispose();
				}
			}

		});
		btn_signUp.setBackground(new Color(128, 128, 128));
		btn_signUp.setBounds(233, 351, 124, 31);
		contentPane.add(btn_signUp);

		lbl_name = new JLabel("");
		lbl_name.setForeground(Color.RED);
		lbl_name.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lbl_name.setBounds(447, 52, 137, 31);
		contentPane.add(lbl_name);

		lbl_ID = new JLabel("");
		lbl_ID.setForeground(Color.RED);
		lbl_ID.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lbl_ID.setBounds(457, 135, 137, 31);
		contentPane.add(lbl_ID);

		lbl_pass1 = new JLabel("");
		lbl_pass1.setForeground(Color.RED);
		lbl_pass1.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lbl_pass1.setBounds(447, 218, 137, 31);
		contentPane.add(lbl_pass1);

		lbl_pass2 = new JLabel("");
		lbl_pass2.setForeground(Color.RED);
		lbl_pass2.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lbl_pass2.setBounds(444, 301, 137, 31);
		contentPane.add(lbl_pass2);

		done = new JLabel("");
		done.setForeground(new Color(0, 100, 0));
		done.setFont(new Font("Dotum", Font.BOLD, 12));
		done.setBounds(218, 381, 157, 31);
		contentPane.add(done);

		setVisible(true);
	}

	private void checkValidUser(String name, String ID, String password,
			String confirmed) {
		lbl_name.setText("");
		lbl_ID.setText("");
		lbl_pass1.setText("");
		lbl_pass2.setText("");
		boolean NameChecked = false;
		boolean IDChecked = false;
		boolean PWChecked = false;
		if (name.length() == 0) {
			lbl_name.setText("enter your Name!");
		} else if (name.length() < 4) {
			lbl_name.setText("can't be shorter than 4");
		} else if (name.charAt(0) >= '0' && ID.charAt(0) <= '9') {
			lbl_name.setText("must begin with [a-z]");
		} else {
			NameChecked = true;
		}
		if (ID.length() == 0) {
			lbl_ID.setText("enter your ID!");
		} else if (ID.length() < 4) {
			lbl_ID.setText("can't be shorter than 4");
		} else if (ID.charAt(0) > 'Z' || ID.charAt(0) < 'A') {
			lbl_ID.setText("must begin with [A-Z]");
		} else {
			IDChecked = true;
		}

		if (password.length() == 0) {
			lbl_pass1.setText("enter your password!");
		} else if (password.length() < 4) {
			lbl_pass1.setText("can't be shorter than 4");
		} else if (confirmed.length() == 0) {
			lbl_pass2.setText("confirm your password!");
		} else if (!password.equals(confirmed)) {
			lbl_pass2.setText("password doesn't match");
		} else {
			PWChecked = true;
		}
		if (NameChecked && IDChecked && PWChecked) {
			// Send to Host to check if it's valid
			String response = windowSocket.getResponseFromServer("signup", ID,
					password, name);
			if (response.equals("ok")) {
				Regitered = true;
				btn_signUp.setText("go back!");
				done.setText("Registration Completed!");
			} else {
				if (response.equals("offline")) {
					final JPanel panel = new JPanel();
					panel.setSize(200, 200);
					panel.setForeground(Color.red);
					JOptionPane
							.showMessageDialog(
									panel,
									"The Main server is now offline, please try again later",
									"Server", JOptionPane.WARNING_MESSAGE);
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					setVisible(false);
					System.exit(0);
				} else {
					lbl_ID.setText(response);
				}
			}
		}

	}

}
