package mase.agents;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;

import jade.core.Agent;
import mase.main.Main;
import mase.behaviours.*;

public class TrackerAgent extends Agent {

	private static final long serialVersionUID = 1L;
	private int id;
	private ArrayList<Point> initialSpaces;
	private ArrayList<Point> finalSpaces;

	private static int messageCounter = 0;

	public TrackerAgent(int id, ArrayList<Point> initialSpaces, ArrayList<Point> finalSpaces) {
		this.id = id;
		this.initialSpaces = initialSpaces;
		this.finalSpaces = finalSpaces;

	}

	public void setup() {
		Main.getInstance().getAgentsAddresses()[id] = getAID();
		if (Main.getInstance().isDebug()) {
			System.out.println(getLocalName() + ": Started. Received " + initialSpaces.size() + " positions.");
		}
		if (Main.getInstance().getChoosenStrategy() == Main.DIJKSTRA_STRATEGY) {
			this.addBehaviour(new DijkstraPathFind(initialSpaces, finalSpaces, Main.getInstance().getHeight(),
					Main.getInstance().getWidth()));
		} else if (Main.getInstance().getChoosenStrategy() == Main.ASTAR_STRATEGY) {
			orderInitialSpacesByDistance();
			this.addBehaviour(new AStarPathFind(initialSpaces, finalSpaces, Main.getInstance().getHeight(),
					Main.getInstance().getWidth()));

		} else if (Main.getInstance().getChoosenStrategy() == Main.COOPERATIVE_ASTAR_STRATEGY) {
			orderInitialSpacesByDistance();
			this.addBehaviour(new CooperativeAStarPathFind(initialSpaces, finalSpaces, Main.getInstance().getHeight(),
					Main.getInstance().getWidth()));
		}
	}

	public void orderInitialSpacesByDistance() {
		ArrayList<Double> distances = new ArrayList<Double>();

		for (int j = 0; j < initialSpaces.size(); j++) {
			double distance = Double.MAX_VALUE;
			for (int i = 0; i < finalSpaces.size(); i++) {
				double tentative = Math.sqrt(Math.pow((initialSpaces.get(j).x - finalSpaces.get(i).x), 2)
						+ Math.pow((initialSpaces.get(j).y - finalSpaces.get(i).y), 2));
				if (tentative < distance) {
					distance = tentative;
				}
			}
			distances.add(distance);
		}

		orderArrayListByIndex(distances, initialSpaces);
	}

	public void orderArrayListByIndex(ArrayList<? extends Number> index, ArrayList<Point> values) {

		for (int i = 1; i < index.size(); i++) {
			int j = i;
			while (j > 0 && index.get(j - 1).doubleValue() > index.get(j).doubleValue()) {
				Collections.swap(index, j - 1, j);
				Collections.swap(values, j - 1, j);
				j--;
			}
		}
	}

	public void orderArrayListByIndexWithAuxArray(ArrayList<? extends Number> index, ArrayList<Point> values,
			ArrayList<? extends Number> aux) {

		for (int i = 1; i < index.size(); i++) {
			int j = i;
			while (j > 0 && index.get(j - 1).doubleValue() > index.get(j).doubleValue()) {
				Collections.swap(index, j - 1, j);
				Collections.swap(values, j - 1, j);
				Collections.swap(aux, j - 1, j);
				j--;
			}
		}
	}

	public static synchronized void printAccountedMessage(String msg) {
		System.out.println(String.format("%d - %s", messageCounter++, msg));
	}

}
