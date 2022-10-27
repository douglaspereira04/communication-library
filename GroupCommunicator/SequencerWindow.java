import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class SequencerWindow extends JFrame {
	
	private static final long serialVersionUID = -5431964320762825207L;
	protected JPanel panel = new JPanel();
	protected JLabel sequenceLabel = new JLabel();

	protected JList<Object> pendingList = new JList<>();

	protected JList<Object> receivedList = new JList<>();
	protected ArrayList<Object> receivedArray = new ArrayList<>();
	
	protected JScrollPane scroll;
	public SequencerWindow() {

		this.setSize(400,400);
		
		sequenceLabel.setText("SEQ: 0");
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
		panel.invalidate();
		panel.validate();
		panel.repaint();
	}
	
}
