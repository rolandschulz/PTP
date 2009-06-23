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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.core.analysis.binding.Binder;

/**
 * Implements the Reset VPG Statistics action in the Refactor/(Debugging) menu
 * 
 * @author Jeff Overbey
 */
public class ResetVPGStatistics extends FortranEditorActionDelegate
{
    public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException
    {
        try
        {
        	progressMonitor.beginTask("Waiting for background work to complete (Photran indexer)", IProgressMonitor.UNKNOWN);
            PhotranVPG.getDatabase().resetStatistics();
            Binder.resetStatistics();
        }
        catch (Exception e)
        {
            throw new InvocationTargetException(e);
        }
        finally
        {
        	progressMonitor.done();
        }
    }
}