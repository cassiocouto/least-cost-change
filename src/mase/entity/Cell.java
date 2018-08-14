package mase.entity;

import java.awt.Point;

public class Cell {
	private int weight;
	private Point position;
	private int discoverer = -1;
	
	public Cell(int weight, Point position){
		this.weight = weight;
		this.position = position;
	}

	public int getWeight() {
		return weight;
	}

	public Point getPosition() {
		return position;
	}
	
	public void setDiscoverer(int agentid) {
		if(discoverer == -1) {
			discoverer = agentid;
		}
	}
	
	public int getDiscoverer() {
		return discoverer;
	}
}
