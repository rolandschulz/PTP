/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;

public class Region implements IRegion
{
    private int offset, length;
    
    public Region(int offset, int length)
    {
        this.offset = offset;
        this.length = length;
    }

    public int getOffset()
    {
        return offset;
    }

    public int getLength()
    {
        return length;
    }
}
