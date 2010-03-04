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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.photran.internal.core.preprocessor.c.CppHelper;
import org.eclipse.photran.internal.core.preprocessor.c.IToken;
import org.eclipse.photran.internal.core.util.SemanticError;

/**
 * A test case for making sure that code processed by the CPreprocessor
 * can be restored to the original source code. Based on the
 * SourceReproductionTestCase class.
 * @author Matthew Michelotti
 */
public class CppIsolatedSourceReproductionTestCase extends AbstractCppTestCase
{
    //private String filename;
    //private boolean print;
    
    public CppIsolatedSourceReproductionTestCase(File file, boolean isFixedForm, String testCaseDescription, boolean print)
    {
        super(file, isFixedForm, testCaseDescription);
        //filename = file.getName();
        //this.print = print;
    }

    @Override
    protected void handleTokens(IToken startToken) throws IOException, SemanticError
    {
        String originalSourceCode = getSourceCodeFromFile(file);
        String reproducedSourceCode = getSourceCodeFromTokens(startToken);
        
        assertEquals(originalSourceCode, reproducedSourceCode);
        
        /*int line = 1;
        int col = 1;
        for(int i = 0; i < originalSourceCode.length(); i++) {
            char origChar = originalSourceCode.charAt(i);
            
            if(reproducedSourceCode.length() <= i) {
                fail("Reproduced code ended prematurely at line "
                    + line + ", col " + col + " of " + filename);
            }
            
            if(origChar != reproducedSourceCode.charAt(i)) {
                fail("Incorrect source reproduction at line "
                    + line + ", col" + col + " of " + filename);
            }
            
            if(origChar == '\n') {
                line++;
                col = 1;
            }
            else col++;
        }*/
    }
    
    private static String getSourceCodeFromFile(File file) throws IOException
    {
        StringBuffer sb = new StringBuffer(4096);
        BufferedReader in = new BufferedReader(new FileReader(file));
        for (int ch = in.read(); ch >= 0; ch = in.read())
            sb.append((char)ch);
        in.close();
        return sb.toString();
    }
    
    private String getSourceCodeFromTokens(IToken startToken) {
    	/*
        if(print) {
            System.out.println();
            System.out.println("----------------------------------------------------");
            System.out.println("file: " + filename);
            System.out.println("----------------------------------------------------");
            for(IToken t = startToken; t != null; t = t.getNext()) {
                System.out.println(CppHelper.getTokenDetails(t, "~", "|", false));
            }
        }
        */
        
        return CppHelper.reproduceSourceCode(startToken);
    }
    
    public CppIsolatedSourceReproductionTestCase() { super(null, false, ""); } // to keep JUnit quiet

}
