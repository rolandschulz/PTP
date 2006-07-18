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
package org.eclipse.ptp.debug.internal.ui.views.variable;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.debug.internal.ui.UIDebugManager;
import org.eclipse.ptp.debug.internal.ui.actions.AddPExpressionAction;
import org.eclipse.ptp.debug.internal.ui.views.AbstractPDebugEventHandler;
import org.eclipse.ptp.debug.ui.IPTPDebugUIConstants;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.ui.listeners.IJobChangedListener;
import org.eclipse.ptp.ui.listeners.ISetListener;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * @author Clement chu
 */
public class PVariableView extends AbstractDebugView implements IPropertyChangeListener, IJobChangedListener, ISetListener {
	private AbstractPDebugEventHandler fEventHandler;
	private UIDebugManager uiManager = null;
	private PVariableViewer viewer = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createViewer(org.eclipse.swt.widgets.Composite)
	 */
	protected Viewer createViewer(Composite parent) {
		uiManager = PTPDebugUIPlugin.getUIDebugManager();
		PTPDebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
		
		// add tree viewer
		viewer = new PVariableViewer(parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		viewer.setContentProvider(new PVariableContentProvider());
		viewer.setLabelProvider(new PVariableLabelProvider());
		viewer.setSorter(new PVariableViewerSorter());
		viewer.setUseHashlookup(true);
		viewer.setInput(uiManager.getJobVariableManager());
	
		uiManager.addJobChangedListener(this);
		uiManager.addSetListener(this);
		setEventHandler(new PVariableViewEventHandler(this));
		
		return viewer;
	}
	public UIDebugManager getUIManager() {
		return uiManager;
	}
	public void refresh() {
		viewer.refresh();
	}
	/**
	 * Sets the event handler for this view
	 * 
	 * @param eventHandler event handler
	 */
	protected void setEventHandler(AbstractPDebugEventHandler eventHandler) {
		this.fEventHandler = eventHandler;
	}
	/**
	 * Returns the event handler for this view
	 * 
	 * @return The event handler for this view
	 */
	protected AbstractPDebugEventHandler getEventHandler() {
		return this.fEventHandler;
	}	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createActions()
	 */
	protected void createActions() {
		setAction(AddPExpressionAction.name, new AddPExpressionAction(this));

		String cur_jid = uiManager.getCurrentJobId();
		setActionsEnable(cur_jid != null && cur_jid.length() > 0);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#configureToolBar(org.eclipse.jface.action.IToolBarManager)
	 */
	protected void configureToolBar(IToolBarManager toolBarMgr) {
		toolBarMgr.add(new Separator(IPTPDebugUIConstants.ADD_VAR_GROUP));

		toolBarMgr.appendToGroup(IPTPDebugUIConstants.ADD_VAR_GROUP, getAction(AddPExpressionAction.name));		
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
		updateObjects();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		PTPDebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		uiManager.removeSetListener(this);
		uiManager.removeJobChangedListener(this);
		super.dispose();
		if (getEventHandler() != null) {
			getEventHandler().dispose();
		}	
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.listeners.IJobChangedListener#jobChangedEvent(java.lang.String, java.lang.String)
	 */
	public void jobChangedEvent(final String cur_job_id, final String pre_job_id) {
		WorkbenchJob uiJob = new WorkbenchJob("Updating annotation...") {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				doJobChangedEvent(cur_job_id, pre_job_id, monitor);
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		uiJob.setSystem(true);
		uiJob.setPriority(Job.INTERACTIVE);
		uiJob.schedule();		
	}
	private void doJobChangedEvent(String cur_job_id, String pre_job_id, IProgressMonitor monitor) {
		setActionsEnable(cur_job_id != null && cur_job_id.length() > 0);
	}
	public void setActionsEnable(boolean enable) {
		getAction(AddPExpressionAction.name).setEnabled(enable);
	}
	public void deleteSetEvent(IElementSet set) {
		refresh();
	}
	public void changeSetEvent(IElementSet currentSet, IElementSet preSet) {}
	public void createSetEvent(IElementSet set, IElement[] elements) {}
	public void addElementsEvent(IElementSet set, IElement[] elements) {}
	public void removeElementsEvent(IElementSet set, IElement[] elements) {}
}
