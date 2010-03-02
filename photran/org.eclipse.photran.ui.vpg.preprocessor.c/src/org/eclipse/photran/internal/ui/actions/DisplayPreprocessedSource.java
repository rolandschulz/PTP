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
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringBufferInputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.photran.internal.core.lexer.CPreprocessingInputStream;
import org.eclipse.photran.internal.ui.editor.FixedFormFortranEditor;
import org.eclipse.photran.internal.ui.editor.FreeFormFortranEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.Workbench;

/**
 * Implements the Display Preprocessed Source action in the File menu
 *
 * @author Jeff Overbey
 */
@SuppressWarnings({ "deprecation", "restriction" })
public class DisplayPreprocessedSource extends FortranEditorActionDelegate
{
    public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException
    {
        try
        {
        	boolean isFixedForm = getFortranEditor().isFixedForm();
        	
            IDocument doc = getFortranEditor().getIDocument();
            InputStream in = new StringBufferInputStream(doc.get());
            InputStream cppIn = new CPreprocessingInputStream(getFortranEditor().getIFile(), null, in);
            
            File tempFile = File.createTempFile(
            	"tmp",
            	isFixedForm ? ".f" : ".f90");
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
            		isFixedForm ? FixedFormFortranEditor.EDITOR_ID
            				    : FreeFormFortranEditor.EDITOR_ID,
            		true);
        }
        catch (Exception e)
        {
        	String message = e.getMessage();
        	if (message == null) message = e.getClass().getName();
        	MessageDialog.openError(getFortranEditor().getShell(), "Error", message);
        }
    }
}
