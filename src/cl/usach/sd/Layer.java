package cl.usach.sd;

import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.WireKOut;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;
import java.util.Stack;

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
		System.out.println("Current_Node: "+myNode.getID() );
		
		if(msg.getDestination() == myNode.getID()){
			if(msg.getData() < 0){
								
				Message answer = new Message(msg.getPath().pop(),msg.getQuery(),msg.getDestination());
				
				answer.setData(((SNode) myNode).getBD()[msg.getQuery()]);
				
				answer.setPath(msg.getPath());
				System.out.println("\tQuery received: "+msg.getQuery()+"- Requester Node "+msg.getRemitent()+"- Query value "+answer.getData());

				sendmessage(myNode, layerId, (Object) answer);
				
			}
			else{
				if(msg.getPath().isEmpty()){
					System.out.println("\tQuery answered: "+msg.getQuery()+"- Node: "+msg.getRemitent()+"- Query Value: "+msg.getData());
					((SNode) myNode).cacheUpdate(msg.getRemitent(), msg.getQuery(), msg.getData());
					((SNode) myNode).cacheShow();
				}
				else{
					((SNode) myNode).cacheUpdate(msg.getRemitent(), msg.getQuery(), msg.getData());
					((SNode)myNode).cacheShow();
					
					msg.setDestination(msg.getPath().pop());
					sendmessage(myNode,layerId,msg);				
				}
			}
		}
		else{
			if(msg.getRemitent() != myNode.getID()){
				if( ((SNode) myNode).cacheReview(msg.getDestination(), msg.getQuery())){
					System.out.println("Lo tengo en cach�");
				}
				else{
					System.out.println("\tQuery transmiter: "+msg.getQuery()+"- Query Node: "+msg.getDestination()+"- Query Value: ? ");
					msg.getPath().push( (int) myNode.getID());
					sendmessage(myNode,layerId,msg);
				}
			}
			else{
				System.out.println("\tQuery generator: "+msg.getQuery()+"- Query Node: "+msg.getDestination()+"- Query Value: ? ");
				sendmessage(myNode,layerId,msg);
			}
		}
		getStats();
	}

	private void getStats() {
		Observer.message.add(1);
	}

	public void sendmessage(Node currentNode, int layerId, Object msg) {
		Node bestNextNode;
		if( ((Message) msg).getData() < 0){
			bestNextNode = ((Linkable) currentNode.getProtocol(0)).getNeighbor(0);
			int destination = ((Message) msg).getDestination();
			int minDistance = moduleMinus(destination, (int) bestNextNode.getID());
			
			int DHTElements = 0;
			while(((SNode) currentNode).getDHT()[DHTElements]>0) DHTElements++;
			
			for(int i = 0; i < DHTElements;i++){
				int tempDistance = moduleMinus(destination,((SNode) currentNode).getDHT()[i]);
				if(tempDistance<minDistance){				
					bestNextNode = Network.get(((SNode) currentNode).getDHT()[i]);
					minDistance = tempDistance;
				}
			}
			
		}
		else{
			bestNextNode =  Network.get(((Message) msg).getDestination());
			//System.out.println(bestNextNode);
			//System.out.println("*");
		}
		((Transport) currentNode.getProtocol(transportId)).send(currentNode, bestNextNode, msg, layerId);
		//System.out.println("*");
		return;
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
