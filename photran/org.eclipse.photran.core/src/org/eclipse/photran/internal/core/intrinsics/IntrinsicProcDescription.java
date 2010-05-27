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
package org.eclipse.photran.internal.core.intrinsics;

import java.util.ArrayList;
import java.util.List;

/**
 * A summary of a Fortran intrinsic procedure.
 * <p>
 * The list of intrinsic procedures is in Section 13.5 of the Fortran 2008 ISO standard.
 * 
 * @author Jeff Overbey
 */
public final class IntrinsicProcDescription implements Comparable<IntrinsicProcDescription>
{
    public final String genericName;
    public final String args;
    public final String description;

    IntrinsicProcDescription(String name, String args, String description)
    {
        super();
        this.genericName = name;
        this.args = args;
        this.description = description;
    }

    public int compareTo(IntrinsicProcDescription that)
    {
        return this.genericName.compareTo(that.genericName);
    }

    @Override public int hashCode()
    {
        return this.genericName.hashCode();
    }
    
    @Override public boolean equals(Object that)
    {
        if (that == null || !this.getClass().equals(that.getClass()))
            return false;
        else
            return ((IntrinsicProcDescription)that).genericName.equals(this.genericName);
    }
    
    @Override public String toString()
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("! "); sb.append(description); sb.append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
        sb.append("!\n"); //$NON-NLS-1$
        sb.append("! Usage: "); sb.append(genericName); sb.append(args); sb.append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
//        for (String form : getAllForms())
//        {
//            sb.append("!     "); sb.append(form); sb.append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
//        }
        sb.append("!\n"); //$NON-NLS-1$
        sb.append("INTRINSIC "); sb.append(genericName); sb.append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
        
        return sb.toString();
    }

    /**
     * @return a list of every set of arguments with which this subprogram may be invoked.
     * For example, the <tt>count</tt> intrinsic has the following forms:
     * <ul>
     * <li> <tt>count(mask)</tt>
     * <li> <tt>count(mask, dim)</tt>
     * <li> <tt>count(mask, dim, kind)</tt>
     * </ul>
     */
    public List<String> getAllForms()
    {
        final String OR = " or "; //$NON-NLS-1$
        
        int orIndex = args.indexOf(OR);
        
        if (orIndex > 0)
        {
            String args1 = args.substring(0, orIndex);
            String args2 = args.substring(orIndex + OR.length());

            List<String> result = new ArrayList<String>();
            result.addAll(getAllForms(args1));
            result.addAll(getAllForms(args2));
            return result;
        }
        else
        {
            return getAllForms(args);
        }
    }

    private List<String> getAllForms(String args)
    {
        args = removeParensAndRBracket(args);
        int lbracket = findLBracket(args);
        String requiredArgs = args.substring(0, lbracket).trim();
        String allOptionalArgs = args.substring(Math.min(lbracket+1, args.length())).trim();

        List<String> result = new ArrayList<String>();
        
        result.add(proposalFor(requiredArgs));
        
        if (allOptionalArgs.length() > 0)
        {
            for (int comma = allOptionalArgs.indexOf(',', 1);
                 comma >= 0;
                 comma = allOptionalArgs.indexOf(',', comma+1))
            {
                result.add(proposalFor(requiredArgs + allOptionalArgs.substring(0, comma).trim()));
            }
            
            result.add(proposalFor(requiredArgs + allOptionalArgs));
        }
        
        return result;
    }

    private String removeParensAndRBracket(String args)
    {
        return args.replaceAll("[(\\])]", ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private int findLBracket(String args)
    {
        int lbracket = args.indexOf('[');
        if (lbracket >= 0)
            return lbracket;
        else
            return args.length();
    }

    private String proposalFor(String args)
    {
        return genericName + "(" + args + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}