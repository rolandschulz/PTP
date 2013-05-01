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
package org.eclipse.ptp.internal.debug.ui.views;

import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IWorkbenchPart;

/**
 * @author Clement chu
 *
 */
public abstract class AbstractDebugEventHandlerView extends AbstractDebugView {
	/**
	 * Event handler for this view
	 */
	private AbstractDebugEventHandler fEventHandler;

	/**
	 * Sets the event handler for this view
	 * 
	 * @param eventHandler event handler
	 */
	protected void setEventHandler(AbstractDebugEventHandler eventHandler) {
		this.fEventHandler = eventHandler;
	}
	
	/**
	 * Returns the event handler for this view
	 * 
	 * @return The event handler for this view
	 */
	protected AbstractDebugEventHandler getEventHandler() {
		return this.fEventHandler;
	}	
	
	/**
	 * @see IWorkbenchPart#dispose()
	 */
	public void dispose() {
		super.dispose();
		if (getEventHandler() != null) {
			getEventHandler().dispose();
		}	
	}
	
	/**
	 * @see org.eclipse.debug.ui.AbstractDebugView#becomesHidden()
	 */
	protected void becomesHidden() {
		super.becomesHidden();
		getEventHandler().viewBecomesHidden();
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractDebugView#becomesVisible()
	 */
	protected void becomesVisible() {
		super.becomesVisible();
		getEventHandler().viewBecomesVisible();
	}
	
	protected void clearStatusLine() {
		IStatusLineManager manager = getViewSite().getActionBars().getStatusLineManager(); 
		manager.setErrorMessage(null);
		manager.setMessage(null);
	}
}
