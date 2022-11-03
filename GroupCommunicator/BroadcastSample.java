import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import gc.ConfigLoader;
import gc.GroupCommunicator;
import gc.Message;
import gc.OrderedGroupCommunicator;

public class BroadcastSample {
	static boolean stop = false;

	public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
		Scanner scanner = new Scanner(System.in);
		Integer paramId = null;
		OrderedGroupCommunicator loaded = null;
		try {
			paramId = Integer.valueOf(args[0]);
		} catch (Exception e) {
		}
		if (paramId == null) {
			loaded = ConfigLoader.loadOGC("node.config");
		} else {
			loaded = ConfigLoader.loadOGC("node.config", paramId);
		}
		final OrderedGroupCommunicator gc = loaded;

		System.out.println("Init");
		gc.init();
		System.out.println("Done");

		CopyOnWriteArrayList<Message> pending = gc.getPending();
		LinkedBlockingQueue<Message> delivered = gc.getDelivered();

		NodeWindow window = new NodeWindow(paramId);

		Thread receiveingThread = new Thread(() -> {
			try {
				while (!stop) {
					//System.out.println("---------------ESPERA--------------");
					ArrayList<Object> pendingList = new ArrayList<>();
					for (Iterator<Message> iterator = pending.iterator(); iterator.hasNext();) {
						Message message = (Message) iterator.next();
						String str = "Nº Sequencia: " + message.getSequence() + " -> " + (String) message.getPayload();
						pendingList.add(str);
					}
					
					for (Iterator<Message> iterator = delivered.iterator(); iterator.hasNext();) {
						Message message = (Message) iterator.next();
						String str = "Nº Sequencia: " + message.getSequence() + " -> " + (String) message.getPayload();
						pendingList.add(str);
					}
					window.updatePending(pendingList.toArray());
					
					Message received = gc.receive(100);
					
					
					pendingList.clear();
					for (Iterator<Message> iterator = pending.iterator(); iterator.hasNext();) {
						Message message = (Message) iterator.next();
						String str = "Nº Sequencia: " + message.getSequence() + " -> " + (String) message.getPayload();
						pendingList.add(str);
					}
					
					for (Iterator<Message> iterator = delivered.iterator(); iterator.hasNext();) {
						Message message = (Message) iterator.next();
						String str = "Nº Sequencia: " + message.getSequence() + " -> " + (String) message.getPayload();
						pendingList.add(str);
					}
					
					if (received != null) {

						//System.out.println("--------------------------------------");

						//System.out.println("---------------RECEBIDO---------------");
						Thread.sleep((int)GroupCommunicator.TIME_FACTOR*500);
						String str = "Nº Sequencia: " + received.getSequence() + " -> " + (String) received.getPayload();
						//System.err.println((String) received.getPayload());
						pendingList.add(0, str);
						window.updatePending(pendingList.toArray());

						Thread.sleep((int)GroupCommunicator.TIME_FACTOR*500);
						
						pendingList.remove(str);
						window.updatePending(pendingList.toArray());
						
						window.updateReceived(received.getPayload());
						//System.out.println("--------------------------------------");
					} else {
						window.updatePending(pendingList.toArray());
						//System.out.println("--------------------------------------");

					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});

		receiveingThread.start();
		
		Thread sequencingThread = null;
		if (paramId == gc.getSequencer()) {
			final LinkedBlockingQueue<Message> sequencedQueue = gc.sequencedMessages;
			SequencerWindow seqWindow = new SequencerWindow();
			sequencingThread = new Thread(() -> {
				try {
					while (!stop) {
						Message message = sequencedQueue.poll(100, TimeUnit.MILLISECONDS);
						if (message != null) {
							System.out.println("---------------SEQUENCIADO---------------");
							System.out.println(
									"Nº Sequencia: " + message.getSequence() + " -> " + (String) message.getPayload());
							System.out.println("--------------------------------------");
							seqWindow.updateSequenced((String) message.getPayload());
							Thread.sleep((int)GroupCommunicator.TIME_FACTOR*250);
							seqWindow.updateSequence();
							seqWindow.updateSequenced(message.getSequence()  + " -> " + (String) message.getPayload());
							Thread.sleep((int)GroupCommunicator.TIME_FACTOR*250);
							seqWindow.updateSequenced("");
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});
			sequencingThread.start();
		}

		gc.setDelayedBroadcast(true);
		int broadcastsAmount = Integer.valueOf(args[1]);
		Random rand = new Random();
		for (int i = 0; i < broadcastsAmount; i++) {
			String m = gc.getId()+""+String.valueOf((char)(i + 65));
			Thread.sleep((int)GroupCommunicator.TIME_FACTOR*(500+rand.nextInt(2000)));
			System.out.println("ENVIANDO");
			window.updateLastSent(m);
			gc.broadcast(m);
		}
		Thread.sleep((int)GroupCommunicator.TIME_FACTOR*1000);
		window.updateLastSent("");
		
		scanner.nextLine();
		stop = true;
		receiveingThread.join();
		if(paramId == gc.getSequencer()) {
			sequencingThread.join();
		}
		receiveingThread.join();

		gc.stop();
		gc.close();

		scanner.close();
	}
}
