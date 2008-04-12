/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.search;

import org.eclipse.photran.internal.ui.search.ReferenceSearch.SearchScope;


public class FortranFindReferencesProjectActionDelegate extends
                FortranFindReferencesActionDelegate
{
    @Override
    protected SearchScope getSearchScope()
    {
        return SearchScope.PROJECT;
    }
}
