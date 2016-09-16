package mase.agents;

import java.awt.Color;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import mase.GUI.GUI;
import mase.main.Main;

public class TrackerAgent extends Agent {

	private static final long serialVersionUID = 1L;
	private int id;
	private ArrayList<Point> initialSpaces;

	public TrackerAgent(int id, ArrayList<Point> initialSpaces) {
		this.id = id;
		this.initialSpaces = initialSpaces;
		orderInitialSpacesByDistance();
	}

	public void setup() {
		Main.agentsAddresses[id] = getAID();
		if (Main.debug) {
			System.out.println(getLocalName() + ": Started. Received " + initialSpaces.size() + " positions.");
		}
		if (Main.choosenStrategy == Main.DIJKSTRA_STRATEGY) {
			this.addBehaviour(new DijkstraPathFind());
		} else {
			this.addBehaviour(new AStarPathFind());

		}
	}

	private class DijkstraPathFind extends CyclicBehaviour {

		private static final long serialVersionUID = 1L;
		private boolean[][] visited;
		private Long[][] sum;
		private Point[][] parent;
		private Point initialSpace;
		private ArrayList<Point> actualSpaces;
		private ArrayList<Point> adjacentSpaces;
		private int height;
		private int width;

		private ArrayList<Point> pathFound;
		private long minimumCost = Long.MAX_VALUE;

		public DijkstraPathFind() {
			height = Main.getWeightedGraph().length;
			width = Main.getWeightedGraph()[0].length;
			parent = new Point[height][width];
		}

		public void action() {
			if (initialSpaces.size() == 0) {
				if (Main.debug) {
					System.out.println(getLocalName() + ": sending proposal to manager. My proposal is " + minimumCost);
				}
				ACLMessage m = new ACLMessage(ACLMessage.PROPOSE);
				m.addReceiver(Main.GRIDManagerAddress);
				m.setContent(minimumCost + "");
				myAgent.send(m);
				myAgent.doWait();

				m = myAgent.receive();
				if (m.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
					if (Main.debug) {
						System.out.println(getLocalName() + ": My proposal won!");
					}
					m = new ACLMessage(ACLMessage.INFORM);
					m.addReceiver(Main.GRIDManagerAddress);
					try {
						m.setContentObject(pathFound);
						myAgent.send(m);
					} catch (IOException e) {
						e.printStackTrace();
					}

				}

				myAgent.doDelete();

			} else {
				initialSpace = initialSpaces.remove(0);

				sum = new Long[height][width];
				for (int i = 0; i < height; i++) {
					for (int j = 0; j < width; j++) {
						sum[i][j] = Long.MAX_VALUE;
						GUI.auxiliaryColors[i][j] = new Color(255, 255, 255);
					}
				}
				Main.gui.repaint2();
				sum[initialSpace.x][initialSpace.y] = (long) Main.getWeightedGraph()[initialSpace.x][initialSpace.y]
						.getWeight();

				visited = new boolean[height][width];

				actualSpaces = new ArrayList<Point>();
				adjacentSpaces = new ArrayList<Point>();

				actualSpaces.add(initialSpace);
				do {
					for (Point actualSpace : actualSpaces) {

						if (visited[actualSpace.x][actualSpace.y]) {
							continue;
						}
						GUI.auxiliaryColors[actualSpace.x][actualSpace.y] = new Color(1f, 0.4f, 0.3f, 0.5f);
						for (int i = -1; i <= 1; i++) {
							for (int j = -1; j <= 1; j++) {
								if (i == 0 && j == 0)
									continue;
								int nextX = (int) (actualSpace.x + i);
								if (nextX < 0 || nextX >= height)
									break;

								int nextY = (int) (actualSpace.y + j);
								if (nextY < 0 || nextY >= width)
									continue;

								if (visited[nextX][nextY])
									continue;

								long tentative = Main.getWeightedGraph()[nextX][nextY].getWeight()
										+ sum[actualSpace.x][actualSpace.y];

								if (tentative < sum[nextX][nextY]) {
									sum[nextX][nextY] = tentative;
									parent[nextX][nextY] = actualSpace;
									adjacentSpaces.add(new Point(nextX, nextY));
									GUI.auxiliaryColors[nextX][nextY] = new Color((float) 0, (float) 0, (float) 0,
											(float) 0.5);
								}

							}
						}
						visited[actualSpace.x][actualSpace.y] = true;
						Main.gui.repaint2();
						//myAgent.doWait(3);
					}
					actualSpaces = new ArrayList<Point>();
					actualSpaces.addAll(adjacentSpaces);
					adjacentSpaces = new ArrayList<Point>();
				} while (actualSpaces.size() != 0);
				retrievePath();
			}
		}

