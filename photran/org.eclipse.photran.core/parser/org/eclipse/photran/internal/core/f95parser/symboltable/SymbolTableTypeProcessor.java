package org.eclipse.photran.internal.core.f95parser.symboltable;

public abstract class SymbolTableTypeProcessor
{
	public Object ifInteger(SymbolTableType type) {return null;}
	public Object ifReal(SymbolTableType type) {return null;}
	public Object ifDoublePrecision(SymbolTableType type) {return null;}
	public Object ifComplex(SymbolTableType type) {return null;}
	public Object ifLogical(SymbolTableType type) {return null;}
	public Object ifCharacter(SymbolTableType type) {return null;}
	public Object ifDerivedType(String derivedTypeName, SymbolTableType type) {return null;}
}
