/******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.remotetools.environment.launcher.variables;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IDynamicVariableResolver;
import org.eclipse.ptp.remotetools.environment.launcher.RemoteLauncherPlugin;

/**
 * @author laggarcia
 * @since 3.0.0
 */
public class RemoteWorkspaceResolver implements IDynamicVariableResolver {

	protected static final String USER_WORKSPACE_VARIABLE = "user_workspace"; //$NON-NLS-1$

	protected static final String CONCATENATE_CHAR = "_"; //$NON-NLS-1$

	protected static final String DEFAULT_USER_WORKSPACE = "generic_user_workspace"; //$NON-NLS-1$

	public RemoteWorkspaceResolver() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.variables.IDynamicVariableResolver#resolveValue(org.eclipse.core.variables.IDynamicVariable,
	 *      java.lang.String)
	 */
	public String resolveValue(IDynamicVariable variable, String argument)
			throws CoreException {

		String variableName = variable.getName();

		if (variableName.equals(USER_WORKSPACE_VARIABLE)) {
			try {
				String hostname = InetAddress.getLocalHost().getHostName();
				String username = System.getProperty("user.name"); //$NON-NLS-1$
				return hostname + CONCATENATE_CHAR + username;
			} catch (UnknownHostException uhe) {
				uhe.printStackTrace();
				return DEFAULT_USER_WORKSPACE;
			}
		}
		throw new CoreException(new Status(IStatus.ERROR, RemoteLauncherPlugin
				.getUniqueIdentifier(), IStatus.OK,
				Messages.invalidEclipseVariableErrorMessage, null));

	}

}
