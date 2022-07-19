import java.io.IOException;
import java.util.Scanner;
import gc.OrderedGroupCommunicator;

public class ChangRobertsSample {

	//lider
	static int leader = -1;

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
		
		final OrderedGroupCommunicator sock = loaded;

		System.out.println("Binding");
		sock.bind();
		
        scanner.nextLine();

		System.out.println("Connecting");
		sock.connect();
		System.out.println("Accepting");
        sock.accept();
        
		System.out.println("Done");
		
		
		Thread receiveingThread = new Thread(()->{
			try {
				while(!stop) {
					Thread.sleep(1000);
					Object[] message = (Object[]) sock.receive(100);
					if(message != null) {
						if(message[0].equals("election")) {
							//eleição
							if(((int)message[2]) == sock.getId()) {
								//é o q iniciou a eleição
								//inicia divulgação do elegido
								int next = sock.getNeighbor();
								message[0] = "elected";
								leader = (int) message[1];
								
								System.out.println("O elegido foi definido: "+leader);
								System.out.println("Enviando para "+next);
								
								Thread.sleep(1000);
								sock.send(next, message);
							}else if(((int)message[1])<sock.getId()){
								//não é o q iniciou a eleição e é maior
								//atualiza o maior
								int next = sock.getNeighbor();

								System.out.println("Convocação de "+message[2]);
								System.out.println("Recebi: "+message[1]
										+", meu ID, "+ sock.getId()+", é maior");
								System.out.println("Enviando para "+next);
								
								message[1] = sock.getId();
								Thread.sleep(1000);
								sock.send(next, message);
							}else {
								//não é o q iniciou a eleição e não é maior
								//repassa a mensagem
								int next = sock.getNeighbor();
								System.out.println("Convocação de "+message[2]);
								System.out.println("Não sou maior, repassando "+ message[1] +" para "+next);
								
								Thread.sleep(1000);
								sock.send(next, message);
							}
						}else if(message[0].equals("elected")){
							if(((int)message[2]) != sock.getId()) {
								//mensagem de divulgação de elegido e 
								//não é o que iniciou a eleição
								//repassa a divulgação
								leader = (int) message[1];
								int next = sock.getNeighbor();
								System.out.println("Elegido: "+leader);
								System.out.println("Repassando para "+next);
								
								Thread.sleep(1000);
								sock.send(next, message);
							}else {
								System.out.println("Fim eleição");
							}
						}else if(message[0].equals("message")) {
							System.out.println(message[1]);
						}
					}
				}
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}
		});
		receiveingThread.start();
		
		System.out.println("Comandos:\n unicast <id> <messagem>\n broadcast <mensagem>\n eleger \n idlider \n stop \n close");
		while(true) {
			String comando = scanner.next();
			
			if(comando.equals("unicast")) {
				int id = scanner.nextInt();
				Object[] message = {"message",scanner.next()};
				sock.send(id, message);
			}else if(comando.equals("broadcast")){
				Object[] message = {"message",scanner.next()};
				sock.broadcast(message);
			}else if(comando.equals("eleger")){
				Object[] election = {"election", sock.getId(), sock.getId()};
				sock.send(sock.getNeighbor(), election);
			} else if(comando.equals("idlider")){
				System.out.println(leader);
			} else if(comando.equals("stop")){
				sock.stop();
			}else if(comando.equals("close")){
				sock.close();
				stop = true;
				break;
			}
			
		}
		
		scanner.close();
		receiveingThread.join();
	}

}
