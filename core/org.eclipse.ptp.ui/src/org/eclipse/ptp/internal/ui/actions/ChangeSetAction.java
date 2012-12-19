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
import org.eclipse.jface.action.Separator;
import org.eclipse.ptp.internal.ui.ParallelImages;
import org.eclipse.ptp.ui.actions.GotoAction;
import org.eclipse.ptp.ui.actions.GotoDropDownAction;
import org.eclipse.ptp.ui.messages.Messages;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.ptp.ui.views.AbstractParallelElementView;

/**
 * @author Clement chu
 * 
 */
public class ChangeSetAction extends GotoDropDownAction {
	public static final String name = Messages.ChangeSetAction_0;

	/**
	 * Constructor
	 * 
	 * @param view
	 */
	public ChangeSetAction(AbstractParallelElementView view) {
		super(name, view);
		setImageDescriptor(ParallelImages.ID_ICON_CHANGESET_NORMAL);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.actions.GotoDropDownAction#createDropDownMenu(org.eclipse.jface.action.MenuManager)
	 */
	@Override
	protected void createDropDownMenu(MenuManager dropDownMenuMgr) {
		String curID = view.getCurrentSetID();
		if (curID == null || curID.length() == 0) {
			return;
		}

		addAction(dropDownMenuMgr, IElementHandler.SET_ROOT_ID, IElementHandler.SET_ROOT_ID, curID, null);
		IElementHandler setManager = view.getCurrentElementHandler();
		if (setManager == null) {
			return;
		}

		IElementSet[] sets = setManager.getSets();
		if (sets.length > 1) {
			dropDownMenuMgr.add(new Separator());
		}
		for (IElementSet set : sets) {
			if (set instanceof IElementSet) {
				if (set.getID().equals(IElementHandler.SET_ROOT_ID)) {
					continue;
				}

				addAction(dropDownMenuMgr, set.getID(), set.getID(), curID, null);
			}
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
		IAction action = new InternalSetAction(e_name, id, view, this);
		action.setChecked(curID.equals(id));
		action.setEnabled(true);
		dropDownMenuMgr.add(action);
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
		IElementHandler setManager = view.getCurrentElementHandler();
		if (setManager == null) {
			return;
		}

		IElementSet[] sets = setManager.getSets();
		for (int i = 0; i < sets.length; i++) {
			if (view.getCurrentSetID().equals(sets[i].getID())) {
				if (i + 1 < sets.length) {
					run(sets[i + 1]);
				} else {
					run(sets[0]);
				}
				break;
			}
		}
	}

	/**
	 * run action
	 * 
	 * @param elements
	 * @param set
	 */
	private void run(IElementSet set) {
		view.selectSet(set);
		view.refresh(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.actions.GotoDropDownAction#run(java.util.BitSet, java.lang.String, java.lang.Object)
	 */
	@Override
	public void run(BitSet elements, String id, Object data) {
		IElementHandler setManager = view.getCurrentElementHandler();
		if (setManager == null) {
			return;
		}

		run(setManager.getSet(id));
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
		}
	}
}
