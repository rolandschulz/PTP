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
package org.eclipse.ptp.pldt.openmp.analysis.dictionary;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.internal.core.dom.parser.c.CFunction;
import org.eclipse.cdt.internal.core.dom.parser.c.CParameter;
import org.eclipse.cdt.internal.core.dom.parser.c.CStructure;
import org.eclipse.cdt.internal.core.dom.parser.c.CTypedef;
import org.eclipse.cdt.internal.core.dom.parser.c.CVariable;

/**
 * Holds a symbol definition
 * 
 * @author pazel
 * 
 */

public class Symbol {
	/** what is being defined */
	protected IASTDeclarator declarator_ = null;
	/**
	 * declaration can be either IASTDeclaration or IASTParameterDeclaration;
	 * this is the broader containing statement
	 */
	protected IASTNode declaration_ = null;
	private static final boolean traceOn = false;

	/**
	 * constructor
	 * 
	 * @param declarator -
	 *            IASTDeclarator
	 * @param declaration -
	 *            IASTDeclartion (for this declarator - and possibly others
	 */
	public Symbol(IASTDeclarator declarator, IASTDeclaration declaration) {
		declarator_ = declarator;
		declaration_ = declaration;
	}

	/**
	 * constructor
	 * 
	 * @param declarator -
	 *            IASTDeclarator
	 * @param declaration -
	 *            IASTParameterDeclartion (for this declarator - and possibly
	 *            others
	 */
	public Symbol(IASTDeclarator declarator,
			IASTParameterDeclaration declaration) {
		declarator_ = declarator;
		declaration_ = declaration;
	}

	/**
	 * getName - get the name of the declarator
	 * 
	 * @return IASTName
	 */
	public IASTName getName() {
		return declarator_.getName();
	}

	/**
	 * getDeclarator - accessor to declarator
	 * 
	 * @return - IASTDeclarator
	 */
	public IASTDeclarator getDeclarator() {
		return declarator_;
	}

	/**
	 * getDeclaration - get the related declaration
	 * 
	 * @return - IASTNode (can be IASTDeclaration or IASTParameterDeclaration)
	 */
	public IASTNode getDeclaration() {
		return declaration_;
	}

	/**
	 * getScope - get the scope for this declarator
	 * 
	 * @return IScope
	 */
	public IScope getScope() {
		// find the unique scope for this name
		IASTName name = declarator_.getName();
		IBinding binding = name.resolveBinding();
		//IBinding binding = name.getBinding();
		if (binding == null)
			return null;

		IScope scope = null;
		try {
			scope = binding.getScope();
		} catch (DOMException e) {
			System.out.println("SymbolBucket.find: exception " + e);
			return null;
		}

		return scope;
	}

	/**
	 * getDefiningFunction - get the function in which declarator is defined
	 * 
	 * @return IASTNode - either IASTTranslationUnit or IASTFunctionDefinition
	 */

	public IASTNode getDefiningFunction() {
		IASTNode node = getPhysicalNode();// should it be scope's node?

		// keep moving up the tree until we find the node
		while (true) {
			if (node == null)
				return null;
			if (node instanceof IASTTranslationUnit)
				return node; // global dict
			if (node instanceof IASTFunctionDefinition)
				return node; // our function
			node = node.getParent();
		}
	}

	/**
	 * get the physical node (IASTNode) that this symbol is contained in. <br>
	 * (Formerly supplied by IScope.getPhysicalNode() )
	 * Note: this isn't used, and doesn't work. Left here for possible future fixup.
	 * PASTOMPFactory.isSymbolRelevant computes this differently now, using some
	 * classes/packages with discouraged access, so it seems eventually it would be nice
	 * to fix this to be "proper."
	 * 
	 * @return
	 */
	// @SuppressWarnings("restriction")
	public IASTNode getPhysicalNode() {
		IASTNode node = null;
		IASTName name = declarator_.getName();
		IASTNode parentNode=name.getParent();
		boolean foo=true;
		if(foo)
			return parentNode;
		
		// Also retrievable from IScope (which was original question)
		IScope scope = getScope();
		IName sn;
		try {
			sn = scope.getScopeName();
			if(sn instanceof IASTName){
				name=(IASTName)sn; // this *should* be more accurate
			}
		} catch (DOMException e) {return null;}
		
		IBinding binding = name.resolveBinding();

		// try to avoid discouraged access
//		if(binding instanceof IFunction){
//			IFunction func = (IFunction)binding;
//			IASTTranslationUnit ast = declarator_.getTranslationUnit();
//			IASTName[] defs=ast.getDefinitionsInAST(binding);
//			IASTName def=defs[0];
//			
//		}
		if (binding instanceof CFunction) {
			CFunction cf = (CFunction) binding;
			if (traceOn)
				System.out.println("Symbol .. CFunction: " + cf.getName());


			node = cf.getPhysicalNode();
		} else if (binding instanceof CParameter) {
			CParameter cp = (CParameter) binding;
			if (traceOn)
				System.out.println("Symbol .. CParameter: " + cp.getName());

		} else if (binding instanceof CVariable) {
			CVariable cv = (CVariable) binding;
			if (traceOn)
				System.out.println("Symbol .. CVariable: " + cv.getName());
			node = cv.getPhysicalNode();
		} else if (binding instanceof CTypedef) {
			CTypedef ct = (CTypedef) binding;
			if (traceOn)
				System.out.println("Symbol .. CTypedef: " + ct.getName());
			node = ct.getPhysicalNode();
		} else if (binding instanceof CStructure) {
			CStructure cs = (CStructure) binding;
			if (traceOn)
				System.out.println("Symbol .. CStructure: " + cs.getName());
			node = cs.getPhysicalNode();
		}else {// any other types I can get a physical node from???
			System.out
					.println("**\n**Symbol.getPhysicalNode(): Binding type unknown. is: "
							+ binding);// ProblemBinding
		}
		//System.out.println("nodeSig: "+node.getRawSignature()+" name: "+name);
		return node;
	}
}
