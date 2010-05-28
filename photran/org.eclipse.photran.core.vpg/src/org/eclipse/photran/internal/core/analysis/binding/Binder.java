package org.eclipse.photran.internal.core.analysis.binding;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.core.vpg.PhotranVPGBuilder;

/**
 * Performs name-binding analysis on a Fortran file, storing the results in the VPG.
 * <p>
 * This class should only be invoked by {@link PhotranVPGBuilder}; clients should use
 * {@link Token#resolveBinding()} and related methods to look up binding information.
 * 
 * @author Jeff Overbey
 */
public class Binder
{
    private Binder() {}

    private static Map<Class<?>, Long> avgTimes = new HashMap<Class<?>, Long>();
    private static Map<Class<?>, Long> counts = new HashMap<Class<?>, Long>();

    private static Map<Class<?>, Long> maxTimes = new HashMap<Class<?>, Long>();
    private static Map<Class<?>, String> maxFiles = new HashMap<Class<?>, String>();

    public static void bind(IFortranAST ast, IFile file)
    {
        // Name-binding analysis                    Logging/timing
        // =======================================  ===========================================================
        PhotranVPG vpg = PhotranVPG.getInstance();  String filename = file.getName();
                                                    StringBuilder sb = new StringBuilder("  - Binder#bind: "); //$NON-NLS-1$
        
                                                    long start = System.currentTimeMillis();
        ast.accept(new ImplicitSpecCollector());    logTime(start, ImplicitSpecCollector.class, filename);
        
                                                    start = System.currentTimeMillis();
        ast.accept(new PrivateCollector());         logTime(start, PrivateCollector.class, filename);
        
                                                    start = System.currentTimeMillis();
        ast.accept(new DefinitionCollector(file));  logTime(start, DefinitionCollector.class, filename);
        
                                                    start = System.currentTimeMillis();
        ast.accept(new SpecificationCollector());   logTime(start, SpecificationCollector.class, filename);
        
                                                    start = System.currentTimeMillis();
        ast.accept(new ModuleLoader(file));         logTime(start, ModuleLoader.class, filename);
        // TODO: Type check here so derived type components can be resolved
                                                    start = System.currentTimeMillis();
        vpg.enableDefinitionCaching();
        ast.accept(new ReferenceCollector());
        vpg.disableDefinitionCaching();             logTime(start, ReferenceCollector.class, filename);
                                                    vpg.debug(sb.toString(), ""); //$NON-NLS-1$
    }

    private static void logTime(long start, Class<?> clazz, String filename)
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
        
        long oldMaxTime = maxTimes.containsKey(clazz) ? maxTimes.get(clazz) : 0L;
        if (elapsed > oldMaxTime)
        {
            maxTimes.put(clazz, elapsed);
            maxFiles.put(clazz, filename);
        }
    }
    
    public static long getAvgTime(Class<?> clazz)
    {
        return avgTimes.containsKey(clazz) ? avgTimes.get(clazz) : 0L;
    }

    public static void printStatisticsOn(PrintStream ps)
    {
        ps.println("Name Binding Analysis Statistics:"); //$NON-NLS-1$
        
        ps.println();
        ps.println("    Average Times:"); //$NON-NLS-1$
        for (Class<?> clazz : avgTimes.keySet())
            ps.println("        " + clazz.getSimpleName() + ": " + avgTimes.get(clazz) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        ps.println();
        ps.println("    Maximum Times:"); //$NON-NLS-1$
        for (Class<?> clazz : maxTimes.keySet())
            ps.println("        " + clazz.getSimpleName() + ": " + maxTimes.get(clazz) + " ms (" + maxFiles.get(clazz) + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    public static void resetStatistics()
    {
        counts.clear();
        avgTimes.clear();
    }
}
