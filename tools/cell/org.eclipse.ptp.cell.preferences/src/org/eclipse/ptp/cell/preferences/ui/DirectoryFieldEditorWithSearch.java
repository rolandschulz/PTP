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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.ptp.cell.utils.searcher.Searcher;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;


/**
 * @author laggarcia
 * @since 3.0.0
 */
public class DirectoryFieldEditorWithSearch extends DirectoryFieldEditor
		implements FieldEditorWithSearch {

	private FieldEditorWithSearchDelegate delegate;

	/**
	 * 
	 */
	public DirectoryFieldEditorWithSearch() {
	}

	/**
	 * @param name
	 * @param labelText
	 * @param parent
	 */
	public DirectoryFieldEditorWithSearch(String name, String labelText,
			Composite parent) {
		super(name, labelText, parent);
		setSearchButtonText(PreferenceConstantsFromFile.searchButtonText);
	}

	/*
	 * (non-Javadoc) Method declared on StringFieldEditor (and FieldEditor).
	 */
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		setValidateStrategy(VALIDATE_ON_KEY_STROKE);
		super.doFillIntoGrid(parent, numColumns - 1);
		Button searchButton = getDelegate().getSearchControl(parent);
		checkParent(searchButton, parent);
		int widthHint = convertHorizontalDLUsToPixels(searchButton,
				IDialogConstants.BUTTON_WIDTH);
		getDelegate().setSearchButtonLayout(widthHint);
	}

    /* (non-Javadoc)
     * Method declared on FieldEditor.
     */
    protected void doStore() {
    	this.refreshValidState();
    	if (this.checkState()) {
    		super.doStore();
    	}
    }
	
	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	protected void adjustForNumColumns(int numColumns) {
		// Nothing need to be done in this case
	}

	protected FieldEditorWithSearchDelegate getDelegate() {
		if (this.delegate == null) {
			this.delegate = new FieldEditorWithSearchDelegate();
		}
		return this.delegate;
	}

	/*
	 * (non-Javadoc) Method declared on FieldEditor.
	 */
	public int getNumberOfControls() {
		return getDelegate().getNumberOfControls(super.getNumberOfControls());
	}

	/**
	 * Sets the text for the Search Button
	 * @param buttonText
	 */
	public void setSearchButtonText(String buttonText) {
		getDelegate().setSearchButtonText(buttonText);
	}

	/**
	 * Associates a Searcher search engine with the Search Button
	 * 
	 * @param searcher
	 */
	public void addSearcher(Searcher searcher) {
		// If the DirectoryFieldEditorWithSearch is already created, the Search
		// Button already exists.
		getDelegate().addSearcher(searcher);
	}

}
