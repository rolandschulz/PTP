/*******************************************************************************
 * Copyright (c) 2010 Poznan Supercomputing and Networking Center
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan Konczak (PSNC) - initial implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.smoa.ui.rservices;

import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.swt.widgets.Shell;

public class SMOAUIConnectionManager implements IRemoteUIConnectionManager {

	public IRemoteConnection newConnection(Shell shell) {
		// TODO Auto-generated method stub
		return null;
	}

	public IRemoteConnection newConnection(Shell shell, String[] attrHints, String[] attrHintValues) {
		// TODO Auto-generated method stub
		return null;
	}

	public void openConnectionWithProgress(Shell shell, IRunnableContext context, IRemoteConnection connection) {
		// TODO Auto-generated method stub

	}

	public void updateConnection(Shell shell, IRemoteConnection connection) {
		// TODO Auto-generated method stub

	}

}
