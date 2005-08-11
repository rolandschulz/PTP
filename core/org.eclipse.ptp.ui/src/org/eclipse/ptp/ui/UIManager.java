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
		IElementSet set = setManager.getSet(setID);
		addToSet(elements, set);
		updateMatchElementSets(set, setManager);
	}
	public String createSet(IElement[] elements, String setID, String setName, ISetManager setManager) {
		IElementSet set = new ElementSet(setID, setName, true);
		addToSet(elements, set);
		setManager.add(set);
		updateMatchElementSets(set, setManager);
		return set.getID();
	}
	public void removeSet(String setID, ISetManager setManager) {
		IElementSet set = setManager.getSet(setID);
		String[] sets = set.getMatchSets();
		for (int i=0; i<sets.length; i++) {
			setManager.getSet(sets[i]).removeMatchSet(setID);
		}
		setManager.remove(setID);
	}
	public void removeFromSet(IElement[] elements, String setID, ISetManager setManager) {
		IElementSet set = setManager.getSet(setID);
		for (int i=0; i<elements.length; i++) {
			set.remove(elements[i]);
		}
		updateMatchElementSets(set, setManager);
	}
	public void updateMatchElementSets(IElementSet targetSet, ISetManager setManager) {
		IElementSet[] sets = setManager.getSortedSets();
		for (int i=1; i<sets.length; i++) {
			if (sets[i].getID().equals(targetSet.getID()))
				continue;
			
			IElement[] elements = sets[i].getElements();
			for (int j=0; j<elements.length; j++) {
				if (targetSet.contains(elements[j].getID())) {
					targetSet.addMatchSet(sets[i].getID());
					sets[i].addMatchSet(targetSet.getID());
					break;
				}
			}
		}
	}
}
