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
package org.eclipse.rephraserengine.internal.examples.testing;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.rephraserengine.core.util.MathUtil;
import org.eclipse.rephraserengine.core.util.Pair;
import org.eclipse.rephraserengine.testing.combinatorial.ForEach;

/**
 * Examples of how to use {@link ForEach} methods.
 * 
 * @author Jeff Overbey
 */
public class ForEachExamples extends TestCase
{
    /**
     * Illustrates a simple use of {@link ForEach#combinationOf(List)}.
     * <p>
     * This example prints all combinations of 1 to 4 people to {@link System#out}.
     */
    public void testForeachCombination()
    {
        List<String> people = Arrays.asList("Jim", "Kathy", "Bob", "Alice");
        for (List<String> combination : ForEach.combinationOf(people))
            System.out.println(combination);
    }

    /**
     * This test verifies that {@link ForEach#combinationOf(int, int, List)} returns the expected
     * number of results.
     */
    public void testCountCombinations()
    {
        List<String> people = Arrays
            .asList("Jim", "Kathy", "Bob", "Alice", "Sam", "Eor", "Crystal");

        for (int min = 1; min <= people.size(); min++)
        {
            for (int max = min; max <= people.size(); max++)
            {
                System.out.println("Choosing at least " + min + " and at most " + max + " people");

                long actualCombinations = 0;
                for (List<String> combination : ForEach.combinationOf(min, max, people))
                {
                    System.out.println(combination);
                    actualCombinations++;
                }

                long expectedCombinations = 0L;
                for (int k = min; k <= max; k++)
                    expectedCombinations += MathUtil.binomialCoefficient(people.size(), k);

                assertEquals(expectedCombinations, actualCombinations);
            }
        }
    }
    
    /**
     * Illustrates a simple use of {@link ForEach#pairOf(List)}.
     * <p>
     * This example tests that an alternative implementation of the modulo operator
     * ({@link #mod(int, int)}) returns the same results as Java's <code>%</code>
     * operator.
     */
    public void testForEachPair()
    {
        List<Integer> numbers = Arrays.asList(
            Integer.MIN_VALUE, -19, -18, -17, -11, -5, -2, -1, 0,
            1, 2, 5, 10, 11, 16, 17, 18, 19, Integer.MAX_VALUE);

        for (Pair<Integer, Integer> pair : ForEach.pairOf(numbers))
        {
            if (pair.snd == 0) continue;
            
            System.out.println("Checking mod(" + pair.fst + ", " + pair.snd + ")");
            int expected = pair.fst % pair.snd;
            int actual = mod(pair.fst, pair.snd);
            assertEquals(expected, actual);
        }
    }
    
    /**
     * Alternative definition of the modulo operator.
     * <p>
     * See, for example, http://en.wikipedia.org/wiki/Modulo_operation
     * 
     * @return lhs % rhs
     */
    private static int mod(int lhs, int rhs)
    {
        return lhs - rhs * (lhs/rhs);
    }
}
