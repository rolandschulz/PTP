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
 * Collection of assorted math routines.
 * 
 * @author Jeff Overbey
 * 
 * @since 2.0
 */
public class MathUtil
{
    private MathUtil() {;}
    
    /** @return the binomial coefficient <i>C(n, k)</i> */
    public static long binomialCoefficient(int n, int k)
    {
        if (k > n) return 0;

        long result = 1;
        for (int i = 1; i <= k; i++)
             result = result * (n-k+i) / i;
        return result;
    }
    
    /** @return the <i>n!</i> */
    public static long factorial(int n)
    {
        if (n < 0) throw new IllegalArgumentException();
        if (n < 2) return 1;

        long result = 1;
        for (int i = n; i >= 2; i--)
             result *= i;
        return result;
    }
}
