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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.DrillDownComposite;

public class ParallelJobsView extends AbstractParallelView implements SelectionListener {
	public static ParallelJobsView instance = null;

	private ParallelElementLabelProvider labelProvider = new ParallelElementLabelProvider();

	private IPElement selectedElement = null;

	private TerminateAllAction terminateAllAction = null;

	/*
	private ShowProcessesAction showProcessesAction = null;

	private ShowAllNodesAction showAllNodesAction = null;

	private boolean SHOW_PROCESS_ONLY = false;
	*/

	//private TreeViewer treeViewer = null;
	private List jobsList;
	private Text jobDetails;

	public ParallelJobsView() {
		super();
		System.out.println("ParallelProcessesView starting up.");
		instance = this;
	}

	public void dispose() {
		super.dispose();
		instance = null;
	}

	public static ParallelJobsView getInstance() {
		// if (instance == null)
		// UIUtils.showView(UIUtils.ParallelProcessesView_ID);
		return instance;
	}

	public void reset(int style) {
		//setInput(launchManager.getUniverse(), style);
	}

	public void refresh(final IPElement[] elements) {
		Runnable runnable = new Runnable() {
			public void run() {
				for (int i = 0; i < elements.length; i++) {
					resetEntireList();
//					treeViewer.refresh(elements[i]);
				}
			}
		};
		execStyle(ASYN_STYLE, runnable);
	}

	public void refreshAll(int state, final boolean isUpdateLabel) {
		Runnable runnable = new Runnable() {
			public void run() {
				resetEntireList();
//				treeViewer.refresh(isUpdateLabel);
			}
		};
		execStyle(state, runnable);
	}

