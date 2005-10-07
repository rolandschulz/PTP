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
import java.util.BitSet;
import java.util.Hashtable;
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
	public final static int SE = 1;
	public final static int SW = 2;
	public final static int NW = 3;
	public final static int NE = 4;
	
	protected BitSet selectedElements = new BitSet();
	
	// default element info
	protected int e_offset_x = 5;
	protected int e_spacing_x = 4;
	protected int e_offset_y = 5;
	protected int e_spacing_y = 4;
	protected int e_width = 16;
	protected int e_height = 16;
	
	//selection
	protected final int sel_length = 2;
	protected final int sel_gap = 2;
	protected int sel_size = 1;
	protected Color sel_color = null;
	
	protected Cursor ibeamCursor = null;
	protected Hashtable keyActionMap = new Hashtable();
	protected Listener listener = null;
	
	protected int max_e_row = -1;
	protected int max_e_col = -1;
	protected int current_top_row = -1;
	
	protected boolean mouseDown = false;
	protected boolean mouseDoubleClick = false;
	protected boolean continueSelection = false;
	protected boolean specialSelection = false;
	protected Point selection = null;
	protected Point doubleClickSelection = null;
	protected Point movingSelectionStart = null;
	protected Point movingSelectionEnd = null;
	protected int actualScrollStart_y = 0;
	
	protected int autoScrollDirection = SWT.NULL;	// the direction of autoscrolling (up, down, right, left)
	protected int verticalScrollOffset = 0;
	protected BitSet input = null;
	
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
		Display display = getDisplay();
		calculateVerticalScrollBar();
		createKeyBindings();
		//ibeamCursor = new Cursor(display, SWT.CURSOR_IBEAM);
		setCursor(ibeamCursor);
		installListeners();
		initializeAccessible();
		sel_color = display.getSystemColor(SWT.COLOR_BLUE);
	}
	public void setInput(BitSet input) {
		checkWidget();
		if (input == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		this.input = input;
		resetInfo();
		calculateVerticalScrollBar();
	}
	public BitSet getInput() {
		return input;
	}
	public void setSelectionSize(int size) {
		this.sel_size = size;
	}
	public void setSelectionColor(int colorIndex) {
		checkWidget();
		this.sel_color = getDisplay().getSystemColor(colorIndex);
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
	public int getTotalElements() {
		return input==null?0:input.cardinality();
	}

	protected int getElementHeight() {
		return (e_spacing_y + e_height);
	}
	protected int getElementWidth() {
		return (e_spacing_x + e_width);
	}
	protected int getMaxCol() {
		if (max_e_col == -1) {
			int width = getClientArea().width - getVerticalBar().getSize().x;
			max_e_col = Math.max(0, (width - e_offset_x) / getElementWidth() + 1);
		}
		return max_e_col;
	}
	protected int getMaxClientRow() {
		return (getClientArea().height - e_offset_y) / getElementHeight();
	}
	protected int getMaxRow() {
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
	protected int getMaxHeight() {
		return getMaxRow() * getElementHeight();
	}
	protected int getVerticalIncrement() {
		return e_spacing_y + e_height;
	}
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

	protected int getCurrentTopRow() {
		return current_top_row;
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
					setVerticalScrollOffset(getVerticalBar().getSelection(), true);
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
	protected boolean isReachTop() {
		return (verticalScrollOffset==0);
	}
	protected boolean isReachBottom() {
		return ((getMaxRow()-getMaxClientRow())* getElementHeight()==verticalScrollOffset);
	}
	protected void doAutoScroll(int y_loc) {
		if (y_loc > getClientArea().height && !isReachBottom()) {
			doAutoScroll(SWT.DOWN, getElementHeight());
		}
		else  if (y_loc < 0 && !isReachTop()) {
			doAutoScroll(SWT.UP, -getElementHeight());
		}
		else {
			endAutoScroll();
		}
	}
	protected void doAutoScroll(int direction, final int distance) {
		//If we're already autoscrolling in the given direction do nothing
		if (autoScrollDirection == direction) {
			return;
		}
		Runnable timer = null;
		final int TIMER_INTERVAL = 50;
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
			int scrollOffset = Math.min(verticalScrollOffset + scrollRows * getVerticalIncrement(), (getMaxRow()-getMaxClientRow())* getElementHeight());
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
		scroll(0, 0, 0, pixelOffset - verticalScrollOffset, clientArea.width, clientArea.height, adjustScrollBar);
		
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
		int col = Math.min(x_loc / getElementWidth(), getMaxCol()-1);
		if (isApproximate)
			return col;
		int test_loc_x = getElementWidth() * col;
		if (x_loc > test_loc_x && x_loc < (test_loc_x + e_width)) {
			return col;
		}
		return -1;
	}
	protected int getSelectedRow(int y_loc, boolean isApproximate) {
		int row = Math.max(0, (verticalScrollOffset + y_loc) / getElementHeight());
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
	protected int calculateMovingSelection(int e_x, int e_y) {
		int direction = SE;
		if (movingSelectionStart.x > e_x) {
			direction = SW;
		}
		if (movingSelectionStart.y > e_y) {
			if (direction == SW)
				direction = NW;
			else
				direction = NE;
		}
		return direction;
	}
	protected void selectElements(int col_start, int col_end, int row_start, int row_end) {
		selectedElements.clear();
		int total = getTotalElements();
		for (int row_count=row_start; row_count<row_end; row_count++) {
			for (int col_count=col_start; col_count<col_end; col_count++) {
				int index = findSelectionIndex(row_count, col_count);
				if (index < total) {
					selectedElements.set(index);
				}
			}
		}
		//System.out.println("---size: " + selectedElements.cardinality());
	}
	protected void doMouseSelection(int mouse_x, int mouse_y) {
		Rectangle clientArea = getClientArea();
		int end_x = Math.min(getMaxCol()*getElementWidth(), Math.max(0+sel_size, mouse_x));
		int end_y = Math.min(clientArea.height-e_spacing_y-sel_size, Math.max(0+sel_size, mouse_y));
		
		int direction = calculateMovingSelection(end_x, end_y);
		int s_x = movingSelectionStart.x;
		int e_x = end_x;
		int s_y = movingSelectionStart.y;
		int e_y = end_y;
		
		switch(direction) {
		case SW:
			s_x = end_x;
			e_x = movingSelectionStart.x;
			break;
		case NW:
			s_x = end_x;
			e_x = movingSelectionStart.x;
			s_y = end_y;
			e_y = movingSelectionStart.y;
			break;
		case NE:
			s_y = end_y;
			e_y = movingSelectionStart.y;
			break;
		}
		
		int col_start = getSelectedCol(s_x, true);
		int col_end = Math.min(getSelectedCol(e_x-e_offset_x, true)+1, getMaxCol());
		int row_start = getSelectedRow(s_y, true);
		int row_end = Math.min(getSelectedRow(e_y-e_offset_y, true)+1, getMaxRow());

		selectElements(col_start, col_end, row_start, row_end);
		if ((movingSelectionStart.y + verticalScrollOffset == actualScrollStart_y)) {
			if (movingSelectionEnd != null) {
				//redraw whole view if changed the direction
				if (calculateMovingSelection(movingSelectionEnd.x, movingSelectionEnd.y) != direction) {
					movingSelectionEnd = new Point(end_x, end_y);
					redraw();
					return;
				}
				switch(direction) {
				case SE:
					if (e_x < movingSelectionEnd.x) {
						col_end = Math.min(getSelectedCol(movingSelectionEnd.x-e_offset_x, true)+1, getMaxCol());
					}
					if (e_y < movingSelectionEnd.y) {
						row_end = Math.min(getSelectedRow(movingSelectionEnd.y-e_offset_y, true)+1, getMaxRow());
					}
					break;
				case SW:
					if (e_x > movingSelectionEnd.x) {
						col_start = getSelectedCol(movingSelectionEnd.x, true);
					}
					if (e_y < movingSelectionEnd.y) {
						row_end = Math.min(getSelectedRow(movingSelectionEnd.y-e_offset_y, true)+1, getMaxRow());
					}
					break;
				case NW:
					if (e_x > movingSelectionEnd.x) {
						col_start = getSelectedCol(movingSelectionEnd.x, true);
					}
					if (e_y > movingSelectionEnd.y) {
						row_start = getSelectedRow(movingSelectionEnd.y, true);
					}
					break;
				case NE:
					if (e_x < movingSelectionEnd.x) {
						col_end = Math.min(getSelectedCol(movingSelectionEnd.x-e_offset_x, true)+1, getMaxCol());
					}
					if (e_y > movingSelectionEnd.y) {
						row_start = getSelectedRow(movingSelectionEnd.y, true);
					}
					break;
				}
			}
			redraw(col_start, col_end, row_start, row_end, direction);
		}
		movingSelectionEnd = new Point(end_x, end_y);
	}
	protected void redraw(int col_start, int col_end, int row_start, int row_end, int direction) {
		int redrawStart_x = e_offset_x + (col_start * getElementWidth());
		int redrawEnd_x = e_offset_x + (col_end * getElementWidth());
		int redrawStart_y = e_offset_y + (row_start * getElementHeight()) - verticalScrollOffset;
		int redrawEnd_y = e_offset_y + (row_end * getElementHeight()) - verticalScrollOffset;
		
		switch(direction) {
		case SE:
			redraw(redrawStart_x-(e_spacing_x+sel_size), redrawStart_y-(e_spacing_y+sel_size), (redrawEnd_x-redrawStart_x)+(e_spacing_x+sel_size*2), (redrawEnd_y-redrawStart_y)+(e_spacing_y+sel_size*2), false);
			break;
		case SW:
			redraw(redrawStart_x-(e_spacing_x+sel_size*2), redrawStart_y-(e_spacing_y+sel_size*2), (redrawEnd_x-redrawStart_x)+(e_spacing_x+sel_size*4), (redrawEnd_y-redrawStart_y)+(e_spacing_y+sel_size*4), false);
			break;
		case NW:
			redraw(redrawStart_x-(e_spacing_x+sel_size*2), redrawStart_y-(e_spacing_y+sel_size*2), (redrawEnd_x-redrawStart_x)+(e_spacing_x+sel_size*4), (redrawEnd_y-redrawStart_y)+(e_spacing_y+sel_size*4), false);
			break;
		case NE:
			redraw(redrawStart_x-(e_spacing_x+sel_size), redrawStart_y-(e_spacing_y+sel_size), (redrawEnd_x-redrawStart_x)+(e_spacing_x+sel_size*2), (redrawEnd_y-redrawStart_y)+(e_spacing_y+sel_size*2), false);
			break;
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

		newGC.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

		int total = getTotalElements();		
		
		for (int row_count=row_start; row_count<row_end; row_count++) {
			for (int col_count=col_start; col_count<col_end; col_count++) {
				int index = findSelectionIndex(row_count, col_count);
				if (index < total) {
					int x_loc = e_offset_x + ((col_count) * getElementWidth());
					int y_loc = e_offset_y + ((row_count) * getElementHeight()) - verticalScrollOffset;
					BitSet newBitSet = new BitSet();
					newBitSet.set(index);
					if (selectedElements.intersects(newBitSet)) {
						newGC.fillRectangle(x_loc, y_loc, e_width, e_height);
					}
					else {
						newGC.drawImage(image, x_loc, y_loc);
					}
				}
			}			
		}
		if (movingSelectionStart != null && movingSelectionEnd != null) {
			drawSelection(newGC);
		}
		gc.drawImage(imageBuffer, render_x, render_y);
		newGC.dispose();
		imageBuffer.dispose();
		clearMargin(gc, getBackground());
	}
	private void drawSelection(GC gc) {
		gc.setForeground(sel_color);
		gc.setLineWidth(sel_size);
		int start_x = movingSelectionStart.x;
		int start_y = movingSelectionStart.y;
		if (autoScrollDirection == SWT.DOWN) {
		}
		else if (autoScrollDirection == SWT.UP) {
		}
		
		int end_x = 0;
		int end_y = 0;
		switch (calculateMovingSelection(movingSelectionEnd.x, movingSelectionEnd.y)) {
		case SE:
			while (start_x < movingSelectionEnd.x) {
				gc.drawLine(start_x, movingSelectionStart.y, start_x+sel_length, movingSelectionStart.y);
				start_x += (sel_length*sel_gap);
			}
			while (start_y < movingSelectionEnd.y) {
				gc.drawLine(movingSelectionStart.x, start_y, movingSelectionStart.x, start_y+sel_length);
				start_y += (sel_length*sel_gap);
			}
			start_x -= sel_gap;
			start_y -= sel_gap;
			end_x = start_x;
			end_y = start_y;
			if (isReachBottom() || movingSelectionEnd.y < (getMaxClientRow() * getElementHeight())) {
				while (end_x > movingSelectionStart.x) {
					gc.drawLine(end_x-sel_length, start_y, end_x, start_y);
					end_x -= (sel_length*sel_gap);
				}
			}
			while (end_y > movingSelectionStart.y) {
				gc.drawLine(start_x, end_y-sel_length, start_x, end_y);
				end_y -= (sel_length*sel_gap);
			}
			break;
		case SW:
			while (start_x > movingSelectionEnd.x) {
				gc.drawLine(start_x-sel_length, movingSelectionStart.y, start_x, movingSelectionStart.y);
				start_x -= (sel_length*sel_gap);
			}
			while (start_y < movingSelectionEnd.y) {
				gc.drawLine(movingSelectionStart.x, start_y, movingSelectionStart.x, start_y+sel_length);
				start_y += (sel_length*sel_gap);
			}			
			start_x += sel_gap;
			start_y -= sel_gap;
			end_x = start_x;
			end_y = start_y;
			if (isReachBottom() || movingSelectionEnd.y < (getMaxClientRow() * getElementHeight())) {
				while (end_x < movingSelectionStart.x) {
					gc.drawLine(end_x, start_y, end_x+sel_length, start_y);
					end_x += (sel_length*sel_gap);
				}
			}
			while (end_y > movingSelectionStart.y) {
				gc.drawLine(start_x, end_y-sel_length, start_x, end_y);
				end_y -= (sel_length*sel_gap);
			}
			break;
		case NW:
			while (start_x > movingSelectionEnd.x) {
				gc.drawLine(start_x-sel_length, movingSelectionStart.y, start_x, movingSelectionStart.y);
				start_x -= (sel_length*sel_gap);
			}
			while (start_y > movingSelectionEnd.y) {
				gc.drawLine(movingSelectionStart.x, start_y-sel_length, movingSelectionStart.x, start_y);
				start_y -= (sel_length*sel_gap);
			}
			start_x += sel_gap;
			start_y += sel_gap;
			end_x = start_x;
			end_y = start_y;
			if (isReachTop() || movingSelectionEnd.y > 1) {
				while (end_x < movingSelectionStart.x) {
					gc.drawLine(end_x, start_y, end_x+sel_length, start_y);
					end_x += (sel_length*sel_gap);
				}
			}
			while (end_y < movingSelectionStart.y) {
				gc.drawLine(start_x, end_y, start_x, end_y+sel_length);
				end_y += (sel_length*sel_gap);
			}
			break;
		case NE:
			while (start_x < movingSelectionEnd.x) {
				gc.drawLine(start_x, movingSelectionStart.y, start_x+sel_length, movingSelectionStart.y);
				start_x += (sel_length*sel_gap);
			}
			while (start_y > movingSelectionEnd.y) {
				gc.drawLine(movingSelectionStart.x, start_y-sel_length, movingSelectionStart.x, start_y);
				start_y -= (sel_length*sel_gap);
			}
			start_x -= sel_gap;
			start_y += sel_gap;
			end_x = start_x;
			end_y = start_y;
			if (isReachTop() || movingSelectionEnd.y > 1) {			
				while (end_x > movingSelectionStart.x) {
					gc.drawLine(end_x-sel_length, start_y, end_x, start_y);
					end_x -= (sel_length*sel_gap);
				}
			}
			while (end_y < movingSelectionStart.y) {
				gc.drawLine(start_x, end_y, start_x, end_y+sel_length);
				end_y += (sel_length*sel_gap);
			}
			break;
		}
	}
	protected void clearMargin(GC gc, Color background) {
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
		movingSelectionStart = null;
		movingSelectionEnd = null;
		actualScrollStart_y = 0;
		max_e_row = -1;
		max_e_col = -1;
		selectedElements.clear();
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

		int start_x = Math.min(getMaxCol()*getElementWidth(), Math.max(0+sel_size, event.x));
		int start_y = Math.min(getClientArea().height, Math.max(0+sel_size, event.y));
		
		selection = new Point(start_x, start_y);
		int selectedIndex = findSelectedIndex(selection.x, selection.y, false);
		System.out.println("---- index: " + selectedIndex);
	}
	protected void handleMouseUp(Event event) {
		if (movingSelectionStart != null || movingSelectionEnd != null)
			redraw();
		
		resetInfo();
		endAutoScroll();
	}
	protected void handleMouseDoubleClick(Event event) {
		if (event.button != 1) {
			return;
		}
		resetInfo();
		mouseDoubleClick = true;
		doubleClickSelection = new Point(event.x, event.y);
		//TODO: double click action
	}
	protected void handleMouseMove(final Event event) {
		if (!mouseDown || mouseDoubleClick) return;
		if ((event.stateMask & SWT.BUTTON1) == 0) {
			return;
		}
		update();
		doAutoScroll(event.y);
		if (movingSelectionStart == null) {
			movingSelectionStart = new Point(selection.x, selection.y);
			actualScrollStart_y = movingSelectionStart.y + verticalScrollOffset;
		}
		else
			doMouseSelection(event.x, event.y);
		
		System.out.println(" " + movingSelectionStart + ", " + selection);		
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

		if (autoScrollDirection == SWT.NULL && movingSelectionStart != null && movingSelectionEnd != null) {
			int s_x = movingSelectionStart.x;
			int e_x = event.x;
			int s_y = movingSelectionStart.y;
			int e_y = event.x;
			
			switch(calculateMovingSelection(event.x, event.y)) {
			case SW:
				s_x = event.x;
				e_x = movingSelectionStart.x;
				break;
			case NW:
				s_x = event.x;
				e_x = movingSelectionStart.x;
				s_y = event.y;
				e_y = movingSelectionStart.y;
				break;
			case NE:
				s_y = event.y;
				e_y = movingSelectionStart.y;
				break;
			}			
			col_start = Math.max(0, getSelectedCol(s_x - e_offset_x, true));
			col_end = Math.min(getSelectedCol(e_x + event.width - e_offset_x, true) + 1, getMaxCol());
			row_start = Math.max(0, getSelectedRow(s_y - e_offset_y, true));
			row_end = Math.min(getSelectedRow(e_y + event.height - e_offset_y, true) + 1, getMaxRow());
			renderWidth = s_x + e_x + event.width;
			renderHeight = s_y + e_y + event.height;
		}
		else {
			//row_start = Math.max(0, getSelectedRow(0 - e_offset_y, true));
			row_start = Math.max(0, getCurrentTopRow());
			row_end = Math.min(row_end + row_start + 1, getMaxRow()); 
		}
		//System.out.println("performPaint: " + col_start + "," + col_end + " - " + row_start + "," + row_end);
		performPaint(event.gc, col_start, col_end, row_start, row_end, 0, 0, renderWidth, renderHeight);
	}	
	protected void handleResize(Event event) {
		resetInfo();
		setVerticalScrollBar(getVerticalBar());
		if (!claimBottomFreeSpace())
			calculateTopIndex();
	}
	/****************************
	 * testing only
	 **************/
	public static void main(String[] args) {
        final Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setLayout(new FillLayout());
        shell.setSize(400, 400);
        shell.setLocation(0, 0);
        IconCanvas test = new IconCanvas(shell, SWT.NONE);
        BitSet testInput = new BitSet();
		testInput.set(0,1000);
        test.setInput(testInput);
        
        shell.open();
        while (!shell.isDisposed()) {
        	if (!display.readAndDispatch())
        		display.sleep();
       	}
	}
}
