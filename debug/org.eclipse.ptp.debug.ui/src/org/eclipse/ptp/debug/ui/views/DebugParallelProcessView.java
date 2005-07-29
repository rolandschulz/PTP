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

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.debug.core.IDebugParallelModelListener;
import org.eclipse.ptp.debug.ui.ImageUtil;
import org.eclipse.ptp.debug.ui.actions.CreateGroupAction;
import org.eclipse.ptp.debug.ui.actions.DeleteGroupAction;
import org.eclipse.ptp.debug.ui.actions.DeleteProcessAction;
import org.eclipse.ptp.debug.ui.actions.GroupAction;
import org.eclipse.ptp.debug.ui.actions.ParallelDebugAction;
import org.eclipse.ptp.debug.ui.actions.RegisterAction;
import org.eclipse.ptp.debug.ui.actions.ResumeAction;
import org.eclipse.ptp.debug.ui.actions.SuspendAction;
import org.eclipse.ptp.debug.ui.actions.TerminateAction;
import org.eclipse.ptp.debug.ui.model.IElement;
import org.eclipse.ptp.debug.ui.model.IElementGroup;
import org.eclipse.ptp.debug.ui.model.IGroupManager;
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
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * @author clement chu
 * 
 */
public class DebugParallelProcessView extends AbstractDebugParallelView {
	public static final String VIEW_ID = "org.eclipse.ptp.debug.ui.views.debugParallelProcessView";

	private final static String DEFAULT_TITLE = "Parallel";
	private static DebugParallelProcessView instance = null;

	// actions
	protected ParallelDebugAction resumeAction = null;
	protected ParallelDebugAction suspendAction = null;
	protected ParallelDebugAction terminateAction = null;
	// protected ParallelDebugAction stepIntoAction = null;
	// protected ParallelDebugAction stepOverAction = null;
	// protected ParallelDebugAction stepReturnAction = null;
	protected ParallelDebugAction registerAction = null;
	protected ParallelDebugAction createGroupAction = null;
	protected ParallelDebugAction deleteGroupAction = null;
	protected ParallelDebugAction deleteProcessAction = null;

	// group
	protected IGroupManager groupManager = null;
	protected String cur_group_id = IGroupManager.GROUP_ROOT_ID;
	protected IElementGroup cur_element_group = null;
	protected int cur_group_size = 0;
	protected int fisrt_selected_element_id = -1;

	// node info
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

	// tooltip
	protected Shell toolTipShell = null;
	protected Timer hovertimer = null;
	protected final int hide_time = 2000;

	// mouse
	protected boolean isDoubleClick = false;
	protected int keyCode = SWT.None;

	// selection area
	protected int drag_x = -1;
	protected int drag_y = -1;
	protected Shell selectionShell = null;
	protected final boolean fill_rect_dot = false;
	protected final int rect_dot_size = 2;
	protected final int rect_dot_disc = 3;
	
	//key
	protected int DEFAULT_CTRL_KEY = Platform.getOS().equals(Platform.OS_MACOSX)?SWT.COMMAND:SWT.CTRL;

	public static Image[][] statusImages = {
		{
			ImageUtil.getImage(ImageUtil.IMG_PRO_ERROR),
			ImageUtil.getImage(ImageUtil.IMG_PRO_ERROR_SEL), },
		{
			ImageUtil.getImage(ImageUtil.IMG_PRO_RUNNING),
			ImageUtil.getImage(ImageUtil.IMG_PRO_RUNNING_SEL), },
		{
			ImageUtil.getImage(ImageUtil.IMG_PRO_STARTED),
			ImageUtil.getImage(ImageUtil.IMG_PRO_STARTED_SEL), },
		{
			ImageUtil.getImage(ImageUtil.IMG_PRO_SUSPENDED),
			ImageUtil.getImage(ImageUtil.IMG_PRO_SUSPENDED_SEL), },
		{
			ImageUtil.getImage(ImageUtil.IMG_PRO_STOPPED),
			ImageUtil.getImage(ImageUtil.IMG_PRO_STOPPED_SEL), }
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
	 * 
	 * @return DebugParallelProcessView instance
	 */
	public static DebugParallelProcessView getInstance() {
		if (instance == null)
			instance = new DebugParallelProcessView();
		return instance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		createElementView(parent);
		createToolBarActions();

		initialView();
		createContextMenu();
	}

	protected void createContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(drawComp);
		drawComp.setMenu(menu);
		// Be sure to register it so that other plug-ins can add actions.
		getSite().registerContextMenu(menuMgr, this);
	}

