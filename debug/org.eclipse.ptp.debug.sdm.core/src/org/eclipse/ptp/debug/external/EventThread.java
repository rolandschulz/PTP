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
package org.eclipse.ptp.debug.external;

import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.core.util.Queue;

/**
 * Event Thread blocks on the event Queue, wakes up
 * when events are available and notify all the observers.
 */
public class EventThread extends Thread {
	AbstractDebugger dbg;
	
	public EventThread(AbstractDebugger d) {
		super("IDebugger Event Thread"); //$NON-NLS-1$
		dbg = d;
	}
	
	public void run() {
		// Signal by the session of time to die.
		while (dbg.isExiting() != true) {
			IPCDIEvent event = null;
			Queue eventQueue = dbg.getEventQueue();
			// removeItem() will block until an item is available.
			try {
				event = (IPCDIEvent) eventQueue.removeItem();
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
	}
}
