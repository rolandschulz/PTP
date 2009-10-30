/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rephraserengine.core.IResourceFilter;
import org.eclipse.rephraserengine.core.DefaultResourceFilter;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.Workbench;

/**
 * Class providing information about the active page/editor/shell, current selection, etc. in the
 * Eclipse workbench.
 * <p>
 * This information is populated immediately when the class is constructed and is not updated.  If
 * the workbench selection changes between the time this object is constructed and the time one of
 * its methods is called, the information provided will be based on the old selection (i.e., what
 * was selected when the constructor was called).  Thus, {@link WorkbenchSelectionInfo} objects
 * should be created and then immediately used and disposed of.
 *
 * @author Jeff Overbey, Tim Yuvashev
 */
@SuppressWarnings("restriction")
public class WorkbenchSelectionInfo
{
    private IResourceFilter resourceFilter;

    private IEditorPart activeEditor;
    private IFile fileInEditor;
    private ISelection selection;
    private List<IResource> selectedResources;
    private List<IFile> allFilesInSelectedResources;
    private ITextSelection selectionInEditor;

    /**
     * Default constructor; uses a {@link DefaultResourceFilter}.
     *
     * @see #WorkbenchSelectionInfo(IResourceFilter)
     */
    public WorkbenchSelectionInfo()
    {
        this(new DefaultResourceFilter());
    }

    /**
     * Constructor.
     *
     * @param resourceFilter the resource filter that will be used to determine what resources are
     *                       included in the results of {@link #getAllFilesInSelectedResources()}
     *                       and {@link #getFileInEditor()}
     */
    public WorkbenchSelectionInfo(IResourceFilter resourceFilter)
    {
        this.resourceFilter = resourceFilter;

        selection = null;
        selectedResources = Collections.<IResource>emptyList();
        activeEditor = null;
        fileInEditor = null;
        selectionInEditor = null;

        IWorkbenchWindow activeWindow = Workbench.getInstance().getActiveWorkbenchWindow();
        if (activeWindow == null) return;

        selection = activeWindow.getSelectionService().getSelection();

        if (selection instanceof IStructuredSelection)
        {
            selectedResources = getResourcesSelectedIn((IStructuredSelection)selection);
            allFilesInSelectedResources = findAllFilesIn(selectedResources);
        }

        IWorkbenchPage activePage = activeWindow.getActivePage();
        if (activePage == null) return;

        activeEditor = activePage.getActiveEditor();
        if (activeEditor == null) return;

        IEditorInput input = activeEditor.getEditorInput();
        if (!(input instanceof IFileEditorInput)) return;

        IFileEditorInput fileInput = (IFileEditorInput)input;
        fileInEditor = fileInput.getFile();
        if (!resourceFilter.shouldProcess(fileInEditor)) fileInEditor = null;

        if (selection instanceof ITextSelection)
        {
            selectionInEditor = (ITextSelection)selection;
            if (fileInEditor != null)
            {
                selectedResources = Collections.<IResource>singletonList(fileInEditor);
                allFilesInSelectedResources = Collections.<IFile>singletonList(fileInEditor);
            }
        }
    }

    private List<IResource> getResourcesSelectedIn(IStructuredSelection selection)
    {
        List<IResource> result = new ArrayList<IResource>();

        for (Object selectedItem : selection.toList())
        {
            if (selectedItem instanceof IAdaptable)
            {
                IAdaptable item = (IAdaptable)selectedItem;
                IResource res = (IResource)item.getAdapter(IResource.class);
                if (res != null && resourceFilter.shouldProcess(res))
                    result.add(res);
            }
        }

        return result;
    }

    private List<IFile> findAllFilesIn(Collection<IResource> resources)
    {
        return findAllFilesIn(resources.toArray(new IResource[resources.size()]));
    }

    private List<IFile> findAllFilesIn(IResource[] resources)
    {
        ArrayList<IFile> files = new ArrayList<IFile>();
        for (IResource r : resources)
        {
            if (resourceFilter.shouldProcess(r))
            {
                if (r instanceof IFile)
                {
                    files.add((IFile)r);
                }
                else if (r instanceof IFolder || r instanceof IProject)
                {
                    try
                    {
                        files.addAll(findAllFilesIn(((IContainer)r).members()));
                    }
                    catch (CoreException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        return files;
    }

    public boolean someFilesAreSelected()
    {
        return allFilesInSelectedResources != null && !allFilesInSelectedResources.isEmpty();
    }

    /** @return true iff the active editor in the workbench is editing a document based on an
     *          {@link IFile}, and that file was accepted by the resource filter passed to the
     *          <code>WorkbenchSelectionInfo</code> constructor
     *  @see #WorkbenchSelectionInfo(IResourceFilter)
     */
    public boolean editingAnIFile()
    {
        return fileInEditor != null;
    }

    /** @return the file open in the active editor in the workbench, or <code>null</code> if there
     *          is no editor open, it is editing a document not based on an {@link IFile}, or the
     *          file being edited was not accepted by the resource filter passed to the
     *          <code>WorkbenchSelectionInfo</code> constructor
     *  @see #WorkbenchSelectionInfo(IResourceFilter)
     */
    public IFile getFileInEditor()
    {
        return fileInEditor;
    }

    /** @return true iff the active editor in the workbench is a text editor, and the current
     *          workbench selection is a text selection
     */
    public boolean isTextSelectedInEditor()
    {
        return selectionInEditor != null;
    }

    /** @return the text selection in the active editor in the workbench (if the active editor is a
     *          text editor and the current workbench selection is a text selection), or
     *          <code>null</code>
     */
    public ITextSelection getSelectionInEditor()
    {
        return selectionInEditor;
    }

    /**
     * Returns a list of all acceptable files in the current workbench selection.
     * <p>
     * If the user has selected one or more resources (i.e., files, folder, or projects, usually
     * selected in the Project Explorer view), then this list will include all of the files in the
     * selected resources that are acceptable to the resource filter passed to the
     * <code>WorkbenchSelectionInfo</code> constructor.  Projects and folders acceptable to the
     * resource filter are traversed recursively, so this list will include all acceptable files
     * in those resources.
     * <p>
     * If the user has selected text in a text editor, and the active editor in the workbench is
     * editing a document based on an {@link IFile}, and that file was accepted by the resource
     * filter passed to the <code>WorkbenchSelectionInfo</code> constructor, then this list contains
     * only one file: the file in the active editor.
     *
     *  @see #WorkbenchSelectionInfo(IResourceFilter)
     */
    public List<IFile> getAllFilesInSelectedResources()
    {
        return allFilesInSelectedResources;
    }
}
