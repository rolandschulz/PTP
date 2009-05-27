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

import junit.framework.ComparisonFailure;
import junit.framework.TestCase;

import org.eclipse.photran.core.vpg.util.SemanticError;
import org.eclipse.photran.internal.core.lexer.LexerFactory;
import org.eclipse.photran.internal.core.lexer.SourceForm;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.Parser;

/**
 * A test case for parsing a single file.  Created by <code>ParserTestSuite</code>.
 * 
 * @author joverbey
 */
public abstract class AbstractParserTestCase extends TestCase
{
    protected File file = null;
    protected boolean isFixedForm = false;
    protected String fileDescription = null;

    /**
     * Constructor
     * 
     * @param filename the file to parse
     * @param isFixedForm true iff the file is in fixed format
     */
    public AbstractParserTestCase(File file, boolean isFixedForm, String testCaseDescription)
    {
        super("test"); // name of method to run
        this.file = file;
        this.isFixedForm = isFixedForm;
        this.fileDescription = testCaseDescription;
    }
    
    /**
     * Method called by JUnit: Parses the given file.
     * 
     * @throws Exception
     */
    public final void test() throws Exception
    {
        if (file == null) return; // for when JUnit invokes the no-arguments constructor and reflectively invokes this method
        
        try
        {
            SourceForm sourceForm = (isFixedForm ? SourceForm.FIXED_FORM : SourceForm.UNPREPROCESSED_FREE_FORM); /*SourceForm.preprocessedFreeForm(new IncludeLoaderCallback(null)
            {
                @Override public InputStream getIncludedFileAsStream(String fileToInclude) throws FileNotFoundException
                {
                    throw new FileNotFoundException("INCLUDE lines not supported in JUnit tests");
                }
            }));*/
            ASTExecutableProgramNode ast = new Parser().parse(LexerFactory.createLexer(file, sourceForm, true));
            assertTrue(ast != null);
            handleAST(ast);
        }
        catch (ComparisonFailure f)
        {
            throw f;
        }
        catch (Throwable t)
        {
            System.err.println(file.getName());
            throw new Exception(fileDescription, t);
        }
    }

    /**
     * Subclasses can override this method to do something interesting with the AST
     * 
     * @param ast The abstract syntax tree for the Fortran file that was parsed
     * @throws IOException 
     */
    protected abstract void handleAST(ASTExecutableProgramNode ast) throws IOException, SemanticError;
}
