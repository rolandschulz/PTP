/*******************************************************************************
 * Copyright (c) 2006, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.photran.internal.ui.search;

import org.eclipse.core.resources.IFile;
import org.eclipse.search.ui.text.Match;

/**
 * A match returned from a Fortran search.
 * <p>
 * Based on org.eclipse.cdt.internal.ui.search.PDOMSearchMatch.
 * 
 * @author Quillback
 */
public class VPGSearchMatch extends Match
{
    public VPGSearchMatch(IFile element, int offset, int length)
    {
        super(element, offset, length);
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof VPGSearchMatch))
            return false;
        VPGSearchMatch other = (VPGSearchMatch)obj;
        return getElement().equals(other.getElement())
            && getOffset() == other.getOffset()
            && getLength() == other.getLength();
    }
    
    @Override
    public String toString() {
        return this.getElement().toString() +
            ", offset " + this.getOffset() +
            ", length " + this.getLength();
    }
}
