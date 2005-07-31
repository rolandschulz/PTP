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

package org.eclipse.ptp.debug.external;

import org.eclipse.ptp.debug.external.event.DebugEvent;
import org.eclipse.ptp.debug.external.utils.Queue;

/**
 * Event Thread blocks on the event Queue, wakes up
 * when events are available and notify all the observers.
 */
public class EventThread extends Thread {
	IDebugger dbg;
	
	public EventThread(IDebugger d) {
		super("IDebugger Event Thread"); //$NON-NLS-1$
		dbg = d;
	}
	
	public void run() {
		// Signal by the session of time to die.
		while (dbg.isExiting() != true) {
			DebugEvent event = null;
			Queue eventQueue = dbg.getEventQueue();
			// removeItem() will block until an item is available.
			try {
				event = (DebugEvent) eventQueue.removeItem();
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
			
			try {
				if (event != null) {
					dbg.notifyObservers(event);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("EventThread exits");
	}
}