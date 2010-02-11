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
package org.eclipse.photran.internal.tests;

import java.io.File;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.Parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;

/**
 * A test case which runs the parser over a file, expecting a successful parse.
 * Created by {@link ParserTestSuite}.
 * 
 * @author joverbey
 */
public class ParserTestCase extends AbstractParserTestCase
{
    public ParserTestCase(File file, boolean isFixedForm, String testCaseDescription)
    {
        super(file, isFixedForm, testCaseDescription);
    }

    @Override
    protected void handleAST(ASTExecutableProgramNode ast)
    {
        checkCorrectParenting(ast);
    }
    
    private void checkCorrectParenting(final ASTExecutableProgramNode ast)
    {
        ast.accept(new GenericASTVisitor()
        {
            private IASTNode expectedParent = null;
            
            @Override public void visitASTNode(IASTNode node)
            {
                if (node != ast && node.getParent() == null)
                    System.err.println("!"); // Set breakpoint here
                
                if (node == ast)
                    assertNull(node.getParent());
                else
                    assertNotNull(node.getParent());
                
                assertEquals(expectedParent, node.getParent());
                IASTNode myParent = expectedParent;
                
                expectedParent = node;
                traverseChildren(node);
                expectedParent = myParent;
            }
            
            @Override public void visitToken(Token token)
            {
                if (token.getParent() == null)
                    System.err.println("!"); // Set breakpoint here
                
                assertEquals(expectedParent, token.getParent());
            }
        });
    }
    
    public ParserTestCase() { super(null, false, ""); } // to keep JUnit quiet
}
