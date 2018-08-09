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
import mase.util.PriorityQueue;

public class TrackerAgent extends Agent {

	private static final long serialVersionUID = 1L;
	private int id;
	private ArrayList<Point> initialSpaces;
	private ArrayList<Point> finalSpaces;

	public TrackerAgent(int id, ArrayList<Point> initialSpaces, ArrayList<Point> finalSpaces) {
		this.id = id;
		this.initialSpaces = initialSpaces;
		this.finalSpaces = finalSpaces;
		orderInitialSpacesByDistance();

	}

	public void setup() {
		Main.getInstance().getAgentsAddresses()[id] = getAID();
		if (Main.getInstance().isDebug()) {
			System.out.println(getLocalName() + ": Started. Received " + initialSpaces.size() + " positions.");
		}
		if (Main.getInstance().getChoosenStrategy() == Main.DIJKSTRA_STRATEGY) {
			this.addBehaviour(new DijkstraPathFind());
		} else {
			// this.addBehaviour(new AStarPathFind());
			// this.addBehaviour(new AStarPathFind2(initialSpaces,
			// Main.getInstance().getWeightedGraph()[0].length,
			// Main.getInstance().getWeightedGraph().length));

		}
	}

	private class DijkstraPathFind extends CyclicBehaviour {

		private static final long serialVersionUID = 1L;
		private boolean[][] visited;
		private Long[][] sum;
		private Point[][] parent;
		private Point initialSpace;
		private PriorityQueue actualSpaces;
		private PriorityQueue adjacentSpaces;
		private int height;
		private int width;

		private ArrayList<Point> pathFound;
		private long minimumCost = Long.MAX_VALUE;

		public DijkstraPathFind() {
			height = Main.getInstance().getWeightedGraph().length;
			width = Main.getInstance().getWeightedGraph()[0].length;
			parent = new Point[height][width];
		}

		public void action() {
			if (initialSpaces.size() == 0) {
				if (Main.getInstance().isDebug()) {
					// System.out.println(getLocalName() + ": sending proposal
					// to manager. My proposal is " + minimumCost);
				}
				ACLMessage m = new ACLMessage(ACLMessage.PROPOSE);
				m.addReceiver(Main.getInstance().getGRIDManagerAddress());
				m.setContent(minimumCost + "");
				myAgent.send(m);
				myAgent.doWait();

				m = myAgent.receive();
				if (m.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
					if (Main.getInstance().isDebug()) {
						System.out.println(getLocalName() + ": My proposal won!");
					}
					m = new ACLMessage(ACLMessage.INFORM);
					m.addReceiver(Main.getInstance().getGRIDManagerAddress());
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
						// GUI.getInstance().getAuxiliaryColors()[i][j] = new
						// Color(255, 255, 255);
					}
				}
				if (Main.getInstance().getGui() != null) {
					Main.getInstance().getGui().repaint2();
				}
				sum[initialSpace.x][initialSpace.y] = (long) Main.getInstance()
						.getWeightedGraph()[initialSpace.x][initialSpace.y].getWeight();

				visited = new boolean[height][width];

				actualSpaces = new PriorityQueue();
				adjacentSpaces = new PriorityQueue();

				actualSpaces.add(0, initialSpace);
				do {
					for (int aux = 0; aux < actualSpaces.size(); aux++) {
						Point actualSpace = (Point) actualSpaces.get(aux);
						if (visited[actualSpace.x][actualSpace.y]) {
							continue;
						}

						for (int i = -1; i <= 1; i++) {
							for (int j = -1; j <= 1; j++) {

								int nextX = (int) (actualSpace.x + i);
								int nextY = (int) (actualSpace.y + j); // if (nextY < 0 || nextY >= width)continue;

								try {
									if (visited[nextX][nextY])
										continue;
									long tentative = Main.getInstance().getWeightedGraph()[nextX][nextY].getWeight()
											+ sum[actualSpace.x][actualSpace.y];
									if (tentative < sum[nextX][nextY]) {
										sum[nextX][nextY] = tentative;
										parent[nextX][nextY] = actualSpace;
										if (!adjacentSpaces.contains(new Point(nextX, nextY))) {
											adjacentSpaces.add(tentative, new Point(nextX, nextY));
										}
										// GUI.getInstance().getAuxiliaryColors()[nextX][nextY]
										// = new Color((float) 0,
										// (float) 0, (float) 0, (float) 0.5);
									}
								} catch (Exception e) {
									//e.printStackTrace();
								}

							}
						}
						visited[actualSpace.x][actualSpace.y] = true;
						if (Main.getInstance().getGui() != null)
							Main.getInstance().getGui().repaint2();
						// myAgent.doWait(1);
					}
					actualSpaces = new PriorityQueue();
					actualSpaces.addAll(adjacentSpaces);
					System.out.println(actualSpaces);
					adjacentSpaces = new PriorityQueue();
				} while (actualSpaces.size() != 0);
				retrievePath();
			}
		}

