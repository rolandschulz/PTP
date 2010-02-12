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

import org.eclipse.rephraserengine.testing.combinatorial.Foreach;

/**
 * 
 * @author Jeff Overbey
 */
public class ForeachExamples extends TestCase
{
    public void testForeachCombination()
    {
        List<String> people = Arrays.asList("Jim", "Kathy", "Bob", "Alice");
        for (List<String> combination : Foreach.combinationOf(2, 3, people))
            System.out.println(combination);
    }
}
