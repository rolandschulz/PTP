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

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElseStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorEndifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorErrorStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfdefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfndefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorObjectStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorPragmaStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorUndefStatement;

/**
 * Create our new PAST types from old AST types
 * 
 * @author pazel
 */
public class PASTFactory
{
	/**
	 * PASTFactoryMake - Conversion from AST to PAST
	 * 
	 * @param node
	 *            : IASTPreprocessorStatement
	 * @return PASTNode
	 */
	public static PASTNode PASTFactoryMake(IASTPreprocessorStatement node)
	{
		if (node instanceof IASTPreprocessorEndifStatement)
			return new PASTEndif((IASTPreprocessorEndifStatement) node);
		else if (node instanceof IASTPreprocessorElifStatement)
			return new PASTElif((IASTPreprocessorElifStatement) node);
		else if (node instanceof IASTPreprocessorElseStatement)
			return new PASTElse((IASTPreprocessorElseStatement) node);
		else if (node instanceof IASTPreprocessorIfndefStatement)
			return new PASTIfndef((IASTPreprocessorIfndefStatement) node);
		else if (node instanceof IASTPreprocessorIfdefStatement)
			return new PASTIfdef((IASTPreprocessorIfdefStatement) node);
		else if (node instanceof IASTPreprocessorIfStatement)
			return new PASTIf((IASTPreprocessorIfStatement) node);
		else if (node instanceof IASTPreprocessorErrorStatement)
			return new PASTError((IASTPreprocessorErrorStatement) node);
		else if (node instanceof IASTPreprocessorPragmaStatement)
			return new PASTPragma((IASTPreprocessorPragmaStatement) node);
		else if (node instanceof IASTPreprocessorUndefStatement)
			return new PASTUndef((IASTPreprocessorUndefStatement) node);
		else if (node instanceof IASTPreprocessorIncludeStatement)
			return new PASTInclusionStatement((IASTPreprocessorIncludeStatement) node);
		else if (node instanceof IASTPreprocessorFunctionStyleMacroDefinition)
			return new PASTFunctionMacro((IASTPreprocessorFunctionStyleMacroDefinition) node);
		else if (node instanceof IASTPreprocessorObjectStyleMacroDefinition)
			return new PASTObjectMacro((IASTPreprocessorObjectStyleMacroDefinition) node);
		else {
			return null;// what is node? LocationMap$ASTWarning.
		}
	}
}
