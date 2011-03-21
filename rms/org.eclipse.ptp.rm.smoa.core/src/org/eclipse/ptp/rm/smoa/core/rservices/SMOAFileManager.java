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

package org.eclipse.ptp.rm.smoa.core.rservices;

import java.net.URI;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ptp.remote.core.IRemoteFileManager;

import com.smoa.comp.sdk.SMOAStaging;

public class SMOAFileManager implements IRemoteFileManager {

	final private SMOAStaging staging;
	private final SMOAConnection connection;

	public SMOAFileManager(SMOAConnection c) {
		this.connection = c;
		staging = c.getSMOAStaging();
	}

	public String getDirectorySeparator() {
		return "/"; //$NON-NLS-1$
	}

	public SMOAFileStore getResource(String path) {
		return new SMOAFileStore(path, staging, connection, null);
	}

	public String toPath(URI uri) {
		// TODO Auto-generated method stub
		return null;
	}

	public URI toURI(IPath path) {
		// TODO Auto-generated method stub
		return null;
	}

	public URI toURI(String path) {
		// TODO Auto-generated method stub
		return null;
	}

}
