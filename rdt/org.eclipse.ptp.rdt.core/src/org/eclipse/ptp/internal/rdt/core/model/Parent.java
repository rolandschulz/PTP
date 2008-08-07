/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core.model;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IParent;

public abstract class Parent extends CElement implements IParent {
	private static final long serialVersionUID = 1L;

	protected List<ICElement> fChildren;
	
	public Parent(ICElement parent, int type, String name) {
		super(parent, type, name);
		fChildren = new LinkedList<ICElement>();
	}
	
	public ICElement[] getChildren() throws CModelException {
		return fChildren.toArray(new ICElement[fChildren.size()]);
	}
	
	public List<ICElement> getChildrenOfType(int type) throws CModelException {
		List<ICElement> children = new LinkedList<ICElement>();
		for (ICElement element : fChildren) {
			if (element.getElementType() == type) {
				children.add(element);
			}
		}
		return children;
	}
	
	public boolean hasChildren() {
		return fChildren.size() > 0;
	}
	
	public void addChild(CElement element) {
		fChildren.add(element);
	}

	protected List<ICElement> internalGetChildren() {
		return fChildren;
	}
}
