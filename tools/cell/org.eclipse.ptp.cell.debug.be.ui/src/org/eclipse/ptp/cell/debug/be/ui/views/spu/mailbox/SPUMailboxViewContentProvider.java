/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */

package org.eclipse.ptp.cell.debug.be.ui.views.spu.mailbox;

import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.internal.ui.views.IDebugExceptionHandler;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.cell.debug.be.DebugBEActivator;



/**
 * Provides content for the SPU Mailbox view.
 *
 * @author Ricardo M. Matinata
 * @since 1.3
 */
public class SPUMailboxViewContentProvider implements IStructuredContentProvider {

	/**
	 * Handler for exceptions as content is retrieved
	 */
	private IDebugExceptionHandler fExceptionHandler = null;

	public SPUMailboxViewContentProvider() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements( Object inputElement ) {
		if ( inputElement instanceof ICDebugTarget ) {
			ICDebugTarget target = (ICDebugTarget)inputElement;
			if ( target != null ) {
				Object[] events = DebugBEActivator.getDefault().getSPUProcessor().processSPUMailbox(target);
				if ( events != null )
					return events;
			}
		}
		return new Object[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged( Viewer viewer, Object oldInput, Object newInput ) {
	}

	/**
	 * Sets an exception handler for this content provider.
	 * 
	 * @param handler debug exception handler or <code>null</code>
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
