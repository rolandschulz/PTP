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
package org.eclipse.ptp.debug.internal.ui.views.array;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ptp.debug.core.model.IPVariable;
import org.eclipse.ptp.debug.internal.ui.actions.AddVariableAction;
import org.eclipse.ptp.debug.internal.ui.views.AbstractPDebugViewEventHandler;
import org.eclipse.ptp.debug.internal.ui.views.PTabFolder;
import org.eclipse.ptp.debug.internal.ui.views.PTabItem;
import org.eclipse.ptp.debug.ui.IPTPDebugUIConstants;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Clement chu
 *
 */
public class ArrayView extends PTabFolder {
	protected AbstractPDebugViewEventHandler fEventHandler = null;
	
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		setEventHandler(new ArrayViewEventHandler(this));
	}
	protected void setEventHandler(AbstractPDebugViewEventHandler eventHandler) {
		this.fEventHandler = eventHandler;
	}
	protected AbstractPDebugViewEventHandler getEventHandler() {
		return this.fEventHandler;
	}
	public void dispose() {
		if (getEventHandler() != null)
			getEventHandler().dispose();
		super.dispose();
	}
	public void repaint(boolean all) {
		for (PTabItem item : getItems()) {
			((ArrayTabItem)item).clearContext();
			((ArrayTabItem)item).setReloadVariable(all);
		}
	}
	
	protected void configureToolBar(IToolBarManager toolBarMgr) {
		toolBarMgr.appendToGroup(IPTPDebugUIConstants.IUITABVARIABLEGROUP, new AddVariableAction(this));
	}
	
	public void createTabItem(String tabName, Object selection) throws DebugException {
		if (selection instanceof IPVariable) {
			if (items.containsKey(tabName)) {
				throw new DebugException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IStatus.ERROR, "Already existed variable", null));
			}
			ArrayTabItem item = new ArrayTabItem(this, tabName, (IPVariable)selection);
			item.init((IPVariable)selection);
			item.displayTab();
 			folder.setSelection(item.getTabItem());
 			items.put(tabName, item);
		}
	}
}
