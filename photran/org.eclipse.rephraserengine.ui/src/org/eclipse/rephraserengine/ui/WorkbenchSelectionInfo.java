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
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.rephraserengine.core.resources.DefaultResourceFilter;
import org.eclipse.rephraserengine.core.resources.IResourceFilter;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

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
 * 
 * @since 1.0
 */
@SuppressWarnings("restriction")
public class WorkbenchSelectionInfo
{
    private IResourceFilter resourceFilter;
    private String errorMsg;

    private IEditorPart activeEditor;
    private IFile fileInEditor;
    private ISelection selection;
    private List<IResource> selectedResources;
    private List<IFile> allFilesInSelectedResources;
    private ITextSelection selectionInEditor;
    private Boolean someFilesAreSelected;

    /**
     * Default constructor; uses a {@link DefaultResourceFilter} and tracks the selection in the
     * active workbench window.
     *
     * @see #WorkbenchSelectionInfo(IResourceFilter)
     */
    public WorkbenchSelectionInfo()
    {
        this(new DefaultResourceFilter());
    }

    /**
     * Constructor; uses the given resource filter and tracks the selection in the active workbench
     * window.
     *
     * @param resourceFilter the resource filter that will be used to determine what resources are
     *                       included in the results of {@link #getAllFilesInSelectedResources()}
     *                       and {@link #getFileInEditor()}
     */
    public WorkbenchSelectionInfo(IResourceFilter resourceFilter)
    {
        this(resourceFilter, Workbench.getInstance().getActiveWorkbenchWindow());
    }

    /**
     * Constructor; uses a {@link DefaultResourceFilter} and tracks the selection in the active
     * workbench window.
     *
     * @see #WorkbenchSelectionInfo(IResourceFilter)
     *
     * @since 2.0
     */
    public WorkbenchSelectionInfo(IWorkbenchWindow workbenchWindow)
    {
        this(new DefaultResourceFilter(), workbenchWindow);
    }

