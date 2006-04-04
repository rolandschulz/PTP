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
package org.eclipse.ptp.ui;

/**
 * @author Clement chu
 *
 */
public interface IPTPUIConstants {
	public static final String PLUGIN_ID = PTPUIPlugin.getUniqueIdentifier();
	public static final String PREFIX = PLUGIN_ID + ".";
	
	public static final String VIEW_PARALLELMACHINE = PREFIX + "views.parallelMachineView";
	public static final String VIEW_PARALLELJOB = PREFIX + "views.parallelJobView";
	public static final String VIEW_PARALLELProcess = PREFIX + "views.parallelProcessView";

	public static final String PERSPECTIVE_RUN = PREFIX + "PTPRunPerspective";

	public static final String SEARCH_PAGE = PREFIX + "PSearchPage";
	public static final String SEARCH_RESULT_PAGE = PREFIX + "PSearchResultPage";
	
    public static final String ACTION_SET = PREFIX + "actionSets";
    public static final String ACTION_SHOW_LEGEND = PREFIX + "actions.showLegendAction";
    
    public static final String IUINAVIGATORGROUP = "navigatorgroup";
    public static final String IUIACTIONGROUP = "actiongroup";
    public static final String IUISETGROUP = "setgroup";
    public static final String IUICHANGESETGROUP = "changesetgroup";
    public static final String IUIVIEWGROUP = "viewgroup";
    public static final String IUIEMPTYGROUP = "emptygroup";
    
	public final static int NODE_USER_ALLOC_EXCL = 0;
	public final static int NODE_USER_ALLOC_SHARED = 1;
	public final static int NODE_OTHER_ALLOC_EXCL = 2;
	public final static int NODE_OTHER_ALLOC_SHARED = 3;
	public final static int NODE_DOWN = 4;
	public final static int NODE_ERROR = 5;
	public final static int NODE_EXITED = 6;
	public final static int NODE_RUNNING = 7;
	public final static int NODE_UNKNOWN = 8;
	public final static int NODE_UP = 9;
	
	public final static int PROC_ERROR = 0;
	public final static int PROC_EXITED = 1;
	public final static int PROC_EXITED_SIGNAL = 2;
	public final static int PROC_RUNNING = 3;
	public final static int PROC_STARTING = 4;
	public final static int PROC_STOPPED = 5;
	
	public static final int INTERNAL_ERROR = 150;
	public static final int STATUS_CODE_QUESTION = 10000;
	public static final int STATUS_CODE_INFO = 10001;
	public static final int STATUS_CODE_ERROR = 10002;
	
	//preferences
	public static final String VIEW_ICON_SPACING_X = "icon_spacing_x";
	public static final String VIEW_ICON_SPACING_Y = "icon_spacing_y";
	public static final String VIEW_ICON_WIDTH = "icon_width";
	public static final String VIEW_ICON_HEIGHT = "icon_height";
	public static final String VIEW_TOOLTIP_SHOWALLTIME = "tt_show_all_time";
	public static final String VIEW_TOOLTIP_TIMEOUT = "tt_timeout";
	public static final String VIEW_TOOLTIP_ISWRAP = "tt_iswap";
	public static final int DEFAULT_VIEW_ICON_SPACING_X = 4;
	public static final int DEFAULT_VIEW_ICON_SPACING_Y = 4;
	public static final int DEFAULT_VIEW_ICON_WIDTH = 16;
	public static final int DEFAULT_VIEW_ICON_HEIGHT = 16;
	public static final long DEFAULT_VIEW_TOOLTIP = 5000;
}
