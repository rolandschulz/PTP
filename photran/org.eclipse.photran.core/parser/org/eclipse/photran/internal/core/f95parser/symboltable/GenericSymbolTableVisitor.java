package org.eclipse.photran.internal.core.f95parser.symboltable;

/**
 * Generic visitor for the Fortran symbol table hierarchy.
 * 
 * This Visitor does <i>not</i> distinguish between types of
 * entries in the symbol table.  Usually the type of each
 * entry is important, and so you should use
 * <code>SymbolTableVisitor</code> instead.
 * 
 * @author joverbey
 */
public class GenericSymbolTableVisitor
{
    public void preparingToVisitTable(SymbolTable table) {;}
    public void doneVisitingTable(SymbolTable table) {;}
    
    public void visit(SymbolTableEntry entry) {;}
}