	public void refresh(final IPElement element) {
		Runnable runnable = new Runnable() {
			public void run() {
				resetEntireList();
//				treeViewer.refresh(element, true);
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
		//updateButton();
		initNode();
	}

	public void stopped() {
		System.out.println("ParallelProcessesView - stopped");
		// refresh Node only
		// refresh(launchManager.getProcessRoot().getNodes());
		//updateButton();
	}

	public void run() {
		System.out.println("ParallelProcessesView - run");
		//updateButton();
		//resetEntireList();
		refreshAll(ASYN_STYLE, true);
		// removerAllProcessViewer();
	}

	public void abort() {
		System.out.println("ParallelProcessesView - abort");
		// reset(ASYN_STYLE);
		//resetEntireList();
		refreshAll(ASYN_STYLE, true);
		//updateButton();
	}

	public void exit() {
		System.out.println("ParallelProcessesView - exit");
		//resetEntireList();
		//updateButton();
	}

	public void updatedStatusEvent() {
		// reset(ASYN_STYLE);
		//resetEntireList();
		refreshAll(ASYN_STYLE, true);
	}

	public void execStatusChangeEvent(Object object) {
		if (object instanceof IPElement)
			refresh((IPElement) object);

		//updateButton();
	}

	public void monitoringSystemChangeEvent(Object object) {
		//resetEntireList();
		//treeViewer.setInput(launchManager.getUniverse());
		//treeViewer.expandAll();
		refreshAll(ASYN_STYLE, true);
	}

	public void sysStatusChangeEvent(Object object) {
		// initNode();
		// reset(ASYN_STYLE);
		//resetEntireList();
		refreshAll(ASYN_STYLE, true);
	}

	public void processOutputEvent(Object object) {
	}

	public void errorEvent(Object object) {
		// refresh Node only
		// refresh(launchManager.getProcessRoot().getNodes());
		//updateButton();
	}

	public void updateButton() {
		//terminateAllAction
		//		.setEnabled(launchManager.getCurrentState() == IModelManager.STATE_RUN);

		/* boolean isEnabled = launchManager.isMPIRuning(); */
		//boolean isEnabled = true;
		//showAllNodesAction.setEnabled(isEnabled);
		//showProcessesAction.setEnabled(isEnabled);
		/*
		 * searchAction.setEnabled(isEnabled);
		 * viewStatusAction.setEnabled(isRuning);
		 * abortAction.setEnabled(isRuning); exitAction.setEnabled(isRuning);
		 */
	}

	private void initNode() {
		System.out.println("ParallelProcessView: initNode");
		//resetEntireList();
		reset(ASYN_STYLE);
	}

	public void setFocus() {
		setSelection();
	}

	public void createPartControl(Composite parent) {
		createAction();
		addActionsToToolbar();
		createControl(parent);
		//updateButton();
		registerViewer();
		System.out.println("ParallelProcessesView - calling initNode");
		initNode();
	}

	protected void createAction() {
		terminateAllAction = new TerminateAllAction(this);
		//showAllNodesAction = new ShowAllNodesAction(this);
		//showProcessesAction = new ShowProcessesAction(this);
		//showAllNodesAction.setChecked(true);

	}
	
	protected void addActionsToToolbar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars()
				.getToolBarManager();
		//toolbarManager.add(showAllNodesAction);
		//toolbarManager.add(showProcessesAction);

		//toolbarManager.add(new Separator());
		toolbarManager.add(terminateAllAction);

	}

	public Object[] getElements(Object parent) {
		if (parent instanceof IPElement) {
			switch (((IPElement) parent).getElementType()) {
			case IPElement.P_UNIVERSE:
				return ((IPUniverse) parent).getSortedJobs();
			case IPElement.P_JOB:
				return ((IPJob) parent).getSortedProcesses();
			/*
			 * case IPElement.P_UNIVERSE: return
			 * ((IPUniverse)parent).getSortedMachines(); case
			 * IPElement.P_MACHINE: return ((IPMachine)parent).getSortedNodes();
			 * case IPElement.P_NODE: return
			 * ((IPNode)parent).getSortedProcesses();
			 */
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

	/* occurs when someone clicks on an element on the list on the left */
	public void widgetSelected(SelectionEvent e) {
		System.out.println("TO-DO/NOTE: Selected a job, here we would check if the job was "+
			"running and if so enable the terminate button!");
		terminateAllAction.setEnabled(true);
		/* something like this code below - with some modifications */
		//terminateAllAction
		//		.setEnabled(launchManager.getCurrentState() == IModelManager.STATE_RUN);
		refreshRightSide();		
	}

	/* this is called when someone clicks on an element that they already had clicked
	 * on previously
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
	}
	
	private void refreshRightSide()
	{
		jobDetails.setText("");
		if(jobsList == null || jobsList.getItemCount() <= 0) return;
		int idx = jobsList.getSelectionIndex();
		int jobSize = jobsList.getItemCount();
		if(idx >= 0 && idx < jobSize) {
			String[] a = jobsList.getSelection();
			String name = a[0];
			/* find this job name */
			IPUniverse u = launchManager.getUniverse();
			IPJob j = u.findJobByName(name);
			if(j != null) {
				jobDetails.append("Job: "+j.toString()+"\n");
				jobDetails.append("Number of processes: "+j.totalProcesses()+"\n");
				jobDetails.append("Number of nodes used: "+j.totalNodes()+"\n");
			}
			else {
				jobDetails.append("ERROR: JOB NOT FOUND");
			}
		}
	}
	
	public String getSelectedJob()
	{
		if(jobsList == null || jobsList.getItemCount() <= 0) return null;
		String[] a = jobsList.getSelection();
		if(a == null || a.length <= 0) return null;
		return a[0];
	}
	
	private void resetEntireList()
	{
		if(jobsList == null || jobDetails == null) return;
		int idx = jobsList.getSelectionIndex();
		jobsList.removeAll();
		jobDetails.setText("");
		
		IPUniverse u = launchManager.getUniverse();
		IPJob jobs[] = u.getJobs();
		if(jobs.length <= 0) return;
		
		for(int i=0; i<jobs.length; i++) {
			jobsList.add(jobs[i].toString());
		}
		
		if(idx >= 0)
			jobsList.setSelection(idx);
		
		/* which index is selected - if any? */
		refreshRightSide();
	}

	protected void createControl(Composite parent) {
		Composite controlComp = new Composite(parent, SWT.NONE);
		controlComp.setLayout(new FormLayout());
		
		final Sash sash = new Sash(controlComp, SWT.VERTICAL);
		FormData data = new FormData();
		data.top = new FormAttachment(0, 0);
		data.bottom = new FormAttachment(100, 0);
		data.left = new FormAttachment(50, 0);
		sash.setLayoutData(data);
		
		jobsList = new List(controlComp, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
		data = new FormData();
		data.top = new FormAttachment(0, 0);
		data.bottom = new FormAttachment(100, 0);
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(sash, 0);
		jobsList.setLayoutData(data);
		
		jobDetails = new Text(controlComp, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL);
		data = new FormData();
		data.top = new FormAttachment(0, 0);
		data.bottom = new FormAttachment(100, 0);
		data.left = new FormAttachment(sash, 0);
		data.right = new FormAttachment(100, 0);
		jobDetails.setLayoutData(data);
		
		jobsList.addSelectionListener(this);
		/* need this to make the sash stay where you drag it to */
		sash.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				((FormData)sash.getLayoutData()).left = new FormAttachment(0, event.x);
				sash.getParent().layout();
			}
		});
		
		resetEntireList();
		/*
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
		*/
			/*
			 * protected void createTreeItem(Widget parent, Object element, int
			 * index) { Item item = newItem(parent, SWT.NULL, index);
			 * updateItem(item, element); updatePlus(item, element); if (element
			 * instanceof IPElement) ((IPElement)element).setData(item); }
			 */
		/*
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
		*/
		setSelection();
	}

