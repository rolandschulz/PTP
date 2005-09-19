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
package org.eclipse.ptp.ui.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ptp.ui.IManager;
import org.eclipse.ptp.ui.listeners.IPaintListener;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;
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
public abstract class AbstractParallelElementView extends AbstractParallelView implements IPaintListener {
	// Composite
	protected ScrolledComposite sc = null;
	protected Composite drawComp = null;
	// protected Image fBuffer = null;
	// default element info
	protected int e_offset_x = 5;
	protected int e_spacing_x = 4;
	protected int e_offset_y = 5;
	protected int e_spacing_y = 4;
	protected int e_width = 16;
	protected int e_height = 16;
	// Set
	// protected ISetManager setManager = null;
	protected IManager manager = null;
	protected IElementSet cur_element_set = null;
	protected int cur_set_size = 0;
	protected int fisrt_selected_element_id = -1;
	// view info
	protected int view_width = 200;
	protected int view_height = 40;
	protected int visible_e_col = 10;
	protected int visible_e_row = 5;
	// tooltip
	protected Shell toolTipShell = null;
	protected Label toolTipLabel = null;
	// protected Timer hovertimer = null;
	// protected final int hide_time = 2000;
	// mouse
	protected int keyCode = SWT.None;
	protected int last_event_type = SWT.NONE;
	// selection area
	protected int drag_x = -1;
	protected int drag_y = -1;
	protected Shell selectionShell = null;
	protected boolean fill_rect_dot = false;
	protected int rect_dot_size = 2;
	protected int rect_dot_disc = 3;
	// key
	protected int DEFAULT_CTRL_KEY = SWT.CTRL;
	protected int A_KEY = '\u0061';
	//title
	protected final String EMPTY_TITLE = " "; 
	protected Listener myDrawingMouseListener = new Listener() {
		public void handleEvent(Event e) {
			drawingMouseHandleEvent(e);
		}
	};
	protected SelectedAreaInfo selectedAreaInfo = new SelectedAreaInfo();

