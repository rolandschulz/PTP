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
package org.eclipse.photran.refactoring.tests.interchangeloops;

import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.core.refactoring.ExtractProcedureRefactoring;
import org.eclipse.photran.internal.core.refactoring.InterchangeLoopsRefactoring;
import org.eclipse.photran.internal.core.refactoring.IntroImplicitNoneRefactoring;
import org.eclipse.photran.internal.core.refactoring.RenameRefactoring;
import org.eclipse.photran.refactoring.tests.RefactoringTestCase;

/**
 * 
 * @author Tim
 */
public class InterchangeLoopsTestCase extends RefactoringTestCase
{
    private String myFilename = null;
    private NullProgressMonitor pm = new NullProgressMonitor();
    private ITextSelection selection = null;
    private static final String DIR = "interchange-loops-test-code";
    
    public InterchangeLoopsTestCase(){;}
    
    public InterchangeLoopsTestCase(String filename, Object ignore)
    {
        this.myFilename = filename;
        this.setName("test");
    }
    
    protected void doRefactoring() throws Exception
    {
        String description = "Attempt to interchange loops in " + myFilename;
            
        InterchangeLoopsRefactoring refactoring = createRefactoring(myFilename);
        
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
    
    protected InterchangeLoopsRefactoring createRefactoring(String filename) throws Exception
    {
        final IFile thisFile = importFile(DIR, filename);
        return new InterchangeLoopsRefactoring(thisFile, selection);
    }
    
    @Override protected String readTestFile(String srcDir, String filename) throws IOException, URISyntaxException
    {
        String result = super.readTestFile(srcDir, filename);
        
        int startOffset = result.lastIndexOf('\n', result.indexOf("!<<<<<START")) + 1;
        int endOffset = result.indexOf("!<<<<<END");
        if (startOffset <= 0 || endOffset < 0 || endOffset <= startOffset)
            throw new Error("Malformed test case");
        
        selection = new TextSelection(startOffset, endOffset-startOffset);
        
        return result;
    }

    protected String readTestFile(String filename) throws IOException, URISyntaxException
    {
        return super.readTestFile(DIR, filename);
    }
    
    public void test() throws Exception
    {
        if (myFilename == null) return; // when JUnit invokes this outside a test suite
        
        doRefactoring();
        assertEquals(
            readTestFile(myFilename + ".result"), // expected result
            readWorkspaceFile(myFilename));       // actual refactored file
    }
    
    public static class InterchangeLoopsFailureTest extends InterchangeLoopsTestCase
    {
        private static String failFilename;
        
        public InterchangeLoopsFailureTest(){;}
        
        public InterchangeLoopsFailureTest(String filename, Object ignore)
        {
            super(filename, ignore);
            failFilename = filename;
        }
        
        @Override
        protected void doRefactoring() throws Exception
        {
            String description = "Attempt to interchange loops in " + failFilename;
                
            InterchangeLoopsRefactoring refactoring = createRefactoring(failFilename);
            
            RefactoringStatus status = refactoring.checkInitialConditions(new NullProgressMonitor());
            assertTrue(status.hasFatalError());
        }

        @Override
        public void test() throws Exception
        {
            doRefactoring();
        }   
    }
}
