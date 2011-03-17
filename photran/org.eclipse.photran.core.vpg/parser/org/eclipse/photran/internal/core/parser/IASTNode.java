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

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;                   import org.eclipse.photran.internal.core.SyntaxException;                   import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Set;

@SuppressWarnings("all")
public interface IASTNode extends Cloneable
{
    Object clone();
    IASTNode getParent();
    void setParent(IASTNode parent);
    Iterable<? extends IASTNode> getChildren();
    void accept(IASTVisitor visitor);
    void replaceChild(IASTNode node, IASTNode withNode);
    void removeFromTree();
    void replaceWith(IASTNode newNode);
    void replaceWith(String literalString);
    <T extends IASTNode> Set<T> findAll(Class<T> targetClass);
    <T extends IASTNode> T findNearestAncestor(Class<T> targetClass);
    <T extends IASTNode> T findFirst(Class<T> targetClass);
    <T extends IASTNode> T findLast(Class<T> targetClass);
    Token findFirstToken();
    Token findLastToken();
    boolean isFirstChildInList();
    IPreprocessorReplacement printOn(PrintStream out, IPreprocessorReplacement currentPreprocessorDirective);
}
