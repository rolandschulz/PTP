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
package org.eclipse.photran.internal.ui.actions;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.photran.internal.core.lexer.CPreprocessingReader;
import org.eclipse.photran.internal.ui.editor.FortranEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.Workbench;

/**
 * Implements the Display Preprocessed Source action in the File menu
 *
 * @author Jeff Overbey
 */
@SuppressWarnings({ "restriction" })
public class DisplayPreprocessedSource extends FortranEditorActionDelegate
{
    public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException
    {
        try
        {
        	boolean isFixedForm = getFortranEditor().isFixedForm();
        	
            IDocument doc = getFortranEditor().getIDocument();
            Reader in = new StringReader(doc.get());
            Reader cppIn = new CPreprocessingReader(getFortranEditor().getIFile(), null, in);
            
            File tempFile = File.createTempFile(
            	"tmp", //$NON-NLS-1$
            	isFixedForm ? ".f" : ".f90"); //$NON-NLS-1$ //$NON-NLS-2$
            tempFile.deleteOnExit();
            PrintStream out =
            	new PrintStream(
            		new BufferedOutputStream(
            			new FileOutputStream(tempFile)));
            for (int c = cppIn.read(); c != -1; c = cppIn.read())
            	out.print((char)c);
            out.close();

            IDE.openEditor(
            		Workbench.getInstance().getActiveWorkbenchWindow().getActivePage(),
            		tempFile.toURI(),
            		FortranEditor.EDITOR_ID,
            		true);
        }
        catch (Exception e)
        {
        	String message = e.getMessage();
        	if (message == null) message = e.getClass().getName();
        	MessageDialog.openError(getFortranEditor().getShell(), "Error", message); //$NON-NLS-1$
        }
    }
}
