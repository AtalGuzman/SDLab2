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
	private int ttl = Configuration.getInt("init.1statebuilder.ttl");
	private int k = Configuration.getInt("init.1statebuilder.k");
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
			if(!((SNode3) myNode).getMsg().contains(msg.getId())){
			//Si no tiene ning˙n dato, quiere decir que debo responder una query
				if(msg.getData() < 0){
					((SNode3) myNode).getMsg().add(msg.getId());
					System.out.println("He recibido una solicitud");
			//Se muestran los datos
					System.out.println("\tQuery \n\t[super-peer_destination, data_id, data, ttl]\n\t["+msg.getDestination()+",\t"+msg.getQuery()+",\t?, "+msg.getTtl()+"]");
					//sendmessage(myNode, layerId, (Object) answer);
					msg.getPath().push((int)myNode.getID());
					msg.setTtl(ttl);
					this.krandom(this.k, msg, (SNode3) myNode);
					
				}else{
			//EL mensaje corresponde a una respuesta.
					if(((SNode3) myNode).getSuper_peer() == 1){
						System.out.println("\tActualizar CachÈ");
						((SNode3) myNode).cacheUpdate(msg.getRemitent(),((SNode3) Network.get(msg.getRemitent())).getMiSubNet(), msg.getData(), msg.getData());
					}
					if(msg.getPath().isEmpty()){
						System.out.println("Se ha finalizado la consulta");
						System.out.println("\tQuery Answered\n\t[node, super_peer, data_id, data]\n\t["+msg.getRemitent()+",\t"+((SNode3) Network.get(msg.getRemitent())).getMiSubNet()+",\t"+msg.getQuery()+",\t"+msg.getData()+"]");
					}
					else{
						int nextNode = msg.getPath().pop();
						msg.setDestination(nextNode);
						this.sendmessage(myNode, Network.get(nextNode), layerId, msg);
					}
				}
			} 
			else{
				System.out.println("\tMENSAJE REDUNDANTE ID: "+msg.getId());
			}
		}
		else{
		//Si no soy el destino entonces se debe revisar si es mi solicitud yo o si llegÛ a un
		//nodo intermediario
			if(msg.getRemitent() != myNode.getID()){
		//Si soy un nodo intermediario
				if( ((SNode3) myNode).getSuper_peer() == 1){
		//Si es un super-peer se debe utilizar chord
					if(!((SNode3) myNode).msgRegister(msg.getId())){
						System.out.println("\tSuper-peer "+myNode.getID()+"\n\tCHORD");
						msg.getPath().push((int)myNode.getID());
						System.out.println("\tMENSAJE ID: "+msg.getId());
						System.out.println("\tQuery \n\t[super-peer_destination, data_id, data, ttl]\n\t["+msg.getDestination()+",\t"+msg.getQuery()+",\t?, "+msg.getTtl()+"]");
						//System.out.println("\tQuery answered \n\t[node_destination,\tdata_id,\tdata]\n\t["+answer.getDestination()+",\t"+answer.getQuery()+",\t"+answer.getData()+"]");
						msg.setTtl(this.ttl);
						if(msg.getTtl()<=0){
							System.out.println("\tSe ha acabado el ttl de la query");
						} 
						else{ 
							((SNode3) myNode).getMsg().add(msg.getId());
							sendMessageChord(myNode, layerId, (Object) msg,superpeer);	
						}
					} else{
						System.out.println("\tMENSAJE REDUNDANTE ID: "+msg.getId());
					}
				}
				else if(((SNode3) myNode).getSuper_peer() == 0){
		//Debo revisar si contengo la consulta solicitada en mi mismo 
					int[] BD = ((SNode3) myNode).getBD();
					int encontrado = -1;
					int index = 0;
					for(int i = 0; i < BD.length;i++){
						if(BD[i] == msg.getQuery()){
							System.out.println("\tSolicitud encontrada en el nodo "+myNode.getID());
							encontrado = 1;
							index = i;
						}
					}
					if(encontrado != 1){
		//Si soy un peer normal se debe utilizar k-random walks
						if(((SNode3) myNode).getSubNet() != ((SNode3) Network.get(msg.getRemitent())).getSubNet()) System.out.println("\tNO SE HA ENCONTRADO LA SOLICITUD EN LA SUB-RED");
						System.out.println("\tSoy un peer normal, debo continuar con K-random");
						System.out.println("\tMENSAJE ID: "+msg.getId());
						System.out.println("\tQuery \n\t[super-peer_destination, data_id, data, ttl]\n\t["+msg.getDestination()+",\t"+msg.getQuery()+",\t?, "+msg.getTtl()+"]");
						msg.setTtl(msg.getTtl()-1);
						if(msg.getTtl()<=0){
							System.out.println("\tSe ha acabado el ttl de la query");
						} 
						else{ 
							this.krandom(1, msg, (SNode3) myNode);
						}
					}		
					else if(encontrado == 1){
						System.out.println("\tSe ha encontrado la Query");
						System.out.println("\tQuery \n\t[super-peer_destination, data_id, data, ttl]\n\t["+msg.getDestination()+",\t"+msg.getQuery()+",\t?, "+msg.getTtl()+"]");
						Message answer = msg;
						answer.setRemitent((int) myNode.getID());
						answer.setData( ((SNode3) myNode).getBD()[index] );
						int nextNode = answer.getPath().pop();
						answer.setDestination(nextNode);
						answer.setId(Integer.toString(answer.getDestination())+Integer.toString(answer.getQuery())+Integer.toString(answer.getRemitent()));
						System.out.println("\tMENSAJE ID: "+answer.getId());
						System.out.println("\tSe enviar· el mensaje a "+nextNode);
						this.sendmessage(myNode, Network.get(nextNode),layerId, answer);
					}
				} else {/*Nada*/}
			}
			else{
		//Si soy el que mandÛ la solicitud, debo iniciar la b˙squeda en k-random hacia el super-peer
				System.out.println("\tSe inicia la b˙squeda k-random walks");
				System.out.println("\tQuery \n\t[super-peer_destination, data_id, data, ttl]\n\t["+msg.getDestination()+",\t"+msg.getQuery()+",\t?, "+msg.getTtl()+"]");
				System.out.println("\tMENSAJE ID: "+msg.getId());
				this.krandom(this.k, msg, (SNode3) myNode);
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
			System.out.println("\t\tHash mejor nodo: "+ ((SNode3) bestNextNode).getHash().substring(0, 3)+ "(ID: "+bestNextNode.getID()+")");
			System.out.println("\t\tDistancia del siguiente nodo al destino: "+ minDistance);
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
		System.out.println("\tSE ENVÕAN "+k+" WALKERS");
		if(node.getSuper_peer() == 1){
			for(int i = 0; i< k && i < degree; i++){
				destiny = CommonState.r.nextInt(degree);
				SNode3 nextNode = (SNode3) ((Linkable) node.getProtocol(0)).getNeighbor(destiny);
				while(nextNode.getSuper_peer() != 0){
					//System.out.println("*** "+destiny);
					destiny = CommonState.r.nextInt(degree);
					nextNode = (SNode3) ((Linkable) node.getProtocol(0)).getNeighbor(destiny);
				}
				//nextNode = (SNode3) Network.get(6);
				System.out.println("\tRandom Walk hacia: "+nextNode.getID());
				sendmessage(node, nextNode, layerId, (Object) msg);
			}
		}
		else{
			for(int i = 0; i< k && i < degree; i++){
				destiny = CommonState.r.nextInt(degree);
				SNode3 nextNode = (SNode3) ((Linkable) node.getProtocol(0)).getNeighbor(destiny);
				System.out.println("\tRandom Walk hacia: "+nextNode.getID());
				sendmessage(node, nextNode, layerId, (Object) msg);
			}
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
