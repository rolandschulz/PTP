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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ltk.core.refactoring.FileStatusContext;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.rephraserengine.core.util.OffsetLength;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;
import org.eclipse.rephraserengine.internal.core.preservation.Model;
import org.eclipse.rephraserengine.internal.core.preservation.ModelDiff;
import org.eclipse.rephraserengine.internal.core.preservation.PrimitiveOp;
import org.eclipse.rephraserengine.internal.core.preservation.ModelDiff.EdgeAdded;
import org.eclipse.rephraserengine.internal.core.preservation.ModelDiff.EdgeDeleted;
import org.eclipse.rephraserengine.internal.core.preservation.ModelDiff.EdgeSinkChanged;
import org.eclipse.rephraserengine.internal.core.preservation.ModelDiff.ModelDiffProcessor;
import org.eclipse.rephraserengine.internal.core.preservation.PrimitiveOp.Alpha;

@SuppressWarnings("unchecked")
public class PreservationAnalysis
{
    private IAdapterManager adapterManager;

    private EclipseVPG vpg;
    private IProgressMonitor progressMonitor;

    private Map<String, Model> initialModels = new HashMap<String, Model>();

    private List<PrimitiveOp> primitiveOps = new LinkedList<PrimitiveOp>();

    private Set<Integer> preserveEdgeTypes = Collections.<Integer>emptySet();

    public PreservationAnalysis(EclipseVPG vpg, IProgressMonitor pm, int... edgeTypes)
    {
        this.adapterManager = Platform.getAdapterManager();
        this.vpg = vpg;
        this.progressMonitor = pm;

        preserveEdgeTypes = new HashSet<Integer>(edgeTypes.length);
        for (int type : edgeTypes)
            preserveEdgeTypes.add(type);
    }

    public void monitor(IFile file)
    {
        if (!vpg.db.isInHypotheticalMode())
        {
            try
            {
                progressMonitor.subTask("Please wait; switching database to hypothetical mode");
                vpg.db.enterHypotheticalMode();
            }
            catch (IOException e)
            {
                throw new Error(e);
            }
        }

        progressMonitor.subTask("Computing initial model");
        String filename = EclipseVPG.getFilenameForIFile(file);
        initialModels.put(filename, new Model(vpg, filename));
    }

    public void markAlpha(IFile file, Object node)
    {
        String filename = EclipseVPG.getFilenameForIFile(file);
        OffsetLength offsetLength = (OffsetLength)adapterManager.getAdapter(node, OffsetLength.class);
        if (offsetLength == null)
            throw new Error("Unable to get OffsetLength adapter for " + node.getClass().getName());

        Alpha alpha = PrimitiveOp.alpha(
            filename,
            offsetLength.getOffset(),
            offsetLength.getPositionPastEnd());

        if (needToMergeAlpha(alpha))
            mergeAlpha(alpha);
        else
            addAlpha(alpha);

        adapterManager.getAdapter(node, ResetOffsetLength.class);
    }

    private boolean needToMergeAlpha(Alpha alpha)
    {
        return !primitiveOps.isEmpty()
            && lastOp() instanceof Alpha
            && ((Alpha)lastOp()).filename.equals(alpha.filename)
            && ((Alpha)lastOp()).j.lb == alpha.j.lb;
            //&& ((Alpha)lastOp()).preserveEdgeTypes.equals(alpha.preserveEdgeTypes);
    }

    private PrimitiveOp lastOp()
    {
        return primitiveOps.get(primitiveOps.size()-1);
    }

    private void mergeAlpha(Alpha alpha2)
    {
        Alpha alpha1 = (Alpha)primitiveOps.remove(primitiveOps.size()-1);

        Alpha newAlpha = PrimitiveOp.alpha(
            alpha1.filename,
            alpha1.j.lb,
            alpha1.j.lb + alpha1.j.cardinality() + alpha2.j.cardinality());
            //alpha1.preserveEdgeTypes);

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
        String filename = EclipseVPG.getFilenameForIFile(file);

        progressMonitor.subTask("Normalizing initial model");
        initialModels.get(filename).inormalize(primitiveOps, preserveEdgeTypes);

        progressMonitor.subTask("Computing derivative model");
        Model derivativeModel = new Model(vpg, EclipseVPG.getFilenameForIFile(file));

        progressMonitor.subTask("Normalizing derivative model");
        derivativeModel.dnormalize(primitiveOps, preserveEdgeTypes);

        progressMonitor.subTask("Differencing initial and derivative models");
        ModelDiff diff = initialModels.get(filename).compareAgainst(derivativeModel);
        describeDifferences(status, diff, file);

        try
        {
            progressMonitor.subTask("Switching database out of hypothetical mode");
            vpg.db.leaveHypotheticalMode();
        }
        catch (IOException e)
        {
            throw new Error(e);
        }
    }

    private void describeDifferences(final RefactoringStatus status, ModelDiff diff, final IFile file)
    {
        Object ast = vpg.acquireTransientAST(EclipseVPG.getFilenameForIFile(file));
        final String code = ast == null ? null : vpg.getSourceCodeFromAST(ast);

        diff.processUsing(new ModelDiffProcessor()
        {
            @Override
            public void processEdgeAdded(EdgeAdded addition)
            {
                String msg = "Completing this transformation will introduce an unexpected "
                    + vpg.describeEdgeType(addition.edgeType).toLowerCase();

                status.addError(msg,
                    new PostTransformationContext(
                        file,
                        code,
                        addition.toRegion()));
            }

            @Override
            public void processEdgeDeleted(EdgeDeleted deletion)
            {
                String msg = "Completing this transformation will cause an existing "
                    + vpg.describeEdgeType(deletion.edgeType).toLowerCase()
                    + " to disappear";

                status.addError(msg,
                    new FileStatusContext(
                        file,
                        deletion.toRegion()));
            }

            @Override
            public void processEdgeSinkChanged(EdgeSinkChanged change)
            {
                String msg = "Completing this transformation will cause an existing "
                    + vpg.describeEdgeType(change.edgeType).toLowerCase()
                    + " to change";

                status.addError(msg,
                    new PostTransformationContext(
                        file,
                        code,
                        change.toRegion()));
            }

        });
    }
}