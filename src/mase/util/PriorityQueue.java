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
		if (priority.size() == 0) {
			priority.add(elementPriority);
			value.add(element);
			return;
		} else if (priority.size() == 1) {
			if (priority.get(0).doubleValue() <= elementPriority.doubleValue()) {
				priority.add(elementPriority);
				value.add(element);
				return;
			} else {
				priority.add(0, elementPriority);
				value.add(0, element);
				return;
			}
		}

		if (elementPriority.doubleValue() >= priority.get(priority.size() - 1).doubleValue()) {
			priority.add(elementPriority);
			value.add(element);
		} else {
			binaryInsert(elementPriority, element);
		}

	}

	public void add(Number elementPriority, Object element, int inferiorLimit) {
		if (priority.size() == 0) {
			priority.add(elementPriority);
			value.add(element);
			return;
		} else if (priority.size() == 1) {
			if (priority.get(0).doubleValue() <= elementPriority.doubleValue()) {
				priority.add(elementPriority);
				value.add(element);
				return;
			} else {
				priority.add(0, elementPriority);
				value.add(0, element);
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

	private void binaryInsert(Number elementPriority, Object element) {
		int inferior = 0;
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
		priority.add(superior, elementPriority);
		value.add(superior, element);
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
		priority.add(superior, elementPriority);
		value.add(superior, element);
	}

	private PriorityQueue quickSort(PriorityQueue q, int left, int right) { 

		int pi = partition(q, left, right);
		
		q = quickSort(q, left, pi-1);
		q = quickSort(q, pi, right);
		
		return q;
	}

	private int partition(PriorityQueue q, int left, int right) {
		double pivot = q.priority.get(right).doubleValue();
		int i = left - 1;

		for (int j = left; j < right; j++) {
			double curr = q.priority.get(j).doubleValue();
			if (curr <= pivot) {
				i++;
				double a0 = q.priority.get(i).doubleValue();
				Object b0 = q.value.get(i);
				double a1 = q.priority.get(j).doubleValue();
				Object b1 = q.value.get(j);
				q.priority.set(i, a1);
				q.value.set(i, b1);
				q.priority.set(j, a0);
				q.value.set(j, b0);

			}
		}
		double a0 = q.priority.get(i + 1).doubleValue();
		Object b0 = q.value.get(i + 1);
		double a1 = q.priority.get(right).doubleValue();
		Object b1 = q.value.get(right);
		q.priority.set(i + 1, a1);
		q.value.set(i + 1, b1);
		q.priority.set(right, a0);
		q.value.set(right, b0);
		return i + 1;
	}

	private void merge() {

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
		
		
		for (int i = 0; i < q.size(); i++) {
			this.add(q.getPriority().get(i), q.getValue().get(i));
		}
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

}
