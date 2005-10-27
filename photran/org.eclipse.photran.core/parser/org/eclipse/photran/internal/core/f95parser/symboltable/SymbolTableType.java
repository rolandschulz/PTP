package org.eclipse.photran.internal.core.f95parser.symboltable;

import org.eclipse.photran.internal.core.f95parser.GenericParseTreeVisitor;
import org.eclipse.photran.internal.core.f95parser.Nonterminal;
import org.eclipse.photran.internal.core.f95parser.ParseTreeNode;
import org.eclipse.photran.internal.core.f95parser.ParseTreeSearcher;
import org.eclipse.photran.internal.core.f95parser.Terminal;
import org.eclipse.photran.internal.core.f95parser.Token;

/**
 * Describes the various types that appear in a <code>SymbolTable</code>,
 * i.e., the types that variables can have, functions can return, etc.
 * 
 * The only subclasses of this should be inner classes.
 * 
 * @author joverbey
 */
public abstract class SymbolTableType
{
    /**
     * @return a user-readable description of this type
     */
    public abstract String getDescription();
    
    protected ParseTreeNode kindOrCharSelector = null;
    
    protected String describeKindOrCharSelector()
    {
        if (kindOrCharSelector == null) return "";
        
        // Create a name by concatening the text of the child tokens
        //Token name = new Token();
        final StringBuffer sb = new StringBuffer();
        sb.append(", ");
        kindOrCharSelector.visitUsing(new GenericParseTreeVisitor()
            {
	            public void visitToken(Token token)
	        	{
	        		sb.append(token.getText());
	        	}
            });
        return sb.toString();
    }

    public String toString()
    {
        return getDescription();
    }
    
    public static SymbolTableType createFromTypeSpecNode(ParseTreeNode typeSpec)
    {
        if (typeSpec.getRootNonterminal() != Nonterminal.XTYPESPEC)
            throw new SymbolTableError("Non-type-spec node passed to SymbolTableType#createFromTypeSpecNode");
        
        // # R502
        // <xTypeSpec> ::=
        //   T_INTEGER |
        //   T_REAL |
        //   T_DOUBLEPRECISION |
        //   T_COMPLEX |
        //   T_LOGICAL |
        //   T_CHARACTER |
        //   T_INTEGER <xKindSelector> |
        //   T_REAL <xKindSelector> |
        //   T_DOUBLE T_PRECISION |
        //   T_COMPLEX <xKindSelector> |
        //   T_CHARACTER <xCharSelector> |
        //   T_LOGICAL <xKindSelector> |
        //   T_TYPE T_LPAREN <xTypeName> T_RPAREN ;
        
        SymbolTableType type = null;
        
        Token token = ParseTreeSearcher.findFirstTokenIn(typeSpec);
        if (token.getTerminal() == Terminal.T_INTEGER)
            type = SymbolTableType.INTEGER;
        else if (token.getTerminal() == Terminal.T_REAL)
            type = SymbolTableType.REAL;
        else if (token.getTerminal() == Terminal.T_DOUBLEPRECISION
              || token.getTerminal() == Terminal.T_DOUBLE)
            type = SymbolTableType.DOUBLEPRECISION;
        else if (token.getTerminal() == Terminal.T_COMPLEX)
            type = SymbolTableType.COMPLEX;
        else if (token.getTerminal() == Terminal.T_LOGICAL)
            type = SymbolTableType.LOGICAL;
        else if (token.getTerminal() == Terminal.T_CHARACTER)
            type = SymbolTableType.CHARACTER;
        else if (token.getTerminal() == Terminal.T_TYPE)
        {
            ParseTreeNode typeName = ParseTreeSearcher.findFirstNodeIn(typeSpec, Nonterminal.XTYPENAME);
            Token typeToken = ParseTreeSearcher.findFirstIdentifierIn(typeName);
            type = SymbolTableType.getDerivedType(typeToken);
        }
        
        ParseTreeNode kindSelector = ParseTreeSearcher.findFirstNodeIn(typeSpec, Nonterminal.XKINDSELECTOR);
        ParseTreeNode charSelector = ParseTreeSearcher.findFirstNodeIn(typeSpec, Nonterminal.XCHARSELECTOR);
        if (kindSelector != null)
            type.kindOrCharSelector = kindSelector;
        if (charSelector != null)
            type.kindOrCharSelector = charSelector;
        
        return type;
    }

    public static SymbolTableType INTEGER = new SymbolTableType()
    {
        public String getDescription()
        {
            return "integer" + describeKindOrCharSelector();
        }
    };

    public static SymbolTableType REAL = new SymbolTableType()
    {
        public String getDescription()
        {
            return "real" + describeKindOrCharSelector();
        }
    };

    public static SymbolTableType DOUBLEPRECISION = new SymbolTableType()
    {
        public String getDescription()
        {
            return "double precision" + describeKindOrCharSelector();
        }
    };

    public static SymbolTableType COMPLEX = new SymbolTableType()
    {
        public String getDescription()
        {
            return "complex" + describeKindOrCharSelector();
        }
    };

    public static SymbolTableType LOGICAL = new SymbolTableType()
    {
        public String getDescription()
        {
            return "logical" + describeKindOrCharSelector();
        }
    };

    public static SymbolTableType CHARACTER = new SymbolTableType()
    {
        public String getDescription()
        {
            return "character" + describeKindOrCharSelector();
        }
    };

    public static SymbolTableType getDerivedType(final Token description)
    {
        return new SymbolTableType()
        {
	        public String getDescription()
	        {
	            return "type(" + description.getText() + ")"
	            	+ describeKindOrCharSelector();
	            // Yeah, I know it *shouldn't* have a kind or char selector...
	        }
        };
    };
}
