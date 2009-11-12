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
import org.eclipse.rephraserengine.internal.core.preservation.PrimitiveOp;
import org.eclipse.rephraserengine.internal.core.preservation.PrimitiveOpList;
import org.eclipse.rephraserengine.internal.core.preservation.ModelDiff.EdgeAdded;
import org.eclipse.rephraserengine.internal.core.preservation.ModelDiff.EdgeDeleted;
import org.eclipse.rephraserengine.internal.core.preservation.ModelDiff.EdgeSinkChanged;
import org.eclipse.rephraserengine.internal.core.preservation.ModelDiff.ModelDiffProcessor;
import org.eclipse.rephraserengine.internal.core.preservation.PrimitiveOp.Alpha;
import org.eclipse.rephraserengine.internal.core.preservation.PrimitiveOp.Epsilon;
import org.eclipse.rephraserengine.internal.core.preservation.PrimitiveOp.Rho;

/**
 * Checks for preservation of semantic edges in a program graph modulo a sequence of primitive
 * transformations.
 *
 * @author Jeff Overbey
 *
 * @since 1.0
 */
@SuppressWarnings("unchecked")
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

        progressMonitor.subTask("Please wait; switching database to hypothetical mode");
        ensureDatabaseIsInHypotheticalMode();

        this.initialModel = new Model("initial model", progressMonitor, ticks, vpg, filenames);
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
            throw new Error("Unable to get OffsetLength adapter for " + node.getClass().getName());

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
            throw new Error("Unable to get OffsetLength adapter for " + node.getClass().getName());

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
            throw new Error("Unable to get OffsetLength adapter for " + node.getClass().getName());

        Rho rho = PrimitiveOp.rho(
            filename,
            offsetLength.getOffset(),
            oldLength,
            newLength);

        primitiveOps.add(rho);
    }

    @Override public String toString()
    {
        return primitiveOps.toString();
    }

    public void checkForPreservation(
        RefactoringStatus status,
        IProgressMonitor progressMonitor, int ticks)
    {
        printDebug("INITIAL MODEL", initialModel);
        printDebug("NORMALIZING RELATIVE TO", primitiveOps);

        initialModel.inormalize(primitiveOps, preserveEdgeTypes, progressMonitor);
        printDebug("NORMALIZED INITIAL MODEL", initialModel);

        printDebug("File ordering:", initialModel.getFiles());
        Model derivativeModel = new Model(
            "derivative model",
            progressMonitor, ticks+3,
            vpg,
            initialModel.getFiles());
        printDebug("DERIVATIVE MODEL", derivativeModel);

        derivativeModel.dnormalize(primitiveOps, preserveEdgeTypes, progressMonitor);
        printDebug("NORMALIZED DERIVATIVE MODEL", derivativeModel);

        ModelDiff diff = initialModel.compareAgainst(derivativeModel, progressMonitor);

        describeDifferences(status, diff);

        leaveHypotheticalMode(progressMonitor);
    }

    private void leaveHypotheticalMode(IProgressMonitor progressMonitor) throws Error
    {
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

    private void printDebug(String string, Object object)
    {
        System.out.println();
        System.out.println();
        System.out.print(string); System.out.println(":");
        System.out.println(object.toString());
    }

    private void describeDifferences(final RefactoringStatus status, ModelDiff diff)
    {
        final HashMap<IFile, String> modifiedSourceCode = new HashMap<IFile, String>();

        diff.processUsing(new ModelDiffProcessor()
        {
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
                String msg = "Completing this transformation will introduce an unexpected "
                    + vpg.describeEdgeType(addition.edgeType).toLowerCase()
                    + " (" + addition + ")"
                    ;

                status.addError(msg,
                    new PostTransformationContext(
                        addition.getFileContainingRegion(),
                        getCode(addition.getFileContainingRegion(), modifiedSourceCode),
                        addition.toRegion()));
            }

            @Override
            public void processEdgeDeleted(EdgeDeleted deletion)
            {
                String msg = "Completing this transformation will cause an existing "
                    + vpg.describeEdgeType(deletion.edgeType).toLowerCase()
                    + " to be eliminated"
                    + " (" + deletion + ")"
                    ;

                status.addError(msg,
                    new FileStatusContext(
                        deletion.getFileContainingRegion(),
                        deletion.toRegion()));
            }

            @Override
            public void processEdgeSinkChanged(EdgeSinkChanged change)
            {
                String msg = "Completing this transformation will cause an existing "
                    + vpg.describeEdgeType(change.edgeType).toLowerCase()
                    + " to change"
                    + " (" + change + ")"
                    ;

                status.addError(msg,
                    new PostTransformationContext(
                        change.getFileContainingRegion(),
                        getCode(change.getFileContainingRegion(), modifiedSourceCode),
                        change.toRegion()));
            }

        });
    }
}