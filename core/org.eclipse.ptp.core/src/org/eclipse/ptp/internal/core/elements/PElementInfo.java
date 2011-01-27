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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ptp.core.elements.IPElement;

/**
 *
 */
public class PElementInfo {
	private final Map<String, IPElement> fChildren = Collections.synchronizedMap(new HashMap<String, IPElement>());
	protected final IPElement element;

	public PElementInfo(IPElement element) {
		this.element = element;
	}

	/**
	 * Add a child to this element
	 * 
	 * @param member
	 */
	public void addChild(IPElement member) {
		fChildren.put(member.getID(), member);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new Error();
		}
	}

	/**
	 * Find child with corresponding key
	 * 
	 * @param key
	 * @return child
	 */
	public IPElement findChild(String key) {
		synchronized (fChildren) {
			if (fChildren.containsKey(key)) {
				return fChildren.get(key);
			}
		}
		return null;
	}

	/**
	 * Get all children of this element
	 * 
	 * @return children
	 */
	public IPElement[] getChildren() {
		return fChildren.values().toArray(new IPElement[size()]);
	}

	/**
	 * Get the element
	 * 
	 * @return element
	 */
	public IPElement getElement() {
		return element;
	}

	/**
	 * Check if this element has any children
	 * 
	 * @return true if we have children
	 */
	public boolean hasChildren() {
		return size() > 0;
	}

	/**
	 * Check if child is one of ours
	 * 
	 * @param child
	 * @return true if child is one of ours
	 */
	public boolean includesChild(IPElement child) {
		if (fChildren.containsKey(child.getID())) {
			return true;
		}
		return false;
	}

	/**
	 * Remove child
	 * 
	 * @param member
	 */
	public void removeChild(IPElement member) {
		fChildren.remove(member.getID());
	}

	/**
	 * Remove all children
	 */
	public void removeChildren() {
		fChildren.clear();
	}

	/**
	 * Add children
	 * 
	 * @param children
	 */
	public void setChildren(IPElement[] children) {
		for (IPElement element : children) {
			fChildren.put(element.getID(), element);
		}
	}

	/**
	 * Return number of children
	 * 
	 * @return number of children
	 */
	public int size() {
		return fChildren.size();
	}
}
