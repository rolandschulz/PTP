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
import java.util.LinkedList;

import org.eclipse.fdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.fdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.fdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.fdt.core.parser.ast.IASTDeclaration;
import org.eclipse.fdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.fdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.fdt.core.parser.ast.IASTField;
import org.eclipse.fdt.core.parser.ast.IASTFunction;
import org.eclipse.fdt.core.parser.ast.IASTInclusion;
import org.eclipse.fdt.core.parser.ast.IASTLinkageSpecification;
import org.eclipse.fdt.core.parser.ast.IASTMacro;
import org.eclipse.fdt.core.parser.ast.IASTMethod;
import org.eclipse.fdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.fdt.core.parser.ast.IASTNode;
import org.eclipse.fdt.core.parser.ast.IASTScope;
import org.eclipse.fdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.fdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.fdt.core.parser.ast.IASTUsingDirective;
import org.eclipse.fdt.core.parser.ast.IASTVariable;
import org.eclipse.fdt.internal.core.parser.QuickParseCallback;
import org.eclipse.fdt.internal.core.parser.ast.complete.ASTLinkageSpecification;
import org.eclipse.fdt.internal.core.parser.ast.complete.ASTScope;

/**
 * @author hamer
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class StructuralParseCallback extends QuickParseCallback{

	protected LinkedList scopeStack = new LinkedList();
	protected IASTScope currentScope = null;
	protected int inclusionLevel = 0;

	
	private void addElement (IASTDeclaration element){
		if(inclusionLevel == 0){
			if( currentScope instanceof ASTScope )
				((ASTScope)currentScope).addDeclaration(element);
			else if( currentScope instanceof ASTLinkageSpecification )
				((ASTLinkageSpecification)currentScope).addDeclaration( element );
		}
	}
	
	private void enterScope(IASTNode node){
		if(node instanceof IASTScope){
			if(node instanceof ASTScope){
				((ASTScope)node).initDeclarations();
			}
			pushScope((IASTScope)node);
		}
	}

	private void exitScope(IASTNode node){
		if(node instanceof IASTScope){
			popScope();
		}
	}
	
	private void pushScope( IASTScope scope ){
		scopeStack.addFirst( currentScope );
		currentScope = scope;
	}
	
	private IASTScope popScope(){
		IASTScope oldScope = currentScope;
		currentScope = (scopeStack.size() > 0 ) ? (IASTScope) scopeStack.removeFirst() : null;
		return oldScope;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptMacro(org.eclipse.fdt.core.parser.ast.IASTMacro)
	 */
	public void acceptMacro(IASTMacro macro) {
		if(inclusionLevel == 0)
			macros.add(macro);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptVariable(org.eclipse.fdt.core.parser.ast.IASTVariable)
	 */
	public void acceptVariable(IASTVariable variable) {
		addElement(variable);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptFunctionDeclaration(org.eclipse.fdt.core.parser.ast.IASTFunction)
	 */
	public void acceptFunctionDeclaration(IASTFunction function) {
		if(function.getOwnerTemplateDeclaration() == null)
			addElement(function);
		else if(function.getOwnerTemplateDeclaration() instanceof IASTTemplateDeclaration)
			addElement((IASTTemplateDeclaration)function.getOwnerTemplateDeclaration());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptTypedefDeclaration(org.eclipse.fdt.core.parser.ast.IASTTypedefDeclaration)
	 */
	public void acceptTypedefDeclaration(IASTTypedefDeclaration typedef) {
		addElement(typedef);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptUsingDirective(org.eclipse.fdt.core.parser.ast.IASTUsingDirective)
	 */
	public void acceptUsingDirective(IASTUsingDirective usageDirective) {
		addElement(usageDirective);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptEnumerationSpecifier(org.eclipse.fdt.core.parser.ast.IASTEnumerationSpecifier)
	 */
	public void acceptEnumerationSpecifier(IASTEnumerationSpecifier enumeration) {
		enterScope(enumeration);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptElaboratedForewardDeclaration(org.eclipse.fdt.core.parser.ast.IASTElaboratedTypeSpecifier)
	 */
	public void acceptElaboratedForewardDeclaration(IASTElaboratedTypeSpecifier elaboratedType) {
		enterScope(elaboratedType);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptAbstractTypeSpecDeclaration(org.eclipse.fdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration)
	 */
	public void acceptAbstractTypeSpecDeclaration(IASTAbstractTypeSpecifierDeclaration abstractDeclaration) {
		if(abstractDeclaration.getOwnerTemplateDeclaration() == null)
			addElement(abstractDeclaration);
		else if(abstractDeclaration.getOwnerTemplateDeclaration() instanceof IASTTemplateDeclaration)
			addElement((IASTTemplateDeclaration)abstractDeclaration.getOwnerTemplateDeclaration());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#enterInclusion(org.eclipse.fdt.core.parser.ast.IASTInclusion)
	 */
	public void enterInclusion(IASTInclusion inclusion) {
		if(inclusionLevel == 0)
			inclusions.add(inclusion);
		inclusionLevel++;
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#enterNamespaceDefinition(org.eclipse.fdt.core.parser.ast.IASTNamespaceDefinition)
	 */
	public void enterNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition) {
		addElement(namespaceDefinition);
		enterScope(namespaceDefinition);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#enterClassSpecifier(org.eclipse.fdt.core.parser.ast.IASTClassSpecifier)
	 */
	public void enterClassSpecifier(IASTClassSpecifier classSpecification) {
		enterScope(classSpecification);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#enterLinkageSpecification(org.eclipse.fdt.core.parser.ast.IASTLinkageSpecification)
	 */
	public void enterLinkageSpecification(IASTLinkageSpecification linkageSpec) {
		addElement(linkageSpec);
		enterScope(linkageSpec);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#enterCompilationUnit(org.eclipse.fdt.core.parser.ast.IASTCompilationUnit)
	 */
	public void enterCompilationUnit(IASTCompilationUnit compilationUnit) {
	enterScope(compilationUnit);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptMethodDeclaration(org.eclipse.fdt.core.parser.ast.IASTMethod)
	 */
	public void acceptMethodDeclaration(IASTMethod method) {
		if(method.getOwnerTemplateDeclaration() == null)
			addElement(method);
		else if(method.getOwnerTemplateDeclaration() instanceof IASTTemplateDeclaration)
			addElement((IASTTemplateDeclaration)method.getOwnerTemplateDeclaration());
	}	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptField(org.eclipse.fdt.core.parser.ast.IASTField)
	 */
	public void acceptField(IASTField field) {
		addElement(field);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#exitCompilationUnit(org.eclipse.fdt.core.parser.ast.IASTCompilationUnit)
	 */
	public void exitCompilationUnit(IASTCompilationUnit compilationUnit) {
		exitScope(compilationUnit);
		this.compUnit = compilationUnit;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#exitInclusion(org.eclipse.fdt.core.parser.ast.IASTInclusion)
	 */
	public void exitInclusion( IASTInclusion inclusion )
	{
		inclusionLevel--;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.IQuickParseCallback#getCompilationUnit()
	 */
	public IASTCompilationUnit getCompilationUnit() {
		return compUnit;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#exitClassSpecifier(org.eclipse.fdt.core.parser.ast.IASTClassSpecifier)
	 */
	public void exitClassSpecifier(IASTClassSpecifier classSpecification) {
		exitScope(classSpecification);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#exitLinkageSpecification(org.eclipse.fdt.core.parser.ast.IASTLinkageSpecification)
	 */
	public void exitLinkageSpecification(IASTLinkageSpecification linkageSpec) {
		exitScope(linkageSpec);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#exitNamespaceDefinition(org.eclipse.fdt.core.parser.ast.IASTNamespaceDefinition)
	 */
	public void exitNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition) {
		exitScope(namespaceDefinition);
	}

		
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#enterFunctionBody(org.eclipse.fdt.core.parser.ast.IASTFunction)
	 */
	public void enterFunctionBody(IASTFunction function) {
		if(function.getOwnerTemplateDeclaration() == null)
			addElement(function);
		else if(function.getOwnerTemplateDeclaration() instanceof IASTTemplateDeclaration)
			addElement((IASTTemplateDeclaration)function.getOwnerTemplateDeclaration());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#enterMethodBody(org.eclipse.fdt.core.parser.ast.IASTMethod)
	 */
	public void enterMethodBody(IASTMethod method) {
		if(method.getOwnerTemplateDeclaration() == null)
			addElement(method);
		else if(method.getOwnerTemplateDeclaration() instanceof IASTTemplateDeclaration)
			addElement((IASTTemplateDeclaration)method.getOwnerTemplateDeclaration());
	}

}
