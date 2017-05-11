package cl.usach.sd;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

public class TrafficGenerator implements Control {
	private final static String PAR_PROT = "protocol";
	private final int layerId;

	public TrafficGenerator(String prefix) {
		layerId = Configuration.getPid(prefix + "." + PAR_PROT);

	}

	@Override
	public boolean execute() {
		// Distribución uniforme entre [0-1]
		double uniformRandom = CommonState.r.nextDouble();

		// Considera cualquier nodo de manera aleatoria de la red
		Node initNode = Network.get(CommonState.r.nextInt(Network.size()));

		int sendNode = CommonState.r.nextInt(Network.size());

		// Se crea un nuevo mensaje
		Message message = new Message("Number random: " + uniformRandom, sendNode);

		// Y se envía, para realizar la simulación
		// Los parámetros corresponde a:
		// long arg0: Delay del evento
		// Object arg1: Evento enviado
		// Node arg2: Nodo por el cual inicia el envío del dato
		// int arg3: Número de la capa del protocolo que creamos (en este caso
		// de layerId)
		EDSimulator.add(0, message, initNode, layerId);

		return false;
	}

}
