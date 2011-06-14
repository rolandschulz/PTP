/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.index;

import java.io.Serializable;

import org.eclipse.cdt.internal.core.pdom.IndexerProgress;

/**
 * A record of a remote indexer's indexing progress
 * @author vkong
 *
 */
public class RemoteIndexerProgress implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public String status;
	public int fRequestedFilesCount;
	public int fCompletedSources;	
	public int fPrimaryHeaderCount;	// headers parsed that were actually requested
	public int fCompletedHeaders;	// all headers including those found through inclusions
	public int fTimeEstimate;		// fall-back for the time where no file-count is available

	public RemoteIndexerProgress() {
	}

	public RemoteIndexerProgress(RemoteIndexerProgress info) {
		fRequestedFilesCount= info.fRequestedFilesCount;
		fCompletedSources= info.fCompletedSources;
		fCompletedHeaders= info.fCompletedHeaders;
		fPrimaryHeaderCount= info.fPrimaryHeaderCount;
	}
	
	public RemoteIndexerProgress(IndexerProgress info) {
		fRequestedFilesCount= info.fRequestedFilesCount;
		fCompletedSources= info.fCompletedSources;
		fCompletedHeaders= info.fCompletedHeaders;
		fPrimaryHeaderCount= info.fPrimaryHeaderCount;
	}
	
	/**
	 * Creates an IndexerProgress with the same progress information as the given
	 * RemoteIndexerProgress  
	 * @param progress
	 * @return
	 */
	public static IndexerProgress getIndexerProgress(RemoteIndexerProgress progress) {
		IndexerProgress info = new IndexerProgress();
		if (progress != null) {
			info.fRequestedFilesCount= progress.fRequestedFilesCount;
			info.fCompletedSources= progress.fCompletedSources;
			info.fCompletedHeaders= progress.fCompletedHeaders;
			info.fPrimaryHeaderCount= progress.fPrimaryHeaderCount;
		}
		return info;
	}

	public int getEstimatedTicks() {
		return fRequestedFilesCount > 0 ? fRequestedFilesCount : fTimeEstimate;
	}	
}
