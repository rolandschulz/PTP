/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.core.vpg;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ImplicitSpec;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.lexer.Token;

import bz.over.vpg.TokenRef;
import bz.over.vpg.VPGDependency;
import bz.over.vpg.VPGEdge;

/**
 * The <code>PhotranVPGBuilder</code> provides methods for manipulating the
 * VPG database; these methods are used only when collecting bindings.
 * 
 * @author Jeff Overbey
 */
public class PhotranVPGBuilder extends PhotranVPG
{
	protected PhotranVPGBuilder()
	{
		super();
	}

	public void markFileAsExportingModule(IFile file, String moduleName)
	{
		ensure(new VPGDependency<IFortranAST, Token>(this, "module:" + canonicalizeIdentifier(moduleName), getFilenameForIFile(file)));
	}
	
	public void markFileAsImportingModule(IFile file, String moduleName)
	{
		ensure(new VPGDependency<IFortranAST, Token>(this, getFilenameForIFile(file), "module:" + canonicalizeIdentifier(moduleName)));
	}
	
	public void setDefinitionFor(TokenRef<Token> tokenRef, Definition definition)
	{
		setAnnotation(tokenRef, DEFINITION_ANNOTATION_TYPE, definition);
	}
	
	public void markScope(Token identifier, ScopingNode scope)
	{
		ensure(new VPGEdge<IFortranAST, Token>(this, identifier.getTokenRef(), scope.getRepresentativeToken(), DEFINED_IN_SCOPE_EDGE_TYPE));
	}
	
	public void markBinding(Token reference, PhotranTokenRef definition)
	{
		ensure(new VPGEdge<IFortranAST, Token>(this, reference.getTokenRef(), definition, BINDING_EDGE_TYPE));
	}
	
	public void markRenamedBinding(Token reference, PhotranTokenRef definition)
	{
		ensure(new VPGEdge<IFortranAST, Token>(this, reference.getTokenRef(), definition, RENAMED_BINDING_EDGE_TYPE));
	}
	
	public void setScopeImplicitSpec(ScopingNode scope, ImplicitSpec implicitSpec)
	{
		if (implicitSpec != null)
			setAnnotation(scope.getRepresentativeToken(), SCOPE_IMPLICIT_SPEC_ANNOTATION_TYPE, implicitSpec);
		else
			deleteAnnotation(scope.getRepresentativeToken(), SCOPE_IMPLICIT_SPEC_ANNOTATION_TYPE);
	}
	
	public void setDefaultScopeVisibilityToPrivate(ScopingNode scope)
	{
		setAnnotation(scope.getRepresentativeToken(), SCOPE_DEFAULT_VISIBILITY_IS_PRIVATE_ANNOTATION_TYPE, Boolean.TRUE);
	}
}
