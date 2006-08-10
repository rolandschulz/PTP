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
package org.eclipse.ptp.debug.ui.model;

import org.eclipse.ptp.ui.model.Element;
import org.eclipse.ptp.ui.model.IElement;

/**
 * @author Clement
 */
public class DebugElement extends Element {
	public static final int NOTHING = 0;
	public static final int VALUE_DIFF = 1;
	protected int type = NOTHING;
	
	/** Constructor
	 * @param parent Parent element
	 * @param id element ID
	 * @param name element name
	 */
	public DebugElement(IElement parent, String id, String name) {
		super(parent, id, name);
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getType() {
		return type;
	}
	public void resetType() {
		type = NOTHING;
	}
}
