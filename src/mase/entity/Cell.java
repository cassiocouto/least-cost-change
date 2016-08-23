package mase.entity;

import java.awt.Point;

public class Cell {
	private int weight;
	private Point position;
	
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
}
