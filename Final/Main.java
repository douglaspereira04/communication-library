import java.io.IOException;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Main {

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
		
		
		
		//começa sem líder
		int leader = -1;
		
		LinkedBlockingQueue<Object[]> received = new LinkedBlockingQueue<>();
		
		
		Thread receiveingThread = new Thread(()->{
			try {
				while(true) {
					Object[] message = (Object[]) sock.receive();
					if(message[0].equals("election")) {
						//eleição
						if(((int)message[2]) == sock.getId()) {
							//é o q iniciou a eleição
							//inicia divulgação do elegido
							int next = sock.getNeighbor();
							message[0] = "elected";
							sock.send(next, message);
						}else if(((int)message[1])<sock.getId()){
							//não é o q iniciou a eleição e é maior
							//atualiza o maior
							int next = sock.getNeighbor();
							message[1] = sock.getId();
							sock.send(next, message);
						}else {
							//não é o q iniciou a eleição e não é maior
							//repassa a mensagem
							int next = sock.getNeighbor();
							sock.send(next, message);
						}
					}else if(message[0].equals("elected") && ((int)message[2]) != sock.getId()) {
						//mensagem de divulgação de elegido e não é o que iniciou a eleição
						//repassa a divulgação
						int next = sock.getNeighbor();
						sock.send(next, message);
					}else if(message[0].equals("terminate")) {
						break;
					}else {
						received.add(message);
					}
				}
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}
		});
		receiveingThread.start();
		
		while(true) {
			System.out.println("Comandos:\n unicast <id> <messagem>\n broadcast <mensagem>\n receive <timeout>\n eleger");
			String comando = scanner.next();
			
			if(comando.equals("unicast")) {
				int id = scanner.nextInt();
				Object[] message = {"message",scanner.next()};
				sock.send(id, message);
			}else if(comando.equals("broadcast")){
				Object[] message = {"message",scanner.next()};
				sock.broadcast(message);
			}else if(comando.equals("receive")){
				long timeout = scanner.nextInt();
				Object[] message = received.poll(timeout, TimeUnit.MILLISECONDS);
				System.out.println((String)message[1]);
			}else if(comando.equals("eleger")){
				Object[] election = {"election", sock.getId(), sock.getId()};
				sock.send(sock.getNeighbor(), election);
			} else {
				Object[] message = {"terminate",scanner.next()};
				sock.broadcast(message);
				break;
			}
			
		}
		
		receiveingThread.join();
	}

}
