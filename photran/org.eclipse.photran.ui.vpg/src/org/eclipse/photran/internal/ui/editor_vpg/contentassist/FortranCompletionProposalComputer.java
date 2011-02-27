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
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.Definition.Classification;
import org.eclipse.photran.internal.core.intrinsics.IntrinsicProcDescription;
import org.eclipse.photran.internal.core.intrinsics.Intrinsics;
import org.eclipse.photran.internal.core.model.FortranElement;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.ui.FortranTemplateManager;
import org.eclipse.photran.internal.ui.editor.CompletionComputer;
import org.eclipse.photran.internal.ui.editor.FortranTemplateContext;
import org.eclipse.swt.graphics.Image;

/**
 * Computes the list of items be shown in the content assist list.
 * 
 * @author Jeff Overbey
 * 
 * @see FortranCompletionProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
 */
class FortranCompletionProposalComputer extends CompletionComputer
{
    private HashMap<String, TreeSet<Definition>> defs;
    private String scope;

    FortranCompletionProposalComputer(HashMap<String, TreeSet<Definition>> defs, String scope, IDocument document, int offset) throws BadLocationException
    {
        super(document, offset);
        this.defs = defs;
        this.scope = scope;
    }

    public List<ICompletionProposal> proposalsFromDefs() throws BadLocationException
    {
        TreeSet<FortranCompletionProposal> proposals = new TreeSet<FortranCompletionProposal>();
        for (;;)
        {
            addProposals(defs.get(scope), proposals);
            
            int colon = scope.indexOf(':');
            if (colon < 0)
                break;
            else
                scope = scope.substring(colon+1);
        }
        return toProposalArray(proposals);
    }

    private List<ICompletionProposal> toProposalArray(TreeSet<FortranCompletionProposal> proposals)
    {
        List<ICompletionProposal> result = new ArrayList<ICompletionProposal>(proposals.size());
        for (FortranCompletionProposal p : proposals)
            result.add(p.wrappedProposal);
        return result;
    }

    private void addProposals(Iterable<Definition> proposalsToConsider,
                              TreeSet<FortranCompletionProposal> proposals)
        throws BadLocationException
    {
        if (proposalsToConsider != null)
        {
            for (Definition def : proposalsToConsider)
            {
                if (def.getClassification().equals(Classification.MAIN_PROGRAM))
                    continue;
                
                String identifier = def.getDeclaredName();
                String canonicalizedId = def.getCanonicalizedName();
                if (canonicalizedId.startsWith(prefix) && canonicalizedId.endsWith(suffix))
                    proposals.add(createProposal(identifier,
                                                 def.describeClassification(),
                                                 getImage(def.getClassification())));
            }
        }
    }

    public List<ICompletionProposal> proposalsFromIntrinsics() throws BadLocationException
    {
        TreeSet<FortranCompletionProposal> proposals = new TreeSet<FortranCompletionProposal>();
        for (IntrinsicProcDescription proc : Intrinsics.getAllIntrinsicProcedures())
        {
            String canonicalizedId = PhotranVPG.canonicalizeIdentifier(proc.genericName);
            if (canonicalizedId.startsWith(prefix) && canonicalizedId.endsWith(suffix))
            {
                //proposals.add(createProposal(proc.genericName.toLowerCase(), proc.description));

                for (String proposal : proc.getAllForms())
                {
                    proposals.add(createProposal(proposal.toLowerCase(), proc.description));
                }
            }
        }
        return toProposalArray(proposals);
    }

    private HashMap<Classification, Image> imageCache = new HashMap<Classification, Image>(); 
    
    private Image getImage(Classification classification)
    {
        if (!imageCache.containsKey(classification))
            imageCache.put(classification, getImageDescriptor(classification).createImage());

        return imageCache.get(classification);
    }

