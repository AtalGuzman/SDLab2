package cl.usach.sd;

import peersim.core.GeneralNode;

public class Node extends GeneralNode {
	private int[] DHT;
	private int[][] Cache;
	private int[] BD;
	
	public Node(String prefix) {
		super(prefix);
	}
	public int[] getDHT() {
		return DHT;
	}
	public void setDHT(int[] dHT) {
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
}
