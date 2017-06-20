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
	
	//Cach� del nodo
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
	
	/* M�todo para revisar si un consulta est� en cach� o no
	 * Recibe como entrada:
	 * 		node: La id del nodo al que se le hace la consulta
	 * 		dataId: La id del dato en la base de datos que se est� haciendo
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
	
	/* M�todo para retornar los valor en la cach�
	 * Recibo como entrada:
	 * 		node: La id del nodo al que se le hace la consulta
	 * 		dataId: La id del dato en la base de datos que se est� haciendo
	 * Retorna: el dato en cach� que hizo el hit
	 * */
	public int[] cacheHit(int node, int dataId){
		int [] answer = new int[3];
		for(int i = 0; i< this.Cache.length; i++){
			if(node == this.Cache[i][0] && dataId == this.Cache[i][1]){
				answer[0] = this.Cache[i][0];
				answer[1] = this.Cache[i][1];
				answer[2] = this.Cache[i][2];
			}
		}
		return answer;
	}

	/* M�todo para actualizar la cach� cuando el mensaje realiza
	 * el camino inverso como una respuesta.
	 * En caso de que el cach� est� lleno se hace una pol�tica FIFO
	 * Recibe como entrada:
	 * 		nodeId: id del nodo que ten�a originalmente el dato
	 * 		query: id del dato en la base de datos del nodo que lo posee
	 * 		data: informaci�n guardada en la base de datos
	 * */
	public void cacheUpdate(int nodeId,int query,int data){
		if(this.Cache[this.Cache.length-1][0] != -1){
			System.out.println("\tFULL CACHE-FIFO");
			System.out.println("\tCache deprecated");
			this.cacheShow();
			int i = 0;
			for(i = 0; i< this.Cache.length-1;i++){
				this.Cache[i][0] = this.Cache[i+1][0];
				this.Cache[i][1] = this.Cache[i+1][1];
				this.Cache[i][2] = this.Cache[i+1][2]; 
			}
			this.Cache[i][0] = nodeId;
			this.Cache[i][1] = query;
			this.Cache[i][2] = data;		
		}
		else{
			int index = 0;
			while(this.Cache[index][0]>=0) index++; 
			this.Cache[index][0] = nodeId;
			this.Cache[index][1] = query;
			this.Cache[index][2] = data;			
		}
	}
	
	/* M�todo para mostrar el contenido de la cach� del nodo
	 * */
	public void cacheShow(){
		if(this.Cache[0][0] != -1){
			System.out.println("\t\tNODE\tDATA_ID\tDATA");
			for(int i  = 0; i < this.Cache.length;i++){
				if(this.Cache[i][0] >=0) System.out.println("\t\t"+this.Cache[i][0]+"\t"+this.Cache[i][1]+"\t"+this.Cache[i][2]);
			}
		} else{
			System.out.println("\tEmpty Cach�:");
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
