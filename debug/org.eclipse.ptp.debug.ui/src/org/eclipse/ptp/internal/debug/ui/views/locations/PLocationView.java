/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.debug.ui.views.locations;

import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.debug.core.IPSession;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.core.model.IPDebugElement;
import org.eclipse.ptp.internal.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.internal.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.internal.debug.ui.UIDebugManager;
import org.eclipse.ptp.internal.debug.ui.actions.CreateLocationSetAction;
import org.eclipse.ptp.internal.debug.ui.messages.Messages;
import org.eclipse.ptp.internal.debug.ui.views.AbstractPDebugViewEventHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchActionConstants;

public class PLocationView extends AbstractDebugView implements IDebugContextListener {
	private AbstractPDebugViewEventHandler eventHandler;
	private UIDebugManager uiManager = null;
	private TableViewer viewer = null;

	public static final String COLUMN_FUNCTION = Messages.PLocationView_0;
	public static final String COLUMN_FILE = Messages.PLocationView_1;
	public static final String COLUMN_LINE = Messages.PLocationView_2;
	public static final String COLUMN_PROCESSES = Messages.PLocationView_3;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.AbstractDebugView#createViewer(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	protected Viewer createViewer(Composite parent) {
		uiManager = PTPDebugUIPlugin.getUIDebugManager();
		DebugUITools.getDebugContextManager().addDebugContextListener(this);

		// add table viewer
		viewer = new TableViewer(parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
		Table table = viewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));

		// create the table columns
		for (int i = 0; i < 4; i++) {
			new TableColumn(table, SWT.NULL).setResizable(true);
		}

		TableColumn[] columns = table.getColumns();
		columns[0].setText(COLUMN_FILE);
		columns[1].setText(COLUMN_FUNCTION);
		columns[2].setText(COLUMN_LINE);
		columns[3].setText(COLUMN_PROCESSES);

		for (int i = 0; i < 4; i++) {
			final int j = i; // Need finalness for closure
			columns[j].addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					((PLocationViewerSorter) viewer.getSorter()).setColumn(j);
					refresh();
				}
			});
		}

		PixelConverter pc = new PixelConverter(parent);
		columns[0].setWidth(pc.convertWidthInCharsToPixels(30));
		columns[1].setWidth(pc.convertWidthInCharsToPixels(30));
		columns[2].setWidth(pc.convertWidthInCharsToPixels(10));
		columns[3].setWidth(pc.convertWidthInCharsToPixels(10));

		viewer.setColumnProperties(new String[] { COLUMN_FUNCTION, COLUMN_FILE, COLUMN_LINE, COLUMN_PROCESSES });

		PLocationContentProvider contentProvider = new PLocationContentProvider();
		PLocationLabelProvider labelProvider = new PLocationLabelProvider();

		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(labelProvider);
		viewer.setSorter(new PLocationViewerSorter());
		viewer.setUseHashlookup(true);

		// initialize the viewer given the current debug context, if any
		updateContext(DebugUITools.getDebugContext());

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateActionsEnable();
			}
		});
		setEventHandler(new PLocationViewEventHandler(this));
		return viewer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractDebugView#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		DebugUITools.getDebugContextManager().removeDebugContextListener(this);
		if (getEventHandler() != null) {
			getEventHandler().dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.contexts.IDebugContextListener#debugContextChanged
	 * (org.eclipse.debug.ui.contexts.DebugContextEvent)
	 */
	public void debugContextChanged(DebugContextEvent event) {
		updateContext(event.getContext());
	}

	private void updateContext(Object context) {
		if (context instanceof IStructuredSelection) {
			Object selection = ((IStructuredSelection) context).getFirstElement();
			if (selection instanceof IPDebugElement) {
				setViewerContent(((IPDebugElement) selection).getSession());
			} else if (selection instanceof IPLaunch) {
				IPLaunch launch = (IPLaunch) selection;
				String jobId = launch.getJobId();
				if (jobId != null) {
					IPSession session = PTPDebugCorePlugin.getDebugModel().getSession(jobId);
					setViewerContent(session);
				}
			}
		}
	}

	public void setViewerContent(IPSession session) {
		if (viewer != null && viewer.getInput() != session) {
			viewer.setInput(session);
		}
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

	protected void setEventHandler(AbstractPDebugViewEventHandler eventHandler) {
		this.eventHandler = eventHandler;
	}

	protected AbstractPDebugViewEventHandler getEventHandler() {
		return this.eventHandler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractDebugView#createActions()
	 */
	@Override
	protected void createActions() {
		setAction(CreateLocationSetAction.ID, new CreateLocationSetAction(this));
		setAction(DOUBLE_CLICK_ACTION, getAction(CreateLocationSetAction.ID));
		updateActionsEnable();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.AbstractDebugView#configureToolBar(org.eclipse.jface
	 * .action.IToolBarManager)
	 */
	@Override
	protected void configureToolBar(IToolBarManager toolBarMgr) {
		toolBarMgr.add(getAction(CreateLocationSetAction.ID));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractDebugView#getHelpContextId()
	 */
	@Override
	protected String getHelpContextId() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.AbstractDebugView#fillContextMenu(org.eclipse.jface
	 * .action.IMenuManager)
	 */
	@Override
	protected void fillContextMenu(IMenuManager menu) {
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menu.add(getAction(CreateLocationSetAction.ID));
		updateObjects();
	}

	private boolean isCurrentJobAvailable() {
		String cur_jid = uiManager.getCurrentJobId();
		return (cur_jid != null && cur_jid.length() > 0);
	}

	public void updateActionsEnable() {
		getAction(CreateLocationSetAction.ID).setEnabled(!viewer.getSelection().isEmpty() && isCurrentJobAvailable());
	}

}
