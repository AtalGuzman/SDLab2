package cl.usach.sd;

import java.util.Stack;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.edsim.EDSimulator;

public class TrafficGenerator implements Control {
	private final static String PAR_PROT = "protocol";
	private final int layerId;

	public TrafficGenerator(String prefix) {
		layerId = Configuration.getPid(prefix + "." + PAR_PROT);

	}

	@Override
	public boolean execute() {
		System.out.println("\nTRAFFIC GENERATOR [time = "+CommonState.getTime()+"]");
		// Considera cualquier nodo peer de manera aleatoria de la red
		SNode3 initNode = (SNode3) Network.get(CommonState.r.nextInt(Network.size()));
		int cant_super = 0;
		for(int i = 0; i < Network.size();i++){
			if(  ((SNode3)Network.get(i)).getSuper_peer() == 1 ) cant_super++;
		}
		while(initNode.getSuper_peer() != 0){
			 initNode = (SNode3) Network.get(CommonState.r.nextInt(Network.size()));
		}
		
		//Parámetros para la creación del mensaje	
		int sendId = CommonState.r.nextInt(cant_super);
		
		
		int query = CommonState.r.nextInt(Network.size()-cant_super)+cant_super;
		
		System.out.println("Nodo "+initNode.getID()+" solicita el documento "+query+" al super-peer "+sendId);
		
		System.out.println("\t\nGENERATOR NODE: "+initNode.getID()+"\n");
		//Creación del mensaje
		Message msg = new Message(sendId, query,(int) initNode.getID(),sendId);
		
		System.out.print("\tIP: "+initNode.getID()+" ");
		System.out.print("\tNeighbours: ");
		int degree = ((Linkable) initNode.getProtocol(0)).degree();
		for(int k = 0; k < degree;k++){
			System.out.print(((Linkable) initNode.getProtocol(0)).getNeighbor(k).getID()+" ");
		}
		System.out.print("\t\tBD: ");
		for(int k = 0; k < initNode.getBD().length;k++){
			System.out.print(initNode.getBD()[k]+" ");
		}
		msg.getPath().push((int)initNode.getID());

		//msg.setDestination(0);
		//msg.setQuery(45);
		msg.setId(Integer.toString(msg.getDestination())+Integer.toString(msg.getQuery())+Integer.toString(msg.getRemitent()));
		//Se agrega al camino del mensaje la id del nodo precursor
		EDSimulator.add(0, msg, initNode, layerId);

		return false;
	}

}
