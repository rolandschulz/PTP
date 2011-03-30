package org.eclipse.ptp.rm.jaxb.ui;

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public interface IJAXBUINonNLSConstants extends IJAXBNonNLSConstants {

	int DEFAULT = UNDEFINED;

	Color DKBL = Display.getDefault().getSystemColor(SWT.COLOR_DARK_BLUE);
	Color DKMG = Display.getDefault().getSystemColor(SWT.COLOR_DARK_MAGENTA);
	Color DKRD = Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED);

	String COURIER = "Courier";//$NON-NLS-1$

	String COLUMN_NAME = "Name";//$NON-NLS-1$
	String COLUMN_VALUE = "Value";//$NON-NLS-1$
	String COLUMN_DEFAULT = "Default";//$NON-NLS-1$
	String COLUMN_DESC = "Description";//$NON-NLS-1$
	String COLUMN_STATUS = "Status";//$NON-NLS-1$
	String COLUMN_TOOLTIP = "Tooltip";//$NON-NLS-1$
	String COLUMN_TYPE = "Type";//$NON-NLS-1$

	String TABLE = "table";//$NON-NLS-1$
	String TREE = "tree";//$NON-NLS-1$
	String LABEL = "label";//$NON-NLS-1$
	String TEXT = "text";//$NON-NLS-1$
	String CHECKBOX = "checkbox";//$NON-NLS-1$
	String SPINNER = "spinner";//$NON-NLS-1$
	String COMBO = "combo";//$NON-NLS-1$
	String RADIOBUTTON = "radioButton";//$NON-NLS-1$
	String BROWSELOCAL = "browseLocal";//$NON-NLS-1$
	String BROWSEREMOTE = "browseRemote";//$NON-NLS-1$

	// SWT
	String ALL = "ALL";//$NON-NLS-1$
	String ARROW = "ARROW";//$NON-NLS-1$
	String BACKGROUND = "BACKGROUND";//$NON-NLS-1$
	String BALLOON = "BALLOON";//$NON-NLS-1$
	String BAR = "BAR";//$NON-NLS-1$
	String BEGINNING = "BEGINNING";//$NON-NLS-1$
	String BORDER = "BORDER";//$NON-NLS-1$
	String BORDER_DASH = "BORDER_DASH";//$NON-NLS-1$
	String BORDER_DOT = "BORDER_DOT";//$NON-NLS-1$
	String BORDER_SOLID = "BORDER_SOLID";//$NON-NLS-1$
	String BOTTOM = "BOTTOM";//$NON-NLS-1$
	String CASCADE = "BOTTOM";//$NON-NLS-1$
	String CENTER = "CENTER";//$NON-NLS-1$
	String CHECK = "CHECK";//$NON-NLS-1$
	String DIALOG_TRIM = "DIALOG_TRIM";//$NON-NLS-1$
	String DOWN = "DOWN";//$NON-NLS-1$
	String DROP_DOWN = "DROP_DOWN";//$NON-NLS-1$
	String FILL = "FILL";//$NON-NLS-1$
	String FILL_EVEN_ODD = "FILL_EVEN_ODD";//$NON-NLS-1$
	String FILL_WINDING = "FILL_WINDING";//$NON-NLS-1$
	String FOREGROUND = "FOREGROUND";//$NON-NLS-1$
	String FULL_SELECTION = "FULL_SELECTION";//$NON-NLS-1$
	String H_SCROLL = "H_SCROLL";//$NON-NLS-1$
	String HORIZONTAL = "HORIZONTAL";//$NON-NLS-1$
	String LEAD = "LEAD";//$NON-NLS-1$
	String LEFT = "LEFT";//$NON-NLS-1$
	String LEFT_TO_RIGHT = "LEFT_TO_RIGHT";//$NON-NLS-1$
	String LINE_CUSTOM = "LINE_CUSTOM";//$NON-NLS-1$
	String LINE_DASH = "LINE_DASH";//$NON-NLS-1$
	String LINE_DASHDOT = "LINE_DASHDOT";//$NON-NLS-1$
	String LINE_DASHDOTDOT = "LINE_DASHDOTDOT";//$NON-NLS-1$
	String LINE_DOT = "LINE_DOT";//$NON-NLS-1$
	String LINE_SOLID = "LINE_SOLID";//$NON-NLS-1$
	String MODELESS = "MODELESS";//$NON-NLS-1$
	String MULTI = "MULTI";//$NON-NLS-1$
	String NO = "NO";//$NON-NLS-1$
	String NO_BACKGROUND = "NO_BACKGROUND";//$NON-NLS-1$
	String NO_FOCUS = "NO_FOCUS";//$NON-NLS-1$
	String NO_MERGE_PAINTS = "NO_MERGE_PAINTS";//$NON-NLS-1$
	String NO_RADIO_GROUP = "NO_RADIO_GROUP";//$NON-NLS-1$
	String NO_REDRAW_RESIZE = "NO_REDRAW_RESIZE";//$NON-NLS-1$
	String NO_SCROLL = "NO_SCROLL";//$NON-NLS-1$
	String NO_TRIM = "NO_TRIM";//$NON-NLS-1$
	String NONE = "NONE";//$NON-NLS-1$
	String NORMAL = "NORMAL";//$NON-NLS-1$
	String ON_TOP = "ON_TOP";//$NON-NLS-1$
	String OPEN = "OPEN";//$NON-NLS-1$
	String POP_UP = "POP_UP";//$NON-NLS-1$
	String PRIMARY_MODAL = "PRIMARY_MODAL";//$NON-NLS-1$
	String PUSH = "PUSH";//$NON-NLS-1$
	String RADIO = "RADIO";//$NON-NLS-1$
	String READ_ONLY = "READ_ONLY";//$NON-NLS-1$
	String RESIZE = "RESIZE";//$NON-NLS-1$
	String RIGHT = "RIGHT";//$NON-NLS-1$
	String RIGHT_TO_LEFT = "RIGHT_TO_LEFT";//$NON-NLS-1$
	String SCROLL_LINE = "SCROLL_LINE";//$NON-NLS-1$
	String SCROLL_LOCK = "SCROLL_LOCK";//$NON-NLS-1$
	String SCROLL_PAGE = "SCROLL_PAGE";//$NON-NLS-1$
	String SHADOW_ETCHED_IN = "SHADOW_ETCHED_IN";//$NON-NLS-1$
	String SHADOW_ETCHED_OUT = "SHADOW_ETCHED_OUT";//$NON-NLS-1$
	String SHADOW_IN = "SHADOW_IN";//$NON-NLS-1$
	String SHADOW_NONE = "SHADOW_NONE";//$NON-NLS-1$
	String SHADOW_OUT = "SHADOW_OUT";//$NON-NLS-1$
	String SHELL_TRIM = "SHELL_TRIM";//$NON-NLS-1$
	String SHORT = "SHORT";//$NON-NLS-1$
	String SIMPLE = "SIMPLE";//$NON-NLS-1$
	String SINGLE = "SINGLE";//$NON-NLS-1$
	String SMOOTH = "SMOOTH";//$NON-NLS-1$
	String TITLE = "TITLE";//$NON-NLS-1$
	String TOGGLE = "TOGGLE";//$NON-NLS-1$
	String TOP = "TOGGLE";//$NON-NLS-1$
	String UP = "UP";//$NON-NLS-1$
	String V_SCROLL = "V_SCROLL";//$NON-NLS-1$
	String VERTICAL = "VERTICAL";//$NON-NLS-1$
	String WRAP = "WRAP";//$NON-NLS-1$
	String YES = "YES";//$NON-NLS-1$
}
