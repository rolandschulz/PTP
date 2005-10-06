package org.eclipse.photran.core.programrepresentation;

import java.util.Iterator;

import org.eclipse.photran.internal.core.f95parser.ParseTreeNode;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTable;

/**
 * Represents a complete program representation: parse tree, <code>Presentation</code> object, and
 * symbol table
 * 
 * @author joverbey
 */
public class Program extends ParseTreePres
{
    private SymbolTable symbolTable;

    public Program(ParseTreeNode parseTree, Presentation presentation, SymbolTable symbolTable)
    {
        super(parseTree, presentation);
        this.symbolTable = symbolTable;
    }

    public void rebuildSymbolTable() throws Exception
    {
        symbolTable = SymbolTable.createSymbolTableFor(parseTree);
    }

    public SymbolTable getSymbolTable()
    {
        return symbolTable;
    }
}
