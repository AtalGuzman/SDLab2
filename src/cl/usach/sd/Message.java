package cl.usach.sd;
import java.util.Stack;
/**
 * Clase la cual vamos a utilizar para enviar datos de un Peer a otro
 */
public class Message {
	private int destination;
	private int query;
	private int remitent;
	private int data = -1;
	private Stack<Integer> path = new Stack<Integer>();
	
	public Message(int destination, int query, int remitent) {
		this.setDestination(destination);
		this.setQuery(query);
		this.setRemitent(remitent);
	}

	public int getDestination() {
		return destination;
	}

	public void setDestination(int destination) {
		this.destination = destination;
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

	public Stack<Integer> getPath() {
		return path;
	}

	public void setPath(Stack<Integer> path) {
		this.path = path;
	}
	
}
