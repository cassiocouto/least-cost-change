package mase.behaviours;

import java.awt.Point;
import java.util.ArrayList;

import jade.core.behaviours.OneShotBehaviour;
import mase.main.Main;
import mase.util.PriorityQueue;

public class DijkstraPathFind extends OneShotBehaviour {

	private static final long serialVersionUID = 1L;
	private PriorityQueue frontier;
	private int height;
	private int width;
	private Point[][] cameFrom;
	private Double[][] costSoFar;
	private ArrayList<Point> initialSpaces;
	private ArrayList<Point> bestPath;
	private double leastCostFound;

	public DijkstraPathFind(ArrayList<Point> initialSpaces) {
		frontier = new PriorityQueue();
		height = Main.getInstance().getWeightedGraph().length;
		width = Main.getInstance().getWeightedGraph()[0].length;
		cameFrom = new Point[height][width];
		costSoFar = new Double[height][width];
		this.initialSpaces = initialSpaces;
		leastCostFound = Double.MAX_VALUE;
	}

	public void action() {
		Point currentInitialPoint;
		while (initialSpaces.size() > 0) {
			currentInitialPoint = initialSpaces.remove(0);
			frontier.add(0, currentInitialPoint);
			costSoFar[currentInitialPoint.x][currentInitialPoint.y] = new Double(
					Main.getInstance().getWeightedGraph()[currentInitialPoint.x][currentInitialPoint.y].getWeight());
			boolean found = false;
			while (!found || frontier.size() > 0) {
				Point actualPoint = (Point) frontier.remove(0);
				if (Main.getInstance().getFinalSpaces().contains(actualPoint)) {
					found = true;
				} else {
					ArrayList<Point> neighbours = getNeighbours(actualPoint);
					for (int i = 0; i < neighbours.size(); i++) {
						double newCost = costSoFar[actualPoint.x][actualPoint.y].doubleValue()
								+ (double) Main.getInstance().getWeightedGraph()[neighbours.get(i).x][neighbours.get(i).y]
										.getWeight();
						if (costSoFar[neighbours.get(i).x][neighbours.get(i).y] == null
								|| newCost < costSoFar[neighbours.get(i).x][neighbours.get(i).y]) {
							costSoFar[neighbours.get(i).x][neighbours.get(i).y] = newCost;
							cameFrom[neighbours.get(i).x][neighbours.get(i).y] = actualPoint;
							frontier.add(newCost, neighbours.get(i));
						}
					}
				}
			}
		}

	}

	public ArrayList<Point> getNeighbours(Point actual) {
		ArrayList<Point> neighbours = new ArrayList<Point>();
		int[] index = new int[] { -1, 0, 1 };
		for (int i : index) {
			for (int j : index) {
				if (i == 0 && j == 0) {
					continue;
				} else if (actual.x + i < 0 || actual.x + i > height) {
					continue;
				} else if (actual.y + j < 0 || actual.y + j > width) {
					continue;
				} else {
					neighbours.add(new Point(actual.x + i, actual.y + j));
				}
			}
		}
		return neighbours;
	}

}
