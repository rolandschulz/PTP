/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.rdt.make.internal.ui.editor;

import org.eclipse.cdt.make.internal.ui.editor.MakefileContentOutlinePage;
import org.eclipse.jface.viewers.ISelection;

public class AddBuildTargetAction extends
		org.eclipse.cdt.make.internal.ui.editor.AddBuildTargetAction {

	public AddBuildTargetAction(MakefileContentOutlinePage outliner) {
		super(outliner);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.internal.ui.editor.AddBuildTargetAction#canActionBeAdded(org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public boolean canActionBeAdded(ISelection selection) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.internal.ui.editor.AddBuildTargetAction#run()
	 */
	@Override
	public void run() {
		//do nothing
	}

}
