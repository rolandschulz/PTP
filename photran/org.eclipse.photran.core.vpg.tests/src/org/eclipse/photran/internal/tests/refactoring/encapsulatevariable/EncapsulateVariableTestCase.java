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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.internal.core.refactoring.EncapsulateVariableRefactoring;
import org.eclipse.photran.internal.core.util.LineCol;
import org.eclipse.photran.internal.tests.RefactoringTestCase;

/**
 *
 * @author Tim
 */
public class EncapsulateVariableTestCase extends RefactoringTestCase
{
    private ArrayList<String> myFilenames;
    private String selectionFile;
    public static final String DIR = "refactoring-test-code/encapsulate-variable";
    private ITextSelection selection;
    private NullProgressMonitor pm = new NullProgressMonitor();

    public EncapsulateVariableTestCase(){;}

    public EncapsulateVariableTestCase(ArrayList<String> fNames, String selectionfName, Object ignore)
    {
        myFilenames = fNames;
        selectionFile = selectionfName;
        this.setName("test");
    }

    protected void doRefactoring() throws Exception
    {
        IFile selFile = getFileAndSetSelection(DIR, selectionFile);

        for(String fName : myFilenames)
        {
            getFile(DIR, fName);
        }

        project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
        String before = compileAndRunFortranProgram();

        EncapsulateVariableRefactoring refactoring = new EncapsulateVariableRefactoring();
        refactoring.initialize(selFile, selection);
        refactoring.replaceAccessesInDeclaringModule(true);

        String description = "Attempt to encapsulate variables in " + selectionFile;
        RefactoringStatus status = refactoring.checkInitialConditions(pm);
        assertTrue(description + " failed initial precondition check: " + status.toString(), !status.hasError());

        status = refactoring.checkFinalConditions(pm);
        assertTrue(description + " failed final precondition check: " + status.toString(), !status.hasError());

        Change change = refactoring.createChange(pm);
        assertNotNull(description + " returned null Change object", change);
        assertTrue(description + " returned invalid Change object", change.isValid(pm).isOK());
        change.perform(pm);

        project.refreshLocal(IResource.DEPTH_INFINITE, pm);

        String after = compileAndRunFortranProgram();
        System.out.println(after);
        assertEquals(before, after);
    }

    protected IFile getFile(String dir, String fName) throws Exception
    {
        //String text = super.readTestFile(DIR, fName);
        return importFile(dir, fName);
    }

    protected IFile getFileAndSetSelection(String dir, String fName) throws Exception
    {
        String text = super.readTestFile(DIR, fName);
        setSelection(text, fName);
        return importFile(dir, fName);
    }

    protected String readTestFile(String filename) throws IOException, URISyntaxException
    {
        return super.readTestFile(DIR, filename);
    }

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

        selection = new TextSelection(actualOffset, length);
    }

    /*protected String readTestFile(String filename) throws IOException, URISyntaxException
    {
        return super.readTestFile(DIR, filename);
    }*/

    public void test() throws Exception
    {
        if (myFilenames == null) return; // when JUnit invokes this outside a test suite

        doRefactoring();
        String expected = readTestFile(selectionFile + ".result");
        String actual = readWorkspaceFile(selectionFile);

        assertEquals(sanitize(expected), sanitize(actual));

        for(String fName : myFilenames)
        {
            String loopExpected = readTestFile(fName + ".result");
            String loopActual = readWorkspaceFile(fName);
            assertEquals(sanitize(loopExpected), sanitize(loopActual));
        }
    }

    public String sanitize(String dirtyString)
    {
        return dirtyString.replaceAll("\r", "");
    }
}
