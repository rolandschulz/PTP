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
package org.eclipse.cldt.core.parser;

import java.util.Iterator;

import org.eclipse.cldt.core.parser.ast.IASTASMDefinition;
import org.eclipse.cldt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.cldt.core.parser.ast.IASTClassReference;
import org.eclipse.cldt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cldt.core.parser.ast.IASTCodeScope;
import org.eclipse.cldt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cldt.core.parser.ast.IASTDeclaration;
import org.eclipse.cldt.core.parser.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cldt.core.parser.ast.IASTEnumerationReference;
import org.eclipse.cldt.core.parser.ast.IASTEnumerationSpecifier;
import org.eclipse.cldt.core.parser.ast.IASTEnumeratorReference;
import org.eclipse.cldt.core.parser.ast.IASTField;
import org.eclipse.cldt.core.parser.ast.IASTFieldReference;
import org.eclipse.cldt.core.parser.ast.IASTFunction;
import org.eclipse.cldt.core.parser.ast.IASTFunctionReference;
import org.eclipse.cldt.core.parser.ast.IASTInclusion;
import org.eclipse.cldt.core.parser.ast.IASTLinkageSpecification;
import org.eclipse.cldt.core.parser.ast.IASTMacro;
import org.eclipse.cldt.core.parser.ast.IASTMethod;
import org.eclipse.cldt.core.parser.ast.IASTMethodReference;
import org.eclipse.cldt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cldt.core.parser.ast.IASTNamespaceReference;
import org.eclipse.cldt.core.parser.ast.IASTParameterReference;
import org.eclipse.cldt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cldt.core.parser.ast.IASTTemplateInstantiation;
import org.eclipse.cldt.core.parser.ast.IASTTemplateParameterReference;
import org.eclipse.cldt.core.parser.ast.IASTTemplateSpecialization;
import org.eclipse.cldt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cldt.core.parser.ast.IASTTypedefReference;
import org.eclipse.cldt.core.parser.ast.IASTUsingDeclaration;
import org.eclipse.cldt.core.parser.ast.IASTUsingDirective;
import org.eclipse.cldt.core.parser.ast.IASTVariable;
import org.eclipse.cldt.core.parser.ast.IASTVariableReference;

/**
 * @author jcamelon
 *
 */
public interface ISourceElementRequestor {
	
	public boolean acceptProblem( IProblem problem );

	public void acceptMacro( IASTMacro macro );
	public void acceptVariable( IASTVariable variable );
	public void acceptFunctionDeclaration( IASTFunction function );
	public void acceptUsingDirective( IASTUsingDirective usageDirective );
	public void acceptUsingDeclaration( IASTUsingDeclaration usageDeclaration );
	public void acceptASMDefinition( IASTASMDefinition asmDefinition );
	public void acceptTypedefDeclaration( IASTTypedefDeclaration typedef );
	public void acceptEnumerationSpecifier( IASTEnumerationSpecifier enumeration );
	public void acceptElaboratedForewardDeclaration( IASTElaboratedTypeSpecifier elaboratedType );
	public void acceptAbstractTypeSpecDeclaration( IASTAbstractTypeSpecifierDeclaration abstractDeclaration );

	public void enterFunctionBody( IASTFunction function );
	public void exitFunctionBody( IASTFunction function );
	
	public void enterCodeBlock( IASTCodeScope scope );
	public void exitCodeBlock( IASTCodeScope scope );

	public void enterCompilationUnit( IASTCompilationUnit compilationUnit ); 
	public void enterInclusion( IASTInclusion inclusion ); 
	public void enterNamespaceDefinition( IASTNamespaceDefinition namespaceDefinition );
	public void enterClassSpecifier( IASTClassSpecifier classSpecification );
	public void enterLinkageSpecification( IASTLinkageSpecification linkageSpec );
	
	public void enterTemplateDeclaration( IASTTemplateDeclaration declaration );
	public void enterTemplateSpecialization( IASTTemplateSpecialization specialization );
	public void enterTemplateInstantiation( IASTTemplateInstantiation instantiation );
	
	public void acceptMethodDeclaration( IASTMethod method );
	public void enterMethodBody( IASTMethod method );
	public void exitMethodBody( IASTMethod method );
	public void acceptField( IASTField field );

	public void acceptClassReference( IASTClassReference reference );
	public void acceptTypedefReference( IASTTypedefReference reference );
	public void acceptNamespaceReference( IASTNamespaceReference reference );
	public void acceptEnumerationReference( IASTEnumerationReference reference );
	public void acceptVariableReference( IASTVariableReference reference );
	public void acceptFunctionReference( IASTFunctionReference reference );
	public void acceptFieldReference( IASTFieldReference reference );
	public void acceptMethodReference( IASTMethodReference reference );
	public void acceptEnumeratorReference( IASTEnumeratorReference reference );
	public void acceptParameterReference(IASTParameterReference reference);
	public void acceptTemplateParameterReference( IASTTemplateParameterReference reference );
	public void acceptFriendDeclaration( IASTDeclaration declaration );
	
	public void exitTemplateDeclaration( IASTTemplateDeclaration declaration );
	public void exitTemplateSpecialization( IASTTemplateSpecialization specialization );
	public void exitTemplateExplicitInstantiation( IASTTemplateInstantiation instantiation );
	
	public void exitLinkageSpecification( IASTLinkageSpecification linkageSpec );
	public void exitClassSpecifier( IASTClassSpecifier classSpecification ); 	 
	public void exitNamespaceDefinition( IASTNamespaceDefinition namespaceDefinition ); 
	public void exitInclusion( IASTInclusion inclusion ); 
	public void exitCompilationUnit( IASTCompilationUnit compilationUnit );

	/**
	 * @param finalPath
	 * @return
	 */
	public CodeReader createReader(String finalPath, Iterator workingCopies );

}
