/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.remotetools.environment.launcher.internal;

import java.util.Enumeration;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ptp.remotetools.environment.launcher.RemoteLauncherPlugin;
import org.eclipse.ptp.remotetools.environment.launcher.core.ILaunchObserver;
import org.eclipse.ptp.utils.core.extensionpoints.ExtensionPointEnumeration;

public class LaunchObserverIterator implements Enumeration {
	
	ExtensionPointEnumeration enumeration = new ExtensionPointEnumeration(RemoteLauncherPlugin.OBERVER_EXTENSION_ID);
	IConfigurationElement current;
	
	public LaunchObserverIterator() {
	}
	
	public String getID() {
		if (current == null) return null;
		return current.getAttribute("id"); //$NON-NLS-1$
	}
	
	public String getName() {
		if (current == null) return null;
		return current.getAttribute("name");		 //$NON-NLS-1$
	}
	
	public ILaunchObserver getInstance() {
		if (current == null) return null;
		try {
			return (ILaunchObserver) current.createExecutableExtension("class"); //$NON-NLS-1$
		} catch (CoreException e) {
			return null;
		}
	}


	public boolean hasMoreElements() {
		return enumeration.hasMoreElements();
	}


	public Object nextElement() {
		current = (IConfigurationElement) enumeration.nextElement();
		return current;
	}
}
