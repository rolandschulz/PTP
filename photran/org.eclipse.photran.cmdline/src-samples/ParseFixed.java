import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.photran.cmdline.CmdLineBase;
import org.eclipse.photran.internal.core.SyntaxException;
import org.eclipse.photran.internal.core.lexer.LexerException;
import org.eclipse.photran.internal.core.lexer.LexerFactory;
import org.eclipse.photran.internal.core.lexer.SourceForm;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.Parser;

public class ParseFixed extends CmdLineBase
{
    public static void main(String[] args) throws CoreException, IOException, LexerException, SyntaxException
    {
        if (args.length != 1)
        {
            System.err.println("Usage: parsefixed filename");
            System.exit(1);
        }

        ASTExecutableProgramNode ast = new Parser().parse(
            LexerFactory.createLexer(new File(args[0]), SourceForm.FIXED_FORM, true));
        
        if (ast == null)
        {
            System.err.println("Parse failed");
            System.exit(2);
        }
    }
}
