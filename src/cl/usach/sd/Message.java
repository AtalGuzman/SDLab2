package cl.usach.sd;
import java.util.Stack;
/**
 * Clase la cual vamos a utilizar para enviar datos de un Peer a otro
 */
public class Message {
	//El mensaje posee un destion
	private int destination;
	//La query a la base de datos del destino
	private int query;
	//El que envía el mensaje
	private int remitent;
	//Se inicializa el mensaje sin datos
	private int data = -1;
	//Se inicializa el camino del mensaje como un stack, para
	//que sea sencillo recorrerlo inversamente
	private Stack<Integer> path = new Stack<Integer>();
	
	//Constructor del mensaje
	public Message(int destination, int query, int remitent) {
		this.setDestination(destination);
		this.setQuery(query);
		this.setRemitent(remitent);
	}
	
	/**Setters y getters de cada uno de los atributos del mensaje**/
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
