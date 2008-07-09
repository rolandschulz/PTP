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
 *    Ed Swartz (Nokia)
 *    IBM Corporation
 *******************************************************************************/

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.search.PDOMSearchTreeLabelProvider
 * Version: 1.5
 */

package org.eclipse.ptp.internal.rdt.ui.search;

import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.util.Messages;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;

public class RemoteSearchTreeLabelProvider extends RemoteSearchLabelProvider {
	
	public RemoteSearchTreeLabelProvider(AbstractTextSearchViewPage page) {
		super(page);
	}
	
	@Override
	public String getText(Object element) {
		final String text= super.getText(element);
		final int count= getMatchCount(element);
		if (count <= 1) {
			return text;
		}
		return text + " " //$NON-NLS-1$
				+ Messages.format(CSearchMessages.CSearchResultCollector_matches, new Integer(count)); 
	}
}
