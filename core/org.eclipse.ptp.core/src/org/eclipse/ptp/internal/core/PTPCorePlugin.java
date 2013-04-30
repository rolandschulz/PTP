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
package org.eclipse.ptp.internal.core;

import java.util.HashSet;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.internal.core.messages.Messages;
import org.osgi.framework.BundleContext;

public class PTPCorePlugin extends Plugin {
	public static final String PLUGIN_ID = "org.eclipse.ptp.core"; //$NON-NLS-1$

	// The shared instance.
	private static PTPCorePlugin fPlugin;

	/**
	 * Returns the shared instance.
	 */
	public static PTPCorePlugin getDefault() {
		return fPlugin;
	}

	/**
	 * Generate a unique identifier
	 * 
	 * @return unique identifier string
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
	 * Create log entry from an IStatus
	 * 
	 * @param status
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	/**
	 * Create log entry from a string
	 * 
	 * @param msg
	 */
	public static void log(String msg) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, msg, null));
	}

	/**
	 * Create log entry from a Throwable
	 * 
	 * @param e
	 */
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, Messages.PTPCorePlugin_0, e));
	}

	/*
	 * Resource bundle
	 */
	private ResourceBundle resourceBundle;

	/**
	 * The constructor.
	 */
	public PTPCorePlugin() {
		super();
		fPlugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle(PLUGIN_ID + ".ParallelPluginResources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		setDefaultLaunchDelegates();
		ResourcesPlugin.getWorkspace().addSaveParticipant(getUniqueIdentifier(), new ISaveParticipant() {
			public void saving(ISaveContext saveContext) throws CoreException {
				Preferences.savePreferences(getUniqueIdentifier());
			}

			public void rollback(ISaveContext saveContext) {
			}

			public void prepareToSave(ISaveContext saveContext) throws CoreException {
			}

			public void doneSaving(ISaveContext saveContext) {
			}
		});
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		try {
			Preferences.savePreferences(getUniqueIdentifier());
			ResourcesPlugin.getWorkspace().removeSaveParticipant(getUniqueIdentifier());
		} finally {
			super.stop(context);
			fPlugin = null;
		}
	}

	private void setDefaultLaunchDelegates() {
		ILaunchManager launchMgr = DebugPlugin.getDefault().getLaunchManager();

		HashSet<String> debugSet = new HashSet<String>();
		debugSet.add(ILaunchManager.DEBUG_MODE);

		ILaunchConfigurationType localCfg = launchMgr
				.getLaunchConfigurationType(IPTPLaunchConfigurationConstants.LAUNCH_APP_TYPE_ID);
		if (localCfg != null) {
			try {
				if (localCfg.getPreferredDelegate(debugSet) == null) {
					ILaunchDelegate[] delegates = localCfg.getDelegates(debugSet);
					for (ILaunchDelegate delegate : delegates) {
						if (IPTPLaunchConfigurationConstants.PREFERRED_DEBUG_LAUNCH_DELEGATE.equals(delegate.getId())) {
							localCfg.setPreferredDelegate(debugSet, delegate);
							break;
						}
					}
				}
			} catch (CoreException e) {
			}

			HashSet<String> runSet = new HashSet<String>();
			runSet.add(ILaunchManager.RUN_MODE);

			try {
				if (localCfg.getPreferredDelegate(runSet) == null) {
					ILaunchDelegate[] delegates = localCfg.getDelegates(runSet);
					for (ILaunchDelegate delegate : delegates) {
						if (IPTPLaunchConfigurationConstants.PREFERRED_RUN_LAUNCH_DELEGATE.equals(delegate.getId())) {
							localCfg.setPreferredDelegate(runSet, delegate);
							break;
						}
					}
				}
			} catch (CoreException e) {
			}
		}
	}
}