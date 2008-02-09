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
package org.eclipse.ptp.debug.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.ptp.debug.core.event.IPDebugEvent;
import org.eclipse.ptp.debug.core.sourcelookup.IPSourceLocation;
import org.eclipse.ptp.debug.core.sourcelookup.PSourceLookupDirector;
import org.eclipse.ptp.debug.internal.core.PDebugConfiguration;
import org.eclipse.ptp.debug.internal.core.sourcelookup.CommonSourceLookupDirector;
import org.eclipse.ptp.debug.internal.core.sourcelookup.SourceUtils;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class PTPDebugCorePlugin extends AbstractUIPlugin {
	class EventDispatchJob extends Job {
		private EventNotifier fNotifier = new EventNotifier();

		/**
		 * Creates a new event dispatch job.
		 */
		public EventDispatchJob() {
			super("EventDispatchJob");
			setPriority(Job.INTERACTIVE);
			setSystem(true);
		}
		
		public boolean shouldRun() {
			return shouldSchedule();
		}

		public boolean shouldSchedule() {
			return !(isShuttingDown() || fEventListeners.isEmpty());
		}

		protected IStatus run(IProgressMonitor monitor) {
			while (!fEventQueue.isEmpty()) {
				IPDebugEvent event = null;
				synchronized (fEventQueue) {
					if (!fEventQueue.isEmpty()) {
						event = (IPDebugEvent) fEventQueue.remove(0);
					}
				}
				if (event != null) {
					fNotifier.dispatch(event);
				}
			}
			return Status.OK_STATUS;
		}
	}
	
	class EventNotifier implements ISafeRunnable {
		private IPDebugEvent fEvent;
		private IPDebugEventListener fListener;

		public void dispatch(IPDebugEvent event) {
			fEvent = event;
			try {
				setDispatching(true);
				Object[] listeners = getEventListeners();
				for (int i = 0; i < listeners.length; i++) {
					fListener = (IPDebugEventListener) listeners[i];
					SafeRunner.run(this);
				}

			} finally {
				setDispatching(false);
			}
			fEvent = null;
			fListener = null;
		}

		public void handleException(Throwable exception) {
			log(new Status(IStatus.ERROR, getUniqueIdentifier(), INTERNAL_ERROR,
					"PTPDebugCorePlugin occurred exception while dispacthing debug event", exception));
		}

		public void run() throws Exception {
			fListener.handleDebugEvent(fEvent);
		}
	}
	
	public static final String PLUGIN_ID = "org.eclipse.ptp.debug.core";
	
	public static final int INTERNAL_ERROR = 1000;
	
	/**
	 * Constant identifying the job family identifier for the background event
	 * job.
	 */
	public static final Object FAMILY_EVENT = new Object();
	
	public static final String PDEBUGGER_EXTENSION_POINT_ID = "PDebugger";
	public static final String DEBUGGER_ELEMENT = "debugger";

	private static PTPDebugCorePlugin plugin;
	private static PDebugModel debugModel = null;

	/**
	 * @return
	 */
	public static PDebugModel getDebugModel() {
		return debugModel;
	}
	
	/**
	 * @return
	 */
	public static PTPDebugCorePlugin getDefault() {
		return plugin;
	}

	/**
	 * @return
	 */
	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return PLUGIN_ID;
		}
		return getDefault().getBundle().getSymbolicName();
	}
	
	/**
	 * @return
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
	
	/**
	 * @param status
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	
	/**
	 * @param message
	 */
	public static void log(String message) {
		getDefault().getLog().log(new Status(IStatus.ERROR, getUniqueIdentifier(), INTERNAL_ERROR, message, null));
	}
	
	/**
	 * @param t
	 */
	public static void log(Throwable t) {
		Throwable top = t;
		if (t instanceof DebugException) {
			DebugException de = (DebugException) t;
			IStatus status = de.getStatus();
			if (status.getException() != null) {
				top = status.getException();
			}
		}
		// this message is intentionally not internationalized, as an exception
		// may
		// be due to the resource bundle itself
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), INTERNAL_ERROR, "Internal error logged from PDI Debug: ", top));
	}

	private HashMap<String, PDebugConfiguration> fDebugConfigurations;
	private CommonSourceLookupDirector fCommonSourceLookupDirector;
	private EventDispatchJob dispatchJob = new EventDispatchJob();
	private ListenerList fEventListeners = new ListenerList();
	private List<IPDebugEvent> fEventQueue = new ArrayList<IPDebugEvent>();
	private boolean fShuttingDown = false;
	private int fDispatching = 0;

	public PTPDebugCorePlugin() {
		super();
		plugin = this;
	}

	/**
	 * @param listener
	 */
	public void addDebugEventListener(IPDebugEventListener listener) {
		fEventListeners.add(listener);
	}

	/**
	 * @param event
	 */
	public void fireDebugEvent(IPDebugEvent event) {
		if (isShuttingDown() || event == null || fEventListeners.isEmpty())
			return;
		synchronized (fEventQueue) {
			fEventQueue.add(event);
		}
		dispatchJob.schedule();
	}

	/**
	 * @return
	 */
	public int getCommandTimeout() {
		return getPluginPreferences().getInt(IPDebugConstants.PREF_PTP_DEBUG_COMM_TIMEOUT);
	}

	/**
	 * @return
	 */
	public IPSourceLocation[] getCommonSourceLocations() {
		return SourceUtils.getCommonSourceLocationsFromMemento(getPluginPreferences().getString(
				IPDebugConstants.PREF_SOURCE_LOCATIONS));
	}

	/**
	 * @return
	 */
	public PSourceLookupDirector getCommonSourceLookupDirector() {
		return fCommonSourceLookupDirector;
	}

	/**
	 * @param id
	 * @return
	 * @throws CoreException
	 */
	public IPDebugConfiguration getDebugConfiguration(String id) throws CoreException {
		if (fDebugConfigurations == null) {
			initializeDebugConfiguration();
		}
		IPDebugConfiguration dbgCfg = (IPDebugConfiguration) fDebugConfigurations.get(id);
		if (dbgCfg == null) {
			IStatus status = new Status(IStatus.ERROR, getUniqueIdentifier(), 100, "PTPDebugCorePlugin Debug Configuration Error",
					null);
			throw new CoreException(status);
		}
		return dbgCfg;
	}

	/**
	 * @return
	 */
	public IPDebugConfiguration[] getDebugConfigurations() {
		if (fDebugConfigurations == null) {
			initializeDebugConfiguration();
		}
		return (IPDebugConfiguration[]) fDebugConfigurations.values().toArray(new IPDebugConfiguration[0]);
	}

	/**
	 * @return
	 */
	public IPDebugConfiguration getDefaultDebugConfiguration() {
		IPDebugConfiguration result = null;
		try {
			result = getDebugConfiguration(getPluginPreferences().getString(
					IPDebugConstants.PREF_DEFAULT_DEBUGGER_TYPE));
		} catch (CoreException e) {
		}
		return result;
	}

	public boolean isDefaultDebugConfiguration(String id) {
		return (id.compareTo(getPluginPreferences().getString(
				IPDebugConstants.PREF_DEFAULT_DEBUGGER_TYPE)) == 0);
	}

	/**
	 * Returns whether debug events are being dispatched
	 */
	public synchronized boolean isDispatching() {
		return fDispatching > 0;
	}

	/**
	 * Returns whether this plug-in is in the process of being shutdown.
	 * 
	 * @return whether this plug-in is in the process of being shutdown
	 */
	public boolean isShuttingDown() {
		return fShuttingDown;
	}

	/**
	 * @param listener
	 */
	public void removeDebugEventListener(IPDebugEventListener listener) {
		fEventListeners.remove(listener);
	}

	/**
	 * @param locations
	 */
	public void saveCommonSourceLocations(IPSourceLocation[] locations) {
		getPluginPreferences().setValue(IPDebugConstants.PREF_SOURCE_LOCATIONS,
				SourceUtils.getCommonSourceLocationsMemento(locations));
	}

	public void saveDefaultDebugConfiguration(String id) {
		getPluginPreferences().setValue(IPDebugConstants.PREF_DEFAULT_DEBUGGER_TYPE,
				(id != null) ? id : "");
	}

	/**
	 * Sets whether this plug-in is in the process of being shutdown.
	 * 
	 * @param value
	 *            whether this plug-in is in the process of being shutdown
	 */
	public void setShuttingDown(boolean value) {
		fShuttingDown = value;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		debugModel = new PDebugModel();
		initializeCommonSourceLookupDirector();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		try {
			setShuttingDown(true);
			debugModel.shutdown();
			disposeCommonSourceLookupDirector();
			disposeDebugConfigurations();
			DebugJobStorage.removeDebugStorages();
		} finally {
			super.stop(context);
		}
	}

	/**
	 * @param director
	 */
	private void convertSourceLocations(CommonSourceLookupDirector director) {
		director.setSourceContainers(SourceUtils.convertSourceLocations(getCommonSourceLocations()));
	}

	/**
	 * 
	 */
	private void disposeCommonSourceLookupDirector() {
		if (fCommonSourceLookupDirector != null)
			fCommonSourceLookupDirector.dispose();
	}

	private void disposeDebugConfigurations() {
		if (fDebugConfigurations != null) {
			fDebugConfigurations.clear();
			fDebugConfigurations = null;
		}
	}

	/**
	 * @return
	 */
	private Object[] getEventListeners() {
		return fEventListeners.getListeners();
	}

	/**
	 * 
	 */
	private void initializeCommonSourceLookupDirector() {
		if (fCommonSourceLookupDirector == null) {
			fCommonSourceLookupDirector = new CommonSourceLookupDirector();
			String newMemento = getPluginPreferences().getString(
					IPDebugConstants.PREF_COMMON_SOURCE_CONTAINERS);
			if (newMemento.length() == 0) {
				// Convert source locations to source containers
				convertSourceLocations(fCommonSourceLookupDirector);
			} else {
				try {
					fCommonSourceLookupDirector.initializeFromMemento(newMemento);
				} catch (CoreException e) {
					log(e.getStatus());
				}
			}
		}
	}

	/**
	 * 
	 */
	private void initializeDebugConfiguration() {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(getUniqueIdentifier(),
				PDEBUGGER_EXTENSION_POINT_ID);
		if (extensionPoint != null) {
			IConfigurationElement[] infos = extensionPoint.getConfigurationElements();
			fDebugConfigurations = new HashMap<String, PDebugConfiguration>(infos.length);
			for (int i = 0; i < infos.length; i++) {
				IConfigurationElement configurationElement = infos[i];
				PDebugConfiguration configType = new PDebugConfiguration(configurationElement);
				fDebugConfigurations.put(configType.getID(), configType);
			}
		}
	}

	/**
	 * @param dispatching
	 */
	private synchronized void setDispatching(boolean dispatching) {
		if (dispatching) {
			fDispatching++;
		} else {
			fDispatching--;
		}
	}
}
