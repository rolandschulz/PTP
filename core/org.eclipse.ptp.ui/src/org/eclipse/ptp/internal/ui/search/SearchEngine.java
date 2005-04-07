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
import org.eclipse.core.runtime.SubProgressMonitor;

/**
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
