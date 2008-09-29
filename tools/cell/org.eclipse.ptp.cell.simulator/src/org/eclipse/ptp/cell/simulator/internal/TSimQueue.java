/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.cell.simulator.internal;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;


class TSimQueue {
	List requestQueue = new LinkedList();
	Set requestSet = new HashSet();
	
	public TSimQueue() {
	}
	
	public synchronized void addRequest(ITSimRequest request) {
		if (requestSet.contains(request)) {
			return;
		}
		requestQueue.add(request);
		requestSet.add(request);
		notify();
	}
	
	public int size() {
		return requestQueue.size();
	}
	
	public boolean isEmpty() {
		return requestQueue.isEmpty();
	}
	
	public synchronized ITSimRequest popRequest() throws InterruptedException {
		while (isEmpty()) {
			wait();
		}
		ListIterator iterator = requestQueue.listIterator();
		ITSimRequest request = (ITSimRequest) iterator.next();
		iterator.remove();
		requestSet.remove(request);
		return request;
	}
}
