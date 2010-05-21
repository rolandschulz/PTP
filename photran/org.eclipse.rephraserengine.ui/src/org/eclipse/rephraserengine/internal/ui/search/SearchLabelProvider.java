/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.rephraserengine.internal.ui.search;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * @author Markus Schorn
 * @author Doug Schaefer
 * @author Jeff Overbey
 *
 * @since 2.0
 */
public class SearchLabelProvider extends LabelProvider
{
    private final AbstractTextSearchViewPage page;
    private final WorkbenchLabelProvider wrappedProvider;

    public SearchLabelProvider(AbstractTextSearchViewPage page)
    {
        this.page = page;
        this.wrappedProvider = new WorkbenchLabelProvider();
    }

    public Image getImage(Object element)
    {
        return wrappedProvider.getImage(element);
    }

    public String getText(Object element)
    {
        String text = wrappedProvider.getText(element);
        int count = getMatchCount(element);
        if (count == 0) { return text; }
        return text + " (" + count + (count == 1 ? " match)" : " matches)");
    }

    protected int getMatchCount(Object element)
    {
        return page.getInput().getMatchCount(element);
    }
}
