/*******************************************************************************
 * 
 * Copyright (c) 2002, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial implementation base on code from rational/IBM
 ******************************************************************************/

package org.eclipse.fdt.internal.ui.compare;

import java.util.Iterator;

import org.eclipse.fdt.core.parser.CodeReader;
import org.eclipse.fdt.core.parser.DefaultProblemHandler;
import org.eclipse.fdt.core.parser.IProblem;
import org.eclipse.fdt.core.parser.ISourceElementRequestor;
import org.eclipse.fdt.core.parser.ParserMode;
import org.eclipse.fdt.core.parser.ParserUtil;
import org.eclipse.fdt.core.parser.ast.IASTASMDefinition;
import org.eclipse.fdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.fdt.core.parser.ast.IASTClassReference;
import org.eclipse.fdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.fdt.core.parser.ast.IASTCodeScope;
import org.eclipse.fdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.fdt.core.parser.ast.IASTDeclaration;
import org.eclipse.fdt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.fdt.core.parser.ast.IASTEnumerationReference;
import org.eclipse.fdt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.fdt.core.parser.ast.IASTEnumeratorReference;
import org.eclipse.fdt.core.parser.ast.IASTField;
import org.eclipse.fdt.core.parser.ast.IASTFieldReference;
import org.eclipse.fdt.core.parser.ast.IASTFunction;
import org.eclipse.fdt.core.parser.ast.IASTFunctionReference;
import org.eclipse.fdt.core.parser.ast.IASTInclusion;
import org.eclipse.fdt.core.parser.ast.IASTLinkageSpecification;
import org.eclipse.fdt.core.parser.ast.IASTMacro;
import org.eclipse.fdt.core.parser.ast.IASTMethod;
import org.eclipse.fdt.core.parser.ast.IASTMethodReference;
import org.eclipse.fdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.fdt.core.parser.ast.IASTNamespaceReference;
import org.eclipse.fdt.core.parser.ast.IASTParameterReference;
import org.eclipse.fdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.fdt.core.parser.ast.IASTTemplateInstantiation;
import org.eclipse.fdt.core.parser.ast.IASTTemplateParameterReference;
import org.eclipse.fdt.core.parser.ast.IASTTemplateSpecialization;
import org.eclipse.fdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.fdt.core.parser.ast.IASTTypedefReference;
import org.eclipse.fdt.core.parser.ast.IASTUsingDeclaration;
import org.eclipse.fdt.core.parser.ast.IASTUsingDirective;
import org.eclipse.fdt.core.parser.ast.IASTVariable;
import org.eclipse.fdt.core.parser.ast.IASTVariableReference;

/**
 *
 */
