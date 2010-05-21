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
 * A rule indicating that a particular type of semantic edge should be preserved in a program graph;
 * also indicates the direction(s) in which such edges should be preserved.
 *
 * @author Jeff Overbey
 *
 * @since 1.0
 */
public final class PreservationRule
{
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Factory Methods (alpha/epsilon methods normalize to rho-operations)
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static final PreservationRule preserveIncoming(int edgeType)
    {
        return new PreservationRule(edgeType, true, false);
    }

    public static final PreservationRule preserveOutgoing(int edgeType)
    {
        return new PreservationRule(edgeType, false, true);
    }

    public static final PreservationRule preserveAll(int edgeType)
    {
        return new PreservationRule(edgeType, true, true);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Class Members
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private int edgeTypeToPreserve;
    private boolean preserveIncoming, preserveOutgoing;

    private PreservationRule(int edgeType, boolean preserveIncoming, boolean preserveOutgoing)
    {
        this.edgeTypeToPreserve = edgeType;
        this.preserveIncoming = preserveIncoming;
        this.preserveOutgoing = preserveOutgoing;
    }

    /**
     * @since 2.0
     */
    public boolean definitelyShouldNotPreserve(boolean isIncoming, boolean isOutgoing, int edgeType)
    {
        // entirely contained in affected region
        return edgeType == edgeTypeToPreserve && isIncoming && isOutgoing;
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
    
    @Override public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Preserve ");
        if (preserveIncoming && preserveOutgoing)
            sb.append("all");
        else if (preserveIncoming)
            sb.append("incoming");
        else if (preserveOutgoing)
            sb.append("outgoing");
        sb.append(" edges of type ");
        sb.append(edgeTypeToPreserve);
        return sb.toString();
    }
}
