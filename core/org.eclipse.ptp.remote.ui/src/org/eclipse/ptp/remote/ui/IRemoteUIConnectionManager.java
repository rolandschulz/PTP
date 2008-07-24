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
package org.eclipse.ptp.remote.ui;

import org.eclipse.swt.widgets.Shell;


public interface IRemoteUIConnectionManager {	
	/**
	 * Create a new connection. The implementation can chose to do this in any way,
	 * but typically will use a dialog or wizard.
	 */
	public void newConnection(Shell shell);
}
