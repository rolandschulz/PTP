// Copyright (c) 2013 IBM Corporation and others. All rights reserved. 
// This program and the accompanying materials are made available under the 
// terms of the Eclipse Public License v1.0s which accompanies this distribution, 
// and is available at http://www.eclipse.org/legal/epl-v10.html

package org.eclipse.ptp.rm.ibm.lsf.ui.model;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;

public class TableContentsProvider implements ITableLabelProvider {

	/**
	 * Create the content provider for the a table in a custom widget dialoh
	 */
	public TableContentsProvider() {
	}

	@Override
	/**
	 * Add listener for notifications of state changes for a label
	 * Does nothing in this implementation
	 * 
	 * @param arg0: The listener
	 */
	public void addListener(ILabelProviderListener arg0) {
	}

	@Override
	/**
	 * Dispose of the label provider
	 * Does nothing in this implementation
	 */
	public void dispose() {
	}

	@Override
	/**
	 * Return the image for a column in the table
	 * Does nothing in this implementation
	 * 
	 * @param arg0: The element containing data for the table row
	 * @param arg1: The column index for the column to process
	 * 
	 * @return: The image for the column
	 */
	public Image getColumnImage(Object arg0, int arg1) {
		return null;
	}

	@Override
	/**
	 * Return the text for a column in the table
	 * 
	 * @param arg0: The element containing data for the table row
	 * @param arg1: The column index for the column to process
	 * 
	 * @return: The text for the column
	 */
	public String getColumnText(Object data, int columnIndex) {
		return ((String[]) data)[columnIndex];
	}

	@Override
	/**
	 * Returns whether the label would be affected by a change in the property
	 * 
	 * @param arg0: The element
	 * @param arg1: The property
	 * 
	 * @return: Boolean indicating if the label is affected, always false
	 */
	public boolean isLabelProperty(Object arg0, String arg1) {
		return false;
	}

	@Override
	/**
	 * Remove the listener for notifications of state changes for a label
	 * Does nothing in this implementation
	 * 
	 * @param arg0: The listener
	 */
	public void removeListener(ILabelProviderListener arg0) {
	}

}
