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
package org.eclipse.photran.internal.core.intrinsics;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.photran.internal.core.FortranCorePlugin;


/**
 * 
 * @author Jeff Overbey
 */
public class Intrinsics
{
    private static final String INTRINSIC_PROCEDURE_LIST = "org/eclipse/photran/internal/core/intrinsics/intrinsic-procedures.txt"; //$NON-NLS-1$
    
    private Intrinsics() {;}
    
    private static TreeSet<IntrinsicProcDescription> intrinsicProcedures = null;
    
    public static TreeSet<IntrinsicProcDescription> getAllIntrinsicProcedures()
    {
        if (intrinsicProcedures == null) loadData();
        
        return intrinsicProcedures;
    }

    private static void loadData()
    {
        intrinsicProcedures = new TreeSet<IntrinsicProcDescription>();

        for (String line : readIntrinsicProceduresFile())
        {
            String[] fields = line.split("\t"); //$NON-NLS-1$
            if (fields.length != 3) throw new Error("Malformed input"); //$NON-NLS-1$
            
            String name = fields[0].replace(' ', '_');
            String args = fields[1];
            String description = fields[2];
            
            intrinsicProcedures.add(new IntrinsicProcDescription(name, args, description));
        }
    }

    private static List<String> readIntrinsicProceduresFile()
    {
        URL url = FortranCorePlugin.getDefault().getBundle().getResource(INTRINSIC_PROCEDURE_LIST);
        if (url == null) throw new Error("Unable to locate " + INTRINSIC_PROCEDURE_LIST); //$NON-NLS-1$
        return readLinesFrom(url);
    }

    private static List<String> readLinesFrom(URL url) throws Error
    {
        List<String> result = new ArrayList<String>(128);

        BufferedReader in = null;
        try
        {
            in = new BufferedReader(new InputStreamReader(url.openStream()));
            for (String line = in.readLine(); line != null; line = in.readLine())
                result.add(line);
        }
        catch (IOException e)
        {
            throw new Error(e);
        }
        finally
        {
            close(in);
        }
        
        return result;
    }

    private static void close(BufferedReader in) throws Error
    {
        if (in != null) try
        {
            in.close();
        }
        catch (IOException e)
        {
            throw new Error(e);
        }
    }
}