/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jeff Overbey, UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.internal.ui.browser;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;
import org.eclipse.rephraserengine.ui.actions.VPGWindowActionDelegate;
import org.eclipse.swt.widgets.Display;

/**
 * Implements the Browse VPG Database action in the Refactor/(Debugging) menu
 *
 * @author Jeff Overbey
 */
public class BrowseDBAction extends VPGWindowActionDelegate implements Runnable
{
    private EclipseVPG vpg;

    @Override
    protected void run(EclipseVPG vpg, IProgressMonitor progressMonitor) throws Exception
    {
        this.vpg = vpg;
        Display.getDefault().asyncExec(this);
    }

    public void run()
    {
        new VPGBrowser(this.vpg).open();
    }
}
