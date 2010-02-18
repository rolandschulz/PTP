import java.io.File;
import java.io.PrintStream;

import org.eclipse.core.resources.Util;
import org.eclipse.photran.cmdline.CmdLineBase;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.IBodyConstruct;
import org.eclipse.photran.internal.core.parser.Parser.ASTVisitor;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;

public class MoreDefs extends CmdLineBase
{
    public static void main(String[] args)
    {
        String filename = parseCommandLine("moredefs", args);

        Util.DISPLAY_WARNINGS = false;
        PhotranVPG.getInstance().start();
        IFortranAST ast = parse(filename);

        ast.accept(new ListDefinitions());
        
        PhotranVPG.getDatabase().close();
    }

    private static final class ListDefinitions extends ASTVisitor
    {
        private int indentation = 0;

        private PrintStream ps = System.out;

        @Override public void visitASTNode(IASTNode node)
        {
            // Unfortunately, we don't have a "visitScopingNode" method, so we have to do this
            // to find all scopes other than the outermost (file) scope
            if (!(node instanceof ScopingNode) || (node instanceof ASTExecutableProgramNode)) return;
            ScopingNode scope = (ScopingNode)node;
            
            indentation += 4;
            
            try
            {
                for (Definition d : scope.getAllDefinitions())
                    describeDeclaration(d);
            }
            catch (Exception e)
            {
                ps.println("EXCEPTION: " + e.getClass().getName() + ": " + e.getMessage());
                e.printStackTrace(ps);
            }
            
            traverseChildren(node);

            indentation -= 4;
        }

        protected void describeDeclaration(Definition def)
        {
            printIndent();
            
            ps.print(def.getCanonicalizedName());
            
            if (def.isSubprogramArgument())
            {
                ps.print(" - intent(");
                if (def.isIntentIn()) ps.print("in");
                if (def.isIntentOut()) ps.print("out");
                ps.print(")");
            }
            
            ps.print(" in ");
            
            String filePath = def.getTokenRef().getFilename();
            String filename = filePath.substring(filePath.lastIndexOf(File.separatorChar)+1);
            ps.print(filename);

            Token tokenInAST = def.getTokenRef().findToken();

            ps.print(" at offset ");
            ps.print(tokenInAST.getFileOffset());
            
            ps.print(" on line ");
            ps.print(tokenInAST.getLine());
            
            ps.println();
            
            printIndent();
            ps.println("    It is first declared in this statement:");
            printIndent();
            ps.println(tokenInAST.findNearestAncestor(IBodyConstruct.class));
        }

        protected void printIndent()
        {
            for (int i = 0; i < indentation; i++)
                ps.print(' ');
        }
    }
}
