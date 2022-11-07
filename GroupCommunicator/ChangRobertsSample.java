import java.io.IOException;
import java.util.Scanner;

import gc.ConfigLoader;
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
		
		final OrderedGroupCommunicator gc = loaded;

		System.out.println("Init");
		gc.init();
		System.out.println("Done");
		
		Thread receiveingThread = new Thread(()->{
			try {
				while(!stop) {
					Thread.sleep(1000);
					Object[] message = (Object[]) gc.receive(100).getPayload();
					if(message != null) {
						if(message[0].equals("election")) {
							//eleição
							if(((int)message[2]) == gc.getId()) {
								//é o q iniciou a eleição
								//inicia divulgação do elegido
								int next = gc.getNeighbor();
								message[0] = "elected";
								leader = (int) message[1];
								
								System.out.println("O elegido foi definido: "+leader);
								System.out.println("Enviando para "+next);
								
								Thread.sleep(1000);
								gc.send(next, message, -1);
							}else if(((int)message[1])<gc.getId()){
								//não é o q iniciou a eleição e é maior
								//atualiza o maior
								int next = gc.getNeighbor();

								System.out.println("Convocação de "+message[2]);
								System.out.println("Recebi: "+message[1]
										+", meu ID, "+ gc.getId()+", é maior");
								System.out.println("Enviando para "+next);
								
								message[1] = gc.getId();
								Thread.sleep(1000);
								gc.send(next, message, -1);
							}else {
								//não é o q iniciou a eleição e não é maior
								//repassa a mensagem
								int next = gc.getNeighbor();
								System.out.println("Convocação de "+message[2]);
								System.out.println("Não sou maior, repassando "+ message[1] +" para "+next);
								
								Thread.sleep(1000);
								gc.send(next, message, -1);
							}
						}else if(message[0].equals("elected")){
							if(((int)message[2]) != gc.getId()) {
								//mensagem de divulgação de elegido e 
								//não é o que iniciou a eleição
								//repassa a divulgação
								leader = (int) message[1];
								int next = gc.getNeighbor();
								System.out.println("Elegido: "+leader);
								System.out.println("Repassando para "+next);
								
								Thread.sleep(1000);
								gc.send(next, message, -1);
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
		
		System.out.println("Comandos:\n unicast <id> <delay> <messagem>\n broadcast <mensagem>\n eleger \n idlider \n stop \n close");
		while(true) {
			String comando = scanner.next();
			
			if(comando.equals("unicast")) {
				int id = scanner.nextInt();
				int delay = scanner.nextInt();
				Object[] message = {"message",scanner.next()};
				if(delay > 0) {
					gc.send(id, message, delay);
				}else {
					gc.send(id, message, -1);
				}
			}else if(comando.equals("broadcast")){
				Object[] message = {"message",scanner.next()};
				gc.broadcast(message, -1);
			}else if(comando.equals("eleger")){
				Object[] election = {"election", gc.getId(), gc.getId()};
				gc.send(gc.getNeighbor(), election, -1);
			} else if(comando.equals("idlider")){
				System.out.println(leader);
			} else if(comando.equals("stop")){
				gc.stop();
			}else if(comando.equals("close")){
				gc.close();
				stop = true;
				break;
			}
			
		}
		
		scanner.close();
		receiveingThread.join();
	}

}
