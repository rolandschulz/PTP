/*******************************************************************************
 * Copyright (c) 2006, 2011 QNX Software Systems and others.
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
 * Version: 1.7
 */

package org.eclipse.ptp.internal.rdt.ui.search;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.search.LineSearchElement;
import org.eclipse.cdt.internal.ui.util.Messages;
import org.eclipse.jface.viewers.StyledString;


public class RemoteSearchTreeLabelProvider extends RemoteSearchLabelProvider {
	
	public RemoteSearchTreeLabelProvider(RemoteSearchViewPage page) {
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
	
	@Override
	public StyledString getStyledText(Object element) {
		if (element instanceof ITranslationUnit) {
			StyledString styled = new StyledString(super.getText(element));
			final int count= getMatchCount(element);
			if (count > 1) {
				final String matchesCount = " " //$NON-NLS-1$
					+ Messages.format(CSearchMessages.CSearchResultCollector_matches, new Integer(count));
				styled.append(matchesCount, StyledString.COUNTER_STYLER);
				return styled;
			}
		}
		if (!(element instanceof LineSearchElement))
			return new StyledString(getText(element));
		LineSearchElement lineElement = (LineSearchElement) element;
		String enclosingName = ""; //$NON-NLS-1$
		ICElement enclosingElement = lineElement.getMatches()[0].getEnclosingElement();
		if (fPage.isShowEnclosingDefinitions() && enclosingElement != null) {
			enclosingName = enclosingElement.getElementName() + ", "; //$NON-NLS-1$
		}
		Integer lineNumber = lineElement.getLineNumber();
		String prefix = Messages.format(CSearchMessages.CSearchResultCollector_line, enclosingName, lineNumber);
		prefix += ":  "; //$NON-NLS-1$
		StyledString location = new StyledString(prefix, StyledString.QUALIFIER_STYLER);
		return location.append(super.getStyledText(element));
	}

}
