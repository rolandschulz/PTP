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
package org.eclipse.ptp.debug.external.core;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.debug.core.IAbstractDebugger;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;

public class EventThread {
	private List fEventQueue = new ArrayList();
	private EventDispatchJob dispatchJob = new EventDispatchJob(); 

	IAbstractDebugger dbg;
	
	public EventThread(IAbstractDebugger dbg) {
		this.dbg = dbg;
	}
	public void cancelJob() {
		dispatchJob.cancel();
	}
	public void fireDebugEvent(IPCDIEvent event) {
		synchronized (fEventQueue) {
			fEventQueue.add(event);
		}
		dispatchJob.schedule();
	}
	class EventDispatchJob extends Job {
	    /**
         * Creates a new event dispatch job.
         */
        public EventDispatchJob() {
            super("EventDispatchJob"); 
            setPriority(Job.INTERACTIVE);
            setSystem(true);
        }
        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
         */
        protected IStatus run(IProgressMonitor monitor) {
            while (!dbg.isExited() && !fEventQueue.isEmpty()) {
            	IPCDIEvent event = null;
	            synchronized (fEventQueue) {
	                if (!fEventQueue.isEmpty()) {
	                	event = (IPCDIEvent) fEventQueue.remove(0);
	                }
	            }
	            if (event != null) {
					dbg.notifyObservers(event);
	            }
            }
            return Status.OK_STATUS;
        }
	}    
}
