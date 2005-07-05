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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ptp.debug.core.PProcess;
import org.eclipse.ptp.debug.ui.ImageUtil;
import org.eclipse.ptp.debug.ui.actions.CreateGroupAction;
import org.eclipse.ptp.debug.ui.actions.DeleteGroupAction;
import org.eclipse.ptp.debug.ui.actions.GroupAction;
import org.eclipse.ptp.debug.ui.actions.ParallelDebugAction;
import org.eclipse.ptp.debug.ui.actions.ResumeAction;
import org.eclipse.ptp.debug.ui.actions.SuspendAction;
import org.eclipse.ptp.debug.ui.actions.TerminateAction;
import org.eclipse.ptp.debug.ui.model.Element;
import org.eclipse.ptp.debug.ui.model.ElementGroup;
import org.eclipse.ptp.debug.ui.model.GroupManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
	//protected ParallelDebugAction stepIntoAction = null;
	//protected ParallelDebugAction stepOverAction = null;
	//protected ParallelDebugAction stepReturnAction = null;
	protected ParallelDebugAction createGroupAction = null;
	protected ParallelDebugAction deleteGroupAction = null;

	//group 
	protected GroupManager groupManager = null;
	protected int cur_group_id = 0;
	protected ElementGroup cur_process_group = null;
	protected int cur_group_size = 0;
	
	//node info
	protected final int e_offset_x = 5;
	protected final int e_spacing_x = 4;
	protected final int e_offset_y = 5;
	protected final int e_spacing_y = 4;
	protected final int e_width = 16;
	protected final int e_height = 16;

	protected int view_width = 200;
	protected int view_height = 40;
	protected int visible_e_col = 10;
	protected int visible_e_row = 5;
	
	protected ScrolledComposite sc = null;
	protected Composite drawComp = null;
	
	//tooltip
	protected Shell toolTipShell = null;
	protected Timer hovertimer = null;
	protected final int hide_time = 1000;
	
	//selection area
	protected int drag_x = 0;
	protected int drag_y = 0;
	protected Shell selectionShell = null;
	protected final boolean fill_rect_dot = false;
	protected final int rect_dot_size = 2;
	protected final int rect_dot_disc = 3;
		
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

	protected Listener myDrawingMouseListener = new Listener() {
		public void handleEvent(Event e) {
			drawingMouseHandleEvent(e);
		}
	};
    /**
	 * constructor of DebugParallelProcessView
	 */
	public DebugParallelProcessView() {
		groupManager = uiDebugManager.getGroupManager();
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
		createNoteView(parent);
		createDebugToolBarActions();
		createMenuActions();
	}
	
	/** Create Menu Action and initial the Element
	 * 
	 */
	protected void createMenuActions() {
		ElementGroup[] groups = groupManager.getSortedGroups();
		if (groups.length == 0) {
			PProcess[] processes = debugManager.getProcesses();
			int total_processes = processes.length;

			if (total_processes > 0) {			
				ElementGroup group = new ElementGroup(groupManager.size(), true);
				for (int i=0; i<total_processes; i++) {
					group.addElement(new Element(processes[i].getID()));
				}
				groupManager.addGroup(group);
			
				groups = new ElementGroup[1];
				groups[0] = group;
			}
		}

		for(int i=0; i<groups.length; i++) {
			createGroupActions(groups[i]);
		}

		//always select the first group 
		if (groups.length > 0) {
			selectGroup(groups[0].getText());
		}
		updateMenu();
	}
	
	public void createGroupActions(ElementGroup group) {
		selectGroup(group.getText());
		getViewSite().getActionBars().getMenuManager().add(new GroupAction(group.getText(), group.getID(), this));
	}
	
	public void updateMenu() {
		IMenuManager manager = getViewSite().getActionBars().getMenuManager();		
		ElementGroup[] groups = groupManager.getGroups();
		for (int i=0; i<groups.length; i++) {
			IContributionItem item = manager.find(groups[i].getText());
			if (item != null && item instanceof ActionContributionItem) {
				((ActionContributionItem)item).getAction().setChecked(groups[i].isSelected());
			}
		}
		enableDeleteGroupAction(cur_group_id != 0);
		enableCreateGroupAction(groups.length>0);
	}
	
	public void enableDeleteGroupAction(boolean enable) {
		deleteGroupAction.setEnabled(enable);		
	}
	public void enableCreateGroupAction(boolean enable) {
		createGroupAction.setEnabled(enable);		
	}
	
	/**
	 * create tool bar actions
	 */
	protected void createDebugToolBarActions() {
		resumeAction = new ResumeAction(this);
		suspendAction = new SuspendAction(this);
		terminateAction = new TerminateAction(this);
		//stepIntoAction = new StepIntoAction(this);
		//stepOverAction = new StepOverAction(this);
		//stepReturnAction = new StepReturnAction(this);
		createGroupAction = new CreateGroupAction(this);
		deleteGroupAction = new DeleteGroupAction(this);
		
		IToolBarManager toolBarMgr = getViewSite().getActionBars().getToolBarManager();
		toolBarMgr.add(resumeAction);
		toolBarMgr.add(suspendAction);
		toolBarMgr.add(terminateAction);
		toolBarMgr.add(new Separator());
		//toolBarMgr.add(stepIntoAction);
		//toolBarMgr.add(stepOverAction);
		//toolBarMgr.add(stepReturnAction);
		//toolBarMgr.add(new Separator());
		toolBarMgr.add(createGroupAction);
		toolBarMgr.add(deleteGroupAction);
	}
	
	protected void createNoteView(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));

		sc = new ScrolledComposite(parent, SWT.V_SCROLL);
	    sc.setLayout(new FillLayout());
	    sc.setLayoutData(new GridData(GridData.FILL_BOTH));
	    sc.setMinSize(200, 200);
	    
	    ScrollBar vbar = sc.getVerticalBar();
	    vbar.setIncrement(e_height + e_spacing_y);
	    vbar.setPageIncrement(e_height + e_spacing_y);
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
	    		if (cur_process_group != null)
	    			paintCanvas(e.gc);
	    	}
	    });
	    
	    drawComp.addListener(SWT.MouseHover, new Listener() {
	    	public void handleEvent(Event e) {
	    		//System.out.println("Hover("+e.x+":"+e.y+")");
	    		int element_num = findElementNum(e.x, e.y);
	    		if (element_num > -1) {
   					showToolTip(String.valueOf(cur_process_group.getElementID(element_num)), e.x, e.y);
	    		}
	    	}
	    });
	    drawComp.addListener(SWT.MouseDown, myDrawingMouseListener);
	    //drawComp.addListener(SWT.DragDetect, myDrawingMouseListener);
	    drawComp.addListener(SWT.MouseMove, myDrawingMouseListener);
	    drawComp.addListener(SWT.MouseUp, myDrawingMouseListener);
	    drawComp.addListener(SWT.MouseExit, myDrawingMouseListener);
	}
	
	protected void drawingMouseHandleEvent(Event e) {
		final int mx = e.x;
		final int my = e.y;
		//System.out.println("drag:("+drag_x+":"+drag_y+"), mouse:("+mx+":"+my+"), view:("+view_width+":"+view_height+"), " + drawComp.getBounds());
		switch (e.type) {
			case SWT.MouseDown:
				//System.out.println("Down("+mx+":"+my+")");
				drag_x = 0;
				drag_y = 0;					
				break;
			case SWT.DragDetect:
				//System.out.println("Draf("+mx+":"+my+")");
				drag_x = mx;
				drag_y = my;
				break;
			case SWT.MouseMove:
				if (isDragOnView(mx, my)) {
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
				break;				
			case SWT.MouseUp:
				//System.out.println("Up("+mx+":"+my+")");
				disposeSelectionArea();
				
				//selection area
				if (isDragOnView(mx, my)) {
					//findSelectedElements(drag_x, drag_y, mx, my);
					drag_x = 0;
					drag_y = 0;					
					break;
				} 

				int element_num = findElementNum(mx, my);
				if (element_num > -1) {
					cur_process_group.select(element_num);
					drawComp.redraw();
				}
				break;
			default:
				//System.out.println("Other("+mx+":"+my+")");
				disposeSelectionArea();
				drag_x = 0;
				drag_y = 0;					
				break;
		}	
	}
	
	protected boolean isDragOnView(int mx, int my) {
		int start_y = sc.getOrigin().y;
		int end_y = start_y + view_height;
		return ((drag_x > 0 && drag_y > 0) && (mx > 0 && mx < view_width && my > start_y && my < end_y));
	}
	
	protected void disposeSelectionArea() {
		if (selectionShell != null) {
			selectionShell.dispose();
			selectionShell = null;
		}		
	}
	
	protected void drawSelectedArea(final int mx, final int my) {
		//switch drag_x and mx or drag_y and my
		int start_x = drag_x;
		int start_y = drag_y;
		int end_x = mx;
		int end_y = my;
		
		if (drag_x > mx) {
			end_x = drag_x;
			start_x = mx;
		}
		if (drag_y > my) {
			end_y = drag_y;
			start_y = my;
		}		
		
		Point locpt = getViewActualLocation(drawComp, null, start_x, start_y);
		Point mpt = getViewActualLocation(drawComp, null, end_x, end_y);

		selectionShell.setLocation(locpt);

		int len_x = Math.abs(mpt.x-locpt.x);
		int len_y = Math.abs(mpt.y-locpt.y);
		
		selectionShell.setBounds(locpt.x, locpt.y, len_x+rect_dot_disc, len_y+rect_dot_disc);

		int last_x = len_x-rect_dot_disc;
		int last_y = len_y-rect_dot_disc;
		
		Region region = new Region();
		Rectangle pixel = new Rectangle(0, 0, rect_dot_size, rect_dot_size);
		for (int y=0; y<len_y; y+=rect_dot_disc) {
			for (int x=0; x<len_x; x+=rect_dot_disc) {
				if (fill_rect_dot || (y == 0 || y >= last_y || x == 0 || x >= last_x)) {
					pixel.x = x;
					pixel.y = y;
					region.add(pixel);
				}
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
		
		Point l_size = toolTipLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		l_size.x += 2;
		l_size.y += 2;
		toolTipLabel.setSize(l_size);
		toolTipShell.pack();
		
		Rectangle area = toolTipShell.getClientArea();
		toolTipLabel.setSize(area.width, area.height);
		
		//Find out the location to display tooltip
		Point ept = FindElementLocation(mx, my);		
		Point s_size = toolTipShell.getSize();

		int new_x = ept.x - (s_size.x / 2);
		Rectangle shell_rect = Display.getCurrent().getBounds();
		//reset the x to fit into screen size
		new_x = (new_x<shell_rect.x)?0:((new_x+s_size.x)>shell_rect.width)?(shell_rect.width-s_size.x-10):new_x;
		int new_y = ept.y - e_height - e_spacing_y;
		new_y = (new_y<sc.getOrigin().y)?(ept.y + e_height + e_spacing_y):new_y;
		
		toolTipShell.setLocation(getViewActualLocation(drawComp, null, new_x, new_y));
		toolTipShell.setVisible(true);		
	
		createToolTipTimer();
	}
	
	protected Point getViewActualLocation(Control from, Control to, int mx, int my) {
		Point mappedpt = getViewSite().getShell().getDisplay().map(from, to, 0, 0);
		Point newpt = new Point(mx, my);
		
		newpt.x += mappedpt.x;
		newpt.y += mappedpt.y;
		return newpt;
	}
	
	private void createToolTipTimer() {
		cancelToolTipTimer();
		hovertimer = new Timer();
		hovertimer.schedule(new TimerTask() {
			public void run() {
				cancelToolTipTimer();
				drawComp.getDisplay().asyncExec(new Runnable() {
					public void run() {
						hideToolTip();
					}
				});
			}
		}, hide_time);
	}
	
	private void cancelToolTipTimer() {		
		if(hovertimer != null) {
			try {
				hovertimer.cancel();
			} catch(IllegalStateException e) { }
			hovertimer = null;
		}
	}

	protected List findSelectedElements(int s_x, int s_y, int e_x, int e_y) {
		//switch s_x and e_x or s_y and e_y
		int start_x = s_x;
		int start_y = s_y;
		int end_x = e_x;
		int end_y = e_y;
		
		if (s_x > e_x) {
			end_x = s_x;
			start_x = e_x;
		}
		if (s_y > e_y) {
			end_y = s_y;
			start_y = e_y;
		}
		
		/* add the selection area size
		 * by 2 to prevent selection edge on element
		 */
		start_x += rect_dot_size*2;
		start_y += rect_dot_size*2;
		end_x -= rect_dot_size*2;
		end_y -= rect_dot_size*2;
		
		List aList = new ArrayList(1);
		
		//note: plus spacing to contain elements within the spacing
		int col = (Math.abs(start_x - e_offset_x + e_spacing_x) / (e_width + e_spacing_x) + 1);
		col = col>visible_e_col?(col-1):col;
		int start_loc_x = e_offset_x + (e_width + e_spacing_x) * (col - 1);
		
		//note: plus spacing to contain elements within the spacing
		int row = (Math.abs(start_y - e_offset_x + e_spacing_y) / (e_height + e_spacing_y) + 1);
		int start_loc_y = e_offset_x + (e_height + e_spacing_y) * (row - 1);

		int total_col = ((end_x - start_loc_x) / (e_width + e_spacing_x) + 1);
		int total_row = ((end_y - start_loc_y) / (e_height + e_spacing_y) + 1);
		
		//int node_num = (visible_e_col * (row - 1) + col) - 1;
		System.out.println(total_col + ":"+ total_row);
		
		return aList;
	}
	
	protected Point FindElementLocation(int mx, int my) {
		int col = (Math.abs(mx - e_offset_x) / (e_width + e_spacing_x)) + 1;
		col = col>visible_e_col?(col-1):col;
		int loc_x = e_offset_x + (e_width + e_spacing_x) * (col - 1);
		
		int row = (Math.abs(my - e_offset_x) / (e_height + e_spacing_y)) + 1;
		int loc_y = e_offset_x + (e_height + e_spacing_y) * (row - 1);
		return new Point(loc_x, loc_y);
	}
	
	protected int findElementNum(int mx, int my) {
		int col = (Math.abs(mx - e_offset_x) / (e_width + e_spacing_x)) + 1;
		//in case if there is no scrollbar
		col = col>visible_e_col?(col-1):col;
		int loc_x = e_offset_x + (e_width + e_spacing_x) * (col - 1);
		
		if (mx > loc_x && mx < (loc_x + e_width)) {
			int row = (Math.abs(my - e_offset_x) / (e_height + e_spacing_y)) + 1;
			int loc_y = e_offset_x + (e_height + e_spacing_y) * (row - 1);
			if (my > loc_y && my < (loc_y + e_height)) {
				int node_num = visible_e_col * (row - 1) + col;
				if (node_num <= cur_group_size)
					return (node_num - 1);
			}
		}
		return -1;
	}
	
	protected void updateViewSize(boolean isVSrcoll) {
		Point pt = sc.getSize();
		view_width = isVSrcoll?(pt.x-sc.getVerticalBar().getSize().x):pt.x;
		view_height = pt.y;
		visible_e_col = (view_width - e_offset_x) / (e_width + e_spacing_x);
		visible_e_row = (view_height - e_offset_x) / (e_height + e_spacing_y) + 1 ;
	}
	
	protected int calculateHeight() {
    	if (visible_e_col > 0) {
	    	int max_node_row = (cur_group_size / visible_e_col) + ((cur_group_size%visible_e_col==0)?0:1);
	    	return max_node_row * (e_height + e_spacing_y) + e_offset_x;
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
	}

	/**
	 * Print visible area ONLY
	 * Find out process numbers of beginning and ending   
	 * 
	 * @param g
	 */
	protected void paintCanvas(GC g) {
		Element[] elements = cur_process_group.getSortedElements();
	
		int start_row = Math.round((sc.getOrigin().y - e_offset_x) / (e_height + e_spacing_y));
		int start_num = start_row * visible_e_col;

		int end_num =  ((start_row + visible_e_row + 1) * visible_e_col);
		end_num = (end_num>elements.length)?elements.length:end_num;

		//System.out.println("("+px+":"+py+"), ("+pw+":"+ph+"), ("+view_width+":"+view_height+"), (Row: " + visible_node_row +", Col: " + visible_node_col+"), (Start: " + start_num + ", End: " + end_num+")");
		for (int i=start_num; i<end_num; i++) {			
			drawElement(i+1, elements[i], g);
		}
	}
	
	protected void drawElement(int index, Element element, GC g) {
		int x = (visible_e_col>index)?index:(index%visible_e_col);
		x = (x==0)?visible_e_col:x;
		int x_loc = e_offset_x + ((x-1) * (e_width + e_spacing_x)); 
		
		int y = (x==visible_e_col)?index/visible_e_col:(visible_e_col>index)?1:((index/visible_e_col) + 1);
		int y_loc = e_offset_x + ((y-1) * (e_height + e_spacing_y));

		int status = debugManager.getProcess(element.getID()).getStatus();
		g.drawImage(statusImages[status][element.isSelected()?1:0], x_loc, y_loc);
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
		if (selection instanceof StructuredSelection) {
			for (Iterator i=((StructuredSelection)selection).iterator(); i.hasNext();) {
				Object obj = i.next();
				if (obj instanceof PProcess) {
					Element element = cur_process_group.getElementByID(((PProcess)obj).getID());
					element.setSelected(true);
				}
			}
		}
	}
	public ISelection getSelection() {
		return new StructuredSelection(cur_process_group.getSelectedElements());
	}
	
	public int getCurrentGroupID() {
		return cur_group_id;
	}
	public void selectGroup(String groupText) {
		deSelectGroup();		
		this.cur_process_group =  groupManager.getGroup(groupText);
		this.cur_group_id = cur_process_group.getID();
		this.cur_group_size = cur_process_group.size();
		cur_process_group.setSelected(true);
	}
	public void deSelectGroup() {
		if (cur_process_group != null)
			cur_process_group.setSelected(false);
	}
	public ElementGroup getCurrentGroup() {
		return cur_process_group;
	}
	public void redraw() {
		adjustDrawingView();
		drawComp.redraw();
	}
	public GroupManager getGroupManager() {
		return groupManager;
	}
}
