import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class NodeWindow extends JFrame {
	protected JPanel panel = new JPanel();
	protected JLabel idLabel = new JLabel();

	protected JList<Object> pendingList = new JList<>();

	protected JList<Object> receivedList = new JList<>();
	protected ArrayList<Object> receivedArray = new ArrayList<>();
	
	protected JScrollPane scroll;
	public NodeWindow(int id) {

		this.setSize(400,400);
		
		idLabel.setText("ID: "+id);
		panel.add(idLabel);

		pendingList.setListData(new Object[] {"Vazia"});
		receivedList.setListData(receivedArray.toArray());
		panel.add(pendingList);
		panel.add(receivedList);
		scroll = new JScrollPane(panel);
		this.setContentPane(scroll);
		this.setVisible(true);
	}
	
	public void updatePending(Object[] data) {
		pendingList.setListData(data);
		panel.invalidate();
		panel.validate();
		panel.repaint();
	}
	
	public void updateReceived(Object received) {
		receivedArray.add(received);
		receivedList.setListData(receivedArray.toArray());
		panel.invalidate();
		panel.validate();
		panel.repaint();
	}
	
}
