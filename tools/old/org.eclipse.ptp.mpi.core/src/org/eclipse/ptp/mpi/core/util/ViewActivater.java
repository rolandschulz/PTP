/**********************************************************************
 * Copyright (c) 2005 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.mpi.core.util;

import org.eclipse.jface.util.Assert;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

/**
 * @author xue
 * 
 */
public class ViewActivater
{
    /**
     * Activate the view identified by the view id.
     * 
     * @param strViewId
     */
    public static void activateView(String strViewId)
    {
        Assert.isNotNull(strViewId);
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        try {
            page.showView(strViewId);
        } catch (PartInitException e) {
            e.printStackTrace();
        }
        // page.activate(page.findView(strViewId));
    }
}
