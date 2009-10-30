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
package org.eclipse.photran.refactoring.tests.keyword;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.internal.core.refactoring.KeywordCaseRefactoring;
import org.eclipse.photran.refactoring.tests.RefactoringTestCase;

/**
 * Test cases for Keyword Case Refactoring
 * @author Kurt Hendle
 */
public class KeywordCaseTestCase extends RefactoringTestCase
{
    private static final String DIR = "keyword-case-test-code";

    private static NullProgressMonitor pm = new NullProgressMonitor();

    protected String filename;
    protected boolean lowerCase;

    public KeywordCaseTestCase() {;} // when JUnit invokes a subclass outside a test suite

    public KeywordCaseTestCase(String filename, boolean lowerCase)
    {
        this.filename = filename;
        this.lowerCase = lowerCase;
        this.setName("test");
    }

    /** Borrowed from IntroImplicitTestCase.java */
    protected void doRefactoring() throws Exception
    {
        String description = "Attempt to upcase keywords in " + filename;

        KeywordCaseRefactoring refactoring = createRefactoring(filename);
        refactoring.setLowerCase(lowerCase);

        RefactoringStatus status = refactoring.checkInitialConditions(pm);
        assertTrue(description + " failed initial precondition check: " + status.toString(), !status.hasError());

        status = refactoring.checkFinalConditions(pm);
        assertTrue(description + " failed final precondition check: " + status.toString(), !status.hasError());

        Change change = refactoring.createChange(pm);
        assertNotNull(description + " returned null Change object", change);
        assertTrue(description + " returned invalid Change object", change.isValid(pm).isOK());
        change.perform(pm);

        project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
    }

    private KeywordCaseRefactoring createRefactoring(final String filename) throws Exception
    {
        final IFile thisFile = importFile(DIR, filename);

        ArrayList<IFile> files = new ArrayList<IFile>();
        files.add(thisFile);

        KeywordCaseRefactoring r = new KeywordCaseRefactoring();
        r.initialize(files);
        return r;
    }

    protected String readTestFile(String filename) throws IOException, URISyntaxException
    {
        return super.readTestFile(DIR, filename);
    }

    /**
     * Borrowed from IntroImplicitTestCase.java *
     *
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
