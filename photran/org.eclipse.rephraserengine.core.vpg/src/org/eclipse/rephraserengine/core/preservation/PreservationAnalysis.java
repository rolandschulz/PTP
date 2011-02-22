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

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ltk.core.refactoring.FileStatusContext;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.rephraserengine.core.preservation.ModelDiff.EdgeAdded;
import org.eclipse.rephraserengine.core.preservation.ModelDiff.EdgeDeleted;
import org.eclipse.rephraserengine.core.preservation.ModelDiff.EdgeSinkChanged;
import org.eclipse.rephraserengine.core.preservation.ModelDiff.ModelDiffProcessor;
import org.eclipse.rephraserengine.core.util.OffsetLength;
import org.eclipse.rephraserengine.core.vpg.eclipse.EclipseVPG;

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
    private static final String SEPARATOR = ","; //$NON-NLS-1$
    
    private IAdapterManager adapterManager;

    private EclipseVPG vpg;
    //private IProgressMonitor progressMonitor;

    private Model initialModel;
    private ReplacementList replacements;
    private PreservationRuleset ruleset;
    
    private String fields;
    private String times;

    /** @since 3.0 */
    public PreservationAnalysis(
        EclipseVPG vpg,
        IProgressMonitor progressMonitor, int ticks,
        IFile file,
        PreservationRuleset ruleset)
    {
        this(vpg, progressMonitor, ticks, EclipseVPG.getFilenameForIFile(file), ruleset);
    }

    /** @since 3.0 */
    public PreservationAnalysis(
        EclipseVPG vpg,
        IProgressMonitor progressMonitor, int ticks,
        Collection<IFile> files,
        PreservationRuleset ruleset)
    {
        this(vpg, progressMonitor, ticks, getFilenames(files), ruleset);
    }

    /** @since 3.0 */
    public PreservationAnalysis(
        EclipseVPG vpg,
        IProgressMonitor progressMonitor, int ticks,
        String filename,
        PreservationRuleset ruleset)
    {
        this(vpg, progressMonitor, ticks, Collections.singletonList(filename), ruleset);
    }

    /** @since 3.0 */
    public PreservationAnalysis(
        EclipseVPG vpg,
        IProgressMonitor progressMonitor, int ticks,
        List<String> filenames,
        PreservationRuleset ruleset)
    {
        this.adapterManager = Platform.getAdapterManager();
        this.vpg = vpg;
        //this.progressMonitor = progressMonitor;

        this.replacements = new ReplacementList();
        this.ruleset = ruleset;

        this.fields = "Filenames"; //$NON-NLS-1$
        this.times = filenames.toString();

        progressMonitor.subTask(Messages.PreservationAnalysis_EnteringHypotheticalMode);
        long start = System.currentTimeMillis();
        ensureDatabaseIsInHypotheticalMode();
        this.fields += SEPARATOR + "Hypothetical"; //$NON-NLS-1$
        this.times += SEPARATOR + (System.currentTimeMillis()-start);

        start = System.currentTimeMillis();
        this.initialModel = new Model("initial model", progressMonitor, ticks, vpg, filenames); //$NON-NLS-1$
        this.fields += SEPARATOR + "Init Model"; //$NON-NLS-1$
        this.times += SEPARATOR + (System.currentTimeMillis()-start);
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
        if (!vpg.isInHypotheticalMode())
        {
            try
            {
                vpg.enterHypotheticalMode();
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

        Replacement alpha = new Replacement(
            filename,
            offsetLength.getOffset(),
            0,
            offsetLength.getLength());

        replacements.add(alpha);
        adapterManager.getAdapter(node, ResetOffsetLength.class);
    }

    public void markEpsilon(IFile file, Object node)
    {
        String filename = EclipseVPG.getFilenameForIFile(file);
        OffsetLength offsetLength = (OffsetLength)adapterManager.getAdapter(node, OffsetLength.class);
        if (offsetLength == null)
            throw new Error("Unable to get OffsetLength adapter for " + node.getClass().getName()); //$NON-NLS-1$

        Replacement epsilon = new Replacement(
            filename,
            offsetLength.getOffset(),
            offsetLength.getLength(),
            0);

        replacements.add(epsilon);
    }

    public void markRho(IFile file, Object node, int oldLength, int newLength)
    {
        String filename = EclipseVPG.getFilenameForIFile(file);
        OffsetLength offsetLength = (OffsetLength)adapterManager.getAdapter(node, OffsetLength.class);
        if (offsetLength == null)
            throw new Error("Unable to get OffsetLength adapter for " + node.getClass().getName()); //$NON-NLS-1$

        Replacement rho = new Replacement(
            filename,
            offsetLength.getOffset(),
            oldLength,
            newLength);

        replacements.add(rho);
    }

//    /**
//     * @since 3.0
//     */
//    public void markMu(IFile file, Object oldNode, Object newNode)
//    {
//        String filename = EclipseVPG.getFilenameForIFile(file);
//
//        OffsetLength oldOffsetLength = (OffsetLength)adapterManager.getAdapter(oldNode, OffsetLength.class);
//        if (oldOffsetLength == null)
//            throw new Error("Unable to get OffsetLength adapter for " + oldNode.getClass().getName()); //$NON-NLS-1$
//
//        OffsetLength newOffsetLength = (OffsetLength)adapterManager.getAdapter(newNode, OffsetLength.class);
//        if (newOffsetLength == null)
//            throw new Error("Unable to get OffsetLength adapter for " + newNode.getClass().getName()); //$NON-NLS-1$
//
//        Mu mu = PrimitiveOp.mu(
//            filename,
//            oldOffsetLength,
//            newOffsetLength);
//
//        replacements.add(mu);
//    }
//
//    /**
//     * @since 3.0
//     */
//    public void markMu(IFile file, OffsetLength oldOffsetLength, OffsetLength newOffsetLength)
//    {
//        String filename = EclipseVPG.getFilenameForIFile(file);
//
//        Mu mu = PrimitiveOp.mu(
//            filename,
//            oldOffsetLength,
//            newOffsetLength);
//
//        replacements.add(mu);
//    }

    @Override public String toString()
    {
        return replacements.toString();
    }

    public void checkForPreservation(
        RefactoringStatus status,
        IProgressMonitor progressMonitor, int ticks)
    {
        printDebug("INITIAL MODEL", initialModel); //$NON-NLS-1$
        printDebug("NORMALIZING RELATIVE TO", replacements); //$NON-NLS-1$

        long start = System.currentTimeMillis();
        initialModel.inormalize(replacements, progressMonitor);
        this.fields += SEPARATOR + "I-Normalize"; //$NON-NLS-1$
        this.times += SEPARATOR + (System.currentTimeMillis()-start);
        printDebug("NORMALIZED INITIAL MODEL", initialModel); //$NON-NLS-1$

        printDebug("File ordering:", initialModel.getFiles()); //$NON-NLS-1$
        start = System.currentTimeMillis();
        Model derivativeModel = new Model(
            "derivative model", //$NON-NLS-1$
            progressMonitor, ticks+3,
            vpg,
            initialModel.getFiles());
        this.fields += SEPARATOR + "D-Model"; //$NON-NLS-1$
        this.times += SEPARATOR + (System.currentTimeMillis()-start);
        printDebug("DERIVATIVE MODEL", derivativeModel); //$NON-NLS-1$

        start = System.currentTimeMillis();
        derivativeModel.dnormalize(replacements, progressMonitor);
        this.fields += SEPARATOR + "D-Normalize"; //$NON-NLS-1$
        this.times += SEPARATOR + (System.currentTimeMillis()-start);
        printDebug("NORMALIZED DERIVATIVE MODEL", derivativeModel); //$NON-NLS-1$

        start = System.currentTimeMillis();
        ModelDiff diff = initialModel.checkPreservation(derivativeModel, ruleset, progressMonitor);
        this.fields += SEPARATOR + "Preservation"; //$NON-NLS-1$
        this.times += SEPARATOR + (System.currentTimeMillis()-start);

        start = System.currentTimeMillis();
        describeDifferences(status, diff);
        this.fields += SEPARATOR + "Desc Diff"; //$NON-NLS-1$
        this.times += SEPARATOR + (System.currentTimeMillis()-start);

        start = System.currentTimeMillis();
        leaveHypotheticalMode(progressMonitor);
        this.fields += SEPARATOR + "Leave Hypoth"; //$NON-NLS-1$
        this.times += SEPARATOR + (System.currentTimeMillis()-start);
        
        logTimes();
    }

    private void logTimes()
    {
        try
        {
            FileWriter output = new FileWriter("/Users/joverbey/Desktop/times.csv", true); //$NON-NLS-1$
            output.write(fields + "\n"); //$NON-NLS-1$
            output.write(times + "\n"); //$NON-NLS-1$
            output.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void leaveHypotheticalMode(IProgressMonitor progressMonitor) throws Error
    {
        try
        {
            progressMonitor.subTask(Messages.PreservationAnalysis_ExitingHypotheticalMode);
            vpg.leaveHypotheticalMode();
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
                        vpg.describeEdgeType(addition.edge.getType()).toLowerCase(),
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
                        vpg.describeEdgeType(deletion.edge.getType()).toLowerCase(),
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
                        vpg.describeEdgeType(change.edge.getType()).toLowerCase(),
                        change);
                status.addError(msg);

                status.addError(
                    Messages.bind(
                        Messages.PreservationAnalysis_EdgeWillChange,
                        vpg.describeEdgeType(change.edge.getType()).toLowerCase()),
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
    
    /** @since 3.0 */
    public static boolean printModelOn(PrintStream ps, IFile file, EclipseVPG<?,?,?> vpg) throws UnsupportedEncodingException, IOException, CoreException
    {
        String filename = EclipseVPG.getFilenameForIFile(file);
        if (filename == null) return false;
        
        ArrayList<Integer> lineMap = new ArrayList<Integer>();
        String fileContents = readStream(lineMap,
            new BufferedReader(new InputStreamReader(file.getContents(true), file.getCharset())));
        ps.println(filename);
        ps.println();
        Model model = new Model("edge model", new NullProgressMonitor(), 0, vpg, filename); //$NON-NLS-1$
        ps.print(model.toString(filename, fileContents, lineMap));
        return true;
    }

    private static String readStream(ArrayList<Integer> lineMap, Reader in) throws IOException
    {
        StringBuffer sb = new StringBuffer(4096);
        for (int offset = 0, ch = in.read(); ch >= 0; ch = in.read())
        {
            sb.append((char)ch);
            offset++;

            if (ch == '\n' && lineMap != null)
            {
                //System.out.println("Line " + (lineMap.size()+1) + " starts at offset " + offset);
                lineMap.add(offset);
            }
        }
        in.close();
        return sb.toString();
}
}