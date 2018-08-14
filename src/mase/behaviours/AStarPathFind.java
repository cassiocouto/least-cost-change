package mase.behaviours;

import java.awt.Point;
import java.util.ArrayList;

import mase.main.Main;
import mase.util.PriorityQueue;

public class AStarPathFind extends PathFind {

	protected Point myFinalSpace;

	public AStarPathFind(ArrayList<Point> initialSpaces, ArrayList<Point> finalSpaces, int height, int width) {
		super(initialSpaces, finalSpaces, height, width);
	}

	private static final long serialVersionUID = 1L;

	public void findThePath() {
		initialSpace = initialSpaces.remove(0);
		myFinalSpace = chooseFinalPoint();

		sum = new Long[height][width];
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				sum[i][j] = Long.MAX_VALUE;
			}
		}
		sum[initialSpace.x][initialSpace.y] = (long) Main.getInstance()
				.getWeightedGraph()[initialSpace.x][initialSpace.y].getWeight();

		visited = new boolean[height][width];

		actualSpaces = new PriorityQueue();
		adjacentSpaces = new PriorityQueue();

		actualSpaces.add(0, initialSpace);
		for (int aux = 0; !isGoalFound(visited); aux++) {
			Point actualSpace = (Point) actualSpaces.get(aux);
			if (visited[actualSpace.x][actualSpace.y]) {
				continue;
			} else if (sum[actualSpace.x][actualSpace.y] > minimumCost) {
				break;
			}

			for (int i = -1; i <= 1; i++) {
				for (int j = -1; j <= 1; j++) {

					int nextX = (int) (actualSpace.x + i);
					int nextY = (int) (actualSpace.y + j); // if (nextY < 0 || nextY >= width)continue;

					try {
						if (visited[nextX][nextY]) {
							continue;
						}
						long tentative = Main.getInstance().getWeightedGraph()[nextX][nextY].getWeight()
								+ sum[actualSpace.x][actualSpace.y];
						if (tentative < sum[nextX][nextY]) {
							sum[nextX][nextY] = tentative;
							parent[nextX][nextY] = actualSpace;
							adjacentSpaces.add(tentative + getHeuristic(actualSpace), new Point(nextX, nextY));
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
			retrievePath();
		}
	}

	public Point chooseFinalPoint() {
		double dist = Double.MAX_VALUE;
		Point choosen = null;
		for (Point finalSpace : finalSpaces) {
			double curr_dist = Point.distance(finalSpace.x, finalSpace.y, initialSpace.x, initialSpace.y);
			if (dist > curr_dist) {
				dist = curr_dist;
				choosen = finalSpace;
			}
		}

		return choosen;
	}

	public double getHeuristic(Point p) {
		return Point.distance(myFinalSpace.x, myFinalSpace.y, p.x, p.y);
	}

}