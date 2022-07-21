package gc;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class VectorClock implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected Map<Integer, Long> vectorClock;
	
	public VectorClock() {
		
	}
	
	public VectorClock(Set<Integer> ids) {
		this.vectorClock = new HashMap<>();
		for (Iterator<Integer> iterator = ids.iterator(); iterator.hasNext();) {
			Integer id = (Integer) iterator.next();
			this.vectorClock.put(id, new Long(0));
		}
	}

	public long get(int i) {
		return this.vectorClock.get(i);
	}
	
	public void increment(int i) {
		this.vectorClock.put(i, (this.vectorClock.get(i)+1));
	}
	
	public void set(VectorClock other) {
		for (Map.Entry<Integer, Long> entry : this.vectorClock.entrySet()) {
			Integer i = entry.getKey();
			this.vectorClock.put(i, other.get(i));
		}
	}
	
	public void merge(VectorClock other) {
		for (Map.Entry<Integer, Long> entry : this.vectorClock.entrySet()) {
			Integer j = entry.getKey();
			this.vectorClock.put(j, Math.max(this.vectorClock.get(j), other.get(j)));
		}
	}
	
	public boolean lessEqual(VectorClock other) {
		for (Map.Entry<Integer, Long> entry : this.vectorClock.entrySet()) {
			Integer i = entry.getKey();
			Long t = entry.getValue();
			if(t > other.get(i)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean equals(VectorClock other) {
		for (Map.Entry<Integer, Long> entry : this.vectorClock.entrySet()) {
			Integer i = entry.getKey();
			Long t = entry.getValue();
			if(t != other.get(i)) {
				return false;
			}
		}
		return true;
	}

	public boolean less(VectorClock other) {
		return this.lessEqual(other) && (!this.equals(other));
	}
	
	public boolean concurrent(VectorClock other) {
		return (!this.lessEqual(other)) && (!other.lessEqual(this));
	}
	
}
