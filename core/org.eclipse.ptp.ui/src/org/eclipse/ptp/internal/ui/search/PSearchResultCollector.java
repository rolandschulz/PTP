/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.internal.ui.search;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.core.IPElement;
import org.eclipse.search.ui.text.Match;

/**
 *
 */
public class PSearchResultCollector {
    private PSearchResult searchResult = null;
    private IProgressMonitor monitor = null;
    private int matchCount = 0;
    
    public PSearchResultCollector(PSearchResult searchResult, IProgressMonitor monitor) {
		this.searchResult = searchResult;
		this.monitor = monitor;
		matchCount = 0;
	}
    
	public IProgressMonitor getProgressMonitor() {
		 return monitor;
	}

	public void done() {
	}
	
	public int getMatchCounter() {
	    return matchCount;
	}
	
	public void addMatch(IPElement element, int start, int end) {
	    matchCount++;
	    searchResult.addMatch(new Match(element, start, end));
	}
}
