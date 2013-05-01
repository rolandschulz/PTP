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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.internal.ui.ParallelImages;
import org.eclipse.ptp.internal.ui.messages.Messages;
import org.eclipse.ptp.internal.ui.model.IElementHandler;
import org.eclipse.ptp.internal.ui.model.IElementSet;
import org.eclipse.ptp.internal.ui.views.AbstractParallelElementView;

/**
 * @author clement chu
 * 
 */
public class CreateSetAction extends GotoDropDownAction {
	public static final String name = Messages.CreateSetAction_0;

	/**
	 * Constructor
	 * 
	 * @param view
	 */
	public CreateSetAction(AbstractParallelElementView view) {
		super(name, view);
		setImageDescriptor(ParallelImages.ID_ICON_CREATESET_NORMAL);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.actions.GotoDropDownAction#createDropDownMenu(org.eclipse.jface.action.MenuManager)
	 */
	@Override
	protected void createDropDownMenu(MenuManager dropDownMenuMgr) {
		String curID = view.getCurrentSetID();
		IElementHandler setManager = view.getCurrentElementHandler();
		if (setManager == null) {
			return;
		}

		for (IElementSet set : setManager.getSets()) {
			addAction(dropDownMenuMgr, set.getID(), set.getID(), curID, null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.actions.GotoDropDownAction#addAction(org.eclipse.jface.action.MenuManager, java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	protected void addAction(MenuManager dropDownMenuMgr, String e_name, String id, String curID, Object data) {
		IAction action = new InternalSetAction(Messages.CreateSetAction_1 + e_name, id, view, this);
		action.setEnabled(!curID.equals(id));
		dropDownMenuMgr.add(action);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.actions.ParallelAction#run(java.util.BitSet)
	 */
	@Override
	public void run(BitSet elements) {
		run(elements, null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.actions.GotoDropDownAction#run(java.util.BitSet, java.lang.String, java.lang.Object)
	 */
	@Override
	public void run(BitSet elements, String setID, Object data) {
		if (validation(elements)) {
			final IElementHandler setManager = view.getCurrentElementHandler();
			if (setManager == null) {
				return;
			}

			if (setID == null) {
				IInputValidator inputValidator = new IInputValidator() {
					@Override
					public String isValid(String newText) {
						if (newText == null || newText.length() == 0) {
							return Messages.CreateSetAction_2;
						}

						if (setManager.getSet(newText) != null) {
							return NLS.bind(Messages.CreateSetAction_5, newText);
						}

						return null;
					}
				};
				InputDialog inputDialog = new InputDialog(getShell(), Messages.CreateSetAction_3, Messages.CreateSetAction_4,
						"", inputValidator); //$NON-NLS-1$
				if (inputDialog.open() == InputDialog.CANCEL) {
					return;
				}

				setID = inputDialog.getValue();
				view.getUIManager().createSet(setID, setID, setManager, elements);
			} else {
				view.getUIManager().addToSet(setID, setManager, elements);
			}

			view.selectSet(setManager.getSet(setID));
			// Need to deselect all elements manually
			// view.update();
			view.refresh(false);
		}
	}

	/**
	 * Inner internal set action
	 * 
	 * @author clement
	 * 
	 */
	private class InternalSetAction extends GotoAction {
		public InternalSetAction(String name, String id, AbstractParallelElementView view, GotoDropDownAction action) {
			super(name, id, view, action, null);
			setImageDescriptor(ParallelImages.ID_ICON_CREATESET_NORMAL);
		}
	}
}
