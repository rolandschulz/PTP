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
package org.eclipse.ptp.debug.internal.core.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.IProcessEvent;
import org.eclipse.ptp.core.IProcessListener;
import org.eclipse.ptp.debug.core.IAbstractDebugger;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.core.model.IPseudoProcess;

/**
 * @deprecated
 */
public class PseudoProcess implements IProcessListener, IPseudoProcess {
	private final String PROCESS_NAME = "Process ";
	/***************************************************************************************************************************************************************************************************
	 * IProcess
	 **************************************************************************************************************************************************************************************************/
	private static final int MAX_WAIT_FOR_DEATH_ATTEMPTS = 10;
	private static final int TIME_TO_WAIT_FOR_THREAD_DEATH = 500;
	private IPLaunch launch;
	private int fExitValue;
	private IStreamsProxy fStreamsProxy;
	private boolean fTerminated;
	private Map fAttributes;
	/***************************************************************************************************************************************************************************************************
	 * Process
	 **************************************************************************************************************************************************************************************************/
	private boolean finished = false;
	private InputStream err = null;
	private InputStream in = null;
	private OutputStream out = null;
	private IPProcess pproc = null;
	private IAbstractDebugger debugger = null;

	public PseudoProcess(IAbstractDebugger debugger, IPProcess pproc, IPLaunch launch, Map attributes) {
		this.debugger = debugger;
		this.pproc = pproc;
		this.launch = launch;
		init(attributes);
	}
	private void init(Map attributes) {
		finished = false;
		err = null;
		in = new PseudoInputStream();
		out = new PseudoOutputStream();
		pproc.addProcessListener(this);

		initializeAttributes(attributes);
		fTerminated = true;
		try {
			exitValue();
		} catch (IllegalThreadStateException e) {
			fTerminated = false;
		}
		fStreamsProxy = new PStreamsProxy(getInputStream(), getErrorStream(), getOutputStream());
		launch.addProcess(this);
		fireCreationEvent();
	}
	public int exitValue() {
		if (finished)
			return 0;
		else
			throw new IllegalThreadStateException();
	}
	public void destroy() {
		finished = true;
		pproc.removerProcessListener(this);
		((PseudoInputStream) in).destroy();
		try {
			((PseudoInputStream) in).close();
			((PseudoOutputStream) out).close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public InputStream getErrorStream() {
		return err;
	}
	public InputStream getInputStream() {
		return in;
	}
	public OutputStream getOutputStream() {
		return out;
	}
	public void processEvent(IProcessEvent event) {
		switch (event.getType()) {
		case IProcessEvent.STATUS_EXIT_TYPE:
			destroy();
			break;
		case IProcessEvent.ADD_OUTPUT_TYPE:
			((PseudoInputStream) in).printString(event.getInput());
			break;
		}
	}
	public void kill() throws DebugException {
		if (!finished) {
			try {
				debugger.stop(debugger.getSession().createBitList(getTargetID()));
			} catch (PCDIException e) {
				throw new DebugException(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), null));
			}
		}
	}
	public int getTargetID() {
		return pproc.getTaskId();
	}
	/***************************************************************************************************************************************************************************************************
	 * IProcess interface
	 **************************************************************************************************************************************************************************************************/
	private void initializeAttributes(Map attributes) {
		if (attributes != null) {
			Iterator keys = attributes.keySet().iterator();
			while (keys.hasNext()) {
				String key = (String) keys.next();
				setAttribute(key, (String) attributes.get(key));
			}
		}
	}
	public String getAttribute(String key) {
		if (fAttributes == null) {
			return null;
		}
		return (String) fAttributes.get(key);
	}
	public int getExitValue() throws DebugException {
		if (isTerminated()) {
			return fExitValue;
		}
		throw new DebugException(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), IStatus.ERROR, "Exit value not available", null));
	}
	public String getLabel() {
		return PROCESS_NAME + getTargetID();
	}
	public ILaunch getLaunch() {
		return launch;
	}
	public IStreamsProxy getStreamsProxy() {
		return fStreamsProxy;
	}
	protected void fireCreationEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.CREATE));
	}
	protected void fireEvent(DebugEvent event) {
		DebugPlugin manager = DebugPlugin.getDefault();
		if (manager != null) {
			manager.fireDebugEventSet(new DebugEvent[] { event });
		}
	}
	protected void fireTerminateEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.TERMINATE));
	}
	protected void fireChangeEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.CHANGE));
	}
	public void setAttribute(String key, String value) {
		if (fAttributes == null) {
			fAttributes = new HashMap(5);
		}
		Object origVal = fAttributes.get(key);
		if (origVal != null && origVal.equals(value)) {
			return;
		}
		fAttributes.put(key, value);
		fireChangeEvent();
	}
	public boolean canTerminate() {
		return !fTerminated;
	}
	public boolean isTerminated() {
		return fTerminated;
	}
	public void terminate() throws DebugException {
		if (!isTerminated()) {
			if (fStreamsProxy instanceof PStreamsProxy) {
				((PStreamsProxy) fStreamsProxy).kill();
			}
			kill();
			int attempts = 0;
			while (attempts < MAX_WAIT_FOR_DEATH_ATTEMPTS) {
				try {
					fExitValue = exitValue();
					return;
				} catch (IllegalThreadStateException ie) {
				}
				try {
					Thread.sleep(TIME_TO_WAIT_FOR_THREAD_DEATH);
				} catch (InterruptedException e) {
				}
				attempts++;
			}
			throw new DebugException(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), IStatus.ERROR, "Pseudo process termiante failed", null));
		}
	}
	protected void terminated() {
		if (fStreamsProxy instanceof PStreamsProxy) {
			((PStreamsProxy) fStreamsProxy).close();
		}
		fTerminated = true;
		try {
			fExitValue = exitValue();
		} catch (IllegalThreadStateException ie) {
		}
		fireTerminateEvent();
	}
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IProcess.class)) {
			return this;
		}
		if (adapter.equals(IDebugTarget.class)) {
			return ((IPLaunch) getLaunch()).getDebugTarget(getTargetID());
		}
		return null;
	}
}
