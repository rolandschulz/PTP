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
 * 
 * Modified by:
 * 		Claudia Konbloch, Forschungszentrum Juelich GmbH
 *******************************************************************************/
package org.eclipse.ptp.rm.lml.ui;

import org.eclipse.ptp.rm.lml.core.ILMLCoreConstants;

public interface ILMLUIConstants extends ILMLCoreConstants{
	public static final String PLUGIN_ID = LMLUIPlugin.getUniqueIdentifier();
	public static final String PREFIX = PLUGIN_ID + "."; //$NON-NLS-1$
	
	public static final String VIEW_PARALLELNODES = PREFIX + "views.NodesView"; //$NON-NLS-1$
	public static final String VIEW_PARALLELNODES2 = PREFIX + "views.NodedisplayView";//$NON-NLS-1$
	public static final String VIEW_PARALLELJOBS = PREFIX + "views.parallelJobsView"; //$NON-NLS-1$
	public static final String VIEW_TABLE_1 = PREFIX + "views.TableView1"; //$NON-NLS-1$
	public static final String VIEW_TABLE_2 = PREFIX + "views.TableView2"; //$NON-NLS-1$
	public static final String VIEW_PARALLELProcess = PREFIX + "views.parallelProcessView"; //$NON-NLS-1$
	public static final String VIEW_LML = PREFIX + "views.LMLView"; //$NON-NLS-1$
	public static final String VIEW_JOB = PREFIX + "view.OwnJobView";//$NON-NLS-1$

	public static final String RM_REMOTE_SERVICES = PREFIX + "remoteServices"; //$NON-NLS-1$
	
	public static final String PERSPECTIVE_RUN = PREFIX + "PTPRunPerspective"; //$NON-NLS-1$

	public static final String SEARCH_PAGE = PREFIX + "PSearchPage"; //$NON-NLS-1$
	public static final String SEARCH_RESULT_PAGE = PREFIX + "PSearchResultPage"; //$NON-NLS-1$
	
    public static final String ACTION_SET = PREFIX + "actionSets"; //$NON-NLS-1$
    public static final String ACTION_SHOW_LEGEND = PREFIX + "actions.showLegendAction"; //$NON-NLS-1$
    
    public static final String IUINAVIGATORGROUP = "navigatorgroup"; //$NON-NLS-1$
    public static final String IUISETGROUP = "setgroup"; //$NON-NLS-1$
    public static final String IUICHANGESETGROUP = "changesetgroup"; //$NON-NLS-1$
    public static final String IUIVIEWGROUP = "viewgroup"; //$NON-NLS-1$
    public static final String IUIZOOMGROUP = "zoomgroup"; //$NON-NLS-1$
    public static final String IUIEMPTYGROUP = "emptygroup"; //$NON-NLS-1$
    public static final String IUIJOBGROUP = "jobgroup"; //$NON-NLS-1$
 	
	public static final int INTERNAL_ERROR = 150;
	public static final int STATUS_CODE_QUESTION = 10000;
	public static final int STATUS_CODE_INFO = 10001;
	public static final int STATUS_CODE_ERROR = 10002;
	
	//preferences
	public static final String VIEW_ICON_SPACING_X = "icon_spacing_x"; //$NON-NLS-1$
	public static final String VIEW_ICON_SPACING_Y = "icon_spacing_y"; //$NON-NLS-1$
	public static final String VIEW_ICON_WIDTH = "icon_width"; //$NON-NLS-1$
	public static final String VIEW_ICON_HEIGHT = "icon_height"; //$NON-NLS-1$
	public static final String VIEW_TOOLTIP_SHOWALLTIME = "tt_show_all_time"; //$NON-NLS-1$
	public static final String VIEW_TOOLTIP_TIMEOUT = "tt_timeout"; //$NON-NLS-1$
	public static final String VIEW_TOOLTIP_ISWRAP = "tt_iswap"; //$NON-NLS-1$
	public static final int DEFAULT_VIEW_ICON_SPACING_X = 4;
	public static final int DEFAULT_VIEW_ICON_SPACING_Y = 4;
	public static final int DEFAULT_VIEW_ICON_WIDTH = 16;
	public static final int DEFAULT_VIEW_ICON_HEIGHT = 16;
	public static final long DEFAULT_VIEW_TOOLTIP = 5000;
	
	public static final int DEFAULT = UNDEFINED;

