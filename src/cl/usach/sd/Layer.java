package cl.usach.sd;

import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.WireKOut;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

public class Layer implements Cloneable, EDProtocol {
	private static final String PAR_TRANSPORT = "transport";
	private static String prefix = null;
	private int transportId;
	private int layerId;

	/**
	 * Método en el cual se va a procesar el mensaje que ha llegado al Nodo
	 * desde otro Nodo. Cabe destacar que el mensaje va a ser el evento descrito
	 * en la clase, a través de la simulación de eventos discretos.
	 */
	@Override
	public void processEvent(Node myNode, int layerId, Object event) {
		Message msg = (Message) event;
		if(msg.getDestination() == myNode.getID()){
			System.out.println("Tengo que responderle a este tipo");
		}
		else{
			if(msg.getRemitent() != myNode.getID()){
				System.out.println("Debo revisar cach� un env�o como intermediario");
			}
			else{
				sendmessage(myNode,layerId,msg);
			}
		}
		getStats();
	}

	private void getStats() {
		Observer.message.add(1);
	}

	public void sendmessage(Node currentNode, int layerId, Object msg) {
		Node bestNextNode = ((Linkable) currentNode.getProtocol(0)).getNeighbor(0);
		int destination = ((Message) msg).getDestination();
		int minDistance = moduleMinus(destination, (int) bestNextNode.getID());
		int DHTElements = 0;
		
		while(((SNode) currentNode).getDHT()[DHTElements]>0) DHTElements++;
		System.out.println("El destino es "+((Message) msg).getDestination());
		System.out.println("El remitente es "+ ((Message) msg).getRemitent());
		
		for(int i = 0; i < DHTElements;i++){
			System.out.println("La mejor distancias es "+minDistance+" hacia el nodo "+bestNextNode.getID());
			int tempDistance = moduleMinus(((SNode) currentNode).getDHT()[i],destination);
			if(tempDistance<minDistance){				
				bestNextNode = Network.get(((SNode) currentNode).getDHT()[i]);
				minDistance = tempDistance;
			}
		}

		/**
		 * Envió del dato a través de la capa de transporte, la cual enviará
		 * según el ID del emisor y el receptor
		 */
		((Transport) currentNode.getProtocol(transportId)).send(currentNode, bestNextNode, msg, layerId);
		// Otra forma de hacerlo
		// ((Transport)
		// currentNode.getProtocol(FastConfig.getTransport(layerId))).send(currentNode,
		// searchNode(sendNode), message, layerId);

	}
	
	private int moduleMinus(int a, int b) {
		int answer = a-b;
		if(answer < 0){
			answer = Network.size()+answer;
		}
		return answer;
	}

	
	/**
	 * Constructor por defecto de la capa Layer del protocolo construido
	 * 
	 * @param prefix
	 */
	public Layer(String prefix) {
		/**
		 * Inicialización del Nodo
		 */
		Layer.prefix = prefix;
		transportId = Configuration.getPid(prefix + "." + PAR_TRANSPORT);
		/**
		 * Siguiente capa del protocolo
		 */
		layerId = transportId + 1;
	}

	private Node searchNode(int id) {
		return Network.get(id);
	}

	/**
	 * Definir Clone() para la replicacion de protocolo en nodos
	 */
	public Object clone() {
		Layer dolly = new Layer(Layer.prefix);
		return dolly;
	}
	
}
