package gc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class OrderedGroupCommunicator extends GroupCommunicator {

	// Sequencer node will be the node 0
	protected int sequencer = 0;
	protected AtomicLong sequence = new AtomicLong(0);
	protected long next = 0;
	protected Map<Integer,VectorClock> vectorClocks;
	protected boolean delayedBroadcast = false;

	public OrderedGroupCommunicator(int id, Map<Integer, InetSocketAddress> socketAddresses, List<Integer> ids, int sequencer) {
		super(id, socketAddresses, ids);
		
		this.sequencer = sequencer;
		
		vectorClocks = new HashMap<>();
		vectorClocks.put(id, new VectorClock(this.socketAddresses.keySet()));

		for (Map.Entry<Integer, InetSocketAddress> entry : socketAddresses.entrySet()) {
			Integer procId = entry.getKey();
			Thread thread = new Thread(() -> {
				try {
					this.receiveMessages(procId);
				} catch (ClassNotFoundException | IOException e) {
					e.printStackTrace();
				}
			});
			this.threads.put(procId, thread);
		}
		
		this.deliverThread = new Thread(()->{
			try {
				this.deliver();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
	}
	
	/**
	 * Puts received messages in pending list and notifies deliver thread, 
	 * through semaphore, that messages were received. 
	 * If received message is a sequence request, 
	 * instead of adding to pending list,
	 * message will be broadcasted with sequence number.
	 * 
	 * @param j index of object input stream
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	@Override
	public void receiveMessages(int j) throws ClassNotFoundException, IOException {
		Message message;
		ObjectInputStream in = this.inStream.get(j);
		while (!this.stop) {
			message = (Message) in.readObject();
			if(message == null) {
				this.stop = true;
				this.broadcast(message);
			}else if (message.getType() == MessageType.SEQ) {
				this.sequencerBroadcast(message);
			} else {
				this.pending.add(message);
				this.receivedSem.release();
			}
		}
	}
	
	/**
	 * Removes messages from pending list and adds to deliver queue
	 * in causal order following Schiper-Eggli-Sandoz Algorithm
	 * for unicasted messages, and in total order following
	 * UB Sequencer algorithm for broadcasted messages.
	 */
	@Override
	public void deliver() throws InterruptedException {
		while (!this.stop) {
			this.receivedSem.acquire();
			boolean delivered = false;
			do {
				delivered = this.deliverOrderedBroadcast();
				delivered = delivered || this.deliverOrderedUnicast();
			} while (delivered);
		}
	}

	/**
	 * Adds sequence number to message and broadcasts it.
	 * 
	 * @param message
	 * @throws IOException
	 */
	public void sequencerBroadcast(Message message) throws IOException {
		long sequence = this.sequence.getAndIncrement();
		message.setSequence(sequence);
		message.setType(MessageType.BROADCAST);
		if(this.delayedBroadcast == true) {
			Random rand = new Random();
			int pos = rand.nextInt(this.ids.size());
			int delay = rand.nextInt(5000) + 500;
			int toDelay = this.ids.get(pos);
			this.broadcast(message, toDelay, delay);
		}else {
			this.broadcast(message);
		}
	}

	/**
	 * Removes next broadcasted sequenced message 
	 * from pending list following Sequencer algorithm
	 * 
	 * @return found message or null
	 */
	public Message getNextBroadcast() {
		for (Iterator<Message> iterator = this.pending.iterator(); iterator.hasNext();) {
			Message message = (Message) iterator.next();
			if (message.getType() == MessageType.BROADCAST && message.getSequence() == this.next) {
				this.pending.remove(message);
				return message;
			}
		}
		return null;
	}

	/**
	 * Delivers received broadcast messages
	 * following Sequencer algorithm
	 * @return boolean indicating if a message
	 * was delivered
	 */
	public boolean deliverOrderedBroadcast() {
		boolean delivered = false;
		Message receivedMessage = getNextBroadcast();
		if (receivedMessage != null) {
			this.delivered.add(receivedMessage);
			this.next++;
			delivered = true;
		}
		return delivered;

	}

	/**
	 * Merges vector clocks according to 
	 * Schiper-Eggli-Sandoz algorithm
	 * @param V_M vector clocks of message
	 */
	public void mergeVectorClocks(Map<Integer, VectorClock> V_M) {
		for (Map.Entry<Integer, VectorClock> entry : V_M.entrySet()) {
			Integer procId = entry.getKey();
			VectorClock vm = entry.getValue();

			if (procId != this.id && vm != null) {
				if (this.vectorClocks.get(procId) != null) {
					this.vectorClocks.get(procId).merge(vm);
				} else {
					this.vectorClocks.put(procId, vm);
				}
			}
		}
	}

	/**
	 * Removes next unicasted message from pending list 
	 * following Schiper-Eggli-Sandoz
	 * protocol to maintain causal order
	 * 
	 * @return found message or null
	 */
	public Message getNextUnicast() {
		for (Iterator<Message> messagesIterator = this.pending.iterator(); messagesIterator.hasNext();) {
			Message message = (Message) messagesIterator.next();
			if (message.getType() == MessageType.UNICAST) {

				Map<Integer, VectorClock> V_M = message.getVP();
				VectorClock t = V_M.get(this.id);
				VectorClock tp = this.vectorClocks.get(this.id);
				if (t == null || t.lessEqual(tp)) {
					this.pending.remove(message);
					return message;
				}
			}
		}
		return null;
	}

	/**
	 * Delivers unicasted messages from pending list 
	 * following Schiper-Eggli-Sandoz
	 * protocol to maintain causal order
	 * 
	 * @return boolean indicating 
	 * if a message was delivered
	 */
	public boolean deliverOrderedUnicast() {
		boolean delivered = false;
		Message message = null;
		synchronized (vectorClocks) {
			message = getNextUnicast();
			if (message != null) {
				this.delivered.add(message);
				
				Map<Integer, VectorClock> V_M = message.getVP();
				VectorClock tm = message.getTm();

				this.vectorClocks.get(this.id).merge(tm);
				this.mergeVectorClocks(V_M);
				this.vectorClocks.get(this.id).increment(this.id);
				delivered = true;
			}
		}
		return delivered;
	}

	/**
	 * Sends message Follows Schiper-Eggli-Sandoz protocol to maintain causal order
	 */
	@Override
	public void send(int id, Object payload) throws IOException {
		synchronized (vectorClocks) {
			
			this.vectorClocks.get(this.id).increment(this.id);
	
			Message message = new Message(
					MessageType.UNICAST, payload, 
					this.vectorClocks, 
					this.vectorClocks.get(this.id));

			ObjectOutputStream out = this.outStream.get(id);
			synchronized (out) {
				out.writeObject(message);
			}
	
			if (this.vectorClocks.get(id) == null) {
				this.vectorClocks.put(id, new VectorClock(this.socketAddresses.keySet()));
			}
			this.vectorClocks.get(id).set(this.vectorClocks.get(this.id));

		}
	}

	/**
	 * For test 
	 * Sends message with a delay Follows Schiper-Eggli-Sandoz protocol to
	 * maintain causal order
	 * 
	 * @throws ClassNotFoundException
	 */
	public void send(int id, Object payload, int delay) 
			throws IOException, ClassNotFoundException {
		synchronized (vectorClocks) {
			this.vectorClocks.get(this.id).increment(this.id);
			//copy to prevents the vectors in message to change
			Message message = copy(new Message(MessageType.UNICAST, 
					payload, this.vectorClocks, 
					this.vectorClocks.get(this.id)));

			ObjectOutputStream out = this.outStream.get(id);
			new Thread(() -> {
				try {
					Thread.sleep(delay);
					synchronized (out) {
						out.writeObject(message);
					}
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
				}
			}).start();
	
			if (this.vectorClocks.get(id) == null) {
				this.vectorClocks.put(id, new VectorClock(this.socketAddresses.keySet()));
			}
			this.vectorClocks.get(id).set(this.vectorClocks.get(this.id));
		}
	}

	/**
	 * Total order is guaranteed through Sequencer, UB variation. Broadcasts a
	 * message with payload as message payload.
	 */
	@Override
	public void broadcast(Object payload) throws IOException {
		Message message = new Message(MessageType.SEQ, payload);
		ObjectOutputStream out = this.outStream.get(this.sequencer);
		synchronized (out) {
			out.writeObject(message);
		}
	}
	

	public int getSequencer() {
		return sequencer;
	}

	public void setSequencer(int sequencer) {
		this.sequencer = sequencer;
	}

	public void setDelayedBroadcast(boolean delayedBroadcast) {
		this.delayedBroadcast = delayedBroadcast;
	}
	

}
