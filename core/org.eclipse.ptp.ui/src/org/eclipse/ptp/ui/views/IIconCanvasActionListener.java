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

import java.util.BitSet;

/**
 * @author Clement chu
 * 
 */
public interface IIconCanvasActionListener {
	public static final int COPY_ACTION = 1;
	public static final int CUT_ACTION = 2;
	public static final int PASTE_ACTION = 3;
	public static final int DELETE_ACTION = 4;
	public static final int DOUBLE_CLICK_ACTION = 5;

	// public static final int SELECTION_ACTION = 6;

	/**
	 * Handle action
	 * 
	 * @param type
	 *            action type
	 * @param elements
	 *            BitSet containing element indexes
	 * @since 7.0
	 */
	public void handleAction(int type, BitSet elements);

	/**
	 * Handle action
	 * 
	 * @param type
	 *            action type
	 * @param index
	 *            element index
	 */
	public void handleAction(int type, int index);
}
