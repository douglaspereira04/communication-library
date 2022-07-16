import java.io.IOException;
import java.util.Scanner;

public class BroadcastSample {
	public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
		Scanner scanner = new Scanner(System.in);

		OrderedGroupCommunicator sock = ConfigLoader.loadT5Socket("node.config", Integer.valueOf(args[0]));

		System.out.println("Binding");
		sock.bind();

		scanner.nextLine();

		System.out.println("Connecting");
		sock.connect();
		System.out.println("Accepting");
		sock.accept();

		System.out.println("Done");

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
	}
}
