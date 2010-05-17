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
package org.eclipse.photran.internal.tests.vpg;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.PhotranWorkspaceTestCase;

/**
 * Unit tests for {@link Definition#resolveInterfaceBinding()}
 * and {@link Definition#findMatchingDeclarationsInInterfaces()}
 *
 * @author Jeff Overbey
 */
public class InterfaceLinkTests extends PhotranWorkspaceTestCase
{
    private static final String DIR = "vpg-test-code/interface-links";

    private IFile interface1;
    private IFile definition1;
    private IFile definition2;
    private IFile interface2;

    private Definition if1;
    private Definition if2;
    private Definition if3;
    private Definition if4;
    private Definition def1;
    private Definition def2;
    private Definition def3;

    @Override public void setUp() throws Exception
    {
        super.setUp();

        /* The four files are imported in this order so that
         * the INTERFACE block in the first file is seen /before/
         * all of the corresponding subprogram declarations,
         * while the INTERFACE block in the last file is seen
         * /after/ all of the corresponding subprograms.
         */

        interface1 = importFile(Activator.getDefault(), DIR, "interface1.f90");
        definition1 = importFile(Activator.getDefault(), DIR, "definition1.f90");
        definition2 = importFile(Activator.getDefault(), DIR, "definition2.f90");
        interface2 = importFile(Activator.getDefault(), DIR, "interface2.f90");
        //                    File      Line  Column
        if1  = findDefinition(interface1,  2, 20); // Ordinary INTERFACE block
        if2  = findDefinition(interface1, 10, 24); // INTERFACE block for a subprogram parameter
        if3  = findDefinition(interface1, 17, 24); // Ordinary INTERFACE block
        if4  = findDefinition(interface2,  2, 20); // Ordinary INTERFACE block
        def1 = findDefinition(interface1, 24, 18);
        def2 = findDefinition(definition1, 1, 18);
        def3 = findDefinition(definition2, 1, 18);
    }

    public void testResolveInterfaceBinding() throws Exception
    {
        checkContainsAllDefinitions(if1.resolveInterfaceBinding());
        assertTrue(if2.resolveInterfaceBinding().isEmpty()); // Interface for subprogram parameter
        checkContainsAllDefinitions(if3.resolveInterfaceBinding());
        checkContainsAllDefinitions(if4.resolveInterfaceBinding());
    }

    public void testFindMatchingDeclarationsInInterfaces() throws Exception
    {
        checkContainsAllInterfaces(def1.findMatchingDeclarationsInInterfaces());
        checkContainsAllInterfaces(def2.findMatchingDeclarationsInInterfaces());
        checkContainsAllInterfaces(def3.findMatchingDeclarationsInInterfaces());
    }

    private void checkContainsAllDefinitions(Collection<Definition> defs)
    {
        assertEquals(3, defs.size());
        assertTrue(defs.contains(def1));
        assertTrue(defs.contains(def2));
        assertTrue(defs.contains(def3));
    }

    private void checkContainsAllInterfaces(Collection<Definition> defs)
    {
        assertEquals(3, defs.size());
        assertTrue(defs.contains(if1));
        assertFalse(defs.contains(if2)); // Interface for subprogram parameter
        assertTrue(defs.contains(if3));
        assertTrue(defs.contains(if4));
    }

    private Definition findDefinition(IFile file, int line, int col)
    {
        IFortranAST ast = PhotranVPG.getInstance().acquireTransientAST(file); assertNotNull(ast);
        Token token = findTokenByLineCol(ast, line, col); assertNotNull(token);
        List<Definition> defs = token.resolveBinding(); assertEquals(1, defs.size());
        Definition def = defs.get(0);
        assertEquals(file, def.getTokenRef().getFile());
        assertEquals(token.getFileOffset(), def.getTokenRef().getOffset());
        return def;
    }

    private Token findTokenByLineCol(IFortranAST ast, int line, int col)
    {
        for (Token token : ast)
            if (token.getLine() == line && token.getCol() == col)
                return token;

        return null;
    }
}
