/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.fdt.internal.core.parser.token;

import org.eclipse.fdt.core.parser.IOffsetDuple;

/**
 * @author jcamelon
 *
 */
public class OffsetDuple implements IOffsetDuple
{
	private final int lineFloor, lineCeiling;
    /**
     * @param floor
     * @param ceiling
     */
    public OffsetDuple(int floor, int ceiling)
    {
        lineFloor = floor; 
        lineCeiling = ceiling;
    }
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.IOffsetDuple#getCeilingOffset()
     */
    public int getCeilingOffset()
    {
        return lineCeiling;
    }
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.IOffsetDuple#getFloorOffset()
     */
    public int getFloorOffset()
    {
        return lineFloor;
    }
}
