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
package org.eclipse.ptp.internal.ui.hover;
/**
 * @author Clement chu
 * 
 */
public interface IIconHoverTag {
	public final static String UNDERLINE_TAG = "u"; //$NON-NLS-1$
	public final static String STRIKE_TAG = "s"; //$NON-NLS-1$
	public final static String BOLD_TAG = "b"; //$NON-NLS-1$
	public final static String ITALIC_TAG = "i"; //$NON-NLS-1$
	public final static String P_TAG = "p"; //$NON-NLS-1$
	public final static String KEY_TAG = "key"; //$NON-NLS-1$
	public final static String HIGHLIGHT_TAG = "hl"; //$NON-NLS-1$
	public final static String INDENT_TAG = "ind"; //$NON-NLS-1$
	public final static String NEXT_LINE_TAG = "br"; //$NON-NLS-1$
		
	public final static char CLOSED_TAG = '/';
	public final static char START_TAG = '<';
	public final static char END_TAG = '>';
}
