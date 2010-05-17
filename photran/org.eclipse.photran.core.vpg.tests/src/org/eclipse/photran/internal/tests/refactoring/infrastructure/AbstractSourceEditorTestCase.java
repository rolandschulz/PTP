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
package org.eclipse.photran.internal.tests.refactoring.infrastructure;

import java.io.File;

import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.SourcePrinter;
import org.eclipse.photran.internal.tests.PhotranTestCase;

public abstract class AbstractSourceEditorTestCase extends PhotranTestCase
{
    /**
     * Parses the given file as free form Fortran source code
     */
    public ASTExecutableProgramNode load(String filename) throws Exception
    {
        return parse(new File("../org.eclipse.photran.core.vpg.tests/refactoring-test-code/infrastructure/source-editor/" + filename));
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