	public static final String COLOR_WHITE = "SWT.COLOR_WHITE";//$NON-NLS-1$
	public static final String COLOR_BLACK = "SWT.COLOR_BLACK";//$NON-NLS-1$
	public static final String COLOR_RED = "SWT.COLOR_RED";//$NON-NLS-1$
	public static final String COLOR_DARK_RED = "SWT.COLOR_DARK_RED";//$NON-NLS-1$
	public static final String COLOR_GREEN = "SWT.COLOR_GREEN";//$NON-NLS-1$
	public static final String COLOR_DARK_GREEN = "SWT.COLOR_DARK_GREEN";//$NON-NLS-1$
	public static final String COLOR_YELLOW = "SWT.COLOR_YELLOW";//$NON-NLS-1$
	public static final String COLOR_DARK_YELLOW = "SWT.COLOR_DARK_YELLOW";//$NON-NLS-1$
	public static final String COLOR_BLUE = "SWT.COLOR_BLUE";//$NON-NLS-1$
	public static final String COLOR_DARK_BLUE = "SWT.COLOR_DARK_BLUE";//$NON-NLS-1$
	public static final String COLOR_MAGENTA = "SWT.COLOR_MAGENTA";//$NON-NLS-1$
	public static final String COLOR_DARK_MAGENTA = "SWT.COLOR_DARK_MAGENTA";//$NON-NLS-1$
	public static final String COLOR_CYAN = "SWT.COLOR_CYAN";//$NON-NLS-1$
	public static final String COLOR_DARK_CYAN = "SWT.COLOR_DARK_CYAN";//$NON-NLS-1$
	public static final String COLOR_GRAY = "SWT.COLOR_GRAY";//$NON-NLS-1$
	public static final String COLOR_DARK_GRAY = "SWT.COLOR_DARK_GRAY";//$NON-NLS-1$;
	public static final String COLOR_INFO_BACKGROUND = "SWT.COLOR_INFO_BACKGROUND";//$NON-NLS-1$
	public static final String COLOR_INFO_FOREGROUND = "SWT.COLOR_INFO_FOREGROUND";//$NON-NLS-1$
	public static final String COLOR_LIST_BACKGROUND = "SWT.COLOR_LIST_BACKGROUND";//$NON-NLS-1$
	public static final String COLOR_LIST_FOREGROUND = "SWT.COLOR_LIST_FOREGROUND";//$NON-NLS-1$
	public static final String COLOR_LIST_SELECTION = "SWT.COLOR_LIST_SELECTION";//$NON-NLS-1$
	public static final String COLOR_LIST_SELECTION_TEXT = "SWT.COLOR_LIST_SELECTION_TEXT";//$NON-NLS-1$
	public static final String COLOR_TITLE_BACKGROUND = "SWT.COLOR_TITLE_BACKGROUND";//$NON-NLS-1$
	public static final String COLOR_TITLE_BACKGROUND_GRADIENT = "SWT.COLOR_TITLE_BACKGROUND_GRADIENT";//$NON-NLS-1$
	public static final String COLOR_TITLE_FOREGROUND = "SWT.COLOR_TITLE_FOREGROUND";//$NON-NLS-1$
	public static final String COLOR_TITLE_INACTIVE_BACKGROUND = "SWT.COLOR_TITLE_INACTIVE_BACKGROUND";//$NON-NLS-1$
	public static final String COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT = "SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT";//$NON-NLS-1$
	public static final String COLOR_TITLE_INACTIVE_FOREGROUND = "SWT.COLOR_TITLE_INACTIVE_FOREGROUND";//$NON-NLS-1$
	public static final String COLOR_WIDGET_BACKGROUND = "SWT.COLOR_WIDGET_BACKGROUND";//$NON-NLS-1$
	public static final String COLOR_WIDGET_BORDER = "SWT.COLOR_WIDGET_BORDER";//$NON-NLS-1$
	public static final String COLOR_WIDGET_DARK_SHADOW = "SWT.COLOR_WIDGET_DARK_SHADOW";//$NON-NLS-1$
	public static final String COLOR_WIDGET_FOREGROUND = "SWT.COLOR_WIDGET_FOREGROUND";//$NON-NLS-1$
	public static final String COLOR_WIDGET_HIGHLIGHT_SHADOW = "SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW";//$NON-NLS-1$
	public static final String COLOR_WIDGET_LIGHT_SHADOW = "SWT.COLOR_WIDGET_LIGHT_SHADOW";//$NON-NLS-1$
	public static final String COLOR_WIDGET_NORMAL_SHADOW = "SWT.COLOR_WIDGET_NORMAL_SHADOW";//$NON-NLS-1$

