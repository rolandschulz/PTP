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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ptp.debug.core.model.IPStackFrame;
import org.eclipse.ptp.internal.debug.ui.IPTPDebugUIConstants;
import org.eclipse.ptp.internal.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.internal.debug.ui.messages.Messages;
import org.eclipse.ptp.internal.ui.IPTPUIConstants;
import org.eclipse.ptp.internal.ui.views.AbstractParallelView;
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
public abstract class PTabFolder extends AbstractParallelView {
	protected Map<String, PTabItem> items = new HashMap<String, PTabItem>();
	protected CTabFolder folder = null;

	@Override
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
			@Override
			public void minimize(CTabFolderEvent event) {
				folder.setMinimized(true);
			}

			@Override
			public void maximize(CTabFolderEvent event) {
				folder.setMaximized(true);
				folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			}

			@Override
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

	@Override
	public void setFocus() {
		folder.setFocus();
	}

	public CTabFolder getTabFolder() {
		return folder;
	}

	@Override
	public void dispose() {
		closeAllTabs();
		super.dispose();
	}

	public void closeTab(String name) {
		if (items != null) {
			items.remove(name);
		}
	}

	public void closeAllTabs() {
		for (PTabItem item : getItems()) {
			item.dispose();
		}
		items.clear();
	}

	public PTabItem[] getItems() {
		return items.values().toArray(new PTabItem[0]);
	}

	public PTabItem getTab(String name) {
		return items.get(name);
	}

	public IPStackFrame getStackFrame() throws CoreException {
		ISelection selection = getViewSite().getPage().getSelection(IDebugUIConstants.ID_DEBUG_VIEW);
		if (selection instanceof StructuredSelection) {
			Object obj = ((StructuredSelection) selection).getFirstElement();
			if (obj instanceof IPStackFrame) {
				return (IPStackFrame) obj;
			}
		}
		/*
		 * IWorkbenchWindow activeWindow = PTPDebugUIPlugin.getActiveWorkbenchWindow();
		 * if (activeWindow == null)
		 * throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IPTPUIConstants.INTERNAL_ERROR,
		 * "No active window found", null));
		 * 
		 * IWorkbenchPage page = activeWindow.getActivePage();
		 * if (page == null)
		 * throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IPTPUIConstants.INTERNAL_ERROR,
		 * "No active page found", null));
		 * 
		 * IViewPart part = page.findView(IDebugUIConstants.ID_DEBUG_VIEW);
		 * if (part != null) {
		 * IDebugView adapter = (IDebugView)part.getAdapter(IDebugView.class);
		 * if (adapter != null) {
		 * ISelection selection = adapter.getViewer().getSelection();
		 * if (selection instanceof StructuredSelection) {
		 * Object obj = ((StructuredSelection)selection).getFirstElement();
		 * if (obj instanceof IPStackFrame) {
		 * return (IPStackFrame)obj;
		 * }
		 * }
		 * }
		 * }
		 */
		throw new CoreException(new Status(IStatus.ERROR, PTPDebugUIPlugin.getUniqueIdentifier(), IPTPUIConstants.INTERNAL_ERROR,
				Messages.PTabFolder_0, null));
	}

	public abstract void createTabItem(String tabName, Object selection) throws DebugException;
}
