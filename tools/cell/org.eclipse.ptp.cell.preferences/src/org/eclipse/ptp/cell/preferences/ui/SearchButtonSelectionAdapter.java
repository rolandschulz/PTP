/******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.cell.preferences.ui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.cell.utils.searcher.SearchFailedException;
import org.eclipse.ptp.cell.utils.searcher.Searcher;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;


/**
 * @author laggarcia
 * @since 3.0.0
 */
public class SearchButtonSelectionAdapter extends SelectionAdapter {

	private Searcher searcher = null;

	public SearchButtonSelectionAdapter(Searcher searcher) {
		this.searcher = searcher;
	}

	public void widgetSelected(SelectionEvent evt) {
		try {
			this.searcher.search();
		} catch (SearchFailedException sfe) {
			Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getShell();
			MessageDialog.openWarning(shell,
					PreferenceConstantsFromFile.searchEngineInformationTitle,
					sfe.getLocalizedMessage());
		}
	}

}
