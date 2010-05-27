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
package org.eclipse.photran.internal.ui.editor_vpg.contentassist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.properties.SearchPathProperties;
import org.eclipse.photran.internal.ui.editor.FortranEditor;
import org.eclipse.photran.internal.ui.editor_vpg.FortranEditorTasks;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

public class FortranCompletionProcessor implements IContentAssistProcessor
{
    /** Scope map: scopes.get(n) is the qualified name of the scope at line (n+1) */
    ArrayList<String> scopes = new ArrayList<String>();
    
    /** Maps qualified scope names to lists of definitions declared in that scope */
    HashMap<String, TreeSet<Definition>> defs = new HashMap<String, TreeSet<Definition>>();
    
    private String errorMessage = null;
    
    public IContentAssistant setup(FortranEditor editor)
    {
        String contentAssistEnabledProperty = new SearchPathProperties().getProperty(
            editor.getIFile(),
            SearchPathProperties.ENABLE_CONTENT_ASSIST_PROPERTY_NAME);
        if (contentAssistEnabledProperty != null && contentAssistEnabledProperty.equals("true")) //$NON-NLS-1$
        {
            final Color LIGHT_YELLOW = new Color(null, new RGB(255, 255, 191));
            
            FortranEditorTasks.instance(editor).addASTTask(new FortranCompletionProcessorASTTask(this));
            FortranEditorTasks.instance(editor).addVPGTask(new FortranCompletionProcessorVPGTask(this));
            
            ContentAssistant assistant = new ContentAssistant();
            for (String partitionType : FortranEditor.PARTITION_TYPES)
                assistant.setContentAssistProcessor(this, partitionType);
            assistant.enableAutoActivation(false); //assistant.setAutoActivationDelay(500);
            assistant.setProposalPopupOrientation(IContentAssistant.CONTEXT_INFO_BELOW);
            assistant.setContextInformationPopupBackground(LIGHT_YELLOW);
            assistant.setProposalSelectorBackground(LIGHT_YELLOW);
            return assistant;
        }
        else return null;
    }

    public synchronized ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset)
    {
        if (defs == null) return null;
        
//        return new ICompletionProposal[]
//        {
//            new CompletionProposal("Hello", offset, 0, 5),
//            new CompletionProposal("Goodbye", offset, 0, 7)
//        };
        
        try
        {
            errorMessage = null;
            
            IDocument document = viewer.getDocument();
            
            int line = determineLineNumberForOffset(offset, document);
            String scopeName = determineScopeNameForLine(line);
            
            if (scopeName == null)
                return null;
            else
            {
                return new FortranCompletionProposalComputer(defs, scopeName, document, offset)
                           .compute();
            }
        }
        catch (Exception e)
        {
            errorMessage = e.getClass().getName() + " - " + e.getMessage(); //$NON-NLS-1$
            return null;
        }
    }

    private int determineLineNumberForOffset(int offset, IDocument document) throws BadLocationException
    {
        int line = document.getLineOfOffset(offset);
        
        /*
         * The mapping between scopes and lines is reconfigured only when
         * the editor is reconciled and a new AST is available.  Therefore,
         * if the user adds a line or two at the bottom of the file and
         * invokes content assist before the editor can be reconciled, the
         * line number may be greater than what the last line of the file
         * was the last time we parsed it.  In this case, reset the line
         * to be the last line of the file.
         */
        if (!scopes.isEmpty() && line >= scopes.size())
            line = scopes.size()-1;
        return line;
    }

    private final String determineScopeNameForLine(int line)
    {
        String scopeName = scopes.get(line);
        if (scopeName == null) return null;
        
        /*
         * Again, the mapping between scopes and lines is reconfigured only
         * when the editor is reconciled and a new AST is available.  If
         * the user invokes content assist and the scope is reported to be
         * the outermost scope (""), it's probably because the user has
         * added lines beyond the end-statement of the previous program
         * unit and the editor hasn't been reconciled yet.  (The outermost
         * scope usually contains just a single module or program, so
         * it is rare that a user would type anything below the
         * first program unit, particularly in the outermost scope).
         * Therefore, populate the list of completion proposals based on
         * the *preceding* scope.
         */
        while (scopeName.equals("") && line >= 0) //$NON-NLS-1$
            scopeName = scopes.get(--line);
        return scopeName;
    }

    public IContextInformation[] computeContextInformation(ITextViewer viewer,
                                                           int offset)
    {
        return null;
    }

    public char[] getCompletionProposalAutoActivationCharacters()
    {
        return null;
    }

    public char[] getContextInformationAutoActivationCharacters()
    {
        return null;
    }

    public IContextInformationValidator getContextInformationValidator()
    {
        return null;
    }

    public String getErrorMessage()
    {
        return errorMessage;
    }
}
