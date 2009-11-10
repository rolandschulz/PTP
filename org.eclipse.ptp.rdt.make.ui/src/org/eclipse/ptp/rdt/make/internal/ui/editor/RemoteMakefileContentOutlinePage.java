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
import org.eclipse.cdt.make.internal.ui.editor.MakefileEditor;

public class RemoteMakefileContentOutlinePage extends
		MakefileContentOutlinePage {

	public RemoteMakefileContentOutlinePage(MakefileEditor editor) {
		super(editor);
		fAddBuildTargetAction = new AddBuildTargetAction(this);
		fOpenIncludeAction = new OpenIncludeAction(this);
	}

}
