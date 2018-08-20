package mase.behaviours;

import java.awt.Point;
import java.util.ArrayList;

import mase.agents.TrackerAgent;
import mase.main.Main;
import mase.util.PriorityQueue;

public class DijkstraPathFind extends PathFind {

	private static final long serialVersionUID = 1L;

	public DijkstraPathFind(ArrayList<Point> initialSpaces, ArrayList<Point> finalSpaces, int height, int width) {
		super(initialSpaces, finalSpaces, height, width);
	}

	public void findThePath(boolean findBest) {

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
				TrackerAgent.printAccountedMessage(myAgent.getLocalName()+": Giving up the position");
				break;
			}

			for (int i = -1; i <= 1; i++) {
				for (int j = -1; j <= 1; j++) {

					int nextX = (int) (actualSpace.x + i);
					int nextY = (int) (actualSpace.y + j); // if (nextY < 0 || nextY >= width)continue;

					try {
						if (visited[nextX][nextY]) {
							continue;
						} else if (!findBest && pathAlreadyFound(new Point(nextX, nextY))) {
							TrackerAgent.printAccountedMessage(myAgent.getLocalName()+": Someone else found a path! I'll follow this trail");
							pathAlreadyFound = true;
							meetingPoint = new Point(nextX, nextY);
							parent[nextX][nextY] = actualSpace;
							continue;
						}
						long tentative = Main.getInstance().getGraph()[nextX][nextY].getWeight()
								+ sum[actualSpace.x][actualSpace.y];
						if (tentative < sum[nextX][nextY]) {
							sum[nextX][nextY] = tentative;
							parent[nextX][nextY] = actualSpace;
							adjacentSpaces.add(tentative + getHeuristic(new Point(nextX, nextY)),
									new Point(nextX, nextY));
						}
					} catch (Exception e) {
					}

				}
			}

			visited[actualSpace.x][actualSpace.y] = true;
			actualSpaces.addAll(adjacentSpaces, aux);
			adjacentSpaces = new PriorityQueue();

		}

		if (isGoalFound(visited)) {
			TrackerAgent.printAccountedMessage(myAgent.getLocalName()+": I found a path!");
			long currentMinimumSum = Long.MAX_VALUE;
			Point choosenFinalSpace = null;
			for (Point finalSpace : finalSpaces) {
				if (currentMinimumSum > sum[finalSpace.x][finalSpace.y]) {
					currentMinimumSum = sum[finalSpace.x][finalSpace.y];
					choosenFinalSpace = finalSpace;
				}
			}
			retrievePath(currentMinimumSum, choosenFinalSpace);
			markPath(choosenFinalSpace);
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
