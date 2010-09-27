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
package org.eclipse.rephraserengine.internal.ui.actions;

import java.io.PrintStream;

import org.eclipse.rephraserengine.core.preservation.PreservationAnalysis;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;
import org.eclipse.rephraserengine.ui.WorkbenchSelectionInfo;
import org.eclipse.rephraserengine.ui.actions.VPGOutputWindowAction;

/**
 * Implements the Display Edge Model action in the Refactor/(Debugging) menu
 *
 * @author Jeff Overbey
 *
 * @since 1.0
 */
public class DisplayModelAction extends VPGOutputWindowAction
{
    @Override
    protected void writeOutput(EclipseVPG vpg, PrintStream ps)
    {
        try
        {
            WorkbenchSelectionInfo info = new WorkbenchSelectionInfo();
            if (!info.editingAnIFile() || !PreservationAnalysis.printModelOn(ps, info.getFileInEditor(), vpg))
                ps.print(Messages.DisplayModelAction_EditorMustBeOpen);
        }
        catch (Exception e)
        {
            ps.print(Messages.DisplayModelAction_AnErrorOccurred + "\n"); //$NON-NLS-1$
            e.printStackTrace(ps);
        }
    }
}