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

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ITypedNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;

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
        super(msg + describeLocation((IASTNode)node));
    }

    private static String describeLocation(IASTNode node)
    {
        if (node == null) return "";
        
        Token tok = node.findFirstToken();
        if (tok == null) return "";
            
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
}
