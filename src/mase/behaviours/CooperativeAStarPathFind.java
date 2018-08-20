package mase.behaviours;

import java.awt.Point;
import java.util.ArrayList;

import jade.lang.acl.ACLMessage;
import mase.main.Main;

public class CooperativeAStarPathFind extends AStarPathFind {
	private static final long serialVersionUID = 1L;

	public CooperativeAStarPathFind(ArrayList<Point> initialSpaces, ArrayList<Point> finalSpaces, int height,
			int width) {
		super(initialSpaces, finalSpaces, height, width);
	}

	public void action() {
		if (initialSpaces.size() == 0) {
			informExecutionSuccessAndExit();
		} else {
			findThePath(false);
		}
	}

	public void informExecutionSuccessAndExit() {
		ACLMessage m = new ACLMessage(ACLMessage.INFORM);
		m.addReceiver(Main.getInstance().getGRIDManagerAddress());
		myAgent.send(m);
		myAgent.doDelete();
	}

	public boolean pathAlreadyFound(Point p) {
		return Main.getInstance().getGraph()[p.x][p.y].getTrackerId() != null;
	}

}
