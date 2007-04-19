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

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.internal.core.parser.scanner2.LocationMap.ASTInclusionStatement;

/**
 * 
 * @author pazel
 *
 */
public class PASTInclusionStatement extends PASTNode implements IASTPreprocessorIncludeStatement
{
	protected ASTInclusionStatement incl_ = null;
	
    /**
     * PASTInclusionStatement - constructor
     * @param incl: ASTInclusionStatement
     */
	public PASTInclusionStatement(ASTInclusionStatement incl)
	{
		super(incl);
		incl_ = incl;
	}
    
    public String getType()
    {
        return "#include";
    }


	public String getPath() {
		return incl_.getPath();
	}

	// cdt40
	public IASTName getName() {
		return incl_.getName();
	}

	// cdt40
	public boolean isSystemInclude() {
		return incl_.isSystemInclude();
	}
	// cdt40
	public boolean isActive() {
		return incl_.isActive();
	}
	// cdt40
	public boolean isResolved() {
		return incl_.isResolved();
	}
	
}
