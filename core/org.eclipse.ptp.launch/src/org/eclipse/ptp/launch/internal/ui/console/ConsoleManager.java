/**
 * Copyright (c) 2007 ORNL and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the term of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 
 * @author - Feiyi Wang
 * initial API and implementation
 * 
 */

package org.eclipse.ptp.launch.internal.ui.console;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.rmsystem.IResourceManagerControl;

public class ConsoleManager {

	private class DebugEventListener implements IDebugEventSetListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org
		 * .eclipse.debug.core.DebugEvent[])
		 */
		public void handleDebugEvents(DebugEvent[] events) {
			for (DebugEvent event : events) {
				if (event.getKind() == DebugEvent.CREATE) {
					IPLaunch launch = null;
					if (event.getSource() instanceof IPDebugTarget) {
						launch = (IPLaunch) ((IPDebugTarget) event.getSource()).getLaunch();
					} else if (event.getSource() instanceof IProcess) {
						IProcess p = (IProcess) event.getSource();
						if (p.getLaunch() instanceof IPLaunch) {
							launch = (IPLaunch) p.getLaunch();
						}
					}
					if (launch != null) {
						IResourceManagerControl rm = launch.getResourceManager();
						try {
							ILaunchConfiguration configuration = launch.getLaunchConfiguration();
							if (configuration != null
									&& configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_CONSOLE, false)) {
								JobConsole jc = new JobConsole(rm, launch.getJobId());
								synchronized (consoles) {
									consoles.put(launch.getJobId(), jc);
								}
							}
						} catch (CoreException e) {
						}
					}
				}
			}
		}
	}

	private final Map<String, JobConsole> consoles = new HashMap<String, JobConsole>();
	private final IDebugEventSetListener fEventListener = new DebugEventListener();

	private static ConsoleManager fInstance;

	public static ConsoleManager getInstance() {
		if (fInstance == null) {
			fInstance = new ConsoleManager();
		}
		return fInstance;
	}

	private ConsoleManager() {
	}

	/**
	 * Start listening for events
	 */
	public void start() {
		DebugPlugin.getDefault().addDebugEventListener(fEventListener);
	}

	/**
	 * Shut down the console manager. This removes any job consoles that have
	 * been created and all listeners.
	 */
	public void stop() {
		DebugPlugin.getDefault().removeDebugEventListener(fEventListener);
		synchronized (consoles) {
			for (JobConsole jc : consoles.values()) {
				jc.removeConsole();
			}
			consoles.clear();
		}
	}
}
