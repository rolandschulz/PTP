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
package org.eclipse.ptp.debug.external.core.cdi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.model.IPCDISignal;
import org.eclipse.ptp.debug.external.core.cdi.model.Signal;
import org.eclipse.ptp.debug.external.core.cdi.model.Target;
import org.eclipse.ptp.debug.external.core.commands.CLIListSignalsCommand;

/**
 * @author Clement chu
 */
public class SignalManager extends Manager {
	IPCDISignal[] EMPTY_SIGNALS = {};
	Signal[] noSigs = new Signal[0];
	Map signalsMap;

	public SignalManager(Session session) {
		super(session, false);
		signalsMap = new Hashtable();
	}
	
	public void shutdown() {
		signalsMap.clear();
	}

	synchronized List getSignalsList(Target target) {
		List signalsList = (List)signalsMap.get(target);
		if (signalsList == null) {
			signalsList = Collections.synchronizedList(new ArrayList());
			signalsMap.put(target, signalsList);
		}
		return signalsList;
	}

	IPCDISignal[] createSignals(Target target) throws PCDIException {
		return createSignals(target, null);
	}

	IPCDISignal[] createSignals(Target target, String name) throws PCDIException {
		Session session = (Session)getSession();
		BitList tasks = session.createBitList(target.getTargetID());
		
		CLIListSignalsCommand command = new CLIListSignalsCommand(tasks, name);
		session.getDebugger().postCommand(command);
		IPCDISignal[] signals = command.getInfoSignals();
		if (signals == null) {
			throw new PCDIException("No signal found");
		}
		return signals;
	}

	/**
	 * Method hasSignalChanged.
	 * @param sig
	 * @param mISignal
	 * @return boolean
	 */
	private boolean hasSignalChanged(IPCDISignal sig, IPCDISignal signal) {
		return !sig.getName().equals(signal.getName()) ||
			sig.isStopSet() != signal.isStop() ||
			sig.isIgnore() != !signal.isPass();
	}

	protected IPCDISignal findSignal(Target target, String name) {
		IPCDISignal sig = null;
		List signalsList = (List)signalsMap.get(target);
		if (signalsList != null) {
			IPCDISignal[] sigs = (IPCDISignal[])signalsList.toArray(new IPCDISignal[0]);
			for (int i = 0; i < sigs.length; i++) {
				if (sigs[i].getName().equals(name)) {
					sig = sigs[i];
					break;
				}
			}
		}
		return sig;
	}

	public IPCDISignal getSignal(Target target, String name) {
		IPCDISignal sig = findSignal(target, name);
		if (sig == null) {
			// The session maybe terminated because of the signal.
			sig = new Signal(target, name, false, false, false, name);
		}
		return sig;
	}

	public void handle(Signal sig, boolean isIgnore, boolean isStop) throws PCDIException {
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

		Target target = (Target)sig.getTarget();
/*
		CLIHandle handle = factory.createCLIHandle(buffer.toString());
		try {
			miSession.postCommand(handle);
			handle.getMIInfo();
		} catch (MIException e) {
			throw new MI2CDIException(e);
		}
*/		
		sig.handle(isIgnore, isStop);
//		miSession.fireEvent(new MISignalChangedEvent(miSession, sig.getName()));
	}

	public IPCDISignal[] getSignals(Target target) throws PCDIException {
		List signalsList = (List)signalsMap.get(target);
		if (signalsList == null) {
			update(target);
		}
		signalsList = (List)signalsMap.get(target);
		if (signalsList != null) {
			return (IPCDISignal[])signalsList.toArray(new IPCDISignal[0]);
		}
		return EMPTY_SIGNALS;
	}

	public void update(Target target) throws PCDIException {
		IPCDISignal[] new_sigs = createSignals(target);
		List eventList = new ArrayList(new_sigs.length);
		List signalsList = getSignalsList(target);
		for (int i = 0; i<new_sigs.length; i++) {
			IPCDISignal sig = findSignal(target, new_sigs[i].getName());
			if (sig != null) {
				if (hasSignalChanged(sig, new_sigs[i])) {
					// Fire ChangedEvent
					((Signal)sig).setSignal(new_sigs[i]);
					//eventList.add(new MISignalChangedEvent(miSession, miSigs[i].getName())); 
				}
			} else {
				signalsList.add(new Signal(target, new_sigs[i]));
			}
		}
		//MIEvent[] events = (MIEvent[])eventList.toArray(new MIEvent[0]);
		//miSession.fireEvents(events);
	}
} 
