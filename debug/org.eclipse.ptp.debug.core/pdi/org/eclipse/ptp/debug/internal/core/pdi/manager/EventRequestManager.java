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
package org.eclipse.ptp.debug.internal.core.pdi.manager;

import java.util.Observable;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.PDebugUtils;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIEventRequestManager;
import org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIStopDebuggerRequest;

/**
 * @author clement
 * 
 */
public class EventRequestManager extends AbstractPDIManager implements IPDIEventRequestManager {
	public class EventRequestDispatchJob extends Job {
		private Vector<IPDIEventRequest> fRequests = null;
		
		public EventRequestDispatchJob() {
			super("PTP Debug Request Job");
			setSystem(true);
			fRequests = new Vector<IPDIEventRequest>(10);
		}
		
		/**
		 * @param request
		 * @throws PDIException
		 */
		public void addEventRequest(IPDIEventRequest request) throws PDIException {
			synchronized (fRequests) {
				if (containEventRequest(request))
					throw new PDIException(request.getTasks(), "[" + request.getName() + "] request is already added.");

				PDebugUtils.println("Msg: EventRequestManager - addEventRequest(): Request: " + request);
				fRequests.add(request);
			}
			schedule();
		}
		
		/**
		 * 
		 */
		public void cleanEventRequests() {
			synchronized (fRequests) {
				fRequests.clear();
			}
		}
		
		/**
		 * @param request
		 * @return
		 */
		public boolean containEventRequest(IPDIEventRequest request) {
			synchronized (fRequests) {
				return fRequests.contains(request);
			}
		}
		
		/**
		 * @return
		 */
		public IPDIEventRequest getCurrentEventRequest() {
			synchronized (fRequests) {
				if (fRequests.isEmpty())
					return null;
				return fRequests.get(0);
			}
		}
		
		/**
		 * @return
		 */
		public IPDIEventRequest[] getEventRequests() {
			synchronized (fRequests) {
				return fRequests.toArray(new IPDIEventRequest[0]);
			}
		}
		
		/**
		 * 
		 */
		public void removeCurrentEventRequest() {
			synchronized (fRequests) {
				if (!fRequests.isEmpty())
					fRequests.remove(0);
			}
			schedule();
		}
		
		/**
		 * @param request
		 * @throws PDIException
		 */
		public void removeEventRequest(IPDIEventRequest request) throws PDIException {
			synchronized (fRequests) {
				if (request.getStatus() == IPDIEventRequest.RUNNING)
					throw new PDIException(request.getTasks(), "[" + request.getName() + "] request cannot be deleted during executing.");
				if (!containEventRequest(request))
					throw new PDIException(request.getTasks(), "[" + request.getName() + "] request is not existed or already deleted.");
				fRequests.remove(request);
			}
		}
		
		/**
		 * @throws PDIException
		 */
		public void removeEventRequests() throws PDIException {
			synchronized (fRequests) {
				int end = fRequests.size() - 1;
				for (int i=end; i>=0; i--) {
					removeEventRequest(fRequests.get(i));
					fRequests.remove(i);
				}
			}
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		public IStatus run(IProgressMonitor monitor) {
			SafeRunner.run(new ISafeRunnable() {
				public void handleException(Throwable exception) {
					PTPDebugCorePlugin.log(exception);
				}
				public void run() throws Exception {
					execute(getCurrentEventRequest());
				}
			});			
			return Status.OK_STATUS;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#shouldRun()
		 */
		public boolean shouldRun() {
			synchronized (fRequests) {
				return (!fRequests.isEmpty() && getCurrentEventRequest().getStatus() == IPDIEventRequest.UNKNOWN);
			}
		}
	}

	private EventRequestDispatchJob dispatchJob = new EventRequestDispatchJob();
	
	public EventRequestManager(IPDISession session) {
		super(session, false);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequestManager#addEventRequest(org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequest)
	 */
	public void addEventRequest(IPDIEventRequest request) throws PDIException {
		if (!(request instanceof IPDIStopDebuggerRequest)) {
			if (request.getTasks().isEmpty()) {
				throw new PDIException(request.getTasks(), request.getName()  + ": No tasks found");
			}
			if (session.getTaskManager().isAllPending(request.getTasks())) {
				throw new PDIException(request.getTasks(), request.getName() + ": Request tasks are in pending status.");
			}
		}
		dispatchJob.addEventRequest(request);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequestManager#canExecute(org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequest)
	 */
	public boolean canExecute(IPDIEventRequest request) {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequestManager#cleanEventRequests()
	 */
	public void cleanEventRequests() {
		session.getEventManager().removeAllRegisteredEventRequests();
		dispatchJob.cleanEventRequests();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequestManager#deleteAllEventRequests()
	 */
	public void deleteAllEventRequests() throws PDIException {
		dispatchJob.removeEventRequests();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequestManager#deleteEventRequest(org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequest)
	 */
	public void deleteEventRequest(IPDIEventRequest request) throws PDIException {
		dispatchJob.removeEventRequest(request);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequestManager#execute(org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequest)
	 */
	public void execute(IPDIEventRequest request) {
		session.getEventManager().registerEventRequest(request);
		request.execute(session.getDebugger());
		if (request.getStatus() == IPDIEventRequest.ERROR) {
			session.getEventManager().notifyEventRequest(request);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequestManager#getRequests()
	 */
	public IPDIEventRequest[] getRequests() {
		return dispatchJob.getEventRequests();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.internal.core.pdi.AbstractPDIManager#shutdown()
	 */
	public void shutdown() {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.internal.core.pdi.AbstractPDIManager#update(org.eclipse.ptp.core.util.BitList)
	 */
	@Override
	public void update(BitList tasks) throws PDIException {
	}
	
	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable o, Object arg) {
		if (arg instanceof IPDIEventRequest) {
			dispatchJob.removeCurrentEventRequest();
		}
	}
}
