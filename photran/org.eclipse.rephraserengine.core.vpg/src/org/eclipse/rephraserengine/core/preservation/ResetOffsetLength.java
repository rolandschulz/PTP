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
package org.eclipse.rephraserengine.core.preservation;

/**
 * After an AST node is marked for an alpha-operation, the analyzer will request that the node be
 * adapted to this type.  Clients may ignore this request.
 * <p>
 * In Photran, this has the side effect of resetting the offset of every token under the node to -1,
 * so that the subtree will be excluded when attempting to compute offset information for other
 * nodes (see Photran's <code>ASTNodeAdapterFactory</code> for details).
 *
 * @author Jeff Overbey
 *
 * @see PreservationAnalysis#markAlpha(org.eclipse.core.resources.IFile, Object)
 * @see PreservationAnalysis#markEpsilon(org.eclipse.core.resources.IFile, Object)
 *
 * @since 1.0
 */
public final class ResetOffsetLength
{
    private ResetOffsetLength() {}

    public static final ResetOffsetLength RESET = new ResetOffsetLength();
}
