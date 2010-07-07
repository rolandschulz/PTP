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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import org.eclipse.rephraserengine.internal.core.preservation.ModelDiff.EdgeAdded;
import org.eclipse.rephraserengine.internal.core.preservation.ModelDiff.EdgeDeleted;
import org.eclipse.rephraserengine.internal.core.preservation.ModelDiff.EdgeSinkChanged;
import org.eclipse.rephraserengine.internal.core.preservation.ModelDiff.ModelDiffProcessor;
import org.eclipse.rephraserengine.internal.core.preservation.PrimitiveOp;
import org.eclipse.rephraserengine.internal.core.preservation.PrimitiveOp.Alpha;
import org.eclipse.rephraserengine.internal.core.preservation.PrimitiveOp.Epsilon;
import org.eclipse.rephraserengine.internal.core.preservation.PrimitiveOp.Mu;
import org.eclipse.rephraserengine.internal.core.preservation.PrimitiveOp.Rho;
import org.eclipse.rephraserengine.internal.core.preservation.PrimitiveOpList;

/**
 * Checks for preservation of semantic edges in a program graph modulo a sequence of primitive
 * transformations.
 *
 * @author Jeff Overbey
 *
 * @since 1.0
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class PreservationAnalysis
{
    private IAdapterManager adapterManager;

    private EclipseVPG vpg;
    //private IProgressMonitor progressMonitor;

    private Model initialModel;
    private PrimitiveOpList primitiveOps;
    private Set<PreservationRule> preserveEdgeTypes;

    public PreservationAnalysis(
        EclipseVPG vpg,
        IProgressMonitor progressMonitor, int ticks,
        IFile file,
        PreservationRule... edgeTypes)
    {
        this(vpg, progressMonitor, ticks, EclipseVPG.getFilenameForIFile(file), edgeTypes);
    }

    public PreservationAnalysis(
        EclipseVPG vpg,
        IProgressMonitor progressMonitor, int ticks,
        Collection<IFile> files,
        PreservationRule... edgeTypes)
    {
        this(vpg, progressMonitor, ticks, getFilenames(files), edgeTypes);
    }

    public PreservationAnalysis(
        EclipseVPG vpg,
        IProgressMonitor progressMonitor, int ticks,
        String filename,
        PreservationRule... edgeTypes)
    {
        this(vpg, progressMonitor, ticks, Collections.singletonList(filename), edgeTypes);
    }

    public PreservationAnalysis(
        EclipseVPG vpg,
        IProgressMonitor progressMonitor, int ticks,
        List<String> filenames,
        PreservationRule... edgeTypes)
    {
        this.adapterManager = Platform.getAdapterManager();
        this.vpg = vpg;
        //this.progressMonitor = progressMonitor;

        this.primitiveOps = new PrimitiveOpList();

        this.preserveEdgeTypes = new HashSet<PreservationRule>(edgeTypes.length);
        for (PreservationRule type : edgeTypes)
            this.preserveEdgeTypes.add(type);

        progressMonitor.subTask(Messages.PreservationAnalysis_EnteringHypotheticalMode);
        ensureDatabaseIsInHypotheticalMode();

        this.initialModel = new Model("initial model", progressMonitor, ticks, vpg, filenames); //$NON-NLS-1$
    }

    private static List<String> getFilenames(Collection<IFile> files)
    {
        List<String> result = new ArrayList<String>(files.size());
        for (IFile file : files)
            result.add(EclipseVPG.getFilenameForIFile(file));
        return result;
    }

    private void ensureDatabaseIsInHypotheticalMode() throws Error
    {
        if (!vpg.db.isInHypotheticalMode())
        {
            try
            {
                vpg.db.enterHypotheticalMode();
            }
            catch (IOException e)
            {
                throw new Error(e);
            }
        }
    }

    public void markAlpha(IFile file, Object node)
    {
        String filename = EclipseVPG.getFilenameForIFile(file);
        OffsetLength offsetLength = (OffsetLength)adapterManager.getAdapter(node, OffsetLength.class);
        if (offsetLength == null)
            throw new Error("Unable to get OffsetLength adapter for " + node.getClass().getName()); //$NON-NLS-1$

        Alpha alpha = PrimitiveOp.alpha(
            filename,
            offsetLength.getOffset(),
            offsetLength.getPositionPastEnd());

        primitiveOps.add(alpha);
        adapterManager.getAdapter(node, ResetOffsetLength.class);
    }

    public void markEpsilon(IFile file, Object node)
    {
        String filename = EclipseVPG.getFilenameForIFile(file);
        OffsetLength offsetLength = (OffsetLength)adapterManager.getAdapter(node, OffsetLength.class);
        if (offsetLength == null)
            throw new Error("Unable to get OffsetLength adapter for " + node.getClass().getName()); //$NON-NLS-1$

        Epsilon epsilon = PrimitiveOp.epsilon(
            filename,
            offsetLength.getOffset(),
            offsetLength.getPositionPastEnd());

        primitiveOps.add(epsilon);
    }

    public void markRho(IFile file, Object node, int oldLength, int newLength)
    {
        String filename = EclipseVPG.getFilenameForIFile(file);
        OffsetLength offsetLength = (OffsetLength)adapterManager.getAdapter(node, OffsetLength.class);
        if (offsetLength == null)
            throw new Error("Unable to get OffsetLength adapter for " + node.getClass().getName()); //$NON-NLS-1$

        Rho rho = PrimitiveOp.rho(
            filename,
            offsetLength.getOffset(),
            oldLength,
            newLength);

        primitiveOps.add(rho);
    }

    /**
     * @since 3.0
     */
    public void markMu(IFile file, Object oldNode, Object newNode)
    {
        String filename = EclipseVPG.getFilenameForIFile(file);

        OffsetLength oldOffsetLength = (OffsetLength)adapterManager.getAdapter(oldNode, OffsetLength.class);
        if (oldOffsetLength == null)
            throw new Error("Unable to get OffsetLength adapter for " + oldNode.getClass().getName()); //$NON-NLS-1$

        OffsetLength newOffsetLength = (OffsetLength)adapterManager.getAdapter(newNode, OffsetLength.class);
        if (newOffsetLength == null)
            throw new Error("Unable to get OffsetLength adapter for " + newNode.getClass().getName()); //$NON-NLS-1$

        Mu mu = PrimitiveOp.mu(
            filename,
            oldOffsetLength,
            newOffsetLength);

        primitiveOps.add(mu);
    }

    /**
     * @since 3.0
     */
    public void markMu(IFile file, OffsetLength oldOffsetLength, OffsetLength newOffsetLength)
    {
        String filename = EclipseVPG.getFilenameForIFile(file);

        Mu mu = PrimitiveOp.mu(
            filename,
            oldOffsetLength,
            newOffsetLength);

        primitiveOps.add(mu);
    }

    @Override public String toString()
    {
        return primitiveOps.toString();
    }

    public void checkForPreservation(
        RefactoringStatus status,
        IProgressMonitor progressMonitor, int ticks)
    {
        printDebug("INITIAL MODEL", initialModel); //$NON-NLS-1$
        printDebug("NORMALIZING RELATIVE TO", primitiveOps); //$NON-NLS-1$

        initialModel.inormalize(primitiveOps, preserveEdgeTypes, progressMonitor);
        printDebug("NORMALIZED INITIAL MODEL", initialModel); //$NON-NLS-1$

        printDebug("File ordering:", initialModel.getFiles()); //$NON-NLS-1$
        Model derivativeModel = new Model(
            "derivative model", //$NON-NLS-1$
            progressMonitor, ticks+3,
            vpg,
            initialModel.getFiles());
        printDebug("DERIVATIVE MODEL", derivativeModel); //$NON-NLS-1$

        derivativeModel.dnormalize(primitiveOps, preserveEdgeTypes, progressMonitor);
        printDebug("NORMALIZED DERIVATIVE MODEL", derivativeModel); //$NON-NLS-1$

        ModelDiff diff = initialModel.compareAgainst(derivativeModel, progressMonitor);

        describeDifferences(status, diff);

        leaveHypotheticalMode(progressMonitor);
    }

    private void leaveHypotheticalMode(IProgressMonitor progressMonitor) throws Error
    {
        try
        {
            progressMonitor.subTask(Messages.PreservationAnalysis_ExitingHypotheticalMode);
            vpg.db.leaveHypotheticalMode();
        }
        catch (IOException e)
        {
            throw new Error(e);
        }
    }

    private void printDebug(String string, Object object)
    {
        System.out.println();
        System.out.println();
        System.out.print(string); System.out.println(":"); //$NON-NLS-1$
        System.out.println(object.toString());
    }

    private void describeDifferences(final RefactoringStatus status, ModelDiff diff)
    {
        final HashMap<IFile, String> modifiedSourceCode = new HashMap<IFile, String>();

        diff.processUsing(new ModelDiffProcessor()
        {
            @Override
            public void processAllEdgesDeleted(Set<String> filesWithAllEdgesDeleted)
            {
                String msg = Messages.PreservationAnalysis_TheFollowingFilesWillNotCompile;
                status.addError(msg);

                for (String filename : filesWithAllEdgesDeleted)
                    status.addError("    " + filename, //$NON-NLS-1$
                        new FileStatusContext(EclipseVPG.getIFileForFilename(filename), null));
            }
            
            private String getCode(IFile file, HashMap<IFile, String> modifiedSourceCode)
            {
                if (file == null)
                    return null;
                else if (modifiedSourceCode.containsKey(file))
                    return modifiedSourceCode.get(file);
                else
                {
                    Object ast = vpg.acquireTransientAST(EclipseVPG.getFilenameForIFile(file));
                    if (ast == null) return null;

                    String code = vpg.getSourceCodeFromAST(ast);
                    if (code == null) return null;

                    modifiedSourceCode.put(file, code);
                    return code;
                }
            }

            @Override
            public void processEdgeAdded(EdgeAdded addition)
            {
                String msg =
                    Messages.bind(
                        Messages.PreservationAnalysis_TransformationWillIntroduce,
                        vpg.describeEdgeType(addition.edgeType).toLowerCase(),
                        addition);
                status.addError(msg);

                status.addError(Messages.PreservationAnalysis_FromHere,
                    new PostTransformationContext(
                        addition.getFileContainingSourceRegion(),
                        getCode(addition.getFileContainingSourceRegion(), modifiedSourceCode),
                        addition.getSourceRegion()));

                status.addError(Messages.PreservationAnalysis_ToHere,
                    new PostTransformationContext(
                        addition.getFileContainingSinkRegion(),
                        getCode(addition.getFileContainingSinkRegion(), modifiedSourceCode),
                        addition.getSinkRegion()));
            }

            @Override
            public void processEdgeDeleted(EdgeDeleted deletion)
            {
                String msg =
                    Messages.bind(
                        Messages.PreservationAnalysis_TransformationWillEliminate,
                        vpg.describeEdgeType(deletion.edgeType).toLowerCase(),
                        deletion);
                status.addError(msg);

                status.addError(Messages.PreservationAnalysis_FromHere,
                    new FileStatusContext(
                        deletion.getFileContainingSourceRegion(),
                        deletion.getSourceRegion()));

                status.addError(Messages.PreservationAnalysis_ToHere,
                    new FileStatusContext(
                        deletion.getFileContainingSinkRegion(),
                        deletion.getSinkRegion()));
            }

            @Override
            public void processEdgeSinkChanged(EdgeSinkChanged change)
            {
                String msg =
                    Messages.bind(
                        Messages.PreservationAnalysis_TransformationWillChange,
                        vpg.describeEdgeType(change.edgeType).toLowerCase(),
                        change);
                status.addError(msg);

                status.addError(
                    Messages.bind(
                        Messages.PreservationAnalysis_EdgeWillChange,
                        vpg.describeEdgeType(change.edgeType).toLowerCase()),
                    new PostTransformationContext(
                        change.getFileContainingSourceRegion(),
                        getCode(change.getFileContainingSourceRegion(), modifiedSourceCode),
                        change.getSourceRegion()));

                status.addError(Messages.PreservationAnalysis_ToHere,
                    new PostTransformationContext(
                        change.getFileContainingSinkRegion(),
                        getCode(change.getFileContainingSinkRegion(), modifiedSourceCode),
                        change.getSinkRegion()));

                status.addError(Messages.PreservationAnalysis_WillPointHereInstead,
                    new PostTransformationContext(
                        change.getFileContainingNewSinkRegion(),
                        getCode(change.getFileContainingNewSinkRegion(), modifiedSourceCode),
                        change.getNewSinkRegion()));
            }

        });
    }
}