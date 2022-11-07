package gc;

import java.io.Serializable;
import java.util.Map;

public class Message implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected Object payload;
	protected MessageType type;
	protected long sequence = -1;
	protected Map<Integer, VectorClock> vp;
	protected VectorClock tm;
	public int sender = -1;
	
	public Message() {
		
	}
	
	public Message(MessageType type, Object payload, int sender) {
		this.payload = payload;
		this.type = type;
		this.sender = sender;
	}
	
	public Message(MessageType type, Object payload, Map<Integer, VectorClock> vp, VectorClock tm, int sender) {
		this.payload = payload;
		this.type = type;
		this.vp = vp;
		this.tm = tm;
		this.sender = sender;
	}
	
	public Object getPayload() {
		return this.payload;
	}
	
	public void setSequence(long sequence) {
		this.sequence = sequence;
	}
	
	public long getSequence() {
		return this.sequence;
	}
	
	public Map<Integer, VectorClock> getVP() {
		return vp;
	}

	public void setVP(Map<Integer, VectorClock> vectorClocks) {
		this.vp = vectorClocks;
	}

	public MessageType getType() {
		return this.type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

	public VectorClock getTm() {
		return tm;
	}

	public void setTm(VectorClock tm) {
		this.tm = tm;
	}
	
}
