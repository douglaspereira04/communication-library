import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import gc.Message;

public class SequencerWindow extends JFrame {
	
	private static final long serialVersionUID = -5431964320762825207L;
	protected JPanel panel = new JPanel();
	protected JLabel sequenceLabel = new JLabel();
	protected JLabel sequencedLabel = new JLabel();
	protected int seq = 0;
	
	protected JScrollPane scroll;
	public SequencerWindow() {

		this.setSize(400,400);
		initialize();
	}
	
	public void initialize() {
		sequenceLabel.setText("SEQ: 0  ");
		
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.gridwidth = 1;
		c.gridheight = 1;
		
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridy = 0;
		panel.add(sequenceLabel, c);
		
		c.gridy = 1;
		c.ipadx = 50;
		c.ipady = 50;
		sequencedLabel.setHorizontalAlignment(SwingConstants.CENTER);
		sequencedLabel.setVerticalAlignment(SwingConstants.CENTER);
		panel.add(sequencedLabel, c);
		

		scroll = new JScrollPane(panel);
		this.setContentPane(scroll);
		this.setVisible(true);
		
		
		
	}
	
	public JLabel messageLabel(JLabel label, Message m) {
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
	public void updateSequence() {
		seq++;
		sequenceLabel.setText("SEQ: "+seq);
		panel.invalidate();
		panel.validate();
		panel.repaint();
	}
	
	public void updateSequenced(Message m) {

		
		sequencedLabel = messageLabel(sequencedLabel, m);
		
		panel.invalidate();
		panel.validate();
		panel.repaint();
	}
	
	
}
