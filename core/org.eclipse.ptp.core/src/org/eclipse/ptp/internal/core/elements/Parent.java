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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.elementcontrols.IPElementControl;
import org.eclipse.ptp.core.elements.IPElement;

/**
 *  
 */
public abstract class Parent extends PElement {
	public static void sort(IPElementControl elements[]) {
		Arrays.sort(elements);
	}

	public Parent(String id, IPElementControl parent, int type, IAttribute[] attrs) {
		super(id, parent, type, attrs);
	}

	public boolean hasChildren() {
		return getElementInfo().hasChildren();
	}

	protected void addChild(IPElementControl member) {
		getElementInfo().addChild(member);
	}

	protected IPElementControl findChild(String key) {
		return getElementInfo().findChild(key);
	}

	protected IPElementControl[] getChildren() {
		return getElementInfo().getChildren();
	}

	protected List<PElement> getChildrenOfType(int type) {
		IPElementControl[] children = getChildren();
		int size = children.length;
		ArrayList<PElement> list = new ArrayList<PElement>(size);
		for (int i = 0; i < size; ++i) {
			PElement elt = (PElement) children[i];
			if (elt.getElementType() == type) {
				list.add(elt);
			}
		}
		return list;
	}

	protected Collection<IPElementControl> getCollection() {
		return getElementInfo().getCollection();
	}

	protected void removeChild(IPElement member) {
		getElementInfo().removeChild(member);
	}

	protected void removeChildren() {
		getElementInfo().removeChildren();
	}
}