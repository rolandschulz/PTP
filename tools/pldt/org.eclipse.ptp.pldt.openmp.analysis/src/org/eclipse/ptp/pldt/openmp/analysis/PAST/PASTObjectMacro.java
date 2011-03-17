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

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorObjectStyleMacroDefinition;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * 
 * @author pazel
 * 
 */
public class PASTObjectMacro extends PASTNode implements IASTPreprocessorObjectStyleMacroDefinition {
	protected IASTPreprocessorObjectStyleMacroDefinition objMacro_ = null;

	/**
	 * PASTObjectMacro - constructor
	 * 
	 * @param objMacro
	 *            : ASTObjectMacro
	 */
	public PASTObjectMacro(IASTPreprocessorObjectStyleMacroDefinition objMacro) {
		super((ASTNode) objMacro);
		objMacro_ = objMacro;
	}

	/**
	 * @since 4.1
	 */
	public IASTNode copy(CopyStyle style) {
		return objMacro_.copy(style);
	}

	@Override
	public String getType() {
		return "#define"; //$NON-NLS-1$
	}

	/**
	 * Delegated methods for interface
	 * IASTPreprocessorObjectStyleMacroDefinition
	 */
	public int getRoleForName(IASTName n) {
		return objMacro_.getRoleForName(n);
	}

	public String getExpansion() {
		return objMacro_.getExpansion();
	}

	public IASTName getName() {
		return objMacro_.getName();
	}

	public void setExpansion(String exp) {
		objMacro_.setExpansion(exp);
	}

	public void setName(IASTName name) {
		objMacro_.setName(name);
	}

	public IASTFileLocation getExpansionLocation() {
		return objMacro_.getExpansionLocation();
	}

	public boolean isPartOfTranslationUnitFile() {
		return objMacro_.isPartOfTranslationUnitFile();
	}

}
