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
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.ptp.ui.model.ISetManager;
import org.eclipse.ptp.ui.model.internal.ElementSet;

/**
 * @author clement chu
 *
 */
public class UIManager {
	protected IModelManager modelManager = null;
	
	public UIManager() {
		modelManager = PTPCorePlugin.getDefault().getModelManager();
	}
	public IModelManager getModelManager() {
		return modelManager;
	}
	
	private void addToSet(IElement[] elements, IElementSet set) {
		for (int i=0; i<elements.length; i++) {
			set.add(elements[i]);
		}
	}
	public void addToSet(IElement[] elements, String setID, ISetManager setManager) {
		addToSet(elements, setManager.getSet(setID));
	}
	public String createSet(IElement[] elements, String setID, ISetManager setManager) {
		IElementSet set = new ElementSet(setID, true);
		addToSet(elements, set);
		setManager.add(set);
		return set.getID();
	}
	public void removeSet(String setID, ISetManager setManager) {
		setManager.remove(setID);
	}
	public void removeFromSet(IElement[] elements, String setID, ISetManager setManager) {
		IElementSet set = setManager.getSet(setID);
		for (int i=0; i<elements.length; i++) {
			set.remove(elements[i]);
		}
	}	
}
