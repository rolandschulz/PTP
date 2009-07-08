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
package org.eclipse.photran.internal.core.analysis.dependence;

/**
 * Implements the GCD dependence test.
 * <p>
 * The GCD test is a simple dependence testing algorithm that simply
 * checks whether gcd(a1, ..., an, b1, ..., bn) divides b0 - a0.
 * <p>
 * Reference: Allen and Kennedy, <i>Optimizing Compilers for Modern
 * Architectures,</i> p. 96.
 * 
 * @author Jeff Overbey
 * @see IDependenceTester
 */
class GCDTest implements IDependenceTester
{
    public boolean test(int n, int[] L, int[] U, int[] a, int[] b, Direction[] direction)
    {
        assert n >= 1 && a.length == n+1 && b.length == n+1;
        
        int gcd = a[1];
        for (int i = 1; i <= n; i++)
        {
            gcd = gcd(gcd, a[i]);
            gcd = gcd(gcd, b[i]);
        }
        
        return divides(gcd, b[0]-a[0]);
    }

    /** @return the greatest common divisor of n and m */
    private static int gcd(int n, int m)
    {
        // Euclidean algorithm
        assert n >= 0 && m >= 0;
        
        while (m != 0)
        {
            int t = m;
            m = n % m;
            n = t;
        }
        return n;
    }   
    
    /** @return true iff n | m */
    private static boolean divides(int n, int m)
    {
        return m % n == 0;
    }
}
