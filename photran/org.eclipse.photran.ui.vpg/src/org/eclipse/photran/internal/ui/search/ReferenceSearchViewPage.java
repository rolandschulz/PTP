/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.photran.internal.ui.search;

import org.eclipse.cdt.internal.ui.util.EditorUtility;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author Doug Schaefer
 *
 */
@SuppressWarnings("restriction")
public class ReferenceSearchViewPage extends AbstractTextSearchViewPage {

	private IReferencesSearchContentProvider contentProvider;
	
	public ReferenceSearchViewPage(int supportedLayouts) {
		super(supportedLayouts);
	}

	public ReferenceSearchViewPage() {
		super();
	}

	@Override
	protected void elementsChanged(Object[] objects) {
		if (contentProvider != null)
			contentProvider.elementsChanged(objects);
	}

	@Override
	protected void clear() {
		if (contentProvider != null)
			contentProvider.clear();
	}

	@Override
	protected void configureTreeViewer(TreeViewer viewer) {
		contentProvider = new ReferenceSearchTreeContentProvider();
		viewer.setContentProvider((ReferenceSearchTreeContentProvider)contentProvider);
		viewer.setLabelProvider(new ReferenceSearchTreeLabelProvider(this));
	}

	@Override
	protected void configureTableViewer(TableViewer viewer) {
		contentProvider = new ReferenceSearchListContentProvider();
		viewer.setContentProvider((ReferenceSearchListContentProvider)contentProvider);
		viewer.setLabelProvider(new ReferenceSearchListLabelProvider(this));
	}

    @Override
	protected void showMatch(Match match, int currentOffset, int currentLength, boolean activate) throws PartInitException  {
		try {
			IFile element= (IFile)match.getElement();
			IPath path = element.getFullPath();
			IEditorPart editor = EditorUtility.openInEditor(path, null);
			if (editor instanceof ITextEditor) {
				ITextEditor textEditor = (ITextEditor)editor;
				textEditor.selectAndReveal(currentOffset, currentLength);
			}
		} catch (CoreException e) {
			CUIPlugin.log(e);
		}
	}
}
