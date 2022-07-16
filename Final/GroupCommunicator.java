import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Unordere group communicator
 * 
 * @author douglas
 *
 */
public class GroupCommunicator {

	// Thread safe lists
	protected List<Message> pending;// Not delivered
	protected LinkedBlockingQueue<Message> delivered;
	protected Semaphore receivedSem;
	protected Thread deliverThread;

	protected Map<Integer, InetSocketAddress> socketAddresses;
	protected Map<Integer, ObjectOutputStream> outStream;
	protected Map<Integer, ObjectInputStream> inStream;
	protected Map<Integer, Socket> out;
	protected Map<Integer, Socket> in;
	protected ServerSocket server;
	protected Map<Integer, Thread> threads;
	protected int id = -1;
	protected List<Integer> ids;


	/**
	 * Creates a unordered group communicator instance given the addresses of all
	 * nodes and the id of the node in instantiation.
	 * 
	 * @param id
	 * @param socketAddresses
	 */
	public GroupCommunicator(int id, Map<Integer, InetSocketAddress> socketAddresses) {
		int amount = socketAddresses.size();
		this.id = id;
		this.socketAddresses = socketAddresses;
		this.pending = Collections.synchronizedList(new ArrayList<>());
		this.delivered = new LinkedBlockingQueue<>();
		this.receivedSem = new Semaphore(0);

		this.threads = new HashMap<>();
		this.out = new HashMap<>();
		this.in = new HashMap<>();
		this.inStream = new HashMap<>();
		this.outStream = new HashMap<>();

		for (Map.Entry<Integer, InetSocketAddress> entry : socketAddresses.entrySet()) {
			Integer procId = entry.getKey();
			this.ids.add(procId);
			
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
	 * Create socket and bind to port defined in configuration file
	 * 
	 * @throws IOException
	 */
	public void bind() throws IOException {
		this.server = new ServerSocket(socketAddresses.get(this.id).getPort());
	}

	/**
	 * Creates stream sockets to connect to nodes
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public void connect() throws UnknownHostException, IOException {

		for (Map.Entry<Integer, InetSocketAddress> entry : socketAddresses.entrySet()) {
			Integer procId = entry.getKey();
			InetSocketAddress address = entry.getValue();
			Socket sock = new Socket(address.getHostName(), address.getPort());
			this.out.put(procId, sock);
		}
	}

	/**
	 * Listens for nodes connections and accepts. Gets output and input streams from
	 * sockets and starts receiving and delivering.
	 * 
	 * @throws IOException
	 */
	public void accept() throws IOException {

		for (Map.Entry<Integer, InetSocketAddress> entry : socketAddresses.entrySet()) {
			Integer procId = entry.getKey();
			Socket sock = server.accept();
			this.in.put(procId, sock);
		}

		for (Map.Entry<Integer, Socket> entry : out.entrySet()) {
			Integer procId = entry.getKey();
			Socket outSock = entry.getValue();
			ObjectOutputStream objOutput = new ObjectOutputStream(outSock.getOutputStream());
			this.outStream.put(procId, objOutput);
		}

		for (Map.Entry<Integer, Socket> entry : in.entrySet()) {
			Integer procId = entry.getKey();
			Socket inSock = entry.getValue();
			ObjectInputStream objInput = new ObjectInputStream(inSock.getInputStream());
			this.inStream.put(procId, objInput);
		}

		this.start();
	}

	/**
	 * Starts receiving and delivering threads
	 */
	public void start() {
		for (Map.Entry<Integer, Thread> entry : this.threads.entrySet()) {
			Thread thread = entry.getValue();
			thread.start();
		}

		this.deliverThread.start();
	}

	/**
	 * Receives messages and notifies, through semaphore, that messages were
	 * received
	 * 
	 * @param index
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public void receiveMessages(int j) throws ClassNotFoundException, IOException {
		Message message;
		ObjectInputStream in = this.inStream.get(j);
		while (true) {
			message = (Message) in.readObject();
			this.pending.add(message);
			this.receivedSem.release();
		}
	}

	/**
	 * Returns a delivered message. Blocks until delivered message available
	 * 
	 * @return delivered message
	 * @throws InterruptedException
	 */
	public Object receive() throws InterruptedException {
		return this.delivered.take().getPayload();
	}


	/**
	 * Version of {@link #receive()} with timeout in milliseconds
	 * 
	 * @return delivered message or null
	 * @throws InterruptedException
	 */
	public Object receive(long timeout) throws InterruptedException {
		Message message = this.delivered.poll(timeout, TimeUnit.MILLISECONDS);
		if(message != null) {
			return message.getPayload();
		}
		return null;
	}
	

	
	/**
	 * Delivers pending messages
	 * @throws InterruptedException 
	 */
	public void deliver() throws InterruptedException {
		while (true) {
			this.receivedSem.acquire();
			this.delivered.add(this.pending.remove(0));
		}
	}

	/**
	 * Unicast
	 * 
	 * @param id
	 * @param message
	 * @throws IOException
	 */
	public void send(int id, Object payload) throws IOException {
		Message message = new Message(MessageType.MESSAGE, payload);
		synchronized (this.outStream.get(id)) {
			this.outStream.get(id).writeObject(message);
		}
	}

	/**
	 * For test Unicast with delay
	 * 
	 * @param id
	 * @param message
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void send(int id, Object payload, int delay) 
			throws IOException, ClassNotFoundException {
		Message message = new Message(MessageType.MESSAGE, payload);
		Message copy = copy(message);
		ObjectOutputStream out = this.outStream.get(id);
		new Thread(() -> {
			try {
				Thread.sleep(delay);
				synchronized (out) {
					out.writeObject(copy);
				}
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
	}

	/**
	 * Broadcasts a message with payload as message payload.
	 * 
	 * @param payload
	 * @throws IOException
	 */
	public void broadcast(Object payload) throws IOException {
		Message message = new Message(MessageType.MESSAGE, payload);
		this.broadcast(message);
	}

	/**
	 * Broadcasts message
	 * 
	 * @param message
	 * @throws IOException
	 */
	public void broadcast(Message message) throws IOException {
		for (Map.Entry<Integer, ObjectOutputStream> entry : outStream.entrySet()) {
			ObjectOutputStream out = entry.getValue();
			synchronized (out) {
				out.writeObject(message);
			}
		}
	}

	public Message copy(Message message) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(message);
		oos.flush();
		oos.close();
		bos.close();
		byte[] byteData = bos.toByteArray();
		ByteArrayInputStream bais = new ByteArrayInputStream(byteData);
		try {
			return (Message) new ObjectInputStream(bais).readObject();
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Gets ID
	 * 
	 * @return id
	 */
	public int getId() {
		return this.id;
	}

	public int getNeighbor() {
		return this.ids.get(this.ids.indexOf(this.getId())+1);
	}
	
	
}
