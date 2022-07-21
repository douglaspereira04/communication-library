Compilar:
	javac gc/*.java
	jar cf gc.jar gc/GroupCommunicator.class gc/Message.class gc/MessageType.class gc/OrderedGroupCommunicator.class gc/VectorClock.class gc/ConfigLoader.class
	javac -cp gc.jar *.java

Executar:
	Para cada id em terminais diferentes:
		java ChangRobertsSample <id>
		ou
		java UnicastSample <id>
		ou
		java BroadcastSample <id> <numero de broadcasts que serao feitos>
	
Os ids dos processos para o exemplo UnicastSample  deve ser necessáriamente 0, 1 e 2.
Para ChangRobertsSample, BroadcastSample pode ser configurado qualquer valor.
	
