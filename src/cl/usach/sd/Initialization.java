package cl.usach.sd;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Linkable;
import peersim.core.Network;

public class Initialization implements Control {
	String prefix;

	int idLayer;
	int idTransport;
	int d;
	int c;
	int BDSize;
	int argExample;

	public Initialization(String prefix) {
		this.prefix = prefix;
		/**
		 * Para obtener valores que deseamos como argumento del archivo de
		 * configuración, podemos colocar el prefijo de la inicialización en
		 * este caso "init.1statebuilder" y luego la variable de entrada
		 */
		// Configuration.getPid retornar al número de la capa
		// que corresponden esa parte del protocolo
		this.idLayer = Configuration.getPid(prefix + ".protocol");
		this.idTransport = Configuration.getPid(prefix + ".transport");
		System.out.println(prefix+".BD");
		// Configuration.getInt retorna el número del argumento
		// que se encuentra en el archivo de configuración.
		// También hay Configuration.getBoolean, .getString, etc...
		this.d = Configuration.getInt(prefix + ".d");
		this.c = Configuration.getInt(prefix + ".c");
		this.BDSize = d*Network.size();
		System.out.println("D: "+d);
		System.out.println("C: "+c);
		//System.out.println("Arg: " + argExample);
	}

	/**
	 * Ejecución de la inicialización en el momento de crear el overlay en el
	 * sistema
	 */
	@Override
	public boolean execute() {
		/**
		 * Tira un número random el cual corresponderá a un Nodo de la red
		 */

		/**
		 * Asignar un valor al atributo del peer (o nodo) de la red
		 */
		System.out.println("INICIALIZACION");
		int size = Network.size();
		for(int i = 0; i < size; i++){
			Node node = (Node) Network.get(i);
			long id = node.getID();
			((Linkable) node.getProtocol(0)).addNeighbor(Network.get((int)(id+1)%size));
		}
		for(int i = 0; i < size; i++){
			Node node = (Node) Network.get(i);
			node.setDHT(DHTInicialization(node,1+2*d));
			node.setCache(CacheInicialization());
			node.setBD(BDInicialization());
		}	
		BDPopulate();

		for(int i = 0; i < size; i++){
			Node node = (Node) Network.get(i);
			System.out.println("Soy el nodo "+ node.getID()+" y tengo de vecino a "+ Network.get((int)((Linkable) node.getProtocol(0)).getNeighbor(0).getID()).getID());
			System.out.println("\tTengo "+((Linkable) node.getProtocol(0)).degree()+" vecinos");
			System.out.println("\tMi tabla hash corresponde a:");
			for(int j = 0; j < node.getDHT().length;j++){
				if(node.getDHT()[j] >= 0) System.out.println("\t\tNodo DHT: "+node.getDHT()[j]);
			}
			for(int j = 0; j < node.getBD().length;j++){
				if(node.getDHT()[j] >= 0) System.out.println("\t\tBD: "+node.getBD()[j]);
			}
		}
		
		return true;
	}
	
	private void BDPopulate(){
		for(int i = 0; i < this.BDSize; i++){
			Node node = (Node) Network.get(i%Network.size());
			int bdIndex = new Double(i/Network.size()).intValue();
			node.getBD()[bdIndex] = i;
		}
	}
	private int[] BDInicialization(){
		int[] arr = new int[this.d];
		return arr;
	}
	private int[][] CacheInicialization(){
		return new int[this.c][3];
	}
	private int[] DHTInicialization(Node node, int DHTSize){
		int[] _dht = new int[DHTSize];
		for(int i = 0; i < DHTSize; i++){
			_dht[i] = -1;
		}
		int x = 1;
		while(Math.pow(2,x)<=Network.size()){
			int dis = new Double(Network.size()/Math.pow(2,x)).intValue();
			_dht[x-1] = ((int)node.getID()+dis)%Network.size(); 
			x++;
		}
		
		return _dht;
	}
}
