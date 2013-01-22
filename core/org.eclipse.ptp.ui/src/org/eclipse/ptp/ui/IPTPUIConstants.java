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
	public static final String PREFIX = PLUGIN_ID + "."; //$NON-NLS-1$

	public static final String IUINAVIGATORGROUP = "navigatorgroup"; //$NON-NLS-1$
	public static final String IUIACTIONGROUP = "actiongroup"; //$NON-NLS-1$
	public static final String IUISETGROUP = "setgroup"; //$NON-NLS-1$
	public static final String IUICHANGESETGROUP = "changesetgroup"; //$NON-NLS-1$
	public static final String IUIVIEWGROUP = "viewgroup"; //$NON-NLS-1$
	public static final String IUIZOOMGROUP = "zoomgroup"; //$NON-NLS-1$
	public static final String IUIEMPTYGROUP = "emptygroup"; //$NON-NLS-1$

	public static final int INTERNAL_ERROR = 150;
	public static final int STATUS_CODE_QUESTION = 10000;
	public static final int STATUS_CODE_INFO = 10001;
	public static final int STATUS_CODE_ERROR = 10002;

	// preferences
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
}
