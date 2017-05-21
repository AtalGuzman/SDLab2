package cl.usach.sd;

import java.util.Stack;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
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
		System.out.println("TRAFFIC GENERATOR [time = "+CommonState.getTime()+"]");
		// Considera cualquier nodo de manera aleatoria de la red
		SNode initNode = (SNode) Network.get(CommonState.r.nextInt(Network.size()));
	
		//Par�metros para la creaci�n del mensaje	
		int sendId = CommonState.r.nextInt(Network.size());
		while(sendId == initNode.getID()) sendId = CommonState.r.nextInt(Network.size());
		int query = CommonState.r.nextInt(initNode.getBD().length);
		System.out.println("\t\nGENERATOR NODE: "+initNode.getID()+"\n");
		//Creaci�n del mensaje
		Message msg = new Message(sendId, query,(int) initNode.getID());

		//Se agrega al camino del mensaje la id del nodo precursor
		msg.getPath().push( (int) initNode.getID());
		EDSimulator.add(0, msg, initNode, layerId);

		return false;
	}

}
