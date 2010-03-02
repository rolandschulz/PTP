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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.photran.internal.core.lexer.CPreprocessedSourceForm;
import org.eclipse.photran.internal.core.lexer.SourceForm;
import org.eclipse.photran.internal.core.lexer.preprocessor.fortran_include.IncludeLoaderCallback;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.SourcePrinter;
import org.eclipse.photran.internal.tests.AbstractParserTestCase;

public class CppIntegratedSourceReproductionTestCase extends AbstractParserTestCase
{
    public CppIntegratedSourceReproductionTestCase(File file, boolean isFixedForm, String testCaseDescription)
    {
        super(file, isFixedForm, testCaseDescription);
    }

    @Override protected SourceForm createSourceForm()
    {
        return new CPreprocessedSourceForm(new IncludeLoaderCallback(null)
        {
            @Override public InputStream getIncludedFileAsStream(String fileToInclude) throws FileNotFoundException
            {
                throw new FileNotFoundException("#include lines not supported in JUnit tests");
            }
        });
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
