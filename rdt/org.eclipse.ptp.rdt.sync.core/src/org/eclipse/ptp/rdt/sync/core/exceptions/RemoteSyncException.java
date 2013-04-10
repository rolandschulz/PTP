/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core.exceptions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

// An exception to wrap exceptions for which the Remote Sync services cannot recover.
public class RemoteSyncException extends CoreException {
	private static final String pluginID = "org.eclipse.ptp.rdt.sync.git.core"; //$NON-NLS-1$
	private static final long serialVersionUID = 1L;

	public RemoteSyncException(String arg0) {
		super(new Status(IStatus.ERROR, pluginID, arg0));
	}

	public RemoteSyncException(String arg0, Throwable arg1) {
		super(new Status(IStatus.ERROR, pluginID, arg0, arg1));

	}

	public RemoteSyncException(Throwable arg0) {
		super(new Status(IStatus.ERROR, pluginID, (arg0==null ? null : arg0.toString()), arg0));
	}

	public RemoteSyncException(Status status) {
		super(status);
	}
}
