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
package org.eclipse.ptp.internal.ui.actions;

import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ptp.internal.ui.ParallelImages;
import org.eclipse.ptp.ui.actions.ParallelAction;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.old.UIUtils;
import org.eclipse.ptp.ui.views.AbstractParallelElementView;

/**
 * @author clement chu
 *
 */
public class DeleteProcessAction extends ParallelAction {
	public static final String name = "Delete Element";
	
	public DeleteProcessAction(AbstractParallelElementView view) {
		super(name, view);
	    setImageDescriptor(ParallelImages.ID_ICON_DELETEPROCESS_NORMAL);
	    setDisabledImageDescriptor(ParallelImages.ID_ICON_DELETEPROCESS_DISABLE);
	}

	public void run(IElement[] elements) {
		if (validation(elements)) {
			IElementSet set = view.getCurrentSet();
			if (set.size() == elements.length) {
				callDeleteGroupAction();
			} else {
				IElementHandler setManager = view.getCurrentElementHandler();
				if (setManager == null)
					return;

				if (UIUtils.showQuestionDialog("Delete Confirmation", "Are you sure you want to delete (" + elements.length + ") element from this set?"))	{			
					view.getUIManger().removeFromSet(elements, set.getID(), setManager);
					view.selectSet(setManager.getSet(set.getID()));
					view.updateTitle();
					view.refresh();
				}
			}
		}		
	}
	
	private void callDeleteGroupAction() {
		IToolBarManager manager = view.getViewSite().getActionBars().getToolBarManager();
		IContributionItem item = manager.find(DeleteSetAction.name);
		if (item != null && item instanceof ActionContributionItem) {
			((ActionContributionItem)item).getAction().run();
		}
	}
}
