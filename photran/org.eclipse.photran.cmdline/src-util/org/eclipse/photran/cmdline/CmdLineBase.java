package org.eclipse.photran.cmdline;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;

public class CmdLineBase
{
	protected CmdLineBase() {;}
	   
	protected static String parseCommandLine(String cmdName, String[] args)
    {
        if (args.length != 1)
        {
            System.err.println("Usage: " + cmdName + " filename");
            System.exit(1);
        }
        String filename = args[0];
        return filename;
    }

	protected static IFortranAST parse(String filename)
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
