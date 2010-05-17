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
package org.eclipse.rephraserengine.internal.examples.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.rephraserengine.core.refactorings.IResourceRefactoring;
import org.eclipse.rephraserengine.core.refactorings.UserInputBoolean;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

/**
 * A "refactoring" (sort of) that converts tabs to spaces in a text file.
 *
 * @author Jeff Overbey
 */
public final class ConvertTabsToSpacesRefactoring extends Refactoring implements IResourceRefactoring
{
    private List<IFile> files = null;

    private int tabWidth = 4;

    public void initialize(List<IFile> files)
    {
        this.files = files;
    }

    @Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
    {
        assert files != null && !files.isEmpty();

        pm.beginTask("Checking files", files.size());
        pm.setTaskName("Checking files");
        for (IFile file : files)
        {
            if (!file.isAccessible())
                return fatalError("The file " + file.getName() + " is not accessible.");

            if (file.isReadOnly())
                return fatalError("The file " + file.getName() + " is read-only.");

            pm.worked(1);
        }
        pm.done();

        return new RefactoringStatus();
    }

    private RefactoringStatus fatalError(String message)
    {
        return RefactoringStatus.createFatalErrorStatus(message);
    }

    @UserInputBoolean(label="Use 8-character instead of 4-character tab stops", defaultValue=false)
    public void setWideTabs(boolean useWideTabs)
    {
        tabWidth = useWideTabs ? 8 : 4;
    }

    @Override
    public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
    {
        assert files != null && !files.isEmpty();

        return new RefactoringStatus();
    }

    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException
    {
        assert files != null && !files.isEmpty();

        CompositeChange change = new CompositeChange(getName());
        pm.beginTask("Converting tabs", files.size());
        pm.setTaskName("Converting tabs");

        for (IFile file : files)
        {
            try
            {
                pm.subTask("Modifying " + file.getName());
                TextFileChange fileChange = change(file);
                if (fileChange != null)
                    change.add(fileChange);
            }
            catch (IOException e)
            {
                throw new CoreException(new Status(
                    IStatus.ERROR,
                    "org.eclipse.rephraserengine.examples.text",
                    IStatus.OK,
                    "The file " + file.getName() + " could not be read",
                    e));
            }
            pm.worked(1);
        }

        pm.done();
        return change;
    }

    /** @return a TextFileChange that will replace all tabs with spaces in the given file, or
     *          <code>null</code> if there are no tab characters in the file */
    private TextFileChange change(IFile file) throws IOException, CoreException
    {
        TextFileChange change = new TextFileChange("Replace tabs in " + file.getName(), file);
        change.setEdit(new MultiTextEdit());

        Reader in = new BufferedReader(new InputStreamReader(file.getContents(true),
                                                             file.getCharset()));
        int offset = 0;
        int column = 0;
        int numberOfTabsReplaced = 0;
        for (int ch = in.read(); ch >= 0; ch = in.read())
        {
            if (ch == '\t')
            {
                int spacesToNextTabStop = tabWidth - (column % tabWidth);
                change.addEdit(new ReplaceEdit(offset, 1, spaces(spacesToNextTabStop)));
                column += spacesToNextTabStop;
                numberOfTabsReplaced += 1;
            }
            else if (ch == '\n')
            {
                column = 0;
            }
            else
            {
                column++;
            }
            offset++;
        }
        in.close();

        if (numberOfTabsReplaced == 0)
            return null;
        else
            return change;
    }

    private String spaces(int count)
    {
        assert 0 <= count && count <= tabWidth && tabWidth <= 8;

        switch (count)
        {
            case 0: return "";
            case 1: return " ";
            case 2: return "  ";
            case 3: return "   ";
            case 4: return "    ";
            case 5: return "     ";
            case 6: return "      ";
            case 7: return "       ";
            case 8: return "        ";
            default: throw new IllegalArgumentException();
        }
    }

    @Override
    public String getName()
    {
        return "Convert Tabs to Spaces";
    }
}
