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
		
		System.out.println("LAYER [time ="+CommonState.getTime()+"]");
		Message msg = (Message) event;
		System.out.println("Current_Node: "+myNode.getID() );
		
		//Si el nodo actual es el destino del mensaje
		if(msg.getDestination() == myNode.getID()){
		//Si no tiene ning˙n dato, quiere decir que debo responder una query
			if(msg.getData() < 0){
		//Se crea la respuesta, pero el destino corresponde al nodo que 
		//le enviÛ el mensaje, para asegurar que se mande por el mismo camino
		//pero en sentido contrario
				Message answer = new Message(msg.getPath().pop(),msg.getQuery(),msg.getDestination());
				
		//Se agregan los datos correspondientes
				answer.setData(((SNode3) myNode).getBD()[msg.getQuery()]);
				
		//Se agrega el camino que debe enviar
				answer.setPath(msg.getPath());
				
		//Se muestran los datos
				System.out.println("\tQuery received\n\t[node_destination,\tdata_id,\tdata]\n\t["+msg.getDestination()+",\t"+msg.getQuery()+",\t?]");
				System.out.println("\tAnswer \n\t[node_destination,\tdata_id,\tdata]\n\t["+answer.getDestination()+",\t"+answer.getQuery()+",\t"+answer.getData()+"]");

		//Se envÌa el mensaje
				sendmessage(myNode, layerId, (Object) answer);
			}
			else{
		//Se verifica si el camino est· vacÌo
				if(msg.getPath().isEmpty()){
		//Si est· vacÌo es porque recibÌ la respuesta solicitada, actualizo mi cachÈ
					System.out.println("\tQuery answered\n\t[node_destination,\tdata_id,\tdata]\n\t["+msg.getDestination()+",\t"+msg.getQuery()+",\t"+msg.getData()+"]");
					((SNode3) myNode).cacheUpdate(msg.getRemitent(), msg.getQuery(), msg.getData());
		//Se muestra la cachÈ			
					System.out.println("\tCache update");
					((SNode3) myNode).cacheShow();
				}
				else{
		//Si no, entonces soy parte del camino de regreso de la respuesta
		//Se actualiza la cachÈ
					((SNode3) myNode).cacheUpdate(msg.getRemitent(), msg.getQuery(), msg.getData());
		//Se muestra por pantalla el cache actualizado
					System.out.println("\tCache update");
					((SNode3)myNode).cacheShow();
		//Se actualiza el siguiente destino
					msg.setDestination(msg.getPath().pop());
					System.out.println("\tAnswer transmited\n\t[node_destination,\tdata_id,\tdata]\n\t["+msg.getDestination()+",\t"+msg.getQuery()+",\t"+msg.getData()+"]");
		//Se envÌa el mensaje
					sendmessage(myNode,layerId,msg);				
				}
			}
		}
		else{
		//Si no soy el destino entonces se debe revisar si es mi solicitud yo o si llegÛ a un
		//nodo intermediario
			if(msg.getRemitent() != myNode.getID()){
		//Si soy un nodo intermediario, debo revisar si tengo la consulta en cachÈ
				if( ((SNode3) myNode).cacheReview(msg.getDestination(), msg.getQuery())){
		//Si lo tengo en cachÈ entonces debo responder la solicitud y enviar un mensaje de vuelta
					System.out.println("\tCache hit");
					int[] hit = ((SNode3) myNode).cacheHit(msg.getDestination(), msg.getQuery());					
					Message answer = new Message(msg.getPath().pop(),msg.getQuery(),msg.getDestination());
					answer.setData(hit[2]);
					answer.setPath(msg.getPath());
					System.out.println("\tQuery answered \n\t[node_destination,\tdata_id,\tdata]\n\t["+answer.getDestination()+",\t"+answer.getQuery()+",\t"+answer.getData()+"]");
					sendmessage(myNode, layerId, (Object) answer);	
				}
				else{
		//Si no lo tengo en cachÈ entonces debo reenviar la solicitud al siguiente mejor nodo
					System.out.println("\tCache miss");
					System.out.println("\tQuery transmited \n\t[node_destination,\tdata_id,\tdata]\n\t["+msg.getDestination()+",\t"+msg.getQuery()+",\t?]");
					msg.getPath().push( (int) myNode.getID());
					sendmessage(myNode,layerId,msg);
				}
			}
			else{
		//Si soy el que mandÛ la solicitud, debo ver si lo tengo en cachÈ
				if( ((SNode3) myNode).cacheReview(msg.getDestination(), msg.getQuery())){
		//Si lo tengo en cachÈ, la solicitud es respondida inmediatamente y no se envÌa el mensaje
					System.out.println("\tCache hit");
					int[] hit = new int[3];
					hit = ((SNode3) myNode).cacheHit(msg.getDestination(), msg.getQuery());
					System.out.println("\tQuery answered \n\t[node_destination,\tdata_id,\tdata]\n\t["+hit[0]+",\t"+hit[1]+",\t"+hit[2]+"]");
				}
				else{
		//De lo contrario hubo un miss en el cachÈ y por tanto se debe enviar el mensaje
					System.out.println("\tCache miss");
					System.out.println("\tQuery generated \n\t[node_destination,\tdata_id,\tdata]\n\t["+msg.getDestination()+",\t"+msg.getQuery()+",\t?]");
					sendmessage(myNode,layerId,msg);
				}
			}
		}
		getStats();
	}

	private void getStats() {
		Observer.message.add(1);
	}

	/* MÈtodo para enviar un mensaje asegurando que siempre se enviar· el mejor nodo 
	 * seg˙n el algoritmo del enunciado o ser· envÌado siguiendo el camino inverso
	 * Recibe como entrada:
	 * 		current node: el nodo actual
	 * 		layerId: el id del layer por donde se envÌa el mensaje
	 * 		msg: mensaje que ser· enviado
	 * */
	public void sendmessage(Node currentNode, int layerId, Object msg) {
		Node bestNextNode;
		//Se verifica si el mensaje es una solicitud o una respuesta
		if( ((Message) msg).getData() < 0){
		//Si es una solicitud
		//Se asume que el mejor nodo es el vecino
			bestNextNode = ((Linkable) currentNode.getProtocol(0)).getNeighbor(0);
		//Se obtiene el destino
			int destination = ((Message) msg).getDestination();
		//Se obtiene la distancia entre el destino y el mejor nodo actual
			int minDistance = moduleMinus(destination, (int) bestNextNode.getID());
			
			int DHTElements = (int) Math.floor(Math.log(Network.size())/Math.log(2));

		
		//Se deben verificar si existen mejor distancias con los elementos de la DHT
			for(int i = 0; i < DHTElements;i++){
				int tempDistance = moduleMinus(destination,((SNode3) currentNode).getDHT()[i]);
				if(tempDistance<minDistance){		
		//Si la distancia con el nodo actual es mejor que la distancia mÌnima
		//Se actualiza el mejor nodo y se actualiza la distancia mÌnima
					bestNextNode = Network.get(((SNode3) currentNode).getDHT()[i]);
					minDistance = tempDistance;
				}
			}
			System.out.println("\t\tBest next node: "+ bestNextNode.getID());
			System.out.println("\t\tNext Nodes's distance to destiny: "+ minDistance);
		}
		else{
		//Si es una respuesta, entonces solo se debe enviar al nodo que est·
		//especÌficado en el mensaje, ya que se asegura que es el del camino inverso
			bestNextNode =  Network.get(((Message) msg).getDestination());
		}
		//El mensaje es enviado
		((Transport) currentNode.getProtocol(transportId)).send(currentNode, bestNextNode, msg, layerId);
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
