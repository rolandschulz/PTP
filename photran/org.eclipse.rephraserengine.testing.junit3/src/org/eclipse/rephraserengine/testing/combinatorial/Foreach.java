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
package org.eclipse.rephraserengine.testing.combinatorial;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * @author joverbey
 */
public class Foreach
{
    private Foreach() {;}
    
    public static <T> Iterable<List<T>> combinationOf(final List<T> list)
    {
        return combinationOf(1, list.size(), list);
    }

    public static <T> Iterable<List<T>> combinationOf(final int minNumElementsInCombination,
                                                      final int maxNumElementsInCombination,
                                                      final List<T> list)
    {
        return new Iterable<List<T>>()
        {
            public Iterator<List<T>> iterator()
            {
                return new CombinationIterator<T>(minNumElementsInCombination,
                                                  maxNumElementsInCombination,
                                                  list);
            }
        };
    }
    
    private static class CombinationIterator<T> implements Iterator<List<T>>
    {
        protected List<T> list;
        protected CombinationGenerator gen;
        protected int[] nextCombination;
        
        public CombinationIterator(int minNumElementsInCombination,
                                   int maxNumElementsInCombination,
                                   List<T> list)
        {
            if (minNumElementsInCombination < 1
                || minNumElementsInCombination > list.size()
                || maxNumElementsInCombination < 0
                || maxNumElementsInCombination > list.size())
                throw new IllegalArgumentException();
            
            this.list = list;
            this.gen = new CombinationGenerator(list.size(), maxNumElementsInCombination);
            this.nextCombination = gen.firstCombination(minNumElementsInCombination);
        }

        public boolean hasNext()
        {
            return nextCombination != null;
        }

        public List<T> next()
        {
            if (nextCombination == null) return null;
            
            List<T> combination = new ArrayList<T>(nextCombination.length);
            for (int i = 0; i < nextCombination.length; i++)
                combination.add(list.get(nextCombination[i]));
            
            nextCombination = gen.nextCombination(nextCombination);
            return combination;
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
