/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.cell.simulator.extensions;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;

/** 
 * Keeps all launch profiles that are registered by plugins.
 * @author Daniel Felix Ferber
 *
 */
public class LaunchProfileManager {
	static LaunchProfileManager instance;
	
	Map launchProfiles = new HashMap();

	public static LaunchProfileManager getInstance() {
		if (instance == null) {
			instance = new LaunchProfileManager();
		}
		return instance;
	}
	
	protected LaunchProfileManager() {
		super();
		loadProfileExtensions();
	}
	
	private void loadProfileExtensions() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint("org.eclipse.ptp.cell.simulator.profile"); //$NON-NLS-1$
		
		IExtension[] extensions = extensionPoint.getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			/*
			 * Retrieve information about the plug-ins bundle.
			 */
			IExtension extension = extensions[i];
			IContributor contributor = extension.getContributor();
			String contributorName = contributor.getName();
			Bundle bundle = Platform.getBundle(contributorName);
			ILog log = Platform.getLog(bundle);

			/*
			 * Transverse all configuration elements and retrieve information.
			 */
			IConfigurationElement elements [] = extension.getConfigurationElements();
			for (int j = 0; j < elements.length; j++) {
				String id = null;
				String name = null;
				String tclScriptPath = null;
				URL tclScriptURL = null;
				String deployPaths[] = null;
				URL deployURLs[] = null;
				
				try {
					/*
					 * Get attributes form an entry for architecture.
					 */
					IConfigurationElement element = elements[j];
					name = element.getAttribute("name"); //$NON-NLS-1$
					id = element.getAttribute("id"); //$NON-NLS-1$
					IConfigurationElement scriptElements[] = element.getChildren("script"); //$NON-NLS-1$
					IConfigurationElement fileElements[] = element.getChildren("file"); //$NON-NLS-1$
				
					/*
					 * Validate the attributes. If any rule fails, then the error is logged and the hole entry will be ignored.
					 */
					// Overall parameters
					if (name == null) throw new IllegalArgumentException(Messages.LaunchProfileManager_MissingProfileName);
					if (id == null) throw new IllegalArgumentException(Messages.LaunchProfileManager_MissingProfileID);
					id = contributorName + "." + id; //$NON-NLS-1$
					// TCL script
					if (scriptElements.length != 1) new IllegalArgumentException(Messages.LaunchProfileManager_ExactlyOneTCLScript);
					tclScriptPath = scriptElements[0].getAttribute("path"); //$NON-NLS-1$
					tclScriptURL = FileLocator.find(bundle, new Path(tclScriptPath), null);
					if (tclScriptURL == null) throw new IllegalArgumentException(NLS.bind(Messages.LaunchProfileManager_TCLScriptFileNotFound, tclScriptPath)); 
					try {
						tclScriptURL = FileLocator.resolve(tclScriptURL);
					} catch (IOException e) {
						throw new IllegalArgumentException(NLS.bind(Messages.LaunchProfileManager_TCLScriptFileNotResolved, tclScriptPath));
					}
					// List of files to be deployed
					Vector tmpDeployPaths = new Vector();
					Vector tmpDeployURLs = new Vector();
					for (int k = 0; k < fileElements.length; k++) {
						String path = fileElements[k].getAttribute("path"); //$NON-NLS-1$
						if (path == null) {
							log.log(new Status(IStatus.ERROR, contributorName, 0, Messages.LaunchProfileManager_EmptyFileNameEntry, null));
							continue;
						}
						URL url = FileLocator.find(bundle, new Path(path), null);
						if (url == null) throw new IllegalArgumentException(NLS.bind(Messages.LaunchProfileManager_DeployFileNotFound, tclScriptPath)); 
						try {
							url = FileLocator.resolve(url);
						} catch (IOException e) {
							throw new IllegalArgumentException(NLS.bind(Messages.LaunchProfileManager_DeployFileNotResolved, tclScriptPath));
						}
						tmpDeployPaths.add(path);
						tmpDeployURLs.add(url);
					}
					deployPaths = new String[tmpDeployPaths.size()];
					deployURLs = new URL[tmpDeployURLs.size()];
					deployPaths = (String[]) tmpDeployPaths.toArray(deployPaths);
					deployURLs = (URL[]) tmpDeployURLs.toArray(deployURLs);
				} catch (IllegalArgumentException e) {
					log.log(new Status(IStatus.ERROR, contributorName, 0, Messages.LaunchProfileManager_ExtensionPointError, e));
					continue;
				}

				/*
				 * Add an entry to the architecture manager.
				 */
				LaunchProfile launchProfile = new LaunchProfile(id, name, tclScriptPath, tclScriptURL, deployPaths, deployURLs);
				launchProfiles.put(launchProfile.getId(), launchProfile);
			}
		}		
	}

	public LaunchProfile [] getLaunchProfiles(){
		Collection set = launchProfiles.values();
		LaunchProfile result [] = new LaunchProfile [set.size()];
		result = (LaunchProfile[]) set.toArray(result);
		return result;
	}

	public LaunchProfile getLaunchProfile(String profileId) {
		return (LaunchProfile) launchProfiles.get(profileId);
	}
}
