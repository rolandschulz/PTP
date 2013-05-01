/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.internal.debug.ui.views.signals;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.internal.ui.views.IDebugExceptionHandler;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.internal.debug.ui.PTPDebugUIPlugin;

/**
 * @author Clement chu
 */
public class SignalsViewContentProvider implements IStructuredContentProvider {
	/**
	 * Handler for exceptions as content is retrieved
	 */
	private IDebugExceptionHandler fExceptionHandler = null;

	public SignalsViewContentProvider() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof IPDebugTarget) {
			IPDebugTarget target = (IPDebugTarget) inputElement;
			try {
				if (target != null) {
					Object[] signals = target.getSignals();
					if (signals != null)
						return signals;
				}
			} catch (DebugException e) {
				if (getExceptionHandler() != null)
					getExceptionHandler().handleException(e);
				else
					PTPDebugUIPlugin.log(e);
			}
		}
		return new Object[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	/**
	 * Sets an exception handler for this content provider.
	 * 
	 * @param handler
	 *            debug exception handler or <code>null</code>
	 */
	protected void setExceptionHandler(IDebugExceptionHandler handler) {
		fExceptionHandler = handler;
	}

	/**
	 * Returns the exception handler for this content provider.
	 * 
	 * @return debug exception handler or <code>null</code>
	 */
	protected IDebugExceptionHandler getExceptionHandler() {
		return fExceptionHandler;
	}
}
