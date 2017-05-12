package cl.usach.sd;

import peersim.core.GeneralNode;
import java.util.Stack;

public class SNode extends GeneralNode {
	private int[] DHT;
	private int[][] Cache;
	private int[] BD;
	
	public SNode(String prefix) {
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
	
	public boolean cacheReview(int node, int dataId){
		int[] result = new int[]{-1,-1,-1};
		boolean answer = false;
		for(int i = 0; i< this.Cache.length; i++){
			if(node == this.Cache[i][0] && dataId == this.Cache[i][0]){
				answer = true;
			}
		}
		return answer;
	}
	
}
