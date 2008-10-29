/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.launch.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;


public class DownloadBackRule implements ISynchronizationRule {
	List<IPath> remoteFiles = new ArrayList<IPath>();
	List<File> localFiles = new ArrayList<File>();
	
	public DownloadBackRule() {
	}
	
	public void add(File local, IPath remote) {
		remoteFiles.add(remote);
		localFiles.add(local);
	}
	
	public File getLocalFile(int index) {
		return (File) localFiles.get(index);
	}
		
	public IPath getRemoteFile(int index) {
		return (IPath) remoteFiles.get(index);
	}
	
	public int count() {
		return remoteFiles.size();
	}

	public boolean isActive() {
		return remoteFiles.size() > 0;
	}

	public boolean isDownloadRule() {
		return true; // by definition
	}

	public boolean isUploadRule() {
		return false; // by definition
	}

	public String toLabel() {
		return "Internal download back rule"; //$NON-NLS-1$
	}

	public void validate() throws CoreException {
		// nothing to validate
	}

}
