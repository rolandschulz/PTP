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
package org.eclipse.ptp.ui.actions;

import org.eclipse.ptp.debug.ui.ImageUtil;
import org.eclipse.ptp.ui.UIManager;
import org.eclipse.ptp.ui.UIUtils;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.ptp.ui.views.AbstractParallelElementView;

/**
 * @author clement chu
 *
 */
public class DeleteSetAction extends ParallelAction {
	public static final String name = "Delete Set";
	
	public DeleteSetAction(AbstractParallelElementView view) {
		super(name, view);
	    setImageDescriptor(ImageUtil.ID_ICON_DELETESET_NORMAL);
	    setDisabledImageDescriptor(ImageUtil.ID_ICON_DELETESET_DISABLE);
	}

	public void run(IElement[] elements) {}
	public void run() {
		IElementSet set = view.getCurrentSet();
		if (set != null && set.size() > 0) {
			if (UIUtils.showQuestionDialog(name + " " + set.getID() + " Confirmation", "Are you sure you want to delete all elements in this set?")) {
				UIManager uiManager = view.getUIManger();
				uiManager.removeSet(set.getID());
						
				IElementSet[] sets = uiManager.getSetManager().getSortedSets();
				if (sets.length > 0) {
					IElementSet lastSet = sets[sets.length-1];
					view.selectSet(lastSet.getID());
					view.getCurrentSet().setAllSelect(false);
					view.update();
					view.redraw();
				}
			}
		}		
	}
}
