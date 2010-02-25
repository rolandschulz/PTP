import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.Util;
import org.eclipse.photran.cmdline.CmdLineBase;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTAssignmentStmtNode;
import org.eclipse.photran.internal.core.parser.IExecutionPartConstruct;
import org.eclipse.photran.internal.core.parser.Parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;

public class EvenMoreDefs extends CmdLineBase
{
    public static void main(String[] args)
    {
        String filename = parseCommandLine("evenmoredefs", args);

        Util.DISPLAY_WARNINGS = false;
        PhotranVPG.getInstance().start();
        IFortranAST ast = parse(filename);

        System.out.println(ast.getRoot());
        displayCounts(ast);
        
        PhotranVPG.getDatabase().close();
    }

    protected static void displayCounts(IFortranAST ast)
    {
        CountingVisitor v = new CountingVisitor();
        ast.accept(v);
        
        for (Definition def : v.writes.keySet())
            System.out.println(
                def.getCanonicalizedName() + " is written " + v.writes.get(def) + " time(s)");
        
        for (Definition def : v.reads.keySet())
            System.out.println(
                def.getCanonicalizedName() + " is read " + v.reads.get(def) + " time(s)");
    }

    /*
     * This isn't complete, but it illustrates the basic idea.
     * 
     * We increment the number of writes when we see the variable on the left-hand side (LHS) of an
     * assignment statement.
     * 
     * Since there are lots of ways you can "read" a variable, I am sort of cheating; every time
     * I see a token, then I count it as a "read" of a variable if
     * (1) the token is an identifier, and we can find its declaration;
     * (2) it is in an executable statement (not a specification statement); and
     * (3) it is NOT on the LHS of an assignment statement.
     */
    private static class CountingVisitor extends GenericASTVisitor
    {
        private HashMap<Definition, Integer> reads = new HashMap<Definition, Integer>();
        private HashMap<Definition, Integer> writes = new HashMap<Definition, Integer>();
        
        @Override public void visitASTAssignmentStmtNode(ASTAssignmentStmtNode node)
        {
            Token lhs = node.getLhsVariable().getName();
            Definition def = resolveBinding(lhs);
            increment(writes, def);
        }
        
        @Override public void visitToken(Token node)
        {
            if (isIdentifier(node) && isInExecutableStmt(node))
            {
                if (!isLHSofAssignmentStmt(node))
                {
                    Definition def = resolveBinding(node);
                    if (def != null && (def.isLocalVariable() || def.isSubprogramArgument()))
                    {
                        increment(reads, def);
                    }
                }
            }
        }

        protected boolean isIdentifier(Token node)
        {
            return node.getTerminal() == Terminal.T_IDENT;
        }

        protected boolean isInExecutableStmt(Token node)
        {
            return node.findNearestAncestor(IExecutionPartConstruct.class) != null;
        }
        
        protected boolean isLHSofAssignmentStmt(Token token)
        {
            ASTAssignmentStmtNode assignmentStmt = token.findNearestAncestor(ASTAssignmentStmtNode.class);
            return assignmentStmt != null
                && token == assignmentStmt.getLhsVariable().getName();
        }

        protected Definition resolveBinding(Token node) throws Error
        {
            List<Definition> bindings = node.resolveBinding();
            if (bindings.size() == 1)
                return bindings.get(0);
            else if (bindings.size() > 1)
                throw new Error("Ambiguous variable name");
            else // (bindings.size() == 0)
                return null; // probably a derived type component (we can't resolve those yet)
        }

        protected void increment(HashMap<Definition, Integer> readsOrWrites, Definition def)
        {
            if (def == null) return;
            
            Integer numWrites = readsOrWrites.get(def);
            if (numWrites == null) numWrites = 0;
            readsOrWrites.put(def, numWrites + 1);
        }
    };
}
