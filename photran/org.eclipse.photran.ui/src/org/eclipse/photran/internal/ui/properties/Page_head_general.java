/*******************************************************************************
 * Copyright (c) 2005, 2007 Intel Corporation and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.properties;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * A copy of CDT's <code>org.eclipse.cdt.ui.newui.Page_head_general</code>
 * 
 * @author Jeff Overbey
 */
public class Page_head_general extends PropertyPage
{
    protected Control createContents(Composite parent)
    {
        noDefaultAndApplyButton();
        return parent;
    }
}
