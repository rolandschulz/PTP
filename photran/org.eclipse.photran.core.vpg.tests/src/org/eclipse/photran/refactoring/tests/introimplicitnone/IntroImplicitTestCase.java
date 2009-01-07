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
package org.eclipse.photran.refactoring.tests.introimplicitnone;

import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.core.vpg.util.LineCol;
import org.eclipse.photran.internal.core.refactoring.IntroImplicitNoneRefactoring;
import org.eclipse.photran.refactoring.tests.RefactoringTestCase;

public class IntroImplicitTestCase extends RefactoringTestCase
{
    private static final String DIR = "intro-implicit-test-code";

    private static NullProgressMonitor pm = new NullProgressMonitor();
    
    protected String filename;
    protected LineCol lineCol;

    public IntroImplicitTestCase() {;}  // when JUnit invokes a subclass outside a test suite

    public IntroImplicitTestCase(String filename, LineCol lineCol)
    {
        this.filename = filename;
        this.lineCol = lineCol;
        this.setName("test");
    }

    protected void doRefactoring() throws Exception
    {
        String description = "Attempt to introduce implicit none at " + lineCol;
            
        IntroImplicitNoneRefactoring refactoring = createRefactoring(filename, lineCol);
        
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

    private IntroImplicitNoneRefactoring createRefactoring(final String filename, final LineCol lineCol) throws Exception
    {
        final IFile thisFile = importFile(DIR, filename);
        
        //project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor()); // Runs in separate thread... grrr...
        
        return new IntroImplicitNoneRefactoring(thisFile, new TextSelection(getLineColOffset(filename, lineCol), 0));
    }
    
    protected String readTestFile(String filename) throws IOException, URISyntaxException
    {
        return super.readTestFile(DIR, filename);
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
            readTestFile(filename + ".result"), // expected result
            readWorkspaceFile(filename));       // actual refactored file
    }
}
