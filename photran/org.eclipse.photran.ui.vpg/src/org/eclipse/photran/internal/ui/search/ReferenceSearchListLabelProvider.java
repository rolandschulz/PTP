/*******************************************************************************
 * Copyright (c) 2006, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 * Markus Schorn (Wind River Systems)
 *******************************************************************************/

package org.eclipse.photran.internal.ui.search;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IndexLocationFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;

/**
 * @author Doug Schaefer
 *
 */
public class ReferenceSearchListLabelProvider extends ReferenceSearchLabelProvider {

	public ReferenceSearchListLabelProvider(AbstractTextSearchViewPage page) {
		super(page);
	}
	
	public String getText(Object element) {
		final String text= super.getText(element);
		
		if (element instanceof IFile) {
			IFile searchElement = (IFile)element;
			final int count= getMatchCount(element); 
			return searchElement.getFullPath() + " " //$NON-NLS-1$
				+ SearchMessages.getFormattedString("CSearchResultCollector.matches", new Integer(count)); //$NON-NLS-1$
		} 
		
		if (element instanceof IIndexFileLocation) {
			IPath path= IndexLocationFactory.getPath((IIndexFileLocation)element); 
			if(path!=null) {
				return path.toString();
			}
		}
		
		if (text == null || "".equals(text)) {
			return SearchMessages.getString("ReferenceSearchListLabelProvider_Unknown_element");
		}
		return text;
	}
}
