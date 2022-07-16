
public enum MessageType {
	MESSAGE, 
	SEQ, //Request to sequencer
	UNICAST, //Causal ordered unicast
	BROADCAST //Total ordered broadcast
}