    /**
     * Constructor; uses the given resource filter and tracks the selection in the given workbench
     * window.
     *
     * @param resourceFilter  the resource filter that will be used to determine what resources are
     *                        included in the results of {@link #getAllFilesInSelectedResources()}
     *                        and {@link #getFileInEditor()}
     * @param workbenchWindow the workbench window whose selection will be tracked
     *
     * @since 2.0
     */
    public WorkbenchSelectionInfo(IResourceFilter resourceFilter, IWorkbenchWindow workbenchWindow)
    {
        this.resourceFilter = resourceFilter;

        selection = null;
        selectedResources = Collections.<IResource>emptyList();
        activeEditor = null;
        fileInEditor = null;
        selectionInEditor = null;

        if (workbenchWindow == null) return;

        selection = workbenchWindow.getSelectionService().getSelection();

        if (selection instanceof IStructuredSelection)
        {
            selectedResources = getResourcesSelectedIn((IStructuredSelection)selection);
            allFilesInSelectedResources = null; // Populate on demand (see getAllFilesInSelectedResources())
        }

        IWorkbenchPage activePage = workbenchWindow.getActivePage();
        if (activePage == null) return;

        activeEditor = activePage.getActiveEditor();
        if (activeEditor == null) return;

        IEditorInput input = activeEditor.getEditorInput();
        if (!(input instanceof IFileEditorInput)) return;

        IFileEditorInput fileInput = (IFileEditorInput)input;
        fileInEditor = fileInput.getFile();
        if (!resourceFilter.shouldProcess(fileInEditor))
        {
            errorMsg = resourceFilter.getError(fileInEditor);
            fileInEditor = null;
        }

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
                if (res != null)
                {
                    if (resourceFilter.shouldProcess(res))
                        result.add(res);
                    else
                        errorMsg = resourceFilter.getError(res);
                }
            }
        }

        if (!result.isEmpty()) errorMsg = null;
        return result;
    }

    /**
     * Returns true if there is at least one file selected.
     * <p>
     * If the <code>canGuess</code> parameter is <code>true</code>, folders and projects will not be
     * traversed recursively; instead, the method will optimistically return true iff the project is
     * refactorable. The caller must then test whether {@link #getAllFilesInSelectedResources()} is
     * empty. This can be used to improve performance.
     * <p>
     * If the <code>canGuess</code> parameter is <code>false<c/ode>, then this method returns true
     * iff {@link #getAllFilesInSelectedResources()} is non-empty.
     * 
     * @since 3.0
     */
    public boolean someFilesAreSelected(boolean canGuess)
    {
        if (someFilesAreSelected == null)
            someFilesAreSelected = internalSomeFilesAreSelected(canGuess);
        
        return someFilesAreSelected;
    }

    public boolean someFilesAreSelected()
    {
        return someFilesAreSelected(false);
    }

    private boolean internalSomeFilesAreSelected(boolean canGuess)
    {
        if (allFilesInSelectedResources != null)
            return !allFilesInSelectedResources.isEmpty();
        else
            return internalSomeFilesAreSelected(
                selectedResources.toArray(new IResource[selectedResources.size()]),
                canGuess);
    }

    private boolean internalSomeFilesAreSelected(IResource[] resources, boolean canGuess)
    {
        for (IResource r : resources)
        {
            if (resourceFilter.shouldProcess(r))
            {
                if (r instanceof IFile)
                {
                    return true;
                }
                else if (r instanceof IFolder || r instanceof IProject)
                {
                    if (r.isAccessible())
                    {
                        if (canGuess)
                        {
                            // Guess that there are resources iff the project is refactorable
                            return resourceFilter.shouldProcess(r.getProject());
                        }
                        else
                        {
                            try
                            {
                                if (internalSomeFilesAreSelected(((IContainer)r).members(), canGuess))
                                    return true;
                            }
                            catch (CoreException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        return false;
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

    /**
     * @return the active editor in the workbench, or <code>null</code> if no editor is active 
     * @since 2.0
     */
    public IEditorPart getActiveEditor()
    {
        return activeEditor;
    }

    /**
     * @return the active editor in the workbench, if it is a text editor, or <code>null</code>
     * if no editor is active or it is not a text editor
     * @since 2.0
     */
    public ITextEditor getActiveTextEditor()
    {
        return activeEditor instanceof ITextEditor ? (ITextEditor)activeEditor : null;
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
     * @return the contents of the active editor in the workbench (if the active editor is a
     *         text editor and its contents can be retrieved), or <code>null</code> otherwise.
     *         Note that, if the editor's contents have not been saved, the <i>unsaved</i>
     *         version (i.e., the current text in the editor) will be returned.
     * @since 2.0
     */
    public String getEditorContents()
    {
        if (activeEditor == null || !(activeEditor instanceof ITextEditor)) return null;

        ITextEditor textEditor = (ITextEditor)activeEditor;
        
        IDocumentProvider dp = textEditor.getDocumentProvider();
        if (dp == null) return null;
        
        IDocument doc = dp.getDocument(textEditor.getEditorInput());
        if (doc == null) return null;
        
        return doc.get();
    }

    /**
     * Returns a list of all of the resources in the current workbench selection.
     * <p>
     * If the user has selected one or more resources (i.e., files, folder, or projects, usually
     * selected in the Project Explorer view), then this list will include all of those resources
     * that are acceptable to the resource filter passed to the <code>WorkbenchSelectionInfo</code>
     * constructor.
     * <p>
     * If the selection includes projects or folders, the {@link IResource} objects for the projects
     * or folders themselves will be returned. To traverse these recursively and retrieve the files
     * in them, use {@link #getAllFilesInSelectedResources()} instead.
     * <p>
     * If the user has selected text in a text editor, and the active editor in the workbench is
     * editing a document based on an {@link IFile}, and that file was accepted by the resource
     * filter passed to the <code>WorkbenchSelectionInfo</code> constructor, then this list contains
     * only one file: the file in the active editor.
     * 
     * @see #WorkbenchSelectionInfo(IResourceFilter)
     * 
     * @since 2.0
     */
    public List<IResource> getSelectedResources()
    {
        return selectedResources;
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
        if (allFilesInSelectedResources == null)
            allFilesInSelectedResources = findAllFilesIn(selectedResources);
        
        return allFilesInSelectedResources;
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
                    if (r.isAccessible())
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
        }
        return files;
    }

    /**
     * @return an error message to display to the user, if all of the files in the selection were
     * filtered out, which describes why that happened and possibly what the user can do about it;
     * may be <code>null</code>
     */
    public String getErrorMessage()
    {
        return errorMsg;
    }
}
