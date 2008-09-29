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

import org.eclipse.jface.util.Assert;
import org.eclipse.ptp.cell.utils.searcher.Searcher;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;


/**
 * @author laggarcia
 * @since 3.0.0
 */
public class FieldEditorWithSearchDelegate {

	protected boolean usingDefaultSearchButtonSelectionListener = true;

	/**
	 * The search button, or <code>null</code> if none (before creation and
	 * after disposal).
	 */
	protected Button searchButton;

	/**
	 * The text for the search button, or <code>null</code> if missing.
	 */
	protected String searchButtonText;

	/**
	 * Get the search control. Create it in parent if required.
	 * 
	 * @param parent
	 * @return Button
	 */
	public Button getSearchControl(Composite parent) {
		if (this.searchButton == null) {
			this.searchButton = new Button(parent, SWT.PUSH);
			if (this.searchButtonText == null) {
				this.searchButtonText = PreferenceConstantsFromFile.searchButtonText;
			}
			this.searchButton.setText(this.searchButtonText);
			this.searchButton.setFont(parent.getFont());
			this.searchButton
					.addSelectionListener(FieldEditorWithSearch.DEFAULT_SEARCH_BUTTON_SELECTION_LISTENER);
			this.searchButton.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					FieldEditorWithSearchDelegate.this.searchButton = null;
				}
			});
		}
		return this.searchButton;
	}

	/**
	 * Sets the text of the search button.
	 * 
	 * @param text
	 *            the new text
	 */
	public void setSearchButtonText(String text) {
		Assert.isNotNull(text);
		this.searchButtonText = text;
		if (this.searchButton != null) {
			this.searchButton.setText(text);
			Point prefSize = this.searchButton.computeSize(SWT.DEFAULT,
					SWT.DEFAULT);
			GridData data = (GridData) this.searchButton.getLayoutData();
			data.widthHint = Math.max(SWT.DEFAULT, prefSize.x);
		}
	}

	public void setSearchButtonLayout(int widthHint) {
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.widthHint = Math.max(widthHint, this.searchButton.computeSize(
				SWT.DEFAULT, SWT.DEFAULT, true).x);
		this.searchButton.setLayoutData(gridData);
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	public int getNumberOfControls(int numberOfControls) {
		return numberOfControls + 1;
	}

	/**
	 * Adds a SearchButtonSelectionAdapter with the provided Searcher as a
	 * SelectionListener of this FieldEditorWithSearch search Button.
	 * 
	 * @param searcher
	 *            the searcher that will provide the search engine to be called
	 *            by the selection event.
	 */
	public void addSearcher(Searcher searcher) {
		// First, we have to remove the default searcher, that does nothing.
		if (this.usingDefaultSearchButtonSelectionListener) {
			this.searchButton
					.removeSelectionListener(FieldEditorWithSearch.DEFAULT_SEARCH_BUTTON_SELECTION_LISTENER);
			this.usingDefaultSearchButtonSelectionListener = false;
		}
		this.searchButton
				.addSelectionListener(new SearchButtonSelectionAdapter(searcher));
	}

}
