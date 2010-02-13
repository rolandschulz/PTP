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
    private final List<String> people = Arrays.asList("Jim", "Kathy", "Bob", "Alice", "Sam", "Eor");

    private final List<Integer> numbers = Arrays.asList(Integer.MIN_VALUE, -19, -18, -17, -11, -5,
        -2, -1, 0, 1, 2, 5, 10, 11, 16, 17, 18, 19, Integer.MAX_VALUE);

    private final List<String> colors = Arrays.asList("Red", "Blue");

    /**
     * Illustrates a simple use of {@link ForEach#combinationOf(List)}.
     * <p>
     * This example prints all combinations of 1 to 4 people to {@link System#out}.
     */
    public void testForeachCombination()
    {
        for (List<String> combination : ForEach.combinationOf(people))
            System.out.println(combination);
    }

    /**
     * This test verifies that {@link ForEach#combinationOf(int, int, List)} returns the expected
     * number of results.
     */
    public void testCountCombinations()
    {
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
     * This example tests that an alternative implementation of the modulo operator (
     * {@link #mod(int, int)}) returns the same results as Java's <code>%</code> operator.
     */
    public void testForEachPair()
    {
        for (Pair<Integer, Integer> pair : ForEach.pairOf(numbers))
        {
            if (pair.snd == 0) continue;

            int expected = pair.fst % pair.snd;
            int actual = mod(pair.fst, pair.snd);
            System.out.println("Checking mod(" + pair.fst + ", " + pair.snd + ") == " + expected);
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
        return lhs - rhs * (lhs / rhs);
    }

    /**
     * Illustrates a simple use of {@link ForEach#combinationOf(List)}.
     * <p>
     * This example prints all combinations of 1 to 4 people to {@link System#out}.
     */
    @SuppressWarnings("unchecked")
    public void testForeachTuple1()
    {
        for (List<Integer> tuple : ForEach.tupleOf(numbers, numbers))
            System.out.println(tuple);
    }

    /**
     * Illustrates a simple use of {@link ForEach#combinationOf(List)}.
     * <p>
     * This example prints all combinations of 1 to 4 people to {@link System#out}.
     */
    @SuppressWarnings("unchecked")
    public void testForeachTuple2()
    {
        for (List<Object> tuple : ForEach.<Object> tupleOf(people, numbers, colors))
            System.out.println(tuple);
    }

    /**
     * This test verifies that {@link ForEach#combinationOf(int, int, List)} returns the expected
     * number of results.
     */
    @SuppressWarnings("unused")
    public void testCountTuples()
    {
        for (int tupleSize = 1; tupleSize <= 5; tupleSize++)
        {
            List<?>[] sets = new List<?>[tupleSize];
            for (int i = 0; i < sets.length; i++)
                sets[i] = numbers;

            int count = 0;
            for (List<?> tuple : ForEach.tupleOf(sets))
                count++;

            assertEquals((int)Math.pow(numbers.size(), tupleSize), count);
        }
    }
}
