package org.eclipse.photran.internal.core.f95parser.symboltable.entries;

import org.eclipse.photran.internal.core.f95parser.GenericParseTreeVisitor;
import org.eclipse.photran.internal.core.f95parser.Nonterminal;
import org.eclipse.photran.internal.core.f95parser.ParseTreeNode;
import org.eclipse.photran.internal.core.f95parser.Token;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTable;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableError;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableVisitor;

/**
 * Symbol table entry for interfaces
 * 
 * NOTE: The identifier token may not actually exist in the program, since
 * interfaces do not have to be named
 * 
 * @author joverbey
 */
public class InterfaceEntry extends SymbolTableEntry
{
    public InterfaceEntry(SymbolTable parentTable, Token identifier, ParseTreeNode correspondingParseTreeNode)
    {
        super(parentTable, identifier, correspondingParseTreeNode);
        
        if (correspondingParseTreeNode.getRootNonterminal() != Nonterminal.XINTERFACEBLOCK)
            throw new SymbolTableError("The ParseTreeNode passed to the InterfaceEntry constructor should be an xInterfaceBlock");
    }

    public InterfaceEntry(SymbolTable parentTable, ParseTreeNode genericSpec, ParseTreeNode correspondingParseTreeNode)
    {
        this(parentTable, createDescriptiveTokenFromGenericSpec(genericSpec), correspondingParseTreeNode);
    }

    private static Token createDescriptiveTokenFromGenericSpec(ParseTreeNode genericSpec)
    {
        // # R1207
        // <xGenericSpec> ::=
        //   T_OPERATOR T_LPAREN <xDefinedOperator> T_RPAREN |
        //   T_ASSIGNMENT T_LPAREN T_EQUALS T_RPAREN ;

        if (genericSpec.getRootNonterminal() != Nonterminal.XGENERICSPEC)
            throw new SymbolTableError("Passed non-generic-spec parse tree node to InterfaceEntry constructor!");
        
        // Create a name by concatening the text of the child tokens
        Token name = new Token();
        final StringBuffer sb = new StringBuffer();
        genericSpec.visitUsing(new GenericParseTreeVisitor()
            {
	            public void visitToken(Token token)
	        	{
	        		sb.append(token.getText());
	        	}
            });
        name.setText(sb.toString());
        
        return name;
    }

    public String getTypeDescription()
    {
        return "Interface";
    }

    public void visitEntryUsing(SymbolTableVisitor visitor)
    {
        visitor.visit(this);
    }
}
