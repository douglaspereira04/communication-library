import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;

public class ConfigLoader {

	public static OrderedGroupCommunicator loadT5Socket(String fileName, int overId) throws IOException, InterruptedException {
		int processAmount = -1;
		int id = -1;
		HashMap<Integer, InetSocketAddress> socketAddresses = new HashMap<>();
		BufferedReader reader;
		String[] address = null;
		String line = null;
		String hostname = null;
		int port = -1;
		try {
			reader = new BufferedReader(new FileReader(fileName));
			
			line = reader.readLine();//first line is process amount line
			processAmount = Integer.valueOf(line.split("=")[1].trim());

			line = reader.readLine();//second line is id line
			id = Integer.valueOf(line.split("=")[1].trim());
			
			for (int i = 0; i < processAmount; i++) {//then lines are addreesses
				line = reader.readLine();
				Integer procId = Integer.valueOf(line.split("=")[0].trim());
				address = line.split("=")[1].trim().split(":");
				hostname = address[0];
				port = Integer.valueOf(address[1]);
				socketAddresses.put(procId, new InetSocketAddress(hostname, port));
			}
			
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		id = overId;
		return new OrderedGroupCommunicator(id, socketAddresses);
	}

}
