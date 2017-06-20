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
	 * M√©todo en el cual se va a procesar el mensaje que ha llegado al Nodo
	 * desde otro Nodo. Cabe destacar que el mensaje va a ser el evento descrito
	 * en la clase, a trav√©s de la simulaci√≥n de eventos discretos.
	 */
	@Override
	public void processEvent(Node myNode, int layerId, Object event) {
		int superpeer = 0;

		for(int i = 0; i < Network.size();i++){
			SNode3 node =(SNode3) Network.get(i);
			if(node.getSuper_peer() == 1) superpeer++;
		}
		
		System.out.println("Hay "+superpeer+" superpeers");
		System.out.println("LAYER [time ="+CommonState.getTime()+"]");
		Message msg = (Message) event;
		System.out.println("Current_Node: "+myNode.getID() );
		
		//Si el nodo actual es el destino del mensaje
		if(msg.getDestination() == myNode.getID()){
			
		//Si no tiene ning˙n dato, quiere decir que debo responder una query
			if(msg.getData() < 0){
		
				System.out.println("He recibido una solicitud");
		//Se muestran los datos
				System.out.println("\tQuery received\n\t[super-peer_destination,\tdata_id,\tdata]\n\t["+msg.getDestination()+",\t"+msg.getQuery()+",\t?]");
				//sendmessage(myNode, layerId, (Object) answer);
			}
			
		}
		else{
		//Si no soy el destino entonces se debe revisar si es mi solicitud yo o si llegÛ a un
		//nodo intermediario
			if(msg.getRemitent() != myNode.getID()){
		//Si soy un nodo intermediario
				if( ((SNode3) myNode).getSuper_peer() == 1){
		//Si es un super-peer se debe utilizar chord
					System.out.println("\tSuper-peer "+myNode.getID()+"\n\tCHORD");
					//System.out.println("\tQuery answered \n\t[node_destination,\tdata_id,\tdata]\n\t["+answer.getDestination()+",\t"+answer.getQuery()+",\t"+answer.getData()+"]");
					sendMessageChord(myNode, layerId, (Object) msg,superpeer);	
				}
				else if(((SNode3) myNode).getSuper_peer() == 0){
		//Si soy un peer normal se debe utilizar k-random walks
					System.out.println("\tSoy un peer normal, debo usar K-random");
					//System.out.println("\tQuery transmited \n\t[node_destination,\tdata_id,\tdata]\n\t["+msg.getDestination()+",\t"+msg.getQuery()+",\t?]");
					//sendmessage(myNode,layerId,msg);
				} else { /*Nada*/}
			}
			else{
		//Si soy el que mandÛ la solicitud, debo iniciar la b˙squeda en k-random hacia el supeer, peer
				System.out.println("Se inicia la b˙squeda k-random walks");
				this.krandom(2, msg, (SNode3) myNode);
			}
		}
		getStats();
	}

	private void sendMessageChord(Node currentNode, int layerId, Object msg, int cantSuperPeer) {
		Node bestNextNode;
		//Se verifica si el mensaje es una solicitud o una respuesta
		if( ((Message) msg).getData() < 0){
		//Si es una solicitud
		//Se asume que el mejor nodo es el vecino
			bestNextNode = ((Linkable) currentNode.getProtocol(0)).getNeighbor(0);
		//Se obtiene el destino
			int destination = ((Message) msg).getDestination();
		//Se obtiene la distancia entre el destino y el mejor nodo actual
			int minDistance = moduleMinus(destination, Integer.toString((int)bestNextNode.getID()));
			
			int DHTElements = (int) Math.floor(Math.log(cantSuperPeer)/Math.log(2));			
		
		//Se deben verificar si existen mejor distancias con los elementos de la DHT
			for(int i = 0; i < DHTElements;i++){
				int tempDistance = moduleMinus(destination,((SNode3) currentNode).getDHT()[i][0]);
				if(tempDistance<minDistance){		
		//Si la distancia con el nodo actual es mejor que la distancia mÌnima
		//Se actualiza el mejor nodo y se actualiza la distancia mÌnima
					bestNextNode = Network.get(Integer.parseInt(((SNode3) currentNode).getDHT()[i][0]));
					minDistance = tempDistance;
				}
			}
			System.out.println("\t\tBest next node Hash: "+ ((SNode3) bestNextNode).getHash().substring(0, 3)+ "(ID: "+bestNextNode.getID()+")");
			System.out.println("\t\tNext Nodes's distance to destiny: "+ minDistance);
		}
		else{
		//Si es una respuesta, entonces solo se debe enviar al nodo que est·
		//especÌficado en el mensaje, ya que se asegura que es el del camino inverso
			bestNextNode =  Network.get(((Message) msg).getDestination());
		}
		//El mensaje es enviado
		//((Transport) currentNode.getProtocol(transportId)).send(currentNode, bestNextNode, msg, layerId);
		return;
	}

	private void getStats() {
		Observer.message.add(1);
	}

	/* MÈtodo para enviar un mensaje en k-random walks, o de vuelta de la solicitud para hacer los envÌos directos
	 * Recibe como entrada:
	 * 		current node: el nodo actual
	 * 		layerId: el id del layer por donde se envÌa el mensaje
	 * 		msg: mensaje que ser· enviado
	 * */
	public void sendmessage(Node currentNode, Node nextNode, int layerId, Object msg) {	
		((Transport) currentNode.getProtocol(transportId)).send(currentNode, nextNode, msg, layerId);
		return;
	}
	
	/* MÈtodo para obtener la distancia de dos elementos
	 * sobre una circunferencia y que adem·s solo 
	 * se puede mover en la direcciÛn de las agujas del reloj
	 * Recibe como entrada:
	 * 		a: id de un nodo
	 * 		b: id de otro nodo
	 * Retorna: La distancia entre los nodos a partir de sus id
	 * */
	private int moduleMinus(int a, String b) {
		int b2 = Integer.parseInt(b);
		int answer = a-b2;
		if(answer < 0){
			answer = Network.size()+answer;
		}
		return answer;
	}
	/*MÈtodo utilizado para la implementaciÛn de k-random walks
	 * Recibe como entrada la cantidad k walkers, el mensaje original
	 * y el nodo precursor del mensaje
	 * */
	private void krandom(int k, Message msg, SNode3 node){
		int destiny = 0;
		int degree = ((Linkable) node.getProtocol(0)).degree();
		
		for(int i = 0; i< k; i++){
			destiny = CommonState.r.nextInt(degree);
			SNode3 nextNode = (SNode3) ((Linkable) node.getProtocol(0)).getNeighbor(destiny);
			System.out.println("Se envÌa el mensaje al nodo "+nextNode.getID());
			sendmessage(node, nextNode, layerId, (Object) msg);
		}
		return;
	}
	/**
	 * Constructor por defecto de la capa Layer del protocolo construido
	 * 
	 * @param prefix
	 */
	public Layer(String prefix) {
		/**
		 * Inicializaci√≥n del Nodo
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
