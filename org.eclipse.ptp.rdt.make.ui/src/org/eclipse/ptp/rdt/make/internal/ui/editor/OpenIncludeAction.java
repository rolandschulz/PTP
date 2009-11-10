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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;


public class OpenIncludeAction extends
		org.eclipse.cdt.make.internal.ui.editor.OpenIncludeAction {

	public OpenIncludeAction(ISelectionProvider provider) {
		super(provider);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.internal.ui.editor.OpenIncludeAction#canActionBeAdded(org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public boolean canActionBeAdded(ISelection selection) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.internal.ui.editor.OpenIncludeAction#run()
	 */
	@Override
	public void run() {
		// do nothing
	}

}
