/*******************************************************************************
 * Copyright (c) 2011 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;
import org.eclipse.photran.internal.ui.FortranTemplateManager;
import org.eclipse.swt.graphics.Image;

/**
 * A completion processor for Fortran code templates.
 * <p>
 * In addition to overriding the required methods from {@link TemplateCompletionProcessor},
 * this class overrides {@link #computeCompletionProposals(ITextViewer, int)} in order to
 * (1) only suggest templates that start with the prefix entered by the user at
 * the insertion point, and (2) replace tabs and newlines in templates to make the template
 * appear &quot;correctly&quot; indented.
 * 
 * @author Jeff Overbey
 */
public final class FortranTemplateCompletionProcessor extends TemplateCompletionProcessor
{
    @Override
    protected TemplateContextType getContextType(ITextViewer viewer, IRegion region)
    {
        return FortranTemplateManager.getInstance().getContextTypeRegistry()
            .getContextType(FortranTemplateContext.ID);
    }

    @Override
    protected Template[] getTemplates(String contextTypeId)
    {
        return FortranTemplateManager.getInstance().getTemplateStore().getTemplates();
    }

    @Override
    protected Image getImage(Template template)
    {
        return null; // return
                     // FortranUIPlugin.getDefault().getImageRegistry().get(FortranUIPlugin.ICON_TEMPLATE);
    }

    /** @see TemplateCompletionProcessor#computeCompletionProposals(ITextViewer, int) */
    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset)
    {
        ITextSelection selection= (ITextSelection) viewer.getSelectionProvider().getSelection();

        // adjust offset to end of normalized selection
        if (selection.getOffset() == offset)
            offset= selection.getOffset() + selection.getLength();

        String prefix= extractPrefix(viewer, offset);
        Region region= new Region(offset - prefix.length(), prefix.length());
        TemplateContext context= createContext(viewer, region);
        if (context == null)
            return new ICompletionProposal[0];

        context.setVariable("selection", selection.getText()); // name of the selection variables {line, word}_selection //$NON-NLS-1$

        Template[] templates= getTemplates(context.getContextType().getId());
        templates = adjustIndentation(templates, viewer, offset);

        List<ICompletionProposal> matches= new ArrayList<ICompletionProposal>();
        for (int i= 0; i < templates.length; i++) {
            Template template= templates[i];
            try {
                context.getContextType().validate(template.getPattern());
            } catch (TemplateException e) {
                continue;
            }
            if (template.matches(prefix, context.getContextType().getId()) && templatePatternStartsWith(prefix, template))
                matches.add(createProposal(template, context, (IRegion) region, getRelevance(template, prefix)));
        }

        Collections.sort(matches, new Comparator<ICompletionProposal>() {
            public int compare(ICompletionProposal o1, ICompletionProposal o2) {
                return ((TemplateProposal)o2).getRelevance() - ((TemplateProposal)o1).getRelevance();
            }
        });

        return matches.toArray(new ICompletionProposal[matches.size()]);
    }

    private boolean templatePatternStartsWith(String prefix, Template template)
    {
        String pattern = template.getPattern();
        if (pattern.startsWith("$")) // Skip prefix in "${return_type} FUNCTION..." template //$NON-NLS-1$
            return pattern.substring(pattern.indexOf('}')+1).trim().startsWith(prefix);
        else
            return pattern.startsWith(prefix);
    }

    private Template[] adjustIndentation(Template[] templates, ITextViewer viewer, int offset)
    {
        String tabAsSpaces = FortranPreferences.TAB_WIDTH.getStringOfSpaces();
        String currentLineIndentation = determineIndentation(viewer, offset);
        
        Template[] result = new Template[templates.length];
        int i = 0;
        for (Template t : templates)
        {
            result[i++] = new Template(
                t.getName(),
                t.getDescription(),
                t.getContextTypeId(),
                t.getPattern()
                    .replace("\t", tabAsSpaces) //$NON-NLS-1$
                    .replace("\n", "\n" + currentLineIndentation), //$NON-NLS-1$ //$NON-NLS-2$
                t.isAutoInsertable());
        }
        return result;
    }

    private String determineIndentation(ITextViewer viewer, int offset)
    {
        try
        {
            IDocument doc = viewer.getDocument();
            int startOfLine = doc.getLineOffset(doc.getLineOfOffset(offset));
            StringBuilder prefix = new StringBuilder(doc.get(startOfLine, offset-startOfLine));
            while (prefix.length() > 0 && !Character.isWhitespace(prefix.charAt(prefix.length()-1)))
                prefix.deleteCharAt(prefix.length()-1);
            return prefix.toString();
        }
        catch (BadLocationException e)
        {
            return ""; //$NON-NLS-1$
        }
    }
}