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

import org.eclipse.rephraserengine.core.util.TwoKeyHashMap;
import org.eclipse.rephraserengine.core.vpg.VPGEdge.Classification;

/**
 * Set of rules indicating how each type of semantic edge should be preserved in a program graph.
 *
 * @author Jeff Overbey
 * 
 * @since 3.0
 */
public class PreservationRuleset
{
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Static Members (for Rules)
    ///////////////////////////////////////////////////////////////////////////////////////////////

    static abstract class Processor
    {
        abstract void handlePreserveAll();
        abstract void handlePreserveSubset();
        abstract void handlePreserveSuperset();
        abstract void handleIgnore();
    }

    private static enum Rule
    {
        PRESERVE_ALL      { @Override protected void processUsing(Processor p) { p.handlePreserveAll(); } },
        PRESERVE_SUBSET   { @Override protected void processUsing(Processor p) { p.handlePreserveSubset(); } },
        PRESERVE_SUPERSET { @Override protected void processUsing(Processor p) { p.handlePreserveSuperset(); } },
        IGNORE            { @Override protected void processUsing(Processor p) { p.handleIgnore(); } };
        
        protected abstract void processUsing(Processor p);
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Class Members
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private TwoKeyHashMap<Integer, Classification, Rule> preservationRules =
        new TwoKeyHashMap<Integer, Classification, Rule>();
    
    public PreservationRuleset() {}
    
    public void preserveAll(int edgeType)
    {
        for (Classification c : Classification.values())
            addRule(edgeType, c, Rule.PRESERVE_ALL);
    }
    
    public void preserveIncoming(int edgeType)
    {
        addRule(edgeType, Classification.INCOMING, Rule.PRESERVE_ALL);
        addRule(edgeType, Classification.EXTERNAL, Rule.PRESERVE_ALL);
    }
    
    private void addRule(int edgeType, Classification classification, Rule rule)
    {
        preservationRules.put(edgeType, classification, rule);
    }
    
    void invokeCallback(int edgeType, Classification classification, Processor processor)
    {
        getRule(edgeType, classification).processUsing(processor);
    }

    private Rule getRule(int edgeType, Classification classification)
    {
        Rule result = preservationRules.getEntry(edgeType, classification);
        if (result != null)
            return result;
        else
            return Rule.IGNORE;
    }

    @Override public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (int type : preservationRules.keySet())
        {
            sb.append("Type "); //$NON-NLS-1$
            sb.append(type);
            sb.append(": "); //$NON-NLS-1$
            for (Classification c : Classification.values())
            {
                sb.append("  "); //$NON-NLS-1$
                sb.append(c.toString());
                sb.append(" - "); //$NON-NLS-1$
                sb.append(getRule(type, c));
            }
            sb.append("\n"); //$NON-NLS-1$
        }
        return sb.toString();
    }
}