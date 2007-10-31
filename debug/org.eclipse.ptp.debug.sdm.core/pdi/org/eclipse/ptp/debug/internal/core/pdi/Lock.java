package org.eclipse.ptp.debug.internal.core.pdi;

/**
 * @author clement
 * 
 */
public class Lock {
	java.lang.Thread heldBy;
	int count;

	public synchronized void aquire() {
		if (heldBy == null || heldBy == java.lang.Thread.currentThread()) {
			heldBy = java.lang.Thread.currentThread();
			count++;
		} else {
			while (true) {
				try {
					wait();
				} catch (InterruptedException e) {
				}
				if (heldBy == null) {
					heldBy = java.lang.Thread.currentThread();
					count++;
					return;
				}
			}
		}
	}
	public synchronized void release() {
		if (heldBy == null || heldBy != java.lang.Thread.currentThread()) {
			throw new IllegalStateException("Thread does not own lock");
		}
		if (--count == 0) {
			heldBy = null;
			notifyAll();
		}
	}
}