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
import java.util.List;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
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
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.debug.core.IPDebugConfiguration;
import org.eclipse.ptp.debug.core.IPDebugConstants;
import org.eclipse.ptp.debug.core.IPDebugEventListener;
import org.eclipse.ptp.debug.core.event.IPDebugEvent;
import org.eclipse.ptp.internal.debug.core.messages.Messages;
import org.eclipse.ptp.internal.debug.core.sourcelookup.CommonSourceLookupDirector;
import org.eclipse.ptp.internal.debug.core.sourcelookup.IPSourceLocation;
import org.eclipse.ptp.internal.debug.core.sourcelookup.PSourceLookupDirector;
import org.eclipse.ptp.internal.debug.core.sourcelookup.SourceUtils;
import org.osgi.framework.BundleContext;

public class PTPDebugCorePlugin extends Plugin {
	private class EventDispatchJob extends Job {
		private final EventNotifier fNotifier = new EventNotifier();

		/**
		 * Creates a new event dispatch job.
		 */
		public EventDispatchJob() {
			super("EventDispatchJob"); //$NON-NLS-1$
			setPriority(Job.INTERACTIVE);
			setSystem(true);
		}

		@Override
		public boolean shouldRun() {
			return shouldSchedule();
		}

		@Override
		public boolean shouldSchedule() {
			return !(isShuttingDown() || fEventListeners.isEmpty());
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			while (!fEventQueue.isEmpty()) {
				IPDebugEvent event = null;
				synchronized (fEventQueue) {
					if (!fEventQueue.isEmpty()) {
						event = fEventQueue.remove(0);
					}
				}
				if (event != null) {
					fNotifier.dispatch(event);
				}
			}
			return Status.OK_STATUS;
		}
	}

	private class EventNotifier implements ISafeRunnable {
		private IPDebugEvent fEvent;
		private IPDebugEventListener fListener;

		public void dispatch(IPDebugEvent event) {
			fEvent = event;
			try {
				setDispatching(true);
				Object[] listeners = getEventListeners();
				for (Object listener : listeners) {
					fListener = (IPDebugEventListener) listener;
					SafeRunner.run(this);
				}

			} finally {
				setDispatching(false);
			}
			fEvent = null;
			fListener = null;
		}

		@Override
		public void handleException(Throwable exception) {
			log(new Status(IStatus.ERROR, getUniqueIdentifier(), INTERNAL_ERROR, Messages.PTPDebugCorePlugin_0, exception));
		}

		@Override
		public void run() throws Exception {
			fListener.handleDebugEvent(fEvent);
		}
	}

	public static final String PLUGIN_ID = "org.eclipse.ptp.debug.core"; //$NON-NLS-1$

	public static final int INTERNAL_ERROR = 1000;

	/**
	 * Constant identifying the job family identifier for the background event
	 * job.
	 */
	public static final Object FAMILY_EVENT = new Object();

	public static final String PDEBUGGER_EXTENSION_POINT_ID = "parallelDebuggers"; //$NON-NLS-1$
	public static final String DEBUGGER_ELEMENT = "debugger"; //$NON-NLS-1$

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
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), INTERNAL_ERROR, Messages.PTPDebugCorePlugin_1, top));
	}

	private HashMap<String, PDebugConfiguration> fDebugConfigurations;
	private CommonSourceLookupDirector fCommonSourceLookupDirector;
	private final EventDispatchJob dispatchJob = new EventDispatchJob();
	private final ListenerList fEventListeners = new ListenerList();
	private final List<IPDebugEvent> fEventQueue = new ArrayList<IPDebugEvent>();
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
		if (isShuttingDown() || event == null || fEventListeners.isEmpty()) {
			return;
		}
		synchronized (fEventQueue) {
			fEventQueue.add(event);
		}
		dispatchJob.schedule();
	}

	/**
	 * @return
	 */
	public int getCommandTimeout() {
		return Preferences.getInt(getUniqueIdentifier(), IPDebugConstants.PREF_DEBUG_COMM_TIMEOUT);
	}

	/**
	 * @return
	 */
	public IPSourceLocation[] getCommonSourceLocations() {
		return SourceUtils.getCommonSourceLocationsFromMemento(Preferences.getString(getUniqueIdentifier(),
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
		IPDebugConfiguration dbgCfg = fDebugConfigurations.get(id);
		if (dbgCfg == null) {
			IStatus status = new Status(IStatus.ERROR, getUniqueIdentifier(), 100, Messages.PTPDebugCorePlugin_2, null);
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
		return fDebugConfigurations.values().toArray(new IPDebugConfiguration[0]);
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
	 * Sets whether this plug-in is in the process of being shutdown.
	 * 
	 * @param value
	 *            whether this plug-in is in the process of being shutdown
	 */
	public void setShuttingDown(boolean value) {
		fShuttingDown = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		PDebugOptions.configure(context);
		ResourcesPlugin.getWorkspace().addSaveParticipant(getUniqueIdentifier(), new ISaveParticipant() {
			@Override
			public void saving(ISaveContext saveContext) throws CoreException {
				Preferences.savePreferences(getUniqueIdentifier());
			}

			@Override
			public void rollback(ISaveContext saveContext) {
			}

			@Override
			public void prepareToSave(ISaveContext saveContext) throws CoreException {
			}

			@Override
			public void doneSaving(ISaveContext saveContext) {
			}
		});
		debugModel = new PDebugModel();
		initializeCommonSourceLookupDirector();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			setShuttingDown(true);
			debugModel.shutdown();
			disposeCommonSourceLookupDirector();
			disposeDebugConfigurations();
			DebugJobStorage.removeDebugStorages();
			Preferences.savePreferences(getUniqueIdentifier());
			ResourcesPlugin.getWorkspace().removeSaveParticipant(getUniqueIdentifier());
		} finally {
			super.stop(context);
		}
	}

	/**
	 * 
	 */
	private void disposeCommonSourceLookupDirector() {
		if (fCommonSourceLookupDirector != null) {
			fCommonSourceLookupDirector.dispose();
		}
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
			String newMemento = Preferences.getString(getUniqueIdentifier(), IPDebugConstants.PREF_COMMON_SOURCE_CONTAINERS);
			if (newMemento.length() == 0) {
				// Convert source locations to source containers
				fCommonSourceLookupDirector.setSourceContainers(SourceUtils.convertSourceLocations(getCommonSourceLocations()));
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
			for (IConfigurationElement configurationElement : infos) {
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
