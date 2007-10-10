package org.eclipse.photran.internal.ui.editor_vpg;

import java.util.HashSet;
import java.util.LinkedList;

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
import org.eclipse.photran.internal.core.parser.Parser.Nonterminal;

public final class DefinitionMap
{
    private static class FullyQualifiedIdentifier
    {
        private LinkedList<Qualifier> qualifiers = new LinkedList<Qualifier>();
        private String canonicalizedName;
        
        FullyQualifiedIdentifier(Definition def)
        {
            canonicalizedName = def.getCanonicalizedName();
            
            AddQualifiers v = new AddQualifiers(qualifiers);
            for (InteriorNode n = def.getTokenRef().findToken().getASTParent();
                 n != null;
                 n = n.getASTParent())
            {
                if (ScopingNode.isScopingNode(n))
                {
                    n.visitOnlyThisNodeUsing(v);
                }
            }
        }
    }
    
    private static class Qualifier
    {
        String canonicalizedName;
        Nonterminal type;
        
        public Qualifier(String canonicalizedName, Nonterminal type)
        {
            if (canonicalizedName == null)
                this.canonicalizedName = "";
            else
                this.canonicalizedName = canonicalizedName;
            this.type = type;
        }
    }
    
    private static class AddQualifiers extends ASTVisitor
    {
        private LinkedList<Qualifier> qualifiers;
        
        AddQualifiers(LinkedList<Qualifier> qualifiers)
        {
            this.qualifiers = qualifiers;
        }
        
        private void addQualifier(String name, InteriorNode node)
        {
            qualifiers.add(0, new Qualifier(name, node.getNonterminal()));
        }

        private void addQualifier(Token name, InteriorNode node)
        {
            qualifiers.add(0, new Qualifier(name.getText(), node.getNonterminal()));
        }

        @Override public void visitASTExecutableProgramNode(ASTExecutableProgramNode node)
        {
            addQualifier("", node);
        }

        @Override public void visitASTMainProgramNode(ASTMainProgramNode node)
        {
            ASTProgramStmtNode ps = node.getProgramStmt();
            if (ps != null && ps.getProgramName() != null)
                addQualifier(ps.getProgramName().getTIdent(), node);
            else
                addQualifier("", node);
        }

        @Override public void visitASTFunctionSubprogramNode(ASTFunctionSubprogramNode node)
        {
            addQualifier(node.getFunctionStmt().getFunctionName().getTIdent(), node);
        }

        @Override public void visitASTSubroutineSubprogramNode(ASTSubroutineSubprogramNode node)
        {
            addQualifier(node.getSubroutineStmt().getSubroutineName().getTIdent(), node);
        }

        @Override public void visitASTModuleNode(ASTModuleNode node)
        {
            addQualifier(node.getModuleStmt().getModuleName().getTIdent(), node);
        }

        @Override public void visitASTBlockDataSubprogramNode(ASTBlockDataSubprogramNode node)
        {
            ASTBlockDataNameNode name = node.getBlockDataStmt().getBlockDataName();
            if (name == null)
                addQualifier("", node);
            else
                addQualifier(name.getTIdent(), node);
        }

        @Override public void visitASTDerivedTypeDefNode(ASTDerivedTypeDefNode node)
        {
            addQualifier(node.getDerivedTypeStmt().getTypeName().getTIdent(), node);
        }

        @Override public void visitASTInterfaceStmtNode(ASTInterfaceStmtNode node)
        {
            if (node.getGenericName() != null)
                addQualifier(node.getGenericName().getTIdent(), node);
        }
    }

    private HashSet<FullyQualifiedIdentifier> definitions = new HashSet<FullyQualifiedIdentifier>();
    
    public DefinitionMap(IFortranAST ast)
    {
        ast.visitUsing(new GenericParseTreeVisitor()
        {
            @Override public void visitParseTreeNode(InteriorNode node)
            {
                if (ScopingNode.isScopingNode(node))
                    for (Definition def : ((ScopingNode)node).getAllDefinitions())
                        definitions.add(new FullyQualifiedIdentifier(def));
            }
        });
    }
}
