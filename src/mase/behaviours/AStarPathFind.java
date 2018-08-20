package mase.behaviours;

import java.awt.Point;
import java.util.ArrayList;

public class AStarPathFind extends DijkstraPathFind {

	public AStarPathFind(ArrayList<Point> initialSpaces, ArrayList<Point> finalSpaces, int height, int width) {
		super(initialSpaces, finalSpaces, height, width);
	}

	private static final long serialVersionUID = 1L;

	public Point chooseFinalPoint(Point p) {
		double dist = Double.MAX_VALUE;
		Point choosen = null;
		for (Point finalSpace : finalSpaces) {
			double curr_dist = Point.distance(finalSpace.x, finalSpace.y, p.x, p.y);
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
