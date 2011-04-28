/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.providers;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.ptp.rm.jaxb.core.ICommandJobRemoteOutputHandler;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.data.PersistentCommandJobStatus;
import org.eclipse.swt.graphics.Image;

/**
 * For the JobList Table Viewer.
 * 
 * @author arossi
 * 
 */
public class CommandJobStatusLabelProvider implements ITableLabelProvider, IJAXBUINonNLSConstants {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.
	 * jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang
	 * .Object, int)
	 */
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang
	 * .Object, int)
	 */
	public String getColumnText(Object element, int columnIndex) {
		PersistentCommandJobStatus status = (PersistentCommandJobStatus) element;
		ICommandJobRemoteOutputHandler h = null;
		switch (columnIndex) {
		case 0:
			return status.getJobId();
		case 1:
			return status.getState();
		case 2:
			return status.getStateDetail();
		case 3:
			h = status.getOutputHandler();
			if (h != null) {
				return h.getRemoteFilePath();
			}
		case 4:
			return ZEROSTR + status.getOutReady();
		case 5:
			h = status.getErrorHandler();
			if (h != null) {
				return h.getRemoteFilePath();
			}
		case 6:
			return ZEROSTR + status.getErrReady();
		}

		return ZEROSTR;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang
	 * .Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse
	 * .jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
	}

}
