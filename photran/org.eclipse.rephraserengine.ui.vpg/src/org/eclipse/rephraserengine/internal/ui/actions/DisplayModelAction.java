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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;
import org.eclipse.rephraserengine.internal.core.preservation.Model;
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
            if (info.editingAnIFile())
            {
                String filename = EclipseVPG.getFilenameForIFile(info.getFileInEditor());
                if (filename != null)
                {
                    ArrayList<Integer> lineMap = new ArrayList<Integer>();
                    String fileContents = readStream(lineMap,
                        new BufferedReader(new InputStreamReader(info.getFileInEditor().getContents(true), info.getFileInEditor().getCharset())));
                    ps.println(filename);
                    ps.println();
                    Model model = new Model("edge model", new NullProgressMonitor(), 0, vpg, filename); //$NON-NLS-1$
                    ps.print(model.toString(filename, fileContents, lineMap));
                    return;
                }
            }

            ps.print(Messages.DisplayModelAction_EditorMustBeOpen);
        }
        catch (Exception e)
        {
            ps.print(Messages.DisplayModelAction_AnErrorOccurred + "\n"); //$NON-NLS-1$
            e.printStackTrace(ps);
        }
    }

    protected String readStream(ArrayList<Integer> lineMap, Reader in) throws IOException
    {
        StringBuffer sb = new StringBuffer(4096);
        for (int offset = 0, ch = in.read(); ch >= 0; ch = in.read())
        {
            sb.append((char)ch);
            offset++;

            if (ch == '\n' && lineMap != null)
            {
                //System.out.println("Line " + (lineMap.size()+1) + " starts at offset " + offset);
                lineMap.add(offset);
            }
        }
        in.close();
        return sb.toString();
}
}