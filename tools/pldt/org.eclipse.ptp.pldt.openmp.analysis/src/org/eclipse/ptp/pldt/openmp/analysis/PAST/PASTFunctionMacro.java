/**********************************************************************
 * Copyright (c) 2006,2012 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.analysis.PAST;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroParameter;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * 
 * @author pazel
 * 
 */
@SuppressWarnings("restriction")
public class PASTFunctionMacro extends PASTNode implements IASTPreprocessorFunctionStyleMacroDefinition {
	protected IASTPreprocessorFunctionStyleMacroDefinition macro_ = null;

	/**
	 * PASTFunctionMacro - "define" function macro (Constructor)
	 * 
	 * @param macro
	 */
	public PASTFunctionMacro(IASTPreprocessorFunctionStyleMacroDefinition macro) {
		super((ASTNode) macro);
		macro_ = macro;
	}

	/**
	 * @since 4.1
	 */
	public IASTNode copy(CopyStyle style) {
		return macro_.copy(style);
	}

	@Override
	public String getType() {
		return "#define"; //$NON-NLS-1$
	}

	/**
	 * Delegated methods for interface
	 * IASTPreprocessorFunctionStyleMacroDefinition
	 */
	public String getExpansion() {
		return macro_.getExpansion();
	}

	public IASTName getName() {
		return macro_.getName();
	}

	@SuppressWarnings("deprecation")
	public void setExpansion(String exp) {
		macro_.setExpansion(exp);
	}

	@SuppressWarnings("deprecation")
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

	public IASTFileLocation getExpansionLocation() {
		return macro_.getExpansionLocation();
	}

	public boolean isPartOfTranslationUnitFile() {
		return macro_.isPartOfTranslationUnitFile();
	}

	@Override
	public IASTNode getOriginalNode() {
		return macro_.getOriginalNode();
	}

}
