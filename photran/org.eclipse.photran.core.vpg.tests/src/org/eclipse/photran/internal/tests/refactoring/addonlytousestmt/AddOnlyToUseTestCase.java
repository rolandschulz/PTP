package org.eclipse.photran.internal.tests.refactoring.addonlytousestmt;
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


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.internal.core.refactoring.AddOnlyToUseStmtRefactoring;
import org.eclipse.photran.internal.core.util.LineCol;
import org.eclipse.photran.internal.tests.RefactoringTestCase;

/**
 *
 * @author Kurt Hendle
 */
public class AddOnlyToUseTestCase extends RefactoringTestCase
{
    private static final String DIR = "refactoring-test-code/add-only-to-use-stmt";

    private static NullProgressMonitor pm = new NullProgressMonitor();

    protected String[] filenames;
    protected LineCol position;
    protected int length;
    protected boolean shouldFailPreconditionCheck;
    protected boolean shouldFailFinalPreconditionCheck;
    protected String[] entityNames = null;

    public AddOnlyToUseTestCase() {;}

    public AddOnlyToUseTestCase(String[] filenames, LineCol position, int length, boolean shouldFailPreconditionCheck,
        boolean shouldFailFinalPreCheck)
    {
        this(filenames, position, length, shouldFailPreconditionCheck,
            shouldFailFinalPreCheck, (String[])null);
    }

    public AddOnlyToUseTestCase(String[] filenames, LineCol position, int length, boolean shouldFailPreconditionCheck,
                                  boolean shouldFailFinalPreCheck, String... entityNames)
    {
        this.filenames = filenames;
        this.position = position;
        this.length = length;
        this.shouldFailPreconditionCheck = shouldFailPreconditionCheck;
        this.shouldFailFinalPreconditionCheck = shouldFailFinalPreCheck;
        this.entityNames = entityNames;
        this.setName("test");
    }

    protected void doRefactoring() throws Exception
    {
        String description = "Attempting to add ONLY clause to USE statement.";

        AddOnlyToUseStmtRefactoring refactoring = createRefactoring(filenames, position, length);

        RefactoringStatus status = refactoring.checkInitialConditions(pm);

        if(shouldFailPreconditionCheck)
            assertTrue(description + " failed initial precondition check: " + status.toString(), status.hasError());

        if(entityNames != null)
        {
            for(int i=0; i<entityNames.length; i++)
                refactoring.addToOnlyList(entityNames[i]);
        }

        status = refactoring.checkFinalConditions(pm);
        if (shouldFailFinalPreconditionCheck)
        {
            assertTrue(description + "failed final precondition check: " + status.toString(), status.hasError());
        }
        else
        {
            assertTrue(description + "should not fail final precondition check: " + status.toString(), !status.hasError());

            Change change = refactoring.createChange(pm);
            assertNotNull(description + " returned null Change object", change);
            assertTrue(description + " returned invalid Change object", change.isValid(pm).isOK());
            change.perform(pm);
        }
    }

    private String readFileToString(final String path) throws IOException
    {
        FileInputStream stream = new FileInputStream(new File(path));
        return readStream(stream);
    }

    private AddOnlyToUseStmtRefactoring createRefactoring(final String[] filenames, final LineCol lineCol, final int length) throws Exception
    {
        for (int i = 1; i < filenames.length; i++)
            importFile(DIR, filenames[i]);

        final IFile file = importFile(DIR, filenames[0]);
        Document doc = new Document(readFileToString(DIR+"/"+filenames[0]));
        TextSelection text = new TextSelection(doc, getLineColOffset(filenames[0], lineCol), length);
        AddOnlyToUseStmtRefactoring r = new AddOnlyToUseStmtRefactoring(file, text);
        r.initialize(file, text);
        return r;
    }

    protected String readTestFile(String filename) throws IOException, URISyntaxException
    {
        return super.readTestFile(DIR, filename);
    }

    public void test() throws Exception
    {
        if (filenames == null) return; // when JUnit invokes this outside a test suite

        doRefactoring();
        if (!shouldFailPreconditionCheck && !shouldFailFinalPreconditionCheck)
            assertEquals(
                sanitize(readTestFile(filenames[0] + ".result")),
                sanitize(readWorkspaceFile(filenames[0])));
    }

    public String sanitize(String dirtyString)
    {
        return dirtyString.replaceAll("\r", "");
    }
}
