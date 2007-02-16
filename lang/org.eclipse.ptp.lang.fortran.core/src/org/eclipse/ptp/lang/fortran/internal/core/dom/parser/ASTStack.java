/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.lang.fortran.internal.core.dom.parser;

import java.util.Iterator;
import java.util.Stack;


/**
 * Stack used to store AST nodes while the AST is built during the parse.
 * 
 * Some grammar rules have arbitrary length lists on the right side.
 * For example the rule for compound statements (where block_item_list is any 
 * number of statements or declarations):
 * 
 * compound-statement ::= <openscope> '{' block_item_list '}'
 * 
 * There is a problem when trying to build the AST node for the compound statment...
 * you don't know how many block_items are contained in the compound statment, so 
 * you don't know how many times to pop the AST stack.
 * 
 * One inelegant solution is to count the block-items as they are parsed. This
 * is inelegant because nested compound-statments are allowed so you would
 * have to maintain several counts at the same time.
 * 
 * This class represents a ASTStack that is implemented as a stack of "AST Scopes".
 * There is a special grammar rule <openscope> that creates a new AST Scope. 
 * So in order to consume all the block_items all that has to be done is 
 * iterate over the topmost scope and then close it when done.
 * 
 * 
 * 
 * @author Mike Kucera
 */
public class ASTStack {
	
	// Stores the AST nodes as the AST is being built
	// TODO private Stack<FortranASTNode> topScope;
	private Stack topScope;
	
	// A stack of stacks, used to implement scoping of the astStack
	// TODO private Stack<Stack<FortranASTNode>> astScopeStack;
	private Stack<Stack> astScopeStack;
	
	public ASTStack() {
		// TODO topScope = new Stack<FortranASTNode>();
		topScope = new Stack();
		// TODO astScopeStack = new Stack<Stack<FortranASTNode>>(); // ititially empty
		astScopeStack = new Stack<Stack>(); // ititially empty
	}
	
	
	/**
	 * Saves the current scope and pushes a new one.
	 * Usually called by the parser actions.
	 */
	public void openASTScope() {
		astScopeStack.push(topScope); 
		// TODO topScope = new Stack<FortranASTNode>();
		topScope = new Stack();
	}
	
	
	/**
	 * Usually called by a reduction rule in this class.
	 */
	public void closeASTScope() {
		topScope = astScopeStack.pop();
	}
	
	public void push(Object o) {
		topScope.push(o);
	}
	
	public Object pop() {
		return topScope.pop();
	}
	
	public Object peek() {
		return topScope.peek();
	}
	
	
	/**
	 * Returns all the elements in the topmost scope as an array.
	 */
	public Object[] topScopeArray() {
		return topScope.toArray();
	}
	
	
	/**
	 * Returns an iterator that will iterate over the topmost scope
	 * starting at the bottom.
	 */
	public Iterator topScopeIterator() { 
		return topScope.iterator();
	}

	public boolean isEmpty() {
		return topScope.isEmpty() && astScopeStack.isEmpty();
	}
	
	
}
