package org.eclipse.ptp.rm.jaxb.core.utils;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;

/**
 * A condition variable for job ids.
 * 
 * @author arossi
 * 
 */
public class JobIdPinTable implements IJAXBNonNLSConstants {
	private final Map<String, Thread> map;

	public JobIdPinTable() {
		map = new HashMap<String, Thread>();
	}

	/**
	 * Adds the id to the pinned table.
	 * 
	 * @param jobId
	 */
	public void pin(String jobId) {
		if (jobId == null) {
			return;
		}
		synchronized (map) {
			while (map.containsKey(jobId)) {
				if (Thread.currentThread().getId() == map.get(jobId).getId()) {
					break;
				}
				try {
					map.wait(STANDARD_WAIT);
				} catch (InterruptedException ignored) {
				}
			}
			map.put(jobId, Thread.currentThread());
		}
	}

	/**
	 * Removes the id to the pinned table and notifies all.
	 * 
	 * @param jobId
	 */
	public void release(String jobId) {
		if (jobId == null) {
			return;
		}
		synchronized (map) {
			while (map.containsKey(jobId)) {
				if (Thread.currentThread().getId() == map.get(jobId).getId()) {
					map.remove(jobId);
					break;
				}
				try {
					map.wait(STANDARD_WAIT);
				} catch (InterruptedException ignored) {
				}
			}
			map.notifyAll();
		}
	}
}
