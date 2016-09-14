package mase.agents;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
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
					}
				}
				sum[initialSpace.x][initialSpace.y] = (long) Main.getWeightedGraph()[initialSpace.x][initialSpace.y]
						.getWeight();

				visited = new boolean[height][width];

				actualSpaces = new ArrayList<Point>();
				adjacentSpaces = new ArrayList<Point>();

				actualSpaces.add(initialSpace);
				do {
					for (Point actualSpace : actualSpaces) {
						if (visited[actualSpace.x][actualSpace.y])
							continue;
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
								}

							}
						}
						visited[actualSpace.x][actualSpace.y] = true;
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
		private Long[][] sum;
		private ArrayList<Point> openSet;
		private Point[][] parent;
		private int height;
		private int width;
		private Point initialSpace;
		private Point finalSpace;
		private long minimumCost;
		private ArrayList<Point> foundPath;

		public AStarPathFind() {
			height = Main.getWeightedGraph().length;
			width = Main.getWeightedGraph()[0].length;
			sum = new Long[height][width];
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					sum[i][j] = Long.MAX_VALUE;
				}
			}
			parent = new Point[height][width];
		}

		public Point chooseFinalPoint() {
			Point finalPoint = null;
			long minimum = Long.MAX_VALUE;

			for (Point p : Main.getFinalSpaces()) {
				long tentative = manhattanDistance(p, initialSpace);
				if (minimum > tentative) {
					minimum = tentative;
					finalPoint = p;
				}
			}
			return finalPoint;
		}

		public void action() {
			if (initialSpaces.size() == 0) {

			} else {
				initialSpace = initialSpaces.remove(0);
				finalSpace = chooseFinalPoint();
				visited = new boolean[height][width];
				openSet = new ArrayList<Point>();
				openSet.add(initialSpace);
				sum[initialSpace.x][initialSpace.y] = (long) Main.getWeightedGraph()[initialSpace.x][initialSpace.y]
						.getWeight();

				boolean found = false;
				do {

					ArrayList<Point> next = new ArrayList<>();
					for (Point actualSpace : openSet) {
						ArrayList<Point> neighbours = new ArrayList<Point>();
						ArrayList<Long> costs = new ArrayList<Long>();
						if (Main.getFinalSpaces().contains(actualSpace)) {
							finalSpace = actualSpace;
							found = true;
							break;
						}
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

								long tentativeCost = sum[actualSpace.x][actualSpace.y].longValue()
										+ Main.getWeightedGraph()[nextX][nextY].getWeight();// +
																							// manhattanDistance(finalSpace,
																							// new
																							// Point(nextX,
																							// nextY));

								if (tentativeCost < sum[nextX][nextY]) {
									parent[nextX][nextY] = actualSpace;
									sum[nextX][nextY] = tentativeCost;
									neighbours.add(new Point(nextX, nextY));
									costs.add(tentativeCost);
								}

							}
						}
						orderArrayListByIndex(costs, neighbours);
						if (costs.size() > 0) {
							long minimum = costs.get(0);
							int count = 0;
							while (count < costs.size() && costs.get(count) == minimum) {
								if (!next.contains(neighbours.get(count))) {
									next.add(neighbours.get(count));
								}
								count++;
							}
						}
						visited[actualSpace.x][actualSpace.y] = true;
					}
					openSet = new ArrayList<Point>();
					openSet.addAll(next);
				} while (openSet.size() > 0 && !found);
				if (found) {
					minimumCost =  sum[finalSpace.x][finalSpace.y];
					//TODO retrieve path
				} else {
					//TODO retake path
				}
			}
		}

		public int manhattanDistance(Point a, Point b) {
			int soma = a.x - b.x;
			if (soma < 0)
				soma = soma * -1;
			int soma2 = a.y - b.y;
			if (soma2 < 0)
				soma2 = soma2 * -1;
			return soma + soma2;
		}

	}
}
