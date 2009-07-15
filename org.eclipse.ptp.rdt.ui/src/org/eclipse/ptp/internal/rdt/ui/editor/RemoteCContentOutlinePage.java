/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
 
package org.eclipse.ptp.internal.rdt.ui.editor;

import org.eclipse.cdt.internal.ui.editor.CContentOutlinePage;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.ptp.internal.rdt.ui.actions.OpenViewActionGroup;
import org.eclipse.ptp.internal.rdt.ui.search.actions.SelectionSearchGroup;
import org.eclipse.ui.actions.ActionGroup;

/**
 * Remote version of the outline page.
 * 
 * @author Mike Kucera
 */
public class RemoteCContentOutlinePage extends CContentOutlinePage {

	public RemoteCContentOutlinePage(CEditor editor) {
		super(editor);
		fOpenIncludeAction= new OpenIncludeAction(this);
	}

	@Override
	protected ActionGroup createSearchActionGroup() {
		return new SelectionSearchGroup(this);
	}

	@Override
	protected ActionGroup createOpenViewActionGroup() {
		OpenViewActionGroup ovag= new OpenViewActionGroup(this);
		ovag.setEnableIncludeBrowser(true);
		return ovag;
	}
}
