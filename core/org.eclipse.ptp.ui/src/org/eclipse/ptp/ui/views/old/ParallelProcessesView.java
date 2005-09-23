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
package org.eclipse.ptp.ui.views.old;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.IPElement;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.core.IPNode;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPUniverse;
import org.eclipse.ptp.ui.actions.old.ShowAllNodesAction;
import org.eclipse.ptp.ui.actions.old.ShowProcessesAction;
import org.eclipse.ptp.ui.actions.old.TerminateAllAction;
import org.eclipse.ptp.ui.old.ParallelElementContentProvider;
import org.eclipse.ptp.ui.old.ParallelElementLabelProvider;
import org.eclipse.ptp.ui.old.UIMessage;
import org.eclipse.ptp.ui.old.UIUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.DrillDownComposite;

/**
 * 
 */
public class ParallelProcessesView extends AbstractParallelView {
	public static ParallelProcessesView instance = null;

	private ParallelElementLabelProvider labelProvider = new ParallelElementLabelProvider();

	private IPElement selectedElement = null;

	private TerminateAllAction terminateAllAction = null;

	private ShowProcessesAction showProcessesAction = null;

	private ShowAllNodesAction showAllNodesAction = null;

	private boolean SHOW_PROCESS_ONLY = false;

	private TreeViewer treeViewer = null;

	public ParallelProcessesView() {
		super();
		System.out.println("ParallelProcessesView starting up.");
		instance = this;
	}

	public void dispose() {
		super.dispose();
		instance = null;
	}

	public static ParallelProcessesView getInstance() {
		// if (instance == null)
		// UIUtils.showView(UIUtils.ParallelProcessesView_ID);
		return instance;
	}

	public void reset(int style) {
		setInput(launchManager.getUniverse(), style);
	}

	public void refresh(final IPElement[] elements) {
		Runnable runnable = new Runnable() {
			public void run() {
				for (int i = 0; i < elements.length; i++) {
					treeViewer.refresh(elements[i]);
				}
			}
		};
		execStyle(ASYN_STYLE, runnable);
	}

	public void refreshAll(int state, final boolean isUpdateLabel) {
		Runnable runnable = new Runnable() {
			public void run() {
				treeViewer.refresh(isUpdateLabel);
			}
		};
		execStyle(state, runnable);
	}

	public void refresh(final IPElement element) {
		Runnable runnable = new Runnable() {
			public void run() {
				treeViewer.refresh(element, true);
				// if (element.isAllStop())
				// treeViewer.refresh(element.getParent());
				/*
				 * Object data = element.getData(); if (data != null && data
				 * instanceof TreeItem) { TreeItem item = (TreeItem)data; if
				 * (!item.isDisposed())
				 * item.setForeground(labelProvider.getForeground(element)); }
				 */
			}
		};
		execStyle(ASYN_STYLE, runnable);
	}

	public void start() {
		System.out.println("ParallelProcessesView - start");
		updateButton();
		initNode();
	}

	public void stopped() {
		System.out.println("ParallelProcessesView - stopped");
		// refresh Node only
		// refresh(launchManager.getProcessRoot().getNodes());
		updateButton();
	}

	public void run(String arg) {
		System.out.println("ParallelProcessesView - run");
		updateButton();
		refreshAll(ASYN_STYLE, true);
		// removerAllProcessViewer();
	}

	public void abort() {
		System.out.println("ParallelProcessesView - abort");
		// reset(ASYN_STYLE);
		refreshAll(ASYN_STYLE, true);
		updateButton();
	}

	public void exit() {
		System.out.println("ParallelProcessesView - exit");
		updateButton();
	}

	public void updatedStatusEvent() {
		// reset(ASYN_STYLE);
		refreshAll(ASYN_STYLE, true);
	}

	public void execStatusChangeEvent(Object object) {
		if (object instanceof IPElement)
			refresh((IPElement) object);

		updateButton();
	}

	public void sysStatusChangeEvent(Object object) {
		// initNode();
		// reset(ASYN_STYLE);
		refreshAll(ASYN_STYLE, true);
	}

	public void processOutputEvent(Object object) {
	}

	public void errorEvent(Object object) {
		System.err.println("TEMPORARY: ParallelProcessesView.errorEvent().  Please mail the PTP devel"+
				"list with this message and how to replicate it.");
		// refresh Node only
		//refresh(launchManager.getUniverse().getNodes());
		updateButton();
	}

