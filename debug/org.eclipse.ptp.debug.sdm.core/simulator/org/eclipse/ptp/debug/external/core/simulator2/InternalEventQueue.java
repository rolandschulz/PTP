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
package org.eclipse.ptp.debug.external.core.simulator2;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

/**
 * @author Clement chu
 * 
 */
public class InternalEventQueue extends Observable {
	private List queue = null;
	//private long current_time = 0;
	private Thread timer = null;
	private boolean running_timer = true;
	private long FIX_TIME = 0;
	
	public InternalEventQueue(long time_range) {
		FIX_TIME = time_range;
		queue = Collections.synchronizedList(new LinkedList());
	}
	public QueueItem getItem() {
		synchronized (queue) {
			return (QueueItem)queue.remove(0);
		}
	}
	public void addItem(QueueItem item) {
		synchronized (queue) {
			if (!same(item)) {
				notifySimulator();
			}
			queue.add(item);
		}
	}
	public boolean same(QueueItem qItem) {
		synchronized (queue) {
			if (queue.isEmpty())
				return true;
			QueueItem item = getItem();
			if (item.equals(qItem)) {
				qItem.getTasks().or(item.getTasks());
				return true;
			}
			queue.add(item);			
			return false;
		}
	}
	public boolean isEmpty() {
		synchronized (queue) {
			return queue.isEmpty();
		}
	}
	private void notifySimulator() {
		if (!isEmpty()) {
			setChanged();
			notifyObservers(getItem());					
		}
	}
	
	public void startTimer() {
		if (timer == null) {
			running_timer = true;
			//current_time = System.currentTimeMillis();
			Runnable runnable = new Runnable() {
				public void run() {
					while (running_timer) {
						try {
							Thread.sleep(FIX_TIME);
						} catch (InterruptedException e) {
							stopTimer();
						} finally {
							notifySimulator();
						}
						
						/*
						if (System.currentTimeMillis() - current_time >= FIX_TIME) {
							current_time = System.currentTimeMillis();
							notifySimulator();
						}
						*/
					}
				}
			};
			timer = new Thread(runnable);
			timer.start();
		}
	}
	
	public void stopTimer() {
		running_timer = false;
		if (timer != null) {
			timer.interrupt();
		} 
		timer = null;
	}
}

