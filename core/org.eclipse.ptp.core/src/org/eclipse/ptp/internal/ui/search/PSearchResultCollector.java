package org.eclipse.ptp.internal.ui.search;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.core.IPElement;
import org.eclipse.search.ui.text.Match;

/**
 * @author Clement
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
