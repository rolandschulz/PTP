/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rtsystem.simulation;
 
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Simple thread-safe Queue implemetation.
 */
public class SimQueue {

	protected List list;

	public SimQueue() {
		list = Collections.synchronizedList(new LinkedList());
	}

	public Object removeItem() throws InterruptedException {
		//print("in removeItem() - entering");
		synchronized (list) {
			while (list.isEmpty()) {
				//print("in removeItem() - about to wait()");
				list.wait();
				//print("in removeItem() - done with wait()");
			}

			// extract the new first cmd
			Object item = list.remove(0);

			//print("in removeItem() - leaving");
			return item;
		}
	}

	public void addItem(Object item) {
		//print("in addItem() - entering");
		synchronized (list) {
			// There will always be room to add to this List
			// because it expands as needed.
			list.add(item);
			//print("in addItem - just added: '" + cmd + "'");

			// After adding, notify any and all waiting
			// threads that the list has changed.
			list.notifyAll();
			//print("in addItem() - just notified");
		}
		//print("in addItem() - leaving");
	}

	public Object[] clearItems() {
		Object[] array;
		synchronized (list) {
			array = list.toArray();
			list.clear();
		}
		return array;
	}

	public boolean isEmpty() {
		boolean empty;
		synchronized (list) {
			empty = list.isEmpty();
		}
		return empty;
	}
	
//	private static void print(String msg) {
//		String name = Thread.currentThread().getName();
//		System.out.println(name + ": " + msg);
//	}
}
