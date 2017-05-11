package cl.usach.sd;

/**
 * Clase la cual vamos a utilizar para enviar datos de un Peer a otro
 */
public class Message {
	private String text;
	private int destination;

	public Message(String text, int destination) {
		this.setText(text);
		this.setDestination(destination);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getDestination() {
		return destination;
	}

	public void setDestination(int destination) {
		this.destination = destination;
	}
}
