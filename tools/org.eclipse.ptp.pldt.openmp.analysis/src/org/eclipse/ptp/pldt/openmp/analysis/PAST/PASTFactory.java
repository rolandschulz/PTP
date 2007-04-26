/**********************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.analysis.PAST;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.internal.core.parser.scanner2.LocationMap.ASTElif;
import org.eclipse.cdt.internal.core.parser.scanner2.LocationMap.ASTElse;
import org.eclipse.cdt.internal.core.parser.scanner2.LocationMap.ASTEndif;
import org.eclipse.cdt.internal.core.parser.scanner2.LocationMap.ASTError;
import org.eclipse.cdt.internal.core.parser.scanner2.LocationMap.ASTFunctionMacro;
import org.eclipse.cdt.internal.core.parser.scanner2.LocationMap.ASTIf;
import org.eclipse.cdt.internal.core.parser.scanner2.LocationMap.ASTIfdef;
import org.eclipse.cdt.internal.core.parser.scanner2.LocationMap.ASTIfndef;
import org.eclipse.cdt.internal.core.parser.scanner2.LocationMap.ASTInclusionStatement;
import org.eclipse.cdt.internal.core.parser.scanner2.LocationMap.ASTObjectMacro;
import org.eclipse.cdt.internal.core.parser.scanner2.LocationMap.ASTPragma;
import org.eclipse.cdt.internal.core.parser.scanner2.LocationMap.ASTUndef;


/**
 * Create our new PAST types from old AST types
 * @author pazel
 */
public class PASTFactory 
{
    /**
     * PASTFactoryMake - Conversion from AST to PAST
     * @param node: IASTPreprocessorStatement
     * @return PASTNode
     */
    public static PASTNode PASTFactoryMake(IASTPreprocessorStatement node)
    {
    	if (node instanceof ASTEndif)
    		return new PASTEndif((ASTEndif)node);
    	else if (node instanceof ASTElif)
    		return new PASTElif((ASTElif)node);
    	else if (node instanceof ASTElse)
    		return new PASTElse((ASTElse)node);
    	else if (node instanceof ASTIfndef)
    		return new PASTIfndef((ASTIfndef)node);
    	else if (node instanceof ASTIfdef)
    		return new PASTIfdef((ASTIfdef)node);
    	else if (node instanceof ASTIf)
    		return new PASTIf((ASTIf)node);
    	else if (node instanceof ASTError)
    		return new PASTError((ASTError)node);
    	else if (node instanceof ASTPragma)
    		return new PASTPragma((ASTPragma)node);
    	else if (node instanceof ASTUndef)
    		return new PASTUndef((ASTUndef)node);
    	else if (node instanceof ASTInclusionStatement)
    		return new PASTInclusionStatement((ASTInclusionStatement)node);
    	else if (node instanceof ASTFunctionMacro)
    		return new PASTFunctionMacro((ASTFunctionMacro)node);
        else if (node instanceof ASTObjectMacro)
            return new PASTObjectMacro((ASTObjectMacro)node);
        else{
        	return null;//what is node?   LocationMap$ASTWarning.
        }
    }
}
