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

import org.eclipse.rephraserengine.core.util.Pair;

/**
 * 
 * @author joverbey
 */
public class ForEach
{
    private ForEach() {;}
    
    public static <T> Iterable<List<T>> combinationOf(List<T> list)
    {
        return combinationOf(1, list.size(), list);
    }
    
    public static <T> Iterable<List<T>> combinationOf(int numElementsToChoose,
                                                      List<T> list)
    {
        return combinationOf(numElementsToChoose, numElementsToChoose, list);
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

    /**
     * Returns all pairs of.
     * @return
     */
    public static <T> Iterable<Pair<T,T>> pairOf(final List<T> objects)
    {
        return new Iterable<Pair<T,T>>()
        {
            public Iterator<Pair<T,T>> iterator()
            {
                return new PairIterator<T>(objects);
            }
        };
    }
    
    private static class PairIterator<T> implements Iterator<Pair<T,T>>
    {
        /** Set of objects from which pairs will be generated */
        protected final List<T> objects;
        
        /** Index of the first component of the pair in {@link #objects} */
        protected int fstIndex;
        
        /** Index of the second component of the pair in {@link #objects} */
        protected int sndIndex;
        
        public PairIterator(List<T> objects)
        {
            this.objects = objects;
            this.fstIndex = 0;
            this.sndIndex = 0;
        }

        public boolean hasNext()
        {
            return fstIndex < objects.size();
        }

        public Pair<T,T> next()
        {
            if (fstIndex == objects.size()) return null;
            
            Pair<T,T> result = new Pair<T,T>(objects.get(fstIndex), objects.get(sndIndex));
            
            if (++sndIndex == objects.size())
            {
                ++fstIndex;
                sndIndex = 0;
            }
            
            return result;
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
