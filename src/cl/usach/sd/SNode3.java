package cl.usach.sd;

import peersim.core.GeneralNode;
import peersim.core.Linkable;
import peersim.core.Network;

import java.util.Stack;

public class SNode3 extends GeneralNode {
	//Determina si en super-peer o no
	private int super_peer = 0;
	//Tabla dht del nodo
	private String[][] DHT;
	
	//Caché del nodo
	private int[][] Cache;
	
	//Base de datos
	private int[] BD;
	
	private int port;
	
	private String hash;
	
	private int[] subNet;
	
	private int miSubNet;
	
	public SNode3(String prefix) {
		super(prefix);
	}
	public String[][] getDHT() {
		return DHT;
	}
	public void setDHT(String[][] dHT) {
		DHT = dHT;
	}
	public int[][] getCache() {
		return Cache;
	}
	public void setCache(int[][] cache) {
		Cache = cache;
	}
	public int[] getBD() {
		return BD;
	}
	public void setBD(int[] bD) {
		BD = bD;
	}
	
	/* Método para revisar si un consulta está en caché o no
	 * Recibe como entrada:
	 * 		node: La id del nodo al que se le hace la consulta
	 * 		dataId: La id del dato en la base de datos que se está haciendo
	 * Retorna: un valor booleano si es verdadero, hubo un hit. Si es falso
	 * 		hubo un miss
	 */
	public boolean cacheReview(int node, int dataId){
		boolean answer = false;
		for(int i = 0; i< this.Cache.length; i++){
			if(node == this.Cache[i][0] && dataId == this.Cache[i][1]){
				answer = true;
			}
		}
		return answer;
	}
	
	/* Método para retornar los valor en la caché
	 * Recibo como entrada:
	 * 		node: La id del nodo al que se le hace la consulta
	 * 		dataId: La id del dato en la base de datos que se está haciendo
	 * Retorna: el dato en caché que hizo el hit
	 * */
	public int[] cacheHit(int node, int dataId){
		int [] answer = new int[3];
		for(int i = 0; i< this.Cache.length; i++){
			if(node == this.Cache[i][0] && dataId == this.Cache[i][1]){
				answer[0] = this.Cache[i][0];
				answer[1] = this.Cache[i][1];
				answer[2] = this.Cache[i][2];
				answer[3] = this.Cache[i][3];
				answer[4] = 0;
				this.Cache[i][4] = 0;
			}
			else{
				this.Cache[i][4]++;
			}
		}
		return answer;
	}

	/* Método para actualizar la caché cuando el mensaje realiza
	 * el camino inverso como una respuesta.
	 * En caso de que el caché esté lleno se hace una política LRU
	 * Por lo tanto, se van guardando los momentos de cada vez que se realiza una consulta
	 * en el quinto registro del caché
	 * Recibe como entrada:
	 * 		nodeId: id del nodo que tenía originalmente el dato
	 * 		superId: id del super-peer que maneja la sub-red
	 * 		query: id del dato en la base de datos del nodo que lo posee
	 * 		data: información guardada en la base de datos
	 * */
	public void cacheUpdate(int nodeId, int superId, int query,int data){
		if(this.Cache[this.Cache.length-1][0] != -1){
			System.out.println("\tFULL CACHE-LRU");
			System.out.println("\tCache deprecated");
			this.cacheShow();
			int index = 0;
			int max = this.Cache[0][4];
			//Se verifica cuál es el que no ha sido utilizado en la 
			//mayor cantidad de tiempo
			for(int i = 0; i< this.Cache.length-1;i++){
				if(max <this.Cache[i][4]){
					max = this.Cache[i][4];
					index = i;
				}
			}
			this.Cache[index][0] = nodeId;
			this.Cache[index][1] = superId;
			this.Cache[index][2] = query;	
			this.Cache[index][3] = data;
		}
		else{
			int index = 0;
			while(this.Cache[index][0]>=0) index++; 
			this.Cache[index][0] = nodeId;
			this.Cache[index][1] = superId;
			this.Cache[index][2] = query;	
			this.Cache[index][3] = data;
		}
	}
	
	/* Método para mostrar el contenido de la caché del nodo
	 * */
	public void cacheShow(){
		if(this.Cache[0][0] != -1){
			System.out.println("\t\tNODE\tDATA_ID\tDATA");
			for(int i  = 0; i < this.Cache.length;i++){
				if(this.Cache[i][0] >=0) System.out.println("\t\t"+this.Cache[i][0]+"\t"+this.Cache[i][1]+"\t"+this.Cache[i][2]);
			}
		} else{
			System.out.println("\tEmpty Caché:");
			System.out.println("\t\t--");
		}
	}
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public void showDht(){
		System.out.println("\tChord fingertable");
		for(int i = 0; i< this.DHT.length;i++){
			System.out.println("\t\tID: "+this.DHT[i][0]+" Hash: "+this.DHT[i][1]);
		}
	}
	public void showSubRed(){
		for(int j = 0; j < this.getSubNet().length; j++){
			System.out.println("\t\tID: "+this.getSubNet()[j]);		
		}
		return;
	}
	public int getSuper_peer() {
		return super_peer;
	}
	public void setSuper_peer(int super_peer) {
		this.super_peer = super_peer;
	}
	public int[] getSubNet() {
		return subNet;
	}
	public void setSubNet(int[] subNet) {
		this.subNet = subNet;
	}
	public int getMiSubNet() {
		return miSubNet;
	}
	public void setMiSubNet(int miSubNet) {
		this.miSubNet = miSubNet;
	}
}
