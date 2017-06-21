package cl.usach.sd;
import java.util.Stack;

import peersim.config.Configuration;
/**
 * Clase la cual vamos a utilizar para enviar datos de un Peer a otro
 */
public class Message {
	//El mensaje posee un destion
	private int finalDestination;
	//La query a la base de datos del destino
	private int query;
	//El que envía el mensaje
	private int remitent;
	//Se inicializa el mensaje sin datos
	private int data = -1;
	
	private int superPeer = 0;
	
	private int ttl = Configuration.getInt("init.1statebuilder.ttl");
	
	//Se inicializa el camino del mensaje como un stack, para
	//que sea sencillo recorrerlo inversamente
	private Stack<Integer> path = new Stack<Integer>();
		
	//Se inicializa el camino del mensaje como un stack, para
	//que sea sencillo recorrerlo inversamente
	
	//Constructor del mensaje
	public Message(int destination, int query, int remitent, int superPeer) {
		this.setDestination(destination);
		this.setQuery(query);
		this.setRemitent(remitent);
		this.setSuperPeer(superPeer);
		
	}
	
	/**Setters y getters de cada uno de los atributos del mensaje**/
	public int getDestination() {
		return finalDestination;
	}

	public void setDestination(int destination) {
		this.finalDestination = destination;
	}

	public int getQuery() {
		return query;
	}

	public void setQuery(int query) {
		this.query = query;
	}

	public int getRemitent() {
		return remitent;
	}

	public void setRemitent(int remitent) {
		this.remitent = remitent;
	}

	public int getData() {
		return data;
	}

	public void setData(int data) {
		this.data = data;
	}
	
	public int getSuperPeer() {
		return superPeer;
	}

	public void setSuperPeer(int superPeer) {
		this.superPeer = superPeer;
	}

	public int getTtl() {
		return ttl;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	public Stack<Integer> getPath() {
		return path;
	}

	public void setPath(Stack<Integer> path) {
		this.path = path;
	}
	
}
