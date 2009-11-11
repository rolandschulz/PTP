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
package org.eclipse.photran.internal.tests.refactoring.infrastructure;

import java.io.IOException;

import org.eclipse.photran.internal.core.SyntaxException;
import org.eclipse.photran.internal.core.lexer.LexerException;
import org.eclipse.photran.internal.core.parser.ASTEntityDeclNode;
import org.eclipse.photran.internal.core.parser.ASTTypeDeclarationStmtNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.tests.BaseTestCase;

/**
 * Test to verify that ASTSeparatedListNode#remove correctly removes commas
 * 
 * @author Jeff Overbey
 */
public class TestSeparatedListRemoval extends BaseTestCase
{
    public void testSeparatedListRemoval() throws IOException, LexerException, SyntaxException
    {
        ASTTypeDeclarationStmtNode stmt = parseStmt("integer a, b, c, d, e");
        IASTListNode<ASTEntityDeclNode> decls = stmt.getEntityDeclList();
        decls.remove(0);
        assertEquals("integer b, c, d, e", stmt.toString().trim());
        ASTEntityDeclNode b = decls.get(0);
        decls.remove(b);
        assertEquals("integer c, d, e", stmt.toString().trim());
        decls.remove(1);
        assertEquals("integer c, e", stmt.toString().trim());
        decls.remove(1);
        assertEquals("integer c", stmt.toString().trim());
    }
}
