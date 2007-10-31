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
package org.eclipse.ptp.debug.internal.core.pdi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.IPDISignalManager;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEvent;
import org.eclipse.ptp.debug.core.pdi.model.IPDISignal;
import org.eclipse.ptp.debug.internal.core.pdi.event.ChangedEvent;
import org.eclipse.ptp.debug.internal.core.pdi.event.SignalInfo;
import org.eclipse.ptp.debug.internal.core.pdi.model.Signal;
import org.eclipse.ptp.debug.internal.core.pdi.request.CommandRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.ListSignalsRequest;
import org.eclipse.ptp.proxy.debug.client.ProxyDebugSignal;

/**
 * @author Clement chu
 */
public class SignalManager extends Manager implements IPDISignalManager {
	IPDISignal[] EMPTY_SIGNALS = {};
	Signal[] noSigs = new Signal[0];
	Map<BitList, List<IPDISignal>> signalsMap;

	public SignalManager(Session session) {
		super(session, false);
		signalsMap = new Hashtable<BitList, List<IPDISignal>>();
	}
	public void shutdown() {
		signalsMap.clear();
	}
	private synchronized List<IPDISignal> getSignalsList(BitList qTasks) {
		List<IPDISignal> signalsList = signalsMap.get(qTasks);
		if (signalsList == null) {
			signalsList = Collections.synchronizedList(new ArrayList<IPDISignal>());
			signalsMap.put(qTasks, signalsList);
		}
		return signalsList;
	}
	private ProxyDebugSignal[] createSignals(BitList qTasks) throws PDIException {
		return createSignals(qTasks, null);
	}
	private ProxyDebugSignal[] createSignals(BitList qTasks, String name) throws PDIException {
		ListSignalsRequest request = new ListSignalsRequest(session, qTasks, name);
		session.getEventRequestManager().addEventRequest(request);
		return request.getSignals(qTasks);
	}
	private ProxyDebugSignal createSignal(BitList qTasks, String name) throws PDIException {
		ProxyDebugSignal[] signals = createSignals(qTasks, name);
		if (signals.length > 0)
			return signals[0];
		return null;
	}
	private boolean hasSignalChanged(IPDISignal sig, ProxyDebugSignal signal) {
		return !sig.getName().equals(signal.getName()) || sig.isStopSet() != signal.isStop() || sig.isIgnore() != !signal.isPass();
	}
	protected IPDISignal findSignal(BitList qTasks, String name) {
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
	public IPDISignal getSignal(BitList qTasks, String name) {
		IPDISignal sig = findSignal(qTasks, name);
		if (sig == null) {
			try {
				ProxyDebugSignal signal = createSignal(qTasks, name);
				if (signal != null) {
					List<IPDISignal> signalsList = getSignalsList(qTasks);
					signalsList.add(new Signal(session, qTasks, signal.getName(), signal.isStop(), signal.isPrint(), signal.isPass(), signal.getDescription()));
				}
			}
			catch (PDIException e) {
			}
			return new Signal(session, qTasks, name, false, false, false, name);
		}
		return sig;
	}

	public void handle(Signal sig, boolean isIgnore, boolean isStop) throws PDIException {
		StringBuffer buffer = new StringBuffer(sig.getName());
		buffer.append(" ");
		if (isIgnore) {
			buffer.append("ignore");
		} else {
			buffer.append("noignore");
		}
		buffer.append(" ");
		if (isStop) {
			buffer.append("stop");
		} else  {
			buffer.append("nostop");
		}
		
		CommandRequest request = new CommandRequest(session, sig.getTasks(), buffer.toString());
		session.getEventRequestManager().addEventRequest(request);
		request.waitUntilCompleted(sig.getTasks());
		sig.setHandle(isIgnore, isStop);
		session.getEventManager().fireEvents(new IPDIEvent[] { new ChangedEvent(new SignalInfo(session, sig.getTasks(), sig.getName(), sig.getDescription(), sig, null)) });
	}
	public IPDISignal[] getSignals(BitList qTasks) throws PDIException {
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
	public void update(BitList qTasks) throws PDIException {
		ProxyDebugSignal[] new_sigs = createSignals(qTasks);
		List<IPDIEvent> eventList = new ArrayList<IPDIEvent>(new_sigs.length);
		List<IPDISignal> signalsList = getSignalsList(qTasks);
		for (int i = 0; i<new_sigs.length; i++) {
			IPDISignal sig = findSignal(qTasks, new_sigs[i].getName());
			if (sig != null) {
				if (hasSignalChanged(sig, new_sigs[i])) {
					// Fire ChangedEvent
					((Signal)sig).setSignal(new_sigs[i].getName(), new_sigs[i].isStop(), new_sigs[i].isPrint(), new_sigs[i].isPass(), new_sigs[i].getDescription());
					eventList.add(new ChangedEvent(new SignalInfo(session, qTasks, sig.getName(), sig.getDescription(), sig, null)));
				}
			} else {
				signalsList.add(new Signal(session, qTasks, new_sigs[i].getName(), new_sigs[i].isStop(), new_sigs[i].isPrint(), new_sigs[i].isPass(), new_sigs[i].getDescription()));
			}
		}
		IPDIEvent[] events = (IPDIEvent[])eventList.toArray(new IPDIEvent[0]);
		session.getEventManager().fireEvents(events);
	}
} 
