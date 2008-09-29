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

import org.eclipse.ptp.cell.utils.searcher.NoEngineSearcher;
import org.eclipse.ptp.cell.utils.searcher.Searcher;
import org.eclipse.swt.events.SelectionListener;


/**
 * @author laggarcia
 * @since 3.0.0
 */
public interface FieldEditorWithSearch {

	public static final Searcher DEFAULT_SEARCHER = new NoEngineSearcher();

	public static final SelectionListener DEFAULT_SEARCH_BUTTON_SELECTION_LISTENER = new SearchButtonSelectionAdapter(
			DEFAULT_SEARCHER);

	public void addSearcher(Searcher searcher);

}