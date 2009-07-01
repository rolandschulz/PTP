/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.search;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.editors.text.ILocationProvider;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Fortran Search Results Implementation.
 * 
 * @author kdecker3, slieter2
 */
public class ReferenceSearchResult
    extends AbstractTextSearchResult
    implements IEditorMatchAdapter, IFileMatchAdapter
{
    private ISearchQuery query;

    public ReferenceSearchResult(ISearchQuery query)
    {
        super();
        this.query = query;
    }

    /* (non-Javadoc)
     * @see org.eclipse.search.ui.text.AbstractTextSearchResult#getEditorMatchAdapter()
     */
    @Override
    public IEditorMatchAdapter getEditorMatchAdapter()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see org.eclipse.search.ui.text.AbstractTextSearchResult#getFileMatchAdapter()
     */
    @Override
    public IFileMatchAdapter getFileMatchAdapter()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see org.eclipse.search.ui.ISearchResult#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.search.ui.ISearchResult#getLabel()
     */
    public String getLabel()
    {
        return getQuery().getLabel();
    }

    /* (non-Javadoc)
     * @see org.eclipse.search.ui.ISearchResult#getQuery()
     */
    public ISearchQuery getQuery()
    {
        return query;
    }

    /* (non-Javadoc)
     * @see org.eclipse.search.ui.ISearchResult#getTooltip()
     */
    public String getTooltip()
    {
        return SearchMessages.getString("ReferenceSearchResult_1");
    }

    /* IFileMatchAdapter Implementation */
    public Match[] computeContainedMatches(AbstractTextSearchResult result,
                                           IFile file)
    {
        return getMatches(file);
    }

    public IFile getFile(Object element)
    {
        return (IFile)element;
    }

    /* IEditorMatchAdapter Implementation */
    public Match[] computeContainedMatches(AbstractTextSearchResult result,
                                           IEditorPart editor)
    {
        IPath path = getFileName(editor);
        IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
        return getMatches(res);
    }

    public boolean isShownInEditor(Match match, IEditorPart editor)
    {
        IPath path = getFileName(editor);
        return path != null && path.equals(((IFile)match.getElement()).getLocation());
    }

    /*
     * Sourced From:
     *  org.eclipse.cdt.internal.ui.search.PDOMSearchResult.getFileName(IEditorPart)
     *  Tag: CDT_4_0_1 
     */
    private IPath getFileName(IEditorPart editor)
    {
        IEditorInput input = editor.getEditorInput();
        if (input instanceof FileEditorInput)
        {
            FileEditorInput fileInput = (FileEditorInput)input;
            return fileInput.getFile().getLocation();
        }
        else if (input instanceof IStorageEditorInput)
        {
            try
            {
                IStorage storage = ((IStorageEditorInput)input).getStorage();
                if (storage.getFullPath() != null) { return storage
                                .getFullPath(); }
            }
            catch (CoreException exc)
            {
                // ignore
            }
        }
        else if (input instanceof IPathEditorInput)
        {
            return ((IPathEditorInput)input).getPath();
        }
        
        ILocationProvider provider = (ILocationProvider)input.getAdapter(ILocationProvider.class);
        if (provider != null)
            return provider.getPath(input);
        else
            return null;
    }
}
