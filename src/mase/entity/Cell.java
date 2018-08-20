package mase.entity;

import java.awt.Point;

import jade.core.AID;

public class Cell {
	private int weight;
	private Point position;
	private AID trackerId = null;

	public Cell(int weight, Point position) {
		this.weight = weight;
		this.position = position;
	}

	public int getWeight() {
		return weight;
	}

	public Point getPosition() {
		return position;
	}

	public AID getTrackerId() {
		return trackerId;
	}

	public synchronized void setTrackerId(AID trackerId) {
		if (this.trackerId == null) {
			this.trackerId = trackerId;
		}
	}

}
