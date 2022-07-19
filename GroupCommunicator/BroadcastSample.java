import java.io.IOException;
import java.util.Scanner;
import gc.OrderedGroupCommunicator;

public class BroadcastSample {
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
		final OrderedGroupCommunicator sock = loaded;
		
		System.out.println("Binding");
		sock.bind();

		scanner.nextLine();

		System.out.println("Connecting");
		sock.connect();
		System.out.println("Accepting");
		sock.accept();

		System.out.println("Done");

		sock.setDelayedBroadcast(true);
		if (sock.getId() == 0) {
			String m = "0";
			sock.broadcast(m);
			System.err.println((String) (sock.receive()));
			System.err.println((String) (sock.receive()));
			System.err.println((String) (sock.receive()));
		}
		if (sock.getId() == 1) {
			System.err.println((String) (sock.receive()));
			String m = "1";
			sock.broadcast(m);
			System.err.println((String) (sock.receive()));
			System.err.println((String) (sock.receive()));
		}
		if (sock.getId() == 2) {
			System.err.println((String) (sock.receive()));
			System.err.println((String) (sock.receive()));
			String m = "2";
			sock.broadcast(m);
			System.err.println((String) (sock.receive()));
		}

		scanner.nextLine();
		
		sock.stop();
		sock.close();
		
		scanner.close();
	}
}
