/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.fdt.core.parser;


import java.util.Iterator;

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
import org.eclipse.fdt.internal.core.parser.InternalParserUtil;


public class NullSourceElementRequestor implements ISourceElementRequestor 
{
    private ParserMode mode = ParserMode.COMPLETE_PARSE;

	public NullSourceElementRequestor()
	{
	}

	public NullSourceElementRequestor( ParserMode mode )
	{
		this.mode = mode;
	}
	/* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptProblem(org.eclipse.fdt.core.parser.IProblem)
     */
    public boolean acceptProblem(IProblem problem)
    {
		return DefaultProblemHandler.ruleOnProblem( problem, mode );
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptMacro(org.eclipse.fdt.core.parser.ast.IASTMacro)
     */
    public void acceptMacro(IASTMacro macro)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptVariable(org.eclipse.fdt.core.parser.ast.IASTVariable)
     */
    public void acceptVariable(IASTVariable variable)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptFunctionDeclaration(org.eclipse.fdt.core.parser.ast.IASTFunction)
     */
    public void acceptFunctionDeclaration(IASTFunction function)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptUsingDirective(org.eclipse.fdt.core.parser.ast.IASTUsingDirective)
     */
    public void acceptUsingDirective(IASTUsingDirective usageDirective)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptUsingDeclaration(org.eclipse.fdt.core.parser.ast.IASTUsingDeclaration)
     */
    public void acceptUsingDeclaration(IASTUsingDeclaration usageDeclaration)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptASMDefinition(org.eclipse.fdt.core.parser.ast.IASTASMDefinition)
     */
    public void acceptASMDefinition(IASTASMDefinition asmDefinition)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptTypedef(org.eclipse.fdt.core.parser.ast.IASTTypedef)
     */
    public void acceptTypedefDeclaration(IASTTypedefDeclaration typedef)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptEnumerationSpecifier(org.eclipse.fdt.core.parser.ast.IASTEnumerationSpecifier)
     */
    public void acceptEnumerationSpecifier(IASTEnumerationSpecifier enumeration)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#enterFunctionBody(org.eclipse.fdt.core.parser.ast.IASTFunction)
     */
    public void enterFunctionBody(IASTFunction function)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#exitFunctionBody(org.eclipse.fdt.core.parser.ast.IASTFunction)
     */
    public void exitFunctionBody(IASTFunction function)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#enterCompilationUnit(org.eclipse.fdt.core.parser.ast.IASTCompilationUnit)
     */
    public void enterCompilationUnit(IASTCompilationUnit compilationUnit)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#enterInclusion(org.eclipse.fdt.core.parser.ast.IASTInclusion)
     */
    public void enterInclusion(IASTInclusion inclusion)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#enterNamespaceDefinition(org.eclipse.fdt.core.parser.ast.IASTNamespaceDefinition)
     */
    public void enterNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#enterClassSpecifier(org.eclipse.fdt.core.parser.ast.IASTClassSpecifier)
     */
    public void enterClassSpecifier(IASTClassSpecifier classSpecification)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#enterLinkageSpecification(org.eclipse.fdt.core.parser.ast.IASTLinkageSpecification)
     */
    public void enterLinkageSpecification(IASTLinkageSpecification linkageSpec)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#enterTemplateDeclaration(org.eclipse.fdt.core.parser.ast.IASTTemplateDeclaration)
     */
    public void enterTemplateDeclaration(IASTTemplateDeclaration declaration)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#enterTemplateSpecialization(org.eclipse.fdt.core.parser.ast.IASTTemplateSpecialization)
     */
    public void enterTemplateSpecialization(IASTTemplateSpecialization specialization)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#enterTemplateExplicitInstantiation(org.eclipse.fdt.core.parser.ast.IASTTemplateInstantiation)
     */
    public void enterTemplateInstantiation(IASTTemplateInstantiation instantiation)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptMethodDeclaration(org.eclipse.fdt.core.parser.ast.IASTMethod)
     */
    public void acceptMethodDeclaration(IASTMethod method)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#enterMethodBody(org.eclipse.fdt.core.parser.ast.IASTMethod)
     */
    public void enterMethodBody(IASTMethod method)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#exitMethodBody(org.eclipse.fdt.core.parser.ast.IASTMethod)
     */
    public void exitMethodBody(IASTMethod method)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptField(org.eclipse.fdt.core.parser.ast.IASTField)
     */
    public void acceptField(IASTField field)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptClassReference(org.eclipse.fdt.core.parser.ast.IASTClassReference)
     */
    public void acceptClassReference(IASTClassReference reference)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#exitTemplateDeclaration(org.eclipse.fdt.core.parser.ast.IASTTemplateDeclaration)
     */
    public void exitTemplateDeclaration(IASTTemplateDeclaration declaration)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#exitTemplateSpecialization(org.eclipse.fdt.core.parser.ast.IASTTemplateSpecialization)
     */
    public void exitTemplateSpecialization(IASTTemplateSpecialization specialization)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#exitTemplateExplicitInstantiation(org.eclipse.fdt.core.parser.ast.IASTTemplateInstantiation)
     */
    public void exitTemplateExplicitInstantiation(IASTTemplateInstantiation instantiation)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#exitLinkageSpecification(org.eclipse.fdt.core.parser.ast.IASTLinkageSpecification)
     */
    public void exitLinkageSpecification(IASTLinkageSpecification linkageSpec)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#exitClassSpecifier(org.eclipse.fdt.core.parser.ast.IASTClassSpecifier)
     */
    public void exitClassSpecifier(IASTClassSpecifier classSpecification)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#exitNamespaceDefinition(org.eclipse.fdt.core.parser.ast.IASTNamespaceDefinition)
     */
    public void exitNamespaceDefinition(IASTNamespaceDefinition namespaceDefinition)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#exitInclusion(org.eclipse.fdt.core.parser.ast.IASTInclusion)
     */
    public void exitInclusion(IASTInclusion inclusion)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#exitCompilationUnit(org.eclipse.fdt.core.parser.ast.IASTCompilationUnit)
     */
    public void exitCompilationUnit(IASTCompilationUnit compilationUnit)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptAbstractTypeSpecDeclaration(org.eclipse.fdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration)
     */
    public void acceptAbstractTypeSpecDeclaration(IASTAbstractTypeSpecifierDeclaration abstractDeclaration)
    {
        // TODO Auto-generated method stub
        
    }




    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptTypedefReference(org.eclipse.fdt.core.parser.ast.IASTTypedefReference)
     */
    public void acceptTypedefReference(IASTTypedefReference reference)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptNamespaceReference(org.eclipse.fdt.core.parser.ast.IASTNamespaceReference)
     */
    public void acceptNamespaceReference(IASTNamespaceReference reference)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptEnumerationReference(org.eclipse.fdt.core.parser.ast.IASTEnumerationReference)
     */
    public void acceptEnumerationReference(IASTEnumerationReference reference)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptVariableReference(org.eclipse.fdt.core.parser.ast.IASTVariableReference)
     */
    public void acceptVariableReference(IASTVariableReference reference)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptFunctionReference(org.eclipse.fdt.core.parser.ast.IASTFunctionReference)
     */
    public void acceptFunctionReference(IASTFunctionReference reference)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptFieldReference(org.eclipse.fdt.core.parser.ast.IASTFieldReference)
     */
    public void acceptFieldReference(IASTFieldReference reference)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptMethodReference(org.eclipse.fdt.core.parser.ast.IASTMethodReference)
     */
    public void acceptMethodReference(IASTMethodReference reference)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptElaboratedForewardDeclaration(org.eclipse.fdt.core.parser.ast.IASTElaboratedTypeSpecifier)
     */
    public void acceptElaboratedForewardDeclaration(IASTElaboratedTypeSpecifier elaboratedType)
    {
        // TODO Auto-generated method stub
        
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
		return InternalParserUtil.createFileReader( finalPath );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptTemplateParameterReference(org.eclipse.fdt.core.parser.ast.IASTTemplateParameterReference)
	 */
	public void acceptTemplateParameterReference(IASTTemplateParameterReference reference) 
	{
		// TODO Auto-generated method stub
		
	} 
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ISourceElementRequestor#acceptFriendDeclaration(org.eclipse.fdt.core.parser.ast.IASTDeclaration)
	 */
	public void acceptFriendDeclaration(IASTDeclaration declaration) {
		// TODO Auto-generated method stub
		
	}
}
