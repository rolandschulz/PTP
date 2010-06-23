/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/

package org.eclipse.rephraserengine.core.vpg.db.profiling;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.HashMap;
import org.eclipse.rephraserengine.core.vpg.TokenRef;
import org.eclipse.rephraserengine.core.vpg.VPG;
import org.eclipse.rephraserengine.core.vpg.VPGDB;
import org.eclipse.rephraserengine.core.vpg.VPGDependency;
import org.eclipse.rephraserengine.core.vpg.VPGEdge;
import org.eclipse.rephraserengine.core.vpg.VPGLog;

/**
 * Collects information about the frequency and duration of VPG database method invocations.
 * <p>
 * The collected information is displayed when the &quot;Print Database Statistic&quot; action is
 * invoked (from the Refactor &gt; (Debugging) menu).
 * <p>
 * The information collected includes the number of method calls made, the average time spent in
 * each method call, and the longest amount of time spent in each method call.
 * 
 * @author Esfar Huq
 *
 * @param <A> AST type
 * @param <T> token type
 * @param <R> TokenRef type
 * @param <D> database type
 * @param <L> VPG log type
 * 
 * @since 3.0
 */
public final class ProfilingDB<A, T, R extends TokenRef<T>, D extends VPGDB<A, T, R, L>, L extends VPGLog<T, R>>
     extends VPGDB<A, T, R, L>
{
    private D db;
    
    /** Maps a method name to the number of calls made to the method */
    private HashMap<String, Integer> methodCalls;

    /** Maps a method name to the total time spent in that method (in milliseconds) */
    private HashMap<String, Long> methodTimes;

    /** Maps a method name to the elapsed time of the longest call for that method (in milliseconds) */
    private HashMap<String, Long> methodLongestCall;
    
    public ProfilingDB(D diskDatabase)
    {
        db = diskDatabase;
        
        methodCalls = new HashMap<String, Integer>();
        methodTimes = new HashMap<String, Long>();
        methodLongestCall = new HashMap<String, Long>();
    }

    @Override public void setVPG(VPG<A, T, R, ? extends VPGDB<A, T, R, L>, L> vpg)
    {
        super.setVPG(vpg);

        long startTime = System.currentTimeMillis();

        db.setVPG(vpg);

        long endTime = System.currentTimeMillis();

        update("setVPG", endTime - startTime); //$NON-NLS-1$
    }

    /**
     * Updates the three fields of this class after a call is made to the database.
     * 
     * @param methodName - the database method that was called
     * @param time - the total time (in milliseconds) that the call took
     */
    private void update(String methodName, long time)
    {
        if (!methodCalls.containsKey(methodName))
        {
            // case where method has been invoked for the first time
            methodCalls.put(methodName, Integer.valueOf(1));
            methodTimes.put(methodName, Long.valueOf(time));
            methodLongestCall.put(methodName, Long.valueOf(time));
        }
        else
        {
            // update number of calls made
            int oldNumCalls = methodCalls.get(methodName).intValue();
            oldNumCalls++;
            methodCalls.put(methodName, Integer.valueOf(oldNumCalls));

            // update total time spent
            long oldTotTime = methodTimes.get(methodName).longValue();
            oldTotTime += time;
            methodTimes.put(methodName, Long.valueOf(oldTotTime));

            // check to see if we should update the longest call field
            if (methodLongestCall.get(methodName).longValue() < time)
                methodLongestCall.put(methodName, Long.valueOf(time));
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // VPG DATABASE METHODS
    ////////////////////////////////////////////////////////////////////////////

    @Override public void flush()
    {
        long startTime = System.currentTimeMillis();
        
        db.flush();
        
        long endTime = System.currentTimeMillis();
        
        update("flush", endTime - startTime); //$NON-NLS-1$
    }

    @Override public void close()
    {
        long startTime = System.currentTimeMillis();
        
        db.close();
        
        long endTime = System.currentTimeMillis();
        
        update("close", endTime - startTime); //$NON-NLS-1$
    }

    @Override public void clearDatabase()
    {
        long startTime = System.currentTimeMillis();
        
        db.clearDatabase();
        
        long endTime = System.currentTimeMillis();
        
        update("clearDatabase", endTime - startTime); //$NON-NLS-1$
    }

    // HYPOTHETICAL UPDATING ///////////////////////////////////////////////////

    @Override public void enterHypotheticalMode() throws IOException
    {
        long startTime = System.currentTimeMillis();
        
        db.enterHypotheticalMode();
        
        long endTime = System.currentTimeMillis();
        
        update("enterHypotheticalMode", endTime - startTime); //$NON-NLS-1$
    }

    @Override public void leaveHypotheticalMode() throws IOException
    {
        long startTime = System.currentTimeMillis();
        
        db.leaveHypotheticalMode();
        
        long endTime = System.currentTimeMillis();
        
        update("leaveHypotheticalMode", endTime - startTime); //$NON-NLS-1$
    }

    @Override public boolean isInHypotheticalMode()
    {
        long startTime = System.currentTimeMillis();
        
        boolean result = db.isInHypotheticalMode();
        
        long endTime = System.currentTimeMillis();
        
        update("isInHypotheticalMode", endTime - startTime); //$NON-NLS-1$
        return result;
    }

    // FILES ///////////////////////////////////////////////////////////////////

    @Override public void updateModificationStamp(String filename)
    {
        long startTime = System.currentTimeMillis();
        
        db.updateModificationStamp(filename);
        
        long endTime = System.currentTimeMillis();
        
        update("updateModificationStamp", endTime - startTime); //$NON-NLS-1$
    }

    @Override public boolean isOutOfDate(String filename)
    {
        long startTime = System.currentTimeMillis();
        
        boolean result = db.isOutOfDate(filename);
        
        long endTime = System.currentTimeMillis();
        
        update("isOutOfDate", endTime - startTime); //$NON-NLS-1$
        return result;
    }

    @Override public void deleteAllEntriesFor(String filename)
    {
        long startTime = System.currentTimeMillis();
        
        db.deleteAllEntriesFor(filename);
        
        long endTime = System.currentTimeMillis();
        
        update("deleteAllEntriesFor", endTime - startTime); //$NON-NLS-1$
    }

    @Override public void deleteAllEdgesAndAnnotationsFor(String filename)
    {
        long startTime = System.currentTimeMillis();
        
        db.deleteAllEdgesAndAnnotationsFor(filename);
        
        long endTime = System.currentTimeMillis();
        
        update("deleteAllEdgesAndAnnotationsFor", endTime - startTime); //$NON-NLS-1$
    }

    @Override public void deleteAllIncomingDependenciesFor(String filename)
    {
        long startTime = System.currentTimeMillis();
        
        db.deleteAllIncomingDependenciesFor(filename);
        
        long endTime = System.currentTimeMillis();
        
        update("deleteAllIncomingDependenciesFor", endTime - startTime); //$NON-NLS-1$
    }

    @Override public void deleteAllOutgoingDependenciesFor(String filename)
    {
        long startTime = System.currentTimeMillis();
        
        db.deleteAllOutgoingDependenciesFor(filename);
        
        long endTime = System.currentTimeMillis();
        
        update("deleteAllOutgoingDependenciesFor", endTime - startTime); //$NON-NLS-1$
    }

    @Override public Iterable<String> listAllFilenames()
    {
        long startTime = System.currentTimeMillis();
        
    	Iterable<String> result = db.listAllFilenames();
    	
    	long endTime = System.currentTimeMillis();
        
        update("listAllFilenames", endTime - startTime); //$NON-NLS-1$
        return result;
    }

    @Override public Iterable<String> listAllFilenamesWithDependents()
    {
        long startTime = System.currentTimeMillis();
        
        Iterable<String> result = db.listAllFilenamesWithDependents();
        
        long endTime = System.currentTimeMillis();
        
        update("listAllFilenamesWithDependents", endTime - startTime); //$NON-NLS-1$
        return result;
    }

    @Override public Iterable<String> listAllDependentFilenames()
    {
        long startTime = System.currentTimeMillis();
        
        Iterable<String> result = db.listAllDependentFilenames();
        
        long endTime = System.currentTimeMillis();
        
        update("listAllDependentFilenames", endTime - startTime); //$NON-NLS-1$
        return result;
    }

    // DEPENDENCIES ////////////////////////////////////////////////////////////

    @Override public void ensure(VPGDependency<A, T, R> dependency)
    {
        long startTime = System.currentTimeMillis();
        
        db.ensure(dependency);
        
        long endTime = System.currentTimeMillis();
        
        update("ensure - dependency", endTime - startTime); //$NON-NLS-1$
    }

    @Override public void delete(VPGDependency<A, T, R> dependency)
    {
        long startTime = System.currentTimeMillis();
        
        db.delete(dependency);
        
        long endTime = System.currentTimeMillis();
        
        update("delete - dependency", endTime - startTime); //$NON-NLS-1$
    }

    @Override public Iterable<String> getOutgoingDependenciesFrom(String filename)
    {
        long startTime = System.currentTimeMillis();
        
        Iterable<String> result = db.getOutgoingDependenciesFrom(filename);
        
        long endTime = System.currentTimeMillis();
        
        update("getOutgoingDependenciesFrom", endTime - startTime); //$NON-NLS-1$
        return result;
    }

    @Override public Iterable<String> getIncomingDependenciesTo(String filename)
    {
        long startTime = System.currentTimeMillis();
        
        Iterable<String> result = db.getIncomingDependenciesTo(filename);
        
        long endTime = System.currentTimeMillis();
        
        update("getIncomingDependenciesTo", endTime - startTime); //$NON-NLS-1$
        return result;
    }

    // EDGES ///////////////////////////////////////////////////////////////////

    @Override public void ensure(VPGEdge<A, T, R> edge)
    {
        long startTime = System.currentTimeMillis();
        
        db.ensure(edge);
        
        long endTime = System.currentTimeMillis();
        
        update("ensure - edge", endTime - startTime); //$NON-NLS-1$
    }

    @Override public void delete(VPGEdge<A, T, R> edge)
    {
        long startTime = System.currentTimeMillis();
        
        db.delete(edge);
        
        long endTime = System.currentTimeMillis();
        
        update("delete - edge", endTime - startTime); //$NON-NLS-1$
    }

    @Override public Iterable<? extends VPGEdge<A, T, R>> getAllEdgesFor(String filename)
    {
        long startTime = System.currentTimeMillis();
        
        Iterable<? extends VPGEdge<A, T, R>> result =  db.getAllEdgesFor(filename);
        
        long endTime = System.currentTimeMillis();
        
        update("getAllEdgesFor", endTime - startTime); //$NON-NLS-1$
        return result;
    }

    // ANNOTATIONS /////////////////////////////////////////////////////////////

    @Override public void setAnnotation(R token, int annotationID, Serializable annotation)
    {
        long startTime = System.currentTimeMillis();
        
        db.setAnnotation(token, annotationID, annotation);
        
        long endTime = System.currentTimeMillis();
        
        update("setAnnotation", endTime - startTime); //$NON-NLS-1$
    }

    @Override public void deleteAnnotation(R token, int annotationID)
    {
        long startTime = System.currentTimeMillis();
        
        db.deleteAnnotation(token, annotationID);
        
        long endTime = System.currentTimeMillis();
        
        update("deleteAnnotation", endTime - startTime); //$NON-NLS-1$
    }

    // UTILITY METHODS /////////////////////////////////////////////////////////

    @Override public void printOn(PrintStream out)
    {
        printStatisticsOn(out);
        out.println();
        db.printOn(out);
    }

    @Override public void printStatisticsOn(PrintStream out)
    {
        out.println();
        out.println("Wrapped database is " + db.getClass().getName()); //$NON-NLS-1$
        out.println();
        out.println("Additional Statistics: "); //$NON-NLS-1$
        out.format("%-35s%-20s%-20s%-20s\n", "Method Name", "Times Called", "Average Time (ms)", "Longest Time (ms)");   //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$//$NON-NLS-5$
        
        for (String aMethod : methodCalls.keySet())
        {
            int numCalls = methodCalls.get(aMethod).intValue();
            double avgTime = (double)methodTimes.get(aMethod) / (double)numCalls;
            long longestCall = methodLongestCall.get(aMethod).longValue();
            
            out.format("%-35s%-20d%-20.2f%-20d\n", aMethod, numCalls, avgTime, longestCall); //$NON-NLS-1$
        }
        
        db.printStatisticsOn(out);
    }

    @Override public void resetStatistics()
    {
        methodCalls = new HashMap <String, Integer>();
        methodTimes = new HashMap <String, Long>();
        methodLongestCall = new HashMap <String, Long>();
    }

    @Override
    public Iterable<? extends VPGEdge<A, T, R>> getOutgoingEdgesFrom(R tokenRef, int edgeType)
    {
        long startTime = System.currentTimeMillis();
        
        Iterable<? extends VPGEdge<A, T, R>> result =  db.getOutgoingEdgesFrom(tokenRef, edgeType);
        
        long endTime = System.currentTimeMillis();
        
        update("getOutgoingEdgesFrom", endTime - startTime); //$NON-NLS-1$
        return result;
    }

    @Override
    public Iterable<? extends VPGEdge<A, T, R>> getIncomingEdgesTo(R tokenRef, int edgeType)
    {
        long startTime = System.currentTimeMillis();
        
        Iterable<? extends VPGEdge<A, T, R>> result = db.getIncomingEdgesTo(tokenRef, edgeType);
        
        long endTime = System.currentTimeMillis();
        
        update("getIncomingEdgesTo", endTime - startTime); //$NON-NLS-1$
        return result;
    }

    @Override
    public Serializable getAnnotation(R tokenRef, int annotationID)
    {
        
        long startTime = System.currentTimeMillis();
        
        Serializable result = db.getAnnotation(tokenRef, annotationID);
       
        long endTime = System.currentTimeMillis();
        
        update("getAnnotation", endTime - startTime); //$NON-NLS-1$
        return result;
    }
}