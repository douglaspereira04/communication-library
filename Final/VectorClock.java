import java.io.Serializable;
import java.util.Iterator;

public class VectorClock implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected long[] vectorClock;
	
	public VectorClock() {
		
	}
	
	public VectorClock(int length) {
		this.vectorClock = new long[length];
	}

	public long get(int i) {
		return this.vectorClock[i];
	}
	
	public void increment(int i) {
		this.vectorClock[i]++;
	}
	
	public void set(VectorClock other) {
		for (int i = 0; i < this.vectorClock.length; i++) {
			this.vectorClock[i] = other.get(i);
		}
	}
	
	public void merge(int i, VectorClock other) {
		this.vectorClock[i]++;
		for (int j = 0; j < i; j++) {
			this.vectorClock[j] = Math.max(this.vectorClock[j], other.get(j));
		}
		for (int j = i+1; j < this.vectorClock.length; j++) {
			this.vectorClock[j] = Math.max(this.vectorClock[j], other.get(j));
		}
	}
	
	public boolean lessEqual(VectorClock other) {
		for (int i = 0; i < this.vectorClock.length; i++) {
			if(this.vectorClock[i] > other.get(i)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean equals(VectorClock other) {
		for (int i = 0; i < this.vectorClock.length; i++) {
			if(this.vectorClock[i] != other.get(i)) {
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