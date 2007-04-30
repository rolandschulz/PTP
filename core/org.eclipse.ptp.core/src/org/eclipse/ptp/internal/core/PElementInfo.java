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
package org.eclipse.ptp.internal.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ptp.core.elementcontrols.IPElementControl;
import org.eclipse.ptp.core.elements.IPElement;

/**
 *
 */
public class PElementInfo {
	private Map<String, IPElementControl> fChildren = new HashMap<String, IPElementControl>();

	protected IPElementControl element;

	public PElementInfo(IPElementControl element) {
		this.element = element;
	}

	public IPElementControl getElement() {
		return element;
	}

	public void addChild(IPElementControl member) {
		fChildren.put(member.getID(), member);
	}

	public void removeChild(IPElement member) {
		fChildren.remove(member.getID());
	}

	public IPElementControl findChild(String key) {
		if (fChildren.containsKey(key))
			return (IPElementControl) fChildren.get(key);
		return null;
	}

	public IPElementControl[] getChildren() {
		synchronized (fChildren) {
			 return (IPElementControl[]) fChildren.values().toArray(
					new IPElementControl[size()]);
		}
	}

	public Collection<IPElementControl> getCollection() {
		synchronized (fChildren) {
			return fChildren.values();
		}
	}

	public boolean includesChild(IPElementControl child) {
		if (fChildren.containsKey(child.getID()))
			return true;
		return false;
	}

	public void removeChildren() {
		fChildren.clear();
	}

	public void setChildren(IPElementControl[] children) {
		for (IPElementControl element : children) {
			fChildren.put(element.getID(), element);
		}
	}

	public boolean hasChildren() {
		return size() > 0;
	}

	public int size() {
		return fChildren.size();
	}

	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new Error();
		}
	}
}
