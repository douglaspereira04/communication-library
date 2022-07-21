import java.io.IOException;
import java.util.Iterator;
import java.util.Scanner;

import gc.ConfigLoader;
import gc.OrderedGroupCommunicator;

public class BroadcastSample {
	static boolean stop = false;
	public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
		Scanner scanner = new Scanner(System.in);
		Integer paramId = null;
		OrderedGroupCommunicator loaded = null;
		try {
			paramId = Integer.valueOf(args[0]);
		} catch (Exception e) {}
		if(paramId == null) {
			loaded = ConfigLoader.loadOGC("node.config");
		}else {
			loaded = ConfigLoader.loadOGC("node.config", paramId);
		}
		final OrderedGroupCommunicator gc = loaded;
		
		System.out.println("Init");
		gc.init();
		System.out.println("Done");

		gc.setDelayedBroadcast(true);
		int broadcastsAmount = Integer.valueOf(args[1]);
		for (int i = 0; i < broadcastsAmount; i++) {
			String m = gc.getId()+"-"+i;
			gc.broadcast(m);
		}
		
		Thread receiveingThread = new Thread(()->{
			try {
				while (!stop) {
					Object received = gc.receive(100);
					if(received != null) {
						System.err.println((String)received);
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		
		receiveingThread.start();
		scanner.nextLine();
		stop = true;
		receiveingThread.join();
		
		gc.stop();
		gc.close();
		
		scanner.close();
	}
}
