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
import java.io.IOException;

import junit.framework.ComparisonFailure;

import org.eclipse.photran.internal.core.lexer.sourceform.UnpreprocessedFixedSourceForm;
import org.eclipse.photran.internal.core.lexer.sourceform.UnpreprocessedFreeSourceForm;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.sourceform.ISourceForm;
import org.eclipse.photran.internal.core.sourceform.SourceForm;
import org.eclipse.photran.internal.core.util.SemanticError;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;

/**
 * Base class for a test case that takes an AST as input.
 * <p>
 * Test cases based on this will usually be created by a {@link PhotranTestSuiteFromFiles}.
 * 
 * @author Jeff Overbey
 */
public abstract class PhotranASTTestCase extends PhotranTestCase
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
    public PhotranASTTestCase(File file, boolean isFixedForm, String testCaseDescription)
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
            ASTExecutableProgramNode ast = parse(file, createSourceForm());
            assertTrue(ast != null);
            assertNull(PhotranVPG.findFirstErrorIn(ast));
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

    protected ISourceForm createSourceForm()
    {
        if (isFixedForm)
            return new UnpreprocessedFixedSourceForm();
        else if (file.getName().endsWith(".F90"))
            return SourceForm.of(null, file.getPath()); // delegate to create CPP source form
        else
            return new UnpreprocessedFreeSourceForm();
    }

    /**
     * Subclasses can override this method to do something interesting with the AST
     * 
     * @param ast The abstract syntax tree for the Fortran file that was parsed
     * @throws IOException 
     */
    protected abstract void handleAST(ASTExecutableProgramNode ast) throws IOException, SemanticError;
}
