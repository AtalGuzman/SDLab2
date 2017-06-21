package cl.usach.sd;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Linkable;
import peersim.core.Network;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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
	SNode3 nodeClone;
	public Initialization(String prefix) {
		this.prefix = prefix;

		this.idLayer = Configuration.getPid(prefix + ".protocol");
		this.idTransport = Configuration.getPid(prefix + ".transport");

		System.err.println("NET PARAMETERS");
		this.CANT_DE_SUPERPEER = Network.size();
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
		int size = Network.size()-1;
		this.nodeClone = (SNode3) Network.get(size);
		this.nodeClone.setSuper_peer(-1); //Este nodo es muy especial, porque es utilizado para realizar los clones de los nodos a agregar a las sub red que tienen asociada cada uno de los super peer.
		
		System.err.println("NODO DE CLONACIÓN: "+nodeClone.getID());
		//int port = this.generatePort();
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
			node.setCache(this.CacheInicialization());
			node.setBD(this.BDInicialization());
			node.setPort(this.generatePort());
			node.setMiSubNet( (int) node.getID());
			node.setMsg(new ArrayList<String>());
			try {
				node.setHash(this.generateHash( (int) node.getID(), node.getPort()));
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	

		//Se muestran por pantalla los datos de la red
		for(int i = 0; i < size; i++){
			SNode3 node = (SNode3) Network.get(i);
			this.DHTInicialization(node);
		}
		
		//Se inicializa la sub red
		for(int i = 0; i < size; i++){
			SNode3 node = (SNode3) Network.get(i);
			if(node.getSuper_peer()==1){
				//System.out.println("Crearé una sub red para el Super peer "+node.getID());
				this.generateSubRed(node);
			}
			
		}
		//Se pueblan todas las base de datos
		BDPopulate();
		for(int i = 0; i < size; i++){
			SNode3 node = (SNode3) Network.get(i);
			System.out.println("\nSP ID: "+node.getID()+"\tIP: "+ node.getID()+"\tPort: "+node.getPort());
			System.out.println("\tHash: "+ node.getHash());
			System.out.println("\tNeighbour SP ID: "+ (int)((Linkable) node.getProtocol(0)).getNeighbor(0).getID());
			System.out.println("\tSub-Net Nodes:");
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
		int elementosBaseDeDatos  = this.BD_SIZE*Network.size();
		//System.out.println("Se agregarán "+elementosBaseDeDatos+" a la base de datos");
		for(int i = (int) this.nodeClone.getID(); i <elementosBaseDeDatos; i++){
			SNode3 node = (SNode3) Network.get(i%Network.size());
			if(node.getSuper_peer()==0){
				int bdIndex = new Double(i/Network.size()).intValue();
				node.getBD()[bdIndex] = i;
			}
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
	 * exclusivamente con -1 y con 0, para el conteo de LRU
	 * */
	private int[][] CacheInicialization(){
		int[][] cache =  new int[this.CACHE_SIZE][5];
		
		for(int i = 0; i < this.CACHE_SIZE; i++){
			for(int j = 0; j < 4; j++){
				cache[i][j] = -1;
			}
			cache[i][4] = 0;
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
			int id = (int)( node.getID()+ Math.pow(2, i))%(Network.size()-1);
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
		SNode3 node2 = (SNode3) this.nodeClone.clone();
		node2.setSuper_peer(0);
		node2.setBD(this.BDInicialization());
		node2.setMiSubNet((int)node.getID());
		Network.add(node2);
		((Linkable) node2.getProtocol(0)).addNeighbor(node);
		((Linkable) node.getProtocol(0)).addNeighbor(node2);

		int idPrimerNode = (int) node2.getID();
		int idVecino = 0;
		int agregado = 0;
		node.setSubNet(new int[NetSize]);
		node.getSubNet()[cant_nodos] = (int) node2.getID();
	
		cant_nodos++;
		//System.out.println("Se agregara el nodo"+node2.getID()+"al Super peer");
		while(cant_nodos < NetSize){
			SNode3 node3 = (SNode3) this.nodeClone.clone();
			int idNodeActual = (int) node3.getID();
			node3.setSuper_peer(0);
			agregado = CommonState.r.nextInt(100);
			Network.add(node3);
			node3.setBD(this.BDInicialization());
			node3.setMiSubNet((int)node.getID());
			node.getSubNet()[cant_nodos] = (int) node3.getID();
			//System.out.println("La probabilidad de agregado es"+agregado);
			if(agregado <30){
				//System.out.println("Se agregara el nodo"+node3.getID()+"al Super peer");
				((Linkable) node3.getProtocol(0)).addNeighbor(node);
				((Linkable) node.getProtocol(0)).addNeighbor(node3);
			} else{
				idVecino = CommonState.r.nextInt(idNodeActual-idPrimerNode)+idPrimerNode;
				//System.out.println("Se agregará el nodo "+node3.getID()+" como vecino al nodo "+idVecino);
				((Linkable) node3.getProtocol(0)).addNeighbor(Network.get(idVecino));
				((Linkable) Network.get(idVecino).getProtocol(0)).addNeighbor(node3);

			}
			cant_nodos++;
		}
	}
}
