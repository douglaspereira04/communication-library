import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class NodeWindow extends JFrame {
	private static final long serialVersionUID = 678710702309785755L;
	
	protected JPanel panel = new JPanel();
	protected JLabel idLabel = new JLabel();
	protected JLabel sequenceLabel = new JLabel();
	protected int seq = 0;
	protected JList<Object> pendingList = new JList<>();

	protected JList<Object> receivedList = new JList<>();
	protected ArrayList<Object> receivedArray = new ArrayList<>();
	protected JLabel sentLabel = new JLabel();
	
	protected JScrollPane scroll;
	public NodeWindow(int id) {

		this.setSize(400,400);

		panel.add(sentLabel);
		idLabel.setText("ID: "+id);
		panel.add(idLabel);
		sequenceLabel.setText("SEQ: "+seq);
		panel.add(sequenceLabel);

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
		seq++;
		sequenceLabel.setText("SEQ: "+seq);
		panel.invalidate();
		panel.validate();
		panel.repaint();
	}
	
	public void updateLastSent(String sent) {
		sentLabel.setText(sent);
		panel.invalidate();
		panel.validate();
		panel.repaint();
	}
	
}
