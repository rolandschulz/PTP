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
import java.util.BitSet;
import java.util.Hashtable;
import java.util.Iterator;
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
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
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
	protected List actionListeners = new ArrayList();
	protected IToolTipProvider toolTipProvider = null;
	protected IImageProvider imageProvider = null;
	// default element info
	protected int e_offset_x = 5;
	protected int e_offset_y = 5;
	protected int e_spacing_x = 4;
	protected int e_spacing_y = 4;
	protected int e_width = 16;
	protected int e_height = 16;
	protected int max_e_row = -1;
	protected int max_e_col = -1;
	// tooltip
	protected Shell toolTipShell = null;
	protected Label toolTipLabel = null;
	// key and listener
	protected Hashtable keyActionMap = new Hashtable();
	protected Listener listener = null;
	protected boolean mouseDown = false;
	protected boolean mouseDoubleClick = false;
	// scrolling and selection
	protected BitSet selectedElements = new BitSet();
	protected BitSet tempSelectedElements = new BitSet();
	protected int sel_size = 1;
	protected final int sel_length = 2;
	protected final int sel_gap = 2;
	protected Color sel_color = null;
	protected Point selection = null;
	protected Point movingSelectionStart = null;
	protected Point movingSelectionEnd = null;
	protected int current_top_row = -1;
	protected int actualScrollStart_y = 0;
	protected int autoScrollDirection = SWT.NULL;
	protected int verticalScrollOffset = 0;
	// mouse and color
	protected Cursor defaultCursor = null;
	protected Color background = null;
	protected Color foreground = null;
	//font
	protected final int font_size = 9;
	//margin
	protected final int margin_text = 2;
	protected Color margin_color = null;
	protected boolean displayRuler = true;
	private final int DEFAULT_OFFSET = 5;
	//tooltip
	protected Color tooltip_color_bg = null;
	protected Color tooltip_color_fg = null;	
	
	// input
	protected int total_elements = 0;
	final static boolean IS_CARBON, IS_GTK, IS_MOTIF;
	final static boolean DOUBLE_BUFFER;
	static {
		String platform = SWT.getPlatform();
		IS_CARBON = "carbon".equals(platform);
		IS_GTK = "gtk".equals(platform);
		IS_MOTIF = "motif".equals(platform);
		DOUBLE_BUFFER = !IS_CARBON;
	}

	public IconCanvas(Composite parent, int style) {
		super(parent, SWT.V_SCROLL | SWT.WRAP | SWT.MULTI | style);
		Display display = getDisplay();
		calculateVerticalScrollBar();
		createKeyBindings();
		defaultCursor = new Cursor(display, SWT.CURSOR_ARROW);
		setCursor(defaultCursor);
		installListeners();
		initializeAccessible();
		sel_color = display.getSystemColor(SWT.COLOR_RED);
		margin_color = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
		tooltip_color_fg = display.getSystemColor(SWT.COLOR_INFO_FOREGROUND);
		tooltip_color_bg = display.getSystemColor(SWT.COLOR_INFO_BACKGROUND);
			
		changeFontSize(display);
		super.setBackground(getBackground());
		super.setForeground(getForeground());
	}
	private void changeFontSize(Display display) {
        FontData[] data = getFont().getFontData();
        for (int i = 0; i < data.length; i++) {
            data[i].setHeight(font_size);
        }
        setFont(new Font(display, data));
	}
	
	public void addActionListener(IIconCanvasActionListener actionListener) {
		if (!actionListeners.contains(actionListener)) {
			actionListeners.add(actionListener);
		}
	}
	public void removeActionListener(IIconCanvasActionListener actionListener) {
		if (actionListeners.contains(actionListener)) {
			actionListeners.remove(actionListener);
		}
	}
	protected void fireAction(int type) {
		int[] indexes = getSelectedIndexes();
		for (Iterator i = actionListeners.iterator(); i.hasNext();) {
			((IIconCanvasActionListener) i.next()).handleAction(type, indexes);
		}
	}
	protected void fireAction(int type, int index) {
		if (index > -1) {
			for (Iterator i = actionListeners.iterator(); i.hasNext();) {
				((IIconCanvasActionListener) i.next()).handleAction(type, index);
			}
		}
	}
	private void resetCanvas() {
		checkWidget();
		current_top_row = -1;
		actualScrollStart_y = 0;
		autoScrollDirection = SWT.NULL;
		verticalScrollOffset = 0;
		selectedElements.clear();
		tempSelectedElements.clear();
		resetMargin();
		resetInfo();
		calculateVerticalScrollBar();
		redraw();
	}
	public void setToolTipProvider(IToolTipProvider toolTipProvider) {
		this.toolTipProvider = toolTipProvider;
	}
	public void setImageProvider(IImageProvider imageProvider) {
		this.imageProvider = imageProvider;
	}
	public void setTotal(int total) {
		if (total < 0) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		this.total_elements = total;
		resetCanvas();
	}
	private void resetMargin() {
		if (isDisplayRuler()) {
			GC newGC = getGC();
			newGC.setFont(getFont());
			e_offset_x = newGC.stringExtent(String.valueOf(total_elements-1)).x + e_spacing_x + margin_text;
			newGC.dispose();
		}
		else {
			e_offset_x = DEFAULT_OFFSET;
		}
	}
	public void setIconSpace(int e_spacing_x, int e_spacing_y) {
		if (e_spacing_x < 0 || e_spacing_y < 0) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		this.e_spacing_x = e_spacing_x;
		this.e_spacing_y = e_spacing_y;
		resetCanvas();
	}
	public void setIconSize(int e_width, int e_height) {
		if (e_width <= 0 || e_height <= 0) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		this.e_height = e_height;
		this.e_width = e_width;
		resetCanvas();
	}
	public int getTotalElements() {
		return total_elements;
	}
	public void setSelectionSize(int size) {
		this.sel_size = size;
	}
	public void setSelectionColor(int colorIndex) {
		checkWidget();
		this.sel_color = getDisplay().getSystemColor(colorIndex);
	}
	public void setDisplayRuler(boolean displayRuler) {
		this.displayRuler = displayRuler;
		resetCanvas();
	}
	public boolean isDisplayRuler() {
		return displayRuler;
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
			max_e_row = total % max_col == 0 ? max_row : max_row + 1;
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
			int max_height = getMaxHeight() + e_offset_y + sel_size;
			if (clientArea.height < max_height) {
				verticalBar.setValues(verticalBar.getSelection(), verticalBar.getMinimum(), max_height, clientArea.height, getVerticalIncrement(), clientArea.height);
			} else if (verticalBar.getThumb() != INACTIVE || verticalBar.getMaximum() != INACTIVE) {
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
		} else {
			intAction = action.intValue();
		}
		return intAction;
	}
	public void setKeyBinding(int key, int action) {
		checkWidget();
		int keyValue = key & SWT.KEY_MASK;
		int modifierValue = key & SWT.MODIFIER_MASK;
		char keyChar = (char) keyValue;
		if (Character.isLetter(keyChar)) {
			char ch = Character.toUpperCase(keyChar);
			int newKey = ch | modifierValue;
			if (action == SWT.NULL) {
				keyActionMap.remove(new Integer(newKey));
			} else {
				keyActionMap.put(new Integer(newKey), new Integer(action));
			}
			ch = Character.toLowerCase(keyChar);
			newKey = ch | modifierValue;
			if (action == SWT.NULL) {
				keyActionMap.remove(new Integer(newKey));
			} else {
				keyActionMap.put(new Integer(newKey), new Integer(action));
			}
		} else {
			if (action == SWT.NULL) {
				keyActionMap.remove(new Integer(key));
			} else {
				keyActionMap.put(new Integer(key), new Integer(action));
			}
		}
	}
	protected void createKeyBindings() {
		// Navigation
		setKeyBinding(SWT.ARROW_UP, ST.LINE_UP);
		setKeyBinding(SWT.ARROW_DOWN, ST.LINE_DOWN);
		setKeyBinding(SWT.ARROW_LEFT, ST.COLUMN_PREVIOUS);
		setKeyBinding(SWT.ARROW_RIGHT, ST.COLUMN_NEXT);
		setKeyBinding(SWT.HOME, ST.LINE_START);
		setKeyBinding(SWT.END, ST.LINE_END);
		setKeyBinding(SWT.PAGE_UP, ST.PAGE_UP);
		setKeyBinding(SWT.PAGE_DOWN, ST.PAGE_DOWN);
		// Selection
		setKeyBinding('A' | SWT.MOD1, ST.SELECT_ALL);
		setKeyBinding(SWT.ARROW_UP | SWT.MOD2, ST.SELECT_LINE_UP);
		setKeyBinding(SWT.ARROW_DOWN | SWT.MOD2, ST.SELECT_LINE_DOWN);
		setKeyBinding(SWT.ARROW_LEFT | SWT.MOD2, ST.SELECT_COLUMN_PREVIOUS);
		setKeyBinding(SWT.ARROW_RIGHT | SWT.MOD2, ST.SELECT_COLUMN_NEXT);
		setKeyBinding(SWT.HOME | SWT.MOD2, ST.SELECT_LINE_START);
		setKeyBinding(SWT.END | SWT.MOD2, ST.SELECT_LINE_END);
		setKeyBinding(SWT.PAGE_UP | SWT.MOD2, ST.SELECT_PAGE_UP);
		setKeyBinding(SWT.PAGE_DOWN | SWT.MOD2, ST.SELECT_PAGE_DOWN);
		// Modification
		// Cut, Copy, Paste, delete
		setKeyBinding('X' | SWT.MOD1, ST.CUT);
		setKeyBinding('C' | SWT.MOD1, ST.COPY);
		setKeyBinding('V' | SWT.MOD1, ST.PASTE);
		setKeyBinding(SWT.BS, ST.DELETE_PREVIOUS);
		setKeyBinding(SWT.DEL, ST.DELETE_NEXT);
	}
	public void invokeAction(int action) {
		checkWidget();
		switch (action) {
		// Navigation
		case ST.LINE_UP:
			doUp(false);
			break;
		case ST.LINE_DOWN:
			doDown(false);
			break;
		case ST.COLUMN_PREVIOUS:
			doPrevious(false);
			break;
		case ST.COLUMN_NEXT:
			doNext(false);
			break;
		case ST.LINE_START:
			doLineStart(false);
			break;
		case ST.LINE_END:
			doLineEnd(false);
			break;
		case ST.PAGE_UP:
			doPageUp(getMaxClientRow());
			break;
		case ST.PAGE_DOWN:
			doPageDown(getMaxClientRow());
			break;
		// Selection
		case ST.SELECT_ALL:
			doSelectionAll();
			break;
		case ST.SELECT_LINE_UP:
			doUp(true);
			break;
		case ST.SELECT_LINE_DOWN:
			doDown(true);
			break;
		case ST.SELECT_COLUMN_PREVIOUS:
			doPrevious(true);
			break;
		case ST.SELECT_COLUMN_NEXT:
			doNext(true);
			break;
		case ST.SELECT_LINE_START:
			doLineStart(true);
			break;
		case ST.SELECT_LINE_END:
			doLineEnd(true);
			break;
		case ST.SELECT_PAGE_UP:
			doSelectionPageUp(getMaxClientRow());
			break;
		case ST.SELECT_PAGE_DOWN:
			doSelectionPageDown(getMaxClientRow());
			break;
		// Modification
		case ST.CUT:
			doCut();
			break;
		case ST.COPY:
			doCopy();
			break;
		case ST.PASTE:
			doPaste();
			break;
		case ST.DELETE_PREVIOUS:
			doDelete();
			break;
		case ST.DELETE_NEXT:
			doDelete();
			break;
		default:
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
				if (isEnabled())
					state |= ACC.STATE_FOCUSABLE;
				if (isFocusControl())
					state |= ACC.STATE_FOCUSED;
				if (isVisible() == false)
					state |= ACC.STATE_INVISIBLE;
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
				case SWT.Dispose:
					handleDispose(event);
					break;
				case SWT.KeyDown:
					handleKeyDown(event);
					break;
				case SWT.KeyUp:
					handleKeyUp(event);
					break;
				case SWT.MouseDown:
					handleMouseDown(event);
					break;
				case SWT.MouseUp:
					handleMouseUp(event);
					break;
				case SWT.MouseDoubleClick:
					handleMouseDoubleClick(event);
					break;
				case SWT.MouseMove:
					handleMouseMove(event);
					break;
				case SWT.Paint:
					handlePaint(event);
					break;
				case SWT.Resize:
					handleResize(event);
					break;
				case SWT.FocusOut:
					handleFocusOut(event);
					break;
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
		addListener(SWT.FocusOut, listener);
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
		} else {
			action = getKeyBinding(event.character | event.stateMask);
			if (action == SWT.NULL) {
				if ((event.stateMask & SWT.CTRL) != 0 && (event.character >= 0) && event.character <= 31) {
					int c = event.character + 64;
					action = getKeyBinding(c | event.stateMask);
				}
			}
		}
		if (action != SWT.NULL) {
			invokeAction(action);
		}
	}
	protected boolean isReachTop() {
		return (verticalScrollOffset <= 0);
	}
	protected boolean isReachBottom() {
		return (getMaxRow() - current_top_row <= getMaxClientRow());
	}
	protected void doAutoScroll(int x_loc, int y_loc) {
		if (y_loc > getClientArea().height && !isReachBottom()) {
			hideToolTip();
			doAutoScroll(SWT.DOWN, getVerticalIncrement(), x_loc, y_loc);
		} else if (y_loc < 0 && !isReachTop()) {
			hideToolTip();
			doAutoScroll(SWT.UP, -getVerticalIncrement(), x_loc, y_loc);
		} else {
			if (movingSelectionStart == null) {
				movingSelectionStart = new Point(selection.x, selection.y);
			} else {
				doMouseSelection(movingSelectionStart.x, movingSelectionStart.y, x_loc, y_loc, false);
			}
			endAutoScroll();
		}
	}
	protected void doAutoScroll(int direction, final int distance, final int x_loc, final int y_loc) {
		// If we're already autoscrolling in the given direction do nothing
		if (autoScrollDirection == direction) {
			return;
		}
		Runnable timer = null;
		final int TIMER_INTERVAL = 50;
		final Display display = getDisplay();
		if (direction == SWT.UP) {
			timer = new Runnable() {
				public void run() {
					if (autoScrollDirection == SWT.UP && !isReachTop()) {
						doSelectionPageUp(distance, x_loc, y_loc, true);
						display.timerExec(TIMER_INTERVAL, this);
					}
				}
			};
		} else if (direction == SWT.DOWN) {
			timer = new Runnable() {
				public void run() {
					if (autoScrollDirection == SWT.DOWN && !isReachBottom()) {
						doSelectionPageDown(distance, x_loc, y_loc, true);
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
		int index = selection == null ? -1 : findSelectedIndexByLocation(selection.x, (actualScrollStart_y - verticalScrollOffset), false);
		if (index > 0) {
			int max_col = getMaxCol();
			int max_row = getMaxClientRow() - 1;
			int total_in_page = max_col * max_row;
			int start_index = Math.max(0, (index - total_in_page));
			actualScrollStart_y -= max_row * getElementHeight();
			if (actualScrollStart_y - verticalScrollOffset < 0) {
				doPageUp(max_row);
			}
			selection.y = Math.max(0, actualScrollStart_y - verticalScrollOffset);
			selectElements(start_index, index);
			redraw(start_index, index);
		}
	}
	protected void doSelectionPageDown(int distance) {
		int index = selection == null ? -1 : findSelectedIndexByLocation(selection.x, (actualScrollStart_y - verticalScrollOffset), false);
		if (index > -1) {
			int max_col = getMaxCol();
			int max_row = getMaxClientRow() - 1;
			int total_in_page = max_col * max_row;
			int end_index = Math.min(getTotalElements(), (index + total_in_page));
			actualScrollStart_y += max_row * getElementHeight();
			if (actualScrollStart_y - verticalScrollOffset > (max_row + 1) * getElementHeight()) {
				doPageDown(max_row);
			}
			selection.y = Math.max(0, actualScrollStart_y - verticalScrollOffset);
			selectElements(index, end_index);
			redraw(index, end_index);
		}
	}
	protected void doSelectionPageUp(int distance, int x_loc, int y_loc, boolean isSelect) {
		doPageUp(distance / getElementHeight());
		if (movingSelectionStart != null && isSelect) {
			movingSelectionStart.y += getVerticalIncrement();
			doMouseSelection(movingSelectionStart.x, movingSelectionStart.y, x_loc, y_loc, true);
		}
	}
	protected void doSelectionPageDown(int distance, int x_loc, int y_loc, boolean isSelect) {
		doPageDown(distance / getElementHeight());
		if (movingSelectionStart != null && isSelect) {
			movingSelectionStart.y -= getVerticalIncrement();
			doMouseSelection(movingSelectionStart.x, movingSelectionStart.y, x_loc, y_loc, true);
		}
	}
	protected void doDelete() {
		fireAction(IIconCanvasActionListener.DELETE_ACTION);
	}
	protected void doCopy() {
		fireAction(IIconCanvasActionListener.COPY_ACTION);
	}
	protected void doCut() {
		fireAction(IIconCanvasActionListener.CUT_ACTION);
	}
	protected void doPaste() {
		fireAction(IIconCanvasActionListener.PASTE_ACTION);
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
			int scrollOffset = Math.min(verticalScrollOffset + (scrollRows * getVerticalIncrement()), (max_row - max_client_row) * getElementHeight());
			if (scrollOffset > verticalScrollOffset) {
				setVerticalScrollOffset(scrollOffset, true);
			}
		}
	}
	protected void doUp(boolean isSelect) {
		int index = selection == null ? -1 : findSelectedIndexByLocation(selection.x, (actualScrollStart_y - verticalScrollOffset), false);
		if (index > 0) {
			int max_col = getMaxCol();
			if ((index - max_col) > -1) {
				actualScrollStart_y -= getElementHeight();
				if (selection.y < getVerticalIncrement()) {
					doPageUp(1);
				}
				selection.y = actualScrollStart_y - verticalScrollOffset;
				if (!isSelect)
					clearSelection(index);
				if (!isSelected(index - max_col))
					selectElement(index - max_col);
				else
					clearSelection(index);
				redraw(index, index - max_col);
			}
		}
	}
	protected void doDown(boolean isSelect) {
		int index = selection == null ? -1 : findSelectedIndexByLocation(selection.x, (actualScrollStart_y - verticalScrollOffset), false);
		if (index > -1) {
			int max_col = getMaxCol();
			if ((index + max_col) < getTotalElements()) {
				actualScrollStart_y += getElementHeight();
				if (selection.y + getVerticalIncrement() > getMaxClientRow() * getElementHeight()) {
					doPageDown(1);
				}
				selection.y = actualScrollStart_y - verticalScrollOffset;
				if (!isSelect)
					clearSelection(index);
				if (!isSelected(index + max_col))
					selectElement(index + max_col);
				else
					clearSelection(index);
				redraw(index, index + max_col);
			}
		}
	}
	protected void doPrevious(boolean isSelect) {
		int index = selection == null ? -1 : findSelectedIndexByLocation(selection.x, (actualScrollStart_y - verticalScrollOffset), false);
		if (index > 0) {
			selection.x -= getElementWidth();
			if (selection.x < e_offset_x) {
				actualScrollStart_y -= getElementHeight();
				if (actualScrollStart_y - verticalScrollOffset < getVerticalIncrement()) {
					doPageUp(1);
				}
				selection.x = e_offset_x + (getMaxCol() * getElementWidth()) - e_width;
				selection.y = actualScrollStart_y - verticalScrollOffset;
			}
			if (!isSelect)
				clearSelection(index);
			if (!isSelected(index - 1))
				selectElement(index - 1);
			else
				clearSelection(index);
			redraw(index - 1, index);
		}
	}
	protected void doNext(boolean isSelect) {
		int index = selection == null ? -1 : findSelectedIndexByLocation(selection.x, (actualScrollStart_y - verticalScrollOffset), false);
		if (index > -1 && index < getTotalElements() - 1) {
			selection.x += getElementWidth();
			if (selection.x > (getMaxCol() * getElementWidth() + e_offset_x)) {
				actualScrollStart_y += getElementHeight();
				if (selection.y + getVerticalIncrement() > getMaxClientRow() * getElementHeight()) {
					doPageDown(1);
				}
				selection.x = e_offset_x + e_spacing_x;
				selection.y = actualScrollStart_y - verticalScrollOffset;
			}
			if (!isSelect)
				clearSelection(index);
			if (!isSelected(index + 1))
				selectElement(index + 1);
			else
				clearSelection(index);
			redraw(index, index + 1);
		}
	}
	protected void doLineStart(boolean isSelect) {
		int index = selection == null ? -1 : findSelectedIndexByLocation(selection.x, (actualScrollStart_y - verticalScrollOffset), false);
		if (index > -1) {
			int start_pos_x = e_offset_x + e_spacing_x;
			int start_index = findSelectedIndexByLocation(start_pos_x, (actualScrollStart_y - verticalScrollOffset), false);
			if (start_index > -1) {
				selection.x = start_pos_x;
				if (!isSelect) {
					selectedElements.clear(index);
					selectElement(start_index);
				}
				else {
					selectElements(start_index, index);
				}
				redraw(start_index, index);
			}
		}
	}
	protected void doLineEnd(boolean isSelect) {
		int index = selection == null ? -1 : findSelectedIndexByLocation(selection.x, (actualScrollStart_y - verticalScrollOffset), false);
		if (index > -1) {
			if (!isSelect)
				selectedElements.clear(index);
			int total = getTotalElements() - 1;
			if (index > total - getMaxCol()) {
				int end_counter = total - index;
				selection.x = (end_counter + 1) * getElementWidth() + e_offset_x;
				if (!isSelect)
					selectElement(index + end_counter);
				else
					selectElements(index, index + end_counter);
				redraw(index, index + end_counter);
			} else {
				int end_pos_x = e_offset_x + (getMaxCol() * getElementWidth()) - e_width;
				int end_index = findSelectedIndexByLocation(end_pos_x, (actualScrollStart_y - verticalScrollOffset), false);
				if (end_index > -1) {
					selection.x = end_pos_x;
					if (!isSelect)
						selectElement(end_index);
					else
						selectElements(index, end_index);
					redraw(index, end_index);
				} else {
					redraw(index, index);
				}
			}
		}
	}
	protected void doSelectionAll() {
		selectedElements.set(0, getTotalElements());
		redraw();
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
			} else if (current_top_row >= max_row) {
				current_top_row = max_row - 1;
			}
		}
	}
	protected int getSelectedCol(int x_loc, boolean isApproximate) {
		int col = Math.min(x_loc / getElementWidth(), getMaxCol() - 1);
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
	protected int findSelectedIndex(int row, int col) {
		return row * getMaxCol() + col;
	}
	protected int findSelectedIndexByLocation(int loc_x, int loc_y, boolean isApproximate) {
		int col = getSelectedCol(loc_x - e_offset_x, isApproximate);
		if (col > -1) {
			int row = getSelectedRow(loc_y - e_offset_y, isApproximate);
			if (row > -1) {
				int index = findSelectedIndex(row, col);
				if (index < getTotalElements())
					return index;
			}
		}
		return -1;
	}
	protected Point findSection(int index) {
		int max_col = getMaxCol();
		int col = (int) Math.floor(index % max_col);
		int row = (int) Math.floor(index / max_col);
		return new Point(col, row);
	}
	protected Point findLocation(int index) {
		Point section = findSection(index);
		int x_loc = (section.x * getElementWidth() + e_offset_x);
		int y_loc = (section.y * getElementHeight() + e_offset_y) - verticalScrollOffset;
		return new Point(x_loc, y_loc);
	}
	protected int getDirection(int s_x, int s_y, int e_x, int e_y) {
		int direction = SE;
		if (s_x > e_x) {
			direction = SW;
		}
		if (s_y > e_y) {
			if (direction == SW)
				direction = NW;
			else
				direction = NE;
		}
		return direction;
	}
	protected BitSet selectElements(int col_start, int col_end, int row_start, int row_end) {
		BitSet newElements = new BitSet();
		int total = getTotalElements();
		for (int row_count = row_start; row_count < row_end; row_count++) {
			for (int col_count = col_start; col_count < col_end; col_count++) {
				int index = findSelectedIndex(row_count, col_count);
				if (index > -1 && index < total) {
					newElements.set(index);
				}
			}
		}
		return newElements;
	}
	private Point getStartedPoint(int x, int y) {
		return new Point(Math.max(e_offset_x-e_spacing_x, x), Math.max(0, Math.min((getMaxRow() - current_top_row) * getElementHeight() + sel_size, y)));
	}
	private Point getFinishedPoint(int x, int y) {
		return new Point(Math.min(getMaxCol()*getElementWidth() + e_offset_x - e_spacing_x, Math.max(e_offset_x-e_spacing_x, x)), Math.min((getMaxRow() - current_top_row) * getElementHeight() + sel_size, Math.max(0 + sel_size, y)));
	}
	protected void doMouseSelection(int mouse_start_x, int mouse_start_y, int mouse_end_x, int mouse_end_y, boolean isScrolling) {
		if (mouse_start_x < (e_offset_x - getElementWidth()) || mouse_start_y > (getMaxRow() - current_top_row + 1) * getElementHeight())
			return;
		
		boolean isInitialPage = (mouse_start_y - verticalScrollOffset == actualScrollStart_y);
		Point start_pt = getStartedPoint(mouse_start_x, mouse_start_y);
		Point end_pt = getFinishedPoint(mouse_end_x, mouse_end_y);
		int direction = getDirection(movingSelectionStart.x, movingSelectionStart.y, end_pt.x, end_pt.y);
		int s_x = start_pt.x;
		int e_x = end_pt.x;
		int s_y = isInitialPage ? start_pt.y : (actualScrollStart_y - verticalScrollOffset);
		int e_y = end_pt.y;
		switch (direction) {
		case SW:
			s_x = end_pt.x;
			e_x = start_pt.x;
			break;
		case NW:
			s_x = end_pt.x;
			e_x = start_pt.x;
			s_y = end_pt.y;
			e_y = start_pt.y;
			break;
		case NE:
			s_y = end_pt.y;
			e_y = start_pt.y;
			break;
		}
		int d_sx = s_x - e_offset_x + e_spacing_x;
		int d_ex = e_x - e_offset_x;
		int d_sy = s_y - e_offset_y + e_spacing_y;
		int d_ey = e_y - e_offset_y;
		
		int col_start = Math.max(0, getSelectedCol(d_sx, true));
		int col_end = Math.min(getSelectedCol(d_ex, true) + 1, getMaxCol());
		int row_start = Math.max(0, getSelectedRow(d_sy, true));
		int row_end = Math.min(getSelectedRow(d_ey, true) + 1, getMaxRow());
		tempSelectedElements.clear();
		tempSelectedElements.or(selectElements(col_start, col_end, row_start, row_end));
		if (movingSelectionEnd != null) {
			int last_end_x = movingSelectionEnd.x;
			int last_end_y = movingSelectionEnd.y;
			// redraw whole view if changed the direction
			if (getDirection(movingSelectionStart.x, movingSelectionStart.y, last_end_x, last_end_y) != direction || (isScrolling && (isReachTop() || isReachBottom()))) {
				movingSelectionEnd.x = end_pt.x;
				movingSelectionEnd.y = end_pt.y;
				redraw();
				return;
			}
			switch (direction) {
			case SE:
				if (e_x < last_end_x) {
					d_ex = last_end_x - e_offset_x - e_spacing_x;
					col_end = Math.min(getSelectedCol(d_ex, true) + 1, getMaxCol());
				}
				if (e_y < last_end_y) {
					d_ey = last_end_y - e_offset_y - e_spacing_y;
					row_end = Math.min(getSelectedRow(d_ey, true) + 1, getMaxRow());
				}
				break;
			case SW:
				if (e_x > last_end_x) {
					d_sx = last_end_x - e_offset_x + e_spacing_x;
					col_start = Math.max(0, getSelectedCol(d_sx, true));
				}
				if (e_y < last_end_y) {
					d_ey = last_end_y - e_offset_y - e_spacing_y;
					row_end = Math.min(getSelectedRow(d_ey, true) + 1, getMaxRow());
				}
				break;
			case NW:
				if (e_x > last_end_x) {
					d_sx = last_end_x - e_offset_x + e_spacing_x;
					col_start = Math.max(0, getSelectedCol(d_sx, true));
				}
				if (e_y > last_end_y) {
					d_sy = last_end_y - e_offset_y + e_spacing_y;
					row_start = Math.max(0, getSelectedRow(d_sy, true));
				}
				break;
			case NE:
				if (e_x < last_end_x) {
					d_ex = last_end_x - e_offset_x - e_spacing_x;
					col_end = Math.min(getSelectedCol(d_ex, true) + 1, getMaxCol());
				}
				if (e_y > last_end_y) {
					d_sy = last_end_y - e_offset_y + e_spacing_y;
					row_start = Math.max(0, getSelectedRow(d_sy, true));
				}
				break;
			}
		}
		movingSelectionEnd = new Point(end_pt.x, end_pt.y);
		redrawByIndex(col_start, col_end, row_start, row_end, direction, isScrolling);
		//redrawByLocation(d_sx, d_ex, d_sy, d_ey, direction, isScrolling);
	}
	protected void redrawByLocation(int d_sx, int d_ex, int d_sy, int d_ey, int direction, boolean isScrolling) {
		int d_x = d_sx;
		int d_y = d_sy + ((direction==NW||direction==NE)?(isScrolling ? getVerticalIncrement():0):0);
		int d_w = e_offset_x + Math.abs(d_ex - d_sx);
		int d_h = e_offset_y + Math.abs(d_ey - d_sy) + ((direction==SE||direction==SW)?(isScrolling ? getVerticalIncrement():0):0); 
		redraw(d_x, d_y, d_w, d_h, false);
	}
	protected void redrawByIndex(int col_start, int col_end, int row_start, int row_end, int direction, boolean isScrolling) {
		int redrawStart_x = e_offset_x + (col_start * getElementWidth()) - e_spacing_x;
		int redrawEnd_x = e_offset_x + (col_end * getElementWidth()) + e_spacing_x;
		int redrawStart_y = e_offset_y + (row_start * getElementHeight()) - verticalScrollOffset;
		int redrawEnd_y = e_offset_y + (row_end * getElementHeight()) + verticalScrollOffset;
		int margin_x = getElementWidth() + sel_size;
		int margin_y = getElementHeight() + sel_size;
		int d_x = Math.max(0, (redrawStart_x - margin_x));
		int d_y = Math.max(0, (redrawStart_y - margin_y)) + ((direction==NW||direction==NE)?(isScrolling ? getVerticalIncrement():0):0);
		int d_w = Math.abs(redrawEnd_x - redrawStart_x) + (margin_x*2);
		int d_h = Math.abs(redrawEnd_y - redrawStart_y) + (margin_y*2) + ((direction==SE||direction==SW)?(isScrolling ? getVerticalIncrement():0):0);
		if (d_x < e_offset_x) {
			d_x = 0;
			d_w += e_offset_x;
		}
		redraw(d_x, d_y, d_w, d_h, false);
	}
	protected void redraw(int start_index, int end_index) {
		int max_col = getMaxCol();
		int start_row = (int) Math.floor(start_index / max_col);
		int start_y_loc = (start_row * getElementHeight() + e_offset_y) - verticalScrollOffset;
		int end_row = (int) Math.floor(end_index / max_col);
		int end_y_loc = (end_row * getElementHeight() + e_offset_y) - verticalScrollOffset;
		int diff = Math.abs(end_row - start_row) + 1;
		redraw(0, Math.min(start_y_loc, end_y_loc), (max_col * getElementWidth() + e_offset_x), diff * getElementHeight(), false);
	}
	protected boolean canSelectElements(Point start_pt, Point end_pt) {
		int start_index = start_pt == null ? -1 : findSelectedIndexByLocation(start_pt.x, start_pt.y, false);
		int end_index = end_pt == null ? -1 : findSelectedIndexByLocation(end_pt.x, end_pt.y, false);
		int start_count = start_index + 1;
		int end_count = end_index;
		if (start_index > end_index) {// swarp
			if (end_index == -1)
				return false;
			start_count = end_count;
			end_count = start_index - 1;
		} else if (start_index < end_index) {
			if (start_index == -1) {
				selectElement(end_index);
				return true;
			}
		} else {// equals
			if (start_index > -1) {
				selectElement(start_index);
				return true;
			}
			return false;
		}
		for (int index = start_count; index < end_count + 1; index++) {
			selectedElements.set(index);
		}
		return true;
	}
	protected void selectElement(int index) {
		if (isSelected(index)) {
			selectedElements.clear(index);
		} else {
			selectedElements.set(index);
		}
	}
	protected boolean isSelected(int index) {
		return selectedElements.get(index);
	}
	protected void clearSelection(int index) {
		selectedElements.clear(index);
	}
	protected void selectElements(int from_index, int to_index) {
		selectElements(from_index, to_index, false);
	}
	protected void selectElements(int from_index, int to_index, boolean checkStatus) {
		for (int index = from_index; index < to_index + 1; index++) {
			if (checkStatus)
				selectElement(index);
			else
				selectedElements.set(index);
		}
	}
	protected void performPaint(GC gc, int col_start, int col_end, int row_start, int row_end, int render_x, int render_y, int renderWidth, int renderHeight) {
		Image imageBuffer = null;
		GC newGC = null;
		if (DOUBLE_BUFFER) {
			imageBuffer = new Image(getDisplay(), renderWidth, renderHeight);
			newGC = new GC(imageBuffer, SWT.LEFT_TO_RIGHT);
		} else {
			newGC = gc;
		}
		newGC.setFont(getFont());
		//draw margin background
		if (isDisplayRuler()) {
			newGC.setBackground(margin_color);
			newGC.fillRectangle(0, 0, e_offset_x - e_spacing_x, getClientArea().height);
		}

		int total = getTotalElements();
		for (int row_count = row_start; row_count < row_end; row_count++) {
			for (int col_count = col_start; col_count < col_end; col_count++) {
				int index = findSelectedIndex(row_count, col_count);
				if (index > -1 && index < total) {
					int x_loc = e_offset_x + ((col_count) * getElementWidth());
					int y_loc = e_offset_y + ((row_count) * getElementHeight()) - verticalScrollOffset;
					if (col_count == 0 && isDisplayRuler()) {
						drawIndex(newGC, String.valueOf(index), y_loc);
					}
					drawImage(newGC, index, x_loc, y_loc, (selectedElements.get(index) || tempSelectedElements.get(index)));
				}
			}
		}
		if (movingSelectionStart != null && movingSelectionEnd != null) {
			drawSelection(newGC);
		}
		if (DOUBLE_BUFFER) {
			gc.drawImage(imageBuffer, render_x, render_y);
			newGC.dispose();
			imageBuffer.dispose();
		}
		clearMargin(gc, getBackground());
	}
	protected void drawIndex(GC gc, String text, int y_loc) {
		Point text_size = gc.stringExtent(text);
		int align =  e_offset_x - e_spacing_x - text_size.x;
		int valign = y_loc + e_height/2 - text_size.y/2 ;
		gc.drawText(text, align, valign);
	}
	protected void drawImage(GC gc, int index, int x_loc, int y_loc, boolean isSelected) {
		Image statusImage = getStatusIcon(index, isSelected);
		if (statusImage != null) {
			gc.drawImage(statusImage, x_loc, y_loc);
			drawSpecial(index, gc, x_loc, y_loc, e_width, e_height);
		}
	}
	private void drawSelection(GC gc) {
		gc.setForeground(sel_color);
		gc.setLineWidth(sel_size);
		Point stationary_pt = getStartedPoint(movingSelectionStart.x, movingSelectionStart.y);
		int start_x = stationary_pt.x;
		int start_y = stationary_pt.y;
		int end_x = 0;
		int end_y = 0;
		switch (getDirection(movingSelectionStart.x, movingSelectionStart.y, movingSelectionEnd.x, movingSelectionEnd.y)) {
		case SE:
			while (start_x < movingSelectionEnd.x) {
				gc.drawLine(start_x, stationary_pt.y, start_x + sel_length, stationary_pt.y);
				start_x += (sel_length * sel_gap);
			}
			while (start_y < movingSelectionEnd.y) {
				gc.drawLine(stationary_pt.x, start_y, stationary_pt.x, start_y + sel_length);
				start_y += (sel_length * sel_gap);
			}
			start_x -= sel_gap;
			start_y -= sel_gap;
			end_x = start_x;
			end_y = start_y;
			if (isReachBottom() || movingSelectionEnd.y < getClientArea().height) {
				while (end_x > stationary_pt.x) {
					gc.drawLine(end_x - sel_length, start_y, end_x, start_y);
					end_x -= (sel_length * sel_gap);
				}
			}
			while (end_y > stationary_pt.y) {
				gc.drawLine(start_x, end_y - sel_length, start_x, end_y);
				end_y -= (sel_length * sel_gap);
			}
			break;
		case SW:
			while (start_x > movingSelectionEnd.x) {
				gc.drawLine(start_x - sel_length, stationary_pt.y, start_x, stationary_pt.y);
				start_x -= (sel_length * sel_gap);
			}
			while (start_y < movingSelectionEnd.y) {
				gc.drawLine(stationary_pt.x, start_y, stationary_pt.x, start_y + sel_length);
				start_y += (sel_length * sel_gap);
			}
			start_x += sel_gap;
			start_y -= sel_gap;
			end_x = start_x;
			end_y = start_y;
			if (isReachBottom() || movingSelectionEnd.y < getClientArea().height) {
				while (end_x < stationary_pt.x) {
					gc.drawLine(end_x, start_y, end_x + sel_length, start_y);
					end_x += (sel_length * sel_gap);
				}
			}
			while (end_y > stationary_pt.y) {
				gc.drawLine(start_x, end_y - sel_length, start_x, end_y);
				end_y -= (sel_length * sel_gap);
			}
			break;
		case NW:
			while (start_x > movingSelectionEnd.x) {
				gc.drawLine(start_x - sel_length, stationary_pt.y, start_x, stationary_pt.y);
				start_x -= (sel_length * sel_gap);
			}
			while (start_y > movingSelectionEnd.y) {
				gc.drawLine(stationary_pt.x, start_y - sel_length, stationary_pt.x, start_y);
				start_y -= (sel_length * sel_gap);
			}
			start_x += sel_gap;
			start_y += sel_gap;
			end_x = start_x;
			end_y = start_y;
			if (isReachTop() || movingSelectionEnd.y > 1) {
				while (end_x < stationary_pt.x) {
					gc.drawLine(end_x, start_y, end_x + sel_length, start_y);
					end_x += (sel_length * sel_gap);
				}
			}
			while (end_y < stationary_pt.y) {
				gc.drawLine(start_x, end_y, start_x, end_y + sel_length);
				end_y += (sel_length * sel_gap);
			}
			break;
		case NE:
			while (start_x < movingSelectionEnd.x) {
				gc.drawLine(start_x, stationary_pt.y, start_x + sel_length, stationary_pt.y);
				start_x += (sel_length * sel_gap);
			}
			while (start_y > movingSelectionEnd.y) {
				gc.drawLine(stationary_pt.x, start_y - sel_length, stationary_pt.x, start_y);
				start_y -= (sel_length * sel_gap);
			}
			start_x -= sel_gap;
			start_y += sel_gap;
			end_x = start_x;
			end_y = start_y;
			if (isReachTop() || movingSelectionEnd.y > 1) {
				while (end_x > stationary_pt.x) {
					gc.drawLine(end_x - sel_length, start_y, end_x, start_y);
					end_x -= (sel_length * sel_gap);
				}
			}
			while (end_y < stationary_pt.y) {
				gc.drawLine(start_x, end_y, start_x, end_y + sel_length);
				end_y += (sel_length * sel_gap);
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
	public void setCursor(Cursor cursor) {
		if (cursor == null) {
			super.setCursor(defaultCursor);
		} else {
			super.setCursor(cursor);
		}
	}
	
	protected void doMouseMoving(Event event) {
		int index = findSelectedIndexByLocation(event.x, event.y, false);
		if (index > -1) {
			setCursor(new Cursor(getDisplay(), SWT.CURSOR_HAND));
			showToolTip(index, event.x, event.y);
			getDisplay().timerExec(5000, new Runnable() {
				public void run() {
					hideToolTip();
				}
			});
		} else {
			setCursor(null);
			hideToolTip();
		}
	}
	protected void resetInfo() {
		hideToolTip();
		mouseDown = false;
		mouseDoubleClick = false;
		movingSelectionStart = null;
		movingSelectionEnd = null;
		max_e_row = -1;
		max_e_col = -1;
		setCursor(null);
	}
	protected void handleDispose(Event event) {
		removeListener(SWT.Dispose, listener);
		event.type = SWT.None;
		resetInfo();
		keyActionMap = null;
		defaultCursor.dispose();
		defaultCursor = null;
		actionListeners.clear();
		total_elements = 0;
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
		if (getTotalElements() == 0 || (event.button != 1) || (IS_CARBON && (event.stateMask & SWT.MOD4) != 0)) {
			return;
		}
		boolean isCtrl = (event.stateMask & SWT.MOD1) != 0;
		boolean isShift = (event.stateMask & SWT.MOD2) != 0;
		if (isShift && isCtrl) {
			isShift = false;
			isCtrl = false;
		}
		if (!isShift && !isCtrl) {
			selectedElements.clear();
			tempSelectedElements.clear();
			selection = null;
		}
		canSelectElements(isShift ? (selection != null ? new Point(selection.x, actualScrollStart_y - verticalScrollOffset) : null) : null, new Point(event.x, event.y));
		//int start_x = Math.min(getMaxCol() * getElementWidth() + e_offset_x, Math.max(0 + sel_size, event.x));
		//int start_y = (Math.min((getMaxRow() - current_top_row) * getElementHeight() + sel_size, Math.max(0 + sel_size, event.y)));
		selection = new Point(event.x, event.y);
		actualScrollStart_y = selection.y + verticalScrollOffset;
		int index = findSelectedIndexByLocation(event.x, event.y, false);
		if (index > -1) {
			fireAction(IIconCanvasActionListener.SELECTION_ACTION, index);
		}
		redraw();
	}
	protected void handleMouseUp(Event event) {
		selectedElements.or(tempSelectedElements);
		if (movingSelectionStart != null) {
			redraw();
		}
		resetInfo();
		endAutoScroll();
	}
	protected void handleMouseDoubleClick(Event event) {
		if (event.button != 1) {
			return;
		}
		resetInfo();
		mouseDoubleClick = true;
		int index = findSelectedIndexByLocation(event.x, event.y, false);
		if (index > -1) {
			fireAction(IIconCanvasActionListener.DOUBLE_CLICK_ACTION, index);
			redraw(index, index);
		}
	}
	protected void handleMouseMove(Event event) {
		if (autoScrollDirection == SWT.NULL)
			doMouseMoving(event);
		if (!mouseDown || mouseDoubleClick || getTotalElements() == 0)
			return;
		if ((event.stateMask & SWT.BUTTON1) == 0) {
			return;
		}
		update();
		doAutoScroll(event.x, event.y);
	}
	protected void handlePaint(Event event) {
		Rectangle clientArea = getClientArea();
		if (getTotalElements() == 0 || event.height == 0 || (clientArea.width == 0 && clientArea.height == 0)) {
			// Check if there is work to do
			return;
		}
		// default
		int renderWidth = clientArea.width;
		int renderHeight = clientArea.height;
		int col_start = 0;
		int col_end = getMaxCol();
		int row_start = 0;
		int row_end = getMaxClientRow();
		if (event.y > 0) {
			Point start_pt = null;
			if (movingSelectionStart != null)
				start_pt = getStartedPoint(movingSelectionStart.x, movingSelectionStart.y);
			else
				start_pt = getStartedPoint(event.x + event.width, event.y + event.height);
			Point end_pt = getFinishedPoint(event.x, event.y);
			int s_x = start_pt.x;
			int e_x = end_pt.x;
			int s_y = start_pt.y;
			int e_y = end_pt.y;
			switch (getDirection(start_pt.x, start_pt.y, end_pt.x, end_pt.y)) {
			case SW:
				s_x = end_pt.x;
				e_x = start_pt.x;
				break;
			case NW:
				s_x = end_pt.x;
				e_x = start_pt.x;
				s_y = end_pt.y;
				e_y = start_pt.y;
				break;
			case NE:
				s_y = end_pt.y;
				e_y = start_pt.y;
				break;
			}
			col_start = Math.max(0, getSelectedCol(s_x - e_offset_x, true));
			col_end = Math.min(getSelectedCol(e_x + event.width - e_offset_x, true) + 1, getMaxCol());
			row_start = Math.max(0, getSelectedRow(s_y - e_offset_y, true));
			row_end = Math.min(getSelectedRow(e_y + event.height - e_offset_y, true) + 1, getMaxRow());
			renderWidth = s_x + e_x + event.width;
			renderHeight = s_y + e_y + event.height;
		} else {
			row_start = Math.max(0, current_top_row);
			row_end = Math.min(row_end + row_start + 1, getMaxRow());
		}
		// System.out.println("performPaint: " + col_start + "," + col_end + " - " + row_start + "," + row_end);
		performPaint(event.gc, col_start, col_end, row_start, row_end, 0, 0, renderWidth, renderHeight);
	}
	protected void handleResize(Event event) {
		int index = selection == null ? -1 : findSelectedIndexByLocation(selection.x, (actualScrollStart_y - verticalScrollOffset), false);
		if (index > -1) {
			selectedElements.clear(index);
			selection = null;
		}
		resetInfo();
		setVerticalScrollBar(getVerticalBar());
		if (!claimBottomFreeSpace())
			calculateTopIndex();
	}
	protected void handleFocusOut(Event event) {
		hideToolTip();
	}
	protected void showToolTip(int index, int mx, int my) {
		if (toolTipProvider == null || index == -1) {
			hideToolTip();
			return;
		}
		if (toolTipShell == null) {
			toolTipShell = new Shell(getShell(), SWT.ON_TOP | SWT.TOOL);
			toolTipLabel = new Label(toolTipShell, SWT.LEFT);
			toolTipLabel.setForeground(tooltip_color_fg);
			toolTipLabel.setBackground(tooltip_color_bg);
		}
		toolTipLabel.setText(getToolTipText(index));
		Point l_size = toolTipLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		l_size.x += 2;
		l_size.y += 2;
		toolTipLabel.setSize(l_size);
		toolTipShell.pack();
		Rectangle area = toolTipShell.getClientArea();
		toolTipLabel.setSize(area.width, area.height);
		// Find out the location to display tooltip
		Point location = findLocation(index);
		Point t_size = toolTipShell.getSize();
		Rectangle clientArea = getClientArea();
		final int gap_x = 5;
		int display_x = location.x + getElementWidth() + gap_x;
		int display_y = location.y + getElementHeight();
		if (display_x + t_size.x > clientArea.width) {
			int lx = location.x - t_size.x - gap_x;
			if (lx > 0)
				display_x = lx; 
		}
		if (display_y > clientArea.height - e_offset_y) {
			display_y = location.y - t_size.y;
		}
		toolTipShell.setLocation(getViewActualLocation(this, null, display_x, display_y));
		toolTipShell.setVisible(true);
	}
	protected void hideToolTip() {
		if (toolTipShell != null) {
			toolTipLabel.dispose();
			toolTipShell.dispose();
			toolTipLabel = null;
			toolTipShell = null;
		}
	}
	protected Point getViewActualLocation(Control from, Control to, int mx, int my) {
		Point mappedpt = getDisplay().map(from, to, 0, 0);
		Point newpt = new Point(mx, my);
		newpt.x += mappedpt.x;
		newpt.y += mappedpt.y;
		return newpt;
	}
	public int[] getSelectedIndexes() {
		int[] retValue = new int[selectedElements.cardinality()];
		for (int i = selectedElements.nextSetBit(0), j = 0; i >= 0; i = selectedElements.nextSetBit(i + 1), j++) {
			retValue[j] = i;
		}
		return retValue;
	}
	protected String getToolTipText(int index) {
		if (toolTipProvider == null)
			return "";
		return toolTipProvider.getToolTipText(index);
	}
	protected Image getStatusIcon(int index, boolean isSelected) {
		if (imageProvider == null)
			return null;
		return imageProvider.getStatusIcon(index, isSelected);
	}
	protected void drawSpecial(int index, GC gc, int x_loc, int y_loc, int width, int height) {
		if (imageProvider != null) {
			imageProvider.drawSpecial(index, gc, x_loc, y_loc, width, height);
		}
	}
	/*******************************************************************************************************************************************************************************************************************************************************************************************************
	 * Self testing
	 ******************************************************************************************************************************************************************************************************************************************************************************************************/
	public static void main(String[] args) {
		final int totalImage = 100000;
        final Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setLocation(0, 0);
        shell.setSize(600, 400);
        shell.setLayout(new FillLayout());

		//File normalFile = new File("D:/eclipse3.1/workspace/org.eclipse.ptp.ui/icons/node/node_running.gif");
		//File selectedFile = new File("D:/eclipse3.1/workspace/org.eclipse.ptp.ui/icons/node/node_running_sel.gif");
		File normalFile = new File("/home/clement/eclipse3.1.1/workspace/org.eclipse.ptp.ui/icons/node/node_running.gif");
		File selectedFile = new File("/home/clement/eclipse3.1.1/workspace/org.eclipse.ptp.ui/icons/node/node_running_sel.gif");
		
		URL normalURL = null;
		URL selectedlURL = null;
		try {
			normalURL = normalFile.toURL();
			selectedlURL = selectedFile.toURL();
		} catch (Exception e) {
			System.out.println("Cannot create the URL: " + e.getMessage());
			return;
		}
		final Image normalImage = ImageDescriptor.createFromURL(normalURL).createImage();
		final Image selectedImage = ImageDescriptor.createFromURL(selectedlURL).createImage();
        
        IconCanvas iconCanvas = new IconCanvas(shell, SWT.NONE);
        iconCanvas.setImageProvider(new IImageProvider() {
        	public Image getStatusIcon(int index, boolean isSelected) {
        		return isSelected?selectedImage:normalImage;
        	}
        	public void drawSpecial(int index, GC gc, int x_loc, int y_loc, int width, int height) {
        	}
        });
        iconCanvas.setToolTipProvider(new IToolTipProvider() {
        	public String getToolTipText(int index) {
        		return " The element: " + index;
        	}
        });
        iconCanvas.setTotal(totalImage);

        shell.open();
        while (!shell.isDisposed()) {
        	if (!display.readAndDispatch())
        		display.sleep();
       	}        
	}
}
