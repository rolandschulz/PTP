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
package org.eclipse.ptp.internal.debug.ui.views.variable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.internal.debug.ui.IPTPDebugUIConstants;
import org.eclipse.ptp.internal.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.internal.debug.ui.UIDebugManager;
import org.eclipse.ptp.internal.debug.ui.PVariableManager.PVariableInfo;
import org.eclipse.ptp.internal.debug.ui.actions.AddPExpressionAction;
import org.eclipse.ptp.internal.debug.ui.actions.CompareValueAction;
import org.eclipse.ptp.internal.debug.ui.actions.DeletePExpressionAction;
import org.eclipse.ptp.internal.debug.ui.actions.EditPExpressionAction;
import org.eclipse.ptp.internal.debug.ui.actions.UpdatePExpressionAction;
import org.eclipse.ptp.internal.debug.ui.views.AbstractPDebugViewEventHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * @author Clement chu
 */
public class PVariableView extends AbstractDebugView implements ICheckStateListener {
	private AbstractPDebugViewEventHandler fEventHandler;
	private UIDebugManager uiManager = null;
	private PVariableCheckboxTableViewer viewer = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createViewer(org.eclipse.swt.widgets.Composite)
	 */
	protected Viewer createViewer(Composite parent) {
		uiManager = PTPDebugUIPlugin.getUIDebugManager();
		
		// add tree viewer
		viewer = new PVariableCheckboxTableViewer(parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		PVariableContentProvider contentProvider = new PVariableContentProvider();
		PVariableLabelProvider labelProvider = new PVariableLabelProvider();

		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(labelProvider);
		viewer.setCheckProvider(labelProvider);
		viewer.setSorter(new PVariableViewerSorter());
		viewer.setUseHashlookup(true);
		viewer.setInput(uiManager.getJobVariableManager());
		viewer.addCheckStateListener(this);
	
		viewer.addDoubleClickListener(new IDoubleClickListener() {
		    public void doubleClick(DoubleClickEvent event) {
		    	if (!getSelection().isEmpty()) {
		    		getAction(EditPExpressionAction.name).run();
		    	}
		    }
		});
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
		    public void selectionChanged(SelectionChangedEvent event) {
		    	updateActionsEnable();
		    }
		});
		setEventHandler(new PVariableViewEventHandler(this));
		return viewer;
	}
	public UIDebugManager getUIManager() {
		return uiManager;
	}
	public void refresh() {
		viewer.refresh();
		updateActionsEnable();
	}
	public ISelection getSelection() {
		return viewer.getSelection();
	}
	/**
	 * Sets the event handler for this view
	 * 
	 * @param eventHandler event handler
	 */
	protected void setEventHandler(AbstractPDebugViewEventHandler eventHandler) {
		this.fEventHandler = eventHandler;
	}
	/**
	 * Returns the event handler for this view
	 * 
	 * @return The event handler for this view
	 */
	protected AbstractPDebugViewEventHandler getEventHandler() {
		return this.fEventHandler;
	}	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createActions()
	 */
	protected void createActions() {
		setAction(AddPExpressionAction.name, new AddPExpressionAction(this));
		setAction(EditPExpressionAction.name, new EditPExpressionAction(this));
		setAction(DeletePExpressionAction.name, new DeletePExpressionAction(this));
		setAction(UpdatePExpressionAction.name, new UpdatePExpressionAction(this));
		setAction(CompareValueAction.name, new CompareValueAction(this));

		updateActionsEnable();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#configureToolBar(org.eclipse.jface.action.IToolBarManager)
	 */
	protected void configureToolBar(IToolBarManager toolBarMgr) {
		toolBarMgr.add(new Separator(IPTPDebugUIConstants.VAR_GROUP));

		toolBarMgr.appendToGroup(IPTPDebugUIConstants.VAR_GROUP, getAction(AddPExpressionAction.name));		
		toolBarMgr.appendToGroup(IPTPDebugUIConstants.VAR_GROUP, getAction(EditPExpressionAction.name));		
		toolBarMgr.appendToGroup(IPTPDebugUIConstants.VAR_GROUP, getAction(DeletePExpressionAction.name));		
		toolBarMgr.appendToGroup(IPTPDebugUIConstants.VAR_GROUP, getAction(UpdatePExpressionAction.name));
		toolBarMgr.add(new Separator());
		toolBarMgr.appendToGroup(IPTPDebugUIConstants.VAR_GROUP, getAction(CompareValueAction.name));
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	protected void fillContextMenu(IMenuManager menu) {
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(getAction(AddPExpressionAction.name));
		menu.add(getAction(EditPExpressionAction.name));
		menu.add(getAction(DeletePExpressionAction.name));
		menu.add(getAction(UpdatePExpressionAction.name));
		menu.add(new Separator());
		menu.add(getAction(CompareValueAction.name));
		updateObjects();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		viewer.removeCheckStateListener(this);
		super.dispose();
		if (getEventHandler() != null) {
			getEventHandler().dispose();
		}	
	}

	private boolean isEmpty() {
		return (viewer.getTable().getItemCount()==0);
	}
	private boolean isCurrentJobAvailable() {
		String cur_jid = uiManager.getCurrentJobId();
		return (cur_jid != null && cur_jid.length() > 0);		
	}
	public void updateActionsEnable() {
		getAction(AddPExpressionAction.name).setEnabled(isCurrentJobAvailable());
		getAction(EditPExpressionAction.name).setEnabled(!getSelection().isEmpty());
		getAction(DeletePExpressionAction.name).setEnabled(!getSelection().isEmpty());
		getAction(UpdatePExpressionAction.name).setEnabled(!isEmpty());
		getAction(CompareValueAction.name).setEnabled(!getSelection().isEmpty());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent)
	 */
	public void checkStateChanged(CheckStateChangedEvent event) {
		Object data = event.getElement();
		if (data instanceof PVariableInfo) {
			try {
				getUIManager().getJobVariableManager().updateVariableStatus((PVariableInfo)data, event.getChecked());
			}
			catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}
}
