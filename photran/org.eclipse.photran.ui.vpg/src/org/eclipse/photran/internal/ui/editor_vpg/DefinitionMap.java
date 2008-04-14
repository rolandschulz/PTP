package org.eclipse.photran.internal.ui.editor_vpg;

import java.util.HashMap;

import org.eclipse.jface.text.TextSelection;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.Intrinsics;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTBlockDataNameNode;
import org.eclipse.photran.internal.core.parser.ASTBlockDataSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTDerivedTypeDefNode;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTGenericNameNode;
import org.eclipse.photran.internal.core.parser.ASTInterfaceBlockNode;
import org.eclipse.photran.internal.core.parser.ASTInterfaceStmtNode;
import org.eclipse.photran.internal.core.parser.ASTMainProgramNode;
import org.eclipse.photran.internal.core.parser.ASTModuleNode;
import org.eclipse.photran.internal.core.parser.ASTProgramStmtNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTVisitor;
import org.eclipse.photran.internal.core.parser.GenericParseTreeVisitor;
import org.eclipse.photran.internal.core.parser.Parser.InteriorNode;
import org.eclipse.photran.internal.core.parser.Parser.Nonterminal;

public abstract class DefinitionMap<T>
{
    private HashMap<String, T> definitions = new HashMap<String, T>();
    
    public DefinitionMap(IFortranAST ast)
    {
        ast.visitUsing(new GenericParseTreeVisitor()
        {
            @Override public void visitParseTreeNode(InteriorNode node)
            {
                if (ScopingNode.isScopingNode(node))
                    for (Definition def : ((ScopingNode)node).getAllDefinitions())
                        // Qualify definitions imported from modules in the *importing* scope
                        definitions.put(qualify(def.getTokenRef().findToken(), (ScopingNode)node),
                                        map(def));
            }
        });
        
        for (String def : definitions.keySet())
            System.out.println(def);
    }
    
    protected abstract T map(Definition def);

    public T lookup(TextSelection selection, IFortranAST ast)
    {
        return lookup(findTokenEnclosing(selection, ast));
    }

    public static Token findTokenEnclosing(TextSelection sel, IFortranAST ast)
    {
        return findTokenEnclosing(sel.getOffset(), ast);
    }

    public static Token findTokenEnclosing(int offset, IFortranAST ast)
    {
        for (Token t : ast)
            if (t.containsFileOffset(offset))
                return t;
        return null;
    }

    public T lookup(Token token)
    {
        if (token == null || token.getTerminal() != Terminal.T_IDENT) return null;
        
        String qualifiedName = qualify(token, token.getEnclosingScope());
        while (true)
        {
            System.out.println("Checking " + qualifiedName);
            if (definitions.containsKey(qualifiedName))
                return definitions.get(qualifiedName);
            
            int index = qualifiedName.indexOf(':');
            if (index < 0)
            {
                Definition intrinsic = Intrinsics.resolveIntrinsic(token);
                if (intrinsic != null)
                    return map(intrinsic);
                else
                    return null;
            }
            else
                qualifiedName = qualifiedName.substring(index+1);
        }
    }

    public static String qualify(Token token, ScopingNode initialScope)
    {
        StringBuilder result = new StringBuilder();
        
        // Append scopes in *reverse* order
        getQualifier(initialScope, result);
        
        // Then append the identifier
        result.append(token.getText().toLowerCase());
        
        return result.toString();
    }

    private static void getQualifier(ScopingNode initialScope,
                                     StringBuilder result)
    {
        // Append scopes in *reverse* order
        for (ScopingNode scope = initialScope;
             scope != null && !(scope instanceof ASTExecutableProgramNode);
             scope = scope.getEnclosingScope())
        {
            result.append(getQualifierElement(scope));
        }
    }

    public static String getQualifier(ScopingNode scope)
    {
        StringBuilder result = new StringBuilder();
        getQualifier(scope, result);
        return result.toString();
    }

    private static String getQualifierElement(ScopingNode node)
    {
        class GetScopeVisitor extends ASTVisitor
        {
            private String name = "";
            
            @Override public void visitASTMainProgramNode(ASTMainProgramNode node)
            {
                ASTProgramStmtNode ps = node.getProgramStmt();
                if (ps != null && ps.getProgramName() != null)
                    name = ps.getProgramName().getProgramName().getText();
            }
    
            @Override public void visitASTFunctionSubprogramNode(ASTFunctionSubprogramNode node)
            {
                name = node.getFunctionStmt().getFunctionName().getFunctionName().getText();
            }
    
            @Override public void visitASTSubroutineSubprogramNode(ASTSubroutineSubprogramNode node)
            {
                name = node.getSubroutineStmt().getSubroutineName().getSubroutineName().getText();
            }
    
            @Override public void visitASTModuleNode(ASTModuleNode node)
            {
                name = node.getModuleStmt().getModuleName().getModuleName().getText();
            }
    
            @Override public void visitASTBlockDataSubprogramNode(ASTBlockDataSubprogramNode node)
            {
                ASTBlockDataNameNode name = node.getBlockDataStmt().getBlockDataName();
                if (name != null)
                    this.name = name.getBlockDataName().getText();
            }
    
            @Override public void visitASTDerivedTypeDefNode(ASTDerivedTypeDefNode node)
            {
                name = node.getDerivedTypeStmt().getTypeName().getText();
            }
    
            @Override public void visitASTInterfaceBlockNode(ASTInterfaceBlockNode node)
            {
                ASTGenericNameNode nm = node.getInterfaceStmt().getGenericName();
                if (nm != null)
                    name = nm.getGenericName().getText();
            }
        }
        
        GetScopeVisitor visitor = new GetScopeVisitor();
        if (node.getNonterminal() == Nonterminal.INTERFACE_BLOCK)
            System.out.println("!");
        node.visitOnlyThisNodeUsing(visitor);
        return node.getNonterminal() + "/" + visitor.name.toLowerCase() + ":";
    }
}
