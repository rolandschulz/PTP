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
package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.InteriorNode;

/**
 * Represents a Visitor for a parse tree, albeit one that cares more about whether it's visiting a
 * token versus an internal node than exactly what type of internal nodes it's stepping through.
 * 
 * See also <code>ASTVisitor</code>, which is more appropriate when the types of the
 * internal nodes are what is important.
 * 
 * @author Jeff Overbey
 */
public abstract class GenericParseTreeVisitor
{
    public void visitParseTreeNode(InteriorNode node)
    {
        ;
    }

    public void visitToken(Token token)
    {
        ;
    }

    public void preparingToVisitChildrenOf(InteriorNode node)
    {
        ;
    }

    public void doneVisitingChildrenOf(InteriorNode node)
    {
        ;
    }
}
