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
package org.eclipse.photran.refactoring.tests.commonblockvarnames;

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
import org.eclipse.photran.internal.core.refactoring.CommonVarNamesRefactoring;
import org.eclipse.photran.internal.core.util.LineCol;
import org.eclipse.photran.refactoring.tests.RefactoringTestCase;

/**
 * Test case for the Make Common Variable Names Consistent refactoring
 *
 * @author Kurt Hendle
 * @author Jeff Overbey
 *
 * @see CommonVarNamesTestSuite
 */
public class CommonVarNamesTestCase extends RefactoringTestCase
{
    private static final String DIR = "make-common-var-names-consist-test-code";

    private static NullProgressMonitor pm = new NullProgressMonitor();

    protected String[] filenames;
    protected LineCol position;
    protected int length;
    protected boolean shouldFailPreconditionCheck;
    protected String[] newNames = null;

    public CommonVarNamesTestCase() {;}

    public CommonVarNamesTestCase(String[] filenames, LineCol position, int length, boolean shouldFailPreconditionCheck)
    {
        this(filenames, position, length, shouldFailPreconditionCheck, (String[])null);
    }

    public CommonVarNamesTestCase(String[] filenames, LineCol position, int length, boolean shouldFailPreconditionCheck,
                                  String... newNames)
    {
        this.filenames = filenames;
        this.position = position;
        this.length = length;
        this.shouldFailPreconditionCheck = shouldFailPreconditionCheck;
        this.newNames = newNames;
        this.setName("test");
    }

    /** very similar to the other refactoring tests */
    protected void doRefactoring() throws Exception
    {
        String description = "Attempting to make COMMON variables consistent throughout project.";

        CommonVarNamesRefactoring refactoring = createRefactoring(filenames, position, length);

//        project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
//        String before = compileAndRunFortranProgram();

        RefactoringStatus status = refactoring.checkInitialConditions(pm);
        assertTrue(description + " failed initial precondition check: " + status.toString(), !status.hasError());

        if(newNames != null)
        {
            for(int i=0; i<newNames.length; i++)
                refactoring.modifyNewName(i, newNames[i]);
        }

        status = refactoring.checkFinalConditions(pm);
        if (shouldFailPreconditionCheck)
        {
            assertTrue(description + " should have failed final precondition check: " + status.toString(), status.hasError());
        }
        else
        {
            assertTrue(description + " failed final precondition check: " + status.toString(), !status.hasError());

            Change change = refactoring.createChange(pm);
            assertNotNull(description + " returned null Change object", change);
            assertTrue(description + " returned invalid Change object", change.isValid(pm).isOK());
            change.perform(pm);

    //        String after = compileAndRunFortranProgram();
    //        System.out.println(after);
    //        assertEquals(before, after);
        }
    }

    private CommonVarNamesRefactoring createRefactoring(final String[] filenames, final LineCol lineCol, final int length) throws Exception
    {
        for (int i = 1; i < filenames.length; i++)
            importFile(DIR, filenames[i]);

        final IFile file = importFile(DIR, filenames[0]);
        Document doc = new Document(readFileToString(DIR+"/"+filenames[0]));
        TextSelection text = new TextSelection(doc, getLineColOffset(filenames[0], lineCol), length);
        CommonVarNamesRefactoring r = new CommonVarNamesRefactoring();
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

    /**
     * Borrowed from IntroImplicitTestCase.java
     */
    public void test() throws Exception
    {
        if (filenames == null) return; // when JUnit invokes this outside a test suite

        doRefactoring();
        if (!shouldFailPreconditionCheck)
        {
            for (String filename : filenames)
            {
                assertEquals(readTestFile(filename + ".result"), // expected result
                             readWorkspaceFile(filename));       // actual refactored file
            }
        }
    }
}
