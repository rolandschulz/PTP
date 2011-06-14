/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    IBM Corporation
 *******************************************************************************/ 

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.typehierarchy.THGraphNode
 * Version: 1.7
 */
package org.eclipse.ptp.internal.rdt.core.typehierarchy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.model.ICElement;

public class THGraphNode implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private List<THGraphEdge> fOutgoing= Collections.emptyList();
	private List<THGraphEdge> fIncoming= Collections.emptyList();
	private ICElement fElement;
	private String fPath;
	private ICElement[] fMembers= null;
	
	public THGraphNode(ICElement element, String path) {
		fElement= element;
		fPath = path;
	}
	
	public String getPath() {
		return fPath;
	}

	public void startEdge(THGraphEdge outgoing) {
		fOutgoing= addElement(fOutgoing, outgoing);
	}

	public void endEdge(THGraphEdge incoming) {
		fIncoming= addElement(fIncoming, incoming);
	}
	
	public ICElement getElement() {
		return fElement;
	}

	private List<THGraphEdge> addElement(List<THGraphEdge> list, THGraphEdge elem) {
		switch (list.size()) {
		case 0:
			return Collections.singletonList(elem);
		case 1:
			list= new ArrayList<THGraphEdge>(list);
			list.add(elem);
			return list;
		}
		list.add(elem);
		return list;
	}

	public List<THGraphEdge> getOutgoing() {
		return fOutgoing;
	}
	
	public List<THGraphEdge> getIncoming() {
		return fIncoming;
	}

	public void setMembers(ICElement[] array) {
		fMembers= array;
	}
	
	public ICElement[] getMembers(boolean addInherited) {
		if (!addInherited) {
			return fMembers;
		}
		ArrayList<ICElement> list= new ArrayList<ICElement>();
		collectMembers(new HashSet<THGraphNode>(), list);
		return list.toArray(new ICElement[list.size()]);
	}

	private void collectMembers(HashSet<THGraphNode> visited, List<ICElement> list) {
		if (visited.add(this)) {
			if (fMembers != null) {
				list.addAll(Arrays.asList(fMembers));
			}
			List<THGraphEdge> bases= getOutgoing();
			for (Iterator<THGraphEdge> iterator = bases.iterator(); iterator.hasNext();) {
				THGraphEdge edge = iterator.next();
				edge.getEndNode().collectMembers(visited, list);
			}
		}
	}
	
	@Override
	public String toString() {
		return fElement == null ? null : fElement.toString();
	}
}
