/*******************************************************************************
 * Copyright (c) 2006, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    IBM Corporation
 *    Ed Swartz (Nokia)
 *******************************************************************************/

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.search.PDOMSearchListLabelProvider
 * Version: 1.7
 */

package org.eclipse.ptp.internal.rdt.ui.search;

import java.net.URI;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.util.Messages;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;

public class RemoteSearchListLabelProvider extends RemoteSearchLabelProvider {

	public RemoteSearchListLabelProvider(AbstractTextSearchViewPage page) {
		super(page);
	}
	
	@Override
	public String getText(Object element) {
		final String text= super.getText(element);
		
		if (element instanceof RemoteSearchElement) {
			RemoteSearchElement searchElement = (RemoteSearchElement)element;
			final int count= getMatchCount(element);
			URI uri = searchElement.getLocation().getURI();
			final String filename = uri == null ? "" : " - " + uri.getPath(); //$NON-NLS-1$ //$NON-NLS-2$
			if (count == 1) {
				return text + filename;
			}
			return text + filename + " " //$NON-NLS-1$
				+ Messages.format(CSearchMessages.CSearchResultCollector_matches, new Integer(count)); 
		} 
		
		if (element instanceof IIndexFileLocation) {
			URI uri = ((IIndexFileLocation)element).getURI();
			return uri == null ? "" : " - " + uri.getPath(); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		return text;
	}
}
