Compilar:
	javac gc/*.java
	jar cf gc.jar gc/GroupCommunicator.class gc/Message.class gc/MessageType.class gc/OrderedGroupCommunicator.class gc/VectorClock.class
	javac -cp gc.jar *.java

Executar:
	Para cada id em terminais diferentes:
		java ChangRobertsSample <id>
		ou
		java UnicastSample <id>
		ou
		java BroadcastSample <id>
	
	Dar enter depois que todos forem iniciados e estiverem em Bind
	
Os ids dos processos para os exemplos UnicastSample e BroadcastSample devem ser necessáriamente 0, 1 e 2.
Para ChangRobertsSample pode ser configurado qualquer valor.
	
