/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.fdt.internal.core.parser;
import java.util.List;

import org.eclipse.fdt.core.parser.ITokenDuple;
import org.eclipse.fdt.core.parser.ast.ASTPointerOperator;
import org.eclipse.fdt.core.parser.ast.IASTArrayModifier;
import org.eclipse.fdt.core.parser.ast.IASTScope;
/**
 * @author jcamelon
 *
 */
public interface IDeclarator
{
	public IASTScope getScope();
    /**
     * @return
     */
    public abstract List getPointerOperators();
    public abstract void addPointerOperator(ASTPointerOperator ptrOp);
    /**
     * @param arrayMod
     */
    public abstract void addArrayModifier(IASTArrayModifier arrayMod);
    /**
     * @return
     */
    public abstract List getArrayModifiers();
    
	/**
	 * @param nameDuple
	 */
	public void setPointerOperatorName(ITokenDuple nameDuple);

	public ITokenDuple getPointerOperatorNameDuple();

}