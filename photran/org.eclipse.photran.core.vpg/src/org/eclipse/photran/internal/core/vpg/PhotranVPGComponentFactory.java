/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.vpg;

import java.io.File;
import java.io.IOException;

import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.Activator;
import org.eclipse.photran.internal.core.FortranCorePlugin;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;
import org.eclipse.rephraserengine.core.vpg.IVPGComponentFactory;
import org.eclipse.rephraserengine.core.vpg.VPGDB;
import org.eclipse.rephraserengine.core.vpg.VPGLog;
import org.eclipse.rephraserengine.core.vpg.db.profiling.ProfilingDB;
import org.eclipse.rephraserengine.core.vpg.eclipse.IEclipseVPGComponentFactory;

/**
 * Photran's {@link IVPGComponentFactory}, which creates the log, database, etc. used by {@link PhotranVPG}.
 * 
 * @author Jeff Overbey
 */
public class PhotranVPGComponentFactory
  implements IEclipseVPGComponentFactory<IFortranAST, Token, PhotranTokenRef>
{
    public VPGLog<Token, PhotranTokenRef> createLog()
    {
        File logFile = new File(
            Activator.getDefault().getStateLocation().addTrailingSeparator().toOSString() +
            "photran70vpg-log.txt"); //$NON-NLS-1$

        return new VPGLog<Token, PhotranTokenRef>(logFile, this);
    }

    public VPGDB<IFortranAST, Token, PhotranTokenRef> createDatabase(VPGLog<Token, PhotranTokenRef> log)
    {
        File file = FortranCorePlugin.inTestingMode()
            ? createTempFile()
            : getFileInPluginStateLocation();
        
        if (FortranPreferences.ENABLE_VPG_LOGGING.getValue())
            System.out.println("Using Photran VPG database " + file.getAbsolutePath()); //$NON-NLS-1$

        return new ProfilingDB<IFortranAST, Token, PhotranTokenRef>(
            new PhotranVPGDB1(this, file, log));
            //new PhotranVPGDB2(this, file, log));
    }

    private static File createTempFile()
    {
        try
        {
            File f = File.createTempFile("vpg", null); //$NON-NLS-1$
            f.deleteOnExit();
            return f;
        }
        catch (IOException e)
        {
            throw new Error(e);
        }
    }

    private static File getFileInPluginStateLocation()
    {
        return new File(Activator.getDefault().getStateLocation().addTrailingSeparator().toOSString() + "photran70vpg.new"); //$NON-NLS-1$
    }

    public PhotranVPGWriter createVPGWriter(VPGDB<IFortranAST, Token, PhotranTokenRef> db, VPGLog<Token,PhotranTokenRef> log)
    {
        return new PhotranVPGWriter(db, log);
    }

    public PhotranTokenRef getVPGNode(String filename, int offset, int length)
    {
        return new PhotranTokenRef(filename, offset, length);
    }
}