		public void retrievePath() {

			long currentMinimumSum = Long.MAX_VALUE;
			Point choosenFinalSpace = null;

			for (Point finalSpace : finalSpaces) {
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
					GUI.getInstance().getAuxiliaryColors()[actualPoint.x][actualPoint.y] = new Color(0, 0, 0);
					if (Main.getInstance().getGui() != null)
						Main.getInstance().getGui().repaint2();
				} while (actualPoint != initialSpace);
				pathFound.add(actualPoint);
			}
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

	private class AStarPathFind extends CyclicBehaviour {

		private static final long serialVersionUID = 1L;
		private boolean[][] visited;
		private double[][] sum;
		private double[][] sumWithHeuristics;
		private Point[][] parent;
		private Point initialSpace;
		private Point temporaryFinalSpace;
		private Point finalSpace;
		private ArrayList<ArrayList<Point>> foundPaths;
		private double temporaryLongestDistance;
		private ArrayList<Point> actualSpaces;
		private ArrayList<Point> adjacentSpaces;
		private int height;
		private int width;
		private boolean avoid1000 = true;

		private ArrayList<Point> pathFound;
		private double minimumCost = Double.MAX_VALUE;

		public AStarPathFind() {
			height = Main.getInstance().getWeightedGraph().length;
			width = Main.getInstance().getWeightedGraph()[0].length;
			parent = new Point[height][width];
			foundPaths = new ArrayList<ArrayList<Point>>();
		}

