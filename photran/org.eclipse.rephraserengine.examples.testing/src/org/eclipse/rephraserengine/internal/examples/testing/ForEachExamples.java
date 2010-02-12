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
import org.eclipse.rephraserengine.testing.combinatorial.ForEach;

/**
 * Examples of how to use {@link ForEach} methods.
 * 
 * @author Jeff Overbey
 */
public class ForEachExamples extends TestCase
{
    public void testForeachCombination()
    {
        List<String> people = Arrays.asList("Jim", "Kathy", "Bob", "Alice");
        for (List<String> combination : ForEach.combinationOf(people))
            System.out.println(combination);
    }

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
}
