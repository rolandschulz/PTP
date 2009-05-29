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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.photran.internal.ui.editor.AbstractFortranEditor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.progress.IProgressService;

/**
 * Base class (with utility methods) for Fortran editor actions
 * 
 * @author Jeff Overbey
 */
public abstract class FortranEditorActionDelegate
    extends Action
    implements IEditorActionDelegate, IWorkbenchWindowActionDelegate, IRunnableWithProgress
{

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////
    
    protected IWorkbenchWindow window = null;
    protected AbstractFortranEditor fEditor = null;
    
    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public FortranEditorActionDelegate() {}
    
    public FortranEditorActionDelegate(AbstractFortranEditor ed) { fEditor = ed; }
    
    ///////////////////////////////////////////////////////////////////////////
    // IActionDelegate Implementation
    ///////////////////////////////////////////////////////////////////////////

    public void run(IAction action)
    {
        if (this.window == null)
            this.window = Workbench.getInstance().getActiveWorkbenchWindow();
        
        if (this.window != null)
        {
            IEditorPart editor = this.window.getActivePage().getActiveEditor();
            fEditor = editor instanceof AbstractFortranEditor ? (AbstractFortranEditor)editor : null;
        }
        
        if (this.fEditor != null)
        {
            IProgressService context = PlatformUI.getWorkbench().getProgressService();
            ISchedulingRule lockEntireWorkspace = ResourcesPlugin.getWorkspace().getRoot();
            try
            {
                context.runInUI(context, this, lockEntireWorkspace);
            }
            catch (InvocationTargetException e)
            {
                e.printStackTrace();
                MessageDialog.openError(fEditor.getShell(), "Unhandled Exception", e.getMessage());
            }
            catch (InterruptedException e)
            {
                // Do nothing
            }
        }
    }

    public void selectionChanged(IAction action, ISelection selection) {;}
    
    ///////////////////////////////////////////////////////////////////////////
    // IWorkbenchWindowActionDelegate Implementation
    ///////////////////////////////////////////////////////////////////////////

    public void init(IWorkbenchWindow window)
    {
        this.window = window;
    }
    
    public void dispose() {;}

    ///////////////////////////////////////////////////////////////////////////
    // IEditorActionDelegate Implementation
    ///////////////////////////////////////////////////////////////////////////

    public void setActiveEditor(IAction action, IEditorPart targetEditor)
    {
        fEditor = targetEditor instanceof AbstractFortranEditor ? (AbstractFortranEditor)targetEditor : null;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Utility Methods for Subclasses
    ///////////////////////////////////////////////////////////////////////////

    protected AbstractFortranEditor getFortranEditor()
    {
        return fEditor;
    }
    
    protected Shell getShell()
    {
        return fEditor == null ? null : fEditor.getShell();
    }

    protected File createTempFile() throws IOException
    {
        File tempFile = File.createTempFile("photran-tmp", ".txt");
        tempFile.deleteOnExit();
        return tempFile;
    }

    protected PrintStream createPrintStream(File tempFile) throws FileNotFoundException
    {
        return new PrintStream(new BufferedOutputStream(new FileOutputStream(tempFile)));
    }

    public void openHtmlViewerOn(String title, String str)
    {
        try
        {
            File temp = createTempFile();
            PrintStream text = createPrintStream(temp);
            text.print(str);
            text.close();
            openHtmlViewerOn(title, temp);
        }
        catch (IOException e)
        {
            MessageDialog.openError(getFortranEditor().getShell(),
                                    "Error",
                                    "Unable to create temporary file.\n\n" + e.getMessage());
        }
    }
    
    public void openHtmlViewerOn(final String title, final File file)
    {
        final class HtmlRunnable implements Runnable
        {
            public void run()
            {
                try
                {
                    // This is cheating, but it works
                    //new URLHyperlink(new Region(0, 0), filPath).open();
                    
                    PlatformUI.getWorkbench().getBrowserSupport().createBrowser(0, null, title, null).openURL(file.toURL());
                }
                catch (final Throwable e)
                {
                    Display.getDefault().asyncExec(new Runnable()
                    {
                        public void run()
                        {
                            MessageDialog.openError(getFortranEditor().getShell(),
                                                    "Error",
                                                    "Unable to open Web browser.\n\n" + e.getMessage());
                        }
                    });
                }
            }
        }
    
        Display.getDefault().syncExec(new HtmlRunnable());
    }
}
