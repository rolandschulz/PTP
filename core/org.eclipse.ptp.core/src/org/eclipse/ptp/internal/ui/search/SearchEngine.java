package org.eclipse.ptp.internal.ui.search;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * @author Clement
 *
 */
public class SearchEngine implements IPSearchConstants {
    public static PSearchPattern createSearchPattern(String patternString, int searchFor, int limitTo) {
	    int mode;
		if(patternString.indexOf('*') != -1  || patternString.indexOf('?') != -1 )
			mode = PATTERN_MATCH;
		else 
			mode = EXACT_MATCH;
        
		return new PSearchPattern(patternString, searchFor, limitTo, mode);
    }
    
	public void search(PSearchPattern pattern, PSearchResultCollector collector) throws InterruptedException {
		MatchLocator matchLocator = new MatchLocator(pattern, collector);
		search(pattern, collector, matchLocator);
	}    
    
	public void search(PSearchPattern pattern, PSearchResultCollector collector, MatchLocator matchLocator) throws InterruptedException {	    
		if(pattern == null)
			return;
		
		//initialize progress monitor
		IProgressMonitor progressMonitor = collector.getProgressMonitor();
		if(progressMonitor != null) 
			progressMonitor.beginTask("Engine searching", 100);

		SubProgressMonitor subMonitor = (progressMonitor == null ) ? null : new SubProgressMonitor(progressMonitor, 100);
		matchLocator.setProgressMonitor(subMonitor);
		
		if(progressMonitor != null && progressMonitor.isCanceled())
			throw new InterruptedException();

		if(progressMonitor != null)
			progressMonitor.subTask("Engine.searching");

		matchLocator.locateMatches(pattern);
		collector.done();
	}    
}
