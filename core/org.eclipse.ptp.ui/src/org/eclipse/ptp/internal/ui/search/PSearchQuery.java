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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.ui.old.ParallelImages;
import org.eclipse.ptp.ui.old.UIMessage;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;

/**
 *
 */
public class PSearchQuery implements ISearchQuery, IPSearchConstants {
    private ISearchResult result;
    private int searchFor;
    private int limitTo;
    private String pattern;
    
    public PSearchQuery(String pattern, int searchFor, int limitTo) {
        this.pattern = pattern;
        this.searchFor = searchFor;
        this.limitTo = limitTo; 
    }
    
    private String getSearchForLabel() {
        switch (searchFor) {
            case SEARCH_NODE:
                return "Node";
            case SEARCH_PROCESS:
                return "Process";
            default:
                return "Node / Process";
        }
    }
    
	public String getSingularLabel() {
	    String[] args = new String[] {pattern, getSearchForLabel()};
		switch (limitTo) {
		    case LIMIT_NUMBER:
		        return UIMessage.getFormattedResourceString("PSearchQuery.singularNumberPostfix", args);
		    case LIMIT_PID:
		        return UIMessage.getFormattedResourceString("PSearchQuery.singularPIDPostfix", args);
		    case LIMIT_EXITCODE:
		        return UIMessage.getFormattedResourceString("PSearchQuery.singularExitCodePostfix", args);
		    default:
		        return pattern;
		}
	}
	
	public String getPluralLabelPattern() {
	    String[] args = new String[] {pattern, "{0}", getSearchForLabel()};
		switch (limitTo) {
		    case LIMIT_NUMBER:
		        return UIMessage.getFormattedResourceString("PSearchQuery.pluralNumberPostfix", args);
		    case LIMIT_PID:
		        return UIMessage.getFormattedResourceString("PSearchQuery.pluralPIDPostfix", args);
		    case LIMIT_EXITCODE:
		        return UIMessage.getFormattedResourceString("PSearchQuery.pluralExitCodePostfix", args);
		    default:
		        return pattern;
		}
	}
	
	public ImageDescriptor getImageDescriptor() {
	    switch (limitTo) {
		    case LIMIT_PID:
		        return ParallelImages.DESC_PROC_RUNNING;
		    case LIMIT_EXITCODE:
		        return ParallelImages.DESC_PROC_RUNNING;
		    default:
		        switch (searchFor) {
		            case SEARCH_NODE:
				        return ParallelImages.DESC_NODE_RUNNING;
				    default:
				        return ParallelImages.DESC_PROC_RUNNING;
		        }
	    }
	}
	
	public IStatus run(IProgressMonitor monitor) {
		final PSearchResult textResult = (PSearchResult) getSearchResult();
		textResult.removeAll();

		SearchEngine engine = new SearchEngine();
		int matchCount= 0;

		int totalTicks= 1000;

		monitor.beginTask("", totalTicks);
		IProgressMonitor mainSearchPM= new SubProgressMonitor(monitor, 1000);

		PSearchResultCollector finalCollector = new PSearchResultCollector(textResult, mainSearchPM);

		PSearchPattern searchPattern = SearchEngine.createSearchPattern(pattern, searchFor, limitTo);
		try {
			engine.search(searchPattern, finalCollector);
		} catch (InterruptedException e) {
		}

		monitor.done();
		matchCount = finalCollector.getMatchCounter();

		return new Status(IStatus.OK, PTPCorePlugin.PLUGIN_ID, 0,"", null);
	}
	
	public String getLabel() {
	    String label = "Search for " + getSearchForLabel();	    
		label += " \"";
		label += pattern;
		label += '"';
		return label;
	}

	public boolean canRerun() {
		return true;
	}

	public boolean canRunInBackground() {
		return true;
	}

	public ISearchResult getSearchResult() {
		if (result == null)
			result= new PSearchResult(this);
		return result;
	}	
}
