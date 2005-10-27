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
/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/
package org.eclipse.ptp.debug.core;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.eclipse.cdt.debug.core.ICBreakpointListener;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.internal.core.IPDebugInternalConstants;
import org.eclipse.ptp.debug.internal.core.ListenerList;
import org.eclipse.ptp.debug.internal.core.PDebugConfiguration;
import org.eclipse.ptp.debug.internal.core.SessionManager;
import org.eclipse.ptp.debug.internal.core.breakpoint.PBreakpoint;
import org.eclipse.ptp.debug.internal.core.sourcelookup.CSourceLookupDirector;
import org.eclipse.ptp.debug.internal.core.sourcelookup.CommonSourceLookupDirector;
import org.eclipse.ptp.debug.internal.core.sourcelookup.SourceUtils;
import org.osgi.framework.BundleContext;

/**
 * The plugin class for C/C++ debug core.
 */
public class PTPDebugCorePlugin extends Plugin {
	/**
	 * The plug-in identifier (value <code>"org.eclipse.ptp.debug.core"</code>).
	 */
	public static final String PLUGIN_ID = "org.eclipse.ptp.debug.core"; //$NON-NLS-1$
	/**
	 * Status code indicating an unexpected internal error.
	 */
	public static final int INTERNAL_ERROR = 1000;
	/**
	 * The shared instance.
	 */
	private static PTPDebugCorePlugin plugin;
	private HashMap fDebugConfigurations;
	private static Logger logger;
	private Level loggingLevel = Level.FINE;
	/**
	 * Breakpoint listener list.
	 */
	private ListenerList fBreakpointListeners;
	/**
	 * Dummy source lookup director needed to manage common source containers.
	 */
	private CommonSourceLookupDirector fCommonSourceLookupDirector;
	private SessionManager fSessionManager = null;
	private Hashtable fDebugSessions = null;
	private ListenerList fDebugSessionListeners;

