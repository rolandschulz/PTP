/*******************************************************************************
 * Copyright (c) 2006, 2013 QNX Software Systems and others.
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
 * Version: 1.11
 */

package org.eclipse.ptp.internal.rdt.ui.search;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.search.LineSearchElement;
import org.eclipse.cdt.internal.ui.util.Messages;
import org.eclipse.cdt.internal.ui.viewsupport.ColoringLabelProvider;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.ptp.internal.rdt.core.search.RemoteLineSearchElement;


public class RemoteSearchListLabelProvider extends ColoringLabelProvider {

	private final RemoteSearchViewPage fPage;
	private final int fColumnIndex;

	public RemoteSearchListLabelProvider(RemoteSearchViewPage page, int columnIndex) {
		super(new RemoteSearchLabelProvider(page));
		fPage = page;
		fColumnIndex = columnIndex;
	}

	@Override
	public void update(ViewerCell cell) {
		Object element = cell.getElement();
		switch (fColumnIndex) {
		case RemoteSearchViewPage.LOCATION_COLUMN_INDEX:
			if (element instanceof RemoteLineSearchElement) {
				RemoteLineSearchElement lineElement = (RemoteLineSearchElement) element;
				String location = RemoteSearchTreeContentProvider.getAbsolutePath(lineElement.getLocation()).toString();
				int lineNumber = lineElement.getLineNumber();
				cell.setText(Messages.format(CSearchMessages.CSearchResultCollector_location, location, lineNumber));
				cell.setImage(CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_SEARCH_LINE));
			}
			break;
		case RemoteSearchViewPage.DEFINITION_COLUMN_INDEX:
			if (element instanceof RemoteLineSearchElement) {
				RemoteLineSearchElement lineElement = (RemoteLineSearchElement) element;
				ICElement enclosingElement = lineElement.getMatches()[0].getEnclosingElement();
				if (fPage.isShowEnclosingDefinitions() && enclosingElement != null) {
					cell.setText(enclosingElement.getElementName());
					cell.setImage(getImage(element));
				} else {
					cell.setText(""); //$NON-NLS-1$
				}
			}
			break;
		case RemoteSearchViewPage.MATCH_COLUMN_INDEX:
			super.update(cell);
			cell.setImage(null);
			break;
		default:
			cell.setText(""); //$NON-NLS-1$
			break;
		}
	}

}
