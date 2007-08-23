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
package org.eclipse.photran.internal.core.analysis.binding;

import org.eclipse.photran.internal.core.parser.ASTBlockDataSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTDerivedTypeDefNode;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTImplicitStmtNode;
import org.eclipse.photran.internal.core.parser.ASTInterfaceBlockNode;
import org.eclipse.photran.internal.core.parser.ASTMainProgramNode;
import org.eclipse.photran.internal.core.parser.ASTModuleNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineSubprogramNode;

/**
 * Visits an AST, collecting implicit statements and updating the VPG accordingly.
 * <p>
 * This visitor should be run <i>top-down:</i> Each scoping node initially inherits the implicit
 * spec of its enclosing scope, and then if an IMPLICIT statement is encountered (as the
 * children of that scoping node are visited), its implicit spec will be adjusted accordingly.
 * 
 * @author Jeff Overbey
 */
public class ImplicitSpecCollector extends BindingCollector
{
    // # R541
    // <ImplicitStmt> ::=
    //   | <LblDef> T_IMPLICIT <ImplicitSpecList> T_EOS
    //   | <LblDef> T_IMPLICIT T_NONE             T_EOS
    // 
    // # R542
    // <ImplicitSpecList> ::=
    //   |                              <ImplicitSpec>
    //   | @:<ImplicitSpecList> T_COMMA <ImplicitSpec>
    // 
    // <ImplicitSpec> ::= <TypeSpec> T_xImpl

    @Override public void visitASTImplicitStmtNode(ASTImplicitStmtNode node)
    {
    	ImplicitSpec implicitSpec = null;
    	
    	if (node.getImplicitSpecList() != null)
    		implicitSpec = new ImplicitSpec(node.getImplicitSpecList());
    	
    	try { vpg.setScopeImplicitSpec(node.getTImplicit().getEnclosingScope(), implicitSpec); }
    	catch (Exception e) { throw new Error(e); }
    }

    

	@Override public void visitASTExecutableProgramNode(ASTExecutableProgramNode node)
	{
		setDefaultImplicitSpec(node);
	}

	@Override public void visitASTMainProgramNode(ASTMainProgramNode node)
	{
		setDefaultImplicitSpec(node);
	}

	@Override public void visitASTFunctionSubprogramNode(ASTFunctionSubprogramNode node)
	{
		setDefaultImplicitSpec(node);
	}
	
	@Override public void visitASTSubroutineSubprogramNode(ASTSubroutineSubprogramNode node)
	{
		setDefaultImplicitSpec(node);
	}

	@Override public void visitASTModuleNode(ASTModuleNode node)
	{
		setDefaultImplicitSpec(node);
	}

	@Override public void visitASTBlockDataSubprogramNode(ASTBlockDataSubprogramNode node)
	{
		setDefaultImplicitSpec(node);
	}

	@Override public void visitASTDerivedTypeDefNode(ASTDerivedTypeDefNode node)
	{
		setDefaultImplicitSpec(node);
	}

	@Override public void visitASTInterfaceBlockNode(ASTInterfaceBlockNode node)
	{
		setDefaultImplicitSpec(node);
	}
	
	private void setDefaultImplicitSpec(ScopingNode node)
    {
		if (!ScopingNode.isScopingNode(node)) return; // May be anonymous interface block
		
		ImplicitSpec implicitSpec;
		
		if (node == node.getGlobalScope())
			implicitSpec = new ImplicitSpec();
		else
			implicitSpec = node.getEnclosingScope().getImplicitSpec();
		
		try { vpg.setScopeImplicitSpec(node, implicitSpec); }
	    catch (Exception e) { throw new Error(e); }
    }
}
