package org.eclipse.photran.internal.core.analysis.binding;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.core.vpg.PhotranVPG;

public class Binder
{
    private Binder() {}
    
    public static void bind(IFortranAST ast, IFile file)
    {
        StringBuilder sb = new StringBuilder("  - Binder#bind: ");
        
        long[] t = new long[6];
        t[0] = System.currentTimeMillis();
        
        ast.accept(new ImplicitSpecCollector());
        t[1] = System.currentTimeMillis();
        sb.append("Impl: " + (t[1] - t[0]));
        
        ast.accept(new DefinitionCollector(file));
        t[2] = System.currentTimeMillis();
        sb.append(", Def: " + (t[2] - t[1]));
        
        ast.accept(new SpecificationCollector());
        t[3] = System.currentTimeMillis();
        sb.append(", Spec: " + (t[3] - t[2]));
        
        ast.accept(new ModuleLoader(file, new NullProgressMonitor()));
        // TODO: Type check here so derived type components can be resolved
        t[4] = System.currentTimeMillis();
        sb.append(", Mod: " + (t[4] - t[3]));
        
        ast.accept(new ReferenceCollector());
        t[5] = System.currentTimeMillis();
        sb.append(", Ref: " + (t[5] - t[4]));
        
        PhotranVPG.getInstance().debug(sb.toString(), "");
    }
}
