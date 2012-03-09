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

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfStatement;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * 
 * @author pazel
 * 
 */
public class PASTIf extends PASTNode implements IASTPreprocessorIfStatement {
	protected IASTPreprocessorIfStatement if_ = null;

	/**
	 * PASTIf - constructor
	 * 
	 * @param ifx
	 */
	@SuppressWarnings("restriction")
	public PASTIf(IASTPreprocessorIfStatement ifx) {
		super((ASTNode) ifx);
		if_ = ifx;
	}

	/**
	 * @since 4.1
	 */
	public IASTNode copy(CopyStyle style) {
		return if_.copy(style);
	}

	@Override
	public String getType() {
		return "#if"; //$NON-NLS-1$
	}

	public boolean taken() {
		return if_.taken();
	}

	// cdt40
	public char[] getCondition() {
		return if_.getCondition();
	}

	public boolean isPartOfTranslationUnitFile() {
		return if_.isPartOfTranslationUnitFile();
	}

	@Override
	public IASTNode getOriginalNode() {
		return if_.getOriginalNode();
	}

}
