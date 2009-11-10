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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.ptp.rdt.core.resources.RemoteNature;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;

/**
 * Remote version of the CDT makefile editor.
 * It hides actions in the Outline view for makefiles in remote projects.
 *
 */
public class RemoteMakefileEditor extends MakefileEditor {
	
	public RemoteMakefileEditor() {
		super();
	}

	@Override
	public MakefileContentOutlinePage getOutlinePage() {
		if (!isRemote())
			return super.getOutlinePage();
		
		if (page == null) {
			page = new RemoteMakefileContentOutlinePage(this);
			page.addSelectionChangedListener(this);
			page.setInput(getEditorInput());
		}
		return page;
	}
	
	private boolean isRemote() {
		IEditorInput editorInput = getEditorInput();
		if (editorInput instanceof IFileEditorInput) {
			IFileEditorInput input = (IFileEditorInput) editorInput;
			IFile file = input.getFile();
			if (file != null) {
				IProject project = file.getProject();
				if (project != null && RemoteNature.hasRemoteNature(project)) {
					return true;
				}
			}
		}			
		return false;
	}
}
