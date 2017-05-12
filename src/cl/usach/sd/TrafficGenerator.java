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
		System.out.println("TRAFFIC GENERATOR");
		// Considera cualquier nodo de manera aleatoria de la red
		SNode initNode = (SNode) Network.get(CommonState.r.nextInt(Network.size()));
	
		//Par�metros para la creaci�n del mensaje	
		int sendId = CommonState.r.nextInt(Network.size());
		while(sendId == initNode.getID()) sendId = CommonState.r.nextInt(Network.size());
		int query = CommonState.r.nextInt(initNode.getBD().length);
		
		//Creaci�n del mensaje
		Message msg = new Message(sendId, query,(int) initNode.getID());

		// Y se envía, para realizar la simulación
		// Los parámetros corresponde a:
		// long arg0: Delay del evento
		// Object arg1: Evento enviado
		// Node arg2: Nodo por el cual inicia el envío del dato
		// int arg3: Número de la capa del protocolo que creamos (en este caso
		// de layerId)
		msg.getPath().push( (int) initNode.getID());
		EDSimulator.add(0, msg, initNode, layerId);

		return false;
	}

}
