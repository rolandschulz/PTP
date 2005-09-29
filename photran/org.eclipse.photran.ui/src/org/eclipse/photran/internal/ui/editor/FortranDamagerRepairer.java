package org.eclipse.photran.internal.ui.editor;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;

/**
 * Defines a damager/repairer to complement the lexer-based syntax highlighting in
 * <code>FortranPartitionScanner</code>.
 * 
 * Since we only have one token in each partition, we can eliminate some of the complexity from the
 * <code>DefaultDamagerRepairer</code>. Also, we eliminate a bug: the default damager repairer
 * only re-colors the file from the changed portion forward; with lexer-based highlighting, tokens
 * above the change may have had their color changed too.
 * 
 * @author joverbey
 */
public final class FortranDamagerRepairer extends DefaultDamagerRepairer
{
    public FortranDamagerRepairer(ITokenScanner scanner)
    {
        super(scanner);
    }

    public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent e,
        boolean documentPartitioningChanged)
    {
        /*
         * This fixes the DefaultDamagerRepairer bug described above. We say that things have been
         * damaged all the way from the start of the file (offset 0), and the whole thing needs to
         * be repainted.
         */
        return new Region(0, partition.getOffset() + partition.getLength());
    }

    public void createPresentation(TextPresentation presentation, ITypedRegion region)
    {
        fScanner.setRange(fDocument, region.getOffset(), region.getLength());
        /*
         * Since there's only one token per partition, we can override DefaultDamagerRepairer's
         * createPresentation with this one, which only calls fScanner.nextToken() once.
         */
        addRange(presentation, region.getOffset(), region.getLength(),
            getTokenTextAttribute(fScanner.nextToken()));
    }
}