	protected void openEditorAction(Object element) {
		System.out.println("PPV.openEditorAction(" + element + ")");
		if (element instanceof IPProcess) {
			openProcessViewer((IPProcess) element);
		}
	}

	private void setSelection() {
		/*
		if (selectedElement == null && !treeViewer.getSelection().isEmpty())
			return;

		if (selectedElement == null) {
			// treeViewer.getTree().setSelection(new TreeItem[] {
			// treeViewer.getTree().getTopItem() });
			treeViewer.setSelection(StructuredSelection.EMPTY);
		} else
			treeViewer.setSelection(new StructuredSelection(selectedElement));
	    */

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

	/*
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
	*/

	/*
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
	*/
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
	/*
		if (!found)
			UIUtils.showErrorDialog(getViewSite().getShell(),
					"Search not found", "Node " + nodeNumber
							+ " cannot be found", UIUtils.NORMAL);
	}*/

	/*
	public void searchForProcess(int processNumber) {

		boolean found = false;
		IPProcess process = launchManager.getUniverse().findProcessByName(
				String.valueOf(processNumber));
		if (process != null) {
			treeViewer.expandToLevel(process.getParent(), 1);
			treeViewer.setSelection(new StructuredSelection(process));
			found = true;

	*/
			/*
			 * Object data = process.getData(); if (data != null && data
			 * instanceof TreeItem) { treeViewer.getTree().setSelection(new
			 * TreeItem[] {(TreeItem) data}); found = true; }
			 */
	/*
		}
		if (!found)
			UIUtils.showErrorDialog(getViewSite().getShell(),
					"Search not found", "Process " + processNumber
							+ " cannot be found", UIUtils.NORMAL);
	}
	*/

	public void selectReveal(IPElement element) {
		/*
		System.out.println("SelectReveal called on: " + element);
		Control ctrl = treeViewer.getControl();
		if (ctrl == null || ctrl.isDisposed())
			return;

		selectedElement = element;
		if (SHOW_PROCESS_ONLY)
			showAllNodes(BUSY_STYLE);
		else
			setSelection();
			*/

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
		//getSite().setSelectionProvider(treeViewer);
	}

	
}
