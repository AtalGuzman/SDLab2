package cl.usach.sd;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.util.IncrementalStats;
import peersim.core.Linkable;

public class Observer implements Control {

	private int layerId;
	private String prefix;

	public static IncrementalStats message = new IncrementalStats();

	public Observer(String prefix) {
		this.prefix = prefix;
		this.layerId = Configuration.getPid(prefix + ".protocol");
	}

	@Override
	public boolean execute() {
		int size = Network.size();
		for (int i = 0; i < Network.size(); i++) {
			SNode node = (SNode) Network.get(i);
			if (!node.isUp()) {
				size--;
			}
			System.err.print("ID: "+node.getID()+"\t Vecino: "+ ((Linkable) node.getProtocol(0)).getNeighbor(0).getID()+"\t");
			System.err.print("DHT: ");
			for(int j = 0; j < node.getDHT().length;j++){
				if(node.getDHT()[j] > -1) System.err.print(node.getDHT()[j]+" ");
			}
			System.err.print("\tBD: ");
			for(int j = 0; j < node.getBD().length;j++){
				System.err.print(node.getBD()[j]+" ");
			}
			System.err.print("\tCache: ");

			for(int j = 0; j < node.getCache().length;j++){
				if(node.getCache()[j][0] > -1) System.err.print("[NODE: "+node.getCache()[j][0]+" DATA_ID: "+node.getCache()[j][1]+" DATA: "+node.getCache()[j][2]+"] ");
				else System.err.print("[]");
			}
			
			System.err.println();
			
		}

		String s = String.format("[time=%d]:[with N=%d nodes] [%d Total send message]", CommonState.getTime(), size,
				(int) message.getSum());

		System.err.println(s);

		return false;
	}

}
