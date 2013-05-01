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
package org.eclipse.ptp.internal.debug.core.pdi.manager;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEvent;
import org.eclipse.ptp.debug.core.pdi.manager.IPDISignalManager;
import org.eclipse.ptp.debug.core.pdi.model.IPDISignal;
import org.eclipse.ptp.debug.core.pdi.model.IPDISignalDescriptor;
import org.eclipse.ptp.debug.core.pdi.request.IPDICommandRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIListSignalsRequest;

/**
 * @author Clement chu
 */
public class SignalManager extends AbstractPDIManager implements IPDISignalManager {
	private IPDISignal[] EMPTY_SIGNALS = {};
	private Map<TaskSet, List<IPDISignal>> signalsMap;

	public SignalManager(IPDISession session) {
		super(session, false);
		signalsMap = new Hashtable<TaskSet, List<IPDISignal>>();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDISignalManager#getSignals(org.eclipse.ptp.core.util.TaskSet)
	 */
	public IPDISignal[] getSignals(TaskSet qTasks) throws PDIException {
		List<IPDISignal> signalsList = signalsMap.get(qTasks);
		if (signalsList == null) {
			update(qTasks);
		}
		signalsList = signalsMap.get(qTasks);
		if (signalsList != null) {
			return (IPDISignal[])signalsList.toArray(new IPDISignal[0]);
		}
		return EMPTY_SIGNALS;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDISignalManager#handle(org.eclipse.ptp.debug.core.pdi.model.IPDISignal, boolean, boolean)
	 */
	public void handle(IPDISignal sig, boolean isIgnore, boolean isStop) throws PDIException {
		StringBuffer buffer = new StringBuffer(sig.getName());
		buffer.append(" "); //$NON-NLS-1$
		if (isIgnore) {
			buffer.append("ignore"); //$NON-NLS-1$
		} else {
			buffer.append("noignore"); //$NON-NLS-1$
		}
		buffer.append(" "); //$NON-NLS-1$
		if (isStop) {
			buffer.append("stop"); //$NON-NLS-1$
		} else  {
			buffer.append("nostop"); //$NON-NLS-1$
		}
		
		IPDICommandRequest request = session.getRequestFactory().getCommandRequest(sig.getTasks(), buffer.toString());
		session.getEventRequestManager().addEventRequest(request);
		request.waitUntilCompleted(sig.getTasks());
		sig.setHandle(isIgnore, isStop);
		session.getEventManager().fireEvents(new IPDIEvent[] { 
				session.getEventFactory().newChangedEvent(
						session.getEventFactory().newSignalInfo(session, sig.getTasks(), sig.getName(), sig.getDescription(), sig, null)) });
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.debug.core.pdi.AbstractPDIManager#shutdown()
	 */
	public void shutdown() {
		signalsMap.clear();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.debug.core.pdi.AbstractPDIManager#update(org.eclipse.ptp.core.util.TaskSet)
	 */
	public void update(TaskSet qTasks) throws PDIException {
		IPDISignalDescriptor[] new_sigs = createSignals(qTasks);
		List<IPDIEvent> eventList = new ArrayList<IPDIEvent>(new_sigs.length);
		List<IPDISignal> signalsList = getSignalsList(qTasks);
		for (int i = 0; i<new_sigs.length; i++) {
			IPDISignal sig = findSignal(qTasks, new_sigs[i].getName());
			if (sig != null) {
				if (hasSignalChanged(sig, new_sigs[i])) {
					// Fire ChangedEvent
					sig.setDescriptor(new_sigs[i]);
					eventList.add(session.getEventFactory().newChangedEvent(
							session.getEventFactory().newSignalInfo(session, qTasks, sig.getName(), sig.getDescription(), sig, null)));
				}
			} else {
				signalsList.add(session.getModelFactory().newSignal(session, qTasks, new_sigs[i]));
			}
		}
		IPDIEvent[] events = (IPDIEvent[])eventList.toArray(new IPDIEvent[0]);
		session.getEventManager().fireEvents(events);
	}
	
	/**
	 * @param qTasks
	 * @return
	 * @throws PDIException
	 */
	private IPDISignalDescriptor[] createSignals(TaskSet qTasks) throws PDIException {
		return createSignals(qTasks, null);
	}
	
	/**
	 * @param qTasks
	 * @param name
	 * @return
	 * @throws PDIException
	 */
	private IPDISignalDescriptor[] createSignals(TaskSet qTasks, String name) throws PDIException {
		IPDIListSignalsRequest request = session.getRequestFactory().getListSignalsRequest(session, qTasks, name);
		session.getEventRequestManager().addEventRequest(request);
		return request.getSignals(qTasks);
	}

	/**
	 * @param qTasks
	 * @return
	 */
	private synchronized List<IPDISignal> getSignalsList(TaskSet qTasks) {
		List<IPDISignal> signalsList = signalsMap.get(qTasks);
		if (signalsList == null) {
			signalsList = Collections.synchronizedList(new ArrayList<IPDISignal>());
			signalsMap.put(qTasks, signalsList);
		}
		return signalsList;
	}
	
	/**
	 * @param sig
	 * @param desc
	 * @return
	 */
	private boolean hasSignalChanged(IPDISignal sig, IPDISignalDescriptor desc) {
		return !sig.getName().equals(desc.getName()) || sig.isStopSet() != desc.getStop() || sig.isIgnore() != !desc.getPass();
	}
	
	/**
	 * @param qTasks
	 * @param name
	 * @return
	 */
	protected IPDISignal findSignal(TaskSet qTasks, String name) {
		IPDISignal sig = null;
		List<IPDISignal> signalsList = signalsMap.get(qTasks);
		if (signalsList != null) {
			IPDISignal[] sigs = (IPDISignal[])signalsList.toArray(new IPDISignal[0]);
			for (int i = 0; i < sigs.length; i++) {
				if (sigs[i].getName().equals(name)) {
					sig = sigs[i];
					break;
				}
			}
		}
		return sig;
	}
} 