		public void action() {
			if (initialSpaces.size() == 0) {

				if (Main.getInstance().isDebug()) {
					// System.out.println(getLocalName() + ": sending proposal
					// to manager. My proposal is " + minimumCost);
				}
				ACLMessage m = new ACLMessage(ACLMessage.PROPOSE);
				m.addReceiver(Main.getInstance().getGRIDManagerAddress());
				m.setContent(minimumCost + "");
				try {
					m.setContentObject(foundPaths);
				} catch (IOException e) {
					e.printStackTrace();
				}
				myAgent.send(m);
				myAgent.doWait();

				myAgent.doDelete();

			} else {
				initialSpace = initialSpaces.remove(0);
				// System.out.println(myAgent.getName() + " is evaluating
				// position: (" + initialSpace.getX() + ", " +
				// initialSpace.getY() + ").");
				if (initialSpaces.size() % 5 == 0) {
					System.gc();
				}

				temporaryLongestDistance = Long.MAX_VALUE;
				for (Point x : finalSpaces) {
					double temp = roundedEuclideanDistance(x, initialSpace);
					if (temp < temporaryLongestDistance) {
						temporaryFinalSpace = x;
						temporaryLongestDistance = temp;
					}
				}

				sum = new double[height][width];
				sumWithHeuristics = new double[height][width];
				for (int i = 0; i < height; i++) {
					for (int j = 0; j < width; j++) {
						sum[i][j] = Double.MAX_VALUE;
						sumWithHeuristics[i][j] = Double.MAX_VALUE;
					}
				}
				sum[initialSpace.x][initialSpace.y] = (double) Main.getInstance()
						.getWeightedGraph()[initialSpace.x][initialSpace.y].getWeight();
				sumWithHeuristics[initialSpace.x][initialSpace.y] = (double) Main.getInstance()
						.getWeightedGraph()[initialSpace.x][initialSpace.y].getWeight();

				visited = new boolean[height][width];

				actualSpaces = new ArrayList<Point>();
				adjacentSpaces = new ArrayList<Point>();
				ArrayList<Point> edges = new ArrayList<Point>();
				actualSpaces.add(initialSpace);
				boolean found = false;
				avoid1000 = true;
				do {
					for (Point actualSpace : actualSpaces) {

						if (visited[actualSpace.x][actualSpace.y]) {
							continue;
						}

						if (finalSpaces.contains(actualSpace)) {
							found = true;
							finalSpace = actualSpace;
							retrievePath();
							break;
						} else {
							for (int i = 0; !found && i < foundPaths.size(); i++) {
								ArrayList<Point> foundPath = foundPaths.get(i);
								if (foundPath.contains(actualSpace)) {
									found = true;
									retrievePathBasedOnFound(i, actualSpace);
									break;
								}
							}
							if (found)
								break;
						}
						ArrayList<Point> neighbours = new ArrayList<Point>();
						ArrayList<Double> neighboursCosts = new ArrayList<Double>();
						ArrayList<Double> neighboursHeuristicsAndCosts = new ArrayList<Double>();

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

								if (Main.getInstance().getWeightedGraph()[nextX][nextY].getWeight() == 1000
										&& avoid1000)
									continue;

								double tentative = Main.getInstance().getWeightedGraph()[nextX][nextY].getWeight()
										+ sum[actualSpace.x][actualSpace.y];
								double heuristic = roundedEuclideanDistance(new Point(nextX, nextY),
										temporaryFinalSpace);
								neighbours.add(new Point(nextX, nextY));
								neighboursCosts.add(tentative + heuristic);
								neighboursHeuristicsAndCosts.add(tentative + heuristic);
							}
						}
						orderArrayListByIndexWithAuxArray(neighboursHeuristicsAndCosts, neighbours, neighboursCosts);
						if (neighbours.size() > 0) {
							double minimum = neighboursHeuristicsAndCosts.get(0);
							while (neighbours.size() > 0 && neighboursHeuristicsAndCosts.get(0) == minimum) {
								Point evaluatedPoint = neighbours.remove(0);
								if (parent[evaluatedPoint.x][evaluatedPoint.y] == null
								/*
								 * || sumWithHeuristics[evaluatedPoint.x][ evaluatedPoint.y] > minimum
								 */) {
									sum[evaluatedPoint.x][evaluatedPoint.y] = neighboursCosts.remove(0);

									sumWithHeuristics[evaluatedPoint.x][evaluatedPoint.y] = neighboursHeuristicsAndCosts
											.remove(0);
									parent[evaluatedPoint.x][evaluatedPoint.y] = actualSpace;
								} else if (sumWithHeuristics[evaluatedPoint.x][evaluatedPoint.y] <= minimum) {
									double distAtual = preciseEuclideanDistance(
											new Point(parent[evaluatedPoint.x][evaluatedPoint.y].x,
													parent[evaluatedPoint.x][evaluatedPoint.y].y),
											temporaryFinalSpace);

									double distNova = preciseEuclideanDistance(
											new Point(evaluatedPoint.x, evaluatedPoint.y), temporaryFinalSpace);
									if (distNova < distAtual) {
										sum[evaluatedPoint.x][evaluatedPoint.y] = neighboursCosts.remove(0);

										sumWithHeuristics[evaluatedPoint.x][evaluatedPoint.y] = neighboursHeuristicsAndCosts
												.remove(0);
										parent[evaluatedPoint.x][evaluatedPoint.y] = actualSpace;
									} else {

									}
								}
								adjacentSpaces.add(evaluatedPoint);

							}

							edges.addAll(neighbours);
						}
						visited[actualSpace.x][actualSpace.y] = true;
						if (Main.getInstance().getGui() != null)
							Main.getInstance().getGui().repaint2();
						// myAgent.doWait(1);
					}
					if (adjacentSpaces.size() == 0) {
						adjacentSpaces.addAll(edges);
						edges = new ArrayList<Point>();
					}
					actualSpaces = new ArrayList<Point>();
					if (!adjacentSpaces.isEmpty()) {
						actualSpaces.addAll(adjacentSpaces);
					} else {
						avoid1000 = false;
						actualSpaces.add(initialSpace);

						visited[initialSpace.x][initialSpace.y] = false;
					}
					adjacentSpaces = new ArrayList<Point>();

				} while (!found);
			}
		}

		public double roundedEuclideanDistance(Point a, Point b) {
			return Math.round(Math.sqrt((Math.pow(a.x - b.x, 2)) + (Math.pow(a.y - b.y, 2))));

		}

		public double preciseEuclideanDistance(Point a, Point b) {
			return Math.sqrt((Math.pow(a.x - b.x, 2)) + (Math.pow(a.y - b.y, 2)));

		}

		public void retrievePath() {

			minimumCost = sum[finalSpace.x][finalSpace.y];
			pathFound = new ArrayList<Point>();
			Point actualPoint = finalSpace;
			do {
				pathFound.add(actualPoint);
				actualPoint = parent[actualPoint.x][actualPoint.y];
				GUI.getInstance().getAuxiliaryColors()[actualPoint.x][actualPoint.y] = new Color(0, 0, 0);
				if (Main.getInstance().getGui() != null)
					Main.getInstance().getGui().repaint2();
			} while (actualPoint != initialSpace);
			pathFound.add(actualPoint);
			foundPaths.add(pathFound);
			System.out.println(myAgent.getName() + " found a unique path for position: (" + initialSpace.getX() + ", "
					+ initialSpace.getY() + ").");
			System.out.println(" There are " + initialSpaces.size() + " positions left");
		}

		public void retrievePathBasedOnFound(int index, Point intersection) {
			ArrayList<Point> previousFoundPath = foundPaths.get(index);
			double costOfThisPath = 0;
			int count = previousFoundPath.size() - 1;
			Point actualSpace = previousFoundPath.get(count);
			ArrayList<Point> newPath = new ArrayList<Point>();
			while (!actualSpace.equals(intersection)) {
				GUI.getInstance().getAuxiliaryColors()[actualSpace.x][actualSpace.y] = new Color(0, 0, 0);
				newPath.add(actualSpace);
				costOfThisPath = costOfThisPath
						+ Main.getInstance().getWeightedGraph()[actualSpace.x][actualSpace.y].getWeight();
				count--;
				actualSpace = previousFoundPath.get(count);
			}
			GUI.getInstance().getAuxiliaryColors()[actualSpace.x][actualSpace.y] = new Color(0, 0, 0);
			newPath.add(actualSpace);
			while (!actualSpace.equals(initialSpace)) {
				GUI.getInstance().getAuxiliaryColors()[actualSpace.x][actualSpace.y] = new Color(0, 0, 0);
				newPath.add(actualSpace);
				costOfThisPath = costOfThisPath
						+ Main.getInstance().getWeightedGraph()[actualSpace.x][actualSpace.y].getWeight();
				actualSpace = parent[actualSpace.x][actualSpace.y];
			}
			GUI.getInstance().getAuxiliaryColors()[actualSpace.x][actualSpace.y] = new Color(0, 0, 0);
			newPath.add(actualSpace);
			if (costOfThisPath < minimumCost) {
				minimumCost = costOfThisPath;
			}
			foundPaths.add(newPath);
			System.out.println(myAgent.getName() + " found a shared path for position: (" + initialSpace.getX() + ", "
					+ initialSpace.getY() + ").");
			System.out.println(" There are " + initialSpaces.size() + " positions left");
		}
	}
}
