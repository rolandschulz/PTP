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
package org.eclipse.rephraserengine.core.util;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * An {@link Iterator} that traverses a {@link List} in reverse order.
 *
 * @author Jeff Overbey
 *
 * @since 3.0
 */
public class ReverseIterator<T> implements Iterator<T>
{
    private final ListIterator<T> it;

    public ReverseIterator(List<T> list)
    {
        this.it = list.listIterator(list.size());
    }

    public boolean hasNext()
    {
        return it.hasPrevious();
    }

    public T next()
    {
        return it.previous();
    }

    public void remove()
    {
        it.remove();
    }
}
