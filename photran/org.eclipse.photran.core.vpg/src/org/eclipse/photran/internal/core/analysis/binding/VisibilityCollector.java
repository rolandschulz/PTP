/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.analysis.binding;

import java.util.List;

import org.eclipse.photran.core.vpg.PhotranTokenRef;
import org.eclipse.photran.internal.core.parser.ASTAccessStmtNode;
import org.eclipse.photran.internal.core.parser.ASTGenericNameNode;
import org.eclipse.photran.internal.core.parser.IAccessId;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;

/**
 * Visits PUBLIC and PRIVATE specification statements in the VPG and
 * sets the visibility of the corresponding definition accordingly.
 * <p>
 * This is a common superclass of both {@link SpecificationCollector}
 * and {@link ModuleLoader}.  Since Access-Spec statements can be
 * used to change the visibility of definitions imported from modules,
 * we must interpret these *twice*: first, we attempt to resolve them
 * to a locally-declared definition in the {@link SpecificationCollector}
 * (since the {@link ModuleLoader} will subsequently export any module
 * symbol tables).  Then, after any modules have been loaded, we
 * attempt to resolve them to an imported definition in the
 * {@link ModuleLoader}.
 * 
 * @author Jeff Overbey
 */
public abstract class VisibilityCollector extends BindingCollector
{
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
        
        IASTListNode<IAccessId> list = node.getAccessIdList();
        if (list == null) return; // This case handled in DefinitionCollector
        
        ScopingNode enclosingScope = node.findNearestAncestor(ScopingNode.class);

        for (int i = 0; i < list.size(); i++)
        {
            if (list.get(i) instanceof ASTGenericNameNode)
            {
                List<PhotranTokenRef> bindings = bindNoImplicits(((ASTGenericNameNode)list.get(i)).getGenericName());

                try
                {
                    for (PhotranTokenRef tr : bindings)
                    {
                        Definition def = vpg.getDefinitionFor(tr);
                        def.setVisibility(node.getAccessSpec(), enclosingScope);
                        vpg.setDefinitionFor(tr, def);
                    }
                }
                catch (Exception e)
                {
                    throw new Error(e);
                }
            }
        }
    }
}