	protected void fillContextMenu(IMenuManager manager) {
		manager.add(resumeAction);
		manager.add(suspendAction);
		manager.add(terminateAction);
		manager.add(new Separator());
		manager.add(registerAction);
		manager.add(new Separator());
		// manager.add(stepIntoAction);
		// manager.add(stepOverAction);
		// manager.add(stepReturnAction);
		// manager.add(new Separator());
		// manager.add(createGroupAction);
		// manager.add(deleteGroupAction);
		// manager.add(new Separator());

		IElementGroup[] groups = groupManager.getSortedGroups();
		for (int i = 0; i < groups.length; i++) {
			IAction action = new GroupAction(groups[i].getID(), this);
			if (i == 0)
				action.setText(GroupAction.GROUP_ROOT);

			action.setChecked(groups[i].getID().equals(cur_group_id));
			manager.add(action);
		}

		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	protected void initialRoot() {
		if (groupManager.size() == 1) {
			uiDebugManager.initialProcess();
		}
	}

	/**
	 * Create Menu Action and initial the Element
	 * 
	 */
	protected void initialView() {
		initialRoot();
		IMenuManager manager = getViewSite().getActionBars().getMenuManager();
		IElementGroup[] groups = groupManager.getSortedGroups();
		for (int i = 1; i < groups.length; i++) {
			IAction action = new GroupAction(groups[i].getID(), this);
			groups[i].setSelected(false);
			manager.add(action);
		}
		selectGroup(groupManager.getGroupRoot().getID());
		updateMenu(manager);
	}

	/**
	 * Before this before, please make sure the group is set
	 * 
	 * @param manager
	 */
	public void updateMenu(IMenuManager manager) {
		IElementGroup[] groups = groupManager.getGroups();
		for (int i = 0; i < groups.length; i++) {
			IContributionItem item = manager.find(groups[i].getID());
			if (item != null && item instanceof ActionContributionItem) {
				IAction action = ((ActionContributionItem) item).getAction();
				action.setChecked(groups[i].isSelected());
				if (action.isChecked()) {
					changeTitle(action.getText(), groups[i].size());
				}
			}
		}
		enableDeleteAction(groups.length > 1 && !cur_group_id.equals(IGroupManager.GROUP_ROOT_ID));
		enableCreateGroupAction(cur_group_size > 0);
		enableRegisterAction(cur_group_size > 0);
	}

	private void changeTitle(final String title, final int size) {
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				setPartName(DEFAULT_TITLE + " - " + title + " (" + size + ")");
			}
		});
	}

	public void enableRegisterAction(boolean enable) {
		registerAction.setEnabled(enable);
	}

	public void enableDeleteAction(boolean enable) {
		// do not allow delete any process or group in Root
		deleteGroupAction.setEnabled(enable);
		deleteProcessAction.setEnabled(enable);
	}

	public void enableCreateGroupAction(boolean enable) {
		createGroupAction.setEnabled(enable);
	}

	/**
	 * create tool bar actions
	 */
	protected void createToolBarActions() {
		resumeAction = new ResumeAction(this);
		suspendAction = new SuspendAction(this);
		terminateAction = new TerminateAction(this);
		// stepIntoAction = new StepIntoAction(this);
		// stepOverAction = new StepOverAction(this);
		// stepReturnAction = new StepReturnAction(this);
		registerAction = new RegisterAction(this);
		createGroupAction = new CreateGroupAction(this);
		deleteGroupAction = new DeleteGroupAction(this);
		deleteProcessAction = new DeleteProcessAction(this);

		IToolBarManager toolBarMgr = getViewSite().getActionBars().getToolBarManager();
		toolBarMgr.add(resumeAction);
		toolBarMgr.add(suspendAction);
		toolBarMgr.add(terminateAction);
		toolBarMgr.add(new Separator());
		toolBarMgr.add(registerAction);
		toolBarMgr.add(new Separator());
		// toolBarMgr.add(stepIntoAction);
		// toolBarMgr.add(stepOverAction);
		// toolBarMgr.add(stepReturnAction);
		// toolBarMgr.add(new Separator());
		toolBarMgr.add(createGroupAction);
		toolBarMgr.add(deleteGroupAction);
		toolBarMgr.add(deleteProcessAction);

		// create Root menu
		IAction action = new GroupAction(IGroupManager.GROUP_ROOT_ID, this);
		action.setText(GroupAction.GROUP_ROOT);
		getViewSite().getActionBars().getMenuManager().add(action);
	}

	protected void createElementView(Composite parent) {
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

		drawComp.addPaintListener(new PaintListener() {
		public void paintControl(PaintEvent e) {
				if (cur_element_group != null)
					paintCanvas(e.gc);
			}
		});
 		drawComp.addListener(SWT.MouseHover, myDrawingMouseListener);
		drawComp.addListener(SWT.MouseDown, myDrawingMouseListener);
		drawComp.addListener(SWT.DragDetect, myDrawingMouseListener);
		drawComp.addListener(SWT.MouseMove, myDrawingMouseListener);
		drawComp.addListener(SWT.MouseUp, myDrawingMouseListener);
		drawComp.addListener(SWT.MouseExit, myDrawingMouseListener);
		drawComp.addListener(SWT.MouseDoubleClick, myDrawingMouseListener);
		drawComp.addListener(SWT.KeyDown, myDrawingMouseListener);
		drawComp.addListener(SWT.KeyUp, myDrawingMouseListener);
	}

	protected void drawingMouseHandleEvent(Event e) {
		final int mx = e.x;
		final int my = e.y;
		// System.out.println("drag:("+drag_x+":"+drag_y+"), mouse:("+mx+":"+my+"), view:("+view_width+":"+view_height+"), " + drawComp.getBounds());
		switch (e.type) {
		case SWT.MouseHover:
			// show tool tips only the mouse is in the view
			if (isInView(mx, my)) {
				int element_num = findElementNum(mx, my);
				if (element_num > -1)
					showToolTip(cur_element_group.get(element_num), mx, my);
			}
			break;
		case SWT.MouseDown:
			clearMouseSetting();
			// unselected all elements only occurred when there is click no press ctrl button
			deselect();
			break;
		case SWT.DragDetect:
			// System.out.println("Draf("+mx+":"+my+")");
			// no drawing selection area if the shift key is pressed
			if (keyCode == SWT.SHIFT)
				break;

			drag_x = mx;
			drag_y = my;
			break;
		case SWT.MouseMove:
			if (isDragging()) {
				if (selectionShell == null) {
					selectionShell = new Shell(drawComp.getShell(), SWT.NO_TRIM | SWT.NO_REDRAW_RESIZE | SWT.ON_TOP);
					selectionShell.setSize(0, 0);
					selectionShell.setVisible(true);
					selectionShell.setBackground(selectionShell.getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
				}
				//only select the bounded area
				deselect(groupManager.getBoundedElements());
				drawSelectedArea(drag_x, drag_y, mx, my);
				IElement[] elements = selectElements(getSelectedRect(drag_x, drag_y, mx, my));
				groupManager.add
				drawComp.redraw();
			}
			break;
		case SWT.MouseUp:
			disposeSelectionArea();

			if (!isDragging())
				selectElement(mx, my);

			clearMouseSetting();
			drawComp.redraw();
			break;
		case SWT.MouseDoubleClick:
			int element_num = findElementNum(mx, my);
			if (element_num > -1)
				registerElement(cur_element_group.get(element_num));

			isDoubleClick = true;
			break;
		case SWT.KeyDown:
			keyCode = e.keyCode;
			if (keyCode == '\u007f') // delete key
				removeProcess();
			else if (keyCode == SWT.PAGE_UP) // page up key
				scrollUp();
			else if (keyCode == SWT.PAGE_DOWN) // page down key
				scrollDown();

			break;
		case SWT.KeyUp:
			keyCode = SWT.None;
			break;
		default:
			// System.out.println("Other("+mx+":"+my+")");
			disposeSelectionArea();
			clearMouseSetting();
			break;
		}
	}

	protected void deselect(IElement[] elements) {
		for (int i=0; i<elements.length; i++) {
			elements[i].setSelected(false);
		}
	}
	
	protected void deselect() {
		//MAC use command, but others machine should use control key
		if (keyCode != DEFAULT_CTRL_KEY) {
			if (cur_element_group != null)
				cur_element_group.setAllSelect(false);
		}		
	}

	protected void clearMouseSetting() {
		isDoubleClick = false;
		drag_x = -1;
		drag_y = -1;
	}

	protected boolean isDragging() {
		return (drag_x >= 0 && drag_y >= 0);
	}

	protected boolean isInView(int mx, int my) {
		int o_y = sc.getOrigin().y;
		return (mx > 0 && mx < view_width && my > o_y && my < o_y + view_height);
	}

	protected void scrollUp() {
		int s_y = sc.getOrigin().y - (e_height + e_spacing_y) * visible_e_row;
		sc.setOrigin(0, s_y);
	}

	protected void scrollDown() {
		int s_y = sc.getOrigin().y + (e_height + e_spacing_y) * visible_e_row;
		sc.setOrigin(0, s_y);
	}

	protected void disposeSelectionArea() {
		if (selectionShell != null) {
			selectionShell.dispose();
			selectionShell = null;
		}
	}

	protected Rectangle getSelectedRect(int dx, int dy, int mx, int my) {
		int start_x = dx;
		int start_y = dy;
		int end_x = mx;
		int end_y = my;

		Point pt = drawComp.getSize();
		if (end_x < 0)
			end_x = 0;
		else if (end_x > (pt.x - e_offset_x))
			end_x = pt.x - e_offset_x;

		if (end_y < 0)
			end_y = 0;
		else if (end_y > pt.y)
			end_y = pt.y;

		// switch dx and mx or dy and my
		if (drag_x > end_x) {
			start_x = end_x;
			end_x = drag_x;
		}
		if (drag_y > end_y) {
			start_y = end_y;
			end_y = drag_y;
		}

		// System.out.println(start_y+":"+end_y+","+my);
		return new Rectangle(start_x, start_y, (end_x - start_x), (end_y - start_y));
	}

	protected void autoScroll(int mx, int my) {
		int s_top = sc.getOrigin().y;
		int s_end = drawComp.getSize().y - view_height;
		if (s_top > 0 && my < s_top) {
			sc.setOrigin(0, s_top - e_height - e_spacing_y);
		} else if (s_top != s_end && my > s_top + view_height) {
			sc.setOrigin(0, s_top + e_height + e_spacing_y);
		}
	}

	protected void drawSelectedArea(Rectangle rect, boolean hasTop, boolean hasBottom) {
		selectionShell.setLocation(rect.x, rect.y);
		selectionShell.setBounds(rect.x, rect.y, rect.width + rect_dot_disc, rect.height + rect_dot_disc);

		int last_x = rect.width - rect_dot_disc;
		int last_y = rect.height - rect_dot_disc;

		Region region = new Region();
		Rectangle pixel = new Rectangle(0, 0, rect_dot_size, rect_dot_size);
		for (int y = 0; y < rect.height; y += rect_dot_disc) {
			for (int x = 0; x < rect.width; x += rect_dot_disc) {
				if (fill_rect_dot || (y == 0 && hasTop) || (y >= last_y && hasBottom) || (x == 0 || x >= last_x)) {
					pixel.x = x;
					pixel.y = y;
					region.add(pixel);
				}
			}
		}
		selectionShell.setRegion(region);
	}

	protected void drawSelectedArea(int dx, int dy, int mx, int my) {
		boolean hasTop = true; // display the top of selectoin
		boolean hasBottom = true; // display the bottom of selection

		autoScroll(mx, my);
		Rectangle rect = getSelectedRect(dx, dy, mx, my);

		Point start_pt = getViewActualLocation(drawComp, null, rect.x, rect.y);
		Point end_pt = getViewActualLocation(drawComp, null, rect.x + rect.width, rect.y + rect.height);
		int top_y = getDisplay().map(sc, null, 0, 0).y;
		int bottom_y = getDisplay().map(sc, null, sc.getSize()).y;

		if (start_pt.y < top_y) {
			start_pt.y = top_y;
			hasTop = false;
		}

		if (end_pt.y > bottom_y) {
			end_pt.y = bottom_y;
			hasBottom = false;
		}

		rect.height = end_pt.y - start_pt.y;
		rect.x = start_pt.x;
		rect.y = start_pt.y;

		drawSelectedArea(rect, hasTop, hasBottom);
	}

	protected void hideToolTip() {
		if (toolTipShell != null) {
			toolTipShell.dispose();
			toolTipShell = null;
		}
	}

	protected void showToolTip(IElement element, int mx, int my) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("ID: " + element.getID());
		IElementGroup[] groups = groupManager.getGroupsWithElement(element.getID());
		if (groups.length > 1)
			buffer.append("\nGroup: ");
		for (int i = 1; i < groups.length; i++) {
			buffer.append(groups[i].getID());
			if (i < groups.length - 1)
				buffer.append(",");
		}
		buffer.append("\nStatus: " + uiDebugManager.getProcessStatus(element.getID()));
		showToolTip(buffer.toString(), mx, my);
	}

	protected void showToolTip(String text, int mx, int my) {
		hideToolTip();
		toolTipShell = new Shell(drawComp.getShell(), SWT.ON_TOP | SWT.TOOL);
		Label toolTipLabel = new Label(toolTipShell, SWT.LEFT);
		Display display = toolTipShell.getDisplay();
		toolTipLabel.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		toolTipLabel.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		toolTipLabel.setText(text);

		Point l_size = toolTipLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		l_size.x += 2;
		l_size.y += 2;
		toolTipLabel.setSize(l_size);
		toolTipShell.pack();

		Rectangle area = toolTipShell.getClientArea();
		toolTipLabel.setSize(area.width, area.height);

		// Find out the location to display tooltip
		Loc e_loc = findLocation(mx, my);
		Point s_size = toolTipShell.getSize();

		int new_x = e_loc.x - (s_size.x / 2);
		Rectangle shell_rect = Display.getCurrent().getBounds();
		// reset the x to fit into screen size
		new_x = (new_x < shell_rect.x) ? 0 : ((new_x + s_size.x) > shell_rect.width) ? (shell_rect.width - s_size.x - 10) : new_x;
		int new_y = e_loc.y - s_size.y;
		new_y = (new_y <= sc.getOrigin().y) ? (e_loc.y + e_height + e_spacing_y) : new_y;

		toolTipShell.setLocation(getViewActualLocation(drawComp, null, new_x, new_y));
		toolTipShell.setVisible(true);

		createToolTipTimer();
	}

	protected Point getViewActualLocation(Control from, Control to, int mx, int my) {
		Point mappedpt = getDisplay().map(from, to, 0, 0);
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
		if (hovertimer != null) {
			try {
				hovertimer.cancel();
			} catch (IllegalStateException e) {
			}
			hovertimer = null;
		}
	}

	protected void selectElements(int cur_element_num) {
		if (keyCode != SWT.SHIFT)
			fisrt_selected_element_id = cur_element_num;
		else {
			if (fisrt_selected_element_id > -1 && cur_element_num > -1) {
				int start_element_id = fisrt_selected_element_id > cur_element_num ? cur_element_num : fisrt_selected_element_id;
				int end_element_id = (start_element_id == cur_element_num ? fisrt_selected_element_id : cur_element_num) + 1;

				IElement[] elements = cur_element_group.getSortedElements();
				for (int num = start_element_id; num < end_element_id; num++) {
					if (num >= elements.length)
						break;

					elements[num].setSelected(true);
				}
			}
		}
	}

	protected void selectElement(int e_x, int e_y) {
		int element_num = findElementNum(e_x, e_y);
		if (element_num > -1)
			cur_element_group.select(element_num, true);

		selectElements(element_num);
	}

	protected IElement[] selectElements(Rectangle rect) {
		//add the selection area size by 2 to prevent selection edge on element
		rect.x += rect_dot_size * 2;
		rect.y += rect_dot_size * 2;
		rect.width -= rect_dot_size * 4;
		rect.height -= rect_dot_size * 4;

		Loc e_loc = findEstimateLocation(rect.x, rect.y);

		// Complex calculation
		int total_col = 0;
		int max_col_width = (visible_e_col * (e_width + e_spacing_x)) + e_offset_x;
		if (rect.x + rect.width > e_loc.x && rect.x < max_col_width)
			total_col = (rect.x + rect.width - e_loc.x) / (e_width + e_spacing_x) + 1;
		// prevent to calculate the gap between the element and vertical scrollbar
		if (rect.x + rect.width > max_col_width)
			total_col--;

		// finish it if the total_col is 0
		if (total_col == 0)
			return new IElement[0];

		int total_row = 0;
		if (rect.y + rect.height > e_loc.y)
			total_row = ((rect.y + rect.height - e_loc.y) / (e_height + e_spacing_y) + 1);

		// finish it if the total_row is 0
		if (total_row == 0)
			return new IElement[0];

		// the first element
		int init_element_num = (visible_e_col * (e_loc.row - 1) + e_loc.col) - 1;
		IElement[] elements = cur_element_group.getSortedElements();
		List selectedElements = new ArrayList();
		
		for (int row_count = 0; row_count < total_row; row_count++) {
			for (int col_count = 0; col_count < total_col; col_count++) {
				int num = init_element_num + col_count + (row_count * visible_e_col);
				if (num >= elements.length)
					break;

				// store the first selected element id in this selection area
				// if (row_count == 0 && col_count == 0)
				// selectElements(num);

				elements[num].setSelected(true);
				selectedElements.add(elements[num]);
			}
		}
		return (IElement[])selectedElements.toArray(new IElement[selectedElements.size()]);
	}

	protected Loc findEstimateLocation(int mx, int my) {
		// note: plus spacing to contain elements within the spacing
		int col = (Math.abs(mx - e_offset_x + e_spacing_x) / (e_width + e_spacing_x)) + 1;
		col = col >= visible_e_col ? visible_e_col : col;
		int loc_x = e_offset_x + (e_width + e_spacing_x) * (col - 1);

		// note: plus spacing to contain elements within the spacing
		int row = (Math.abs(my - e_offset_y + e_spacing_y) / (e_height + e_spacing_y)) + 1;
		int loc_y = e_offset_x + (e_height + e_spacing_y) * (row - 1);
		return new Loc(loc_x, loc_y, col, row);
	}

	protected Loc findLocation(int mx, int my) {
		// note: plus spacing to contain elements within the spacing
		int col = (Math.abs(mx - e_offset_x + e_spacing_x) / (e_width + e_spacing_x)) + 1;
		col = col >= visible_e_col ? visible_e_col : col;
		int loc_x = e_offset_x + (e_width + e_spacing_x) * (col - 1);
		if (mx > loc_x && mx < (loc_x + e_width)) {
			// note: plus spacing to contain elements within the spacing
			int row = (Math.abs(my - e_offset_y + e_spacing_y) / (e_height + e_spacing_y)) + 1;
			int loc_y = e_offset_x + (e_height + e_spacing_y) * (row - 1);
			if (my > loc_y && my < (loc_y + e_height))
				return new Loc(loc_x, loc_y, col, row);
		}
		return null;
	}

	protected int findElementNum(int mx, int my) {
		Loc e_loc = findLocation(mx, my);
		if (e_loc == null)
			return -1;

		int element_num = visible_e_col * (e_loc.row - 1) + e_loc.col;
		if (element_num <= cur_group_size)
			return (element_num - 1);

		return -1;
	}

	protected void updateViewSize(boolean isVSrcoll) {
		Point pt = sc.getSize();
		view_width = isVSrcoll ? (pt.x - sc.getVerticalBar().getSize().x) : pt.x;
		view_height = pt.y;
		visible_e_col = (view_width - e_offset_x) / (e_width + e_spacing_x);
		visible_e_row = (view_height - e_offset_x) / (e_height + e_spacing_y) + 1;
	}

	protected int calculateHeight() {
		if (visible_e_col > 0) {
			int max_element_row = (cur_group_size / visible_e_col) + ((cur_group_size % visible_e_col == 0) ? 0 : 1);
			return max_element_row * (e_height + e_spacing_y) + e_offset_x;
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
		if (new_height > view_height) {// display v_scroll
			updateViewSize(true);
			new_height = calculateHeight();
		}
		drawComp.setSize(view_width, new_height);
		//System.out.println(view_width + ":" + new_height);
	}

	/**
	 * Print visible area ONLY Find out process numbers of beginning and ending
	 */
	protected void paintCanvas(GC g) {
		IElement[] elements = cur_element_group.getSortedElements();

		int start_row = Math.round((sc.getOrigin().y - e_offset_x)
				/ (e_height + e_spacing_y));
		int start_num = start_row * visible_e_col;

		int end_num = ((start_row + visible_e_row + 1) * visible_e_col);
		end_num = (end_num > elements.length) ? elements.length : end_num;

		// System.out.println("("+view_width+":"+view_height+"), (Start: " +
		// start_num + ", End: " + end_num+")");
		for (int i = start_num; i < end_num; i++) {
			drawElement(i + 1, elements[i], g);
		}
	}

	protected void drawElement(int index, IElement element, GC g) {
		int x = (visible_e_col > index) ? index : (index % visible_e_col);
		x = (x == 0) ? visible_e_col : x;
		int x_loc = e_offset_x + ((x - 1) * (e_width + e_spacing_x));

		int y = (x == visible_e_col) ? index / visible_e_col : (visible_e_col > index) ? 1 : ((index / visible_e_col) + 1);
		int y_loc = e_offset_x + ((y - 1) * (e_height + e_spacing_y));

		try {
			drawing(g, uiDebugManager.getProcessStatus(element.getID()), element.isSelected(), element.isRegistered(), x_loc, y_loc);
		} catch (NullPointerException e) {
		}
	}

	protected void drawing(GC g, String status, boolean selected, boolean registered, int x_loc, int y_loc) {
		g.drawImage(getStatusIcon(status, selected), x_loc, y_loc);
		if (registered) {
			g.drawRectangle(x_loc, y_loc, e_width, e_height);
		}
	}

	// FIXME
	protected Image getStatusIcon(String status, boolean selected) {
		if (status.equals(IDebugParallelModelListener.STATUS_RUNNING)) {
			return statusImages[1][selected ? 1 : 0];
		} else if (status.equals(IDebugParallelModelListener.STATUS_STARTING)) {
			return statusImages[2][selected ? 1 : 0];
		} else if (status.equals(IDebugParallelModelListener.STATUS_STOPPED)) {
			return statusImages[3][selected ? 1 : 0];
		} else if (status.equals(IDebugParallelModelListener.STATUS_SUSPENDED)) {
			return statusImages[4][selected ? 1 : 0];
		} else {
			return statusImages[0][selected ? 1 : 0];
		}
	}

	public void setSelection(ISelection selection) {
		if (selection instanceof StructuredSelection) {
			for (Iterator i = ((StructuredSelection) selection).iterator(); i.hasNext();) {
				Object obj = i.next();
				if (obj instanceof IPProcess) {
					IElement element = cur_element_group.getElement(((IPProcess) obj).getKeyString());
					if (element != null)
						element.setSelected(true);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		drawComp.setFocus();
	}

	public ISelection getSelection() {
		return new StructuredSelection(cur_element_group.getSelectedElements());
	}

	public String getCurrentGroupID() {
		return cur_group_id;
	}

	public void selectGroup(String groupID) {
		deSelectGroup();
		this.cur_element_group = groupManager.getGroup(groupID);
		this.cur_group_id = cur_element_group.getID();
		this.cur_group_size = cur_element_group.size();
		cur_element_group.setSelected(true);
	}

	public void deSelectGroup() {
		if (cur_element_group != null)
			cur_element_group.setSelected(false);
	}

	public IElementGroup getCurrentGroup() {
		return cur_element_group;
	}

	public void redraw() {
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (!drawComp.isDisposed()) {
					adjustDrawingView();
					drawComp.redraw();
				}
			}
		});
	}

	public void refresh() {
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (!drawComp.isDisposed())
					drawComp.redraw();
			}
		});
	}

	public void removeProcess() {
		if (!cur_group_id.equals(IGroupManager.GROUP_ROOT_ID)) {
			deleteProcessAction.run(cur_element_group.getSelectedElements());
		}
	}

	public void registerElement(IElement element) {
		if (element.isRegistered())
			uiDebugManager.unregisterElements(new IElement[] { element });
		else
			uiDebugManager.registerElements(new IElement[] { element });

		element.setRegistered(!element.isRegistered());
	}

	public void registerSelectedElements() {
		if (cur_element_group != null) {
			IElement[] elements = cur_element_group.getSelectedElements();
			for (int i = 0; i < elements.length; i++) {
				elements[i].setRegistered(true);
			}
			uiDebugManager.registerElements(elements);
		}
	}

	/*
	 * FIXME Should implemented IParallelModelListener
	 */
	public void run() {
		initialView();
		redraw();
	}

	public void start() {
		initialView();
		redraw();
	}

	public void stop() {
		refresh();
	}

	public void suspend() {
		refresh();
	}

	public void exit() {
		refresh();
	}

	public void error() {
		refresh();
	}

	public void abort() {
	}

	public void stopped() {
	}

	public void monitoringSystemChangeEvent(Object object) {
	}

	public void execStatusChangeEvent(Object object) {
	}

	public void sysStatusChangeEvent(Object object) {
	}

	public void processOutputEvent(Object object) {
	}

	public void errorEvent(Object object) {
	}

	public void updatedStatusEvent() {
	}

	private class Loc {
		private int x = 0;
		private int y = 0;
		private int col = 0;
		private int row = 0;

		public Loc(int x, int y, int col, int row) {
			this.x = x;
			this.y = y;
			this.col = col;
			this.row = row;
		}
		public int getCol() {
			return col;
		}
		public void setCol(int col) {
			this.col = col;
		}
		public int getRow() {
			return row;
		}
		public void setRow(int row) {
			this.row = row;
		}
		public int getX() {
			return x;
		}
		public void setX(int x) {
			this.x = x;
		}
		public int getY() {
			return y;
		}
		public void setY(int y) {
			this.y = y;
		}
	}
}
