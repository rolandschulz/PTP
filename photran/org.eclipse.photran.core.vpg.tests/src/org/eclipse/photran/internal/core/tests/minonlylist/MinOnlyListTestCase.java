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
package org.eclipse.photran.internal.core.tests.minonlylist;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.internal.core.refactoring.MinOnlyListRefactoring;
import org.eclipse.photran.internal.core.util.LineCol;
import org.eclipse.photran.refactoring.tests.RefactoringTestCase;

/**
 *
 * @author Kurt Hendle
 */
public class MinOnlyListTestCase extends RefactoringTestCase
{
    private static final String DIR = "min-only-list-test-code";

    private static NullProgressMonitor pm = new NullProgressMonitor();

    protected String[] filenames;
    protected LineCol position;
    protected int length;
    protected boolean shouldFailPreconditionCheck;
    protected boolean shouldFailFinalPreCheck;

    public MinOnlyListTestCase() {;}

    public MinOnlyListTestCase(String[] filenames, LineCol position, int length, boolean shouldFailPreconditionCheck)
    {
        this.filenames = filenames;
        this.position = position;
        this.length = length;
        this.shouldFailPreconditionCheck = shouldFailPreconditionCheck;
        this.setName("test");
    }

    protected void doRefactoring() throws Exception
    {
        String description = "Attempting to minimize ONLY list.";

        MinOnlyListRefactoring refactoring = createRefactoring(filenames, position, length);

        RefactoringStatus status = refactoring.checkInitialConditions(pm);

        if(shouldFailPreconditionCheck)
            assertTrue(description + " failed initial precondition check: " + status.toString(), status.hasError());

        status = refactoring.checkFinalConditions(pm);
        if (shouldFailPreconditionCheck)
        {   //no error will show up since nothing done in final check conditions
            assertTrue(description + "failed final precondition check: " + status.toString(), !status.hasError());
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

    private MinOnlyListRefactoring createRefactoring(final String[] filenames, final LineCol lineCol, final int length) throws Exception
    {
        for (int i = 1; i < filenames.length; i++)
            importFile(DIR, filenames[i]);

        final IFile file = importFile(DIR, filenames[0]);
        Document doc = new Document(readFileToString(DIR+"/"+filenames[0]));
        TextSelection text = new TextSelection(doc, getLineColOffset(filenames[0], lineCol), length);
        MinOnlyListRefactoring r = new MinOnlyListRefactoring();
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
        if(filenames == null) return;

        doRefactoring();
        if(!shouldFailPreconditionCheck)
            assertEquals(readTestFile(filenames[0] + ".result"), readWorkspaceFile(filenames[0]));
    }
}
