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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.Accessible;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleControlAdapter;
import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Clement chu
 *
 */
public class IconCanvas extends Canvas {
	// default element info
	protected int e_offset_x = 5;
	protected int e_spacing_x = 4;
	protected int e_offset_y = 5;
	protected int e_spacing_y = 4;
	protected int e_width = 16;
	protected int e_height = 16;
	
	protected int sel_length = 2;
	protected int sel_gap = 2;
	protected int sel_size = 1;
	
	protected boolean updateCaretDirection = true;
	protected Cursor ibeamCursor = null;
	protected Hashtable keyActionMap = new Hashtable();
	protected Listener listener = null;
	
	protected int max_e_row = -1;
	protected int max_e_col = -1;
	protected int current_top_row = -1;
	protected boolean doSelection = false;
	protected Shell selectionShell = null;
	
	protected boolean mouseDown = false;
	protected boolean mouseDoubleClick = false;	// true=a double click ocurred. Don't do mouse swipe selection.
	protected boolean doubleClickEnabled = true;	// see getDoubleClickEnabled
	protected boolean continueSelection = false;
	protected boolean specialSelection = false;
	protected Point selection = null;
	protected Point doubleClickSelection = null;			// selection after last mouse double click
	protected Point movingSelection = null;
	
	protected int autoScrollDirection = SWT.NULL;	// the direction of autoscrolling (up, down, right, left)
	protected int autoScrollDistance = 0;
	protected int verticalScrollOffset = 0;		// pixel based
	protected Object input = null;
	
	protected Color background = null;
	protected Color foreground = null;
	
	final static boolean IS_CARBON, IS_GTK, IS_MOTIF;
	static {
		String platform = SWT.getPlatform();
		IS_CARBON = "carbon".equals(platform);
		IS_GTK = "gtk".equals(platform);
		IS_MOTIF = "motif".equals(platform);
	}
	
	public IconCanvas(Composite parent, int style) {
		super(parent, SWT.V_SCROLL | SWT.WRAP | SWT.MULTI | style);
		calculateVerticalScrollBar();
		createKeyBindings();
		//ibeamCursor = new Cursor(getDisplay(), SWT.CURSOR_IBEAM);
		setCursor(ibeamCursor);
		installListeners();
		initializeAccessible();
	}
	public void setInput(Object input) {
		checkWidget();
		if (input == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		this.input = input;
		resetInfo();
		calculateVerticalScrollBar();
	}
	public Object getInput() {
		return input;
	}
	//TODO
	public int[] getInputArray() {
		return ((TestObject)input).getArray();
	}
	public int getTotalElements() {
		//TODO
		return input==null?0:getInputArray().length;
	}
	public int getElementHeight() {
		return (e_spacing_y + e_height);
	}
	public int getElementWidth() {
		return (e_spacing_x + e_width);
	}
	public int getMaxCol() {
		if (max_e_col == -1) {
			int width = getClientArea().width - getVerticalBar().getSize().x;
			max_e_col = Math.max(0, (width - e_offset_x) / getElementWidth() + 1);
		}
		return max_e_col;
	}
	public int getMaxClientRow() {
		return (getClientArea().height - e_offset_y) / getElementHeight();
	}
	public int getMaxRow() {
		if (max_e_row == -1) {
			int total = getTotalElements();
			if (total == 0)
				return 0;
			
			int max_col = getMaxCol();
			if (max_col == 0)
				return 0;
			int max_row = total / max_col;
			max_e_row = total%max_col==0?max_row:max_row + 1;
		}
		return max_e_row;
	}
	public int getMaxHeight() {
		return getMaxRow() * getElementHeight();
	}
	protected int getVerticalIncrement() {
		return e_spacing_y + e_height;
	}
	public void setDoubleClickEnabled(boolean enable) {
		checkWidget();
		doubleClickEnabled = enable;
	}	
	public boolean getDoubleClickEnabled() {
		checkWidget();
		return doubleClickEnabled;
	}
	/*
	private void updateViewSize(boolean isVSrcoll) {
		Rectangle clientArea = getClientArea();
		view_width = isVSrcoll ? (clientArea.width - getVerticalBar().getSize().x) : clientArea.width;
		view_height = clientArea.height;
		visible_e_col = (view_width - e_offset_x) / (e_width + e_spacing_x);
		visible_e_row = (view_height - e_offset_y) / (e_height + e_spacing_y) + 1;
	}
	private int calculateHeight() {
		if (visible_e_col > 0) {
			max_e_row = (totalIcons / visible_e_col) + ((totalIcons % visible_e_col == 0) ? 0 : 1);
			int max_height = max_e_row * (e_height + e_spacing_y) + e_offset_y;
			return (max_height < view_height ? view_height : max_height);
		}
		return view_height;
	}
	public void refreshViewSize() {
		updateViewSize(false);
		int new_height = calculateHeight();
		if (new_height > view_height) {// display v_scroll
			updateViewSize(true);
			new_height = calculateHeight();
			ScrollBar verticalBar = getVerticalBar();
			setVerticalScrollBar(verticalBar);
			if (verticalBar != null) {
				verticalBar.setIncrement(getVerticalIncrement());
			}
		}
	}
	*/
	protected void calculateVerticalScrollBar() {
		setVerticalScrollBar(getVerticalBar());
	}
	protected void setVerticalScrollBar(ScrollBar verticalBar) {
		if (verticalBar != null) {
			Rectangle clientArea = getClientArea();
			final int INACTIVE = 1;
			
			int max_height = getMaxHeight() + e_offset_y + 1;
			if (clientArea.height < max_height) {
				verticalBar.setValues(verticalBar.getSelection(), verticalBar.getMinimum(), max_height, clientArea.height, getVerticalIncrement(), clientArea.height);
			}
			else if (verticalBar.getThumb() != INACTIVE || verticalBar.getMaximum() != INACTIVE) {
				verticalBar.setValues(verticalBar.getSelection(), verticalBar.getMinimum(), INACTIVE, INACTIVE, getVerticalIncrement(), INACTIVE);
			}		
		}
	}
	protected boolean claimBottomFreeSpace() {
		int newVerticalOffset = Math.max(0, getMaxHeight() - getClientArea().height);
		if (newVerticalOffset < verticalScrollOffset) {
			return setVerticalScrollOffset(newVerticalOffset, true);
		}
		return false;
	}
	public Color getBackground() {
		checkWidget();
		if (background == null) {
			return getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		}
		return background;
	}
	public void setBackground(Color color) {
		checkWidget();
		background = color;
		super.setBackground(getBackground());
		redraw();
	}
	public Color getForeground() {
		checkWidget();
		if (foreground == null) {
			return getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND);
		}
		return foreground;
	}
	public void setForeground(Color color) {
		checkWidget();
		foreground = color;
		super.setForeground(getForeground());
		redraw();
	}
	
