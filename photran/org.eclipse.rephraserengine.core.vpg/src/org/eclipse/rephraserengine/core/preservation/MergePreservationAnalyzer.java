/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.core.preservation;

import java.util.Collection;

import org.eclipse.rephraserengine.core.vpg.VPGEdge;

/**
 * 
 * @author Jeff Overbey
 */
final class MergePreservationAnalyzer extends PreservationAnalyzer
{
    public MergePreservationAnalyzer(
        Collection<VPGEdge<?,?,?>> initialEdges,
        Collection<VPGEdge<?,?,?>> finalEdges,
        PreservationRuleset ruleset)
    {
        super(initialEdges, finalEdges, ruleset);
    }

}
