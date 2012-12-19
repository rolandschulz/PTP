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

import java.util.BitSet;

import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.internal.ui.ParallelImages;
import org.eclipse.ptp.ui.IElementManager;
import org.eclipse.ptp.ui.UIUtils;
import org.eclipse.ptp.ui.actions.ParallelAction;
import org.eclipse.ptp.ui.messages.Messages;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.ptp.ui.views.AbstractParallelElementView;

/**
 * @author clement chu
 * 
 */
public class DeleteSetAction extends ParallelAction {
	public static final String name = Messages.DeleteSetAction_0;

	/**
	 * Constructor
	 * 
	 * @param view
	 */
	public DeleteSetAction(AbstractParallelElementView view) {
		super(name, view);
		setImageDescriptor(ParallelImages.ID_ICON_DELETESET_NORMAL);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.actions.ParallelAction#run(java.util.BitSet)
	 */
	@Override
	public void run(BitSet elements) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.IAction#run()
	 */

	@Override
	public void run() {
		IElementSet set = view.getCurrentSet();
		if (set != null && set.size() > 0) {
			IElementHandler setManager = view.getCurrentElementHandler();
			if (setManager == null) {
				return;
			}

			if (UIUtils.showQuestionDialog(NLS.bind(Messages.DeleteSetAction_1, set.getID()), Messages.DeleteSetAction_2)) {
				IElementManager uiManager = view.getUIManager();
				uiManager.removeSet(set.getID(), setManager);

				IElementSet[] sets = setManager.getSets();
				if (sets.length > 0) {
					IElementSet lastSet = sets[sets.length - 1];
					view.selectSet(setManager.getSet(lastSet.getID()));
					// view.update();
					view.refresh(false);
				}
			}
		}
	}
}
