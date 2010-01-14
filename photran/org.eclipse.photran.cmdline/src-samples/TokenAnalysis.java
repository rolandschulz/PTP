import java.io.File;
import java.lang.reflect.Method;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.Util;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.photran.cmdline.CmdLineBase;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;

public class TokenAnalysis extends CmdLineBase
{
    public static void main(String[] args) throws CoreException
    {
        Util.DISPLAY_WARNINGS = false;
        PhotranVPG.getInstance().start();
        
        System.out.println("Filename\tSz\tIntNd\tTok-\tTok+\tWht\tTxt-\tTxt+");
        ResourcesPlugin.getWorkspace().getRoot().accept(new IResourceVisitor()
        {
            public boolean visit(IResource resource) throws CoreException
            {
                if (resource.getName().endsWith(".f90"))
                    analyze(resource.getFullPath().toOSString());
                return true;
            }   
        });
        
        PhotranVPG.getDatabase().close();
    }
    
    private static void analyze(String filename)
    {
        Analyzer a = new Analyzer();
        IFortranAST ast = parse(filename);
        if (ast != null)
        {
            ast.accept(a);
            System.out.println(filename.substring(filename.lastIndexOf('/')+1) + "\t" +
                               new File(filename).length() + "\t" +
                               a.interiorNodes + "\t" +
                               a.hiddenTokens + "\t" +
                               a.nonHiddenTokens + "\t" +
                               a.whiteCharacters + "\t" +
                               a.hiddenTextCharacters + "\t" +
                               a.nonHiddenTextCharacters);
        }
    }
    
    private static class Analyzer extends GenericASTVisitor
    {
        private long interiorNodes = 0;
        
        private long hiddenTokens = 0;
        private long nonHiddenTokens = 0;
        
        private long whiteCharacters = 0;
        private long hiddenTextCharacters = 0;
        private long nonHiddenTextCharacters = 0;
        
        @Override public void visitASTNode(IASTNode node)
        {
            if (node instanceof Token) throw new Error();
            interiorNodes++;
            traverseChildren(node);
        }
        
        @Override public void visitToken(Token token)
        {
            whiteCharacters += token.getWhiteBefore().length();
            
            if (isHidden(token))
            {
                hiddenTokens++;
                hiddenTextCharacters += token.getText().length();
            }
            else
            {
                nonHiddenTokens++;
                nonHiddenTextCharacters += token.getText().length();
            }
        }

        private boolean isHidden(Token token)
        {
            try
            {
                IASTNode parent = token.getParent();
                for (Method m : parent.getClass().getMethods())
                    if (m.getParameterTypes().length == 0
                                    && m.getReturnType() != null
                                    && IASTNode.class.isAssignableFrom(m.getReturnType()))
                            if (m.invoke(parent, new Object[0]) == token)
                                return false;
                return true;
            }
            catch (Exception e)
            {
                throw new Error(e);
            }
        }
    };
}