    private ImageDescriptor getImageDescriptor(Classification classification)
    {
        switch (classification)
        {
        case VARIABLE_DECLARATION:
        case IMPLICIT_LOCAL_VARIABLE:
        case DERIVED_TYPE_COMPONENT:
            return FortranElement.Variable.imageDescriptor();
        
        case BLOCK_DATA:
            return FortranElement.BlockData.imageDescriptor();
        
        case DERIVED_TYPE:
            return FortranElement.DerivedType.imageDescriptor();
            
        case FUNCTION:
            return FortranElement.Function.imageDescriptor();
            
        case INTERFACE:
            return FortranElement.Interface.imageDescriptor();
            
        case MAIN_PROGRAM:
            return FortranElement.MainProgram.imageDescriptor();
            
        case MODULE:
            return FortranElement.Module.imageDescriptor();
        
        case SUBROUTINE:
            return FortranElement.Subroutine.imageDescriptor();
            
        default:
            return FortranElement.unknownImageDescriptor();
        }
    }

//    private FortranCompletionProposal createProposal(String identifier)
//    {
//        return createProposal(identifier, null, null);
//    }

    private FortranCompletionProposal createProposal(String identifier, String description)
    {
        return createProposal(identifier, description, null);
    }

    private FortranCompletionProposal createProposal(String identifier, String description, Image image)
    {
//        return new FortranCompletionProposal(
//            identifier,
//            new CompletionProposal(identifier,
//                                   replOffset,
//                                   replLen,
//                                   identifier.length(),
//                                   image,
//                                   displayString(identifier, description),
//                                   null,
//                                   null));
        return new FortranCompletionProposal(
            identifier,
            new TemplateProposal(new Template(
                                    identifier,
                                    description == null ? "" : description, //$NON-NLS-1$
                                    FortranTemplateContext.ID,
                                    replaceArgumentsWithTemplateVariables(identifier),
                                    true),
                                 new DocumentTemplateContext(
                                     FortranTemplateManager.getInstance().getContextTypeRegistry().getContextType(FortranTemplateContext.ID),
                                     document,
                                     replOffset,
                                     replLen),
                                 new Region(replOffset, replLen),
                                 image,
                                 100));
    }

    /**
     * Converts "len(string, kind)" into "len(${string}, ${kind})", for example
     */
    private String replaceArgumentsWithTemplateVariables(String string)
    {
        if (string.contains("(") && string.contains(")") && string.endsWith(")")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        {
            int lparen = string.indexOf('(');
            int rparen = string.lastIndexOf(')'); // == string.length()-1
            String name = string.substring(0, lparen);
            String[] args = string.substring(lparen+1, rparen).split(","); //$NON-NLS-1$
            
            StringBuilder sb = new StringBuilder(string.length() + 16);
            sb.append(name);
            sb.append('(');
            for (int i = 0; i < args.length; i++)
            {
                if (i > 0) sb.append(", "); //$NON-NLS-1$
                sb.append("${"); //$NON-NLS-1$
                sb.append(args[i].trim().replace(' ', '_'));
                sb.append('}');
            }
            sb.append(')');
            return sb.toString();
        }
        else return string;
    }

//    private String displayString(String identifier, String description)
//    {
//        if (description == null)
//            return identifier;
//        else
//            return identifier + " - " + description; //$NON-NLS-1$
//    }
    
    /**
     * A single proposal which will appear in the content assist list.
     * <p>
     * This class implements {@link Comparable} so that instances can be stored in a
     * {@link TreeMap}.  This ensures that the resulting list will be sorted alphabetically.
     * 
     * @author Jeff Overbey
     */
    private static class FortranCompletionProposal implements Comparable<FortranCompletionProposal>
    {
        public final String canonicalizedId;
        public final ICompletionProposal wrappedProposal;

        public FortranCompletionProposal(String identifier, ICompletionProposal proposal)
        {
            this.canonicalizedId = PhotranVPG.canonicalizeIdentifier(identifier);
            this.wrappedProposal = proposal;
        }

        public int compareTo(FortranCompletionProposal o)
        {
            return canonicalizedId.compareTo(o.canonicalizedId);
        }

        @Override public boolean equals(Object obj)
        {
            return obj != null
                && obj.getClass().equals(this.getClass())
                && ((FortranCompletionProposal)obj).canonicalizedId.equals(this.canonicalizedId);
        }

        @Override public int hashCode()
        {
            return canonicalizedId.hashCode();
        }
    }
}
