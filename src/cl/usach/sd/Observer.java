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
		String observer = "";
		//Nodo utilizada para obtener los datos del la red, para formatear correctamente la salida
		SNode nodeSample = (SNode) Network.get(CommonState.r.nextInt(Network.size()));
		int d = nodeSample.getDHT().length%4;
		int c = nodeSample.getCache().length%4;
		int k = 1;
		String s1 = "\t";
		String s2 = "";
		String s3 = "";
		while(k<d){
			s1 = s1+"\t";
			k++;
		}
		k = 1;
		while(k<c){
			s2 = s2+"\t";
			k++;
		}
		k = 1;
		System.err.println("ID\tVecino\tDHT"+s1+"BD"+s2+"CACHE");
		for (int i = 0; i < Network.size(); i++) {
			s1 = "\t";
			s2 = "\t";
			SNode node = (SNode) Network.get(i);
			if (!node.isUp()) {
				size--;
			}
			System.err.print(node.getID()+"\t"+ ((Linkable) node.getProtocol(0)).getNeighbor(0).getID()+"\t");
			for(int j = 0; j < node.getDHT().length;j++){
				if(node.getDHT()[j] > -1) System.err.print(node.getDHT()[j]+" ");
			}
			System.err.print(" |");
			for(int j = 0; j < node.getBD().length;j++){
				System.err.print(node.getBD()[j]+" ");
			}
			System.err.print("|\t");

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
