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
package org.eclipse.ptp.debug.internal.ui.views;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ptp.debug.ui.IPTPDebugUIConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Clement chu
 *
 */
public abstract class PTabFolder extends PDebugView {
	protected CTabFolder folder = null;

	public void createPartControl(Composite parent) {
		parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		folder = new CTabFolder(parent, SWT.TOP);
		folder.setBorderVisible(true);
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		folder.setSimple(false);
	    folder.setUnselectedImageVisible(false);
	    folder.setUnselectedCloseVisible(false);
	    
	    folder.setMinimizeVisible(true);
	    folder.setMaximizeVisible(true);
	    folder.addCTabFolder2Listener(new CTabFolder2Adapter() {
	    	public void minimize(CTabFolderEvent event) {
	    		folder.setMinimized(true);
	    	}
	    	public void maximize(CTabFolderEvent event) {
	    		folder.setMaximized(true);
	    		folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	    	}
	    	public void restore(CTabFolderEvent event) {
	    		folder.setMinimized(false);
	    		folder.setMaximized(false);
	    		folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	    	}
	    });
	    createToolBarMenu(getViewSite().getActionBars().getToolBarManager());
	}
	
	public void createToolBarMenu(IToolBarManager toolBarMgr) {
		toolBarMgr.add(new Separator(IPTPDebugUIConstants.IUITABVARIABLEGROUP));
		toolBarMgr.add(new Separator(IPTPDebugUIConstants.IUITABEMPTYGROUP));
		configureToolBar(toolBarMgr);
	}
	protected abstract void configureToolBar(IToolBarManager toolBarMgr);
	
	public void setFocus() {
		folder.setFocus();
	}
	
	public CTabFolder getTabFolder() {
		return folder;
	}
	
	public abstract void createTabItem(String tabName, Object selection);
}
