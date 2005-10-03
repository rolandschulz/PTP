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

import java.util.Hashtable;
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
	
	protected boolean updateCaretDirection = true;
	protected Cursor ibeamCursor = null;
	protected Hashtable keyActionMap = new Hashtable();
	protected Listener listener = null;
	
	/*
	private int totalIcons = 0;
	private int visible_e_col = 0;
	private int visible_e_row = 0;
	private int view_width = 0;
	private int view_height = 0;	
	private int max_e_row = 0;
	*/

	protected int max_e_row = -1;
	protected int current_top_row = -1;
	
	protected boolean mouseDown = false;
	protected boolean mouseDoubleClick = false;	// true=a double click ocurred. Don't do mouse swipe selection.
	protected boolean doubleClickEnabled = true;	// see getDoubleClickEnabled
	protected boolean continueSelection = false;
	protected boolean specialSelection = false;
	protected Point selection = null;
	protected Point doubleClickSelection;			// selection after last mouse double click
	
	protected int autoScrollDirection = SWT.NULL;	// the direction of autoscrolling (up, down, right, left)
	protected int autoScrollDistance = 0;
	protected int verticalScrollOffset = 0;		// pixel based
	protected Object input = null;
	
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
		int width = getClientArea().width - getVerticalBar().getSize().x;
		return Math.max(0, (width - e_offset_x) / getElementWidth() + 1);
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
				doSelectionPageUp(getMaxClientRow(), event.x);
				break;
			case ST.SELECT_PAGE_DOWN:
				System.out.println("++++ select page down");
				doSelectionPageDown(getMaxClientRow(), event.x);
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

	protected void doAutoScroll(Event event) {
		Rectangle area = getClientArea();
		int x_loc = Math.max(0, Math.min(event.x, getMaxCol()*getElementWidth()));
		
		if (event.y > area.height) {
			doAutoScroll(SWT.DOWN, getElementHeight(), x_loc);
		}
		else  if (event.y < 0) {
			doAutoScroll(SWT.UP, -getElementHeight(), x_loc);
		}
		else {
			endAutoScroll();
		}
	}
	protected void doAutoScroll(int direction, final int distance, final int x_loc) {
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
						doSelectionPageUp(distance, x_loc);
						display.timerExec(TIMER_INTERVAL, this);
					}
				}
			};
		} else if (direction == SWT.DOWN) {
			timer = new Runnable() {
				public void run() {
					if (autoScrollDirection == SWT.DOWN) {
						doSelectionPageDown(distance, x_loc);
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
	protected void doSelectionPageUp(int distance, int x_loc) {
		int rows = (distance / getElementHeight());
		//TODO selectoin
		doPageUp(rows);
	}
	protected void doSelectionPageDown(int distance, int x_loc) {
		int rows = (distance / getElementHeight());
		//TODO selectoin
		doPageDown(rows);
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
	
	protected int getSelectedCol(int x_loc) {
		int col = (Math.abs(x_loc + e_spacing_x) / getElementWidth()) + 1;
		int max_col = getMaxCol();
		Math.max(col, max_col);
		col = col >= max_col ? max_col : col;
		int test_loc_x = getElementWidth() * (col - 1);
		if (x_loc > test_loc_x && x_loc < (test_loc_x + e_width)) {
			return col - 1;
		}
		return -1;
	}
	protected int getSelectedRow(int y_loc) {
		int row = (Math.abs(verticalScrollOffset + y_loc + e_spacing_y) / getElementHeight()) + 1;
		int test_loc_y = getElementHeight() * (row - 1) - verticalScrollOffset;
		if (y_loc > test_loc_y && y_loc < (test_loc_y + e_height)) {
			return row - 1;
		}
		return -1;
	}
	protected int getSelectedIndex() {
		if (selection != null) {
			int col = getSelectedCol(selection.x);
			if (col > -1) {
				int row = getSelectedRow(selection.y);
				if (row > -1) {
					return row * getMaxCol() + col;
				}
			}
		}
		return -1;
	}
	
	protected void doSelection(int direction) {
		int redrawStart = -1;
		int redrawEnd = -1;

		if (direction == -1) {
			int selectedIndex = getSelectedIndex();
			System.out.println("---- index: " + selectedIndex);
		}
		/*
		if (selectionAnchor == -1) {
			selectionAnchor = selection.x;
		}	
		if (direction == ST.COLUMN_PREVIOUS) {
			if (caretOffset < selection.x) {
				// grow selection
				redrawEnd = selection.x; 
				redrawStart = selection.x = caretOffset;		
				// check if selection has reversed direction
				if (selection.y != selectionAnchor) {
					redrawEnd = selection.y;
					selection.y = selectionAnchor;
				}
			}
			else	// test whether selection actually changed. Fixes 1G71EO1
			if (selectionAnchor == selection.x && caretOffset < selection.y) {
				// caret moved towards selection anchor (left side of selection). 
				// shrink selection			
				redrawEnd = selection.y;
				redrawStart = selection.y = caretOffset;		
			}
		}
		else {
			if (caretOffset > selection.y) {
				// grow selection
				redrawStart = selection.y;
				redrawEnd = selection.y = caretOffset;
				// check if selection has reversed direction
				if (selection.x != selectionAnchor) {
					redrawStart = selection.x;				
					selection.x = selectionAnchor;
				}
			}
			else	// test whether selection actually changed. Fixes 1G71EO1
			if (selectionAnchor == selection.y && caretOffset > selection.x) {
				// caret moved towards selection anchor (right side of selection). 
				// shrink selection			
				redrawStart = selection.x;
				redrawEnd = selection.x = caretOffset;		
			}
		}
		if (redrawStart != -1 && redrawEnd != -1) {
			internalRedrawRange(redrawStart, redrawEnd - redrawStart, true);
			sendSelectionEvent();
		}
		*/
	}	
	
	protected void endAutoScroll() {
		autoScrollDirection = SWT.NULL;
	}
	protected void performPaint(GC gc)	{
		Rectangle clientArea = getClientArea();
		Color background = getBackground();
		
		if (clientArea.width == 0) {
			return;
		}
		if (clientArea.height > 0) {
			// renderHeight will be negative when only top margin needs redrawing
			Color foreground = getForeground();
			Image imageBuffer = new Image(getDisplay(), clientArea.width, clientArea.height);
			GC newGC = new GC(imageBuffer, SWT.LEFT_TO_RIGHT);
			newGC.setFont(getFont());
			newGC.setForeground(foreground);
			newGC.setBackground(background);
			int max_col = getMaxCol();
			int start_num = current_top_row * max_col;
			int end_num = Math.min(getTotalElements(), (current_top_row + getMaxClientRow() + 1) * max_col);
			
			for (int i=start_num; i<end_num; i++) {
				int index = i+1;
				int x = (max_col > index) ? index : (index % max_col);
				x = (x == 0) ? max_col : x;
				int x_loc = e_offset_x + ((x - 1) * (e_width + e_spacing_x));
				
				int y = ((x == max_col) ? index / max_col : (max_col > index) ? 1 : ((index / max_col) + 1));
				int y_loc = e_offset_y + ((y - 1) * getElementHeight()) - verticalScrollOffset;
				
				newGC.fillRectangle(x_loc, y_loc, e_width, e_height);
				newGC.drawString(""+i, x_loc, y_loc);
			}
			clearMargin(newGC, background, clientArea, 0);
			gc.drawImage(imageBuffer, 0, 0);
			newGC.dispose();
			imageBuffer.dispose();
		}
		clearMargin(gc, background, clientArea, 0);
	}

	protected void clearMargin(GC gc, Color background, Rectangle clientArea, int y) {
		// clear the margin background
		gc.setBackground(background);
	}
	
	protected void resetInfo() {
		mouseDown = false;
		mouseDoubleClick = false;
		continueSelection = false;
		specialSelection = false;
		selection = null;
		doubleClickSelection = null;
		max_e_row = -1;
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
		
		event.y -= e_offset_y;
		event.x -= e_offset_x;
		selection = new Point(event.x, event.y);
		doSelection(-1);
	}
	protected void handleMouseUp(Event event) {
		resetInfo();
		endAutoScroll();
	}
	protected void handleMouseDoubleClick(Event event) {
		if (event.button != 1 || doubleClickEnabled == false) {
			return;
		}
		event.y -= e_offset_y;
		event.x -= e_offset_x;
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
		event.y -= e_offset_y;
		event.x -= e_offset_x;
		update();
		doAutoScroll(event);
	}	
	protected void handlePaint(Event event) {
		// Check if there is work to do
		if (event.height == 0) {
			return;
		}
		performPaint(event.gc);
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
		int[] array = new int[100];
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
        shell.setSize(200, 200);
        IconCanvas test = new IconCanvas(shell, SWT.NONE);
        test.setInput(test.createTestObject());
        
        shell.open();
        while (!shell.isDisposed()) {
        	if (!display.readAndDispatch())
        		display.sleep();
       	}
	}
}
