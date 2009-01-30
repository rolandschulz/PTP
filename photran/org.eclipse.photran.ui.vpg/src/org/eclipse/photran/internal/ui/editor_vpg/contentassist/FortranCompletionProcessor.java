package org.eclipse.photran.internal.ui.editor_vpg.contentassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.Intrinsics;
import org.eclipse.photran.internal.core.properties.SearchPathProperties;
import org.eclipse.photran.internal.ui.editor.AbstractFortranEditor;
import org.eclipse.photran.internal.ui.editor_vpg.FortranEditorTasks;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

public class FortranCompletionProcessor implements IContentAssistProcessor
{
    // Scope map: scopes.get(n) is the qualified name of the scope at line (n+1)
    final ArrayList<String> scopes = new ArrayList<String>();
    
    // Maps qualified scope names to lists of definitions declared in that scope
    final HashMap<String, TreeSet<Definition>> defs = new HashMap<String, TreeSet<Definition>>();
    
    private String errorMessage = null;
    
    public IContentAssistant setup(AbstractFortranEditor editor)
    {
        String contentAssistEnabledProperty = SearchPathProperties.getProperty(
            editor.getIFile(),
            SearchPathProperties.ENABLE_CONTENT_ASSIST_PROPERTY_NAME);
        if (contentAssistEnabledProperty != null && contentAssistEnabledProperty.equals("true"))
        {
            final Color LIGHT_YELLOW = new Color(null, new RGB(255, 255, 191));
            
            FortranEditorTasks.instance(editor).addASTTask(new FortranCompletionProcessorASTTask(this));
            FortranEditorTasks.instance(editor).addVPGTask(new FortranCompletionProcessorVPGTask(this));
            
            ContentAssistant assistant = new ContentAssistant();
            for (String partitionType : AbstractFortranEditor.PARTITION_TYPES)
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
//                                       {
//            new CompletionProposal("Hello", offset, 0, 5),
//            new CompletionProposal("Goodbye", offset, 0, 7)
//                                       };
        
        try
        {
            errorMessage = null;
            
            IDocument document = viewer.getDocument();
            
            int line = document.getLineOfOffset(offset);
            if (line >= scopes.size()) return null;
            
            String scope = scopes.get(line);
            if (scope == null) return null;
            
            return new FortranCompletionProposalComputer(defs, scope, document, offset).compute();
        }
        catch (Exception e)
        {
            errorMessage = e.getClass().getName() + " - " + e.getMessage();
            return null;
        }
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
