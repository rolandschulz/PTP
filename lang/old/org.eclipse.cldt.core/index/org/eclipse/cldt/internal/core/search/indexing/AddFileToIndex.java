/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cldt.internal.core.search.indexing;

import java.io.IOException;

import org.eclipse.cldt.core.FortranCorePlugin;
import org.eclipse.cldt.core.ICLogConstants;
import org.eclipse.cldt.core.filetype.ICFileType;
import org.eclipse.cldt.internal.core.index.IIndex;
import org.eclipse.cldt.internal.core.search.processing.JobManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public abstract class AddFileToIndex extends IndexRequest {
	IFile resource;
	private boolean checkEncounteredHeaders;

	public AddFileToIndex(IFile resource, IPath indexPath, IndexManager manager, boolean checkEncounteredHeaders) {
		super(indexPath, manager);
		this.resource = resource;
		this.checkEncounteredHeaders = checkEncounteredHeaders;
	}
	
	public AddFileToIndex(IFile resource, IPath indexPath, IndexManager manager) {
		this(resource,indexPath,manager,false);
	}
	
	public boolean execute(IProgressMonitor progressMonitor) {
		if (progressMonitor != null && progressMonitor.isCanceled()) return true;
	
		if (checkEncounteredHeaders) {
			IProject resourceProject = resource.getProject();
			/* Check to see if this is a header file */ 
			ICFileType type = FortranCorePlugin.getDefault().getFileType(resourceProject,resource.getName());
			
			/* See if this file has been encountered before */
			if (type.isHeader() &&
				manager.haveEncounteredHeader(resourceProject.getFullPath(),resource.getLocation()))
				return true;
		}
		/* ensure no concurrent write access to index */
		IIndex index = manager.getIndex(this.indexPath, true, /*reuse index file*/ true /*create if none*/);
		if (index == null) return true;
		ReadWriteMonitor monitor = manager.getMonitorFor(index);
		if (monitor == null) return true; // index got deleted since acquired
		try {
			monitor.enterWrite(); // ask permission to write
			if (!indexDocument(index)) return false;
		} catch (IOException e) {
			org.eclipse.cldt.internal.core.model.Util.log(null, "Index I/O Exception: " + e.getMessage() + " on File: " + resource.getName(), ICLogConstants.CDT); //$NON-NLS-1$ //$NON-NLS-2$
			if (IndexManager.VERBOSE) {
				JobManager.verbose("-> failed to index " + this.resource + " because of the following exception:"); //$NON-NLS-1$ //$NON-NLS-2$
				e.printStackTrace();
			}
			return false;
		} finally {
			monitor.exitWrite(); // free write lock
		}
		return true;
	}
	
	protected abstract boolean indexDocument(IIndex index) throws IOException;
	
	public String toString() {
		return "indexing " + this.resource.getFullPath(); //$NON-NLS-1$
	}
}