	public void updateButton() {
		terminateAllAction
				.setEnabled(launchManager.getCurrentState() == IModelManager.STATE_RUN);

		/* boolean isEnabled = launchManager.isMPIRuning(); */
		boolean isEnabled = true;
		showAllNodesAction.setEnabled(isEnabled);
		showProcessesAction.setEnabled(isEnabled);
		/*
		 * searchAction.setEnabled(isEnabled);
		 * viewStatusAction.setEnabled(isRuning);
		 * abortAction.setEnabled(isRuning); exitAction.setEnabled(isRuning);
		 */
	}

	private void initNode() {
		System.out.println("ParallelProcessView: initNode");
		reset(ASYN_STYLE);
	}

	public void setFocus() {
		setSelection();
	}

	public void createPartControl(Composite parent) {
		createAction();
		addActionsToToolbar();
		createControl(parent);
		updateButton();
		registerViewer();
		System.out.println("ParallelProcessesView - calling initNode");
		initNode();
	}

	protected void createAction() {
		terminateAllAction = new TerminateAllAction(this);
		showAllNodesAction = new ShowAllNodesAction(this);
		showProcessesAction = new ShowProcessesAction(this);
		showAllNodesAction.setChecked(true);
	}

	protected void addActionsToToolbar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars()
				.getToolBarManager();
		toolbarManager.add(showAllNodesAction);
		toolbarManager.add(showProcessesAction);

