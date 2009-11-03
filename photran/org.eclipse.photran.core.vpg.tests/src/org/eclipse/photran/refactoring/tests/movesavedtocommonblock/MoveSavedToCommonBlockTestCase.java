/*******************************************************************************
 * Copyright (c) 2008 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.refactoring.tests.movesavedtocommonblock;

/**
 *
 * @author Stas Negara
 */

import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.internal.core.refactoring.MoveSavedToCommonBlockRefactoring;
import org.eclipse.photran.internal.core.util.LineCol;
import org.eclipse.photran.refactoring.tests.RefactoringTestCase;

public class MoveSavedToCommonBlockTestCase extends RefactoringTestCase
{
    private static final String DIR = "move-saved-to-common-block-test-code";

    private static NullProgressMonitor pm = new NullProgressMonitor();

    protected String filename;
    protected LineCol lineCol;

    public MoveSavedToCommonBlockTestCase() {}  // when JUnit invokes a subclass outside a test suite

    public MoveSavedToCommonBlockTestCase(String filename, LineCol lineCol)
    {
        this.filename = filename;
        this.lineCol = lineCol;
        this.setName("test");
    }

    protected void doRefactoring() throws Exception
    {
        String description = "Attempt to apply MoveSavedToCommonBlockRefactoring at " + lineCol;

        MoveSavedToCommonBlockRefactoring moveSavedToCommonBlockRefactoring = createRefactoring(filename, lineCol);

        RefactoringStatus status = moveSavedToCommonBlockRefactoring.checkInitialConditions(pm);
        assertTrue(description + " failed initial precondition check: " + status.toString(), !status.hasError());

        status = moveSavedToCommonBlockRefactoring.checkFinalConditions(pm);
        assertTrue(description + " failed final precondition check: " + status.toString(), !status.hasError());

        Change change = moveSavedToCommonBlockRefactoring.createChange(pm);
        assertNotNull(description + " returned null Change object", change);
        assertTrue(description + " returned invalid Change object", change.isValid(pm).isOK());
        change.perform(pm);

        project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
    }

    private MoveSavedToCommonBlockRefactoring createRefactoring(final String filename, final LineCol lineCol) throws Exception
    {
        final IFile thisFile = importFile(DIR, filename);

        MoveSavedToCommonBlockRefactoring r = new MoveSavedToCommonBlockRefactoring();
        r.initialize(thisFile, new TextSelection(getLineColOffset(filename, lineCol), 0));
        return r;
    }

    protected String readTestFile(String filename) throws IOException, URISyntaxException
    {
        return super.readTestFile(DIR, filename);
    }

    public void test() throws Exception
    {
        if (filename == null) return; // when JUnit invokes this outside a test suite

        doRefactoring();
        assertEquals(
            readTestFile(filename + ".result"), // expected result
            readWorkspaceFile(filename));       // actual refactored file
    }
}
