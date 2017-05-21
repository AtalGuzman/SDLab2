package cl.usach.sd;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Linkable;
import peersim.core.Network;
import java.util.Stack;

public class Initialization implements Control {
	String prefix;

	int idLayer;
	int idTransport;
	int b;
	int c;
	int d;
	int BDSize;

	public Initialization(String prefix) {
		this.prefix = prefix;

		this.idLayer = Configuration.getPid(prefix + ".protocol");
		this.idTransport = Configuration.getPid(prefix + ".transport");

		System.err.println("NET PARAMETERS");
		//Se obtiene el parámetro de tamaño de base de datos
		this.b = Configuration.getInt(prefix + ".b");
		//Se obtiene el parámetro de tamaño de caché
		this.c = Configuration.getInt(prefix + ".c");
		this.d = Configuration.getInt(prefix+".d");
		
		this.BDSize = b*Network.size();
		System.err.println("\tNetwork size: "+Network.size());
		System.err.println("\tDB Size: "+b);
		System.err.println("\tCache Size: "+c);
		System.err.println("\tD parameter: "+d);
		System.err.println("\tCACHE FIFO");
	}

	/* Método para ejecutar la inicialización de los
	 * nodos para la red
	 */
	@Override
	public boolean execute() {
		System.err.println("\nINICIALIZATION");
		int size = Network.size();
		int DHTSize = 1+2*d;
		int x = 1;
		
		System.err.println("\n\tDHT Size: "+DHTSize);
		System.err.print("\tx values: ");
		
		while(Math.pow(2,x)<size){
			System.err.print(x+" ");
			x++;
		}
		System.err.println(" [2^x < n]");
		System.err.print("\tDistances: ");
		x = 1;
		while(Math.pow(2,x)<size){
			System.err.print(new Double(Network.size()/Math.pow(2,x)).intValue()+" ");
			x++;
		}
		System.err.println(" [n/2^x]\n");
		
		//Dado que los nodos no tienen vecinos inicialmente
		//se agregan para asegurar que queden agregados 
		//en una geometría circular
		for(int i = 0; i < size; i++){
			SNode node = (SNode) Network.get(i);
			long id = node.getID();
			((Linkable) node.getProtocol(0)).addNeighbor(Network.get((int)(id+1)%size));
		}
		
		//Se inicializan los datos de cada nodo: la dht, la cache vacía
		//y la base de datos
		
		
		for(int i = 0; i < size; i++){
			SNode node = (SNode) Network.get(i);
			node.setDHT(DHTInicialization(node,DHTSize));
			node.setCache(CacheInicialization());
			node.setBD(BDInicialization());
		}	
		
		
		//Se pueblan todas las base de datos
		BDPopulate();

		//Se muestran por pantalla los datos de la red
		/*for(int i = 0; i < size; i++){
			SNode node = (SNode) Network.get(i);
			System.out.println("Node "+ node.getID()+" Neighbour "+ Network.get((int)((Linkable) node.getProtocol(0)).getNeighbor(0).getID()).getID());
			System.out.println("\tDHT");
			for(int j = 0; j < node.getDHT().length;j++){
				if(node.getDHT()[j] >= 0) System.out.println("\t\tNode DHT: "+node.getDHT()[j]);
			}
			for(int j = 0; j < node.getBD().length;j++){
				if(node.getDHT()[j] >= 0) System.out.println("\t\tBD: "+node.getBD()[j]);
			}
			node.cacheShow();
		}*/
		return true;
	}
	
	/* Método para poblar las bases de datos
	 * de cada uno de los nodos, asegurando que los
	 * datos no se van a repetir en cada nodo
	 * */
	private void BDPopulate(){
		for(int i = 0; i < this.BDSize; i++){
			SNode node = (SNode) Network.get(i%Network.size());
			int bdIndex = new Double(i/Network.size()).intValue();
			node.getBD()[bdIndex] = i;
		}
	}
	
	/* Método para inicializar una base de datos
	 * con el tamaño adecuado
	 * */
	private int[] BDInicialization(){
		int[] arr = new int[this.b];
		return arr;
	}
	
	/* Método para inicializar la caché
	 * exclusivamente con -1
	 * */
	private int[][] CacheInicialization(){
		int[][] cache =  new int[this.c][3];
		
		for(int i = 0; i < this.c; i++){
			for(int j = 0; j < 3; j++){
				cache[i][j] = -1;
			}
		}
		return cache;
	}
	
	/* Método para inicializar la tablas hash de cada Nodo
	 * Recibe como entrada:
	 * 		Node: El nodo que será inicializado
	 * 		DHTSize: El tamaño de la tabla que se debe crear
	 * Retorna: Tabla DHT inicializada
	 * */
	private int[] DHTInicialization(SNode node, int DHTSize){
		int[] _dht = new int[DHTSize];
		int x = 1;

		for(int i = 0; i < DHTSize; i++){
			_dht[i] = -1;
		}
		while(Math.pow(2,x)< Network.size()){
			int dis = new Double(Network.size()/Math.pow(2,x)).intValue();
			_dht[x-1] = ((int)node.getID()+dis)%Network.size(); 
			x++;
		}
		return _dht;
	}
}