	/**
	 * The constructor.
	 */
	public PTPDebugCorePlugin() {
		super();
		plugin = this;
	}
	/**
	 * Returns the shared instance.
	 * 
	 * @return the shared instance
	 */
	public static PTPDebugCorePlugin getDefault() {
		return plugin;
	}
	/**
	 * Returns the workspace instance.
	 * 
	 * @return the workspace instance
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 * 
	 * @return the unique identifier of this plugin
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
	 * Logs the specified throwable with this plug-in's log.
	 * 
	 * @param t
	 *            throwable to log
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
		// this message is intentionally not internationalized, as an exception may
		// be due to the resource bundle itself
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), INTERNAL_ERROR, "Internal error logged from CDI Debug: ", top)); //$NON-NLS-1$		
	}
	/**
	 * Logs the specified status with this plug-in's log.
	 * 
	 * @param status
	 *            status to log
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}
	/**
	 * Logs the specified message with this plug-in's log.
	 * 
	 * @param status
	 *            status to log
	 */
	public static void log(String message) {
		getDefault().getLog().log(new Status(IStatus.ERROR, PCDIDebugModel.getDefault().getPluginIdentifier(), INTERNAL_ERROR, message, null));
	}
	private void initializeDebugConfiguration() {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(getUniqueIdentifier(), "PTPDebugger"); //$NON-NLS-1$
		IConfigurationElement[] infos = extensionPoint.getConfigurationElements();
		fDebugConfigurations = new HashMap(infos.length);
		for (int i = 0; i < infos.length; i++) {
			IConfigurationElement configurationElement = infos[i];
			PDebugConfiguration configType = new PDebugConfiguration(configurationElement);
			fDebugConfigurations.put(configType.getID(), configType);
		}
	}
	public IPDebugConfiguration[] getDebugConfigurations() {
		if (fDebugConfigurations == null) {
			initializeDebugConfiguration();
		}
		return (IPDebugConfiguration[]) fDebugConfigurations.values().toArray(new IPDebugConfiguration[0]);
	}
	public IPDebugConfiguration getDebugConfiguration(String id) throws CoreException {
		if (fDebugConfigurations == null) {
			initializeDebugConfiguration();
		}
		IPDebugConfiguration dbgCfg = (IPDebugConfiguration) fDebugConfigurations.get(id);
		if (dbgCfg == null) {
			IStatus status = new Status(IStatus.ERROR, getUniqueIdentifier(), 100, DebugCoreMessages.getString("CDebugCorePlugin.0"), null); //$NON-NLS-1$
			throw new CoreException(status);
		}
		return dbgCfg;
	}
	protected void resetBreakpointsInstallCount() {
		IBreakpointManager bm = DebugPlugin.getDefault().getBreakpointManager();
		IBreakpoint[] breakpoints = bm.getBreakpoints(getUniqueIdentifier());
		for (int i = 0; i < breakpoints.length; ++i) {
			if (breakpoints[i] instanceof PBreakpoint) {
				try {
					((PBreakpoint) breakpoints[i]).resetInstallCount();
				} catch (CoreException e) {
					log(e.getStatus());
				}
			}
		}
	}
	protected SessionManager getSessionManager() {
		return fSessionManager;
	}
	protected void setSessionManager(SessionManager sm) {
		if (fSessionManager != null)
			fSessionManager.dispose();
		fSessionManager = sm;
	}
	/**
	 * Adds the given breakpoint listener to the debug model.
	 * 
	 * @param listener
	 *            breakpoint listener
	 */
	public void addCBreakpointListener(ICBreakpointListener listener) {
		fBreakpointListeners.add(listener);
	}
	/**
	 * Removes the given breakpoint listener from the debug model.
	 * 
	 * @param listener
	 *            breakpoint listener
	 */
	public void removeCBreakpointListener(ICBreakpointListener listener) {
		fBreakpointListeners.remove(listener);
	}
	/**
	 * Returns the list of breakpoint listeners registered with this plugin.
	 * 
	 * @return the list of breakpoint listeners registered with this plugin
	 */
	public Object[] getCBreakpointListeners() {
		return fBreakpointListeners.getListeners();
	}
	private void createBreakpointListenersList() {
		fBreakpointListeners = new ListenerList(1);
	}
	private void disposeBreakpointListenersList() {
		fBreakpointListeners.removeAll();
		fBreakpointListeners = null;
	}
	/* Debug Session Listeners */
	public void addDebugSessionListener(IPDebugListener listener) {
		fDebugSessionListeners.add(listener);
	}
	public void removeDebugSessionListener(IPDebugListener listener) {
		fDebugSessionListeners.remove(listener);
	}
	private void createDebugSessionListenersList() {
		fDebugSessionListeners = new ListenerList(1);
	}
	private void disposeDebugSessionListenersList() {
		fDebugSessionListeners.removeAll();
		fDebugSessionListeners = null;
	}
	public void addDebugSession(IPJob job, IPCDISession session) {
		fDebugSessions.put(job, session);
		Object[] listeners = fDebugSessionListeners.getListeners();
		for (int i = 0; i < listeners.length; ++i)
			((IPDebugListener) listeners[i]).startSession((IPCDISession) session);
	}
	public IPCDISession getDebugSession(IPJob job) {
		return (IPCDISession) fDebugSessions.get(job);
	}
	public void removeDebugSession(IPJob job) {
		IPCDISession session = (IPCDISession)fDebugSessions.remove(job);
		Object[] listeners = fDebugSessionListeners.getListeners();
		for (int i = 0; i < listeners.length; ++i)
			((IPDebugListener) listeners[i]).endSession((IPCDISession) session);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		initializeCommonSourceLookupDirector();
		fDebugSessions = new Hashtable();
		createBreakpointListenersList();
		createDebugSessionListenersList();
		resetBreakpointsInstallCount();
		setSessionManager(new SessionManager());
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		setSessionManager(null);
		disposeDebugSessionListenersList();
		disposeBreakpointListenersList();
		resetBreakpointsInstallCount();
		fDebugSessions.clear();
		disposeCommonSourceLookupDirector();
		super.stop(context);
	}
	private void initializeCommonSourceLookupDirector() {
		if (fCommonSourceLookupDirector == null) {
			fCommonSourceLookupDirector = new CommonSourceLookupDirector();
			String newMemento = PTPDebugCorePlugin.getDefault().getPluginPreferences().getString(IPDebugInternalConstants.PREF_COMMON_SOURCE_CONTAINERS);
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
	private void disposeCommonSourceLookupDirector() {
		if (fCommonSourceLookupDirector != null)
			fCommonSourceLookupDirector.dispose();
	}
	public CSourceLookupDirector getCommonSourceLookupDirector() {
		return fCommonSourceLookupDirector;
	}
	private void convertSourceLocations(CommonSourceLookupDirector director) {
		director.setSourceContainers(SourceUtils.convertSourceLocations(getCommonSourceLocations()));
	}
	public ICSourceLocation[] getCommonSourceLocations() {
		return SourceUtils.getCommonSourceLocationsFromMemento(PTPDebugCorePlugin.getDefault().getPluginPreferences().getString(IPDebugConstants.PREF_SOURCE_LOCATIONS));
	}
	public Logger getLogger() {
		if (logger == null) {
			logger = Logger.getLogger(getClass().getName());
			Handler[] handlers = logger.getHandlers();
			for (int index = 0; index < handlers.length; index++) {
				logger.removeHandler(handlers[index]);
			}
			Handler console = new ConsoleHandler();
			console.setFormatter(new Formatter() {
				public String format(LogRecord record) {
					String out = record.getLevel() + " : " + record.getSourceClassName() + "." + record.getSourceMethodName();
					if (!record.getMessage().equals(""))
						out = out + " : " + record.getMessage();
					out += "\n";
					return out;
				}
			});
			console.setLevel(loggingLevel);
			logger.addHandler(console);
			logger.setLevel(loggingLevel);
		}
		return logger;
	}
}