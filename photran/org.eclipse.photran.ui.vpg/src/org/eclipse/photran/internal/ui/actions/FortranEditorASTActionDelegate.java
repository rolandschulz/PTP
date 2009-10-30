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
import java.util.Collection;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.core.vpg.util.IterableWrapper;
import org.eclipse.photran.core.vpg.util.OffsetLength;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.ui.editor.AbstractFortranEditor;
import org.eclipse.rephraserengine.core.vpg.eclipse.VPGSchedulingRule;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.progress.IProgressService;

/**
 * Base class providing utility methods to actions contributed to the Fortran editor.
 * 
 * @author Jeff Overbey
 */
public abstract class FortranEditorASTActionDelegate extends FortranEditorActionDelegate
{
    ///////////////////////////////////////////////////////////////////////////
    // IActionDelegate Implementation
    ///////////////////////////////////////////////////////////////////////////

	// Copied from FortranEditorActionDelegate, but has VPG scheduling rule
	
    public final void run(IAction action)
    {
        if (this.window != null)
        {
            IEditorPart editor = this.window.getActivePage().getActiveEditor();
            fEditor = editor instanceof AbstractFortranEditor ? (AbstractFortranEditor)editor : null;
        }
        
        if (this.fEditor != null)
        {
            IProgressService context = PlatformUI.getWorkbench().getProgressService();
            
            ISchedulingRule lockEntireWorkspace = ResourcesPlugin.getWorkspace().getRoot();
            ISchedulingRule vpgSched = VPGSchedulingRule.getInstance();
            ISchedulingRule schedulingRule = MultiRule.combine(lockEntireWorkspace, vpgSched);
            
            try
            {
                context.runInUI(context, this, schedulingRule);
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

    ///////////////////////////////////////////////////////////////////////////
    // Utility Methods for Subclasses
    ///////////////////////////////////////////////////////////////////////////

    protected IFortranAST getAST() throws Exception
    {
        IFortranAST ast = PhotranVPG.getInstance().acquireTransientAST(getFortranEditor().getIFile());
//                                                                   new IncludeLoaderCallback(editor.getIFile().getProject()),
//                                                                   ModuleLoaderCallback.IGNORE,
//                                                                   editor.isFixedForm(),
//                                                                   progressMonitor);
        if (ast == null) throw new Exception("Unable to parse file in editor");
        return ast;
    }

    protected Token findEnclosingToken(IFortranAST ast, ITextSelection textSelection)
    {
        for (Token token : new IterableWrapper<Token>(ast))
            if (token.containsFileOffset(new OffsetLength(textSelection.getOffset(), textSelection.getLength())))
                return token;
        return null;
    }

    protected Definition openSelectionDialog(Collection<Definition> defs)
    {
        ElementListSelectionDialog dlg = new ElementListSelectionDialog(getShell(), new LabelProvider()
        {
            @Override public String getText(Object element)
            {
                Definition def = (Definition)element;
                return describeDeclaration(def);
            }
        });
        dlg.setBlockOnOpen(true);
        dlg.setHelpAvailable(false);
        dlg.setIgnoreCase(true);
        dlg.setMessage("Select a declaration to open (? = any character, * = any string):");
        dlg.setMultipleSelection(false);
        dlg.setSize(100, 10);
        dlg.setTitle("Multiple Declarations Found");
        dlg.setElements(defs.toArray());
        dlg.open();
        if (dlg.getResult() != null)
            return (Definition)dlg.getResult()[0];
        else
            return null;
    }

	protected String describeDeclaration(Definition def)
	{
		try
        {
			final IDocument document = getFortranEditor().getDocumentProvider().getDocument(getFortranEditor().getEditorInput());
			
			return def.getCanonicalizedName()
				+ " - "
				+ def.describeClassification()
				+ (def.isSubprogramArgument() ? " (Subprogram Argument)" : "")
				+ " - Line "
				+ (document.getLineOfOffset(def.getTokenRef().findToken().getFileOffset()) + 1)
				+ " - "
				+ def.getTokenRef().getFilename();
		}
        catch (Throwable e)
        {
			return def.toString();
		}
	}
}
