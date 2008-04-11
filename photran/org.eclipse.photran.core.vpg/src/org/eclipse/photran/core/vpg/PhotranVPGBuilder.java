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
		db.ensure(new VPGDependency<IFortranAST, Token, PhotranTokenRef>(this, "module:" + canonicalizeIdentifier(moduleName), getFilenameForIFile(file)));
	}
	
	public void markFileAsImportingModule(IFile file, String moduleName)
	{
	    db.ensure(new VPGDependency<IFortranAST, Token, PhotranTokenRef>(this, getFilenameForIFile(file), "module:" + canonicalizeIdentifier(moduleName)));
	}
	
	public void setDefinitionFor(PhotranTokenRef tokenRef, Definition definition)
	{
	    db.setAnnotation(tokenRef, DEFINITION_ANNOTATION_TYPE, definition);
	}
	
	public void markScope(PhotranTokenRef identifier, ScopingNode scope)
	{
	    db.ensure(new VPGEdge<IFortranAST, Token, PhotranTokenRef>(this, identifier, scope.getRepresentativeToken(), DEFINED_IN_SCOPE_EDGE_TYPE));
	}
	
	public void markBinding(PhotranTokenRef reference, PhotranTokenRef definition)
	{
	    db.ensure(new VPGEdge<IFortranAST, Token, PhotranTokenRef>(this, reference, definition, BINDING_EDGE_TYPE));
	}
	
	public void markRenamedBinding(PhotranTokenRef reference, PhotranTokenRef definition)
	{
	    db.ensure(new VPGEdge<IFortranAST, Token, PhotranTokenRef>(this, reference, definition, RENAMED_BINDING_EDGE_TYPE));
	}
	
	public void setScopeImplicitSpec(ScopingNode scope, ImplicitSpec implicitSpec)
	{
		if (implicitSpec != null)
		    db.setAnnotation(scope.getRepresentativeToken(), SCOPE_IMPLICIT_SPEC_ANNOTATION_TYPE, implicitSpec);
		else
		    db.deleteAnnotation(scope.getRepresentativeToken(), SCOPE_IMPLICIT_SPEC_ANNOTATION_TYPE);
	}
	
	public void setDefaultScopeVisibilityToPrivate(ScopingNode scope)
	{
	    db.setAnnotation(scope.getRepresentativeToken(), SCOPE_DEFAULT_VISIBILITY_IS_PRIVATE_ANNOTATION_TYPE, Boolean.TRUE);
	}
}
