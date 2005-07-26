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
package org.eclipse.ptp.debug.ui.model.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ptp.debug.ui.model.IContainer;
import org.eclipse.ptp.debug.ui.model.IElement;
import org.eclipse.ptp.debug.ui.model.IElementGroup;
import org.eclipse.ptp.debug.ui.model.IGroupManager;

/**
 * @author clement chu
 *
 */
public class GroupManager extends Parent implements IGroupManager {
	public GroupManager() {
		super(GROUP_ROOT_ID, false);
		//create root 
		add(new ElementGroup(GROUP_ROOT_ID));
	}
	public IElementGroup getGroupRoot() {
		if (size() == 0)
			add(new ElementGroup(GROUP_ROOT_ID));
		
		return (IElementGroup)get(GROUP_ROOT_ID);
	}
	public void clearAll() {
		for (Iterator i=elementMap.values().iterator(); i.hasNext();) {
			((IContainer)i.next()).clearAll();
		}
		super.clearAll();
	}
	
	public IElement[] get() {
		return (IElement[])getGroups();
	}		
	public IElementGroup[] getSortedGroups() {
		return (IElementGroup[])getSorted();
	}
	public IElementGroup[] getGroups() {
		return (IElementGroup[])elementMap.values().toArray(new IElementGroup[elementMap.size()]);
	}
	public IElementGroup getGroup(String id) {
		return (IElementGroup)get(id);
	}
	public IElementGroup getGroup(int index) {
		return (IElementGroup)get(index);
	}
	public IElementGroup[] getGroupsWithElement(String id) {
		List aList = new ArrayList();
		IElementGroup[] groups = getSortedGroups();
		for (int i=0; i<groups.length; i++) {
			if (groups[i].contains(id))
				aList.add(groups[i]);
		}
		return (IElementGroup[])aList.toArray(new IElementGroup[aList.size()]);
	}
}
