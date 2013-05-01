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
import org.eclipse.ptp.internal.ui.messages.Messages;
import org.eclipse.ptp.internal.ui.views.AbstractParallelElementView;

/**
 * @author Clement chu
 * 
 */
public class DisplayRulerAction extends ParallelAction {
	public static final String name = Messages.DisplayRulerAction_0;
	private boolean checked = false;

	/**
	 * Constructor
	 * 
	 * @param view
	 */
	public DisplayRulerAction(AbstractParallelElementView view) {
		super(name, IAction.AS_CHECK_BOX, view);
		setEnabled(view.getCurrentID().length() > 0);
		checked = view.isDisplayRuler();
		setChecked(checked);
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
		getViewPart().setDisplayRuler(!checked);
	}
}
