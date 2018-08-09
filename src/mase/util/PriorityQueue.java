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
		} else {
			for (int i = 0; i < priority.size(); i++) {
				if (elementPriority.doubleValue() >= priority.get(i).doubleValue()) {
					continue;
				} else {
					priority.add(i, elementPriority);
					value.add(i, element);
					break;
				}
			}
		}
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
		this.priority.addAll(q.getPriority());
		this.value.addAll(q.getValue());
	}

	public ArrayList<Number> getPriority() {
		return priority;
	}

	public ArrayList<Object> getValue() {
		return value;
	}

}
