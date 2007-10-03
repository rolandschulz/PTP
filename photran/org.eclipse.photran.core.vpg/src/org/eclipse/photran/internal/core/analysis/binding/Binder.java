package org.eclipse.photran.internal.core.analysis.binding;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.photran.core.IFortranAST;

public class Binder
{
    private Binder() {}
    
    public static void bind(IFortranAST ast, IFile file)
    {
        long[] t = new long[6];
        t[0] = System.currentTimeMillis();
        ast.visitTopDownUsing(new ImplicitSpecCollector());
        t[1] = System.currentTimeMillis();
        ast.visitBottomUpUsing(new DefinitionCollector(file));
        t[2] = System.currentTimeMillis();
        ast.visitBottomUpUsing(new SpecificationCollector());
        t[3] = System.currentTimeMillis();
        ast.visitBottomUpUsing(new ModuleLoader(file, new NullProgressMonitor()));
        // TODO: Type check here so derived type components can be resolved
        t[4] = System.currentTimeMillis();
        ast.visitBottomUpUsing(new ReferenceCollector());
        t[5] = System.currentTimeMillis();

//        String s = "";
//        for (int i = 1; i < t.length; i++)
//            s += (t[i] - t[i-1]) + " ";
//        System.out.println(s);
    }
}
