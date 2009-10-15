/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.miners;

import java.io.IOException;

import org.eclipse.cdt.internal.core.indexer.StandaloneFastIndexer;
import org.eclipse.cdt.internal.core.pdom.IndexerProgress;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.ptp.internal.rdt.core.Serializer;
import org.eclipse.ptp.internal.rdt.core.index.RemoteIndexerProgress;
import org.eclipse.rse.dstore.universal.miners.UniversalServerUtilities;

/**
 * A progress monitor that serialize indexer progress information.
 * @author vkong
 *
 */
public class RemoteIndexProgressMonitor implements IProgressMonitor {

	private String fTaskName, fSubTaskName;
	private int fTotalWork;
	private int fCurrentWorked;
	private boolean fIsCanceled;
	private DataElement fStatus;
	private StandaloneFastIndexer fIndexer;
	
	private DataStore _dataStore;
	private static final String CLASS_NAME = "CDTMiner-RemoteIndexProgressMonitor"; //$NON-NLS-1$
	
	public RemoteIndexProgressMonitor(StandaloneFastIndexer indexer, DataElement status, DataStore _dataStore) {
		fStatus = status;
		fIndexer = indexer;
		this._dataStore = _dataStore;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#beginTask(java.lang.String, int)
	 */
	public void beginTask(String name, int totalWork) {
		fTaskName = name;
		fTotalWork = totalWork;		
		serializeProgress("Starting task " + name); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#done()
	 */
	public void done() {
		serializeProgress("Done task " + fTaskName); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#internalWorked(double)
	 */
	public void internalWorked(double work) {
		fCurrentWorked += work;
		printProgress();

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#isCanceled()
	 */
	public boolean isCanceled() {
		return fIsCanceled;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#setCanceled(boolean)
	 */
	public void setCanceled(boolean value) {
		fIsCanceled = value;
		if (value == true) {
			serializeProgress("Task " + fTaskName + " has been cancelled"); //$NON-NLS-1$ //$NON-NLS-2$
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#setTaskName(java.lang.String)
	 */
	public void setTaskName(String name) {
		fTaskName = name;

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#subTask(java.lang.String)
	 */
	public void subTask(String name) {
		fSubTaskName = name;
		printProgress();

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#worked(int)
	 */
	public void worked(int work) {
		internalWorked(work);

	}
	
	private void serializeProgress(String attribute) {
		try {
			IndexerProgress info = fIndexer.getProgressInformation();
			RemoteIndexerProgress remoteInfo = new RemoteIndexerProgress(info);
			remoteInfo.status = attribute;

			String sInfo;
			sInfo = Serializer.serialize(remoteInfo);

			fStatus.setAttribute(DE.A_NAME, attribute);
			fStatus.getDataStore().createObject(fStatus, CDTMiner.T_INDEXER_PROGRESS_INFO, sInfo);
			
			fStatus.getDataStore().refresh(fStatus);
		} catch (IOException e) {
			UniversalServerUtilities.logError(CLASS_NAME, e.toString(), e, _dataStore);
			
		} 
	}
	
	private void printProgress() {	
		serializeProgress("Working... Task: " + fTaskName + " SubTask: "  + fSubTaskName + " Progress: " + fCurrentWorked + " of " + fTotalWork); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	}

}