	protected int getCurrentTopRow() {
		return current_top_row;
		//return (verticalScrollOffset / (e_height + e_spacing_y));
	}
	
	public int getKeyBinding(int key) {
		checkWidget();
		Integer action = (Integer) keyActionMap.get(new Integer(key));
		int intAction;
		
		if (action == null) {
			intAction = SWT.NULL;
		}
		else {
			intAction = action.intValue();
		}
		return intAction;
	}	
	public void setKeyBinding(int key, int action) {
		checkWidget(); 
		
		int keyValue = key & SWT.KEY_MASK;
		int modifierValue = key & SWT.MODIFIER_MASK;
		char keyChar = (char)keyValue;
		if (Character.isLetter(keyChar)) {
			char ch = Character.toUpperCase(keyChar);
			int newKey = ch | modifierValue;
			if (action == SWT.NULL) {
				keyActionMap.remove(new Integer(newKey));
			}
			else {
			 	keyActionMap.put(new Integer(newKey), new Integer(action));
			}
			ch = Character.toLowerCase(keyChar);
			newKey = ch | modifierValue;
			if (action == SWT.NULL) {
				keyActionMap.remove(new Integer(newKey));
			}
			else {
			 	keyActionMap.put(new Integer(newKey), new Integer(action));
			}
		} else {
			if (action == SWT.NULL) {
				keyActionMap.remove(new Integer(key));
			}
			else {
			 	keyActionMap.put(new Integer(key), new Integer(action));
			}
		}
	}
	protected void createKeyBindings() {
		// Navigation
		setKeyBinding(SWT.ARROW_UP, ST.LINE_UP);	
		setKeyBinding(SWT.ARROW_DOWN, ST.LINE_DOWN);
		setKeyBinding(SWT.HOME, ST.LINE_START);
		setKeyBinding(SWT.END, ST.LINE_END);
		setKeyBinding(SWT.PAGE_UP, ST.PAGE_UP);
		setKeyBinding(SWT.PAGE_DOWN, ST.PAGE_DOWN);
		
		// Selection
		setKeyBinding(SWT.ARROW_UP | SWT.MOD2, ST.SELECT_LINE_UP);	
		setKeyBinding(SWT.ARROW_DOWN | SWT.MOD2, ST.SELECT_LINE_DOWN);
		setKeyBinding(SWT.HOME | SWT.MOD2, ST.SELECT_LINE_START);
		setKeyBinding(SWT.END | SWT.MOD2, ST.SELECT_LINE_END);
		setKeyBinding(SWT.PAGE_UP | SWT.MOD2, ST.SELECT_PAGE_UP);
		setKeyBinding(SWT.PAGE_DOWN | SWT.MOD2, ST.SELECT_PAGE_DOWN);
		setKeyBinding('A' | SWT.MOD1, ST.SELECT_ALL);
		
		// Modification
		// Cut, Copy, Paste, delete
		setKeyBinding('X' | SWT.MOD1, ST.CUT);
		setKeyBinding('C' | SWT.MOD1, ST.COPY);
		setKeyBinding('V' | SWT.MOD1, ST.PASTE);
		setKeyBinding(SWT.BS, ST.DELETE_PREVIOUS);
		setKeyBinding(SWT.DEL, ST.DELETE_NEXT);
	}
	public void invokeAction(int action, Event event) {
		checkWidget();
		updateCaretDirection = true;
		switch (action) {
			// Navigation
			case ST.LINE_UP:
				System.out.println("++++ line up");
				doPageUp(1);
				clearSelection(true);
				break;
			case ST.LINE_DOWN:
				System.out.println("++++ line down");
				doPageDown(1);
				clearSelection(true);
				break;
			case ST.LINE_START:
				System.out.println("++++ line start");
				//doLineStart();
				clearSelection(true);
				break;
			case ST.LINE_END:
				System.out.println("++++ line end");
				//doLineEnd();
				clearSelection(true);
				break;
			case ST.PAGE_UP:
				System.out.println("++++ page up");
				doPageUp(getMaxClientRow());
				clearSelection(true);
				break;
			case ST.PAGE_DOWN:
				System.out.println("++++ page down");
				doPageDown(getMaxClientRow());
				clearSelection(true);
				break;
			// Selection	
			case ST.SELECT_LINE_UP:
				System.out.println("++++ page line up");
				//doSelectionLineUp();
				break;
			case ST.SELECT_ALL:
				System.out.println("++++ select all");
				//selectAll();
				break;
			case ST.SELECT_LINE_DOWN:
				System.out.println("++++ select line down");
				//doSelectionLineDown();
				break;
			case ST.SELECT_LINE_START:
				System.out.println("++++ select line start");
				//doLineStart();
				//doSelection(ST.COLUMN_PREVIOUS);
				break;
			case ST.SELECT_LINE_END:
				System.out.println("++++ select line end");
				//doLineEnd();
				//doSelection(ST.COLUMN_NEXT);
				break;
			case ST.SELECT_COLUMN_PREVIOUS:
				System.out.println("++++ select column previous");
				//doSelectionCursorPrevious();
				//doSelection(ST.COLUMN_PREVIOUS);
				break;
			case ST.SELECT_COLUMN_NEXT:
				System.out.println("++++ select column next");
				//doSelectionCursorNext();
				//doSelection(ST.COLUMN_NEXT);
				break;
			case ST.SELECT_PAGE_UP:
				System.out.println("++++ select page up");
				doSelectionPageUp(getMaxClientRow());
				break;
			case ST.SELECT_PAGE_DOWN:
				System.out.println("++++ select page down");
				doSelectionPageDown(getMaxClientRow());
				break;
			// Modification			
			case ST.CUT:
				System.out.println("++++ select cut");
				//cut();
				break;
			case ST.COPY:
				System.out.println("++++ select copy");
				//copy();
				break;
			case ST.PASTE:
				System.out.println("++++ select paste");
				//paste();
				break;
			case ST.DELETE_PREVIOUS:
				System.out.println("++++ select delete previous");
				//doDelete();
				break;
			case ST.DELETE_NEXT:
				System.out.println("++++ select delete next");
				//doDelete();
				break;
			default:
				System.out.println("++++ unknown action: " + action);
				break;
		}
	}	
	protected void initializeAccessible() {
		final Accessible accessible = getAccessible();
		accessible.addAccessibleListener(new AccessibleAdapter() {
			public void getHelp(AccessibleEvent e) {
				e.result = getToolTipText();
			}
		});
		accessible.addAccessibleControlListener(new AccessibleControlAdapter() {
			public void getRole(AccessibleControlEvent e) {
				e.detail = ACC.ROLE_TEXT;
			}
			public void getState(AccessibleControlEvent e) {
				int state = 0;
				if (isEnabled()) state |= ACC.STATE_FOCUSABLE;
				if (isFocusControl()) state |= ACC.STATE_FOCUSED;
				if (isVisible() == false) state |= ACC.STATE_INVISIBLE;
				e.detail = state;
			}
		});		
		addListener(SWT.FocusIn, new Listener() {
			public void handleEvent(Event event) {
				accessible.setFocus(ACC.CHILDID_SELF);
			}
		});
	}	
	protected void installListeners() {
		ScrollBar verticalBar = getVerticalBar();
		
		listener = new Listener() {
			public void handleEvent(Event event) {
				switch (event.type) {
					case SWT.Dispose: handleDispose(event); break;
					case SWT.KeyDown: handleKeyDown(event); break;
					case SWT.KeyUp: handleKeyUp(event); break;
					case SWT.MouseDown: handleMouseDown(event); break;
					case SWT.MouseUp: handleMouseUp(event); break;
					case SWT.MouseDoubleClick: handleMouseDoubleClick(event); break;
					case SWT.MouseMove: handleMouseMove(event); break;
					case SWT.Paint: handlePaint(event); break;
					case SWT.Resize: handleResize(event); break;
				}
			}		
		};
		addListener(SWT.Dispose, listener);
		addListener(SWT.KeyDown, listener);
		addListener(SWT.KeyUp, listener);
		addListener(SWT.MouseDown, listener);
		addListener(SWT.MouseUp, listener);
		addListener(SWT.MouseDoubleClick, listener);
		addListener(SWT.MouseMove, listener);
		addListener(SWT.Paint, listener);
		addListener(SWT.Resize, listener);
		if (verticalBar != null) {
			verticalBar.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					setVerticalScrollOffset(getVerticalBar().getSelection(), false);
					redraw();
				}
			});
		}
	}
	protected void handleKey(Event event) {
		int action;
		if (event.keyCode != 0) {
			action = getKeyBinding(event.keyCode | event.stateMask);
		}
		else {
			action = getKeyBinding(event.character | event.stateMask);
			if (action == SWT.NULL) { 
				if ((event.stateMask & SWT.CTRL) != 0 && (event.character >= 0) && event.character <= 31) {
					int c = event.character + 64;
					action = getKeyBinding(c | event.stateMask);
				}
			}
		}
		if (action != SWT.NULL) {
			invokeAction(action, event);		
		}
	}

	protected void doAutoScroll(int loc_y) {
		if (loc_y > getClientArea().height) {
			doAutoScroll(SWT.DOWN, getElementHeight());
		}
		else  if (loc_y < 0) {
			doAutoScroll(SWT.UP, -getElementHeight());
		}
		else {
			endAutoScroll();
		}
	}
	protected void doAutoScroll(int direction, final int distance) {
		Runnable timer = null;
		final int TIMER_INTERVAL = 50;
		
		//If we're already autoscrolling in the given direction do nothing
		if (autoScrollDirection == direction) {
			return;
		}
		
		final Display display = getDisplay();
		if (direction == SWT.UP) {
			timer = new Runnable() {
				public void run() {
					if (autoScrollDirection == SWT.UP) {
						doSelectionPageUp(distance);
						display.timerExec(TIMER_INTERVAL, this);
					}
				}
			};
		} else if (direction == SWT.DOWN) {
			timer = new Runnable() {
				public void run() {
					if (autoScrollDirection == SWT.DOWN) {
						doSelectionPageDown(distance);
						display.timerExec(TIMER_INTERVAL, this);
					}
				}
			};
		}
		if (timer != null) {
			autoScrollDirection = direction;
			display.timerExec(TIMER_INTERVAL, timer);
		}
	}
	protected void doSelectionPageUp(int distance) {
		doPageUp(distance / getElementHeight());
	}
	protected void doSelectionPageDown(int distance) {
		doPageDown(distance / getElementHeight());
	}
	protected void doPageUp(int rows) {
		if (current_top_row >= 0 && verticalScrollOffset > 0) {	
			int scrollRows = Math.max(1, Math.min(current_top_row, rows));
			
			current_top_row -= scrollRows;
			if (current_top_row < 0)
				current_top_row = 0;
			
			// scroll one page up or to the top
			int scrollOffset = Math.max(0, verticalScrollOffset - scrollRows * getVerticalIncrement());
			if (scrollOffset < verticalScrollOffset) {
				setVerticalScrollOffset(scrollOffset, true);
			}
		}
	}
	protected void doPageDown(int rows) {
		int max_row = getMaxRow();
		int max_client_row = getMaxClientRow();
		if (current_top_row < max_row - max_client_row) {
			int scrollRows = Math.max(1, Math.min(max_row - current_top_row - max_client_row, rows));
			
			current_top_row += scrollRows;
			// scroll one page down or to the bottom
			int scrollOffset = verticalScrollOffset + scrollRows * getVerticalIncrement();
			if (scrollOffset > verticalScrollOffset) {		
				setVerticalScrollOffset(scrollOffset, true);
			}
		}
	}
	protected boolean setVerticalScrollOffset(int pixelOffset, boolean adjustScrollBar) {
		if (pixelOffset == verticalScrollOffset) {
			return false;
		}
		ScrollBar verticalBar = getVerticalBar();
		if (verticalBar != null && adjustScrollBar) {
			verticalBar.setSelection(pixelOffset);
		}
		Rectangle clientArea = getClientArea();
		scroll(0, 0, 0, pixelOffset - verticalScrollOffset, clientArea.width, clientArea.height, true);

		verticalScrollOffset = pixelOffset;
		calculateTopIndex();
		return true;
	}
	protected void endAutoScroll() {
		autoScrollDirection = SWT.NULL;
	}
	
	protected void calculateTopIndex() {
		int verticalIncrement = getVerticalIncrement();
		if (verticalIncrement == 0) {
			return;
		}
		current_top_row = verticalScrollOffset / verticalIncrement;
		if (current_top_row > 0) {
			int max_row = getMaxRow();
			int clientAreaHeight = getClientArea().height;
			if (clientAreaHeight > 0) {
				int fullRowTopPixel = current_top_row * verticalIncrement;
				if (verticalScrollOffset < fullRowTopPixel) {
					current_top_row--;
				}
			}
			else if (current_top_row >= max_row) {
				current_top_row = max_row - 1;
			}
		}
	}
	void clearSelection(boolean sendEvent) {
	}	
	
	protected int getSelectedCol(int x_loc, boolean isApproximate) {
		int col = Math.min(Math.abs(x_loc + e_spacing_x) / getElementWidth(), getMaxCol()-1);
		if (isApproximate)
			return col;
		int test_loc_x = getElementWidth() * col;
		if (x_loc > test_loc_x && x_loc < (test_loc_x + e_width)) {
			return col;
		}
		return -1;
	}
	protected int getSelectedRow(int y_loc, boolean isApproximate) {
		int row = Math.max(0, (Math.abs(verticalScrollOffset + y_loc + e_spacing_y) / getElementHeight()));
		if (isApproximate)
			return row;
		int test_loc_y = (getElementHeight() * row) - verticalScrollOffset;
		if (y_loc > test_loc_y && y_loc < (test_loc_y + e_height)) {
			return row;
		}
		return -1;
	}
	protected int findSelectionIndex(int row, int col) {
		return row * getMaxCol() + col;
	}
	protected int findSelectedIndex(int loc_x, int loc_y, boolean isApproximate) {
		if (selection != null) {
			int col = getSelectedCol(loc_x - e_offset_x, isApproximate);
			if (col > -1) {
				int row = getSelectedRow(loc_y - e_offset_y, isApproximate);
				if (row > -1) {
					int index = findSelectionIndex(row, col);
					if (index < getTotalElements())
						return index;
				}
			}
		}
		return -1;
	}
	protected Integer[] selectElements() {
		int col_start = getSelectedCol(selection.x - e_offset_x, true);
		int col_end = getSelectedCol(movingSelection.x - e_offset_x, true);
		int row_start = getSelectedRow(selection.y - e_offset_y, true);
		int row_end = getSelectedRow(movingSelection.y - e_offset_y, true);

		List selectedElements = new ArrayList();
		int total = getTotalElements();
		for (int row_count=row_start; row_count<row_end; row_count++) {
			for (int col_count=col_start; col_count<col_end; col_count++) {
				int index = findSelectionIndex(row_count, col_count);
				if (index < total)
					selectedElements.add(new Integer(index));
			}			
		}
		return (Integer[])selectedElements.toArray(new Integer[selectedElements.size()]);
	}
	protected Shell getSelectionShell() {
		if (selectionShell == null) {
			selectionShell = new Shell(getShell(), SWT.NO_TRIM | SWT.ON_TOP | SWT.NO_BACKGROUND);
			selectionShell.setSize(0, 0);
			selectionShell.setVisible(true);
			selectionShell.setBackground(selectionShell.getDisplay().getSystemColor(SWT.COLOR_DARK_RED));
		}
		return selectionShell;
	}
	protected void disposeSelectionShell() {
		if (selectionShell != null && !selectionShell.isDisposed()) {
			selectionShell.dispose();
			selectionShell = null;
		}
	}
	
	protected void doMouseSelection(Event event) {
		Shell s_shell = getSelectionShell();
		Composite comp = new Composite(selectionShell, SWT.NO_BACKGROUND);
		GC gc = new GC(comp);
		if (movingSelection == null) {
			//GC gc = getGC(SWT.LEFT_TO_RIGHT);
			gc.setLineWidth(sel_size);
			gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
			gc.drawLine(selection.x, selection.y, selection.x+sel_length, selection.y);
			gc.drawLine(selection.x, selection.y, selection.x, selection.y+sel_length);
			gc.dispose();
			movingSelection = new Point(selection.x+(sel_length*sel_gap), selection.y+(sel_length*sel_gap));
			return;
		}
		if (event.x <= movingSelection.x) {
			if (event.x <= movingSelection.x - (sel_length*sel_gap)) {
				//redraw(event.x, selection.y, movingSelection.x-event.x+(sel_length*sel_gap), movingSelection.y-selection.y+(sel_length*sel_gap), false);
				movingSelection.x = (int)Math.floor(event.x / (sel_length*sel_gap)) * (sel_length*sel_gap);
			}
		}
		if (event.y <= movingSelection.y) {
			if (event.y <= movingSelection.y - (sel_length*sel_gap)) {
				//redraw(selection.x, event.y, movingSelection.x-selection.x+(sel_length*sel_gap), movingSelection.y-event.y+(sel_length*sel_gap), false);
				movingSelection.y = (int)Math.floor(event.y / (sel_length*sel_gap)) * (sel_length*sel_gap);
			}
		}
		//GC gc = getGC(SWT.LEFT_TO_RIGHT);
		gc.setAlpha(50);
		gc.setForeground(getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
		gc.setLineWidth(sel_size);

		drawMouseSelectionTopLeft(gc, event.x, event.y);
		drawMouseSelectionBottomRight(gc);
		gc.dispose();
		s_shell.setBounds(selection.x, selection.y, movingSelection.x-selection.x, movingSelection.y-selection.y);
		//redraw(selection.x+sel_size, selection.y+sel_size, Math.abs(movingSelection.x-selection.x-sel_size), Math.abs(movingSelection.y-selection.y-sel_size), false);
	}
	
	protected void drawMouseSelectionTopLeft(GC gc, int e_x, int e_y) {
		int start_x = movingSelection.x;
		int start_y = movingSelection.y;
		while (start_x < e_x) {
			gc.drawLine(start_x, selection.y, start_x+sel_length, selection.y);
			start_x += (sel_length*sel_gap);
		}
		while (start_y < e_y) {
			gc.drawLine(selection.x, start_y, selection.x, start_y+sel_length);
			start_y += (sel_length*sel_gap);
		}
		movingSelection.x = start_x;
		movingSelection.y = start_y;
	}
	protected void drawMouseSelectionBottomRight(GC gc) {
		int end_x = movingSelection.x;
		int end_y = movingSelection.y;
		while (end_x > selection.x) {
			System.out.println("end_x: " + end_x);
			gc.drawLine(end_x-sel_length, movingSelection.y, end_x, movingSelection.y);
			end_x -= (sel_length*sel_gap);
		}
		while (end_y > selection.y) {
			gc.drawLine(movingSelection.x, end_y-sel_length, movingSelection.x, end_y);
			end_y -= (sel_length*sel_gap);
		}
	}
	protected void performPaint(GC gc, int col_start, int col_end, int row_start, int row_end, int render_x, int render_y, int renderWidth, int renderHeight)	{
		Image imageBuffer = new Image(getDisplay(), renderWidth, renderHeight);
		GC newGC = new GC(imageBuffer, SWT.LEFT_TO_RIGHT);
		Image image = null;
		try {
			URL url = new File("D:/eclipse3.1/workspace/org.eclipse.ptp.ui/icons/node/node_user_excl.gif").toURL();
			image = ImageDescriptor.createFromURL(url).createImage();
		} catch (Exception e) {
			System.out.println("+++++ err: " + e.getMessage());
		}

		if (doSelection)
			newGC.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

		int total = getTotalElements();		
		for (int row_count=row_start; row_count<row_end; row_count++) {
			for (int col_count=col_start; col_count<col_end; col_count++) {
				int index = findSelectionIndex(row_count, col_count);
				if (index < total) {
					int x_loc = e_offset_x + ((col_count) * getElementWidth());
					int y_loc = e_offset_y + ((row_count) * getElementHeight()) - verticalScrollOffset;
					if (image != null && !doSelection) {
						newGC.drawImage(image, x_loc, y_loc);
					}
					else {
						newGC.fillRectangle(x_loc, y_loc, e_width, e_height);
					}
				}
			}			
		}
		gc.drawImage(imageBuffer, render_x, render_y);
		newGC.dispose();
		imageBuffer.dispose();
		clearMargin(gc, getBackground());
	}
	/*
	protected void performPaint(GC gc, int start_num, int end_num, int max_col, int render_x, int render_y, int renderWidth, int renderHeight)	{
		Image imageBuffer = new Image(getDisplay(), renderWidth, renderHeight);
		GC newGC = new GC(imageBuffer, SWT.LEFT_TO_RIGHT);
		Image image = null;
		try {
			URL url = new File("D:/eclipse3.1/workspace/org.eclipse.ptp.ui/icons/node/node_user_excl.gif").toURL();
			image = ImageDescriptor.createFromURL(url).createImage();
		} catch (Exception e) {
			System.out.println("+++++ err: " + e.getMessage());
		}
		for (int i=start_num; i<end_num; i++) {
			int index = i+1;
			int x = (max_col > index) ? index : (index % max_col);
			x = (x == 0) ? max_col : x;
			int x_loc = e_offset_x + ((x - 1) * (e_width + e_spacing_x));
			
			int y = ((x == max_col) ? index / max_col : (max_col > index) ? 1 : ((index / max_col) + 1));
			int y_loc = e_offset_y + ((y - 1) * getElementHeight()) - verticalScrollOffset;
			
			if (image != null) {
				newGC.drawImage(image, x_loc, y_loc);
			}
			//newGC.fillRectangle(x_loc, y_loc, e_width, e_height);
			//newGC.drawString(""+i, x_loc, y_loc);
			//System.out.println("** " + index);
		}
		gc.drawImage(imageBuffer, render_x, render_y);
		newGC.dispose();
		imageBuffer.dispose();
		clearMargin(gc, getBackground());
	}
	*/

	protected void clearMargin(GC gc, Color background) {
		// clear the margin background
		gc.setBackground(background);
	}
	protected GC getGC() {
		return getGC(SWT.NONE);
	}
	protected GC getGC(int style) {
		return new GC(this, style);
	}
	protected void resetInfo() {
		mouseDown = false;
		mouseDoubleClick = false;
		continueSelection = false;
		specialSelection = false;
		selection = null;
		doubleClickSelection = null;
		movingSelection = null;
		doSelection = false;
		max_e_row = -1;
		max_e_col = -1;
		disposeSelectionShell();
	}
	protected void handleDispose(Event event) {
		removeListener(SWT.Dispose, listener);
		event.type = SWT.None;

		//ibeamCursor.dispose();
		ibeamCursor = null;
		keyActionMap = null;
		input = null;
		resetInfo();
	}
	protected void handleKeyDown(Event event) {
		if (event.doit == true) {
			handleKey(event);
		}
	}
	protected void handleKeyUp(Event event) {
		resetInfo();
	}
	protected void handleMouseDown(Event event) {
		mouseDown = true;
		mouseDoubleClick = false;
		if ((event.button != 1) || (IS_CARBON && (event.stateMask & SWT.MOD4) != 0)) {
			return;	
		}
		specialSelection = (event.stateMask & SWT.MOD1)!=0;
		continueSelection = (event.stateMask & SWT.MOD2)!=0;
		
		if (continueSelection && specialSelection) {
			continueSelection = false;
			specialSelection = false;
		}
		
		selection = new Point(event.x, event.y);
		int selectedIndex = findSelectedIndex(selection.x, selection.y, false);
		System.out.println("---- index: " + selectedIndex);
	}
	protected void handleMouseUp(Event event) {
		if (movingSelection != null)
			redraw();
		
		resetInfo();
		endAutoScroll();
	}
	protected void handleMouseDoubleClick(Event event) {
		if (event.button != 1 || doubleClickEnabled == false) {
			return;
		}
		mouseDoubleClick = true;
		selection = null;
		doubleClickSelection = new Point(event.x, event.y);
		//TODO: double click action
	}
	protected void handleMouseMove(Event event) {
		if (!mouseDown) return;
		if ((event.stateMask & SWT.BUTTON1) == 0) {
			return;
		}
		update();
		if (selection != null) {
			doMouseSelection(event);
		}
		doAutoScroll(event.y);
	}	
	protected void handlePaint(Event event) {
		Rectangle clientArea = getClientArea();
		if (event.height == 0 || (clientArea.width == 0 && clientArea.height == 0)) {
			// Check if there is work to do
			return;
		}
		//default
		int renderWidth = clientArea.width;
		int renderHeight = clientArea.height;

		int col_start = 0;
		int col_end = getMaxCol();
		int row_start = 0;
		int row_end = getMaxClientRow();
		doSelection = false;
		if (selection != null && movingSelection != null) {
			col_start = Math.max(0, getSelectedCol(selection.x - e_offset_x, true));
			col_end = Math.min(getSelectedCol(event.x + event.width - e_offset_x, true) + 1, getMaxCol());
			row_start = Math.max(0, getSelectedRow(selection.y - e_offset_y, true));
			row_end = Math.min(getSelectedRow(event.y + event.height - e_offset_y, true) + 1, getMaxRow());
			renderWidth = selection.x + event.x + event.width;
			renderHeight = selection.y + event.y + event.height;
			//doSelection = true;
		}
		else {
			row_start = Math.max(0, getSelectedRow(0 - e_offset_y, true));
			row_end = Math.min(row_end + row_start + 1, getMaxRow()); 
		}
		
		System.out.println(col_start + "," + col_end + " - " + row_start + "," + row_end);
		performPaint(event.gc, col_start, col_end, row_start, row_end, 0, 0, renderWidth, renderHeight);
		/*
		int max_col = getMaxCol();
		int start_num = findSelectedIndex(event.x, event.y, true);
		if (movingSelection == null || start_num < 0) {
			//do nothing if event over the available col
			if (event.x > 0) {
				event.gc.setBackground(getBackground());
				event.gc.fillRectangle(event.x, event.y, event.width, event.height);
				return;
			}
			start_num = current_top_row * max_col;
		} else {
			renderWidth = selection.x + event.x + event.width;
		}
		int total_Elements = getTotalElements();
		int end_num = 0;
		if (movingSelection != null) {
			end_num = Math.min(total_Elements, findSelectedIndex(movingSelection.x, movingSelection.y, true) + (2 * max_col));
			renderHeight = selection.y + event.y + event.height;
		}
		else {
			end_num = Math.min(total_Elements, (current_top_row + getMaxClientRow() + 1) * max_col);
		}
		//System.out.println(event.x +", " + event.y + " -- " + event.width + ", " + event.height);
		performPaint(event.gc, start_num, end_num, max_col, 0, 0, renderWidth, renderHeight);
		*/
	}	
	protected void handleResize(Event event) {
		resetInfo();
		setVerticalScrollBar(getVerticalBar());
		if (!claimBottomFreeSpace())
			calculateTopIndex();
	}
	
	public TestObject createTestObject() {
		return new TestObject();
	}
	
	private class TestObject {
		int[] array = new int[10000];
		public TestObject() {
			for (int i=0; i<array.length; i++)
				array[i] = i;
		}
		public int[] getArray() {
			return array;
		}
	}
	public static void main(String[] args) {
        final Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        shell.setSize(400, 400);
        shell.setLocation(0, 0);
        IconCanvas test = new IconCanvas(shell, SWT.NONE);
        test.setInput(test.createTestObject());
        
        shell.open();
        while (!shell.isDisposed()) {
        	if (!display.readAndDispatch())
        		display.sleep();
       	}
	}
}
