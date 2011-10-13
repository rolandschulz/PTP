/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.remote.internal.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ptp.remote.core.IRemoteProject;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;

/**
 * Factory to create an IRemoteProject
 * 
 * @author greg
 * 
 */
public class RemoteProjectFactory {
	private final IConfigurationElement fConfigElement;

	public RemoteProjectFactory(IConfigurationElement ce) {
		fConfigElement = ce;
	}

	/**
	 * Get the remote project associated with the project.
	 * 
	 * @return IRemoteProject
	 */
	public IRemoteProject getRemoteProject(IProject project) {
		try {
			IRemoteProject remoteProj = (IRemoteProject) fConfigElement
					.createExecutableExtension(RemoteProjectAdapterFactory.ATTR_CLASS);
			remoteProj.setProject(project);
			return remoteProj;
		} catch (CoreException e) {
			PTPRemoteCorePlugin.log(e);
			return null;
		}
	}
}