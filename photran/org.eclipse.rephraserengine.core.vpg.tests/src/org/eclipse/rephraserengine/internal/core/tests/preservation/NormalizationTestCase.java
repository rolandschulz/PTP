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
import org.eclipse.rephraserengine.internal.core.preservation.PrimitiveOpList;

/**
 *
 * @author Jeff Overbey
 */
public class NormalizationTestCase extends TestCase
{
    public void testOffset()
    {
        final String FILE = "no_filename";
        
        PrimitiveOp.Alpha alpha = PrimitiveOp.alpha(FILE, 3, 6);
        int count = alpha.k.cardinality();
        assertEquals(3, count);

        Interval before = new Interval(0, 3);
        Interval overlapped = new Interval(3, 7);
        Interval after = new Interval(7, 11);

        PrimitiveOpList ops = new PrimitiveOpList(alpha);
        
        assertEquals(before.lb, ops.offset(FILE, before.lb));
        assertEquals(before.ub-1, ops.offset(FILE, before.ub-1));
        assertEquals(overlapped.lb+count, ops.offset(FILE, overlapped.lb));
        assertEquals(overlapped.ub-1+count, ops.offset(FILE, overlapped.ub-1));
        assertEquals(after.lb+count, ops.offset(FILE, after.lb));
        assertEquals(after.ub-1+count, ops.offset(FILE, after.ub-1));
    }
}
