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
package org.eclipse.photran.refactoring.tests.rename;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.core.vpg.util.LineCol;
import org.eclipse.photran.internal.core.refactoring.RenameRefactoring;
import org.eclipse.photran.refactoring.tests.RefactoringTestCase;
import org.eclipse.photran.refactoring.tests.rename.RenameTestSuite.Ident;

public abstract class RenameTestCase extends RefactoringTestCase
{
    private static final String DIR = "rename-test-code";

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

    /**
     * Renames the identifier at the given position in the given workspace file to the given new name.  Files in the workspace
     * are updated accordingly.
     */
    protected void doRename(String filename, LineCol lineCol, String newName, Set<String> allFiles) throws Exception
    {
        String description = "Attempt to rename identifier at " + lineCol;

        RenameRefactoring refactoring = createRefactoring(filename, lineCol, allFiles);

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
        RenameRefactoring refactoring = createRefactoring(filename, lineCol, allFiles);

        RefactoringStatus status = refactoring.checkInitialConditions(pm);
        if (status.hasError()) return status;

        refactoring.setNewNameForIdentifier(newName);

        status = refactoring.checkFinalConditions(pm);
        return status;
    }

    private RenameRefactoring createRefactoring(final String filename, final LineCol lineCol, final Set<String> allFiles) throws Exception
    {
    	PhotranVPG.getDatabase().clearDatabase();

        final IFile thisFile = importFile(DIR, filename);
        for (String f : allFiles)
            importFile(DIR, f);

        // Originally, we called acquireTransientAST on each IFile
        PhotranVPG.getInstance().ensureVPGIsUpToDate(new NullProgressMonitor());
        //project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor()); // Runs in separate thread... grrr...

//        System.out.println(filename);
//        for (String f : allFiles)
//            System.out.println(f);
//        PhotranVPG.getInstance().db.printOn(System.out);

        RenameRefactoring r = new RenameRefactoring();
        r.initialize(thisFile, new TextSelection(getLineColOffset(filename, lineCol), 0));
        return r;
    }

    protected String readTestFile(String filename) throws IOException, URISyntaxException
    {
        return super.readTestFile(DIR, filename);
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
    }
}
