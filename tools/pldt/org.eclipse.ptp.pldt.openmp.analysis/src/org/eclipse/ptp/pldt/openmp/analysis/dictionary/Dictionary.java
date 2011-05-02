/**********************************************************************
 * Copyright (c) 2006,2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.analysis.dictionary;

import java.util.Hashtable;
import java.util.LinkedList;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;

/**
 * Dictionary for entire file - global scope and function scopes <br>
 * The dictionary is mainly used to query for symbols within certain scopes
 * and each symbol tells where it is defined, see getSymbolsFor().<br>
 * Would be good to develop further with "where used" information
 * 
 * @author pazel
 */
public class Dictionary
{
	// maps string to linked list of symbols having the same name
	protected Hashtable textToSymbols_ = new Hashtable();
	// maps function (or ast for globals) to linked list of all symbols defined in that function
	protected Hashtable functionToSymbols_ = new Hashtable();

	protected Hashtable stringScopeKeyToSymbol_ = new Hashtable();

	protected IASTTranslationUnit ast_ = null;

	/**
	 * Dictionary - File dictionary
	 * 
	 * @param ast
	 *            - IASTTranslationUnit
	 */
	public Dictionary(IASTTranslationUnit ast)
	{
		ast_ = ast;

		functionToSymbols_.put(ast_, new LinkedList());
	}

	/**
	 * insert - insert a set of symbol specifications using a simple declartion
	 * 
	 * @param decl
	 *            - IASTSimpleDeclaration
	 */
	public void insert(IASTSimpleDeclaration decl)
	{
		IASTDeclarator[] decls = decl.getDeclarators();
		for (int i = 0; i < decls.length; i++)
			insert(decls[i], decl);
	}

	/**
	 * insert - insert a declarator into the dictionaries
	 * 
	 * @param declarator
	 *            - IASTDeclarator
	 * @param decl
	 *            - IASTDeclaration
	 */
	public void insert(IASTDeclarator declarator, IASTDeclaration decl) {
		Symbol symbol = new Symbol(declarator, decl);
		insertMember(symbol);
	}

	/**
	 * insert - insert a declarator into the dictionaries
	 * 
	 * @param declarator
	 *            - IASTDeclarator
	 * @param decl
	 *            - IASTDeclaration
	 */
	public void insert(IASTDeclarator declarator, IASTParameterDeclaration decl) {
		Symbol symbol = new Symbol(declarator, decl);
		insertMember(symbol);
	}

	protected void insertMember(Symbol symbol)
	{
		IASTNode node = symbol.getDefiningFunction();

		// add to string/scope
		try {
			StringScopeKey sskey = new StringScopeKey(symbol.getName());
			stringScopeKeyToSymbol_.put(sskey, symbol);
		} catch (DOMException e) {
			System.out.println("Dictionary.insert exception " + e);
		}

		// Add to function global lists
		if (node == null)
			node = ast_; // ?? maybe should exit instead??
		LinkedList ll = (LinkedList) functionToSymbols_.get(node);
		if (ll == null) {
			ll = new LinkedList();
			functionToSymbols_.put(node, ll);
		}
		ll.add(symbol);

		// Add to text list
		String strname = symbol.getName().toString();
		LinkedList nl = (LinkedList) textToSymbols_.get(strname);
		if (nl == null) {
			nl = new LinkedList(); // definitions for this text name
			textToSymbols_.put(strname, nl);
		}
		nl.add(symbol);
	}

	/**
	 * getSymbolsFor - for a name string, using the global dicationary
	 * 
	 * @param text
	 *            - String
	 * @return Symbol []
	 */
	public Symbol[] getSymbolsFor(String text)
	{
		LinkedList ll = (LinkedList) textToSymbols_.get(text);
		if (ll == null)
			return new Symbol[0];
		Symbol[] ans = new Symbol[ll.size()];
		for (int i = 0; i < ll.size(); i++)
			ans[i] = (Symbol) ll.get(i);
		return ans;
	}

	/**
	 * getSymbolsFor - using a given function scope
	 * 
	 * @param fctnDef
	 *            - IASTFunctionDefinition
	 * @return Symbol []
	 */
	public Symbol[] getSymbolsFor(IASTFunctionDefinition fctnDef)
	{
		LinkedList ll = (LinkedList) functionToSymbols_.get(fctnDef);
		if (ll == null)
			return new Symbol[0];
		Symbol[] ans = new Symbol[ll.size()];
		for (int i = 0; i < ll.size(); i++)
			ans[i] = (Symbol) ll.get(i);
		return ans;
	}

	/**
	 * getSymbolsFor - using the global scope
	 * 
	 * @return Symbol []
	 */
	public Symbol[] getSymbolsFor()
	{
		LinkedList ll = (LinkedList) functionToSymbols_.get(ast_);
		if (ll == null)
			return new Symbol[0];
		Symbol[] ans = new Symbol[ll.size()];
		for (int i = 0; i < ll.size(); i++)
			ans[i] = (Symbol) ll.get(i);
		return ans;
	}

	/**
	 * find - given an IASTName (references, for example)
	 * 
	 * @param name
	 *            - IASTName
	 * @return Symbol
	 * @throws DOMException
	 */
	public Symbol find(IASTName name) throws DOMException
	{
		StringScopeKey key = new StringScopeKey(name);

		return (Symbol) stringScopeKeyToSymbol_.get(key);
	}

	// -------------------------------------------------------------------------
	// Class: StringScopeKey - binding identity of name and scope where defined
	// -------------------------------------------------------------------------
	protected static class StringScopeKey
	{
		public String stringKey_ = "";
		public IScope scopeKey_ = null;

		public StringScopeKey(String stringKey, IScope scopeKey)
		{
			stringKey_ = stringKey;
			scopeKey_ = scopeKey;
		}

		public StringScopeKey(IASTName nameNode) throws DOMException
		{
			stringKey_ = nameNode.toString();
			// IBinding b = nameNode.resolveBinding();
			// if (b instanceof IVariable) {
			// IVariable biv = (IVariable)b;
			// IType t = biv.getType();
			// t=null;
			// }
			IBinding binding = nameNode.resolveBinding();// cdt40 fix: was getBinding(); could have been null
			scopeKey_ = binding.getScope();
		}

		/**
		 * equals
		 * NOTE - This is what makes the stringScopeKeyToSymbol_ hashtable work.
		 * allowing two StringScopeKeys with identical data fields to be "identified"
		 * by the hashtable.
		 */
		public boolean equals(Object that)
		{
			if (this == that)
				return true;
			if (!(that instanceof StringScopeKey))
				return false;
			StringScopeKey thatStringScopeKey = (StringScopeKey) that;
			if (thatStringScopeKey.stringKey_.equals(stringKey_) && thatStringScopeKey.scopeKey_ == scopeKey_)
				return true;
			return false;
		}

		/**
		 * equals
		 * NOTE - This is what makes the stringScopeKeyToSymbol_ hashtable work
		 */
		public int hashCode() {
			return stringKey_.hashCode();
		}

	}
}
