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
package org.eclipse.ptp.debug.internal.ui.actions;

import java.util.BitSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.debug.internal.ui.PDebugImage;
import org.eclipse.ptp.debug.ui.UIDebugManager;
import org.eclipse.ptp.debug.ui.messages.Messages;
import org.eclipse.ptp.debug.ui.views.ParallelDebugView;
import org.eclipse.ptp.ui.IElementManager;

/**
 * @author clement chu
 * 
 */
public class TerminateAction extends DebugAction {
	public static final String name = Messages.TerminateAction_0;

	/**
	 * Constructor
	 * 
	 * @param view
	 */
	public TerminateAction(ParallelDebugView view) {
		super(name, view);
		setImageDescriptor(PDebugImage.getDescriptor(PDebugImage.ICON_TERMINATE_GROUP_NORMAL));
		setDisabledImageDescriptor(PDebugImage.getDescriptor(PDebugImage.ICON_TERMINATE_GROUP_DISABLE));
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
		IElementManager manager = view.getUIManager();
		if (manager instanceof UIDebugManager) {
			try {
				setEnabled(false);
				((UIDebugManager) manager).terminate();
			} catch (CoreException e) {
				setEnabled(true);
			}
		}
	}
}
