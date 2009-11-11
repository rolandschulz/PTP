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
package org.eclipse.photran.internal.tests.refactoring.encapsulatevariable;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.internal.core.refactoring.EncapsulateVariableRefactoring;
import org.eclipse.photran.internal.core.util.LineCol;

/**
 *
 * @author Tim
 */
public class EncapsulateVariableFailTestCase extends EncapsulateVariableTestCase
{
    private ArrayList<String> myFailFilenames;
    private String failSelectionFile;
    private ITextSelection failSelection;
    private boolean isFatal;

    public EncapsulateVariableFailTestCase(){;}

    public EncapsulateVariableFailTestCase(ArrayList<String> fNames, String selectionfName, boolean isFatal, Object ignore)
    {
        super(fNames, selectionfName, ignore);
        myFailFilenames = fNames;
        failSelectionFile = selectionfName;
        this.isFatal = isFatal;
    }

    @Override
    protected void setSelection(String fContent, String filename)
    {
        int startOffset = fContent.indexOf("!") + 1;
        int endOffset = fContent.indexOf(System.getProperty("line.separator"), startOffset);
        if(endOffset <= 0)
            endOffset = fContent.indexOf("\n", startOffset);

        String infoString = fContent.substring(startOffset, endOffset);
        String [] res = infoString.split(",");

        if(res.length < 3 || res.length > 3)
            throw new Error("Malformed test case");

        int line = Integer.parseInt(res[0]);
        int col = Integer.parseInt(res[1]);
        int length = Integer.parseInt(res[2]);

        LineCol lineCol = new LineCol(line, col);

        int actualOffset = getLineColOffset(filename, lineCol);

        failSelection = new TextSelection(actualOffset, length);
    }

    @Override
    protected void doRefactoring() throws Exception
    {
        IFile selFile = getFileAndSetSelection(DIR, failSelectionFile);

        for(String fName : myFailFilenames)
        {
            getFile(DIR, fName);
        }

        project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
        /*String before =*/ compileAndRunFortranProgram();

        EncapsulateVariableRefactoring refactoring = new EncapsulateVariableRefactoring();
        refactoring.initialize(selFile, failSelection);

        //String description = "Attempt to encapsulate variables in " + failSelectionFile;

        RefactoringStatus status = refactoring.checkAllConditions(new NullProgressMonitor());
        if(isFatal)
            assertTrue(status.hasFatalError());
        else
            assertTrue(status.hasError());
    }

    public void test() throws Exception
    {
        if(myFailFilenames == null)
            return;

        doRefactoring();
    }
}
