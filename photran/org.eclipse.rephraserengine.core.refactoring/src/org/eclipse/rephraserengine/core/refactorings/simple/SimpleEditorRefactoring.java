/*******************************************************************************
 * Copyright (c) 2011 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.core.refactorings.simple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.rephraserengine.core.refactorings.IEditorRefactoring;
import org.eclipse.text.edits.ReplaceEdit;

/**
 * This is an extremely simple {@link IEditorRefactoring} implementation intended for use in
 * demonstrations/simple examples. Production code should inherit from
 * <code>VPGEditorRefactoring</code> or another {@link IEditorRefactoring} implementation instead.
 * <p>
 * Subclasses of this class will be similar in appearance to production refactorings (e.g., they
 * will call {@link #findSelected(Class, Object)} and {@link #fail(String)}), but they don't require
 * as much code (e.g., the refactoring name is automatically computed, and there is no separation
 * between precondition checking and change creation).
 * <p>
 * Subclasses must override {@link #perform()}. Typically, the entire refactoring is performed
 * inside this method. They should invoke {@link #fail(String)} if the refactoring encounters an
 * error.
 * <p>
 * Subclasses may also override {@link #beforeUserInput()}, which is invoked before the wizard
 * dialog is displayed.
 * 
 * @author Jeff Overbey
 * 
 * @since 3.0
 */
public abstract class SimpleEditorRefactoring extends Refactoring implements IEditorRefactoring
{
    @SuppressWarnings("serial")
    protected static final class PreconditionFailure extends Exception
    {
        public PreconditionFailure(String message)
        {
            super(message);
        }
    }

    protected IFile file;

    protected ITextSelection selection;

    protected TextFileChange change;

    public final void initialize(IFile file, ITextSelection selection)
    {
        this.file = file;
        this.selection = selection;
        this.change = new TextFileChange(getName(), file);
    }

    @Override
    public final RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException,
        OperationCanceledException
    {
        RefactoringStatus status = new RefactoringStatus();
        try
        {
            beforeUserInput();
        }
        catch (PreconditionFailure e)
        {
            status.addFatalError(e.getMessage());
        }
        catch (Exception e)
        {
            status.addFatalError(e.getClass().getSimpleName() + ": " + e.getMessage()); //$NON-NLS-1$
        }
        return status;
    }

    /**
     * Callback method invoked before the refactoring wizard is displayed to the user.
     * <p>
     * Subclasses may override this method.
     * 
     * @throws Exception
     */
    protected void beforeUserInput() throws Exception
    {
        // Do nothing
    }

    @Override
    public final RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException,
        OperationCanceledException
    {
        RefactoringStatus status = new RefactoringStatus();
        try
        {
            String newFileContents = perform();
            change.initializeValidationData(pm);
            change.setEdit(new ReplaceEdit(0, countCharsIn(file), newFileContents));
        }
        catch (PreconditionFailure e)
        {
            status.addFatalError(e.getMessage());
        }
        catch (Exception e)
        {
            status.addFatalError(e.getClass().getSimpleName() + ": " + e.getMessage()); //$NON-NLS-1$
        }
        return status;
    }

    private int countCharsIn(IFile file) throws CoreException, IOException
    {
        int size = 0;
        Reader in = new BufferedReader(new InputStreamReader(file.getContents(true), file.getCharset()));
        while (in.read() > -1)
            size++;
        in.close();
        return size;
    }

    /**
     * Performs the refactoring, returning the new contents of the file.
     * <p>
     * Subclasses must override this method.
     * 
     * @return the entire, revised contents of the file resulting from the refactoring
     * 
     * @throws PreconditionFailure to display an error message to the user
     * @throws Exception to display the exception class name and an error message to the user
     */
    protected abstract String perform() throws Exception;

    /**
     * Causes the refactoring to fail, displaying the given error message to the user.
     * 
     * @param message the error message to display
     * 
     * @throws PreconditionFailure
     */
    protected final void fail(String message) throws PreconditionFailure
    {
        throw new PreconditionFailure(message);
    }

    /**
     * (Utility method) Finds the least AST node of the given type encompassing the user's text
     * selection in the editor.
     * <p>
     * This implementation assumes a Ludwig-style AST where interior nodes have
     * <code>getChildren</code> and <code>findFirst/LastToken</code> methods, and tokens have
     * <code>getOffset</code> and <code>getLength</code> methods. These are invoked through
     * reflection, so this is by no means an efficient implementation.
     * <p>
     * In production code, an efficient implementation of this method is typically provided by the
     * VPG or AST.
     */
    @SuppressWarnings("unchecked")
    protected <T> T findSelected(Class<T> nodeType, Object node)
    {
        try
        {
            Method getChildren = node.getClass().getMethod("getChildren"); //$NON-NLS-1$
            for (Object child : (Iterable< ? >)getChildren.invoke(node))
            {
                T result = findSelected(nodeType, child);
                if (result != null) return result;
            }

            if (nodeType.isAssignableFrom(node.getClass()))
            {
                Object firstToken = node.getClass().getMethod("findFirstToken").invoke(node); //$NON-NLS-1$
                Object lastToken = node.getClass().getMethod("findLastToken").invoke(node); //$NON-NLS-1$
                if (firstToken != null && lastToken != null)
                {
                    int firstOffset = ((Integer)firstToken.getClass()
                        .getMethod("getOffset").invoke(firstToken)).intValue(); //$NON-NLS-1$
                    int lastOffset = ((Integer)lastToken.getClass()
                        .getMethod("getOffset").invoke(lastToken)).intValue(); //$NON-NLS-1$
                    lastOffset += ((Integer)lastToken.getClass()
                        .getMethod("getLength").invoke(lastToken)).intValue(); //$NON-NLS-1$
                    if (selection.getLength() == 0 && firstOffset <= selection.getOffset()
                        && selection.getOffset() < lastOffset)
                    {
                        // This is the least node enclosing the cursor position; that's good enough
                        return (T)node;
                    }
                    else if (selection.getOffset() == firstOffset
                        && selection.getLength() == (lastOffset - firstOffset))
                    {
                        // The selection exactly encloses this node
                        return (T)node;
                    }
                }
            }

            return null;
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    @Override
    public final Change createChange(IProgressMonitor pm) throws CoreException,
        OperationCanceledException
    {
        return change;
    }

    /**
     * Returns the name of this refactoring.
     * <p>
     * The name of the refactoring is determined by the class name.
     * <ul>
     * <li> "ExtractLocalVariableRefactoring" => "Extract Local Variable"
     * <li> "RenameRefactoring" => "Rename"
     * </ul>
     * Subclasses may override this method to customize the name.
     */
    @Override
    public String getName()
    {
        String name = getClass().getSimpleName();

        if (name.toLowerCase().endsWith("refactoring")) //$NON-NLS-1$
            name = name.substring(0, name.length() - "refactoring".length()); //$NON-NLS-1$

        StringBuilder sb = new StringBuilder(64);
        for (int i = 0, len = name.length(); i < len; i++)
        {
            char c = name.charAt(i);
            if (Character.isUpperCase(c) && i > 0)
                sb.append(" " + c); //$NON-NLS-1$
            else
                sb.append(c);
        }
        return sb.toString();
    }
}