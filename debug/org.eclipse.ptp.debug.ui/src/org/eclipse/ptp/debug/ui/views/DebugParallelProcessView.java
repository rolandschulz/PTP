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
package org.eclipse.ptp.debug.ui.views;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ptp.debug.core.DebugManager;
import org.eclipse.ptp.debug.core.GroupManager;
import org.eclipse.ptp.debug.core.PProcess;
import org.eclipse.ptp.debug.core.PProcessGroup;
import org.eclipse.ptp.debug.ui.ImageUtil;
import org.eclipse.ptp.debug.ui.actions.CreateGroupAction;
import org.eclipse.ptp.debug.ui.actions.GroupAction;
import org.eclipse.ptp.debug.ui.actions.ParallelDebugAction;
import org.eclipse.ptp.debug.ui.actions.ResumeAction;
import org.eclipse.ptp.debug.ui.actions.StepIntoAction;
import org.eclipse.ptp.debug.ui.actions.StepOverAction;
import org.eclipse.ptp.debug.ui.actions.StepReturnAction;
import org.eclipse.ptp.debug.ui.actions.SuspendAction;
import org.eclipse.ptp.debug.ui.actions.TerminateAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;

/**
 * @author clement chu
 *
 */
public class DebugParallelProcessView extends AbstractDebugParallelView {
	public static final String VIEW_ID = "org.eclipse.ptp.debug.ui.views.debugParallelProcessView";
	
	private static DebugParallelProcessView instance = null;
	
	protected ParallelDebugAction resumeAction = null;
	protected ParallelDebugAction suspendAction = null;
	protected ParallelDebugAction terminateAction = null;
	protected ParallelDebugAction stepIntoAction = null;
	protected ParallelDebugAction stepOverAction = null;
	protected ParallelDebugAction stepReturnAction = null;
	protected ParallelDebugAction createGroupAction = null;
	
	protected GroupManager groupManager = null;
	
	protected int cur_group_id = 0;
	protected PProcessGroup cur_process_group = null;
	
	//node info
	protected final int x_offset = 10;
	protected final int x_spacing = 4;
	protected final int y_offset = 10;
	protected final int y_spacing = 4;
	protected final int node_width = 16;
	protected final int node_height = 16;

	protected int view_width = 200;
	protected int view_height = 40;
	protected int visible_node_num = 10;
	
	protected ScrolledComposite sc = null;
	protected Composite drawComp = null;
	
	protected Shell toolTipShell = null;
	protected Timer hovertimer = null;	
		
    public static Image[][] statusImages = {
    	{
			ImageUtil.getImage(ImageUtil.IMG_PRO_ERROR),
			ImageUtil.getImage(ImageUtil.IMG_PRO_ERROR_SEL),
    	},
    	{
			ImageUtil.getImage(ImageUtil.IMG_PRO_RUNNING),
			ImageUtil.getImage(ImageUtil.IMG_PRO_RUNNING_SEL),
    	},
    	{
			ImageUtil.getImage(ImageUtil.IMG_PRO_STARTED),
			ImageUtil.getImage(ImageUtil.IMG_PRO_STARTED_SEL),
    	},
    	{
			ImageUtil.getImage(ImageUtil.IMG_PRO_SUSPENDED),
			ImageUtil.getImage(ImageUtil.IMG_PRO_SUSPENDED_SEL),
    	},
    	{
			ImageUtil.getImage(ImageUtil.IMG_PRO_STOPPED),
			ImageUtil.getImage(ImageUtil.IMG_PRO_STOPPED_SEL),
    	}
    };
	
	/**
	 * constructor of DebugParallelProcessView
	 */
	public DebugParallelProcessView() {
		groupManager = DebugManager.getInstance().getGroupManager();
	}
	
	/**
	 * get DebugParallelProcessView object
	 * @return DebugParallelProcessView instance
	 */
	public static DebugParallelProcessView getInstance() {
		if (instance == null)
			instance = new DebugParallelProcessView();
		return instance;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		createDebugToolBarActions();
		createNoteView(parent);		
	}
	
