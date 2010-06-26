/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.actions;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.photran.internal.core.analysis.binding.VariableAccess;
import org.eclipse.photran.internal.core.lexer.IPreprocessorReplacement;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.rephraserengine.ui.UIUtil;

/**
 * The Highlight Variable Accesses action in the Refactoring/(Debugging) menu.
 * 
 * @author Jeff Overbey
 */
public class HighlightVariableAccesses extends FortranEditorASTActionDelegate
{
    public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException
    {
        try
        {
            File temp = File.createTempFile("photran-tmp", ".htm"); //$NON-NLS-1$ //$NON-NLS-2$
            temp.deleteOnExit();
            final PrintStream ps = UIUtil.createPrintStream(temp);
            ps.println("<html><head><title></title></head><body>"); //$NON-NLS-1$

            printLegend(ps);
            
            ps.println("<pre>"); //$NON-NLS-1$
            
            printProgram(ps);

            ps.println("</pre></body></html>"); //$NON-NLS-1$
            ps.close();
            UIUtil.openHtmlViewerOn("Variable Accesses", temp); //$NON-NLS-1$
        }
        catch (Exception e)
        {
            String message = e.getMessage();
            if (message == null) message = e.getClass().getName();
            MessageDialog.openError(getFortranEditor().getShell(), "Error", message); //$NON-NLS-1$
        }
    }

    private void printLegend(final PrintStream ps)
    {
        ps.println("<table border=\"0\" cellpadding=\"5\" cellspacing=\"0\" align=\"center\"><tr>"); //$NON-NLS-1$
        ps.println("<th>LEGEND:</th>"); //$NON-NLS-1$
        for (VariableAccess access : VariableAccess.values())
        {
            ps.println("<td>"); //$NON-NLS-1$
            ps.print(highlightingStartTag(access));
            ps.println(access);
            ps.print(highlightingEndTag(access));
            ps.println("</td>"); //$NON-NLS-1$
        }
        ps.println("</tr></table>"); //$NON-NLS-1$
        ps.println("<hr>"); //$NON-NLS-1$
    }

    private void printProgram(final PrintStream out) throws Exception
    {
        IPreprocessorReplacement currentPreprocessorDirective = null;
        for (Token token : getAST())
        {
            if (token.getPreprocessorDirective() != currentPreprocessorDirective)
            {
                if (token.getPreprocessorDirective() != null)
                {
                    out.print(token.getWhiteBefore());
                    out.print(token.getPreprocessorDirective());
                }
                currentPreprocessorDirective = token.getPreprocessorDirective();
            }
            
            if (currentPreprocessorDirective == null && token.getPreprocessorDirective() == null)
            {
                out.print(token.getWhiteBefore());
                out.print(highlightingStartTag(token.getVariableAccessType()));
                out.print(token.getText());
                out.print(highlightingEndTag(token.getVariableAccessType()));
                out.print(token.getWhiteAfter());
            }
        }
    }
    
    private String highlightingStartTag(VariableAccess access)
    {
        switch (access)
        {
            case NONE:
                return ""; //$NON-NLS-1$
            case READ:
                return "<b><font style=\"background-color: #66FF66;\">"; //$NON-NLS-1$
            case WRITE:
                return "<b><font style=\"background-color: #FF3333;\">"; //$NON-NLS-1$
            case RW:
                return "<b><font style=\"background-color: #FFFF66;\">"; //$NON-NLS-1$
            case IMPLIED_DO:
                return "<b><font style=\"background-color: #CCCCCC;\">"; //$NON-NLS-1$
            case FORALL:
                return "<b><font style=\"background-color: #CCCCCC;\">"; //$NON-NLS-1$
            case STMT_FUNCTION_ARG:
                return "<b><font style=\"background-color: #CCCCCC;\">"; //$NON-NLS-1$
                
            default:
                throw new IllegalStateException();
        }
    }
    
    private String highlightingEndTag(VariableAccess access)
    {
        if (access.equals(VariableAccess.NONE))
            return ""; //$NON-NLS-1$
        else
            return "</font></b>"; //$NON-NLS-1$
    }
}
