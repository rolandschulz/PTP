/*******************************************************************************
 * Copyright (c) 2008 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.tests.refactoring.rename;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.internal.core.refactoring.RenameRefactoring;
import org.eclipse.photran.internal.core.refactoring.interfaces.IRenameRefactoring;
import org.eclipse.photran.internal.core.util.LineCol;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.PhotranWorkspaceTestCase;
import org.eclipse.photran.internal.tests.refactoring.rename.RenameTestSuite.Ident;

public abstract class RenameTestCase extends PhotranWorkspaceTestCase
{
    private static final String DIR = "refactoring-test-code/rename";

    private static NullProgressMonitor pm = new NullProgressMonitor();

    protected String filename;
    protected Ident variable;
    protected LineCol position;
    protected String newName;

    public RenameTestCase() {;}  // when JUnit invokes a subclass outside a test suite

    public RenameTestCase(String filename, Ident variable, LineCol position, String newName)
    {
        this.filename = filename;
        this.variable = variable;
        this.newName = newName;
        this.position = position;
        this.setName("test");
    }
    
//    public void testLevenshtein()
//    {
//        assertEquals(0, levenshteinDistance("", ""));
//        assertEquals(0, levenshteinDistance("kitten", "kitten"));
//        assertEquals(6, levenshteinDistance("", "kitten"));
//        assertEquals(6, levenshteinDistance("kitten", ""));
//        assertEquals(3, levenshteinDistance("kitten", "sitting"));
//        assertEquals(3, levenshteinDistance("kitten", "kit"));
//        assertEquals(1, levenshteinDistance("kitten", "kittten"));
//        assertEquals(2, levenshteinDistance("kitten", "kien"));
//    }

    /**
     * Computes the Levenshtein distance between two strings.
     *
     * @return the Levenshtein distance between <code>s</code> and <code>t</code>
     */
    /*
     * This is a well-known algorithm; see, for example,
     * http://en.wikipedia.org/wiki/Levenshtein_distance
     */
    protected int levenshteinDistance(String s, String t)
    {
        int m = s.length(), n = t.length();

        int[][] d = new int[m+1][n+1];

        for (int i = 1; i <= m; i++)
            d[i][0] = i;
        for (int j = 1; j <= n; j++)
            d[0][j] = j;

        for (int i = 1; i <= m; i++)
        {
            for (int j = 1; j <= n; j++)
            {
                int cost = s.charAt(i-1) == t.charAt(j-1) ? 0 : 1;
                d[i][j] = min(d[i-1][j] + 1,       // deletion
                              d[i][j-1] + 1,       // insertion
                              d[i-1][j-1] + cost); // substitution
            }
        }

        return d[m][n];
    }

    private int min(int a, int b, int c)
    {
        return Math.min(Math.min(a, b), c);
    }

    protected void assertLevDist(String errorMessage, int expected, String s, String t)
    {
        int actual = levenshteinDistance(s, t);

        if (actual != expected)
        {
            // Use assertEquals so that JUnit will pop up a comparison viewer
            assertEquals("Unexpected Levenshtein distance " + actual + " (expected " + expected + ") " + errorMessage,
                         s,
                         t);
        }
    }

    /**
     * Renames the identifier at the given position in the given workspace file to the given new name.  Files in the workspace
     * are updated accordingly.
     */
    protected void doRename(String filename, LineCol lineCol, String newName, Set<String> allFiles) throws Exception
    {
        String description = "Attempt to rename identifier at " + lineCol;

        IRenameRefactoring refactoring = createRefactoring(filename, lineCol, allFiles);

        RefactoringStatus status = refactoring.checkInitialConditions(pm);
        assertTrue(description + " failed initial precondition check: " + status.toString(), !status.hasError());

        refactoring.setNewNameForIdentifier(newName);

        status = refactoring.checkFinalConditions(pm);
        assertTrue(description + " failed final precondition check: " + status.toString(), !status.hasError());

        Change change = refactoring.createChange(pm);
        assertNotNull(description + " returned null Change object", change);
        assertTrue(description + " returned invalid Change object", change.isValid(pm).isOK());
        change.perform(pm);

        project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
    }

    /**
     * Attempts to renames the identifier at the given position in the given workspace file to the given new name, with
     * the expectation that at least one precondition will fail.  Returns the first failing status.  Does not actually
     * perform the refactoring.
     */
    protected RefactoringStatus attemptRename(String filename, LineCol lineCol, String newName, Set<String> allFiles) throws Exception
    {
        IRenameRefactoring refactoring = createRefactoring(filename, lineCol, allFiles);

        RefactoringStatus status = refactoring.checkInitialConditions(pm);
        if (status.hasError()) return status;

        refactoring.setNewNameForIdentifier(newName);

        status = refactoring.checkFinalConditions(pm);
        return status;
    }

    private IRenameRefactoring createRefactoring(final String filename, final LineCol lineCol, final Set<String> allFiles) throws Exception
    {
    	PhotranVPG.getInstance().clearDatabase();

        final IFile thisFile = importFile(Activator.getDefault(), DIR, filename);
        for (String f : allFiles)
            importFile(Activator.getDefault(), DIR, f);

        // Originally, we called acquireTransientAST on each IFile
        PhotranVPG.getInstance().ensureVPGIsUpToDate(new NullProgressMonitor());
        //project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor()); // Runs in separate thread... grrr...

//        System.out.println(filename);
//        for (String f : allFiles)
//            System.out.println(f);
//        PhotranVPG.getInstance().db.printOn(System.out);

        IRenameRefactoring r = createRefactoring();
        r.initialize(thisFile, new TextSelection(getLineColOffset(filename, lineCol), 0));
        return r;
    }

    protected abstract IRenameRefactoring createRefactoring();

    protected String readTestFile(String filename) throws IOException, URISyntaxException
    {
        return super.readTestFile(Activator.getDefault(), DIR, filename);
    }

    public static class ExpectSuccess extends RenameTestCase
    {
        public ExpectSuccess() {;}  // when JUnit invokes this outside a test suite

        public ExpectSuccess(String filename, Ident variable, LineCol position, String newName)
        {
            super(filename, variable, position, newName);
        }

        /**
         * Given an array with all of the positions of identifiers that should be renamed together, try applying the Rename refactoring to
         * each, and make sure all the others change with it.
         */
        public void test() throws Exception
        {
            if (filename == null || variable == null || newName == null) return; // when JUnit invokes this outside a test suite
            //System.out.println("***** " + variable.getName() + " ==> " + newName + " *****");

            doRename(filename, position, newName, variable.getFiles());
            for (String filename : variable.getFiles())
            {
                int totalLevDist = variable.getReferences(filename).length * levenshteinDistance(variable.getName(), newName);
                assertLevDist("renaming " + variable.getName() + " to " + newName + " (" + position + " - " + variable.getReferences(filename).length + " occurrences)",
                              totalLevDist,
                              readTestFile(filename), // original file
                              readWorkspaceFile(filename)); // refactored file
            }
        }

        @Override protected IRenameRefactoring createRefactoring()
        {
            return new RenameRefactoring();
        }
    }

    public static class ExpectFailure extends RenameTestCase
    {
        public ExpectFailure() {;}  // when JUnit invokes this outside a test suite

        public ExpectFailure(String filename, Ident variable, LineCol position, String newName)
        {
            super(filename, variable, position, newName);
        }

        /**
         * Given an array with all of the positions of identifiers that should be renamed together, try applying the Rename refactoring to
         * each, and make sure a precondition failure is raised.
         * @throws Exception
         */
        public void test() throws Exception
        {
            if (filename == null || variable == null || newName == null) return; // when JUnit invokes this outside a test suite
            //System.out.println("***** " + variable.getName() + " ==> " + newName + " *****");

            RefactoringStatus status = attemptRename(filename, position, newName, variable.getFiles());
            assertTrue("Attempting to rename " + variable.getName() + " to " + newName + " in " + filename
                           + " (" + position + " - " + variable.getReferences(filename).length + " occurrences) "
                           + "should have failed precondition checking.",
                       status.hasError());
        }

        @Override protected IRenameRefactoring createRefactoring()
        {
            return new RenameRefactoring();
        }
    }
}
