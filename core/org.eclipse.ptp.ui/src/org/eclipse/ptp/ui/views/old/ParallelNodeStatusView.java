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

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.IPElement;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.core.IPNode;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.IPUniverse;
import org.eclipse.ptp.core.IParallelModelListener;
import org.eclipse.ptp.internal.core.PNode;
import org.eclipse.ptp.internal.core.PProcess;
import org.eclipse.ptp.ui.actions.old.ShowAllNodesAction;
import org.eclipse.ptp.ui.actions.old.ShowLegendAction;
import org.eclipse.ptp.ui.actions.old.ShowMyAllocatedNodesAction;
import org.eclipse.ptp.ui.actions.old.ShowMyUsedNodesAction;
import org.eclipse.ptp.ui.old.ParallelImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * @author ndebard
 */
public class ParallelNodeStatusView extends AbstractParallelView implements
		IParallelModelListener, ISelectionProvider {
	public static ParallelNodeStatusView instance = null;

	//private TerminateAllAction terminateAllAction = null;

	// private SearchAction searchAction = null;
	private ShowAllNodesAction showAllNodesAction = null;

	private ShowMyAllocatedNodesAction showMyAllocNodesAction = null;

	private ShowMyUsedNodesAction showMyUsedNodesAction = null;

	private ShowLegendAction showLegendAction = null;

	// private ShowProcessesAction showProcessesAction = null;

	protected IPElement[] displayElements = null;

	protected IPUniverse universe = null;

	protected int machine_number = -1;

	/* images */
	public static Image[][] statusImages = {
			{
					/* node allocated by this user - exclusively */
					ParallelImages
							.getImage(ParallelImages.IMG_NODE_USER_ALLOC_EXCL),
					ParallelImages
							.getImage(ParallelImages.IMG_NODE_USER_ALLOC_EXCL_SEL) },
			{
					/* node allocated by this user - shared */
					ParallelImages
							.getImage(ParallelImages.IMG_NODE_USER_ALLOC_SHARED),
					ParallelImages
							.getImage(ParallelImages.IMG_NODE_USER_ALLOC_SHARED_SEL) },
			{
					/* node allocated by another user - exclusively */
					ParallelImages
							.getImage(ParallelImages.IMG_NODE_OTHER_ALLOC_EXCL),
					ParallelImages
							.getImage(ParallelImages.IMG_NODE_OTHER_ALLOC_EXCL_SEL) },
			{
					/* node allocated by another user - shared */
					ParallelImages
							.getImage(ParallelImages.IMG_NODE_OTHER_ALLOC_SHARED),
					ParallelImages
							.getImage(ParallelImages.IMG_NODE_OTHER_ALLOC_SHARED_SEL) },
			{
			/* node is down */
			ParallelImages.getImage(ParallelImages.IMG_NODE_DOWN),
					ParallelImages.getImage(ParallelImages.IMG_NODE_DOWN_SEL) },
			/* node is in some sort of error state */
			{ ParallelImages.getImage(ParallelImages.IMG_NODE_ERROR),
					ParallelImages.getImage(ParallelImages.IMG_NODE_ERROR_SEL), },
			{
			/* node has processes which have exited */
			ParallelImages.getImage(ParallelImages.IMG_NODE_EXITED),
					ParallelImages.getImage(ParallelImages.IMG_NODE_EXITED_SEL) },
			{
					/* node has running processes */
					ParallelImages.getImage(ParallelImages.IMG_NODE_RUNNING),
					ParallelImages
							.getImage(ParallelImages.IMG_NODE_RUNNING_SEL) },
			{
					/* node catch-all - undefined */
					ParallelImages.getImage(ParallelImages.IMG_NODE_UNKNOWN),
					ParallelImages
							.getImage(ParallelImages.IMG_NODE_UNKNOWN_SEL) },
			{
			/* node is up */
			ParallelImages.getImage(ParallelImages.IMG_NODE_UP),
					ParallelImages.getImage(ParallelImages.IMG_NODE_UP_SEL) },
			{ ParallelImages.getImage(ParallelImages.IMG_PROC_ERROR),
					ParallelImages.getImage(ParallelImages.IMG_PROC_ERROR_SEL) },
			{ ParallelImages.getImage(ParallelImages.IMG_PROC_EXITED),
					ParallelImages.getImage(ParallelImages.IMG_PROC_EXITED_SEL) },
			{
					ParallelImages
							.getImage(ParallelImages.IMG_PROC_EXITED_SIGNAL),
					ParallelImages
							.getImage(ParallelImages.IMG_PROC_EXITED_SIGNAL_SEL) },
			{
					ParallelImages.getImage(ParallelImages.IMG_PROC_RUNNING),
					ParallelImages
							.getImage(ParallelImages.IMG_PROC_RUNNING_SEL) },
			{
					ParallelImages.getImage(ParallelImages.IMG_PROC_STARTING),
					ParallelImages
							.getImage(ParallelImages.IMG_PROC_STARTING_SEL) },
			{
					ParallelImages.getImage(ParallelImages.IMG_PROC_STOPPED),
					ParallelImages
							.getImage(ParallelImages.IMG_PROC_STOPPED_SEL) },

	};

	/* indices into the above image array */
	public final static int NODE_USER_ALLOC_EXCL = 0;

	public final static int NODE_USER_ALLOC_SHARED = 1;

	public final static int NODE_OTHER_ALLOC_EXCL = 2;

	public final static int NODE_OTHER_ALLOC_SHARED = 3;

	public final static int NODE_DOWN = 4;

	public final static int NODE_ERROR = 5;

	public final static int NODE_EXITED = 6;

	public final static int NODE_RUNNING = 7;

	public final static int NODE_UNKNOWN = 8;

	public final static int NODE_UP = 9;

	public final static int PROC_ERROR = 10;

	public final static int PROC_EXITED = 11;

	public final static int PROC_EXITED_SIGNAL = 12;

	public final static int PROC_RUNNING = 13;

	public final static int PROC_STARTING = 14;

	public final static int PROC_STOPPED = 15;

	/* indices into the 2D inner arrays */
	public final static int NOT_SELECTED = 0;

	public final static int SELECTED = 1;

	/*
	 * these UI components are global so that different methods can force
	 * updates on them and change their contents
	 */
	private ScrolledComposite sc;

	// protected List text;
	// protected List list;
	protected Table BRtable;

	protected Table BLtable;

	private Composite c, bottomOut;

	private Shell toolTipShell;

	private Label toolTipLabel;

	/* the number of nodes on the cluster */
	int num_nodes = 0;

	/* variables used to keep status of the nodes and the session */
	/*
	 * PORT private IPNode launchedNodes[];
	 */
	private IPProcess[] processList;

	/*
	 * a silly flag used to force a refresh after the MPI application starts
	 * running
	 */
	private boolean runflag = false;

	/*
	 * these globals pertain to how the canvas is drawn, such as offsets and
	 * spacings between nodes.
	 */
	final int x_offset = 10;

	final int x_spacing = 4;

	final int y_offset = 20;

	final int y_spacing = 4;

	final int node_width = 16;

	final int node_height = 16;

	/* just a default, this will be overwritten after a job starts */
	int visible_node_cols = 40;

	private int last_tooltip_node = -1;

	/*
	 * the node, if any, that the user has selected with their mouse - we draw a
	 * box around that node
	 */
	int selected_node_num = -1;

	/* a cheesy variable needed for the list */
	int last_node_num = -1;

	/* used in filtering */
	final int ALL_NODES = 0;

	final int ALLOCATED_NODES = 1;

	final int USED_NODES = 2;

	/* default filter */
	int filterSelection = ALL_NODES;

	final int NODES = 0;

	final int PROCESSES = 1;

	private int mode = NODES;

	public ParallelNodeStatusView() {
		super();
		instance = this;
	}

	public void dispose() {
		super.dispose();
		instance = null;
	}

	public static ParallelNodeStatusView getInstance() {
		// if (instance == null)
		// UIUtils.showView(UIUtils.ParallelNodeStatusView_ID);
		return instance;
	}

	/*
	 * called to refresh the GUI - this asyncExec is needed because there are
	 * many threads running around in there calling different things and the
	 * event threads cannot access GUI controls, this allows us to fire off an
	 * update of the GUI async
	 */
	public void refresh(final boolean resize, final IPElement el) {
		if(sc == null) return;
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (runflag) {
					sc.notifyListeners(SWT.Resize, new Event());
					runflag = false;
				}

				if (resize)
					computeNewCanvasSize();

				if (el == null) {
					// System.out.println("___ WARNING: FULL SCREEN REDRAW!!!
					// ____");
					c.redraw();
				}

				else {
					int index = -1;
					if (el instanceof PProcess) {
						index = ((PProcess) el).getID();
					} else if (el instanceof PNode) {
						index = IPNodeToSystemIndex((PNode) el);
						// System.out.println("NodeRedraw - node = "+(PNode)el);
						// System.out.println("index = "+index);
					}
					if (index >= 0) {
						int x, y, width, height, x_loc, y_loc;
						width = node_width;
						height = node_height;

						x = index % visible_node_cols;
						y = index / visible_node_cols;

						x_loc = x_offset + x_spacing * (x + 1) + node_width * x;
						y_loc = y_offset + y_spacing
								* ((index / visible_node_cols) + 1)
								+ node_height * (index / visible_node_cols);

						c.redraw(x_loc, y_loc, width, height, false);
					}
				}

				updateLowerTextRegions();
			}
		});
	}

	private void filterNodes(boolean user_changed_filter) {
		grabSystemStatus();

		if (filterSelection != ALL_NODES) {
			IPElement[] tmpNodes = new IPElement[displayElements.length];

			int copied_elements = 0;
			// we're going to go through the displayElements array and choose to
			// copy elements
			// into tmpNodes as necessary - as we do so we'll increment a
			// variable that
			// will help us to keep track of our progress through this array

			for (int i = 0; i < displayElements.length; i++) {
				IPNode somenode = (IPNode) displayElements[i];
				if (filterSelection == USED_NODES) {
					if (somenode != null && somenode.hasChildProcesses()) {
						IPProcess procs[] = somenode.getProcesses();
						if (procs != null && procs.length > 0) {
							tmpNodes[copied_elements] = displayElements[i];
							copied_elements++;
						}
					}
				} else if (filterSelection == ALLOCATED_NODES
						&& ((String) somenode.getAttribute("user")).equals(System
								.getProperty("user.name"))) {
					tmpNodes[copied_elements] = displayElements[i];
					copied_elements++;
				}
			}
			System.out.println("FILTER: of " + displayElements.length
					+ " nodes I filtered down to " + copied_elements);
			// reallocate that array and then copy our elements into it
			displayElements = new IPElement[copied_elements];
			System.arraycopy(tmpNodes, 0, displayElements, 0, copied_elements);
		}

		if (user_changed_filter)
			selected_node_num = -1;

		if(displayElements == null) num_nodes = 0;
		else num_nodes = displayElements.length;

		/*
		 * PORT if(session == null) return;
		 *  // refresh the sysDescNodes array grabSystemStatus();
		 * 
		 * if(filterSelection != ALL_NODES) { MISystemDescription[] tmpNodes =
		 * new MISystemDescription[sysDescNodes.length]; int copied_elements =
		 * 0; // we're going to go through the sysDescNodes array and choose to
		 * copy elements // into tmpNodes as necessary - as we do so we'll
		 * increment a variable that // will help us to keep track of our
		 * progress through this array // for(int i=0; i<sysDescNodes.length;
		 * i++) { if(filterSelection == USED_NODES) { IPNode somenode =
		 * systemNodeToIPNode(sysDescNodes[i].getNode()); if(somenode != null &&
		 * somenode.hasChildren()) { IPProcess procs[] =
		 * somenode.getProcesses(); if(procs != null && procs.length > 0) {
		 * tmpNodes[copied_elements] = sysDescNodes[i]; copied_elements++; } } }
		 * else if(filterSelection == ALLOCATED_NODES &&
		 * sysDescNodes[i].getBprocUser().equals(System.getProperty("user.name"))) {
		 * tmpNodes[copied_elements] = sysDescNodes[i]; copied_elements++; } }
		 * System.out.println("FILTER: of "+sysDescNodes.length+" nodes I
		 * filtered down to "+copied_elements); // reallocate that array and
		 * then copy our elements into it sysDescNodes = new
		 * MISystemDescription[copied_elements]; System.arraycopy(tmpNodes, 0,
		 * sysDescNodes, 0, copied_elements); }
		 * 
		 * if(user_changed_filter) selected_node_num = -1;
		 * 
		 * num_nodes = sysDescNodes.length;
		 */
		refresh(true, null);
	}

	private void updateSystemStatus() {
		filterNodes(false);
	}

	/*
	 * fill the array sysDescNodes[] with the status of the system at current
	 * time
	 */
	private void grabSystemStatus() {
		/*
		 * try { MISysStatus sysStatus = new MISysStatus();
		 * session.postCommand(sysStatus); sysDescNodes =
		 * sysStatus.getMISysStatusInfo().getMISystemDescription(); } catch
		 * (MIException e) { }
		 */
		if(universe == null) return;
		universe = launchManager.getUniverse();

		IPMachine[] macs = universe.getSortedMachines();

		/*
		 * machine_number = 0;
		 */
		displayElements = macs[machine_number].getSortedNodes();
		System.out.println("DisplayElements size = "+displayElements.length);
		num_nodes = displayElements.length;
	}

	private void getInitialStatus() {
		System.out.println("GET INITIAL STATUS()");
		/*
		 * session = launchManager.getSession(); if (session == null) return;
		 */

		runflag = true;

		updateSystemStatus();

		updateButton();
		refreshAllProcsStatus();
		refresh(false, null);
	}

	/*
	 * this method will be called when the all processes are stopped
	 */
	public void stopped() {
		updateButton();
	}

	public void start() {
		getInitialStatus();
	}

	public void run(String arg) {
		System.out.println("ParallelNodeStatusView - run");
		getInitialStatus();
		// removerAllProcessViewer();
	}

	public void abort() {
		System.out.println("ParallelNodeStatusView - abort");
		updateSystemStatus();
		refreshAllProcsStatus();
		updateButton();
		/*
		 * updateSystemStatus(); refreshAllProcsStatus(); refresh(false);
		 */
	}

	public void exit() {
		System.out.println("ParallelNodeStatusView - exit");
		/*
		 * updateSystemStatus(); refreshAllProcsStatus(); updateButton();
		 * refresh(false);
		 */
	}

	public void updatedStatusEvent() {
		// System.out.println("ParallelNodeStatusView - status");
		refreshAllProcsStatus();
		updateButton();
		refresh(false, null);
	}

	/*
	 * iterates through the IPNode array to find the one that corresponds to a
	 * particular node number
	 */
	private IPNode systemNodeToIPNode(String nodeNumber) {
		if (displayElements == null)
			return null;

		for (int i = 0; i < displayElements.length; i++) {
			if (!(displayElements[i] instanceof IPNode)) {
				System.out
						.println("**** ERROR ERROR - called systemNodeToIPNode and iterated through an element that wasn't an IPNode!");
			} else {
				if (((IPNode) displayElements[i]).getNodeNumber().equals(
						nodeNumber))
					return (IPNode) displayElements[i];
			}
		}

		return null;

		/*
		 * PORT
		 * 
		 * if(launchedNodes == null) return null; for(int i=0; i<launchedNodes.length;
		 * i++) { if(launchedNodes[i].getNodeNumber().equals(nodeNumber)) return
		 * launchedNodes[i]; } return null;
		 */
	}

	private int IPNodeToSystemIndex(IPNode node) {
		if (node == null || displayElements == null)
			return -1;

		for (int i = 0; i < displayElements.length; i++) {
			if (displayElements[i] == node)
				return i;
		}

		/*
		 * PORT for(int i=0; i<sysDescNodes.length; i++) {
		 * if(sysDescNodes[i].getNode().equals(node.getNodeNumber())) return i; }
		 */
		return -1;

	}

	public void refreshAllProcsStatus() {
		/*
		 * PORT IPRoot root = launchManager.getProcessRoot(); if(root != null)
		 * launchedNodes = root.getNodes(); else { launchedNodes = null;
		 * //System.out.println("!!!!!!!!!!! NULL PROCESS ROOT!"); }
		 * fillUpProcessArray(); refresh(true, null);
		 */
	}

	public void refreshOneProcStatus(PProcess proc) {
		/*
		 * System.out.println("old status =
		 * "+processList[proc.getKeyNumber()].getStatus());
		 * processList[proc.getKeyNumber()] = proc; System.out.println("new
		 * status = "+processList[proc.getKeyNumber()].getStatus());
		 */
		refresh(true, proc);
	}

	private void fillUpProcessArray() {
		System.out.println("*** fillUpProcessArray");
		/* run through and count all processes */

		/*
		 * PORT int num_procs = 0; for(int i=0; i<launchedNodes.length; i++) {
		 * if(launchedNodes[i].hasChildren()) num_procs +=
		 * launchedNodes[i].getProcesses().length; } processList = new
		 * IPProcess[num_procs];
		 * 
		 * for(int i=0; i<launchedNodes.length; i++) {
		 * if(launchedNodes[i].hasChildren()) { IPProcess tmp[] =
		 * getSortedProcessList(launchedNodes[i]);
		 * 
		 * for(int j=0; j<tmp.length; j++) { processList[tmp[j].getKeyNumber()] =
		 * (IPProcess) tmp[j]; } } }
		 */
	}

	public IPProcess[] getSortedProcessList(IPNode node) {
		if (node == null || !node.hasChildProcesses())
			return null;

		IPProcess tmp[] = node.getProcesses();
		Arrays.sort(tmp);

		return tmp;
	}

	protected int setupMachineMenu()
	{
		universe = launchManager.getUniverse();
		
		IMenuManager menuMgr = getViewSite().getActionBars().getMenuManager();
		
		if(universe == null) {
			menuMgr.removeAll();
			menuMgr.add(new Action("<No Machines Found>", IAction.AS_RADIO_BUTTON) {
				public void run() {
				}
			});
			return 0;	
		}

		IPMachine[] macs = universe.getSortedMachines();

		machine_number = 0;

		menuMgr.removeAll();
		for (int i = 0; i < macs.length; i++) {
			final int mnum = i;
			menuMgr.add(new Action("Machine " + (mnum + 1),
					IAction.AS_RADIO_BUTTON) {
				public void run() {
					System.out.println("MACHINE " + (mnum + 1)
							+ " now being displayed.");
					machine_number = mnum;
					grabSystemStatus();
					computeNewCanvasSize();
					updateButton();
					refreshAllProcsStatus();
					refresh(false, null);
				}
			});
		}
		
		return 1;
	}

	protected void createControl(Composite parent)
	{

		universe = launchManager.getUniverse();
		if(setupMachineMenu() == 0) {
			//return;
		}
		
		/*
		 * this.getViewSite().getShell().addFocusListener(new FocusAdapter() {
		 * public void focusLost(FocusEvent e) { System.out.println("focus
		 * lost"); } public void focusGained(FocusEvent e) {
		 * System.out.println("focus gained"); } });
		 */

		sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		ScrollBar vbar = sc.getVerticalBar();
		/*
		 * makes it so that our scrollbar steps in sizes approximately equal to
		 * a node's heightf
		 */
		vbar.setIncrement(node_height + y_spacing);
		vbar.setPageIncrement(node_height + y_spacing);

		c = new Composite(sc, SWT.NONE);
		// c.setBackground(white);
		sc.setContent(c);

		GridLayout glayout = new GridLayout();
		c.setLayout(glayout);
		c.setSize(700, 500);

		FormLayout layout = new FormLayout();
		parent.setLayout(layout);

		/* setup the form layout for the top 'node area' box */
		FormData compositeData = new FormData();
		compositeData.top = new FormAttachment(0);
		compositeData.left = new FormAttachment(0);
		compositeData.right = new FormAttachment(100);
		compositeData.bottom = new FormAttachment(65);
		sc.setLayoutData(compositeData);

		/* setup the form data for the text area */
		FormData bottomData = new FormData();
		bottomData.left = new FormAttachment(0);
		bottomData.right = new FormAttachment(100);
		bottomData.bottom = new FormAttachment(100);
		bottomData.top = new FormAttachment(sc, 20);

		/*
		 * this is kind of crazy - i need two types of layouts on the bottom
		 * region so I'll nest two composites inside of each other, each with
		 * the appropriate layout
		 */

		/*
		 * outer bottom composite - this one has a form layout to fit nicely
		 * with the canvas above
		 */
		bottomOut = new Composite(parent, SWT.BORDER);
		bottomOut.setLayout(new FillLayout());
		bottomOut.setLayoutData(bottomData);

		/* inner bottom composite - this one uses a grid layout */
		Composite bottom = new Composite(bottomOut, SWT.NONE);
		bottom.setLayout(new GridLayout(2, true));

		Group bleft = new Group(bottom, SWT.BORDER);
		bleft.setLayout(new FillLayout());
		bleft.setText("Node Info");
		Group bright = new Group(bottom, SWT.BORDER);
		bright.setLayout(new FillLayout());
		bright.setText("Process Info");

		BLtable = new Table(bleft, SWT.FULL_SELECTION | SWT.BORDER
				| SWT.H_SCROLL | SWT.V_SCROLL);
		BLtable.setHeaderVisible(false);
		BLtable.setLinesVisible(true);
		TableColumn col1 = new TableColumn(BLtable, SWT.LEFT);
		col1.setWidth(50);
		TableColumn col2 = new TableColumn(BLtable, SWT.LEFT);
		col2.setWidth(80);

		BRtable = new Table(bright, SWT.FULL_SELECTION | SWT.BORDER
				| SWT.H_SCROLL | SWT.V_SCROLL);
		BRtable.setHeaderVisible(false);
		BRtable.setLinesVisible(true);
		col1 = new TableColumn(BRtable, SWT.LEFT);
		col1.setWidth(25);
		col2 = new TableColumn(BRtable, SWT.LEFT);
		col2.setWidth(120);

		GridData gdtext = new GridData();
		gdtext.grabExcessVerticalSpace = true;
		gdtext.grabExcessHorizontalSpace = true;
		gdtext.horizontalAlignment = GridData.FILL;
		gdtext.verticalAlignment = GridData.FILL;
		bleft.setLayoutData(gdtext);

		GridData gdlist = new GridData();
		gdlist.grabExcessVerticalSpace = true;
		gdlist.grabExcessHorizontalSpace = true;
		gdlist.horizontalAlignment = GridData.FILL;
		gdlist.verticalAlignment = GridData.FILL;
		bright.setLayoutData(gdlist);

		BRtable.addSelectionListener(new SelectionListener() {
			/* single click - do nothing */
			public void widgetSelected(SelectionEvent e) {
			}

			/* double click - throw up an editor to look at the process */
			public void widgetDefaultSelected(SelectionEvent e) {
				int idx = BRtable.getSelectionIndex();

				if (mode == NODES) {
					IPNode somenode = (IPNode) displayElements[selected_node_num];
					/*
					 * PORT IPNode somenode =
					 * systemNodeToIPNode(sysDescNodes[selected_node_num].getNode());
					 */

					if (somenode != null) {
						IPProcess procs[] = getSortedProcessList(somenode);
						if (idx >= 0 && idx < procs.length)
							launchProcessViewer(procs[idx]);
					}
				} else if (mode == PROCESSES) {
					launchProcessViewer(processList[selected_node_num]);
				}
			}
		});

		sc.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event event) {
				// System.out.println("RESIZE!");
				computeNewCanvasSize();
			}
		});

		c.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				// System.out.println("PAINT: x="+e.x+", y="+e.y+",
				// width="+e.width+", height="+e.height);
				paintCanvas(e.gc);
			}
		});

		Listener mouseListener = new Listener() {
			public void handleEvent(Event e) {
				/*
				 * only bother sending it to the bounds tester if they released
				 * the mouse - signifying a click
				 */
				if (e.type == SWT.MouseDown || e.type == SWT.MouseDoubleClick
						|| e.type == SWT.MouseMove) {
					Object ob = testBounds(e.x, e.y, e.type);
					if (ob != null) {
						if (ob instanceof IPElement) {
							refresh(false, (IPElement) ob);
						} else if (ob instanceof Boolean) {
							Boolean bool = (Boolean) ob;
							if (bool.booleanValue() == true) {
								/*
								 * true means they showed a tool tip - we don't
								 * have to do anything
								 */
							} else {
								hideToolTip();
							}
						}
					} else {
						refresh(false, null);
					}
				} else if (e.type == SWT.MouseExit) {
					hideToolTip();
				}
			}
		};

		c.addListener(SWT.MouseDown, mouseListener);
		c.addListener(SWT.MouseDoubleClick, mouseListener);
		c.addListener(SWT.MouseMove, mouseListener);
	}

	public void updateButton() {
		boolean isRunning = launchManager.getCurrentState() == IModelManager.STATE_RUN;
		// System.out.println("updateButton - isMPIRunning? "+isRuning);

		//terminateAllAction.setEnabled(isRunning);

		// searchAction.setEnabled(isRuning);
		showAllNodesAction.setEnabled(isRunning);
		showMyAllocNodesAction.setEnabled(isRunning);
		showMyUsedNodesAction.setEnabled(isRunning);
		// showLegendAction.setEnabled(isRuning);
		// showProcessesAction.setEnabled(isRuning);
	}

	/* we don't do anything special on focus, yet */
	public void setFocus() {
	}

	public void createPartControl(Composite parent) {
		createAction();
		addActionsToToolbar();
		createControl(parent);
		getInitialStatus();
		registerViewer();
		// updateButton();
	}

	protected void createAction() {
		//terminateAllAction = new TerminateAllAction(this);
		// searchAction = new SearchAction(this);
		showAllNodesAction = new ShowAllNodesAction(this);
		showMyAllocNodesAction = new ShowMyAllocatedNodesAction(this);
		showMyUsedNodesAction = new ShowMyUsedNodesAction(this);
		// showLegendAction = new ShowLegendAction(this);
		// showProcessesAction = new ShowProcessesAction(this);

		showAllNodesAction.setChecked(true);
	}

	protected void addActionsToToolbar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars()
				.getToolBarManager();

		// toolbarManager.add(showLegendAction);
		// toolbarManager.add(searchAction);
		// toolbarManager.add(new Separator());

		toolbarManager.add(showAllNodesAction);
		toolbarManager.add(showMyAllocNodesAction);
		toolbarManager.add(showMyUsedNodesAction);
		// toolbarManager.add(showProcessesAction);

		//toolbarManager.add(new Separator());
		//toolbarManager.add(terminateAllAction);
	}

	public Object[] getElements(Object parent) {
		/* I don't know what would make sense to return here, so how about this? */
		return new Object[100];
	}

	public void launchProcessViewer(IPProcess proc) {
		openProcessViewer(proc);
	}

	public void showProcesses() {
		mode = PROCESSES;
		selected_node_num = -1;
		filterSelection = ALL_NODES;
		filterNodes(false);
		refresh(true, null);
	}

	public void showAllNodes() {
		mode = NODES;
		filterSelection = ALL_NODES;
		filterNodes(true);
	}

	public void showMyAllocatedNodes() {
		mode = NODES;
		filterSelection = ALLOCATED_NODES;
		filterNodes(true);
	}

	public void showMyUsedNodes() {
		mode = NODES;
		filterSelection = USED_NODES;
		filterNodes(true);
	}

	public void searchForNode(int node) {
		// System.out.println("They selected node: "+node);
		/*
		 * PORT if(sysDescNodes == null) return;
		 */
		if (mode == PROCESSES) {

			IPNode somenode = systemNodeToIPNode("" + node + "");
			if (somenode == null || !somenode.hasChildProcesses()) {
				MessageBox mb = new MessageBox(this.getViewSite().getShell(),
						SWT.ICON_ERROR | SWT.OK);
				mb.setMessage("Unable to find node " + node + " or\n"
						+ "node contains none of your processes.");
				mb.open();
				return;
			}
			IPProcess procs[] = getSortedProcessList(somenode);

			/* goto the first process */
			selected_node_num = procs[0].getID();
			moveScrolledAreaToSelection();
			refresh(false, null);
			return;
		}

		int idx = findNodeIndexNumber(node);
		if (idx < 0) {
			MessageBox mb = new MessageBox(this.getViewSite().getShell(),
					SWT.ICON_ERROR | SWT.OK);
			mb.setMessage("Unable to find node " + node);
			mb.open();
		} else {
			selected_node_num = idx;
			moveScrolledAreaToSelection();
			refresh(false, null);
		}
	}

	/*
	 * this takes our scrolledComposite area and adjusts the vertical scrollbar
	 * so that it properly shows the selected element - it requires setting the
	 * scrollbar value and also causing the content inside of the
	 * scrolledComposite to shift its location.
	 */
	private void moveScrolledAreaToSelection() {
		/* we need to scroll the scrollbar now! */
		ScrollBar vbar = sc.getVerticalBar();
		int inc = node_height + y_spacing;
		int row = selected_node_num / visible_node_cols;
		vbar.setSelection(inc * row);
		Control content = sc.getContent();
		if (content != null) {
			Point location = content.getLocation();
			content.setLocation(location.x, -vbar.getSelection());
		}
	}

	public void searchForProcess(int process) {
		/*
		 * PORT if(sysDescNodes == null) return;
		 */

		if (mode == PROCESSES) {
			if (processList == null)
				return;
			if (process < 0 || process >= processList.length) {
				MessageBox mb = new MessageBox(this.getViewSite().getShell(),
						SWT.ICON_ERROR | SWT.OK);
				mb.setMessage("Unable to find process " + process);
				mb.open();
				return;
			}

			selected_node_num = process;
			BRtable.setSelection(0);

			moveScrolledAreaToSelection();

			refresh(false, null);

			return;
		}

		int procNode = findNodeFromProcess(process);
		if (procNode < 0) {
			MessageBox mb = new MessageBox(this.getViewSite().getShell(),
					SWT.ICON_ERROR | SWT.OK);
			mb.setMessage("Unable to find process " + process);
			mb.open();
		} else {
			selected_node_num = procNode;
			moveScrolledAreaToSelection();
			refresh(false, null);

			int which = 0;

			/*
			 * PORT IPNode somenode =
			 * systemNodeToIPNode(sysDescNodes[procNode].getNode());
			 */
			IPNode somenode = (IPNode) displayElements[procNode];
			IPProcess procs[] = getSortedProcessList(somenode);

			for (int i = 0; i < procs.length; i++) {
				String procString = procs[i].getProcessNumber();
				try {
					Integer procInt = new Integer(procString);
					/* if we find it, return the NODE this process is on */
					if (procInt.intValue() == process)
						which = i;
				} catch (NumberFormatException e) {
				}
			}

			BRtable.setSelection(which);
			refresh(false, null);
		}
	}

	public int findNodeFromProcess(int process) {
		System.out.println("findNodeFromProcess(" + process + ")");
		/*
		 * PORT if(sysDescNodes == null) return -1; for(int i=0; i<sysDescNodes.length;
		 * i++) { IPNode somenode =
		 * systemNodeToIPNode(sysDescNodes[i].getNode()); if(somenode != null &&
		 * somenode.hasChildren()) { IPProcess procs[] =
		 * somenode.getProcesses(); for(int j=0; j<procs.length; j++) { String
		 * procString = procs[j].getProcessNumber(); try { Integer procInt = new
		 * Integer(procString); // if we find it, return the NODE this process
		 * is on if(procInt.intValue() == process) return i; }
		 * catch(NumberFormatException e) { } } } }
		 */
		return -1;
	}

	public int findNodeIndexNumber(int node_num) {
		if (displayElements == null)
			return -1;
		for (int i = 0; i < displayElements.length; i++) {
			if (!(displayElements[i] instanceof IPNode)) {
				System.out.println("**** ERROR ERROR - findNodeIndexNumber("
						+ node_num + ") and one element wasn't a node");
			} else {
				try {
					Integer nodeInt = new Integer(((IPNode) displayElements[i])
							.getNodeNumber());
					if (nodeInt.intValue() == node_num)
						return i;
				} catch (NumberFormatException e) {
				}
			}
		}
		/*
		 * PORT if(sysDescNodes == null) return -1; for(int i=0; i<sysDescNodes.length;
		 * i++) { String nodeString = sysDescNodes[i].getNode(); try { Integer
		 * nodeInt = new Integer(nodeString); if(nodeInt.intValue() == node_num)
		 * return i; } catch(NumberFormatException e) { } }
		 */
		return -1;
	}

	public void paintCanvas(GC gc) {
		if(num_nodes > 0) gc.drawText("Machine " + (machine_number + 1), 2, 2);
		if (mode == NODES) {
			for (int i = 0; i < num_nodes; i++) {
				drawElement(i, gc);
			}
		} else if (mode == PROCESSES && processList != null) {
			for (int i = 0; i < processList.length; i++) {
				drawElement(i, gc);
			}
		}
	}

	private void drawElement(int i, GC gc) {
		int x;
		int y;
		int x_loc, y_loc;

		x = i % visible_node_cols;
		y = i / visible_node_cols;

		int imgIndex = 0;
		imgIndex = getElementImageIndex(i);

		x_loc = x_offset + x_spacing * (x + 1) + node_width * x;
		y_loc = y_offset + y_spacing * ((i / visible_node_cols) + 1)
				+ node_height * (i / visible_node_cols);

		if (i == selected_node_num) {
			gc.drawImage(statusImages[imgIndex][SELECTED], x_loc, y_loc);
		} else if (i != selected_node_num) {
			gc.drawImage(statusImages[imgIndex][NOT_SELECTED], x_loc, y_loc);
		}

	}

	public void paintCanvasElement(GC gc, int x, int y, int width, int height) {
		if (mode == NODES)
			gc.fillRectangle(x, y, width, height);
		else
			gc.fillOval(x, y, width, height);
	}

	private int getElementImageIndex(int index) {
		if (mode == NODES) {
			/* first check if I have any processes on these nodes */
			IPNode somenode = (IPNode) displayElements[index];
			/*
			 * IPNode somenode =
			 * systemNodeToIPNode(sysDescNodes[index].getNode());
			 */
			if (somenode != null && somenode.hasChildProcesses()) {
				/* so I have jobs on these nodes - are they running? */
				IPProcess procs[] = somenode.getProcesses();
				if (procs != null && procs.length > 0) {
					boolean all_running = true;
					/*
					 * run through all the procs and make sure they are all
					 * running
					 */
					for (int p = 0; p < procs.length; p++) {
						/*
						 * if any aren't running - then we'll just display it as
						 * red
						 */
						if (!procs[p].getStatus().equals(IPProcess.RUNNING)) {
							all_running = false;
							/* short circuit eval */
							break;
						}
					}
					/* all processes are running */
					if (all_running) {
						return NODE_RUNNING;
					}
					/* any processes not running */
					else {
						return NODE_EXITED;
					}
				}
			}

			/* node up */
			else if (somenode != null
					&& somenode.getAttribute("state").equals("up")) {
				/*
				 * PORT else
				 * if(sysDescNodes[index].getBprocState().equals("up")) {
				 */
				/* what if I have the node allocated to me? */
				if (somenode.getAttribute("user").equals(
						System.getProperty("user.name"))) {
					/*
					 * PORT
					 * if(sysDescNodes[index].getBprocUser().equals(System.getProperty("user.name"))) {
					 */
					/* check the mode */
					String mode = (String) somenode.getAttribute("mode");
					/*
					 * PORT String mode = sysDescNodes[index].getBprocMode();
					 */
					if (mode.equals("0100"))
						return NODE_USER_ALLOC_EXCL;
					else if (mode.equals("0110") || mode.equals("0111")
							|| mode.equals("0101"))
						return NODE_USER_ALLOC_SHARED;
				}
				/* someone else owns it */
				else if (!somenode.getAttribute("user").equals("")) {
					// else if(somenode.getAttrib("user").equals("root")) {
					/*
					 * PORT else
					 * if(!sysDescNodes[index].getBprocUser().equals("root")) {
					 */
					/* check the mode */
					String mode = (String) somenode.getAttribute("mode");
					/*
					 * PORT String mode = sysDescNodes[index].getBprocMode();
					 */
					if (mode.equals("0100"))
						return NODE_OTHER_ALLOC_EXCL;
					else if (mode.equals("0110") || mode.equals("0111")
							|| mode.equals("0101"))
						return NODE_OTHER_ALLOC_SHARED;
				}
				/* default */
				return NODE_UP;
			}
			/* node down */
			else if (somenode != null
					&& somenode.getAttribute("state").equals("down")) {
				/*
				 * PORT else
				 * if(sysDescNodes[index].getBprocState().equals("down")) {
				 */
				return NODE_DOWN;
			} else if (somenode != null
					&& somenode.getAttribute("state").equals("error")) {
				/*
				 * PORT else
				 * if(sysDescNodes[index].getBprocState().equals("error")) {
				 */
				return NODE_ERROR;
			}
		} else if (mode == PROCESSES) {
			/* something's wrong */
			if (processList == null || index < 0 || index >= processList.length)
				return PROC_ERROR;
			if (processList[index] == null)
				return PROC_ERROR;

			String status = processList[index].getStatus();
			if (status == null)
				return PROC_ERROR;

			if (status.equals(IPProcess.STARTING))
				return PROC_STARTING;

			else if (status.equals(IPProcess.RUNNING))
				return PROC_RUNNING;

			else if (status.equals(IPProcess.EXITED))
				return PROC_EXITED;

			else if (status.equals(IPProcess.EXITED_SIGNALLED))
				return PROC_EXITED_SIGNAL;

			else if (status.equals(IPProcess.STOPPED))
				return PROC_STOPPED;

			else if (status.equals(IPProcess.ERROR))
				return PROC_ERROR;

			/* default - something's wrong */
			return PROC_ERROR;
		}

		/* default */
		return NODE_UNKNOWN;
	}

	/*
	 * this attempts to set the canvas area such that its width (in particular)
	 * is not larger than that of the scroll composite's width - only vertical
	 * scrolls not horizontal ones as well
	 */
	private void computeNewCanvasSize() {
		Point scSize = sc.getSize();
		Point bar = sc.getVerticalBar().getSize();

		int num_elements = num_nodes;
		if (mode == PROCESSES && processList != null)
			num_elements = processList.length;

		/*
		 * this'll be our new width - it's the width of the scrolled composite
		 * minus the width that a vertical horiz bar takes up - making this
		 * thing fit perfectly horizontally
		 */
		int newWidth = scSize.x - bar.x;
		int newHeight = 1000;

		/* how many can we fit wide? */
		int howManyWide = newWidth - (x_offset * 2);
		howManyWide /= (node_width + x_spacing);

		visible_node_cols = howManyWide;

		newHeight = (y_offset * 2) + (num_elements / howManyWide)
				* (y_spacing + node_height);

		/*
		 * the above eq doesn't account for the last line if it would result in
		 * a partially full (in the width) line - this next line adds that extra
		 * line on
		 */
		if ((double) num_elements / (double) howManyWide > 0.0) {
			newHeight += (y_spacing * 2) + node_height;
		}

		c.setSize(newWidth, newHeight);
		sc.layout();
	}

	/*
	 * called by the mouse listener and tests if the x,y location is within the
	 * bounds of any of our nodes (boxes). If so, then it display information
	 * where is appropriate based on the mouse event returns 'true' if it was
	 * within the bounds
	 */
	private Object testBounds(int x, int y, int mouseEvent) {
		/* let's see if x,y falls within the bounds of any of our boxes */
		int x_test = (x - x_offset - x_spacing / 2) % (x_spacing + node_width);
		int x_box = -1;
		int y_box = -1;
		Object ret = null;

		int num_elements;
		if (mode == PROCESSES && processList != null)
			num_elements = processList.length;
		else
			num_elements = num_nodes;

		if (x_test >= 0
				&& x_test < x_spacing + node_width
				&& (x - x_offset) < (node_width * visible_node_cols + x_spacing
						* visible_node_cols)) {
			x_box = (x - x_offset - x_spacing / 2) / (x_spacing + node_width);
		}

		int y_test = (y - y_offset - y_spacing / 2) % (y_spacing + node_height);

		if (y_test >= 0 && y_test < y_spacing + node_height) {
			boolean fail = false;
			/*
			 * if we're past the last full line of nodes, perhaps we're over the
			 * edge?
			 */
			if ((y - y_offset) > (node_height
					* (num_elements / visible_node_cols) + y_spacing
					* (num_elements / visible_node_cols))) {
				if (x_box + num_elements - num_elements % visible_node_cols >= num_elements) {
					/* too far! */
					y_box = -1;
					fail = true;
				}
				if ((y - y_offset) > (node_height
						* ((num_elements / visible_node_cols) + 1) + y_spacing
						* ((num_elements / visible_node_cols) + 1))) {
					y_box = -1;
					fail = true;
				}
			}
			if (!fail)
				y_box = (y - y_offset - y_spacing / 2)
						/ (y_spacing + node_height);
		}

		if (x_box != -1 && y_box != -1) {
			/* now translate to 'i' - node # */
			int node_num = x_box + y_box * visible_node_cols;
			boolean show_tool_tip = false;

			if (mouseEvent == SWT.MouseDoubleClick && mode == PROCESSES) {
				int old_node_num = selected_node_num;
				selected_node_num = node_num;
				if (old_node_num != selected_node_num && old_node_num >= 0) {
					if (mode == NODES) {
						refresh(false, displayElements[old_node_num]);
						/*
						 * PORT refresh(false,
						 * systemNodeToIPNode(sysDescNodes[old_node_num].getNode()));
						 */
					} else {
						refresh(false, processList[old_node_num]);
					}
				}
				updateLowerTextRegions();
				launchProcessViewer(processList[selected_node_num]);
				return processList[selected_node_num];
			} else if (mouseEvent == SWT.MouseDown) {
				/* we need to undraw the previous node */
				int old_node_num = selected_node_num;
				selected_node_num = node_num;
				updateLowerTextRegions();
				if (old_node_num != selected_node_num && old_node_num >= 0) {
					if (mode == NODES) {
						refresh(false, displayElements[old_node_num]);
						/*
						 * PORT refresh(false,
						 * systemNodeToIPNode(sysDescNodes[old_node_num].getNode()));
						 */
					} else {
						refresh(false, processList[old_node_num]);
					}
				}
				if (mode == NODES) {
					ret = displayElements[selected_node_num];
					/*
					 * PORT ret =
					 * systemNodeToIPNode(sysDescNodes[selected_node_num].getNode());
					 */
				} else
					ret = processList[selected_node_num];
				show_tool_tip = true;
			} else if (mouseEvent == SWT.MouseMove) {
				/*
				 * returning true here will keep the tooltip visible but won't
				 * redraw everything
				 */
				if (node_num == last_tooltip_node)
					return new Boolean(true);
				ret = new Boolean(true);
				show_tool_tip = true;
			}

			if (show_tool_tip) {
				int tipx, tipy;
				tipx = x_offset + x_box * (x_spacing + node_width) - 12;
				tipy = y_offset + y_box * (y_spacing + node_height)
						+ node_height + 18;

				/* to account for someone scrolling around */
				int selection = sc.getVerticalBar().getSelection();
				tipy += selection;

				if (mode == NODES)
					showToolTip(tipx, tipy,
							((IPNode) displayElements[node_num])
									.getNodeNumber());
				/*
				 * PORT showToolTip(tipx, tipy,
				 * sysDescNodes[node_num].getNode());
				 */
				else
					showToolTip(tipx, tipy, new String("" + node_num + ""));
				last_tooltip_node = node_num;
				return ret;
			}
		}
		last_tooltip_node = -1;
		return new Boolean(false);
	}

	Timer hovertimer;

	/* sadly, we need this to deal with mouse events that don't get caught */
	class HoverTimer extends TimerTask {
		public void run() {
			if (hovertimer != null) {
				try {
					hovertimer.cancel();
				} catch (IllegalStateException e) {
				}
			}
			hovertimer = null;
			/* someone already disposed - our work is done */
			if (toolTipShell == null)
				return;

			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					Point size = sc.getSize();
					Point mloc = Display.getDefault().getCursorLocation();
					Point map = Display.getDefault().map(null, sc, mloc);
					if (map.x < 0 || map.y < 0 || map.x > size.x
							|| map.y > size.y) {
						hideToolTip();
					}
					/* else, still within region - keep polling! */
					else {
						hovertimer = new Timer();
						hovertimer.schedule(new HoverTimer(), 500);
					}
				}
			});

		}
	}

	/*
	 * this is taken for the most part from CTabFolder which allows us to 'fake'
	 * ToolTips
	 */
	private void hideToolTip() {
		if (toolTipShell == null)
			return;
		toolTipShell.dispose();
		toolTipShell = null;
		toolTipLabel = null;
	}

	/*
	 * this is taken for the most part from CTabFolder which allows us to 'fake'
	 * ToolTips
	 */
	private void showToolTip(int x, int y, String str) {
		hideToolTip();
		if (toolTipShell == null) {
			toolTipShell = new Shell(this.getViewSite().getShell(), SWT.ON_TOP
					| SWT.TOOL);
		}
		if (toolTipLabel == null) {
			toolTipLabel = new Label(toolTipShell, SWT.CENTER);
		}
		Display display = toolTipShell.getDisplay();
		toolTipLabel.setForeground(display
				.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		toolTipLabel.setBackground(display
				.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		toolTipLabel.setText(str);

		Point labelSize = toolTipLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		labelSize.x += 2;
		labelSize.y += 2;
		toolTipLabel.setSize(labelSize);
		toolTipShell.pack();
		/*
		 * On some platforms, there is a minimum size for a shell which may be
		 * greater than the label size. To avoid having the background of the
		 * tip shell showing around the label, force the label to fill the
		 * entire client area.
		 */
		Rectangle area = toolTipShell.getClientArea();
		toolTipLabel.setSize(area.width, area.height);
		Point inpt = c.getLocation();
		Point mappedpt = display.map(c, null, inpt);
		Point pt = new Point(x, y);
		pt.x += mappedpt.x;
		pt.y += mappedpt.y;
		toolTipShell.setLocation(pt);
		toolTipShell.setVisible(true);

		if (hovertimer != null) {
			try {
				hovertimer.cancel();
			} catch (IllegalStateException e) {
			}
			hovertimer = null;
		}

		hovertimer = new Timer();
		hovertimer.schedule(new HoverTimer(), 500);
	}

	public void updateLowerTextRegions() {
		BLtable.removeAll();
		int idx = BRtable.getSelectionIndex();
		BRtable.removeAll();
		TableItem item;

		if (selected_node_num < 0)
			return;
		/*
		 * PORT if(mode == NODES && selected_node_num >= sysDescNodes.length)
		 * return;
		 */
		if (mode == NODES && selected_node_num >= displayElements.length)
			return;
		if (mode == PROCESSES
				&& (processList == null || selected_node_num >= processList.length))
			return;

		if (mode == NODES) {
			item = new TableItem(BLtable, 0);

			/*
			 * PORT item.setText(1, sysDescNodes[selected_node_num].getNode());
			 * item.setText(new String[]{"Node
			 * #",sysDescNodes[selected_node_num].getNode()}); item = new
			 * TableItem(BLtable, 0); item.setText(new
			 * String[]{"State",sysDescNodes[selected_node_num].getBprocState()});
			 * item = new TableItem(BLtable, 0); item.setText(new
			 * String[]{"User",sysDescNodes[selected_node_num].getBprocUser()});
			 * item = new TableItem(BLtable, 0); item.setText(new
			 * String[]{"Group",sysDescNodes[selected_node_num].getBprocGroup()});
			 * item = new TableItem(BLtable, 0); item.setText(new
			 * String[]{"Mode",sysDescNodes[selected_node_num].getBprocMode()});
			 * 
			 * IPNode somenode =
			 * systemNodeToIPNode(sysDescNodes[selected_node_num].getNode());
			 */
			item.setText(1, ((IPNode) displayElements[selected_node_num])
					.getNodeNumber());
			item.setText(new String[] {
					"Node #",
					((IPNode) displayElements[selected_node_num])
							.getNodeNumber() });
			item = new TableItem(BLtable, 0);
			item.setText(new String[] {
					"State",
					(String) ((IPNode) displayElements[selected_node_num])
							.getAttribute("state") });
			item = new TableItem(BLtable, 0);
			item.setText(new String[] {
					"User",
					(String) ((IPNode) displayElements[selected_node_num])
							.getAttribute("user") });
			item = new TableItem(BLtable, 0);
			item.setText(new String[] {
					"Group",
					(String) ((IPNode) displayElements[selected_node_num])
							.getAttribute("group") });
			item = new TableItem(BLtable, 0);
			item.setText(new String[] {
					"Mode",
					(String) ((IPNode) displayElements[selected_node_num])
							.getAttribute("mode") });

			IPNode somenode = (IPNode) displayElements[selected_node_num];

			if (somenode != null && somenode.hasChildProcesses()) {
				IPProcess procs[] = getSortedProcessList(somenode);
				for (int i = 0; i < procs.length; i++) {
					item = new TableItem(BRtable, 0);
					if (procs[i].getStatus().equals(IPProcess.STARTING)) {
						item.setImage(0,
								statusImages[PROC_STARTING][NOT_SELECTED]);
					} else if (procs[i].getStatus().equals(IPProcess.RUNNING)) {
						item.setImage(0,
								statusImages[PROC_RUNNING][NOT_SELECTED]);
					} else if (procs[i].getStatus().equals(IPProcess.EXITED)) {
						item.setImage(0,
								statusImages[PROC_EXITED][NOT_SELECTED]);
					} else if (procs[i].getStatus().equals(
							IPProcess.EXITED_SIGNALLED)) {
						item.setImage(0,
								statusImages[PROC_EXITED_SIGNAL][NOT_SELECTED]);
					} else if (procs[i].getStatus().equals(IPProcess.STOPPED)) {
						item.setImage(0,
								statusImages[PROC_STOPPED][NOT_SELECTED]);
					} else if (procs[i].getStatus().equals(IPProcess.ERROR)) {
						item
								.setImage(0,
										statusImages[PROC_ERROR][NOT_SELECTED]);
					}
					item.setText(1, "Process " + procs[i].getProcessNumber()
							+ ", Job " + procs[i].getJob().getJobNumber());
				}
			}
		} else if (mode == PROCESSES) {
			item = new TableItem(BLtable, 0);
			item.setText(new String[] { "Rank",
					processList[selected_node_num].getProcessNumber() });
			item = new TableItem(BLtable, 0);
			item.setText(new String[] { "PID",
					processList[selected_node_num].getPid() });

			String procnum = processList[selected_node_num].getProcessNumber();
			Integer procnumint = null;
			try {
				procnumint = new Integer(procnum);
			} catch (NumberFormatException e) {
				procnumint = null;
			}
			if (procnumint != null) {
				int node_idx = findNodeFromProcess(procnumint.intValue());
				/*
				 * PORT if(node_idx >= 0 && node_idx < sysDescNodes.length) {
				 * item = new TableItem(BLtable, 0); item.setText(new
				 * String[]{"On node",sysDescNodes[node_idx].getNode()}); }
				 */
				if (node_idx >= 0 && node_idx < displayElements.length) {
					item = new TableItem(BLtable, 0);
					item.setText(new String[] {
							"On node",
							((IPNode) displayElements[node_idx])
									.getNodeNumber() });
				}
			}

			item = new TableItem(BLtable, 0);
			item.setText(new String[] { "Status",
					processList[selected_node_num].getStatus() });
			if (processList[selected_node_num].getStatus().equals(
					IPProcess.EXITED)) {
				item = new TableItem(BLtable, 0);
				item.setText(new String[] { "Exit code",
						processList[selected_node_num].getExitCode() });
			} else if (processList[selected_node_num].getStatus().equals(
					IPProcess.EXITED_SIGNALLED)) {
				item = new TableItem(BLtable, 0);
				item.setText(new String[] { "Signal name",
						processList[selected_node_num].getSignalName() });
			}

			item = new TableItem(BRtable, 0);

			if (processList[selected_node_num].getStatus().equals(
					IPProcess.STARTING))
				item.setImage(0, statusImages[PROC_STARTING][NOT_SELECTED]);
			else if (processList[selected_node_num].getStatus().equals(
					IPProcess.RUNNING))
				item.setImage(0, statusImages[PROC_RUNNING][NOT_SELECTED]);
			else if (processList[selected_node_num].getStatus().equals(
					IPProcess.EXITED))
				item.setImage(0, statusImages[PROC_EXITED][NOT_SELECTED]);
			else if (processList[selected_node_num].getStatus().equals(
					IPProcess.EXITED_SIGNALLED))
				item
						.setImage(0,
								statusImages[PROC_EXITED_SIGNAL][NOT_SELECTED]);
			else if (processList[selected_node_num].getStatus().equals(
					IPProcess.STOPPED))
				item.setImage(0, statusImages[PROC_STOPPED][NOT_SELECTED]);
			else if (processList[selected_node_num].getStatus().equals(
					IPProcess.ERROR))
				item.setImage(0, statusImages[PROC_ERROR][NOT_SELECTED]);
			item.setText(1, "Process "
					+ processList[selected_node_num].getProcessNumber());
		}

		/*
		 * we want to reselect / rehighlight any selection they might have
		 * previously made before we redrew this area
		 */
		if (selected_node_num == last_node_num && idx >= 0
				&& idx < BRtable.getItemCount())
			BRtable.setSelection(idx);

		last_node_num = selected_node_num;
	}

	Timer timer;

	public void monitoringSystemChangeEvent(Object object) {
		setupMachineMenu();
		grabSystemStatus();
		computeNewCanvasSize();
		updateButton();
		refreshAllProcsStatus();
		refresh(false, null);
	}

	public void execStatusChangeEvent(Object object) {
		// System.out.println("execStatusChangeEvent");
		/* cancel the timer */
		if (timer != null) {
			try {
				timer.cancel();
			} catch (IllegalStateException e) {
			}
			timer = null;
		}

		timer = new Timer();
		timer.schedule(new ExecStatusChangeTimer(), 1);
		// refreshAllProcsStatus();
	}

	class ExecStatusChangeTimer extends TimerTask {
		public void run() {
			if (timer != null) {
				try {
					timer.cancel();
				} catch (IllegalStateException e) {
				}
			}
			timer = null;
			refresh(false, null);
		}
	}

	public void sysStatusChangeEvent(Object object) {
		updateSystemStatus();
	}

	public void processOutputEvent(Object object) {

	}

	public void errorEvent(Object object) {
		String err = (String) object;
		System.out.println("MI ERROR: " + err);
	}

	/*
	 * TODO set selected element
	 * 
	 * @see org.eclipse.ptp.ui.views.ISelectedElement#selectedElement()
	 */
	public void selectReveal(IPElement element) {
		Control ctrl = sc.getContent();
		if (ctrl == null || ctrl.isDisposed() || displayElements == null)
			return;
		filterSelection = ALL_NODES;
		if (element instanceof IPNode) {
			if (mode != NODES) {
				showAllNodesAction.setChecked(true);
				// showProcessesAction.setChecked(false);

				mode = NODES;
				filterNodes(true);
			}
			selected_node_num = element.getID();
			moveScrolledAreaToSelection();
			refresh(false, null);
		} else if (element instanceof IPProcess) {
			if (mode != PROCESSES) {
				showAllNodesAction.setChecked(false);
				// showProcessesAction.setChecked(true);

				mode = PROCESSES;
				filterNodes(false);
			}
			selected_node_num = element.getID();
			moveScrolledAreaToSelection();
			refresh(true, null);
		}
	}

	public void registerViewer() {
		getSite().setSelectionProvider(this);
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {

	}

	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
	}

	public ISelection getSelection() {
		if(sc == null) return StructuredSelection.EMPTY;
		Control ctrl = sc.getContent();
		if (ctrl == null || ctrl.isDisposed() || selected_node_num == -1)
			return StructuredSelection.EMPTY;

		IPElement element = null;
		if (mode == NODES)
			/*
			 * PORT element =
			 * systemNodeToIPNode(sysDescNodes[selected_node_num].getNode());
			 */
			element = displayElements[selected_node_num];
		else
			element = processList[selected_node_num];
		System.out.println("getSelection() - element = " + element);
		if (element == null)
			return StructuredSelection.EMPTY;

		return new StructuredSelection(element);
	}

	public void setSelection(ISelection selection) {
		System.out.println("testing - setSelection" + selection);
		Control ctrl = sc.getContent();
		if (ctrl == null || ctrl.isDisposed()) {
			return;
		}
		if (selection instanceof IStructuredSelection) {
			Object object = ((IStructuredSelection) selection)
					.getFirstElement();
			if (object instanceof IPElement)
				selectReveal((IPElement) object);
		}
	}
}