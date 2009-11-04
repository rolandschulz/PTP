/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring.infrastructure;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;

/**
 * Methods to reproduce source code from a (modified) AST.
 * 
 * @author Jeff Overbey
 */
public class SourcePrinter
{
    private static final String EOL = System.getProperty("line.separator");
    
    private SourcePrinter() {;}
    
    /** @return the source code for the given program, with the user's comments, capitalization, etc. retained */
    public static String getSourceCodeFromAST(IFortranAST ast)
    {
        return getSourceCodeFromAST(ast.getRoot());
    }
    
    /** @return the source code for the given program, with the user's comments, capitalization, etc. retained */
    public static String getSourceCodeFromAST(ASTExecutableProgramNode ast)
    {
        String result = getSourceCodeFromASTNode(ast);
        
        // When we read in the AST, we use a LineAppendingInputStream so that the
        // user does not have to have a final carriage return in their file.  However,
        // we should chop that off here.
        result = result.substring(0, Math.max(0, result.length() - EOL.length()));
        
        return result;
    }
    
    /** @return the source code for the given AST node, with the user's comments, capitalization, etc. retained */
    public static String getSourceCodeFromASTNode(IASTNode node)
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
        node.printOn(new PrintStream(out), null);
        return out.toString();
    }
}
