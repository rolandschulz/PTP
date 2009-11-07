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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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
import org.eclipse.rephraserengine.internal.core.preservation.ModelDiff.EdgeAdded;
import org.eclipse.rephraserengine.internal.core.preservation.ModelDiff.EdgeDeleted;
import org.eclipse.rephraserengine.internal.core.preservation.ModelDiff.EdgeSinkChanged;
import org.eclipse.rephraserengine.internal.core.preservation.ModelDiff.ModelDiffProcessor;
import org.eclipse.rephraserengine.internal.core.preservation.PrimitiveOp.Alpha;
import org.eclipse.rephraserengine.internal.core.preservation.PrimitiveOp.Epsilon;

/**
 * Analysis that checks for preservation of semantic edges in a program graph.
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
    private IProgressMonitor progressMonitor;

    private Model initialModel;
    private List<PrimitiveOp> primitiveOps;
    private Set<Preserve> preserveEdgeTypes;

    public PreservationAnalysis(EclipseVPG vpg, IProgressMonitor pm, IFile file, Preserve... edgeTypes)
    {
        this(vpg, pm, EclipseVPG.getFilenameForIFile(file), edgeTypes);
    }

    public PreservationAnalysis(EclipseVPG vpg, IProgressMonitor pm, String filename, Preserve... edgeTypes)
    {
        this.adapterManager = Platform.getAdapterManager();
        this.vpg = vpg;
        this.progressMonitor = pm;

        this.primitiveOps = new LinkedList<PrimitiveOp>();

        this.preserveEdgeTypes = new HashSet<Preserve>(edgeTypes.length);
        for (Preserve type : edgeTypes)
            this.preserveEdgeTypes.add(type);

        progressMonitor.subTask("Please wait; switching database to hypothetical mode");
        ensureDatabaseIsInHypotheticalMode();

        progressMonitor.subTask("Computing initial model");
        this.initialModel = new Model(vpg, filename);

        progressMonitor.subTask("Analyzer ready");
    }

//    private String lastSegment(String filename)
//    {
//        int lastSlash = filename.lastIndexOf('/');
//        int lastBackslash = filename.lastIndexOf('\\');
//        if (lastSlash < 0 && lastBackslash < 0)
//            return filename;
//        else
//            return filename.substring(Math.max(lastSlash+1, lastBackslash+1));
//    }

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
            alpha1.k.lb,
            alpha1.k.lb + alpha1.k.cardinality() + alpha2.k.cardinality());
            //alpha1.preserveEdgeTypes);

        addAlpha(newAlpha);
    }

    private void addAlpha(Alpha alpha)
    {
        primitiveOps.add(alpha);
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

    @Override public String toString()
    {
        return primitiveOps.toString();
    }

    public void checkForPreservation(RefactoringStatus status)
    {
        printDebug("INITIAL MODEL", initialModel);
        printDebug("NORMALIZING RELATIVE TO", primitiveOps);

        progressMonitor.subTask("Normalizing initial model");
        initialModel.inormalize(primitiveOps, preserveEdgeTypes);
        printDebug("NORMALIZED INITIAL MODEL", initialModel);

        progressMonitor.subTask("Computing derivative model");
        printDebug("File ordering:", initialModel.getFiles());
        Model derivativeModel = new Model(vpg, initialModel.getFiles());
        printDebug("DERIVATIVE MODEL", derivativeModel);

        progressMonitor.subTask("Normalizing derivative model");
        derivativeModel.dnormalize(primitiveOps, preserveEdgeTypes);
        printDebug("NORMALIZED DERIVATIVE MODEL", derivativeModel);

        progressMonitor.subTask("Differencing initial and derivative models");
        ModelDiff diff = initialModel.compareAgainst(derivativeModel);

        describeDifferences(status, diff);

        leaveHypotheticalMode();
    }

    private void leaveHypotheticalMode() throws Error
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