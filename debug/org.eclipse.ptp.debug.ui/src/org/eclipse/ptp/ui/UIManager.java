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
package org.eclipse.ptp.ui;

import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementGroup;
import org.eclipse.ptp.ui.model.IGroupManager;
import org.eclipse.ptp.ui.model.internal.ElementGroup;
import org.eclipse.ptp.ui.model.internal.GroupManager;

/**
 * @author clement chu
 *
 */
public class UIManager {
	protected IModelManager modelManager = null;
	private IGroupManager groupManager = null;
	
	public UIManager() {
		groupManager = new GroupManager();
		modelManager = PTPCorePlugin.getDefault().getModelManager();
	}
	public void shutdown() {
		groupManager.clearAll();
	}	
	public IGroupManager getGroupManager() {
		return groupManager;
	}	
	public IModelManager getModelManager() {
		return modelManager;
	}
	
	private void addToGroup(IElement[] elements, IElementGroup group) {
		for (int i=0; i<elements.length; i++) {
			group.add(elements[i].cloneElement());
		}
	}
	public void addToGroup(IElement[] elements, String groupID) {
		addToGroup(elements, groupManager.getGroup(groupID));
	}
	public String createGroup(IElement[] elements) {
		IElementGroup group = new ElementGroup(true);
		addToGroup(elements, group);
		groupManager.add(group);
		return group.getID();
	}
	public void removeGroup(String groupID) {
		groupManager.remove(groupID);
	}
	public void removeFromGroup(IElement[] elements, String groupID) {
		IElementGroup group = groupManager.getGroup(groupID);
		for (int i=0; i<elements.length; i++) {
			group.remove(elements[i]);
		}
	}	
}
