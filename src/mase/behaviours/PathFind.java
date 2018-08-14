package mase.behaviours;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import mase.main.Main;
import mase.util.PriorityQueue;

public abstract class PathFind extends CyclicBehaviour {
	private static final long serialVersionUID = 1L;

	protected boolean[][] visited;
	protected Long[][] sum;
	protected Point[][] parent;
	protected Point initialSpace;
	protected PriorityQueue actualSpaces;
	protected PriorityQueue adjacentSpaces;
	protected ArrayList<Point> initialSpaces;
	protected ArrayList<Point> finalSpaces;
	protected int height;
	protected int width;
	protected long minimumCost = Long.MAX_VALUE;
	protected ArrayList<Point> pathFound;

	public PathFind(ArrayList<Point> initialSpaces, ArrayList<Point> finalSpaces, int height, int width) {
		this.height = height;
		this.width = width;
		this.initialSpaces = initialSpaces;
		this.finalSpaces = finalSpaces;
		parent = new Point[height][width];
	}

	public void action() {
		if (initialSpaces.size() == 0) {
			bidProposal();
		} else {
			findThePath();
		}
	}

	public abstract void findThePath();

	public boolean isGoalFound(boolean[][] visited) {
		for (Point finalSpace : finalSpaces) {
			if (visited[finalSpace.x][finalSpace.y]) {
				return true;
			}
		}
		return false;
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
			while (actualPoint != null && !actualPoint.equals(initialSpace)) {
				pathFound.add(actualPoint);
				actualPoint = parent[actualPoint.x][actualPoint.y];
			}
			pathFound.add(actualPoint);
		}
	}

	public void bidProposal() {
		ACLMessage m = new ACLMessage(ACLMessage.PROPOSE);
		m.addReceiver(Main.getInstance().getGRIDManagerAddress());
		m.setContent(minimumCost + "");
		myAgent.send(m);
		myAgent.doWait();

		m = myAgent.receive();
		if (m.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
			if (Main.getInstance().isDebug()) {
				System.out.println(myAgent.getLocalName() + ": My proposal won!");
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
	}

}