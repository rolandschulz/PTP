/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.tests.refactoring.extractprocedure;

import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.internal.core.refactoring.ExtractProcedureRefactoring;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.PhotranWorkspaceTestCase;

public class ExtractProcTestCase extends PhotranWorkspaceTestCase
{
    private static final String DIR = "refactoring-test-code/extract-procedure";

    private static NullProgressMonitor pm = new NullProgressMonitor();

    protected String filename = null;
    protected ITextSelection selectionToExtract = null;

    public ExtractProcTestCase() {;}  // when JUnit invokes a subclass outside a test suite

    public ExtractProcTestCase(String filename, Object ignored) // avoid JUnit constructor
    {
        this.filename = filename;
        this.setName("test");
    }

    protected void doRefactoring() throws Exception
    {
        String description = "Attempt to extract procedure from " + filename;

        ExtractProcedureRefactoring refactoring = createRefactoring(filename);

        project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
        String before = compileAndRunFortranProgram();

        RefactoringStatus status = refactoring.checkInitialConditions(pm);
        assertTrue(description + " failed initial precondition check: " + status.toString(), !status.hasError());

        refactoring.setName("new_procedure");

        status = refactoring.checkFinalConditions(pm);
        assertTrue(description + " failed final precondition check: " + status.toString(), !status.hasError());

        Change change = refactoring.createChange(pm);
        assertNotNull(description + " returned null Change object", change);
        assertTrue(description + " returned invalid Change object", change.isValid(pm).isOK());
        change.perform(pm);

        project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());

        String after = compileAndRunFortranProgram();
        System.out.println(after);
        assertEquals(before, after);
    }

    private ExtractProcedureRefactoring createRefactoring(final String filename) throws Exception
    {
        final IFile thisFile = importFile(Activator.getDefault(), DIR, filename);
        ExtractProcedureRefactoring r = new ExtractProcedureRefactoring();
        r.initialize(thisFile, selectionToExtract);
        return r;
    }

    @Override protected String readTestFile(Plugin activator, String srcDir, String filename) throws IOException, URISyntaxException
    {
        String result = super.readTestFile(activator, srcDir, filename);

        int startOffset = result.lastIndexOf('\n', result.indexOf("!<<<<<START")) + 1;
        int endOffset = result.indexOf("!<<<<<END");
        if (startOffset <= 0 || endOffset < 0 || endOffset <= startOffset)
            throw new Error("Malformed test case");

        selectionToExtract = new TextSelection(startOffset, endOffset-startOffset);

        return result;
    }

    protected String readTestFile(String filename) throws IOException, URISyntaxException
    {
        return super.readTestFile(Activator.getDefault(), DIR, filename);
    }

    /**
     * Given an array with all of the positions of identifiers that should be renamed together, try applying the Rename refactoring to
     * each, and make sure all the others change with it.
     */
    public void test() throws Exception
    {
        if (filename == null) return; // when JUnit invokes this outside a test suite

        doRefactoring();
        assertEquals(
            readTestFile(filename + ".result").replaceAll("\\r", ""), // expected result
            readWorkspaceFile(filename).replaceAll("\\r", ""));       // actual refactored file
    }
}
