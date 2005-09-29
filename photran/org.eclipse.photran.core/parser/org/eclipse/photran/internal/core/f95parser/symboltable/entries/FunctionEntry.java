package org.eclipse.photran.internal.core.f95parser.symboltable.entries;

import org.eclipse.photran.internal.core.f95parser.Nonterminal;
import org.eclipse.photran.internal.core.f95parser.ParseTreeNode;
import org.eclipse.photran.internal.core.f95parser.Token;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTable;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableError;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableVisitor;

/**
 * Symbol table entry for a function
 * 
 * The parameters are stored in the function's child symbol table alongside its
 * local variables.
 * 
 * @author joverbey
 */
public class FunctionEntry extends AbstractSubprogramEntry
{
    public FunctionEntry(SymbolTable parentTable, Token functionIdentifier, ParseTreeNode correspondingParseTreeNode)
    {
        super(parentTable, functionIdentifier, correspondingParseTreeNode);
        
        if (correspondingParseTreeNode.getRootNonterminal() != Nonterminal.XFUNCTIONSUBPROGRAM)
            throw new SymbolTableError("The ParseTreeNode passed to the FunctionEntry constructor should be an xFunctionSubprogram");
    }

    public String getTypeDescription()
    {
        return "Function" + describeInheritedFields();
    }

    public void visitEntryUsing(SymbolTableVisitor visitor)
    {
        visitor.visit(this);
    }
}