		public void retrievePath() {

			long currentMinimumSum = Long.MAX_VALUE;
			Point choosenFinalSpace = null;

			for (Point finalSpace : Main.getFinalSpaces()) {
				if (currentMinimumSum > sum[finalSpace.x][finalSpace.y]) {
					currentMinimumSum = sum[finalSpace.x][finalSpace.y];
					choosenFinalSpace = finalSpace;
				}
			}

			if (minimumCost < currentMinimumSum) {

				return;
			} else {
				minimumCost = currentMinimumSum;
				pathFound = new ArrayList<Point>();
				Point actualPoint = choosenFinalSpace;
				do {
					pathFound.add(actualPoint);
					actualPoint = parent[actualPoint.x][actualPoint.y];
					GUI.auxiliaryColors[actualPoint.x][actualPoint.y] = new Color(0, 0, 0);
					Main.gui.repaint2();
				} while (actualPoint != initialSpace);
				pathFound.add(actualPoint);
			}
		}

	}

	public void orderInitialSpacesByDistance() {
		ArrayList<Double> distances = new ArrayList<Double>();

		for (int j = 0; j < initialSpaces.size(); j++) {
			double distance = Double.MAX_VALUE;
			for (int i = 0; i < Main.getFinalSpaces().size(); i++) {
				double tentative = Math.sqrt(Math.pow((initialSpaces.get(j).x - Main.getFinalSpaces().get(i).x), 2)
						+ Math.pow((initialSpaces.get(j).y - Main.getFinalSpaces().get(i).y), 2));
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

	private class AStarPathFind extends CyclicBehaviour {

		private static final long serialVersionUID = 1L;
		private boolean[][] visited;
		private Double[][] sum;
		private Point[][] parent;
		private Point initialSpace;
		private Point temporaryFinalSpace;
		private Point finalSpace;
		private double temporaryLongestDistance;
		private ArrayList<Point> actualSpaces;
		private ArrayList<Point> adjacentSpaces;
		private int height;
		private int width;

		private ArrayList<Point> pathFound;
		private double minimumCost = Double.MAX_VALUE;

		public AStarPathFind() {
			height = Main.getWeightedGraph().length;
			width = Main.getWeightedGraph()[0].length;
			parent = new Point[height][width];
		}

		public void action() {
			if (initialSpaces.size() == 0) {
				if (Main.debug) {
					System.out.println(getLocalName() + ": sending proposal to manager. My proposal is " + minimumCost);
				}
				ACLMessage m = new ACLMessage(ACLMessage.PROPOSE);
				m.addReceiver(Main.GRIDManagerAddress);
				m.setContent(minimumCost + "");
				myAgent.send(m);
				myAgent.doWait();

				m = myAgent.receive();
				if (m.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
					if (Main.debug) {
						System.out.println(getLocalName() + ": My proposal won!");
					}
					m = new ACLMessage(ACLMessage.INFORM);
					m.addReceiver(Main.GRIDManagerAddress);
					try {
						m.setContentObject(pathFound);
						myAgent.send(m);
					} catch (IOException e) {
						e.printStackTrace();
					}

				}

				myAgent.doDelete();

			} else {
				initialSpace = initialSpaces.remove(0);

				temporaryLongestDistance = Long.MAX_VALUE;
				for (Point x : Main.getFinalSpaces()) {
					double temp = euclideanDistance(x, initialSpace);
					if (temp < temporaryLongestDistance) {
						temporaryFinalSpace = x;
						temporaryLongestDistance = temp;
					}
				}

				sum = new Double[height][width];
				for (int i = 0; i < height; i++) {
					for (int j = 0; j < width; j++) {
						sum[i][j] = Double.MAX_VALUE;
					}
				}
				sum[initialSpace.x][initialSpace.y] = (double) Main.getWeightedGraph()[initialSpace.x][initialSpace.y]
						.getWeight();

				visited = new boolean[height][width];

				actualSpaces = new ArrayList<Point>();
				adjacentSpaces = new ArrayList<Point>();
				ArrayList<Point> edges = new ArrayList<Point>();
				actualSpaces.add(initialSpace);
				boolean found = false;
				do {
					for (Point actualSpace : actualSpaces) {
						if (visited[actualSpace.x][actualSpace.y]) {
							continue;
						}
						GUI.auxiliaryColors[actualSpace.x][actualSpace.y] = new Color(1f, 0.4f, 0.3f, 0.5f);
						if (Main.getFinalSpaces().contains(actualSpace)) {
							found = true;
							finalSpace = actualSpace;
							break;
						}
						ArrayList<Point> neighbours = new ArrayList<Point>();
						ArrayList<Double> neighboursCosts = new ArrayList<Double>();
						for (int i = -1; i <= 1; i++) {
							for (int j = -1; j <= 1; j++) {
								if (i == 0 && j == 0)
									continue;
								int nextX = (int) (actualSpace.x + i);
								if (nextX < 0 || nextX >= height)
									break;

								int nextY = (int) (actualSpace.y + j);
								if (nextY < 0 || nextY >= width)
									continue;

								if (visited[nextX][nextY])
									continue;

								double tentative = Main.getWeightedGraph()[nextX][nextY].getWeight()
										+ sum[actualSpace.x][actualSpace.y];
								double heuristic = euclideanDistance(new Point(nextX, nextY), temporaryFinalSpace);
								neighbours.add(new Point(nextX, nextY));
								neighboursCosts.add(tentative + heuristic);
							}
						}
						orderArrayListByIndex(neighboursCosts, neighbours);
						if (neighbours.size() > 0) {
							double minimum = neighboursCosts.get(0);
							int count = 0;
							while (count < neighboursCosts.size()) {
								if (neighboursCosts.get(count) == minimum) {
									sum[neighbours.get(count).x][neighbours.get(count).y] = neighboursCosts.get(count);
									parent[neighbours.get(count).x][neighbours.get(count).y] = actualSpace;
									adjacentSpaces.add(neighbours.get(count));
									GUI.auxiliaryColors[neighbours.get(count).x][neighbours.get(count).y] = new Color(
											(float) 0, (float) 0, (float) 0, (float) 0.5);
								} else {
									edges.add(neighbours.get(count));
								}
								count++;
							}
						}

						visited[actualSpace.x][actualSpace.y] = true;
						Main.gui.repaint2();
						//myAgent.doWait(3);
					}
					if (found) {
						// TODO retrieve path
						System.out.println("found");
						retrievePath();
						
					}
					if (adjacentSpaces.size() == 0) {
						/*
						 * for (Point p : actualSpaces) {
						 * visited[parent[p.x][p.y].x][parent[p.x][p.y].y] =
						 * false; adjacentSpaces.add(parent[p.x][p.y]); }
						 */
						adjacentSpaces.addAll(edges);
						edges = new ArrayList<Point>();
					}
					actualSpaces = new ArrayList<Point>();
					actualSpaces.addAll(adjacentSpaces);
					adjacentSpaces = new ArrayList<Point>();

				} while (!found);
				retrievePath();
			}
		}

		public double euclideanDistance(Point a, Point b) {
			return Math.sqrt((Math.pow(a.x, 2) + Math.pow(b.x, 2)) + (Math.pow(a.y, 2) + Math.pow(b.y, 2)));
		}

		public double diagonalDistance(Point a, Point b) {
			double dx = Math.abs(a.x - b.x);
			double dy = Math.abs(a.y - b.y);
			double D = 1;
			double D2 = Math.sqrt(2);
			return D * (dx + dy) + (D2 - 2 * D) * Math.min(dx, dy);
		}

		public void retrievePath() {

			double currentMinimumSum = Long.MAX_VALUE;
			Point choosenFinalSpace = null;

			for (Point finalSpace : Main.getFinalSpaces()) {
				if (currentMinimumSum > sum[finalSpace.x][finalSpace.y]) {
					currentMinimumSum = sum[finalSpace.x][finalSpace.y];
					choosenFinalSpace = finalSpace;
				}
			}

			if (minimumCost < currentMinimumSum) {
				return;
			} else {
				minimumCost = currentMinimumSum;
				pathFound = new ArrayList<Point>();
				Point actualPoint = choosenFinalSpace;
				do {
					pathFound.add(actualPoint);
					actualPoint = parent[actualPoint.x][actualPoint.y];
					GUI.auxiliaryColors[actualPoint.x][actualPoint.y] = new Color(0, 0, 0);
					Main.gui.repaint2();
				} while (actualPoint != initialSpace);
				pathFound.add(actualPoint);
			}
		}
	}
}
