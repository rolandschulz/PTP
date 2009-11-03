/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.core.preservation;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.rephraserengine.core.util.OffsetLength;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;
import org.eclipse.rephraserengine.internal.core.preservation.Model;
import org.eclipse.rephraserengine.internal.core.preservation.PrimitiveOp;
import org.eclipse.rephraserengine.internal.core.preservation.PrimitiveOp.Alpha;

@SuppressWarnings("unchecked")
public class PreservationAnalysis
{
    private IAdapterManager adapterManager;
    
    private EclipseVPG vpg;
    
    private Model initialModel = null;
    private boolean normalized = false;
    private List<PrimitiveOp> primitiveOps = new LinkedList<PrimitiveOp>();
    
    public PreservationAnalysis(EclipseVPG vpg)
    {
        this.adapterManager = Platform.getAdapterManager();
        this.vpg = vpg;
    }
    
    private static void debug(Object msg)
    {
        System.err.println(msg.toString());
    }
    
    public void startMonitoring(IFile file)
    {
        debug("Preservation engine is now monitoring changes to " + file.getName());
        
        initialModel = new Model(vpg, EclipseVPG.getFilenameForIFile(file));
        
        debug("INITIAL MODEL:");
        debug(initialModel);
    }
    
    public void finishMonitoring(IFile file)
    {
        debug("Preservation engine is done monitoring changes to " + file.getName());

        for (PrimitiveOp op : primitiveOps)
            initialModel.inormalize(op);
        normalized = true;
        
        debug("NORMALIZED INITIAL MODEL:");
        debug(initialModel);
    }

    public void markAlpha(Object node)
    {
        if (normalized) throw new IllegalStateException("Cannot add operation after normalization");
        
        OffsetLength offsetLength = (OffsetLength)adapterManager.getAdapter(node, OffsetLength.class);
        if (offsetLength == null)
        {
            debug("Unable to get OffsetLength adapter for " + node.getClass().getName());
            throw new Error("Unable to get OffsetLength adapter for " + node.getClass().getName());
        }

        Alpha alpha = PrimitiveOp.alpha(
            offsetLength.getOffset(),
            offsetLength.getPositionPastEnd());
        
        if (needToMergeAlpha(alpha))
        {
            debug("Merging " + alpha);
            mergeAlpha(alpha);
        }
        else
        {
            debug("Adding " + alpha);
            addAlpha(alpha);
        }
        
        adapterManager.getAdapter(node, ResetOffsetLength.class);
    }

    private boolean needToMergeAlpha(Alpha alpha)
    {
        return !primitiveOps.isEmpty()
            && lastOp() instanceof Alpha
            && ((Alpha)lastOp()).j.lb == alpha.j.lb;
    }

    private PrimitiveOp lastOp()
    {
        return primitiveOps.get(primitiveOps.size()-1);
    }

    private void mergeAlpha(Alpha alpha2)
    {
        Alpha alpha1 = (Alpha)primitiveOps.remove(primitiveOps.size()-1);
        
        Alpha newAlpha = PrimitiveOp.alpha(
            alpha1.j.lb,
            alpha1.j.lb + alpha1.j.cardinality() + alpha2.j.cardinality());
        
        addAlpha(newAlpha);
    }

    private void addAlpha(Alpha alpha)
    {
        primitiveOps.add(alpha);
    }

    @Override public String toString()
    {
        return primitiveOps.toString();
    }

    public void checkForPreservation(RefactoringStatus status, IFile file)
    {
        debug("Checking for preservation in " + file.getName());

        Model derivativeModel = new Model(vpg, EclipseVPG.getFilenameForIFile(file));
        
        debug("DERIVATIVE MODEL:");
        debug(derivativeModel);
    }
}
