package org.eclipse.photran.internal.core.analysis.binding;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.core.vpg.PhotranVPG;

public class Binder
{
    private Binder() {}

    private static Map<Class<?>, Long> avgTimes = new HashMap<Class<?>, Long>();
    private static Map<Class<?>, Long> counts = new HashMap<Class<?>, Long>();
    
    public static void bind(IFortranAST ast, IFile file)
    {
        StringBuilder sb = new StringBuilder("  - Binder#bind: ");
        
        long start = System.currentTimeMillis();
        ast.accept(new ImplicitSpecCollector());
        logTime(start, ImplicitSpecCollector.class);
        
        start = System.currentTimeMillis();
        ast.accept(new PrivateCollector());
        logTime(start, PrivateCollector.class);
        
        start = System.currentTimeMillis();
        ast.accept(new DefinitionCollector(file));
        logTime(start, DefinitionCollector.class);
        
        start = System.currentTimeMillis();
        ast.accept(new SpecificationCollector());
        logTime(start, SpecificationCollector.class);
        
        start = System.currentTimeMillis();
        ast.accept(new ModuleLoader(file, new NullProgressMonitor()));
        // TODO: Type check here so derived type components can be resolved
        logTime(start, ModuleLoader.class);
        
        start = System.currentTimeMillis();
        ast.accept(new ReferenceCollector());
        logTime(start, ReferenceCollector.class);
        
        PhotranVPG.getInstance().debug(sb.toString(), "");
    }

    private static void logTime(long start, Class<?> clazz)
    {
        long elapsed = System.currentTimeMillis() - start;
        
        long oldCount = counts.containsKey(clazz) ? counts.get(clazz) : 0L;
        long oldAvgTime = avgTimes.containsKey(clazz) ? avgTimes.get(clazz) : 0L;
        
        /*              a + b
         * oldAvgTime = -----
         *                2
         *                          ( a + b )
         *                         (  -----  ) * 2 + c
         *              a + b + c   (   2   )
         * newAvgTime = --------- = ------------------
         *                  3                3
         */
        long newAvgTime = (oldAvgTime * oldCount + elapsed) / (oldCount + 1);
        
        counts.put(clazz, oldCount+1);
        avgTimes.put(clazz, newAvgTime);
    }
    
    public static long getAvgTime(Class<?> clazz)
    {
        return avgTimes.containsKey(clazz) ? avgTimes.get(clazz) : 0L;
    }

    public static void printStatisticsOn(PrintStream ps)
    {
        ps.println("Name Binding Analysis Statistics:");
        for (Class<?> clazz : avgTimes.keySet())
            ps.println("    " + clazz.getSimpleName() + ": " + avgTimes.get(clazz) + " ms");
    }

    public static void resetStatistics()
    {
        counts.clear();
        avgTimes.clear();
    }
}
