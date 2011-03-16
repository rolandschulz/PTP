/*******************************************************************************
 * Copyright (c) 2010 Andrea Dranberg, John Hammonds, Rajashekhar Arasanal, 
 * Balaji Ambresh Rajkumar and Paramvir Singh.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrea Dranberg, John Hammonds, Rajashekhar Arasanal, Balaji Ambresh Rajkumar
 * and Paramvir Singh - Initial API and implementation
 * 
 *******************************************************************************/
package org.eclipse.photran.internal.tests.refactoring;

import java.util.Collections;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.photran.internal.core.refactoring.RemoveAssignedGotoRefactoring;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.PhotranWorkspaceTestCase;

/** 
 * White-box test cases for the remove assigned goto refactoring.
 * @author Andrea Dranberg
 * @author John Hammonds
 * @author Rajashekhar Arasanal
 * @author Balaji Ambresh Rajkumar
 * @author Paramvir Singh
 */
public class RemoveAssignedGotoWhiteBoxTestCase extends PhotranWorkspaceTestCase
{
    private static final String DIR = "refactoring-test-code/remove-assign-gotos-white-box";

    private static NullProgressMonitor pm = new NullProgressMonitor();

    protected ITextSelection selectionToExtract = null;

    public RemoveAssignedGotoWhiteBoxTestCase()
    {
        this.setName("test");
    }

    /**
     * Test case to check if the tool collects correct number of ASSIGN
     * and assigned GOTOs from the input file one_label_no_goto.f90.
     */
    public void test() throws Exception
    {
        IFile thisFile = importFile(Activator.getDefault(), DIR, "one_label_no_goto.f90");
        RemoveAssignedGotoRefactoring tool = new RemoveAssignedGotoRefactoring();
        tool.initialize(Collections.singletonList(thisFile));
        tool.checkInitialConditions(pm);
        RemoveAssignedGotoRefactoring.FileInfo data =
            RemoveAssignedGotoRefactoring.FileInfoFactory.getInstance(thisFile, tool.getVpg());
        assertTrue(data.getAssignedGotoStmtList().size() == 0);
        assertTrue(data.getAssignedStmtList().size() == 1);
    }
}
