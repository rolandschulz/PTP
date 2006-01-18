package org.eclipse.photran.internal.core.f95parser;

import org.eclipse.photran.internal.core.f95parser.symboltable.GenericSymbolTableVisitor;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTable;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.IntrinsicEntry;

/**
 * Methods for finding nodes in a (Fortran) symbol table
 * 
 * @author joverbey
 */
public class SymbolTableSearcher
{
    private static class VisitorEntryNotification extends Error
    {
        private static final long serialVersionUID = 1L;

        private SymbolTableEntry notificationEntry;

        public VisitorEntryNotification(SymbolTableEntry notificationToken)
        {
            this.notificationEntry = notificationToken;
        }

        public SymbolTableEntry getNotificationEntry()
        {
            return notificationEntry;
        }
    }

    public static SymbolTableEntry findEntryCorrespondingToIdentifier(SymbolTable symTbl,
        final Token targetIdentifier)
    {
        if (targetIdentifier.getTerminal() != Terminal.T_IDENT) return null;

        // FIXME-Jeff: Need to check references too, not just declarations!
        // FIXME-Spiros: I am not sure if it also visits the sub-SymbolTables, I think it should, what happens in the case of a subroutine?
        try
        {
            symTbl.visitUsing(new GenericSymbolTableVisitor()
            {
                public void visit(SymbolTableEntry entry)
                {
                    if (!(entry instanceof IntrinsicEntry))
                        if (entry.getIdentifier() == targetIdentifier)
                            throw new VisitorEntryNotification(entry);
                }
            });
        }
        catch (VisitorEntryNotification e)
        {
            return e.getNotificationEntry();
        }

        return null;
    }
}
