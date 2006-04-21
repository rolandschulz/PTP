/**********************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.analysis.PAST;

import org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroParameter;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.internal.core.parser.scanner2.LocationMap.ASTFunctionMacro;

/**
 * 
 * @author pazel
 *
 */
public class PASTFunctionMacro extends PASTNode implements IASTPreprocessorFunctionStyleMacroDefinition
{
	protected ASTFunctionMacro macro_ = null;
	
    /**
     * PASTFunctionMacro - "define" function macro (Constructor)
     * @param macro
     */
	public PASTFunctionMacro(ASTFunctionMacro macro)
	{
		super(macro);
		macro_ = macro;
	}
    
    public String getType()
    {
        return "#define";
    }

    /**
     * Delegated methods for interface IASTPreprocessorFunctionStyleMacroDefinition
     */
	public String getExpansion() {
		return macro_.getExpansion();
	}

	public IASTName getName() {
		return macro_.getName();
	}

	public void setExpansion(String exp) {
	  macro_.setExpansion(exp);	
	}

	public void setName(IASTName name) {
      macro_.setName(name);		
	}

	public void addParameter(IASTFunctionStyleMacroParameter parm) {
      macro_.addParameter(parm);		
	}

	public IASTFunctionStyleMacroParameter[] getParameters() {
		return macro_.getParameters();
	}

	public int getRoleForName(IASTName n) {
		return macro_.getRoleForName(n);
	}
	
}
