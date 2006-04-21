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

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorObjectStyleMacroDefinition;
import org.eclipse.cdt.internal.core.parser.scanner2.LocationMap.ASTObjectMacro;

/**
 * 
 * @author  pazel
 *
 */
public class PASTObjectMacro extends PASTNode implements IASTPreprocessorObjectStyleMacroDefinition
{
    protected ASTObjectMacro objMacro_ = null;
    
    /**
     * PASTObjectMacro - constructor
     * @param objMacro: ASTObjectMacro
     */
    public PASTObjectMacro(ASTObjectMacro objMacro)
    {
        super(objMacro);
        objMacro_ = objMacro;
    }
    
    public String getType()
    {
        return "#define";
    }

    /**
     * Delegated methods for interface IASTPreprocessorObjectStyleMacroDefinition
     */
    public int getRoleForName(IASTName n)
    {
        return objMacro_.getRoleForName(n);
    }

    public String getExpansion()
    {
        return objMacro_.getExpansion();
    }

    public IASTName getName()
    {
        return objMacro_.getName();
    }

    public void setExpansion(String exp)
    {
        objMacro_.setExpansion(exp);        
    }

    public void setName(IASTName name)
    {
        objMacro_.setName(name);        
    }

}