	public static final String COURIER = "Courier";//$NON-NLS-1$

	public static final String COLUMN_NAME = "Name";//$NON-NLS-1$
	public static final String COLUMN_VALUE = "Value";//$NON-NLS-1$
	public static final String COLUMN_DEFAULT = "Default";//$NON-NLS-1$
	public static final String COLUMN_DESC = "Description";//$NON-NLS-1$
	public static final String COLUMN_STATUS = "Status";//$NON-NLS-1$
	public static final String COLUMN_TOOLTIP = "Tooltip";//$NON-NLS-1$
	public static final String COLUMN_TYPE = "Type";//$NON-NLS-1$

	public static final String TABLE = "table";//$NON-NLS-1$
	public static final String TREE = "tree";//$NON-NLS-1$
	public static final String LABEL = "label";//$NON-NLS-1$
	public static final String TEXT = "text";//$NON-NLS-1$
	public static final String CHECKBOX = "checkbox";//$NON-NLS-1$
	public static final String SPINNER = "spinner";//$NON-NLS-1$
	public static final String COMBO = "combo";//$NON-NLS-1$
	public static final String RADIOBUTTON = "radioButton";//$NON-NLS-1$
	public static final String BROWSELOCAL = "browseLocal";//$NON-NLS-1$
	public static final String BROWSEREMOTE = "browseRemote";//$NON-NLS-1$

