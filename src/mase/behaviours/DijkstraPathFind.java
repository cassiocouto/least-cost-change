package mase.behaviours;

import java.awt.Point;
import java.util.ArrayList;

import mase.agents.TrackerAgent;
import mase.main.Main;
import mase.util.PriorityQueue;

public class DijkstraPathFind extends PathFind {

	private static final long serialVersionUID = 1L;
	private long t1;
	private long t2;
	private long t3;
	private long t4;

	public DijkstraPathFind(ArrayList<Point> initialSpaces, ArrayList<Point> finalSpaces, int height, int width) {
		super(initialSpaces, finalSpaces, height, width);
	}

	public void findThePath(boolean findBest) {
		t1 = 0;
		t2 = 0;
		t3 = 0;
		t4 = 0;
		
		initialSpace = initialSpaces.remove(0);
		myFinalSpace = chooseFinalPoint(initialSpace);
		sum = new Long[height][width];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				sum[i][j] = Long.MAX_VALUE;
			}
		}
		sum[initialSpace.x][initialSpace.y] = (long) Main.getInstance().getGraph()[initialSpace.x][initialSpace.y]
				.getWeight();

		visited = new boolean[height][width];

		actualSpaces = new PriorityQueue();
		adjacentSpaces = new PriorityQueue();

		actualSpaces.add(0, initialSpace);

		boolean pathAlreadyFound = false;
		Point meetingPoint = null;

		for (int aux = 0; !pathAlreadyFound && !isGoalFound(visited); aux++) {
			Point actualSpace = (Point) actualSpaces.get(aux);

			if (visited[actualSpace.x][actualSpace.y]) {
				continue;
			} else if (findBest && sum[actualSpace.x][actualSpace.y] > minimumCost) {
				TrackerAgent.printAccountedMessage(myAgent.getLocalName() + ": Giving up the position");
				break;
			}
			long tempt1 = System.currentTimeMillis();
			Object[] o = evaluateNeighbours(actualSpace, findBest);
			t1 += (System.currentTimeMillis() - tempt1);
			pathAlreadyFound = (Boolean) o[0];
			meetingPoint = (Point) o[1];

			visited[actualSpace.x][actualSpace.y] = true;
			long tempt2 = System.currentTimeMillis();
			actualSpaces.addAll(adjacentSpaces, aux);
			t2 += (System.currentTimeMillis() - tempt2);
			adjacentSpaces = new PriorityQueue();
		}

		if (isGoalFound(visited)) {
			long currentMinimumSum = Long.MAX_VALUE;
			Point choosenFinalSpace = null;
			for (Point finalSpace : finalSpaces) {
				if (currentMinimumSum >= sum[finalSpace.x][finalSpace.y]) {
					currentMinimumSum = sum[finalSpace.x][finalSpace.y];
					choosenFinalSpace = finalSpace;
				}
			}
			long tempt3 = System.currentTimeMillis();
			retrievePath(currentMinimumSum, choosenFinalSpace);
			t3 += (System.currentTimeMillis() - tempt3);
			long tempt4 = System.currentTimeMillis();
			markPath(choosenFinalSpace);
			t4 += (System.currentTimeMillis() - tempt4);
			TrackerAgent.printAccountedMessage(
					myAgent.getLocalName() + ": I found a path!\t" + t1 + "\t" + t2 + "\t" + t3 + "\t" + t4);
		} else if (pathAlreadyFound) {
			markPath(meetingPoint);
		}
	}

	public double getHeuristic(Point p) {
		return 0;
	}

	public Point chooseFinalPoint(Point p) {
		return null;
	}

	public boolean pathAlreadyFound(Point p) {
		return false;
	}

}
