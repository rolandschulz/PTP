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
package org.eclipse.photran.internal.tests.refactoring.infrastructure;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.refactoring.infrastructure.Reindenter;
import org.eclipse.photran.internal.core.refactoring.infrastructure.SourcePrinter;
import org.eclipse.photran.internal.core.refactoring.infrastructure.Reindenter.Strategy;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.tests.RefactoringTestCase;

public class ReindenterTestCase extends RefactoringTestCase
{
    private static final String DIR = "refactoring-test-code/infrastructure/reindenter";

    protected String filename = null;
    protected Strategy strategy;
    private int fromLine;
    private int thruLine;

    public ReindenterTestCase() {;}  // when JUnit invokes a subclass outside a test suite

    public ReindenterTestCase(String filename, Strategy strategy)
    {
        this.filename = filename;
        this.strategy = strategy;
        this.setName("test");
    }

    public void test() throws Exception
    {
        if (filename == null) return; // when JUnit invokes this outside a test suite

        final IFile thisFile = importFile(DIR, filename);

        IFortranAST ast = PhotranVPG.getInstance().acquireTransientAST(thisFile);
        assertNotNull(ast);

        Reindenter.reindent(fromLine, thruLine, ast, strategy);

        thisFile.setContents(
            new ByteArrayInputStream(SourcePrinter.getSourceCodeFromAST(ast).getBytes()),
            true,
            false,
            new NullProgressMonitor());

        project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());

        assertEquals(
            readTestFile(filename + ".result"), // expected result
            readWorkspaceFile(filename));       // actual refactored file
    }

    @Override protected String readTestFile(String srcDir, String filename) throws IOException, URISyntaxException
    {
        String result = super.readTestFile(srcDir, filename);

        int startOffset = result.lastIndexOf('\n', result.indexOf("!<<<<<START")) + 1;
        int endOffset = result.indexOf("!<<<<<END");
        if (startOffset < 0 || endOffset < 0 || endOffset <= startOffset)
            throw new Error("Malformed test case");

        determineLinesFromOffsets(result, startOffset, endOffset);

        return result;
    }

    private void determineLinesFromOffsets(String result, int startOffset, int endOffset)
    {
        int line = 1;

        for (int i = 0; i < result.length(); i++)
        {
            if (i == startOffset)
                this.fromLine = line;

            if (i == endOffset)
                this.thruLine = line;

            if (result.charAt(i) == '\n')
                line++;
        }
    }

    protected String readTestFile(String filename) throws IOException, URISyntaxException
    {
        return super.readTestFile(DIR, filename);
    }
}
