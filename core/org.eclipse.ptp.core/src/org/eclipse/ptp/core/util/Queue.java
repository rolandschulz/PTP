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

package org.eclipse.ptp.core.util;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Queue<T> {
	protected List<T> list;

	public Queue() {
		list = Collections.synchronizedList(new LinkedList<T>());
	}

	public T removeItem() throws InterruptedException {
		synchronized (list) {
			while (list.isEmpty()) {
				list.wait();
			}
			return list.remove(0);
		}
	}

	public void addItem(T item) {
		synchronized (list) {
			list.add(item);
			list.notifyAll();
		}
	}
	
	@SuppressWarnings("unchecked")
    public T[] clearItems() {
		T[] array;
		synchronized (list) {
			array = (T[]) list.toArray();
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
}
