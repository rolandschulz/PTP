package org.eclipse.ptp.launch.internal;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.ptp.core.IModelListener;
import org.eclipse.ptp.core.IModelPresentation;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.events.IModelErrorEvent;
import org.eclipse.ptp.core.events.IModelEvent;
import org.eclipse.ptp.core.events.IModelRuntimeNotifierEvent;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.launch.PTPLaunchPlugin;
import org.eclipse.ptp.rmsystem.IResourceManager;

public class RuntimeProcess implements IProcess, IModelListener {
	private IPLaunch launch = null;
	private IPJob job = null;
	private Map fAttributes;
	private int fExitValue = -1;
	private boolean fTerminated = false;
	private IModelPresentation modelPresentation = null;
	
	public RuntimeProcess(IPLaunch launch, IPJob job, Map attributes) {
		this.launch = launch;
		this.job = job;		
		initializeAttributes(attributes);
		fTerminated = job.isAllStop();
		launch.addProcess(this);
		getModelPresentation().addModelListener(this);
		fireCreationEvent();
	}
	private IModelPresentation getModelPresentation() {
		if (modelPresentation == null)
			modelPresentation = PTPCorePlugin.getDefault().getModelPresentation();
			
		return modelPresentation;
	}	
	private void initializeAttributes(Map attributes) {
		if (attributes != null) {
			Iterator keys = attributes.keySet().iterator();
			while (keys.hasNext()) {
				String key = (String)keys.next();
				setAttribute(key, (String)attributes.get(key));
			}	
		}
	}	
	/***************************************************************************************************************************************************************************************************
	 * IProcess interface
	 **************************************************************************************************************************************************************************************************/
	public String getLabel() {
		return job.getName();
	}
	public ILaunch getLaunch() {
		return launch;
	}
	public IStreamsProxy getStreamsProxy() {
		return null;
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
		throw new DebugException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(), IStatus.ERROR, "Exit value not available", null));
	}
	protected void terminated() {
		fTerminated= true;
		fExitValue = 0;
		getModelPresentation().removeModelListener(this);
		fireTerminateEvent();
	}	
	/***************************************************************************************************************************************************************************************************
	 * ITerminate interface
	 **************************************************************************************************************************************************************************************************/
	public boolean canTerminate() {
		return !fTerminated;
	}
	public boolean isTerminated() {
		return fTerminated;
	}
	public void terminate() throws DebugException {
		if (!isTerminated()) {
			try {
				IResourceManager rm = job.getQueue().getResourceManager();
				rm.terminateJob(job);
			} catch (CoreException e) {
				throw new DebugException(e.getStatus());
			}
		}
	}
	/***************************************************************************************************************************************************************************************************
	 * Adapter interface
	 **************************************************************************************************************************************************************************************************/
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IProcess.class)) {
			return this;
		}
		if (adapter.equals(ILaunch.class)) {
			return getLaunch();
		}
		return null;
	}
	/***************************************************************************************************************************************************************************************************
	 * IModelListener interface
	 **************************************************************************************************************************************************************************************************/
	public void modelEvent(IModelEvent event) {
		if (event instanceof IModelErrorEvent) {
			terminated();
		}
		else if (event instanceof IModelRuntimeNotifierEvent) {
			IModelRuntimeNotifierEvent runtimeEvent = (IModelRuntimeNotifierEvent)event;
			switch (runtimeEvent.getStatus()) {
			case IModelRuntimeNotifierEvent.STOPPED:
				terminated();
				break;
			case IModelRuntimeNotifierEvent.ABORTED:
				terminated();
				break;
			}
		}
	}	
	/***************************************************************************************************************************************************************************************************
	 * Debug Event
	 **************************************************************************************************************************************************************************************************/
	protected void fireCreationEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.CREATE));
	}
	protected void fireEvent(DebugEvent event) {
		DebugPlugin manager= DebugPlugin.getDefault();
		if (manager != null) {
			manager.fireDebugEventSet(new DebugEvent[]{event});
		}
	}
	protected void fireTerminateEvent() {
		fireEvent(new DebugEvent(this, DebugEvent.TERMINATE));
	}
}
