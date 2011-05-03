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
package org.eclipse.ptp.pldt.openmp.analysis.dictionary;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

/**
 * The DictionaryFactory visits an AST and build Symbols for all variables
 * 
 * @author pazel
 * 
 */
public class DictionaryFactory extends ASTVisitor
{
	protected IASTTranslationUnit ast_ = null;
	protected Dictionary fDict_ = null;

	/**
	 * buildDictionary - static method to build dictionary
	 * 
	 * @param ast
	 *            - IASTTranslationUnit
	 * @return Dictionary
	 */
	public static Dictionary buildDictionary(IASTTranslationUnit ast)
	{
		DictionaryFactory df = new DictionaryFactory(ast);
		df.buildDictionary();
		return df.getDictionary();
	}

	/**
	 * DictionaryFactory - constructor
	 * 
	 * @param ast
	 *            - IASTTranslationUnit
	 */
	protected DictionaryFactory(IASTTranslationUnit ast)
	{
		ast_ = ast;

		fDict_ = new Dictionary(ast_);
	}

	/**
	 * Dictionary - accessor to Dictionary
	 * 
	 * @return Dictionary
	 */
	public Dictionary getDictionary()
	{
		return fDict_;
	}

	/**
	 * buildDictionary - build the dictionary
	 * 
	 */
	protected void buildDictionary()
	{
		shouldVisitDeclarations = true;
		ast_.accept(this);
	}

	/**
	 * visit - ASTVisitor method capture to feed information into dicationary
	 * 
	 * @param declaration
	 *            - IASTDeclaration
	 * @return int
	 */
	public int visit(IASTDeclaration declaration)
	{
		// If this is a function definition, acct for the parameters
		if (declaration instanceof IASTFunctionDefinition) {
			IASTFunctionDeclarator ifd = ((IASTFunctionDefinition) declaration).getDeclarator();
			if (ifd instanceof IASTStandardFunctionDeclarator) {
				IASTStandardFunctionDeclarator ifdc = (IASTStandardFunctionDeclarator) ifd;
				IASTParameterDeclaration[] params = ifdc.getParameters();
				for (int i = 0; i < params.length; i++) {
					IASTDeclarator id = params[i].getDeclarator();
					fDict_.insert(id, params[i]);
				}
			}
			// CFG cfg = CFGFactory.buildCFG((IASTFunctionDefinition)declaration);
		}
		else if (declaration instanceof IASTSimpleDeclaration)
		{
			fDict_.insert((IASTSimpleDeclaration) declaration);
		}
		return PROCESS_CONTINUE;
	}
}
