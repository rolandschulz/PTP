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
package org.eclipse.photran.internal.core.tests.preprocessor.c;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.photran.internal.core.lexer.sourceform.CPreprocessedFreeSourceForm;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.SourcePrinter;
import org.eclipse.photran.internal.core.sourceform.ISourceForm;
import org.eclipse.photran.internal.tests.AbstractParserTestCase;

public class CppIntegratedSourceReproductionTestCase extends AbstractParserTestCase
{
    public CppIntegratedSourceReproductionTestCase(File file, boolean isFixedForm, String testCaseDescription)
    {
        super(file, isFixedForm, testCaseDescription);
    }

    @Override protected ISourceForm createSourceForm()
    {
        return new CPreprocessedFreeSourceForm();
    }

    @Override
    protected void handleAST(ASTExecutableProgramNode ast) throws IOException
    {
        String originalSourceCode = getSourceCodeFromFile(file);
        transform(ast);
        String reproducedSourceCode = getSourceCodeFromAST(ast);
        assertEquals(originalSourceCode.replaceAll("\\r", "").trim(), reproducedSourceCode.replaceAll("\\r", "").trim());
    }

    protected void transform(ASTExecutableProgramNode ast)
    {
        // Subclass and override to transform AST first
    }

    private String getSourceCodeFromFile(File file) throws IOException
    {
        StringBuffer sb = new StringBuffer(4096);
        BufferedReader in = new BufferedReader(new FileReader(file));
        for (int ch = in.read(); ch >= 0; ch = in.read())
            sb.append((char)ch);
        in.close();
        return sb.toString();
    }
    
    private String getSourceCodeFromAST(ASTExecutableProgramNode ast)
    {
        return SourcePrinter.getSourceCodeFromAST(ast);
    }

    public CppIntegratedSourceReproductionTestCase() { super(null, false, ""); } // to keep JUnit quiet
}