	protected void createDebugToolBarActions() {
		resumeAction = new ResumeAction(this);
		suspendAction = new SuspendAction(this);
		terminateAction = new TerminateAction(this);
		stepIntoAction = new StepIntoAction(this);
		stepOverAction = new StepOverAction(this);
		stepReturnAction = new StepReturnAction(this);
		createGroupAction = new CreateGroupAction(this);
		
		IToolBarManager toolBarMgr = getViewSite().getActionBars().getToolBarManager();
		toolBarMgr.add(resumeAction);
		toolBarMgr.add(suspendAction);
		toolBarMgr.add(terminateAction);
		toolBarMgr.add(new Separator());
		toolBarMgr.add(stepIntoAction);
		toolBarMgr.add(stepOverAction);
		toolBarMgr.add(stepReturnAction);
		toolBarMgr.add(new Separator());
		toolBarMgr.add(createGroupAction);

		IMenuManager menuMgr = getViewSite().getActionBars().getMenuManager();
		Object[] groups = groupManager.getGroups();		
		for(int i=0; i<groups.length; i++) {
			ParallelDebugAction groupAction = new GroupAction(i, this);
			if (i == 0) {
				groupAction.setChecked(true);
				setCurrentGroupID(cur_group_id);
				setCurrentProcessGroup(cur_group_id);
			}
			menuMgr.add(groupAction);
		}
	}
	
