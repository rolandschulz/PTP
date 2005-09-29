package org.eclipse.photran.internal.core.f95parser.symboltable;

import org.eclipse.photran.internal.core.f95parser.symboltable.entries.BlockDataEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.CommonBlockEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.DerivedTypeEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.ExternalEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.FunctionEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.InterfaceEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.IntrinsicEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.MainProgramEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.ModuleEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.NamelistEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.SubroutineEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.VariableEntry;

/**
 * Visitor for the Fortran symbol table hierarchy
 * 
 * @author joverbey
 */
public class SymbolTableVisitor
{
    public void preparingToVisitTable(SymbolTable table) {;}
    public void doneVisitingTable(SymbolTable table) {;}
    
    public void visit(MainProgramEntry entry) {;}
    public void visit(ModuleEntry entry) {;}
    public void visit(FunctionEntry entry) {;}
    public void visit(SubroutineEntry entry) {;}
    public void visit(DerivedTypeEntry entry) {;}
    public void visit(BlockDataEntry entry) {;}
    public void visit(NamelistEntry entry) {;}
    public void visit(CommonBlockEntry entry) {;}
    public void visit(InterfaceEntry entry) {;}
    public void visit(ExternalEntry entry) {;}
    public void visit(IntrinsicEntry entry) {;}
    public void visit(VariableEntry entry) {;}
}
