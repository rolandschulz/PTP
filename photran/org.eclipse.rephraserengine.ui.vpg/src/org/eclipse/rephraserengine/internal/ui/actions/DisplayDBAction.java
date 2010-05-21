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
package org.eclipse.rephraserengine.internal.ui.actions;

import java.io.PrintStream;

import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;
import org.eclipse.rephraserengine.ui.actions.VPGOutputWindowAction;

/**
 * Implements the Display VPG Database action in the Refactor/(Debugging) menu
 *
 * @author Jeff Overbey
 *
 * @since 1.0
 */
public class DisplayDBAction extends VPGOutputWindowAction
{
    @Override
    protected void writeOutput(EclipseVPG vpg, PrintStream ps)
    {
        vpg.db.printOn(ps);
    }
}