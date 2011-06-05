/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control.internal.utils;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ptp.rm.jaxb.control.JAXBControlConstants;

/**
 * A condition variable for job ids.
 * 
 * @author arossi
 * 
 */
public class JobIdPinTable {

	private class PinData {
		private int count;
		private long tid;
	}

	private final Map<String, PinData> map;

	public JobIdPinTable() {
		map = new HashMap<String, PinData>();
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
				PinData data = map.get(jobId);
				if (Thread.currentThread().getId() == data.tid) {
					data.count++;
					break;
				}
				try {
					map.wait(JAXBControlConstants.STANDARD_WAIT);
				} catch (InterruptedException ignored) {
				}
			}
			PinData data = new PinData();
			data.count = 1;
			data.tid = Thread.currentThread().getId();
			map.put(jobId, data);
		}
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
		synchronized (map) {
			if (map.containsKey(jobId)) {
				PinData data = map.get(jobId);
				if (Thread.currentThread().getId() == data.tid) {
					data.count--;
					if (data.count == 0) {
						map.remove(jobId);
						map.notifyAll();
					}
				}
			}
		}
	}
}
