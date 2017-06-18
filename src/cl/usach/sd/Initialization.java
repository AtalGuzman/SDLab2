package cl.usach.sd;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Linkable;
import peersim.core.Network;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Stack;

public class Initialization implements Control {
	String prefix;

	int idLayer;
	int idTransport;
	
	int CANT_DE_SUPERPEER;
	int CANT_MIN_PEER;
	int CANT_MAX_PEER;
	int CACHE_SIZE;
	int BD_SIZE;
	int TTL;
	int CANT_RANDOM_WALKS; 

	public Initialization(String prefix) {
		this.prefix = prefix;

		this.idLayer = Configuration.getPid(prefix + ".protocol");
		this.idTransport = Configuration.getPid(prefix + ".transport");

		System.err.println("NET PARAMETERS");
		this.CANT_DE_SUPERPEER = Configuration.getInt(prefix + ".s");
		this.CANT_MIN_PEER = Configuration.getInt(prefix + ".n");
		this.CANT_MAX_PEER = Configuration.getInt(prefix+".m");
		this.CACHE_SIZE = Configuration.getInt(prefix+".c");
		this.BD_SIZE = Configuration.getInt(prefix+".b");
		this.TTL = Configuration.getInt(prefix+".ttl");
		this.CANT_RANDOM_WALKS = Configuration.getInt(prefix+".k");
		
		System.err.println("\tNetwork size: "+Network.size());
		System.err.println("\tCANTIDAD DE SUPER-PEER: "+this.CANT_DE_SUPERPEER);
		System.err.println("\tMINIMA CANTIDAD DE PEER POR SUB_RED: "+this.CANT_MIN_PEER);
		System.err.println("\tMAXIMA CANTIDAD DE PEER POR SUB_RED: "+this.CANT_MAX_PEER);
		System.err.println("\tTAMANO DE CACHE DE SUPER PEER: "+this.CACHE_SIZE);
		System.err.println("\tTAMANO DE BD DE PEER: "+this.BD_SIZE);
		System.err.println("\tTTL de K-random walks: "+this.TTL);
		System.err.println("\tCANTIDAD DE WALKERS :"+this.CANT_RANDOM_WALKS);
		System.err.println("\tCACHE LRU");
	}

