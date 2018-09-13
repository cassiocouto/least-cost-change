package mase.util;

import java.util.ArrayList;

public class PriorityQueue {
	private ArrayList<Number> priority;
	private ArrayList<Object> value;

	public PriorityQueue() {
		priority = new ArrayList<Number>();
		value = new ArrayList<>();
	}

	public Object remove(int index) throws ArrayIndexOutOfBoundsException {
		if (index >= value.size())
			throw new ArrayIndexOutOfBoundsException();
		priority.remove(index);
		return value.remove(index);
	}

	public void add(Number elementPriority, Object element) {
		add(elementPriority, element, 0);
	}

	public void add(Number elementPriority, Object element, int inferiorLimit) {

		if (priority.size() == 0) {
			priority.add(elementPriority);
			value.add(element);
			return;
		} else if (priority.size() == 1) {
			Number tempN = priority.get(0);
			Object tempO = value.get(0);

			priority.clear();
			value.clear();

			if (tempN.doubleValue() <= elementPriority.doubleValue()) {
				priority.add(tempN);
				value.add(tempO);
				priority.add(elementPriority);
				value.add(element);
				return;
			} else {
				priority.add(elementPriority);
				value.add(element);
				priority.add(tempN);
				value.add(tempO);
				return;
			}
		}

		if (elementPriority.doubleValue() >= priority.get(priority.size() - 1).doubleValue()) {
			priority.add(elementPriority);
			value.add(element);
		} else {
			binaryInsert(elementPriority, element, inferiorLimit);
		}

	}

	private void binaryInsert(Number elementPriority, Object element, int inferiorLimit) {
		int inferior = inferiorLimit;
		int superior = this.size() - 1;
		int delta = superior - inferior;

		while (delta > 1) {
			int index = (int) (Math.floor((inferior + superior) / 2d));
			if (priority.get(index).doubleValue() > elementPriority.doubleValue()) {
				superior = index;
			} else {
				inferior = index;
			}
			delta = superior - inferior;
		}
		priority.add(inferior, elementPriority);
		value.add(inferior, element);

	}

	public int size() {
		return priority.size();
	}

	public Object get(int i) {
		return value.get(i);
	}

	public boolean contains(Object o) {
		return value.contains(o);
	}

	public void addAll(PriorityQueue q) {

		addAll(q, 0);
	}

	public void addAll(PriorityQueue q, int inferiorLimit) {

		for (int i = 0; i < q.size(); i++) {
			this.add(q.getPriority().get(i), q.getValue().get(i), inferiorLimit);
		}

	}

	public ArrayList<Number> getPriority() {
		return priority;
	}

	public ArrayList<Object> getValue() {
		return value;
	}

	public static void main(String args[]) {
		for (int j = 0; j < 1000001; j = j + 2000) {
			long t1 = System.currentTimeMillis();
			PriorityQueue q = new PriorityQueue();
			for (long i = 0; i < j / 2; i++) {
				q.add(Math.round(1000 * Math.random()), "");

			}

			long t2 = System.currentTimeMillis() - t1;
			long t3 = System.currentTimeMillis();

			PriorityQueue q1 = new PriorityQueue();
			for (long i = 0; i < j; i++) {
				q1.add(Math.round(1000 * Math.random()), "");

			}

			long t4 = System.currentTimeMillis() - t3;
			long t5 = System.currentTimeMillis();

			q.addAll(q1);

			long t6 = System.currentTimeMillis() - t5;

			System.out.println(j + "\t" + t2 + "\t" + t4 + "\t" + t6);
		}
	}

}
