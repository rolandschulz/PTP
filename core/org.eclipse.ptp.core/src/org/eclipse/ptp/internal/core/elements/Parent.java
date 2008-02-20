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
package org.eclipse.ptp.internal.core.elements;

import java.util.Arrays;

import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.elementcontrols.IPElementControl;
import org.eclipse.ptp.core.elements.IPElement;

/**
 *  
 */
public abstract class Parent extends PElement {
	/**
	 * @param elements
	 */
	public static void sort(IPElementControl elements[]) {
		Arrays.sort(elements);
	}

	public Parent(String id, IPElementControl parent, int type, IAttribute<?,?,?>[] attrs) {
		super(id, parent, type, attrs);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elementcontrols.IPElementControl#hasChildren()
	 */
	public boolean hasChildren() {
		return getElementInfo().hasChildren();
	}

	/**
	 * @param member
	 */
	protected void addChild(IPElementControl member) {
		getElementInfo().addChild(member);
	}

	/**
	 * @param key
	 * @return
	 */
	protected IPElementControl findChild(String key) {
		return getElementInfo().findChild(key);
	}

	/**
	 * @return
	 */
	protected IPElementControl[] getChildren() {
		return getElementInfo().getChildren();
	}

	/**
	 * @param member
	 */
	protected void removeChild(IPElement member) {
		getElementInfo().removeChild(member);
	}

	/**
	 * 
	 */
	protected void removeChildren() {
		getElementInfo().removeChildren();
	}
}