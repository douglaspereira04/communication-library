import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import gc.Message;

public class NodeWindow extends JFrame {
	private static final long serialVersionUID = 678710702309785755L;
	
	protected JPanel panel = new JPanel();
	protected JLabel idLabel = new JLabel();
	protected JLabel sequenceLabel = new JLabel();
	protected int seq = 0;

	protected JList<Object> receivedList = new JList<>();
	protected ArrayList<Object> receivedArray = new ArrayList<>();
	protected JLabel sentLabel = new JLabel();
	protected int id;

	protected ArrayList<Message>  pendingArray = new ArrayList<>();
	protected ArrayList<JLabel>  pendingLabelArray = new ArrayList<>();
	protected ArrayList<JLabel> receivedLabelArray = new ArrayList<>();
	
	protected int receivedIndex = 2;
	protected int pendingIndex = 1;
	protected Lock lock = new ReentrantLock();
	
	protected JScrollPane scroll;
	public NodeWindow(int id) {
		this.id = id;

		this.setSize(800,600);

		/*panel.add(sentLabel);
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
		this.setVisible(true);*/
		
		initialize();
	}
	

	public void initialize() {
		panel.add(sentLabel);
		idLabel.setText("ID: "+id);
		sequenceLabel.setText("SEQ: "+seq);

		scroll = new JScrollPane(panel);
		
		sequenceLabel.setText("SEQ: 0  ");
		
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridwidth = 1;
		c.gridheight = 1;
		
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridy = 0;
		panel.add(idLabel, c);

		c.gridx = 0;
		c.gridy = 1;
		panel.add(new JLabel("ENVIADO: "), c);

		c.gridx = 0;
		c.gridy = 2;
		c.ipadx = 50;
		c.ipady = 50;
		panel.add(sentLabel, c);


		c.gridx = 1;
		c.gridy = 0;
		panel.add(new JLabel("RECEBIDO"), c);

		c.gridx = 2;
		c.gridy = 0;
		panel.add(sequenceLabel, c);
		
		c.gridx = 2;
		c.gridy = 1;
		panel.add(new JLabel("ENTREGUE"), c);
		

		scroll = new JScrollPane(panel);
		this.setContentPane(scroll);
		this.setVisible(true);
		
		
		
	}

	public JLabel messageLabel(Message m) {
		JLabel label = new JLabel();
		label.setOpaque(true);
		label.setSize(100, 100);
		if(m.sender == 0 ) {
			label.setBackground(new Color(66, 135, 245));
		}else if(m.sender == 1 ) {
			label.setBackground(new Color(245, 96, 66));
		}else {
			label.setBackground(new Color(147, 245, 66));
		}
		
		if(m.getSequence() != -1) {
			label.setText(m.getSequence()+" -> "+m.getPayload());
		}else {
			label.setText(""+m.getPayload());
		}
		
		return label;

	}
	public JLabel messageLabel(String text) {
		JLabel label = new JLabel(text);
		label.setOpaque(true);
		label.setSize(100, 100);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setVerticalAlignment(SwingConstants.CENTER);
		if(id == 0 ) {
			label.setBackground(new Color(66, 135, 245));
		}else if(id == 1 ) {
			label.setBackground(new Color(245, 96, 66));
		}else {
			label.setBackground(new Color(147, 245, 66));
		}
		
		return label;

	}

	public void updatePending(ArrayList<Message> data) {
		lock.lock();
		panel.invalidate();
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 1;
		c.ipadx = 50;
		c.ipady = 25;
		c.insets = new Insets(5,10,5,10);

		
		for (int i = 0; i < pendingLabelArray.size(); i++) {
			panel.remove(pendingLabelArray.get(i));
		}
		
		ArrayList<Message> p = new ArrayList<Message>(pendingArray);
		
		for (int i = 0; i < pendingArray.size(); i++) {
			Message a = pendingArray.get(i);
			if(!data.contains(a)) {
				p.remove(a);
				pendingLabelArray.remove(i);
			}
		}
		
		for (int i = 0; i < data.size(); i++) {
			Message a = data.get(i);
			if(!p.contains(a)) {
				p.add(a);
				pendingLabelArray.add(messageLabel(a));
			}
		}
		
		for (int i = 0; i < pendingLabelArray.size(); i++) {
			panel.add(pendingLabelArray.get(i), c);
			c.gridy++;
		}
		
		pendingArray = p;

		panel.validate();
		panel.repaint();

		lock.unlock();
	}
	
	public void updateReceived(Message received) {
		lock.lock();
		panel.invalidate();
		JLabel label = messageLabel(received);

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = receivedIndex++;
		c.ipadx = 50;
		c.ipady = 25;
		c.insets = new Insets(5,10,5,10);

		
		panel.add(label, c);
		
		seq++;
		sequenceLabel.setText("SEQ: "+seq);
		
		panel.validate();
		panel.repaint();
		lock.unlock();
	}
	
	public void updateLastSent(String sent) {
		lock.lock();
		panel.invalidate();
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.ipadx = 50;
		c.ipady = 25;
		panel.remove(sentLabel);
		if(sent == "") {
			sentLabel = new JLabel();
		}else {
			sentLabel = messageLabel(sent);
		}
		panel.add(sentLabel, c);

		panel.validate();
		panel.repaint();
		lock.unlock();
	}
	
}
