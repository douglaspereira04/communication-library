import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

public class Message implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected Object payload;
	protected MessageType type;
	protected long sequence;
	protected Map<Integer, VectorClock> vp;
	protected VectorClock tm;
	
	public Message() {
		
	}
	
	public Message(MessageType type, Object payload) {
		this.payload = payload;
		this.type = type;
	}
	
	public Message(MessageType type, Object payload, Map<Integer, VectorClock> vp, VectorClock tm) {
		this.payload = payload;
		this.type = type;
		this.vp = vp;
		this.tm = tm;
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
