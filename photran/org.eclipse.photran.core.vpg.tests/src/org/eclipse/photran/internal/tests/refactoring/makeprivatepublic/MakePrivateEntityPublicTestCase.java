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
package org.eclipse.photran.internal.tests.refactoring.makeprivatepublic;

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
import org.eclipse.photran.internal.core.refactoring.MakePrivateEntityPublicRefactoring;
import org.eclipse.photran.internal.core.util.LineCol;
import org.eclipse.photran.internal.tests.RefactoringTestCase;

/**
 *
 * @author Kurt Hendle
 */
public class MakePrivateEntityPublicTestCase extends RefactoringTestCase
{
    private static final String DIR = "refactoring-test-code/make-private-entity-public";

    private static NullProgressMonitor pm = new NullProgressMonitor();

    protected String filename;
    protected LineCol position;
    protected int length;
    protected boolean shouldFailPreconditionCheck;

    public MakePrivateEntityPublicTestCase() {;}

    public MakePrivateEntityPublicTestCase(String filename, LineCol position, int length, boolean shouldFailPreconditionCheck)
    {
        this.filename = filename;
        this.position = position;
        this.length = length;
        this.shouldFailPreconditionCheck = shouldFailPreconditionCheck;
        this.setName("test");
    }

    protected void doRefactoring() throws Exception
    {
        String description = "Attempting to make selected private variable public.";

        MakePrivateEntityPublicRefactoring refactoring = createRefactoring(filename, position, length);

        RefactoringStatus status = refactoring.checkInitialConditions(pm);

        if(shouldFailPreconditionCheck)
            assertTrue(description + " failed initial precondition check: " + status.toString(), status.hasError());

        status = refactoring.checkFinalConditions(pm);
        assertTrue(description + " failed final precondition check: " + status.toString(), !status.hasError());

        if(!shouldFailPreconditionCheck)
        {
            Change change = refactoring.createChange(pm);
            assertNotNull(description + " returned null Change object", change);
            assertTrue(description + " returned invalid Change object", change.isValid(pm).isOK());
            change.perform(pm);
        }
    }

    private MakePrivateEntityPublicRefactoring createRefactoring(final String filename, final LineCol lineCol, final int length) throws Exception
    {
        final IFile file = importFile(DIR, filename);
        Document doc = new Document(readFileToString(DIR+"/"+filename));
        TextSelection text = new TextSelection(doc, getLineColOffset(filename, lineCol), length);
        MakePrivateEntityPublicRefactoring r = new MakePrivateEntityPublicRefactoring();
        r.initialize(file, text);
        return r;
    }

    private String readFileToString(final String path) throws IOException
    {
        FileInputStream stream = new FileInputStream(new File(path));
        return readStream(stream);
    }

    protected String readTestFile(String filename) throws IOException, URISyntaxException
    {
        return super.readTestFile(DIR, filename);
    }

    public void test() throws Exception
    {
        if(filename == null) return;

        doRefactoring();
        if(!shouldFailPreconditionCheck)
            assertEquals(
                sanitize(readTestFile(filename + ".result")),
                sanitize(readWorkspaceFile(filename)));
    }

    public String sanitize(String dirtyString)
    {
        return dirtyString.replaceAll("\r", "");
    }
}
