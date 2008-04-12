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

import org.eclipse.search.ui.text.AbstractTextSearchViewPage;

/**
 * @author Doug Schaefer
 *
 */
public class ReferenceSearchTreeLabelProvider extends ReferenceSearchLabelProvider {
	
	public ReferenceSearchTreeLabelProvider(AbstractTextSearchViewPage page) {
		super(page);
	}
	
	public String getText(Object element) {
		final String text= super.getText(element);
		final int count= getMatchCount(element);
		if (count == 0) {
			return text;
		}
		return text + " " //$NON-NLS-1$
				+ SearchMessages.getFormattedString("CSearchResultCollector.matches", new Integer(count)); //$NON-NLS-1$
	}

}