	public AbstractParallelElementView() {
		initElementAttribute();
	}
	protected void initialKey(String os) {
		if (os.equals(Platform.OS_MACOSX)) {
			DEFAULT_CTRL_KEY = SWT.COMMAND;
		}
	}
	public void createPartControl(Composite parent) {
		initialKey(Platform.getOS());
		createView(parent);
		setContentDescription(EMPTY_TITLE);
		manager.addPaintListener(this);
	}
	protected void createView(Composite parent) {
		createElementView(parent);
	}
	public IManager getUIManager() {
		return manager;
	}
	public IElementHandler getCurrentElementHandler() {
		return manager.getElementHandler(getCurrentID());
	}	
	protected void changeTitle(String title, String setName, int size) {
		changeTitle(" " + title + " - " + setName + " [" + size + "]");
	}
	protected void changeTitle(final String message) {
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				setContentDescription(message);
			}
		});
	}
	protected Composite createElementView(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gd = new GridLayout(1, false);
		gd.marginHeight = gd.marginWidth = 0;
		composite.setLayout(gd);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		sc = new ScrolledComposite(composite, SWT.V_SCROLL);
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
		drawComp.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		sc.setContent(drawComp);
		drawComp.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				if (cur_element_set != null)
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
		return composite;
	}
	protected void drawingMouseHandleEvent(Event e) {
		// Do nothing when there is no element set
		if (cur_element_set == null)
			return;
		final int mx = e.x;
		final int my = e.y;
		// System.out.println("drag:("+drag_x+":"+drag_y+"), mouse:("+mx+":"+my+"), view:("+view_width+":"+view_height+"), " + drawComp.getBounds());
		switch (e.type) {
		case SWT.MouseHover:
			mouseHoverEvent(mx, my);
			break;
		case SWT.MouseDown:
			/*
			 * int DOUBLE_CLICK_TIME = Display.getCurrent().getDoubleClickTime(); doubleClickExpirationTime = e.time + DOUBLE_CLICK_TIME;
			 */
			mouseDownEvent(mx, my);
			last_event_type = SWT.MouseDown;
			break;
		case SWT.DragDetect:
			mouseDragEvent(mx, my);
			break;
		case SWT.MouseMove:
			mouseMoveEvent(mx, my);
			break;
		case SWT.MouseUp:
			if (last_event_type != SWT.MouseDoubleClick)
				mouseUpEvent(mx, my);
			last_event_type = SWT.MouseUp;
			break;
		case SWT.MouseDoubleClick:
			mouseDoubleClickEvent(mx, my);
			last_event_type = SWT.MouseDoubleClick;
			break;
		case SWT.KeyDown:
			keyDownEvent(mx, my, e.keyCode);
			break;
		case SWT.KeyUp:
			keyUpEvent(mx, my, e.keyCode);
			break;
		default:
			otherEvent(mx, my, e.keyCode);
			break;
		}
	}
	protected void mouseHoverEvent(int mx, int my) {}
	protected void mouseDownEvent(int mx, int my) {
		clearMouseSetting();
		// unselected all elements only occurred when there is click no press ctrl button
		deselect();
		elementRedraw();
	}
	protected void mouseDragEvent(int mx, int my) {
		// System.out.println("Draf("+mx+":"+my+")");
		// no drawing selection area if the shift key is pressed
		if (keyCode != SWT.SHIFT) {
			drag_x = mx;
			drag_y = my;
		}
	}
	protected void mouseMoveEvent(int mx, int my) {
		// show tool tips only the mouse is in the view
		if (isInView(mx, my))
			showToolTip(mx, my);
		else
			hideToolTip();
		if (isDragging()) {
			if (selectionShell == null) {
				selectionShell = new Shell(drawComp.getShell(), SWT.NO_TRIM | SWT.ON_TOP);
				selectionShell.setSize(0, 0);
				selectionShell.setVisible(true);
				selectionShell.setBackground(selectionShell.getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
			}
			// only select the bounded area
			deselect(selectedAreaInfo.getBoundedElements());
			SelectionInfo selectionInfo = getSelectedRect(drag_x, drag_y, mx, my);
			autoScroll(mx, my);
			drawSelectedArea(selectionInfo);
			selectedAreaInfo.addBoundedElement(selectElements(selectionInfo.rect));
			redrawSelectedArea(selectionInfo);
		}
	}
	// TODO NOT FINISHED
	protected void redrawSelectedArea(SelectionInfo selectionInfo) {
		Rectangle oldRect = selectedAreaInfo.getBoundedRect();
		Rectangle newRect = selectionInfo.rect;
		selectedAreaInfo.setBoundedRect(newRect);
		if (oldRect.intersects(newRect)) {
			// System.out.println(oldRect + " - " + rect);
			if (selectedAreaInfo.compareArea(oldRect, newRect)) {
				// use old rect to redraw
				newRect = oldRect;
			}
		} else {
			oldRect = selectedAreaInfo.getBoundedArea(oldRect);
			elementRedraw(oldRect.x, oldRect.y, oldRect.width, oldRect.height, false);
		}
		newRect = selectedAreaInfo.getBoundedArea(newRect);
		elementRedraw(newRect.x, newRect.y, newRect.width, newRect.height, false);
	}
	protected void mouseUpEvent(int mx, int my) {
		disposeSelectionArea();
		if (isDragging())
			selectedAreaInfo.removeAllBoundedElements();
		else
			selectElement(mx, my);
		clearMouseSetting();
		elementRedraw();
	}
	protected abstract void doubleClickAction(int element_num);
	protected void mouseDoubleClickEvent(int mx, int my) {
		int element_num = findElementNum(mx, my);
		if (element_num > -1)
			doubleClickAction(element_num);
	}
	protected void keyDownEvent(int mx, int my, int keyCode) {
		hideToolTip();
		if (keyCode == SWT.PAGE_UP)
			scrollUp();
		else if (keyCode == SWT.PAGE_DOWN)
			scrollDown();
		else if (keyCode == A_KEY) {
			if (this.keyCode == DEFAULT_CTRL_KEY)
				selectAll();
		}
		this.keyCode = keyCode;
	}
	protected void keyUpEvent(int mx, int my, int keyCode) {
		this.keyCode = SWT.None;
	}
	protected void otherEvent(int mx, int my, int keyCode) {
		// System.out.println("Other("+mx+":"+my+")");
		hideToolTip();
		disposeSelectionArea();
		clearMouseSetting();
	}
	protected void selectAll() {
		cur_element_set.setAllSelect(true);
		elementRedraw();
	}
	protected void deselect(IElement[] elements) {
		for (int i = 0; i < elements.length; i++) {
			elements[i].setSelected(false);
		}
	}
	protected void deselect() {
		// MAC use command, but others machine should use control key
		if (keyCode != DEFAULT_CTRL_KEY) {
			if (cur_element_set != null)
				cur_element_set.setAllSelect(false);
		}
	}
	protected void clearMouseSetting() {
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
	private SelectionInfo getSelectedRect(int dx, int dy, int mx, int my) {
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
		/*
		 * int width = Math.abs(start_x - end_x); int height = Math.abs(start_y - end_y); if (drag_x > end_x) { end_x = drag_x; start_x = Math.abs(width - end_x); } if (drag_y > end_y) { end_y = drag_y; start_y = Math.abs(height - end_y); }
		 */
		/*
		 * int t_sx = start_x; int t_sy = start_y; int t_ex = end_x; int t_ey = end_y;
		 * 
		 * if (drag_x > t_ex) { t_sx = t_ex; t_ex = drag_x; } if (drag_y > t_ey) { t_sy = t_ey; t_ey = drag_y; } System.out.println("start_x: " + start_x + ", start_y: " + start_y + ", end_x: " + end_x + ", end_y: " + end_y + ", width: " + width + ", height: " + height); System.out.println("tmp__sx: " +
		 * t_sx + ", tmp__sy: " + t_sy + ", t__ex: " + t_ex + ", t__sy: " + t_ey + ", tmp_w: " + t_width + ", tmp__h: " + t_height);
		 */
		int direction = SelectionInfo.SE;
		if (drag_x > end_x) {
			start_x = end_x;
			end_x = drag_x;
			direction = SelectionInfo.SW;
		}
		if (drag_y > end_y) {
			start_y = end_y;
			end_y = drag_y;
			if (direction == SelectionInfo.SW)
				direction = SelectionInfo.NW;
			else
				direction = SelectionInfo.NE;
		}
		return new SelectionInfo(start_x, start_y, end_x, end_y, direction);
		// return new Rectangle(start_x, start_y, width, height);
	}
	private void autoScroll(int mx, int my) {
		int s_top = sc.getOrigin().y;
		int s_end = drawComp.getSize().y - view_height;
		if (s_top > 0 && my < s_top) {
			sc.setOrigin(0, s_top - e_height - e_spacing_y);
		} else if (s_top != s_end && my > s_top + view_height) {
			sc.setOrigin(0, s_top + e_height + e_spacing_y);
		}
	}
	private void drawSelectedArea(Rectangle rect, boolean hasTop, boolean hasBottom) {
		int last_x = ((rect.width / rect_dot_disc) - 1) * rect_dot_disc;
		int last_y = ((rect.height / rect_dot_disc) - 1) * rect_dot_disc;
		int start_x = 0;
		int start_y = 0;
		int end_x = last_x;
		int end_y = last_y;
		Rectangle pixel = new Rectangle(0, 0, rect_dot_size, rect_dot_size);
		Region region = new Region();
		for (int y = start_y; y <= end_y; y += rect_dot_disc) {
			for (int x = start_x; x <= end_x; x += rect_dot_disc) {
				if (fill_rect_dot || (y == 0 && hasTop) || (y == end_y && hasBottom) || (x == 0 || x == end_x)) {
					pixel.x = x;
					pixel.y = y;
					region.add(pixel);
				}
			}
		}
		selectionShell.setRegion(region);
		selectionShell.setLocation(rect.x, rect.y);
		selectionShell.setBounds(rect.x, rect.y, rect.width, rect.height);
	}
	private void drawSelectedArea(SelectionInfo selectionInfo) {
		boolean hasTop = true; // display the top of selectoin
		boolean hasBottom = true; // display the bottom of selection
		Rectangle newRect = selectionInfo.newRect();
		Point end_pt = getViewActualLocation(drawComp, null, newRect.x + newRect.width, newRect.y + newRect.height);
		Point start_pt = new Point(end_pt.x - newRect.width, end_pt.y - newRect.height);
		int top_y = getDisplay().map(sc, null, 0, 0).y;
		int bottom_y = getDisplay().map(sc, null, sc.getSize()).y;
		switch (selectionInfo.direction) {
		case SelectionInfo.SW:
			break;
		case SelectionInfo.NW:
			break;
		case SelectionInfo.NE:
			break;
		default:
			break;
		}
		if (start_pt.y < top_y) {
			start_pt.y = top_y;
			hasTop = false;
		}
		if (end_pt.y > bottom_y) {
			end_pt.y = bottom_y;
			hasBottom = false;
		}
		newRect.height = end_pt.y - start_pt.y;
		newRect.x = start_pt.x;
		newRect.y = start_pt.y;
		drawSelectedArea(newRect, hasTop, hasBottom);
	}
	protected void showToolTip(int mx, int my) {
		int element_num = findElementNum(mx, my);
		if (element_num == -1) {
			hideToolTip();
			return;
		}
		if (toolTipShell == null) {
			toolTipShell = new Shell(drawComp.getShell(), SWT.ON_TOP | SWT.TOOL);
			toolTipLabel = new Label(toolTipShell, SWT.LEFT);
			Display display = toolTipShell.getDisplay();
			toolTipLabel.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
			toolTipLabel.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		}
		toolTipLabel.setText(getToolTipText(element_num));
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
		Rectangle shell_rect = sc.getBounds();
		// reset the x to fit into screen size, assume scrollbar is 10 width
		new_x = (new_x < shell_rect.x) ? 0 : ((new_x + s_size.x) > (shell_rect.width - 10)) ? (shell_rect.width - s_size.x - 10) : new_x;
		int new_y = e_loc.y - s_size.y;
		new_y = (new_y <= sc.getOrigin().y) ? (e_loc.y + e_height + e_spacing_y + 5) : new_y - 5;
		toolTipShell.setLocation(getViewActualLocation(drawComp, null, new_x, new_y));
		toolTipShell.setVisible(true);
		// createToolTipTimer();
	}
	protected abstract String getToolTipText(int element_num);
	protected Point getViewActualLocation(Control from, Control to, int mx, int my) {
		Point mappedpt = getDisplay().map(from, to, 0, 0);
		Point newpt = new Point(mx, my);
		newpt.x += mappedpt.x;
		newpt.y += mappedpt.y;
		return newpt;
	}
	protected void hideToolTip() {
		if (toolTipShell != null) {
			toolTipLabel.dispose();
			toolTipShell.dispose();
			toolTipLabel = null;
			toolTipShell = null;
		}
	}
	/*
	 * protected void createToolTipTimer() { cancelToolTipTimer(); hovertimer = new Timer(); hovertimer.schedule(new TimerTask() { public void run() { cancelToolTipTimer(); drawComp.getDisplay().asyncExec(new Runnable() { public void run() { hideToolTip(); } }); } }, hide_time); }
	 * 
	 * private void cancelToolTipTimer() { if (hovertimer != null) { try { hovertimer.cancel(); } catch (IllegalStateException e) { } hovertimer = null; } }
	 */
	// TODO not quite good idea for fisrt_selected_element_id
	protected void selectElements(int cur_element_num) {
		if (keyCode != SWT.SHIFT)
			fisrt_selected_element_id = cur_element_num;
		else {
			if (fisrt_selected_element_id > -1 && cur_element_num > -1) {
				int start_element_id = fisrt_selected_element_id > cur_element_num ? cur_element_num : fisrt_selected_element_id;
				int end_element_id = (start_element_id == cur_element_num ? fisrt_selected_element_id : cur_element_num) + 1;
				IElement[] elements = cur_element_set.getSortedElements();
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
			cur_element_set.select(element_num, true);
		selectElements(element_num);
	}
	protected IElement[] selectElements(Rectangle boundedRect) {
		// add the selection area size by 2 to prevent selection edge on element
		Rectangle rect = new Rectangle(0, 0, 0, 0);
		rect.x = boundedRect.x + rect_dot_size * 2;
		rect.y = boundedRect.y + rect_dot_size * 2;
		rect.width = boundedRect.width - rect_dot_size * 4;
		rect.height = boundedRect.height - rect_dot_size * 4;
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
		IElement[] elements = cur_element_set.getSortedElements();
		List selectedElements = new ArrayList();
		for (int row_count = 0; row_count < total_row; row_count++) {
			for (int col_count = 0; col_count < total_col; col_count++) {
				int num = init_element_num + col_count + (row_count * visible_e_col);
				if (num >= elements.length)
					break;
				// store the first selected element id in this selection area
				// if (row_count == 0 && col_count == 0)
				// selectElements(num);
				if (!elements[num].isSelected()) {
					selectedElements.add(elements[num]);
					elements[num].setSelected(true);
				}
			}
		}
		boundedRect.width = (total_col + 1) * (e_width + e_spacing_x);
		boundedRect.height = (total_row + 1) * (e_height + e_spacing_y);
		return (IElement[]) selectedElements.toArray(new IElement[selectedElements.size()]);
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
		if (element_num <= cur_set_size)
			return (element_num - 1);
		return -1;
	}
	private void updateViewSize(boolean isVSrcoll) {
		Point pt = sc.getSize();
		view_width = isVSrcoll ? (pt.x - sc.getVerticalBar().getSize().x) : pt.x;
		view_height = pt.y;
		visible_e_col = (view_width - e_offset_x) / (e_width + e_spacing_x);
		visible_e_row = (view_height - e_offset_x) / (e_height + e_spacing_y) + 1;
	}
	private int calculateHeight() {
		if (visible_e_col > 0) {
			int max_element_row = (cur_set_size / visible_e_col) + ((cur_set_size % visible_e_col == 0) ? 0 : 1);
			int max_height = max_element_row * (e_height + e_spacing_y) + e_offset_x;
			return (max_height < view_height ? view_height : max_height);
		}
		return view_height;
	}
	/**
	 * Calculate the size of drawing component
	 * 
	 */
	private void adjustDrawingView() {
		updateViewSize(false);
		int new_height = calculateHeight();
		if (new_height > view_height) {// display v_scroll
			updateViewSize(true);
			new_height = calculateHeight();
		}
		drawComp.setSize(view_width, new_height);
		// System.out.println(view_width + ":" + new_height);
	}
	/*
	 * FIXME not completed yet -- implement size for drawing private void doubleBufferPaint(GC dest) { Point size= drawComp.getSize(); if (size.x <= 0 || size.y <= 0) return;
	 * 
	 * if (fBuffer != null) { Rectangle r = fBuffer.getBounds(); if (r.width != size.x || r.height != size.y) { fBuffer.dispose(); fBuffer= null; } } if (fBuffer == null) fBuffer= new Image(drawComp.getDisplay(), size.x, size.y);
	 * 
	 * GC gc= new GC(fBuffer); try { gc.setBackground(drawComp.getBackground()); gc.fillRectangle(0, 0, size.x, size.y); paintCanvas(gc); } finally { gc.dispose(); } dest.drawImage(fBuffer, 0, 0); }
	 */
	/**
	 * Print visible area ONLY Find out process numbers of beginning and ending
	 */
	protected void paintCanvas(GC g) {
		IElement[] elements = cur_element_set.getSortedElements();
		int start_row = Math.round((sc.getOrigin().y - e_offset_x) / (e_height + e_spacing_y));
		int start_num = start_row * visible_e_col;
		int end_num = ((start_row + visible_e_row + 1) * visible_e_col);
		end_num = (end_num > elements.length) ? elements.length : end_num;
		// System.out.println("("+view_width+":"+view_height+"), (Start: " + start_num + ", End: " + end_num+")");
		for (int i = start_num; i < end_num; i++)
			drawElement(i + 1, elements[i], g);
	}
	protected void drawElement(int index, IElement element, GC g) {
		int x = (visible_e_col > index) ? index : (index % visible_e_col);
		x = (x == 0) ? visible_e_col : x;
		int x_loc = e_offset_x + ((x - 1) * (e_width + e_spacing_x));
		int y = (x == visible_e_col) ? index / visible_e_col : (visible_e_col > index) ? 1 : ((index / visible_e_col) + 1);
		int y_loc = e_offset_x + ((y - 1) * (e_height + e_spacing_y));
		drawingElement(g, element, x_loc, y_loc);
	}
	protected void drawingElement(GC g, IElement element, int x_loc, int y_loc) {
		Image statusImage = getStatusIcon(element);
		if (statusImage != null) {
			g.drawImage(statusImage, x_loc, y_loc);
			drawingRegisterElement(element, g, x_loc, y_loc, e_width, e_height);
		}
	}
	protected abstract Image getStatusIcon(IElement element);
	// allow change color in rectange
	protected void drawingRegisterElement(IElement element, GC g, int x_loc, int y_loc, int width, int height) {
		if (element.isRegistered())
			g.drawRectangle(x_loc, y_loc, width, height);
	}
	public void setSelection(ISelection selection) {
		if (selection instanceof StructuredSelection) {
			for (Iterator i = ((StructuredSelection) selection).iterator(); i.hasNext();) {
				Object obj = i.next();
				if (obj instanceof IElement) {
					((IElement) obj).setSelected(true);
				}
			}
		}
	}
	public void dispose() {
		manager.removePaintListener(this);
		super.dispose();
	}
	public void setFocus() {
		drawComp.setFocus();
	}
	public ISelection getSelection() {
		if (cur_element_set == null)
			return new StructuredSelection();
		return new StructuredSelection(cur_element_set.getSelectedElements());
	}
	public String getCurrentSetID() {
		return manager.getCurrentSetId();
	}
	public void fireChangeEvent(IElementSet cur_set, IElementSet pre_set) {
		manager.fireEvent(IManager.CHANGE_SET_TYPE, null, cur_set, pre_set);
	}
	public void selectSet(IElementSet set) {
		fireChangeEvent(set, cur_element_set);
		deSelectSet();
		cur_element_set = set;
		cur_set_size = 0;
		if (cur_element_set != null) {
			cur_set_size = cur_element_set.size();
			cur_element_set.setSelected(true);
			manager.setCurrentSetId(cur_element_set.getID());
		}
	}
	public void deSelectSet() {
		if (cur_element_set != null) {
			cur_element_set.setSelected(false);
			cur_element_set.setAllSelect(false);
		}
	}
	public IElementSet getCurrentSet() {
		return cur_element_set;
	}
	public void redraw() {
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (!drawComp.isDisposed())
					adjustDrawingView();
			}
		});
	}
	public void refresh() {
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (!drawComp.isDisposed()) {
					adjustDrawingView();
					elementRedraw();
				}
			}
		});
	}
	// Set element info
	protected abstract void initElementAttribute();
	protected abstract void initialView();
	// Set element to display
	protected abstract void initialElement();
	// set update view details
	public abstract void update();
	public abstract void updateTitle();
	public abstract String getCurrentID();
	private void elementRedraw() {
		elementRedraw(0, sc.getOrigin().y, view_width, view_height, false);
	}
	private void elementRedraw(int x, int y, int width, int height, boolean reDrawChild) {
		if (!drawComp.isDisposed())
			drawComp.redraw(x, y, width, height, reDrawChild);
	}
	/**
	 * Paint Listener
	 */
	public void repaint(Object condition) {
		refresh();
	}

	private class SelectedAreaInfo {
		private List tmpElements = new ArrayList();
		private Rectangle boundedRect = new Rectangle(0, 0, 0, 0);

		public void addBoundedElement(IElement[] elements) {
			tmpElements.add(elements);
		}
		public IElement[] getBoundedElements() {
			if (tmpElements.size() == 0)
				return new IElement[0];
			return (IElement[]) tmpElements.remove(0);
		}
		public void removeAllBoundedElements() {
			tmpElements.clear();
			boundedRect = new Rectangle(0, 0, 0, 0);
		}
		public void setBoundedRect(Rectangle rect) {
			this.boundedRect = rect;
		}
		public Rectangle getBoundedRect() {
			return boundedRect;
		}
		public Rectangle getBoundedArea(Rectangle orgRect) {
			Rectangle rect = new Rectangle(orgRect.x, orgRect.y, orgRect.width, orgRect.height);
			rect.x -= e_width + e_spacing_x;
			rect.y -= e_height + e_spacing_y;
			return rect;
		}
		public boolean compareArea(Rectangle rect1, Rectangle rect2) {
			return (rect1.width * rect1.height) > (rect2.width * rect2.height);
		}
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
	private class SelectionInfo {
		private static final int SE = 1;
		private static final int SW = 2;
		private static final int NW = 3;
		private static final int NE = 4;
		public int direction = SE;
		public Rectangle rect = null;
		public int start_x = 0;
		public int start_y = 0;
		public int end_x = 0;
		public int end_y = 0;
		public int width = 0;
		public int height = 0;

		public SelectionInfo(int start_x, int start_y, int end_x, int end_y, int direction) {
			this.start_x = start_x;
			this.start_y = start_y;
			this.end_x = end_x;
			this.end_y = end_y;
			this.width = (end_x - start_x);
			this.height = (end_y - start_y);
			this.rect = new Rectangle(start_x, start_y, width, height);
			this.direction = direction;
		}
		public int getDirection() {
			return direction;
		}
		public Rectangle newRect() {
			return new Rectangle(start_x, start_y, width, height);
		}
	}
}
