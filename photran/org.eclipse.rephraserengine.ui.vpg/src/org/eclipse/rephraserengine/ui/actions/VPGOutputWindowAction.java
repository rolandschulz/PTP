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
package org.eclipse.rephraserengine.ui.actions;

import java.io.File;
import java.io.PrintStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;
import org.eclipse.rephraserengine.ui.UIUtil;

/**
 * Abstract class for an action that simply prints some text and then opens it in a window for the
 * user to view.
 *
 * @author Jeff Overbey
 * 
 * @since 1.0
 */
public abstract class VPGOutputWindowAction extends VPGWindowActionDelegate
{
    @Override
    protected final void run(EclipseVPG vpg, IProgressMonitor progressMonitor) throws Exception
    {
        progressMonitor.beginTask(Messages.VPGOutputWindowAction_PreparingTextForDisplay,
            IProgressMonitor.UNKNOWN);

        File temp = UIUtil.createTempFile();
        final PrintStream ps = UIUtil.createPrintStream(temp);
        writeOutput(vpg, ps);
        ps.close();

        UIUtil.openHtmlViewerOn("", temp); //$NON-NLS-1$

        progressMonitor.done();
    }

    /**
     * Subclasses must override this method and write text to the given {@link PrintStream}; this
     * text will be displayed to the user.
     *
     * @param vpg an {@link EclipseVPG} contributed to the <i>vpg</i> extension point; if only one
     *            has been contributed, it will be that; otherwise, the user will have been prompted
     *            to select a VPG, and this will be the VPG selected by the user
     * @param ps  a {@link PrintStream} to which the text to display to the user should be written
     */
    protected abstract void writeOutput(EclipseVPG vpg, PrintStream ps);
}