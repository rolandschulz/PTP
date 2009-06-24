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
package org.eclipse.photran.internal.core.analysis.loops;

import org.eclipse.photran.internal.core.parser.Parser.IASTVisitor;

/**
 * An extension of {@link IASTVisitorWithLoops} which contains an additional method so that it can visit
 * {@link ASTProperLoopConstructNode}s.
 * 
 * @author Jeff Overbey
 * @see ASTProperLoopConstructNode
 */
public interface IASTVisitorWithLoops extends IASTVisitor
{
    void visitASTProperLoopConstructNode(ASTProperLoopConstructNode node);
}