	// SWT
	public static final String ALL = "SWT.ALL";//$NON-NLS-1$
	public static final String ARROW = "SWT.ARROW";//$NON-NLS-1$
	public static final String BACKGROUND = "SWT.BACKGROUND";//$NON-NLS-1$
	public static final String BALLOON = "SWT.BALLOON";//$NON-NLS-1$
	public static final String BAR = "SWT.BAR";//$NON-NLS-1$
	public static final String BEGINNING = "SWT.BEGINNING";//$NON-NLS-1$
	public static final String BORDER = "SWT.BORDER";//$NON-NLS-1$
	public static final String BORDER_DASH = "SWT.BORDER_DASH";//$NON-NLS-1$
	public static final String BORDER_DOT = "SWT.BORDER_DOT";//$NON-NLS-1$
	public static final String BORDER_SOLID = "SWT.BORDER_SOLID";//$NON-NLS-1$
	public static final String BOTTOM = "SWT.BOTTOM";//$NON-NLS-1$
	public static final String CASCADE = "SWT.CASCADE";//$NON-NLS-1$
	public static final String CENTER = "SWT.CENTER";//$NON-NLS-1$
	public static final String CHECK = "SWT.CHECK";//$NON-NLS-1$
	public static final String DIALOG_TRIM = "SWT.DIALOG_TRIM";//$NON-NLS-1$
	public static final String DOWN = "SWT.DOWN";//$NON-NLS-1$
	public static final String DROP_DOWN = "SWT.DROP_DOWN";//$NON-NLS-1$
	public static final String FILL = "SWT.FILL";//$NON-NLS-1$
	public static final String FILL_BOTH = "GridData.FILL_BOTH";//$NON-NLS-1$
	public static final String FILL_EVEN_ODD = "SWT.FILL_EVEN_ODD";//$NON-NLS-1$
	public static final String FILL_HORIZONTAL = "GridData.FILL_HORIZONTAL";//$NON-NLS-1$
	public static final String FILL_VERTICAL = "GridData.FILL_VERTICAL";//$NON-NLS-1$
	public static final String FILL_WINDING = "SWT.FILL_WINDING";//$NON-NLS-1$
	public static final String FOREGROUND = "SWT.FOREGROUND";//$NON-NLS-1$
	public static final String FULL_SELECTION = "SWT.FULL_SELECTION";//$NON-NLS-1$
	public static final String H_SCROLL = "SWT.H_SCROLL";//$NON-NLS-1$
	public static final String HORIZONTAL = "SWT.HORIZONTAL";//$NON-NLS-1$
	public static final String LEAD = "SWT.LEAD";//$NON-NLS-1$
	public static final String LEFT = "SWT.LEFT";//$NON-NLS-1$
	public static final String LEFT_TO_RIGHT = "SWT.LEFT_TO_RIGHT";//$NON-NLS-1$
	public static final String LINE_CUSTOM = "SWT.LINE_CUSTOM";//$NON-NLS-1$
	public static final String LINE_DASH = "SWT.LINE_DASH";//$NON-NLS-1$
	public static final String LINE_DASHDOT = "SWT.LINE_DASHDOT";//$NON-NLS-1$
	public static final String LINE_DASHDOTDOT = "SWT.LINE_DASHDOTDOT";//$NON-NLS-1$
	public static final String LINE_DOT = "SWT.LINE_DOT";//$NON-NLS-1$
	public static final String LINE_SOLID = "SWT.LINE_SOLID";//$NON-NLS-1$
	public static final String MODELESS = "SWT.MODELESS";//$NON-NLS-1$
	public static final String MULTI = "SWT.MULTI";//$NON-NLS-1$
	public static final String NO = "SWT.NO";//$NON-NLS-1$
	public static final String NO_BACKGROUND = "SWT.NO_BACKGROUND";//$NON-NLS-1$
	public static final String NO_FOCUS = "SWT.NO_FOCUS";//$NON-NLS-1$
	public static final String NO_MERGE_PAINTS = "SWT.NO_MERGE_PAINTS";//$NON-NLS-1$
	public static final String NO_RADIO_GROUP = "SWT.NO_RADIO_GROUP";//$NON-NLS-1$
	public static final String NO_REDRAW_RESIZE = "SWT.NO_REDRAW_RESIZE";//$NON-NLS-1$
	public static final String NO_SCROLL = "SWT.NO_SCROLL";//$NON-NLS-1$
	public static final String NO_TRIM = "SWT.NO_TRIM";//$NON-NLS-1$
	public static final String NONE = "SWT.NONE";//$NON-NLS-1$
	public static final String NORMAL = "SWT.NORMAL";//$NON-NLS-1$
	public static final String ON_TOP = "SWT.ON_TOP";//$NON-NLS-1$
	public static final String OPEN = "SWT.OPEN";//$NON-NLS-1$
	public static final String POP_UP = "SWT.POP_UP";//$NON-NLS-1$
	public static final String PRIMARY_MODAL = "SWT.PRIMARY_MODAL";//$NON-NLS-1$
	public static final String PUSH = "SWT.PUSH";//$NON-NLS-1$
	public static final String RADIO = "SWT.RADIO";//$NON-NLS-1$
	public static final String READ_ONLY = "SWT.READ_ONLY";//$NON-NLS-1$
	public static final String RESIZE = "SWT.RESIZE";//$NON-NLS-1$
	public static final String RIGHT = "SWT.RIGHT";//$NON-NLS-1$
	public static final String RIGHT_TO_LEFT = "SWT.RIGHT_TO_LEFT";//$NON-NLS-1$
	public static final String SCROLL_LINE = "SWT.SCROLL_LINE";//$NON-NLS-1$
	public static final String SCROLL_LOCK = "SWT.SCROLL_LOCK";//$NON-NLS-1$
	public static final String SCROLL_PAGE = "SWT.SCROLL_PAGE";//$NON-NLS-1$
	public static final String SHADOW_ETCHED_IN = "SWT.SHADOW_ETCHED_IN";//$NON-NLS-1$
	public static final String SHADOW_ETCHED_OUT = "SWT.SHADOW_ETCHED_OUT";//$NON-NLS-1$
	public static final String SHADOW_IN = "SWT.SHADOW_IN";//$NON-NLS-1$
	public static final String SHADOW_NONE = "SWT.SHADOW_NONE";//$NON-NLS-1$
	public static final String SHADOW_OUT = "SWT.SHADOW_OUT";//$NON-NLS-1$
	public static final String SHELL_TRIM = "SWT.SHELL_TRIM";//$NON-NLS-1$
	public static final String SHORT = "SWT.SHORT";//$NON-NLS-1$
	public static final String SIMPLE = "SWT.SIMPLE";//$NON-NLS-1$
	public static final String SINGLE = "SWT.SINGLE";//$NON-NLS-1$
	public static final String SMOOTH = "SWT.SMOOTH";//$NON-NLS-1$
	public static final String TITLE = "SWT.TITLE";//$NON-NLS-1$
	public static final String TOGGLE = "SWT.TOGGLE";//$NON-NLS-1$
	public static final String TOP = "SWT.TOGGLE";//$NON-NLS-1$
	public static final String UP = "SWT.UP";//$NON-NLS-1$
	public static final String V_SCROLL = "SWT.V_SCROLL";//$NON-NLS-1$
	public static final String VERTICAL = "SWT.VERTICAL";//$NON-NLS-1$
	public static final String WRAP = "SWT.WRAP";//$NON-NLS-1$
	public static final String YES = "SWT.YES";//$NON-NLS-1$
}
