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

import org.eclipse.photran.internal.core.parser.ASTAccessStmtNode;
import org.eclipse.photran.internal.core.parser.ASTPrivateSequenceStmtNode;

/**
 * Visits an AST, marking scopes whose default visibilities are PRIVATE
 * so that this will be known when the {@link DefinitionCollector}
 * begins collecting declarations.
 * 
 * @author Jeff Overbey
 */
class PrivateCollector extends BindingCollector
{
    // --VISITOR METHODS-------------------------------------------------

    // # R522
    // <AccessStmt> ::=
    // <LblDef> <AccessSpec> ( T_COLON T_COLON )? <AccessIdList> T_EOS
    // | <LblDef> <AccessSpec> T_EOS
    //
    // # R523
    // <AccessIdList> ::=
    // <AccessId>
    // | @:<AccessIdList> T_COMMA <AccessId>
    //
    // <AccessId> ::=
    // <GenericName>
    // | <GenericSpec>

    @Override public void visitASTAccessStmtNode(final ASTAccessStmtNode node)
    {
        super.traverseChildren(node);
        
        if (node.getAccessIdList() == null)
        {
            if (node.getAccessSpec().isPrivate())
            {
                try
                {
                    setScopeDefaultVisibilityToPrivate(node.getAccessSpec().findFirstToken().getEnclosingScope());
                }
                catch (Exception e)
                {
                    throw new Error(e);
                }
            }
        }
    }

    // # R424
    // <PrivateSequenceStmt> ::=
    //     <LblDef> T_PRIVATE T_EOS
    //  | <LblDef> T_SEQUENCE T_EOS
    
    @Override public void visitASTPrivateSequenceStmtNode(ASTPrivateSequenceStmtNode node)
    {
        super.traverseChildren(node);
        
        if (node.isPrivate())
        {
        	try
        	{
        		setScopeDefaultVisibilityToPrivate(node.getPrivateToken().getEnclosingScope());
        	}
        	catch (Exception e)
        	{
        		throw new Error(e);
        	}
        }
    }

}
