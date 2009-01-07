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

import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;

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
        ;
    }
    
    public ParserTestCase() { super(null, false, ""); } // to keep JUnit quiet
}
