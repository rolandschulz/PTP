/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.core.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A lock table for job ids.
 * 
 * @author arossi
 * 
 */
public class JobIdPinTable {

	private final Map<String, ReentrantLock> map;

	public JobIdPinTable() {
		map = Collections.synchronizedMap(new HashMap<String, ReentrantLock>());
	}

	/**
	 * Adds the id to the pinned table with its associated lock.
	 * 
	 * @param jobId
	 */
	public void pin(String jobId) {
		if (jobId == null) {
			return;
		}
		ReentrantLock lock = map.get(jobId);
		if (lock == null) {
			lock = new ReentrantLock();
		}
		lock.lock();
		map.put(jobId, lock);
	}

	/**
	 * Removes the id from the pinned table and notifies all.
	 * 
	 * @param jobId
	 */
	public void release(String jobId) {
		if (jobId == null) {
			return;
		}
		ReentrantLock lock = map.get(jobId);
		if (lock != null) {
			if (lock.isHeldByCurrentThread()) {
				lock.unlock();
				map.remove(jobId);
			}
		}
	}
}
