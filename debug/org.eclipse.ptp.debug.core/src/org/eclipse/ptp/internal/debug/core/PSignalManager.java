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
package org.eclipse.ptp.internal.debug.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.ptp.debug.core.IPSession;
import org.eclipse.ptp.debug.core.IPSignalManager;
import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.model.IPSignal;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEventListener;
import org.eclipse.ptp.debug.core.pdi.model.IPDISignal;
import org.eclipse.ptp.internal.debug.core.model.PSignal;

/**
 * @author Clement chu
 */
public class PSignalManager implements IAdaptable, IPDIEventListener, IPSignalManager {
	private class PSignalSet {
		private final TaskSet sTasks;
		private IPSignal[] fSignals = null;
		private boolean fIsDisposed = false;

		public PSignalSet(TaskSet sTasks) {
			this.sTasks = sTasks;
		}

		/**
		 * 
		 */
		public synchronized void dispose() {
			if (fSignals != null) {
				for (int i = 0; i < fSignals.length; ++i) {
					((PSignal) fSignals[i]).dispose();
				}
			}
			fSignals = null;
			fIsDisposed = true;
		}

		/**
		 * @param pdiSignal
		 * @return
		 */
		public PSignal find(IPDISignal pdiSignal) {
			try {
				IPSignal[] signals = getSignals();
				for (int i = 0; i < signals.length; ++i) {
					if (signals[i].getName().equals(pdiSignal.getName())) {
						return (PSignal) signals[i];
					}
				}
			} catch (DebugException e) {
			}
			return null;
		}

		/**
		 * @return
		 * @throws DebugException
		 */
		public synchronized IPSignal[] getSignals() throws DebugException {
			if (!fIsDisposed && fSignals == null) {
				try {
					IPDISignal[] pdiSignals = session.getPDISession().getSignalManager().getSignals(sTasks);
					ArrayList<IPSignal> list = new ArrayList<IPSignal>(pdiSignals.length);
					for (int i = 0; i < pdiSignals.length; ++i) {
						list.add(new PSignal(session, sTasks, pdiSignals[i]));
					}
					fSignals = list.toArray(new IPSignal[list.size()]);
				} catch (PDIException e) {
					throwDebugException(e.getMessage(), DebugException.TARGET_REQUEST_FAILED, e);
				}
			}
			return (fSignals != null) ? fSignals : new IPSignal[0];
		}

		/**
		 * @param pdiSignal
		 */
		public void signalChanged(IPDISignal pdiSignal) {
			PSignal signal = find(pdiSignal);
			if (signal != null) {
				signal.fireChangeEvent(DebugEvent.STATE);
			}
		}
	}

	private final IPSession session;
	protected final Map<TaskSet, PSignalSet> fPSignalSetMap = new HashMap<TaskSet, PSignalSet>();

	public PSignalManager(IPSession session) {
		this.session = session;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.IPSignalManager#dispose(org.eclipse
	 * .ptp.core.util.TaskSet)
	 */
	public void dispose(TaskSet qTasks) {
		getSignalSet(qTasks).dispose();
	}

	/**
	 * @param monitor
	 */
	public void dispose(IProgressMonitor monitor) {
		DebugPlugin.getDefault().asyncExec(new Runnable() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				synchronized (fPSignalSetMap) {
					Iterator<PSignalSet> it = fPSignalSetMap.values().iterator();
					while (it.hasNext()) {
						(it.next()).dispose();
					}
					fPSignalSetMap.clear();
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IPDISession.class))
			return getSession();
		if (adapter.equals(PSignalManager.class))
			return this;
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.IPSignalManager#getSignals(org.eclipse
	 * .ptp.core.util.TaskSet)
	 */
	public IPSignal[] getSignals(TaskSet qTasks) throws DebugException {
		return getSignalSet(qTasks).getSignals();
	}

	/**
	 * @param qTasks
	 * @return
	 */
	public PSignalSet getSignalSet(TaskSet qTasks) {
		synchronized (fPSignalSetMap) {
			PSignalSet set = fPSignalSetMap.get(qTasks);
			if (set == null) {
				set = new PSignalSet(qTasks);
				fPSignalSetMap.put(qTasks, set);
			}
			return set;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.event.IPDIEventListener#handleDebugEvents
	 * (org.eclipse.ptp.debug.core.pdi.event.IPDIEvent[])
	 */
	public void handleDebugEvents(IPDIEvent[] events) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.IPSignalManager#signalChanged(org
	 * .eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.ptp.debug.core.pdi.model.IPDISignal)
	 */
	public void signalChanged(TaskSet qTasks, IPDISignal pdiSignal) {
		getSignalSet(qTasks).signalChanged(pdiSignal);
	}

	/**
	 * @return
	 */
	protected IPSession getSession() {
		return session;
	}

	/**
	 * @param message
	 * @param code
	 * @param exception
	 * @throws DebugException
	 */
	protected void throwDebugException(String message, int code, Throwable exception) throws DebugException {
		throw new DebugException(new Status(IStatus.ERROR, PDebugModel.getPluginIdentifier(), code, message, exception));
	}
}
