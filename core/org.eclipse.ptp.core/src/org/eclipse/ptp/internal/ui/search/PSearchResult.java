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

import java.text.MessageFormat;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;

/**
 *
 */
public class PSearchResult extends AbstractTextSearchResult {
    private PSearchQuery query;
	//private static final Match[] NO_MATCHES = new Match[0];

	public PSearchResult(PSearchQuery query){
		this.query = query;
	}		
	
	public String getText() {
		int matchCount= getMatchCount();
		String format= null;
		if (matchCount == 1)
			format = query.getSingularLabel();
		else
			format = query.getPluralLabelPattern();
		return MessageFormat.format(format, new Object[] { new Integer(matchCount) });
	}

	public String getTooltip() {
		return getText();
	}
	public ImageDescriptor getImageDescriptor() {
		return query.getImageDescriptor();
	}
	public ISearchQuery getQuery() {
		return query;
	}
	public String getLabel() {
		int matches = getMatchCount();
		String label = null;
		if (matches == 1)
			return query.getSingularLabel();
		else
			label = query.getPluralLabelPattern();

		return MessageFormat.format(label, new Object[]{new Integer(matches)});
	}
		
	public IEditorMatchAdapter getEditorMatchAdapter() {
		return null;
	}	
	public IFileMatchAdapter getFileMatchAdapter() {
		return null;
	}	
}
