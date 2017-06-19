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
		int superpeer = 0;
		System.err.println("\nInformación sobre los Super-peer\n");
		for (int i = 0; i < Network.size(); i++) {
			SNode3 node = (SNode3) Network.get(i);
			if (!node.isUp()) {
				size--;
			}
			if(node.getSuper_peer() == 1){
				superpeer++;
				System.err.print("Super-peer ");
			System.err.print("IP: "+node.getID()+"\t Vecino: "+ ((Linkable) node.getProtocol(0)).getNeighbor(0).getID()+"\tPuerto: "+node.getPort()+"\t");
			System.err.print("DHT: ");
			
			for(int j = 0; j < node.getDHT().length;j++){
				System.err.print(node.getDHT()[j][1].substring(0,1)+" ");
			}
			System.err.print("\tCache: ");

			for(int j = 0; j < node.getCache().length;j++){
				if(node.getCache()[j][0] > -1) System.err.print("[NODE: "+node.getCache()[j][0]+" DATA_ID: "+node.getCache()[j][1]+" DATA: "+node.getCache()[j][2]+"] ");
				else System.err.print("[]");
			}
			
			System.err.print("\tSub-Net: ");
			for(int j = 0; j < node.getSubNet().length;j++){
				System.err.print(node.getSubNet()[j]+" ");
			}
			
			System.err.println();
			}	
		}
		
		System.err.println("\nInformación sobre sub red");
		
		for (int i = 0; i < Network.size(); i++) {
			SNode3 node = (SNode3) Network.get(i);
			if(node.getSuper_peer()==1){
				System.err.print("\nSUB-RED "+node.getID());
				System.err.print("("+node.getSubNet().length+" peers + 1 Super-peer)");
				for(int j = 0; j < node.getSubNet().length;j++){
					System.err.println();
					SNode3 node2 = (SNode3) Network.get(node.getSubNet()[j]);
					System.err.print("\tIP: "+node2.getID()+" ");
					System.err.print("\tNeighbours: ");
					int degree = ((Linkable) node2.getProtocol(0)).degree();
					for(int k = 0; k < degree;k++){
						System.err.print(((Linkable) node2.getProtocol(0)).getNeighbor(k).getID()+" ");
					}
					System.err.print("\t\tBD: ");
					for(int k = 0; k < node2.getBD().length;k++){
						System.err.print(node2.getBD()[k]+" ");
					}
				}
			System.err.println();
			}
		}
		int cantPeers =size-superpeer;
		System.err.println("\nEn la red hay: "+size+" nodos ("+cantPeers+" peers + "+superpeer+" Super-peers)\n");

		String s = String.format("[time=%d]:[with N=%d nodes] [%d Total send message]", CommonState.getTime(), size,
				(int) message.getSum());
		System.err.println(s);

		return false;
	}

}
