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
 * Indicates one type of a semantic edge that should be preserved in a program graph and the
 * direction(s) in which it should be preserved.
 *
 * @author Jeff Overbey
 *
 * @since 1.0
 */
public final class Preserve
{
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Factory Methods (alpha/epsilon methods normalize to rho-operations)
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static final Preserve incoming(int edgeType)
    {
        return new Preserve(edgeType, true, false);
    }

    public static final Preserve outgoing(int edgeType)
    {
        return new Preserve(edgeType, false, true);
    }

    public static final Preserve all(int edgeType)
    {
        return new Preserve(edgeType, true, true);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Class Members
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private int edgeTypeToPreserve;
    private boolean preserveIncoming, preserveOutgoing;

    private Preserve(int edgeType, boolean preserveIncoming, boolean preserveOutgoing)
    {
        this.edgeTypeToPreserve = edgeType;
        this.preserveIncoming = preserveIncoming;
        this.preserveOutgoing = preserveOutgoing;
    }

    public boolean shouldPreserve(boolean isIncoming, boolean isOutgoing, int edgeType)
    {
        if (edgeType != edgeTypeToPreserve)
            return false;
        else if (isIncoming && isOutgoing) // entirely contained in affected region
            return false;
        else if (isIncoming)
            return preserveIncoming;
        else if (isOutgoing)
            return preserveOutgoing;
        else                               // outside affected region
            return true;
    }
}
