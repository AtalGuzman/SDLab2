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
		boolean answer = false;
		for(int i = 0; i< this.Cache.length; i++){
			if(node == this.Cache[i][0] && dataId == this.Cache[i][0]){
				answer = true;
			}
		}
		return answer;
	}
	
	public void cacheUpdate(int nodeId,int query,int data){
		System.out.println(nodeId+" "+query+" "+data);
		if(this.Cache[this.Cache.length-1][0] != -1){
			//Está lleno, se elimina el elemento más antiguo
			int i = 0;
			for(i = 0; i< this.Cache.length-1;i++){
				this.Cache[i][0] = this.Cache[i+1][0];
				this.Cache[i][1] = this.Cache[i+1][1];
				this.Cache[i][2] = this.Cache[i+1][2]; //Todo se corre un espació
			}
			this.Cache[i][0] = nodeId;
			this.Cache[i][1] = query;
			this.Cache[i][2] = data;		
		}
		else{
			int index = 0;
			while(this.Cache[index][0]>=0) index++; //Mientras una posición de lcaché esté ocupado, se corre una posición
			this.Cache[index][0] = nodeId;
			this.Cache[index][1] = query;
			this.Cache[index][2] = data;			
		}
	}
	
	public void cacheShow(){
		for(int i  = 0; i < this.Cache.length;i++){
			if(this.Cache[i][0] > 0) System.out.println(this.Cache[i][0]+" "+this.Cache[i][1]+" "+this.Cache[i][0]);
		}
	}
}
