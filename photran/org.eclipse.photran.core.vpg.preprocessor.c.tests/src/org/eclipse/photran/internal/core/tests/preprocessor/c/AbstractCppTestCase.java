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
package org.eclipse.photran.internal.core.tests.preprocessor.c;

import java.io.File;
import java.io.IOException;

import junit.framework.ComparisonFailure;
import junit.framework.TestCase;

import org.eclipse.photran.internal.core.preprocessor.c.CppHelper;
import org.eclipse.photran.internal.core.preprocessor.c.IToken;
import org.eclipse.photran.internal.core.util.SemanticError;

/**
 * A test case for parsing a single file with the CPreprocessor. Based
 * on AbstractParserTestCase by joverbey.
 * @author Matthew Michelotti
 */
public abstract class AbstractCppTestCase extends TestCase
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
    public AbstractCppTestCase(File file, boolean isFixedForm, String testCaseDescription)
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
            //SourceForm sourceForm = (isFixedForm ? SourceForm.FIXED_FORM : SourceForm.UNPREPROCESSED_FREE_FORM);
            
            CppHelper cpp = new CppHelper(file);
            handleTokens(cpp.getRemainingTokens());
        }
        catch (ComparisonFailure f)
        {
            throw f;
        }
        catch (Throwable t)
        {
            throw new Exception(fileDescription, t);
        }
    }

    /**
     * Subclasses can override this method to perform the appropriate test on tokens
     * 
     * @param startToken - The first token in a linked-list of tokens
     *        obtained from the CPP. Get other tokens with IToken.getNext().
     * @throws IOException 
     */
    protected abstract void handleTokens(IToken startToken) throws IOException, SemanticError;
}
