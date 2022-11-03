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
	protected JLabel sequencedLabel = new JLabel();
	protected int seq = 0;
	
	protected JScrollPane scroll;
	public SequencerWindow() {

		this.setSize(400,400);
		
		sequenceLabel.setText("SEQ: 0  ");
		panel.add(sequenceLabel);
		panel.add(sequencedLabel);
		scroll = new JScrollPane(panel);
		this.setContentPane(scroll);
		this.setVisible(true);
	}
	public void updateSequence() {
		seq++;
		sequenceLabel.setText("SEQ: "+seq);
		panel.invalidate();
		panel.validate();
		panel.repaint();
	}
	
	public void updateSequenced(String sequenced) {
		sequencedLabel.setText(sequenced);
		panel.invalidate();
		panel.validate();
		panel.repaint();
	}
	
	
}
