package org.eclipse.photran.internal.ui.editor_vpg.contentassist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.Intrinsics;
import org.eclipse.photran.internal.core.analysis.binding.Definition.Classification;
import org.eclipse.photran.internal.core.model.FortranElement;
import org.eclipse.swt.graphics.Image;

class FortranCompletionProposalComputer
{
    private HashMap<String, TreeSet<Definition>> defs;
    private String scope;
    private IDocument document;
    private int offset;
    
    private int prefixIndex;
    private String prefix;
    private int suffixIndex;
    private String suffix;
    private int replOffset;
    private int replLen;

    FortranCompletionProposalComputer(HashMap<String, TreeSet<Definition>> defs, String scope, IDocument document, int offset) throws BadLocationException
    {
        this.defs = defs;
        this.scope = scope;
        this.document = document;
        this.offset = offset;

        this.prefixIndex = findPrefix(document, offset);
        this.prefix = document.get(prefixIndex, offset-prefixIndex).toLowerCase();
        
        this.suffixIndex = findSuffix(document, offset);
        this.suffix = document.get(offset, suffixIndex-offset).toLowerCase();
        
        this.replOffset = prefixIndex;
        this.replLen = suffixIndex - prefixIndex;
    }

    private int findPrefix(IDocument s, int offset) throws BadLocationException
    {
        for (offset--; offset >= 0; offset--)
        {
            char c = s.getChar(offset);
            if (!Character.isLetter(c) && !Character.isDigit(c) && c != '_')
                return offset + 1;
        }
        return 0;
    }

    private int findSuffix(IDocument s, int offset) throws BadLocationException
    {
        int length = s.getLength();
        for (; offset < length; offset++)
        {
            char c = s.getChar(offset);
            if (!Character.isLetter(c) && !Character.isDigit(c) && c != '_')
                return offset;
        }
        return length;
    }

    public ICompletionProposal[] compute() throws BadLocationException
    {
        TreeSet<FortranCompletionProposal> proposals1 = new TreeSet<FortranCompletionProposal>();
        addProposalsFromDefs(proposals1);
        
        TreeSet<FortranCompletionProposal> proposals2 = new TreeSet<FortranCompletionProposal>();
        addIntrinsicProposals(proposals2);
        
        CompletionProposal[] result = new CompletionProposal[proposals1.size() + proposals2.size()];
        int i = 0;
        for (FortranCompletionProposal proposal : proposals1)
            result[i++] = proposal.wrappedProposal;
        for (FortranCompletionProposal proposal : proposals2)
            result[i++] = proposal.wrappedProposal;
        return result;
    }

    private void addProposalsFromDefs(TreeSet<FortranCompletionProposal> proposals) throws BadLocationException
    {
        for (;;)
        {
            addProposals(defs.get(scope), proposals);
            
            int colon = scope.indexOf(':');
            if (colon < 0)
                break;
            else
                scope = scope.substring(colon+1);
        }
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

    private void addIntrinsicProposals(TreeSet<FortranCompletionProposal> proposals)
        throws BadLocationException
    {
        for (String canonicalizedId : Intrinsics.intrinsics)
            if (canonicalizedId.startsWith(prefix) && canonicalizedId.endsWith(suffix))
                proposals.add(createProposal(canonicalizedId,
                                             "Intrinsic",
                                             null));
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

    private FortranCompletionProposal createProposal(String canonicalizedId, String description, Image image)
    {
        return new FortranCompletionProposal(
            canonicalizedId,
            new CompletionProposal(canonicalizedId,
                                   replOffset,
                                   replLen,
                                   canonicalizedId.length(),
                                   image,
                                   canonicalizedId + " - " + description,
                                   null,
                                   null));
    }
    
    private static class FortranCompletionProposal implements Comparable<FortranCompletionProposal>
    {
        public final String canonicalizedId;
        public final CompletionProposal wrappedProposal;

        public FortranCompletionProposal(String canonicalizedId, CompletionProposal completionProposal)
        {
            this.canonicalizedId = canonicalizedId;
            this.wrappedProposal = completionProposal;
        }

        public int compareTo(FortranCompletionProposal o)
        {
            return canonicalizedId.compareTo(o.canonicalizedId);
        }

        @Override public boolean equals(Object obj)
        {
            return obj != null
                && obj.getClass().equals(this.getClass())
                && ((FortranCompletionProposal)obj).canonicalizedId.equals(canonicalizedId);
        }

        @Override public int hashCode()
        {
            return canonicalizedId.hashCode();
        }
    }
}
