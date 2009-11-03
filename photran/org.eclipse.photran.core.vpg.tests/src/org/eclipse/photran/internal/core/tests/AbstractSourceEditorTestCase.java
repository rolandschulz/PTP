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
package org.eclipse.photran.internal.core.tests;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.SourcePrinter;
import org.eclipse.photran.internal.core.util.SemanticError;

public abstract class AbstractSourceEditorTestCase extends TestCase
{
    private ASTExecutableProgramNode result = null;
    
    /**
     * Parses the given file as free form Fortran source code 
     */
    public ASTExecutableProgramNode load(String filename) throws Exception
    {
        new AbstractParserTestCase(new File("../org.eclipse.photran.core.vpg.tests/source-editor-test-code/" + filename), false, AbstractSourceEditorTestCase.class.getName())
        {
            @Override
            protected void handleAST(ASTExecutableProgramNode ast) throws IOException, SemanticError
            {
                result = ast;
            }
        }.test();
        
        if (result == null) throw new Exception("Unable to parse " + filename);
        return result;
    }
    
    /**
     * Checks whether two ASTs produce identical source code
     */
    protected void assertEquals(ASTExecutableProgramNode expected, ASTExecutableProgramNode actual)
    {
        assertEquals(SourcePrinter.getSourceCodeFromAST(expected),
                     SourcePrinter.getSourceCodeFromAST(actual));
    }
}
