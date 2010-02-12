/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.testing.combinatorial;

import java.util.Iterator;

/**
 * Generates all combinations of non-negative integers within specified limits.
 * <p>
 * If a {@link CombinationGenerator} is constructed with parameters (5, 2), it will generate all
 * combinations of up to two integers, each in the range 0..4. Successive calls to
 * {@link #nextCombination(int[])} will return the following int arrays:
 * <pre>
 * 0
 * 1
 * 2
 * 3
 * 4
 * 0 1
 * 0 2
 * 0 3
 * 0 4
 * 1 2
 * 1 3
 * 1 4
 * 2 3
 * 2 4
 * 3 4
 * null
 * </pre>
 * 
 * @author Jeff Overbey
 */
public class CombinationGenerator implements Iterable<int[]>
{
    private int maxValuePlusOne, maxItems;

    public CombinationGenerator(int maxValuePlusOne, int maxItems)
    {
        this.maxValuePlusOne = maxValuePlusOne;
        this.maxItems = Math.min(maxItems, maxValuePlusOne);
    }

    /**
     * Returns the first combination of integers. This is always {0}.
     * <p>
     * It can be passed to {@link #nextCombination(int[])} to generate the next combination.
     * 
     * @return the first combination of integers (always {0})
     * */
    public int[] firstCombination()
    {
        return new int[] { 0 };
    }

    /**
     * Returns the next combination of integers (according to the parameters pass to this
     * {@link CombinationGenerator}'s constructor), given the previous combination returned by this
     * method or by {@link #firstCombination()}.  <i>Note that the <code>lastCombination</code>
     * array is modified in-place.</i>
     * 
     * @param lastCombination an array returned by {@link #firstCombination()} or
     *            {@link #nextCombination(int[])}
     * 
     * @return a combination of integers, or <code>null</code> if there are no more combinations to
     *         generate
     */
    public int[] nextCombination(int[] lastCombination)
    {
        if (lastCombination == null) return null;

        int indexToIncrement = findIndexToIncrement(lastCombination);
        if (indexToIncrement < 0)
        {
            return increaseLength(lastCombination);
        }
        else
        {
            incrementIndex(indexToIncrement, lastCombination);
            return lastCombination;
        }
    }

    private int findIndexToIncrement(int[] configuration)
    {
        int indexToIncrement = configuration.length - 1;

        // The goal is to do something like this:
        // if (configuration[7] == maxValue-1)
        // indexToIncrement = 6;
        // if (configuration[6] == maxValue-2)
        // indexToIncrement = 5;

        for (int i = configuration.length - 1; i >= 0; i--)
        {
            if (configuration[i] == maxValuePlusOne - (configuration.length - i))
            {
                indexToIncrement--;
            }
        }

        return indexToIncrement;
    }

    private void incrementIndex(int indexToIncrement, int[] configuration)
    {
        configuration[indexToIncrement]++;

        for (int i = indexToIncrement + 1; i < configuration.length; i++)
            configuration[i] = configuration[i - 1] + 1;
    }

    private int[] increaseLength(int[] configuration)
    {
        if (configuration.length == maxItems) return null;

        int[] newConfig = new int[configuration.length + 1];
        for (int i = 0; i < newConfig.length; i++)
            newConfig[i] = i;
        return newConfig;
    }

    /* @see java.lang.Iterable#iterator() */
    public Iterator<int[]> iterator()
    {
        return new Iterator<int[]>()
        {
            private int[] nextCombination = firstCombination();
            
            public boolean hasNext()
            {
                return nextCombination != null;
            }

            public int[] next()
            {
                int[] result = nextCombination.clone();
                nextCombination = nextCombination(nextCombination);
                return result;
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }
}
