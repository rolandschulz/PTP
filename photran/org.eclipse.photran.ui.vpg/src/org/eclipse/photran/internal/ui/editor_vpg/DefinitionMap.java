package org.eclipse.photran.internal.ui.editor_vpg;

import java.util.HashMap;

import org.eclipse.jface.text.TextSelection;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.core.vpg.PhotranVPGBuilder;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.Intrinsics;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.lexer.TokenList;
import org.eclipse.photran.internal.core.parser.ASTBlockDataNameNode;
import org.eclipse.photran.internal.core.parser.ASTBlockDataSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTDerivedTypeDefNode;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTGenericNameNode;
import org.eclipse.photran.internal.core.parser.ASTInterfaceBlockNode;
import org.eclipse.photran.internal.core.parser.ASTMainProgramNode;
import org.eclipse.photran.internal.core.parser.ASTModuleNode;
import org.eclipse.photran.internal.core.parser.ASTProgramStmtNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineSubprogramNode;
import org.eclipse.photran.internal.core.parser.Parser.ASTVisitor;
import org.eclipse.photran.internal.core.parser.Parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;

public abstract class DefinitionMap<T>
{
    private HashMap<String, T> definitions = new HashMap<String, T>();
    
    public DefinitionMap(IFortranAST ast)
    {
        this(ast.getRoot());
    }

    public DefinitionMap(ASTExecutableProgramNode ast)
    {
        if (PhotranVPGBuilder.isEmpty(ast)) return;
        
        try
        {
            ast.accept(new GenericASTVisitor()
            {
                @Override public void visitASTNode(IASTNode node)
                {
                    if (ScopingNode.isScopingNode(node))
                        for (Definition def : ((ScopingNode)node).getAllDefinitions())
                        {
                            if (def != null)
                            {
                                String name = def.getCanonicalizedName();
                                String qualifiedName = qualify(name, (ScopingNode)node);
                                definitions.put(qualifiedName, map(qualifiedName, def));
                            }
                        }
                    
                    traverseChildren(node);
                }
            });
        }
        catch (IllegalArgumentException e)
        {
            // Ignore
            // TODO: Caused by PhotranTokenRef getting passed "null" for the filename;
            //       why does that happen?
        }
        
//        for (String def : definitions.keySet())
//            System.out.println(def);
    }

    public DefinitionMap(DefinitionMap<Definition> other)
    {
        for (String key : other.definitions.keySet())
            definitions.put(key, map(key, other.definitions.get(key)));
    }

    
    protected abstract T map(String qualifiedName, Definition def);

    public T lookup(TextSelection selection, TokenList tokenList)
    {
        return lookup(findTokenEnclosing(selection, tokenList));
    }

    public static Token findTokenEnclosing(TextSelection sel, TokenList tokenList)
    {
        return findTokenEnclosing(sel.getOffset(), tokenList);
    }

    public static Token findTokenEnclosing(int offset, TokenList tokenList)
    {
        for (int i = 0, size = tokenList.size(); i < size; i++)
            if (tokenList.get(i).containsFileOffset(offset))
                return tokenList.get(i);
        return null;
    }

    public T lookup(Token token)
    {
        if (token == null || token.getTerminal() != Terminal.T_IDENT) return null;
        
        String qualifiedName = qualify(PhotranVPG.canonicalizeIdentifier(token.getText()), token.getEnclosingScope());
        while (true)
        {
//            System.out.println("Checking " + qualifiedName);
            if (definitions.containsKey(qualifiedName))
                return definitions.get(qualifiedName);
            
            int index = qualifiedName.indexOf(':');
            if (index < 0)
            {
                Definition intrinsic = Intrinsics.resolveIntrinsic(token);
                if (intrinsic != null)
                    return map(intrinsic.getCanonicalizedName(), intrinsic);
                else
                    return null;
            }
            else
                qualifiedName = qualifiedName.substring(index+1);
        }
    }

    public static String qualify(String canonicalizedName, ScopingNode initialScope)
    {
        StringBuilder result = new StringBuilder();
        
        // Append scopes in *reverse* order
        getQualifier(initialScope, result);
        
        // Then append the identifier
        result.append(canonicalizedName);
        
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
        node.accept(visitor);
        return node.getClass().getName() + "/" + visitor.name.toLowerCase() + ":";
    }
}
