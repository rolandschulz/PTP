/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/

package org.eclipse.fdt.internal.ui.search;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.fdt.core.model.ICElement;
import org.eclipse.fdt.core.search.BasicSearchMatch;
import org.eclipse.fdt.core.search.BasicSearchResultCollector;
import org.eclipse.fdt.core.search.ICSearchConstants;
import org.eclipse.fdt.core.search.IMatch;
import org.eclipse.search.ui.text.Match;

public class NewSearchResultCollector extends BasicSearchResultCollector {
	public static final int PARENT_LENGTH = 7;
	public static final String PARENT = "PARENT:"; //$NON-NLS-1$
	
	public static final int NAME_LENGTH = 5;
	public static final String NAME = "NAME:"; //$NON-NLS-1$
	
	public static final int LOCATION_LENGTH = 9;
	public static final String LOCATION = "LOCATION:"; //$NON-NLS-1$
	
	public static final int ELEMENTTYPE_LENGTH = 12;
	public static final String ELEMENTTYPE = "ELEMENTTYPE:"; //$NON-NLS-1$
	
	public static final int VISIBILITY_LENGTH = 11;
	public static final String VISIBILITY = "VISIBILITY:"; //$NON-NLS-1$
	
	private CSearchResult fSearch;
	private IProgressMonitor fProgressMonitor;
	private int fMatchCount;
	

	public NewSearchResultCollector(CSearchResult search, IProgressMonitor monitor) {
		super();
		fSearch= search;
		fProgressMonitor= monitor;
		fMatchCount = 0;
	}

	
	public void accept(IResource resource, int start, int end, ICElement enclosingElement, int accuracy) {
		fMatchCount++;
		fSearch.addMatch(new Match(enclosingElement, start, end-start));
	}


	public void done() {
	}

	public IProgressMonitor getProgressMonitor() {
		return fProgressMonitor;
	}
	
	public int getMatchCount() {
		return fMatchCount;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.search.ICSearchResultCollector#acceptMatch(org.eclipse.fdt.core.search.IMatch)
	 */
	public boolean acceptMatch(IMatch match) throws CoreException {

      BasicSearchMatch searchMatch = (BasicSearchMatch) match;
		
      if( !super.acceptMatch( match ) )
      		return false;

      if( searchMatch.resource == null &&
      	  searchMatch.path == null)
      		return false;
	 
      if (searchMatch.resource != null){
		fMatchCount++;
		int start = match.getStartOffset();
		int end = match.getEndOffset();
		String classifier = PARENT + match.getParentName() + NAME + match.getName() + LOCATION + match.getLocation().toOSString() + ELEMENTTYPE + match.getElementType() + VISIBILITY  + match.getVisibility();
		fSearch.addMatch(new CSearchMatch(classifier,start,end-start, match));
		
		return true;
      }
      else {
    	fMatchCount++;
		int start = match.getStartOffset();
		int end = match.getEndOffset();
		String classifier = PARENT + match.getParentName() + NAME + match.getName() + LOCATION + match.getLocation().toOSString() + ELEMENTTYPE + match.getElementType() + VISIBILITY  + match.getVisibility();
		fSearch.addMatch(new CSearchMatch(classifier,start,end-start, match));
		
		return true;
      }
	}


	/**
	 * @param externalMatchLocation
	 * @param refProject
	 * @return
	 */
	private IFile getUniqueFile(IPath externalMatchLocation, IProject refProject) {
		IFile file = null;
		String fileName = ""; //$NON-NLS-1$
		//Total number of segments in file name
		int segments = externalMatchLocation.segmentCount() - 1;
		for (int linkNumber=0; linkNumber<Integer.MAX_VALUE; linkNumber++){
			if (fileName !="") //$NON-NLS-1$
				fileName = ICSearchConstants.EXTERNAL_SEARCH_LINK_PREFIX + linkNumber + "_" + externalMatchLocation.segment(segments); //$NON-NLS-1$ //$NON-NLS-2$
			else
				fileName = externalMatchLocation.segment(segments);
			
			file=refProject.getFile(fileName);
		    IPath path = file.getLocation();
			
		    //If the location passed in is equal to a location of a file
		    //that is already linked then return the file
		    if (externalMatchLocation.equals(path))
		    	break;
		    
		    //If the file doesn't already exist in the workspace return
		    if (!file.exists())
				break;
		}
		return file;
	}
	
	
}
