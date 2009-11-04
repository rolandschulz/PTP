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
package org.eclipse.rephraserengine.internal.core.tests.preservation;

import junit.framework.TestCase;

import org.eclipse.rephraserengine.internal.core.preservation.Interval;
import org.eclipse.rephraserengine.internal.core.preservation.PrimitiveOp;

/**
 *
 * @author Jeff Overbey
 */
public class NormalizationTestCase extends TestCase
{
    public void testOffset()
    {
        PrimitiveOp.Alpha alpha = PrimitiveOp.alpha("", 3, 6);
        assertEquals(3, alpha.j.cardinality());

        Interval before = new Interval(0, 3);
        Interval overlapped = new Interval(3, 7);
        Interval after = new Interval(7, 11);

        assertEquals(before, alpha.ioffset(before));
        assertEquals(new Interval(3+3, 7+3), alpha.ioffset(overlapped));
        assertEquals(new Interval(7+3, 11+3), alpha.ioffset(after));
    }
}
