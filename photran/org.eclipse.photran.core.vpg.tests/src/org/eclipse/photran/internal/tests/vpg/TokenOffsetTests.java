/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.tests.vpg;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.PhotranWorkspaceTestCase;

/**
 * Unit tests for checking token offsets
 *
 * @author Jeff Overbey
 */
public class TokenOffsetTests extends PhotranWorkspaceTestCase
{
    private static final String DIR = "refactoring-test-code/encapsulate-variable/test-basic-integer";
    private static final String FILE = "test-encap-01a-basic-integer.f90";
    
    private IFile file;

    @Override public void setUp() throws Exception
    {
        super.setUp();

        file = importFile(Activator.getDefault(), DIR, FILE);
        PhotranVPG.getInstance().ensureVPGIsUpToDate(new NullProgressMonitor());
        //PhotranVPG.getDatabase().printOn(System.out);
    }

    public void testListAllModules() throws Exception
    {
        PhotranVPG vpg = PhotranVPG.getInstance();
        IFortranAST ast = vpg.acquirePermanentAST(file);
        
        // No preprocessing in this file
        for (Token t : ast)
        {
            assertEquals(t.getLogicalFile(), t.getPhysicalFile().getIFile());
            assertEquals(t.getFileOffset(), t.getStreamOffset());
        }
        
        vpg.releaseAST(file);
    }
}
