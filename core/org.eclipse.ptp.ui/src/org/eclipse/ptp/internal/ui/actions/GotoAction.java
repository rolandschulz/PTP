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
import org.eclipse.ptp.internal.ui.views.AbstractParallelElementView;

/**
 * @author Clement chu
 * 
 */
public abstract class GotoAction extends ParallelAction {
	protected String id = ""; //$NON-NLS-1$
	protected GotoDropDownAction action = null;
	protected Object data;

	/**
	 * Constructor
	 * 
	 * @param name
	 *            name of action
	 * @param id
	 *            action ID
	 * @param view
	 *            view
	 * @param action
	 *            drop down action
	 */
	public GotoAction(String name, String id, AbstractParallelElementView view, GotoDropDownAction action, Object data) {
		this(name, id, view, IAction.AS_CHECK_BOX, action, data);
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 *            name of action
	 * @param id
	 *            action ID
	 * @param view
	 *            view
	 * @param style
	 *            style of action
	 * @param action
	 *            drop down action
	 */
	public GotoAction(String name, String id, AbstractParallelElementView view, int style, GotoDropDownAction action, Object data) {
		super(name, style, view);
		this.id = id;
		this.action = action;
		this.data = data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.actions.ParallelAction#run(java.util.BitSet)
	 */
	@Override
	public void run(BitSet elements) {
		action.run(elements, id, data);
	}
}
