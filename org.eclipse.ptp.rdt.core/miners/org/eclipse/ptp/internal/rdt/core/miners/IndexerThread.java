/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.rdt.core.miners;

import java.io.IOException;
import java.util.List;

import org.eclipse.cdt.internal.core.indexer.StandaloneFastIndexer;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.rse.dstore.universal.miners.UniversalServerUtilities;


public abstract class IndexerThread extends Thread {
	
	private static final String LOG_TAG = "CDTMiner - IndexerThread"; //$NON-NLS-1$
	
	private final StandaloneFastIndexer indexer;
	private final DataElement status;
	private final CDTMiner miner;
	
	private IProgressMonitor progressMonitor;
	
	
	protected abstract void runIndexer(StandaloneFastIndexer indexer, IProgressMonitor progressMonitor) throws IOException;
	
	
	private IndexerThread(StandaloneFastIndexer indexer, DataElement status, CDTMiner miner) {
		super("Remote Indexer"); //$NON-NLS-1$
		this.indexer = indexer;
		this.status = status;
		this.miner = miner;
	}
	
	
	public static IndexerThread createReindexThread(StandaloneFastIndexer indexer, final List<String> sourcesList, DataElement status, CDTMiner miner) {
		return new IndexerThread(indexer, status, miner) {
			protected void runIndexer(StandaloneFastIndexer indexer, IProgressMonitor progressMonitor) throws IOException {
				indexer.rebuild(sourcesList, progressMonitor);
			}
		};
	}
	
	public static IndexerThread createIndexDeltaThread(StandaloneFastIndexer indexer, final List<String> added, final List<String> changed, final List<String> removed, DataElement status, CDTMiner miner) {
		return new IndexerThread(indexer, status, miner) {
			protected void runIndexer(StandaloneFastIndexer indexer, IProgressMonitor progressMonitor) throws IOException {
				indexer.handleDelta(added, changed, removed, progressMonitor);
			}
		};
	}
	
	
	@Override
	public void run() {
		progressMonitor = new RemoteIndexProgressMonitor(indexer, status, miner._dataStore);
		
		try { 
			indexer.setTraceStatistics(true);
			indexer.setShowProblems(true);
			indexer.setShowActivity(true);
			
			runIndexer(indexer, progressMonitor);
			
		} catch (IOException e) {
			UniversalServerUtilities.logError(LOG_TAG, "I/O Exception while reindexing", e, miner._dataStore); //$NON-NLS-1$
		} catch (Exception e) {
			UniversalServerUtilities.logError(LOG_TAG, "Exception while reindexing", e, miner._dataStore); //$NON-NLS-1$
		} catch (java.lang.VirtualMachineError e) {
			// Kill the JVM in the event of a VirtualMachineError
			System.exit(-1);
	    } finally {
	    	CDTMiner.statusDone(status);
	    }
	}

	
	public void cancel() {
		 // the indexer will notice this and stop running allowing the run() method to return
		progressMonitor.setCanceled(true);
		UniversalServerUtilities.logDebugMessage(LOG_TAG, "Indexer Thread Cancelled", miner._dataStore); //$NON-NLS-1$
	}
}