public class SourceElementRequestorAdapter implements ISourceElementRequestor {
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptAbstractTypeSpecDeclaration(org.eclipse.fdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration)
	 */
	public void acceptAbstractTypeSpecDeclaration(IASTAbstractTypeSpecifierDeclaration abstractDeclaration) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptASMDefinition(org.eclipse.fdt.core.parser.ast.IASTASMDefinition)
	 */
	public void acceptASMDefinition(IASTASMDefinition asmDefinition) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptClassReference(org.eclipse.fdt.core.parser.ast.IASTClassReference)
	 */
	public void acceptClassReference(IASTClassReference reference) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptElaboratedForewardDeclaration(org.eclipse.fdt.core.parser.ast.IASTElaboratedTypeSpecifier)
	 */
	public void acceptElaboratedForewardDeclaration(IASTElaboratedTypeSpecifier elaboratedType) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptEnumerationReference(org.eclipse.fdt.core.parser.ast.IASTEnumerationReference)
	 */
	public void acceptEnumerationReference(IASTEnumerationReference reference) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptEnumerationSpecifier(org.eclipse.fdt.core.parser.ast.IASTEnumerationSpecifier)
	 */
	public void acceptEnumerationSpecifier(IASTEnumerationSpecifier enumeration) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptField(org.eclipse.fdt.core.parser.ast.IASTField)
	 */
	public void acceptField(IASTField field) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptFieldReference(org.eclipse.fdt.core.parser.ast.IASTFieldReference)
	 */
	public void acceptFieldReference(IASTFieldReference reference) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptFunctionDeclaration(org.eclipse.fdt.core.parser.ast.IASTFunction)
	 */
	public void acceptFunctionDeclaration(IASTFunction function) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptFunctionReference(org.eclipse.fdt.core.parser.ast.IASTFunctionReference)
	 */
	public void acceptFunctionReference(IASTFunctionReference reference) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptMacro(org.eclipse.fdt.core.parser.ast.IASTMacro)
	 */
	public void acceptMacro(IASTMacro macro) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptMethodDeclaration(org.eclipse.fdt.core.parser.ast.IASTMethod)
	 */
	public void acceptMethodDeclaration(IASTMethod method) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptMethodReference(org.eclipse.fdt.core.parser.ast.IASTMethodReference)
	 */
	public void acceptMethodReference(IASTMethodReference reference) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptNamespaceReference(org.eclipse.fdt.core.parser.ast.IASTNamespaceReference)
	 */
	public void acceptNamespaceReference(IASTNamespaceReference reference) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptProblem(org.eclipse.fdt.core.parser.IProblem)
	 */
	public boolean acceptProblem(IProblem problem) {
		return DefaultProblemHandler.ruleOnProblem( problem, ParserMode.QUICK_PARSE );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptTypedefDeclaration(org.eclipse.fdt.core.parser.ast.IASTTypedefDeclaration)
	 */
	public void acceptTypedefDeclaration(IASTTypedefDeclaration typedef) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptTypedefReference(org.eclipse.fdt.core.parser.ast.IASTTypedefReference)
	 */
	public void acceptTypedefReference(IASTTypedefReference reference) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptUsingDeclaration(org.eclipse.fdt.core.parser.ast.IASTUsingDeclaration)
	 */
	public void acceptUsingDeclaration(IASTUsingDeclaration usageDeclaration) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptUsingDirective(org.eclipse.fdt.core.parser.ast.IASTUsingDirective)
	 */
	public void acceptUsingDirective(IASTUsingDirective usageDirective) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptVariable(org.eclipse.fdt.core.parser.ast.IASTVariable)
	 */
	public void acceptVariable(IASTVariable variable) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptVariableReference(org.eclipse.fdt.core.parser.ast.IASTVariableReference)
	 */
	public void acceptVariableReference(IASTVariableReference reference) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#enterClassSpecifier(org.eclipse.fdt.core.parser.ast.IASTClassSpecifier)
	 */
	public void enterClassSpecifier(IASTClassSpecifier classSpecification) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#enterCompilationUnit(org.eclipse.fdt.core.parser.ast.IASTCompilationUnit)
	 */
	public void enterCompilationUnit(IASTCompilationUnit compilationUnit) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#enterFunctionBody(org.eclipse.fdt.core.parser.ast.IASTFunction)
	 */
	public void enterFunctionBody(IASTFunction function) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#enterInclusion(org.eclipse.fdt.core.parser.ast.IASTInclusion)
	 */
	public void enterInclusion(IASTInclusion inclusion) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#enterLinkageSpecification(org.eclipse.fdt.core.parser.ast.IASTLinkageSpecification)
	 */
	public void enterLinkageSpecification(IASTLinkageSpecification linkageSpec) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#enterMethodBody(org.eclipse.fdt.core.parser.ast.IASTMethod)
	 */
	public void enterMethodBody(IASTMethod method) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#enterNamespaceDefinition(org.eclipse.fdt.core.parser.ast.IASTNamespaceDefinition)
	 */
	public void enterNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#enterTemplateDeclaration(org.eclipse.fdt.core.parser.ast.IASTTemplateDeclaration)
	 */
	public void enterTemplateDeclaration(IASTTemplateDeclaration declaration) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#enterTemplateInstantiation(org.eclipse.fdt.core.parser.ast.IASTTemplateInstantiation)
	 */
	public void enterTemplateInstantiation(IASTTemplateInstantiation instantiation) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#enterTemplateSpecialization(org.eclipse.fdt.core.parser.ast.IASTTemplateSpecialization)
	 */
	public void enterTemplateSpecialization(IASTTemplateSpecialization specialization) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#exitClassSpecifier(org.eclipse.fdt.core.parser.ast.IASTClassSpecifier)
	 */
	public void exitClassSpecifier(IASTClassSpecifier classSpecification) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#exitCompilationUnit(org.eclipse.fdt.core.parser.ast.IASTCompilationUnit)
	 */
	public void exitCompilationUnit(IASTCompilationUnit compilationUnit) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#exitFunctionBody(org.eclipse.fdt.core.parser.ast.IASTFunction)
	 */
	public void exitFunctionBody(IASTFunction function) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#exitInclusion(org.eclipse.fdt.core.parser.ast.IASTInclusion)
	 */
	public void exitInclusion(IASTInclusion inclusion) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#exitLinkageSpecification(org.eclipse.fdt.core.parser.ast.IASTLinkageSpecification)
	 */
	public void exitLinkageSpecification(IASTLinkageSpecification linkageSpec) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#exitMethodBody(org.eclipse.fdt.core.parser.ast.IASTMethod)
	 */
	public void exitMethodBody(IASTMethod method) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#exitNamespaceDefinition(org.eclipse.fdt.core.parser.ast.IASTNamespaceDefinition)
	 */
	public void exitNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#exitTemplateDeclaration(org.eclipse.fdt.core.parser.ast.IASTTemplateDeclaration)
	 */
	public void exitTemplateDeclaration(IASTTemplateDeclaration declaration) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#exitTemplateExplicitInstantiation(org.eclipse.fdt.core.parser.ast.IASTTemplateInstantiation)
	 */
	public void exitTemplateExplicitInstantiation(IASTTemplateInstantiation instantiation) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#exitTemplateSpecialization(org.eclipse.fdt.core.parser.ast.IASTTemplateSpecialization)
	 */
	public void exitTemplateSpecialization(IASTTemplateSpecialization specialization) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#enterCodeBlock(org.eclipse.fdt.core.parser.ast.IASTScope)
	 */
	public void enterCodeBlock(IASTCodeScope scope) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#exitCodeBlock(org.eclipse.fdt.core.parser.ast.IASTScope)
	 */
	public void exitCodeBlock(IASTCodeScope scope) {
		// TODO Auto-generated method stub
		
	}

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptEnumeratorReference(org.eclipse.fdt.core.parser.ast.IASTEnumerationReference)
     */
    public void acceptEnumeratorReference(IASTEnumeratorReference reference)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptParameterReference(org.eclipse.fdt.internal.core.parser.ast.complete.ASTParameterReference)
     */
    public void acceptParameterReference(IASTParameterReference reference)
    {
        // TODO Auto-generated method stub        
    }

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#createReader(java.lang.String)
	 */
	public CodeReader createReader(String finalPath, Iterator workingCopies) {
		return ParserUtil.createReader(finalPath, workingCopies	);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptTemplateParameterReference(org.eclipse.fdt.core.parser.ast.IASTTemplateParameterReference)
	 */
	public void acceptTemplateParameterReference(IASTTemplateParameterReference reference) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptFriendDeclaration(org.eclipse.fdt.core.parser.ast.IASTDeclaration)
	 */
	public void acceptFriendDeclaration(IASTDeclaration declaration) {
		// TODO Auto-generated method stub
		
	}
}
