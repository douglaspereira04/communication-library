import java.io.IOException;
import java.util.Scanner;

import gc.ConfigLoader;
import gc.OrderedGroupCommunicator;

public class UnicastSample {
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

		if (gc.getId() == 0) {
			String m = "0";
			gc.send(2, m, 1000);
			gc.send(1, m, -1);

		}
		if (gc.getId() == 1) {
			System.err.println((String) (gc.receive().getPayload()));
			String m = "1";
			gc.send(2, m, -1);
		}
		if (gc.getId() == 2) {
			for (int i = 0; i < 2; i++) {
				System.err.println((String) (gc.receive().getPayload()));
			}
		}
		
		scanner.nextLine();

		gc.stop();
		gc.close();
		
		scanner.close();
	}
}
