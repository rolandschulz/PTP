package org.eclipse.photran.internal.ui.editor_vpg;

import java.util.HashMap;

import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTBlockDataNameNode;
import org.eclipse.photran.internal.core.parser.ASTBlockDataSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTDerivedTypeDefNode;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTInterfaceStmtNode;
import org.eclipse.photran.internal.core.parser.ASTMainProgramNode;
import org.eclipse.photran.internal.core.parser.ASTModuleNode;
import org.eclipse.photran.internal.core.parser.ASTProgramStmtNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTVisitor;
import org.eclipse.photran.internal.core.parser.GenericParseTreeVisitor;
import org.eclipse.photran.internal.core.parser.Parser.InteriorNode;

public final class DefinitionMap
{
    private HashMap<String, Definition> definitions = new HashMap<String, Definition>();
    
    public DefinitionMap(IFortranAST ast)
    {
        ast.visitUsing(new GenericParseTreeVisitor()
        {
            @Override public void visitParseTreeNode(InteriorNode node)
            {
                if (ScopingNode.isScopingNode(node))
                    for (Definition def : ((ScopingNode)node).getAllDefinitions())
                        definitions.put(qualify(def.getTokenRef().findToken()),
                                        def);
            }
        });
        
        for (String def : definitions.keySet())
            System.out.println(def);
    }
    
    public Definition lookup(Token token)
    {
        String qualifiedName = qualify(token);
        while (true)
        {
            System.out.println("Checking " + qualifiedName);
            if (definitions.containsKey(qualifiedName))
                return definitions.get(qualifiedName);
            
            int index = qualifiedName.indexOf(':');
            if (index < 0)
                return null;
            else
                qualifiedName = qualifiedName.substring(index+1);
        }
    }

    private String qualify(Token token)
    {
        StringBuilder result = new StringBuilder();
        
        // Append scopes in *reverse* order
        for (ScopingNode scope = token.getEnclosingScope();
             scope != null && !(scope instanceof ASTExecutableProgramNode);
             scope = scope.getEnclosingScope())
        {
            result.append(getQualifier(scope));
        }
        
        // Then append the identifier
        result.append(token.getText().toLowerCase());
        
        return result.toString();
    }
    
    private String getQualifier(ScopingNode node)
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
    
            @Override public void visitASTInterfaceStmtNode(ASTInterfaceStmtNode node)
            {
                if (node.getGenericName() != null)
                    name = node.getGenericName().getGenericName().getText();
            }
        }
        
        GetScopeVisitor visitor = new GetScopeVisitor();
        node.visitOnlyThisNodeUsing(visitor);
        return node.getNonterminal() + "/" + visitor.name.toLowerCase() + ":";
    }
}
