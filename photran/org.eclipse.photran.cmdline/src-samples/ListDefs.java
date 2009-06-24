import java.io.PrintStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.core.vpg.PhotranTokenRef;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ImplicitSpec;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.Parser.ASTVisitor;
import org.eclipse.photran.internal.core.parser.Parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;

public class ListDefs
{
    public static void main(String[] args)
    {
        String filename = parseCommandLine(args);
        startVPG();
        IFortranAST ast = parse(filename);

        System.out.println("COMPLETE SOURCE CODE FOR " + filename.toUpperCase() + "\n");
        printSourceCodeFromAST(ast);
        
        System.out.println("\nSYMBOL TABLE FOR " + filename.toUpperCase());
        printSymbolTable(ast);
        
        System.out.println("\nIDENTIFIER BINDINGS IN " + filename.toUpperCase() + "\n");
        printAllIdentifierBindings(ast);
    }

    private static void printSourceCodeFromAST(IFortranAST ast)
    {
        ast.getRoot().printOn(System.out, null);
    }

    private static void printAllIdentifierBindings(IFortranAST ast)
    {
        ast.accept(new ASTVisitor()
        {
            @Override
            public void visitToken(Token token)
            {
                if (token.isIdentifier())
                    for (Definition def : token.resolveBinding())
                        System.out.println(token.getText() + " (line " + token.getLine() + ") is bound to " + def);
            }
        });
    }

    private static void printSymbolTable(IFortranAST ast)
    {
        ast.accept(new GenericASTVisitor()
        {
            private int indentation = 0;
            private PrintStream ps = System.out;
            
            @Override public void visitASTNode(IASTNode node)
            {
                if (!(node instanceof ScopingNode)) { traverseChildren(node); return; }
                if (node instanceof ASTExecutableProgramNode && node.getParent() != null) { traverseChildren(node); return; }
                
                ScopingNode scope = (ScopingNode)node;
                
                ps.println();
                describeScope(scope);
                
                indentation += 4;
                
                try
                {
                    for (Definition d : scope.getAllDefinitions())
                        println(describeDeclaration(d));
                }
                catch (Exception e)
                {
                    println("EXCEPTION: " + e.getClass().getName() + ": " + e.getMessage());
                    e.printStackTrace(ps);
                }
                
                traverseChildren(node);

                indentation -= 4;
            }

            private void describeScope(ScopingNode scope)
            {
                PhotranTokenRef representativeToken = scope.getRepresentativeToken();
                
                if (representativeToken.getOffset() < 0)
                    print("(Global Scope)");
                else
                    print("Scope: " + representativeToken.getText());
                
                if (scope.isInternal()) ps.print(" (Internal Subprogram)");
                
                if (scope.isDefaultVisibilityPrivate()) ps.print(" - Default Visibility is PRIVATE");
                
                ImplicitSpec implicitSpec = scope.getImplicitSpec();
                if (implicitSpec == null)
                    ps.print(" - Implicit None");
                else
                    ps.print(" - " + implicitSpec.toString());
                
                ps.println();
            }

            protected String describeDeclaration(Definition def)
            {
                try
                {
                    return def.getCanonicalizedName()
                        + " - "
                        + def.describeClassification()
                        + (def.isSubprogramArgument() ? " (Subprogram Argument)" : "")
                        + " - "
                        + def.getTokenRef().getFilename();
                }
                catch (Throwable e)
                {
                    return def.toString();
                }
            }
   
            private void print(String text)
            {
                for (int i = 0; i < indentation; i++)
                    ps.print(' ');
                ps.print(text);
            }
            
            private void println(String text)
            {
                print(text);
                ps.println();
            }
        });
    }
    
    private static String parseCommandLine(String[] args)
    {
        if (args.length != 1)
        {
            System.err.println("Usage: listdefs filename");
            System.exit(1);
        }
        String filename = args[0];
        return filename;
    }

    private static PhotranVPG startVPG()
    {
        PhotranVPG vpg = PhotranVPG.getInstance();
        vpg.start();
        return vpg;
    }

    private static IFortranAST parse(String filename)
    {
        PhotranVPG vpg = PhotranVPG.getInstance();
        IFortranAST ast = vpg.acquireTransientAST((IFile)ResourcesPlugin.getWorkspace().getRoot().findMember(filename));
        if (ast == null)
        {
            System.err.println("Unable to find or parse file " + filename);
            vpg.log.printOn(System.err);
            System.exit(1);
        }
        return ast;
    }
}
