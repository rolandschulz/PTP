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
 * Keeps all architectures that are registered by plugins.
 * @author Daniel Felix Ferber
 *
 */
public class ArchitectureManager {
	static ArchitectureManager instance;
	Map architectures = new HashMap();
	
	public static ArchitectureManager getInstance() {
		if (instance == null) {
			instance = new ArchitectureManager();
		}
		return instance;
	}
	
	protected ArchitectureManager() {
		loadArchitectureExtensions();
	}

	private void loadArchitectureExtensions() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint("org.eclipse.ptp.cell.simulator.architecture"); //$NON-NLS-1$
		
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
				try {
					/*
					 * Get attributes form an entry for architecture.
					 */
					IConfigurationElement element = elements[j];
					id = element.getAttribute("id"); //$NON-NLS-1$
					name = element.getAttribute("name"); //$NON-NLS-1$
					tclScriptPath = element.getAttribute("tclscript"); //$NON-NLS-1$

					/*
					 * Validate the attributes. If any rule fails, then the error is logged and the hole entry will be ignored.
					 */
					if (name == null) throw new IllegalArgumentException(Messages.ArchitectureManager_MissingArchitectureName);
					if (id == null) throw new IllegalArgumentException(Messages.ArchitectureManager_MissingArchitectureID);
					id = contributorName + "." + id; //$NON-NLS-1$
					if (tclScriptPath == null) throw new IllegalArgumentException(Messages.ArchitectureManager_MissingArchitectureTCLScript);
					tclScriptURL = FileLocator.find(bundle, new Path(tclScriptPath), null);
					if (tclScriptURL == null) throw new IllegalArgumentException(NLS.bind(Messages.ArchitectureManager_TCLScriptFileNotFound, tclScriptPath)); 
					try {
						tclScriptURL = FileLocator.resolve(tclScriptURL);
					} catch (IOException e) {
						throw new IllegalArgumentException(NLS.bind(Messages.ArchitectureManager_TCLScriptURLNotResolved, tclScriptPath));
					}
				} catch (IllegalArgumentException e) {
					log.log(new Status(IStatus.ERROR, contributorName, 0, Messages.ArchitectureManager_ExensionPointError, e));
					continue;
				}
				
				/*
				 * Add an entry to the architecture manager.
				 */
				Architecture architecture = new Architecture(id, name, tclScriptPath, tclScriptURL/*, contributor.getName()*/);
				architectures.put(architecture.getId(), architecture);
			}
		}
	}
	
	public Architecture [] getArchitectures(){
		Collection set = architectures.values();
		Architecture result [] = new Architecture [set.size()];
		result = (Architecture[]) set.toArray(result);
		return result;
	}

	public Architecture getArchitecture(String architectureId) {
		return (Architecture) architectures.get(architectureId);
	}
}