	protected void createNoteView(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));

		sc = new ScrolledComposite(parent, SWT.V_SCROLL);
	    sc.setLayout(new FillLayout());
	    sc.setLayoutData(new GridData(GridData.FILL_BOTH));
	    sc.setMinSize(200, 200);
	    
	    ScrollBar vbar = sc.getVerticalBar();
	    vbar.setIncrement(node_height + y_spacing);
	    vbar.setPageIncrement(node_height + y_spacing);
	    sc.addListener(SWT.Resize, new Listener() {
	    	public void handleEvent(Event event) {
	    		adjustDrawingView(sc.getSize());
	    	}
	    });
	
	    drawComp = new Composite(sc, SWT.NONE);
	    sc.setContent(drawComp);
	    drawComp.setLayout(new FillLayout());	
	    drawComp.setLayoutData(new GridData(GridData.FILL_BOTH));
	    
	    Composite bottomComp = new Composite(parent, SWT.BORDER);
	    bottomComp.setLayout(new FillLayout());
	    bottomComp.setLayoutData(new GridData(GridData.FILL_BOTH));
	    new Label(bottomComp, SWT.NONE).setText("Text A");
	    new Label(bottomComp, SWT.NONE).setText("Text B");
	    
	    drawComp.addPaintListener(new PaintListener() {
	    	public void paintControl(PaintEvent e) {
	    		paintCanvas(e.gc);
	    	}
	    });
	    
	    drawComp.addListener(SWT.MouseDown, new Listener() {
	    	public void handleEvent(Event e) {
	    		int node_num = findProcessNum(e.x, e.y);
	    		if (node_num > -1) {
	    			PProcess process = cur_process_group.getProcess(node_num);
	    			boolean isSelected = process.isSelected();
	    			process.setSelected(!isSelected);
	    			drawComp.redraw();
	    		}
	    	}
	    });
	    drawComp.addListener(SWT.MouseHover, new Listener() {
	    	public void handleEvent(Event e) {
	    		int node_num = findProcessNum(e.x, e.y);
	    		if (node_num > -1) {
	    			int process_id = cur_process_group.getProcess(node_num).getProcess_id();
	    			showToolTip("" + process_id, e.x, e.y);
	    		}
	    	}
	    });
	    drawComp.addListener(SWT.MouseDown, new Listener() {
	    	public void handleEvent(Event e) {
	    		int node_num = findProcessNum(e.x, e.y);
	    	}
	    });
	}
	
	protected void hideToolTip() {
		if (toolTipShell != null) {
			toolTipShell.dispose();
			toolTipShell = null;
		}
	}
	
	protected void showToolTip(String text, int mx, int my) {
		hideToolTip();
		toolTipShell = new Shell(drawComp.getShell(), SWT.ON_TOP | SWT.TOOL);
		Label toolTipLabel = new Label(toolTipShell, SWT.CENTER);
		Display display = toolTipShell.getDisplay();
		toolTipLabel.setForeground (display.getSystemColor (SWT.COLOR_INFO_FOREGROUND));
		toolTipLabel.setBackground (display.getSystemColor (SWT.COLOR_INFO_BACKGROUND));
		toolTipLabel.setText(text);	

		Point labelSize = toolTipLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		labelSize.x += 2; labelSize.y += 2;
		toolTipLabel.setSize(labelSize);
		toolTipShell.pack();
		
		Rectangle area = toolTipShell.getClientArea();
		toolTipLabel.setSize(area.width, area.height);
		Point inpt = drawComp.getLocation();
		Point mappedpt = display.map(drawComp, null, inpt);
		Point pt = new Point(mx, my);
		pt.x += mappedpt.x - node_width;
		pt.y += mappedpt.y - node_height;
		
		//Point pt = new Point(mx-x_spacing, my-y_spacing);
		toolTipShell.setLocation(pt);
		toolTipShell.setVisible(true);		
	
		createTimer();
	}
	
	private void createTimer() {
		cancelTimer();
		hovertimer = new Timer();
		hovertimer.schedule(new TimerTask() {
			public void run() {
				cancelTimer();
				DebugParallelProcessView.this.getViewSite().getShell().getDisplay().asyncExec(new Runnable() {
					public void run() {
						hideToolTip();
					}
				});
			}
		}, 1000);
	}
	
	private void cancelTimer() {
		if(hovertimer != null) {
			try {
				hovertimer.cancel();
			} catch(IllegalStateException e) { }
			hovertimer = null;
		}
	}
	
	protected int findProcessNum(int mx, int my) {
		int col = Math.round((mx - x_offset) / (node_width + x_spacing) + 0.5f);
		int col_loc = x_offset + (node_width + x_spacing) * (col - 1);
		
		if (mx > col_loc && mx < (col_loc + node_width)) {
			int row = Math.round((my - y_offset) / (node_height + y_spacing) + 0.5f);
			int row_loc = y_offset + (node_height + y_spacing) * (row - 1);
			if (my > row_loc && my < (row_loc + node_height)) {
				int node_num = visible_node_num * (row - 1) + col;
				if (node_num <= cur_process_group.size())
					return (node_num - 1);
			}
		}
		return -1;
	}
	
	protected void adjustDrawingView(Point point) {
		view_width = point.x - sc.getVerticalBar().getSize().x;
		view_height = point.y;
		visible_node_num = Math.round((view_width - (x_offset * 2) - x_spacing) / (node_width + x_spacing) + 0.5f);		
		
		if (visible_node_num > 0) {
	    	int total_process = cur_process_group.size();
	    	int new_height = Math.round(total_process/visible_node_num + 0.5f) * (node_height + y_spacing) + (y_offset * 2);
	    	
			drawComp.setSize(view_width, new_height);
			sc.layout();
		}
	}
	
	protected void paintCanvas(GC g) {
		PProcess[] processes = cur_process_group.getProcesses();
		for (int i=0; i<processes.length; i++) {
			drawElement(i+1, processes[i].isSelected(), processes[i].getStatus(), g);
		}
	}
	
	protected void drawElement(int index, boolean isSelected, int status, GC g) {
		int x;
		int y;
		int x_loc;
		int y_loc;
		
		x = (visible_node_num>index)?index:(index%visible_node_num);
		x = (x==0)?visible_node_num:x;
		x_loc = x_offset + ((x-1) * (node_width + x_spacing)); 
		
		y = (x==visible_node_num)?index/visible_node_num:(visible_node_num>index)?1:(Math.round(index/visible_node_num + 0.5f));
		y_loc = y_offset + ((y-1) * (node_height + y_spacing));

		g.drawImage(statusImages[status][isSelected?1:0], x_loc, y_loc);
    }
		
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		super.dispose();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IDebugParallelModelListener#run()
	 */
	public void run() {
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IDebugParallelModelListener#suspend()
	 */
	public void suspend() {
		
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IDebugParallelModelListener#stop()
	 */
	public void stop() {
		
	}
	
	public void setSelection(ISelection selection) {
		
	}
	public ISelection getSelection() {
		return null;
	}
	
	public void setCurrentGroupID(int id) {
		this.cur_group_id = id;
	}
	public int getCurrentGroupID() {
		return cur_group_id;
	}
	public void setCurrentProcessGroup(int id) {
		this.cur_process_group =  groupManager.getGroup(id);
	}
	public PProcessGroup getCurrentProcessGroup() {
		return cur_process_group;
	}
	public void redraw() {
		drawComp.redraw();
	}
}
