package org.eclipse.photran.internal.core.f95parser.symboltable.entries;

import org.eclipse.photran.internal.core.f95parser.Nonterminal;
import org.eclipse.photran.internal.core.f95parser.ParseTreeNode;
import org.eclipse.photran.internal.core.f95parser.Token;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTable;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableError;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableVisitor;

/**
 * Symbol table entry for block data
 * 
 * @author joverbey
 */
public class BlockDataEntry extends SymbolTableEntry
{
    public BlockDataEntry(SymbolTable parentTable, Token identifier, ParseTreeNode correspondingParseTreeNode)
    {
        super(parentTable, identifier, correspondingParseTreeNode);
        
        if (correspondingParseTreeNode.getRootNonterminal() != Nonterminal.XBLOCKDATASUBPROGRAM)
            throw new SymbolTableError("The ParseTreeNode passed to the BlockDataEntry constructor should be an xBlockDataSubprogram");
    }

    public String getTypeDescription()
    {
        return "Block Data";
    }

    public void visitEntryUsing(SymbolTableVisitor visitor)
    {
        visitor.visit(this);
    }
}