	/* Método para ejecutar la inicialización de los
	 * nodos para la red
	 */
	@Override
	public boolean execute() {
		System.err.println("\nINICIALIZATION");
		int size = Network.size();
		int port = this.generatePort();
		//int DHTSize = 1+2*d;
		
		//Dado que los Super-peer no tienen vecinos inicialmente
		//se agregan para asegurar que queden agregados 
		//en una geometría circular
		for(int i = 0; i < size; i++){
			SNode3 node = (SNode3) Network.get(i);
			long id = node.getID();
			((Linkable) node.getProtocol(0)).addNeighbor(Network.get((int)(id+1)%size));
		}
		
		//Se inicializan los datos de cada nodo: la dht, la cache vacía
		for(int i = 0; i < size; i++){
			SNode3 node = (SNode3) Network.get(i);
			node.setSuper_peer(1);
			node.setCache(CacheInicialization());
			node.setBD(BDInicialization());
			node.setPort(port);
			try {
				node.setHash(this.generateHash( (int) node.getID(), node.getPort()));
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
		
		
		//Se pueblan todas las base de datos
		//BDPopulate();

		//Se muestran por pantalla los datos de la red
		for(int i = 0; i < size; i++){
			SNode3 node = (SNode3) Network.get(i);
			this.DHTInicialization(node);
			System.out.println("\nNode "+ node.getID());
			System.out.println("\tHash:"+ node.getHash());
			System.out.println("\tNeighbour Super-peer ID: "+ Network.get((int)((Linkable) node.getProtocol(0)).getNeighbor(0).getID()).getID());
			System.out.println("\tSub-Net Nodes:");
			this.generateSubRed(node);
			node.showSubRed();
			node.showDht();
			node.cacheShow();
		}
		return true;
	}
	
	/* Método para poblar las bases de datos
	 * de cada uno de los nodos, asegurando que los
	 * datos no se van a repetir en cada nodo
	 * */
	private void BDPopulate(){
		for(int i = 0; i < this.BD_SIZE; i++){
			SNode3 node = (SNode3) Network.get(i%Network.size());
			int bdIndex = new Double(i/Network.size()).intValue();
			node.getBD()[bdIndex] = i;
		}
	}
	
	/* Método para inicializar una base de datos
	 * con el tamaño adecuado
	 * */
	private int[] BDInicialization(){
		int[] arr = new int[this.BD_SIZE];
		return arr;
	}
	
	/* Método para inicializar la caché
	 * exclusivamente con -1
	 * */
	private int[][] CacheInicialization(){
		int[][] cache =  new int[this.CACHE_SIZE][3];
		
		for(int i = 0; i < this.CACHE_SIZE; i++){
			for(int j = 0; j < 3; j++){
				cache[i][j] = -1;
			}
		}
		return cache;
	}
	
	/* Método para inicializar la tablas hash de cada Nodo
	 * Recibe como entrada:
	 * 		Node: El nodo que será inicializado
	 * En Chord el tamaño de la DHT es log(tamaño de red)
	 * Este valor corresponde a m, donde el tamño de la red es 2^m
	 * Retorna: Tabla DHT inicializada
	 * */
	private void DHTInicialization(SNode3 node){
		int m = (int) Math.floor(Math.log(Network.size())/Math.log(2));
		node.setDHT(new String[m][2]);
		//System.out.println("El tamaño del dht es "+m);
		for(int i = 0; i < m; i++){
			int id = (int)( node.getID()+ Math.pow(2, i))%Network.size();
			//System.out.println("La id a buscar corresponde a "+id);
			SNode3 dhtNode = (SNode3) Network.get(id);
			node.getDHT()[i][0] = Integer.toString((int)dhtNode.getID());
			//System.out.println("El hash del nodo es "+dhtNode.getHash());
			node.getDHT()[i][1] = dhtNode.getHash();			
		}
		return;
	}
	
	/*	Función aportada por Ignacio Ibañez, para la creación de Hash
	 * con el algoritmo SHA1
	 * Este recibe como entrada un string y retorna el código
	 * hash asociado a él.
	 * */
	private String sha1(String input) throws NoSuchAlgorithmException {
	    MessageDigest mDigest = MessageDigest.getInstance("SHA1");
	    byte[] result = mDigest.digest(input.getBytes());
	    StringBuffer sb = new StringBuffer();
	    for (int i = 0; i < result.length; i++) {
	        sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
	    }
	    return sb.toString();
	}
	
	private int generatePort(){
		int port = CommonState.r.nextInt(1000)+3000;
		return port;
	}

	private String generateHash(int id, int port) throws NoSuchAlgorithmException {
		String s= Integer.toString(id)+Integer.toString(port);
		return sha1(s);
	}
	
	private void generateSubRed(SNode3 node){
		int n = this.CANT_MIN_PEER;
		int m = this.CANT_MAX_PEER;
		int NetSize = CommonState.r.nextInt(m+1-n)+n;
		int cant_nodos = 0;
		
		//System.out.println("El tamaño de la red es "+NetSize);

		SNode3 node2 = (SNode3) node.clone();
		node2.setCache(null);
		node2.setDHT(null);
		node2.setPort(-1);
		node2.setSuper_peer(0);
		//System.out.println("La id del nodo creado es "+node2.getID());
		//System.out.println("El hash del nodo creado es "+node2.getHash());
		((Linkable) node.getProtocol(0)).addNeighbor(node2);
		
		cant_nodos++;
		
		while(cant_nodos < NetSize){
			SNode3 node3 = (SNode3) node2.clone();
			((Linkable) node.getProtocol(0)).addNeighbor(node3);
			cant_nodos++;
		}
	}
}