		toolbarManager.add(new Separator());
		toolbarManager.add(terminateAllAction);
	}

	public Object[] getElements(Object parent) {
		if (parent instanceof IPElement) {
			switch (((IPElement) parent).getElementType()) {
			case IPElement.P_UNIVERSE:
				return ((IPUniverse) parent).getSortedMachines();
			case IPElement.P_MACHINE:
				return ((IPMachine) parent).getSortedNodes();
			case IPElement.P_NODE:
				return ((IPNode) parent).getSortedProcesses();
			/*
			 * case IPElement.P_ROOT: if (SHOW_PROCESS_ONLY) return
			 * ((IPJob)parent).getSortedProcesses();
			 * 
			 * return ((IPJob)parent).getSortedNodes();
			 */

			/*
			 * case IPElement.P_NODE: return
			 * ((IPNode)parent).getSortedProcesses();
			 */
			}
		}
		return null;
	}

	protected void createControl(Composite parent) {
		System.out.println("ParallelProcessesView - createControl");
		Composite controlComp = new Composite(parent, SWT.NONE);
		controlComp.setLayout(new FillLayout());

		DrillDownComposite drillDown = new DrillDownComposite(controlComp,
				SWT.BORDER);
		drillDown.setLayout(createGridLayout(1, false, 0, 0));
		drillDown
				.setLayoutData(createDefaultGridData(GridData.FILL_HORIZONTAL));

		treeViewer = new TreeViewer(drillDown, SWT.H_SCROLL | SWT.V_SCROLL) {
			protected Object[] getFilteredChildren(Object parentElement) {
				Object[] objectArray = getElements(parentElement);
				if (objectArray != null)
					return objectArray;
				return new ArrayList().toArray();
			}
			/*
			 * protected void createTreeItem(Widget parent, Object element, int
			 * index) { Item item = newItem(parent, SWT.NULL, index);
			 * updateItem(item, element); updatePlus(item, element); if (element
			 * instanceof IPElement) ((IPElement)element).setData(item); }
			 */
		};
		drillDown.setChildTree(treeViewer);
		treeViewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				ISelection selection = event.getSelection();
				if (selection instanceof IStructuredSelection) {
					openEditorAction(((IStructuredSelection) selection)
							.getFirstElement());
				}
			}
		});

		treeViewer.setLabelProvider(labelProvider);
		treeViewer.setContentProvider(new ParallelElementContentProvider(false,
				false));
		treeViewer.setInput(launchManager.getUniverse());
		treeViewer.expandAll();
		setSelection();
	}

	public void monitoringSystemChangeEvent(Object object) {
		treeViewer.setInput(launchManager.getUniverse());
		treeViewer.expandAll();
	}

	protected void openEditorAction(Object element) {
		System.out.println("PPV.openEditorAction(" + element + ")");
		if (element instanceof IPProcess) {
			openProcessViewer((IPProcess) element);
		}
	}

	private void setSelection() {
		if (selectedElement == null && !treeViewer.getSelection().isEmpty())
			return;

		if (selectedElement == null) {
			// treeViewer.getTree().setSelection(new TreeItem[] {
			// treeViewer.getTree().getTopItem() });
			treeViewer.setSelection(StructuredSelection.EMPTY);
		} else
			treeViewer.setSelection(new StructuredSelection(selectedElement));

		/*
		 * Tree tree = treeViewer.getTree(); TreeItem item = null; if
		 * (selectedElement != null) { Object data = selectedElement.getData();
		 * if (data != null && data instanceof TreeItem) item = (TreeItem)data;
		 * 
		 * selectedElement = null; }
		 * 
		 * if (item == null) item = tree.getTopItem();
		 * 
		 * tree.setSelection(new TreeItem[] { item });
		 */
	}

	public void setInput(final Object input, int style) {
		Runnable runnable = new Runnable() {
			public void run() {
				treeViewer.setInput(input);
				treeViewer.expandAll();
				setSelection();
			}
		};
		execStyle(style, runnable);
	}

	private void showAllNodes(int style) {
		if (SHOW_PROCESS_ONLY) {
			SHOW_PROCESS_ONLY = false;
			showAllNodesAction.setChecked(true);
			showProcessesAction.setChecked(false);
			reset(style);
		}
	}

	public void showAllNodes() {
		if (SHOW_PROCESS_ONLY) {
			SHOW_PROCESS_ONLY = false;
			showAllNodesAction.setChecked(true);
			showProcessesAction.setChecked(false);
			reset(ASYN_STYLE);
		}
	}

	public void showProcesses() {
		if (!SHOW_PROCESS_ONLY) {
			SHOW_PROCESS_ONLY = true;
			showAllNodesAction.setChecked(false);
			showProcessesAction.setChecked(true);
			reset(ASYN_STYLE);
		}
	}

	public void showProcesses(int style) {
		if (!SHOW_PROCESS_ONLY) {
			SHOW_PROCESS_ONLY = true;
			showAllNodesAction.setChecked(false);
			showProcessesAction.setChecked(true);
			reset(style);
		}
	}

	public void searchForNode(int nodeNumber) {
		boolean found = false;
		/*
		 * IPNode node =
		 * launchManager.getProcessRoot().findNode(String.valueOf(nodeNumber));
		 */
		/*
		 * if (node != null) { showAllNodes(BUSY_STYLE);
		 * treeViewer.setSelection(new StructuredSelection(node)); found = true; /*
		 * Object data = node.getData(); if (data != null && data instanceof
		 * TreeItem) { treeViewer.getTree().setSelection(new TreeItem[]
		 * {(TreeItem) data}); found = true; }
		 */
		/*
		 * }
		 */
		if (!found)
			UIUtils.showErrorDialog(getViewSite().getShell(),
					"Search not found", "Node " + nodeNumber
							+ " cannot be found", UIUtils.NORMAL);
	}

	public void searchForProcess(int processNumber) {

		boolean found = false;
		IPProcess process = launchManager.getUniverse().findProcessByName(
				String.valueOf(processNumber));
		if (process != null) {
			treeViewer.expandToLevel(process.getParent(), 1);
			treeViewer.setSelection(new StructuredSelection(process));
			found = true;

			/*
			 * Object data = process.getData(); if (data != null && data
			 * instanceof TreeItem) { treeViewer.getTree().setSelection(new
			 * TreeItem[] {(TreeItem) data}); found = true; }
			 */
		}
		if (!found)
			UIUtils.showErrorDialog(getViewSite().getShell(),
					"Search not found", "Process " + processNumber
							+ " cannot be found", UIUtils.NORMAL);
	}

	public void selectReveal(IPElement element) {
		System.out.println("SelectReveal called on: " + element);
		Control ctrl = treeViewer.getControl();
		if (ctrl == null || ctrl.isDisposed())
			return;

		selectedElement = element;
		if (SHOW_PROCESS_ONLY)
			showAllNodes(BUSY_STYLE);
		else
			setSelection();

		/*
		 * if (element instanceof IPNode) { if (SHOW_PROCESS_ONLY)
		 * showAllNodes(BUSY_STYLE); else setSelection(); } else if (element
		 * instanceof IPProcess) { if (SHOW_PROCESS_ONLY) setSelection(); else
		 * showProcesses(BUSY_STYLE); }
		 */
	}

	public void registerViewer() {
		// Register viewer with site. This must be done before making the
		// actions.
		getSite().setSelectionProvider(treeViewer);
	}
}
