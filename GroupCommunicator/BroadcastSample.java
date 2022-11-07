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
					ArrayList<Message> pendingList = new ArrayList<>();
					for (Iterator<Message> iterator = pending.iterator(); iterator.hasNext();) {
						Message message = (Message) iterator.next();
						pendingList.add(message);
					}
					
					for (Iterator<Message> iterator = delivered.iterator(); iterator.hasNext();) {
						Message message = (Message) iterator.next();
						pendingList.add(message);
					}
					window.updatePending(pendingList);
					
					Message received = gc.receive(100);
					
					
					pendingList.clear();
					for (Iterator<Message> iterator = pending.iterator(); iterator.hasNext();) {
						Message message = (Message) iterator.next();
						pendingList.add(message);
					}
					
					for (Iterator<Message> iterator = delivered.iterator(); iterator.hasNext();) {
						Message message = (Message) iterator.next();
						pendingList.add(message);
					}
					
					if (received != null) {

						//System.out.println("--------------------------------------");

						//System.out.println("---------------RECEBIDO---------------");
						Thread.sleep((int)GroupCommunicator.TIME_FACTOR*500);
						//System.err.println((String) received.getPayload());
						pendingList.add(0, received);
						window.updatePending(pendingList);

						Thread.sleep((int)GroupCommunicator.TIME_FACTOR*500);
						
						pendingList.remove(received);
						window.updatePending(pendingList);
						
						window.updateReceived(received);
						//System.out.println("--------------------------------------");
					} else {
						window.updatePending(pendingList);
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
							long seq = message.getSequence();
							message.setSequence(-1);
							seqWindow.updateSequenced(message);
							Thread.sleep((int)GroupCommunicator.TIME_FACTOR*250);
							seqWindow.updateSequence();
							message.setSequence(seq);
							seqWindow.updateSequenced(message);
							Thread.sleep((int)GroupCommunicator.TIME_FACTOR*250);
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
			String m = String.valueOf((char)(i + 65));
			Thread.sleep((int)GroupCommunicator.TIME_FACTOR*(500+rand.nextInt(2000)));
			System.out.println("ENVIANDO");
			window.updateLastSent(m);
			gc.broadcast(m, paramId);
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
