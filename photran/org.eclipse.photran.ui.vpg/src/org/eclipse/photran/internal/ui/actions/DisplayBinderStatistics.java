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
package org.eclipse.photran.internal.ui.actions;

import java.io.PrintStream;

import org.eclipse.photran.internal.core.analysis.binding.Binder;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;
import org.eclipse.rephraserengine.ui.actions.VPGOutputWindowAction;

/**
 * Implements the Display Binder Statistics action in the Refactor/(Debugging) menu
 *
 * @author Jeff Overbey
 */
public class DisplayBinderStatistics extends VPGOutputWindowAction
{
    @SuppressWarnings("rawtypes")
    @Override
    protected void writeOutput(EclipseVPG vpg, PrintStream ps)
    {
        Binder.printStatisticsOn(ps);
    }
}