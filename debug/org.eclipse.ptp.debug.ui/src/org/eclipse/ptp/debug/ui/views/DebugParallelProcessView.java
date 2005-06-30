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
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
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
	protected final int x_offset = 5;
	protected final int x_spacing = 4;
	protected final int y_offset = 5;
	protected final int y_spacing = 4;
	protected final int node_width = 16;
	protected final int node_height = 16;

	protected int view_width = 200;
	protected int view_height = 40;
	protected int visible_node_col = 10;
	protected int visible_node_row = 5;
	
	protected ScrolledComposite sc = null;
	protected Composite drawComp = null;
	
	protected Shell toolTipShell = null;
	protected Timer hovertimer = null;
	
	//dragging
	protected int drag_x = 0;
	protected int drag_y = 0;
	protected Shell selectionShell = null;
		
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

	protected Listener myMouseDragListener = new Listener() {
		public void handleEvent(Event e) {
			mouseDragHandleEvent(e);
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
	    		adjustDrawingView();
	    	}
	    });
	
	    drawComp = new Composite(sc, SWT.NONE);
	    drawComp.setLayout(new FillLayout());	
	    drawComp.setLayoutData(new GridData(GridData.FILL_BOTH));	    
	    sc.setContent(drawComp);

	    /*
	    Composite bottomComp = new Composite(parent, SWT.BORDER);
	    bottomComp.setLayout(new FillLayout());
	    bottomComp.setLayoutData(new GridData(GridData.FILL_BOTH));
	    new Label(bottomComp, SWT.NONE).setText("Text A");
	    new Label(bottomComp, SWT.NONE).setText("Text B");
	    */
	    
	    drawComp.addPaintListener(new PaintListener() {
	    	public void paintControl(PaintEvent e) {
    			paintCanvas(e.gc);
	    	}
	    });
	    
	    drawComp.addListener(SWT.MouseHover, new Listener() {
	    	public void handleEvent(Event e) {
	    		int node_num = findProcessNum(e.x, e.y);
	    		if (node_num > -1) {
	    			PProcess process = cur_process_group.getProcess(node_num);
   					showToolTip(String.valueOf(process.getProcess_id()), e.x, e.y);
	    		}
	    	}
	    });
	    drawComp.addListener(SWT.MouseDown, myMouseDragListener);
	    drawComp.addListener(SWT.DragDetect, myMouseDragListener);
	    drawComp.addListener(SWT.MouseMove, myMouseDragListener);
	    drawComp.addListener(SWT.MouseUp, myMouseDragListener);
	    drawComp.addListener(SWT.MouseExit, myMouseDragListener);
	}
	
	protected void mouseDragHandleEvent(Event e) {
		final int mx = e.x;
		final int my = e.y;
		switch (e.type) {
			case SWT.MouseDown:
				drag_x = 0;
				drag_y = 0;					
				break;
			case SWT.DragDetect:
				drag_x = mx;
				drag_y = my;
				break;
			case SWT.MouseMove:
				/*
				if ((drag_x > 0 && drag_y > 0) && (mx > 0 && mx < view_width && my > 0 && my < view_height)) {
					if (selectionShell == null) {
						selectionShell = new Shell(drawComp.getShell(), SWT.NO_TRIM | SWT.NO_REDRAW_RESIZE| SWT.ON_TOP);
						Display display = selectionShell.getDisplay();
						selectionShell.setBackground(display.getSystemColor(SWT.COLOR_DARK_RED));
						
						drawSelectedArea(mx, my);
						selectionShell.setVisible(true);
						break;
					}
					drawSelectedArea(mx, my);
				}
				*/
				break;				
			case SWT.MouseUp:
				disposeSelectionArea();
				if (drag_x > 0 && drag_y > 0) {
					drag_x = 0;
					drag_y = 0;					
					break;
				} 

				int node_num = findProcessNum(mx, my);
				if (node_num > -1) {
					PProcess process = cur_process_group.getProcess(node_num);
					boolean isSelected = process.isSelected();
					process.setSelected(!isSelected);
					drawComp.redraw();
				}
				break;
			default:
				disposeSelectionArea();
				drag_x = 0;
				drag_y = 0;					
				break;
		}	
	}
	
	protected void disposeSelectionArea() {
		if (selectionShell != null) {
			selectionShell.dispose();
			selectionShell = null;
		}		
	}
	
	protected void drawSelectedArea(final int mx, final int my) {
		int start_x = drag_x;
		int start_y = drag_y;
		int new_x = mx;
		int new_y = my;
		
		if (drag_x > mx) {
			new_x = drag_x;
			start_x = mx;
		}
		if (drag_y > my) {
			new_y = drag_y;
			start_y = my;
		}		
		
		Point locpt = getViewActualLocation(drawComp, selectionShell, start_x, start_y);
		Point mpt = getViewActualLocation(drawComp, selectionShell, new_x, new_y);

		selectionShell.setLocation(locpt);		

		int len_x = Math.abs(mpt.x-locpt.x);
		int len_y = Math.abs(mpt.y-locpt.y);		

		Region region = new Region();
		Rectangle pixel = new Rectangle(0, 0, 1, 1);
		for (int y=0; y<len_y; y+=4) {
			for (int x=0; x<len_x; x+=4) {
				pixel.x = x;
				pixel.y = y;
				region.add(pixel);
			}
		}
		selectionShell.setRegion(region);
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
				
		Point newpt = getViewActualLocation(drawComp, toolTipShell, mx, my);
		toolTipShell.setLocation((newpt.x - node_width), (newpt.y - node_height));
		toolTipShell.setVisible(true);		
	
		createTimer();
	}
	
	protected Point getViewActualLocation(Composite composite, Shell shell, int mx, int my) {
		Point mappedpt = shell.getDisplay().map(drawComp, null, new Point(0,0));
		Point newpt = new Point(mx, my);
		
		newpt.x += mappedpt.x;
		newpt.y += mappedpt.y;
		return newpt;
	}
	
	private void createTimer() {
		cancelTimer();
		hovertimer = new Timer();
		hovertimer.schedule(new TimerTask() {
			public void run() {
				cancelTimer();
				drawComp.getDisplay().asyncExec(new Runnable() {
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
		int col = (mx - x_offset) / (node_width + x_spacing) + 1;
		col = col>visible_node_col?(col-1):col;
		int col_loc = x_offset + (node_width + x_spacing) * (col - 1);
		
		if (mx > col_loc && mx < (col_loc + node_width)) {
			int row = (my - y_offset) / (node_height + y_spacing) + 1;
			int row_loc = y_offset + (node_height + y_spacing) * (row - 1);
			if (my > row_loc && my < (row_loc + node_height)) {
				int node_num = visible_node_col * (row - 1) + col;
				if (node_num <= cur_process_group.size())
					return (node_num - 1);
			}
		}
		return -1;
	}
	
	protected void updateViewSize(boolean isVSrcoll) {
		Point pt = sc.getSize();
		view_width = isVSrcoll?(pt.x-sc.getVerticalBar().getSize().x):pt.x;
		view_height = pt.y;
		visible_node_col = (view_width - x_offset) / (node_width + x_spacing);
		visible_node_row = (view_height - y_offset) / (node_height + y_spacing) + 1 ;
	}
	
	protected int calculateHeight() {
    	if (visible_node_col > 0) {
        	int total_process = cur_process_group.size();
	    	int max_node_row = (total_process / visible_node_col) + ((total_process%visible_node_col==0)?0:1);
	    	return max_node_row * (node_height + y_spacing) + y_offset;
    	}
    	return view_height;
	}
	
	/**
	 * Calculate the size of drawing component 
	 * 
	 */
	protected void adjustDrawingView() {
		updateViewSize(false);
		int new_height = calculateHeight();
    	if (new_height > view_height) {//display v_scroll
    		updateViewSize(true);
    		new_height = calculateHeight();
    	}	    	
		drawComp.setSize(view_width, new_height);
		//sc.layout();
	}

	/**
	 * Print visible area ONLY
	 * Find out process numbers of beginning and ending   
	 * 
	 * @param g
	 */
	protected void paintCanvas(GC g) {
		PProcess[] processes = cur_process_group.getProcesses();
	
		int start_row = Math.round((sc.getOrigin().y - y_offset) / (node_height + y_spacing));
		int start_num = start_row * visible_node_col;

		int end_num =  ((start_row + visible_node_row + 1) * visible_node_col);
		end_num = (end_num>processes.length)?processes.length:end_num;

		//System.out.println("("+px+":"+py+"), ("+pw+":"+ph+"), ("+view_width+":"+view_height+"), (Row: " + visible_node_row +", Col: " + visible_node_col+"), (Start: " + start_num + ", End: " + end_num+")");
		for (int i=start_num; i<end_num; i++) {
			drawElement(i+1, processes[i].isSelected(), processes[i].getStatus(), g);
		}
	}
	
	protected void drawElement(int index, boolean isSelected, int status, GC g) {
		int x = (visible_node_col>index)?index:(index%visible_node_col);
		x = (x==0)?visible_node_col:x;
		int x_loc = x_offset + ((x-1) * (node_width + x_spacing)); 
		
		int y = (x==visible_node_col)?index/visible_node_col:(visible_node_col>index)?1:((index/visible_node_col) + 1);
		int y_loc = y_offset + ((y-1) * (node_height + y_spacing));

		g.drawImage(statusImages[status][isSelected?1:0], x_loc, y_loc);
		//System.out.println("("+x_loc+":"+y_loc+")");
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
		drawComp.setFocus();
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
		adjustDrawingView();
		drawComp.redraw();
		System.out.println("Called redraw");
	}
}
