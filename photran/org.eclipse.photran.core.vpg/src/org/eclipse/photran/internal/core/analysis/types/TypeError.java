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
package org.eclipse.photran.internal.core.analysis.types;

import org.eclipse.photran.core.vpg.util.Notification;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.GenericParseTreeVisitor;
import org.eclipse.photran.internal.core.parser.ITypedNode;
import org.eclipse.photran.internal.core.parser.Parser.InteriorNode;

/**
 * Exception indicating a typing error, e.g., assignment of a string to an integer variable.
 * 
 * @author Jeff Overbey
 * 
 * @see TypeChecker
 */
public class TypeError extends Exception
{
    private static final long serialVersionUID = 1L;

    public TypeError(ITypedNode node, String msg)
    {
        super(msg + describeLocation((InteriorNode)node));
    }

    private static String describeLocation(InteriorNode node)
    {
        if (node == null) return "";
        
        try
        {
            node.visitUsing(new GenericParseTreeVisitor()
            {
                @Override public void visitToken(Token token)
                {
                    throw new Notification(token);
                }
            });
        }
        catch (Notification n)
        {
            Token tok = (Token)n.getResult();
            
            StringBuilder sb = new StringBuilder();
            sb.append(" (");
            if (tok.getFile() != null) sb.append(tok.getFile().getName() + ", ");
            sb.append("line ");
            sb.append(tok.getLine());
            sb.append(", column ");
            sb.append(tok.getCol());
            sb.append(")");
            return sb.toString();
        }
        
        return "";
    }
}
