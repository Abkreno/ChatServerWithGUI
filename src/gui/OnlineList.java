package gui;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class OnlineList extends JPanel {
	private static final long serialVersionUID = 1L;

	public JList<String> list;
	public DefaultListModel<String> IDs;
	public JFrame window;

	public OnlineList() {
		setLayout(new BorderLayout());
		IDs = new DefaultListModel<String>();
		list = new JList<String>(IDs);
		list.setBackground(Color.gray);
		list.setForeground(Color.green);
		JScrollPane pane = new JScrollPane(list);
		add(pane);
	}

	public void addUser(String userID) {
		IDs.addElement(userID);
	}

	public void removeUser(String userID) {
		if (IDs.contains(userID))
			IDs.remove(IDs.indexOf(userID));
	}

	public String getSelectedUserID() {
		int index = list.getSelectedIndex();
		return IDs.get(index);
	}

	public boolean contains(String string) {
		if (IDs.contains(string))
			return true;
		return false;
	}
}
