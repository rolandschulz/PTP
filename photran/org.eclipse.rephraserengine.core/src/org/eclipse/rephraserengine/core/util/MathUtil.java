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
package org.eclipse.rephraserengine.core.util;

/**
 * 
 * @author joverbey
 */
public class MathUtil
{
    private MathUtil() {;}
    
    /** @return the binomial coefficient <i>n choose k</i> */
    public static long binomialCoefficient(int n, int k)
    {
        if (k > n) return 0;

        long result = 1;
        for (int i = 1; i <= k; i++)
             result = result * (n-k+i) / i;
        return result;
    }

}
