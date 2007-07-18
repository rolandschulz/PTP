/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.remote;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public abstract class AbstractRemoteServicesFactory {
	protected static CoreException makeCoreException(String string) {
		IStatus status = new Status(Status.ERROR, PTPRemotePlugin.getUniqueIdentifier(),
				Status.ERROR, string, null);
		return new CoreException(status);
	}
	
	/**
	 * @param rm
	 * @return
	 */
	public IRemoteServicesDelegate create() {
		return doCreate();
	}

	/**
	 * @param rm
	 * @return
	 */
	protected abstract IRemoteServicesDelegate doCreate();
}
