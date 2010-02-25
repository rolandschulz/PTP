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

package org.eclipse.rephraserengine.internal.ui.search;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.rephraserengine.internal.ui.Activator;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author Doug Schaefer
 * @author Jeff Overbey
 */
@SuppressWarnings("restriction")
public class SearchViewPage extends AbstractTextSearchViewPage {

	private ISearchContentProvider contentProvider;
	
	public SearchViewPage(int supportedLayouts) {
		super(supportedLayouts);
	}

	public SearchViewPage() {
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
		contentProvider = new SearchTreeContentProvider();
		viewer.setContentProvider((SearchTreeContentProvider)contentProvider);
		viewer.setLabelProvider(new SearchLabelProvider(this));
	}

	@Override
	protected void configureTableViewer(TableViewer viewer) {
		contentProvider = new SearchListContentProvider();
		viewer.setContentProvider((SearchListContentProvider)contentProvider);
		viewer.setLabelProvider(new SearchLabelProvider(this));
	}

    @Override
	protected void showMatch(Match match, int currentOffset, int currentLength, boolean activate) throws PartInitException  {
		try {
			IEditorPart editor = openInEditor((IFile)match.getElement());
			if (editor instanceof ITextEditor) {
				ITextEditor textEditor = (ITextEditor)editor;
				textEditor.selectAndReveal(currentOffset, currentLength);
			}
		} catch (CoreException e) {
			Activator.log(e);
		}
	}

    private IEditorPart openInEditor(IFile file) throws PartInitException
    {
        IWorkbenchPage page = Workbench.getInstance().getActiveWorkbenchWindow().getActivePage();
        return page == null ? null : IDE.openEditor(page, file);
    }
}
