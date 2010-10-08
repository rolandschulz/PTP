/**********************************************************************
 * Copyright (c) 2007,2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.openmp.core.actions;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.pldt.common.Artifact;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.actions.RunAnalyseHandlerBase;
import org.eclipse.ptp.pldt.common.util.SourceInfo;
import org.eclipse.ptp.pldt.common.util.ViewActivator;
import org.eclipse.ptp.pldt.openmp.analysis.OpenMPAnalysisManager;
import org.eclipse.ptp.pldt.openmp.analysis.OpenMPError;
import org.eclipse.ptp.pldt.openmp.analysis.OpenMPErrorManager;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTNode;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTPragma;
import org.eclipse.ptp.pldt.openmp.core.OpenMPArtifactMarkingVisitor;
import org.eclipse.ptp.pldt.openmp.core.OpenMPPlugin;
import org.eclipse.ptp.pldt.openmp.core.OpenMPScanReturn;
import org.eclipse.ptp.pldt.openmp.core.analysis.OpenMPCASTVisitor;
import org.eclipse.ptp.pldt.openmp.core.messages.Messages;
import org.eclipse.ptp.pldt.openmp.internal.core.OpenMPIDs;
import org.eclipse.ptp.pldt.openmp.ui.pv.PvPlugin;
import org.eclipse.ptp.pldt.openmp.ui.pv.views.ProblemMarkerAttrIds;
import org.eclipse.ui.texteditor.MarkerUtilities;

/**
 * @author tibbitts
 * @since 4.0
 * 
 */
public class RunAnalyseOpenMPcommandHandler extends RunAnalyseHandlerBase {
	private static final String OPENMP_DIRECTIVE = Messages.RunAnalyseOpenMPcommandHandler_OpenMP_directive;
	private static final boolean traceOn = false;

	/**
	 * Constructor for the "Run Analysis" action
	 */
	public RunAnalyseOpenMPcommandHandler() {
		super("OpenMP", new OpenMPArtifactMarkingVisitor(OpenMPPlugin.MARKER_ID), OpenMPPlugin.MARKER_ID); //$NON-NLS-1$
	}

	/**
	 * Returns OpenMP analysis artifacts for file
	 * 
	 * @param file
	 * @param includes
	 *            OpenMP include paths
	 * @return
	 */
	@Override
	public ScanReturn doArtifactAnalysis(final ITranslationUnit tu, final List<String> includes) {
		OpenMPScanReturn msr = new OpenMPScanReturn();
		final String fileName = tu.getElementName();
		IASTTranslationUnit atu = null;
		ILanguage lang;
		boolean allowPrefixOnlyMatch=OpenMPPlugin.getDefault().getPreferenceStore().getBoolean(OpenMPIDs.OPENMP_RECOGNIZE_APIS_BY_PREFIX_ALONE);
		try {
			lang = tu.getLanguage();

			atu = tu.getAST();
			String languageID = lang.getId();
			
			if(languageID.equals(GCCLanguage.ID) || languageID.equals(GPPLanguage.ID)) {
				// null IASTTranslationUnit when we're doing C/C++ means we should quit.
				// but want to continue to see if this is a fortran file we are analyzing.
				if(atu==null) {// this is null for Fortran file during JUnit testing.
					System.out.println("RunAnalyseOpenMPCommandHandler.doArtifactAnalysis(), atu is null (testing?)");
					return msr;
				}
			}
			
			if (languageID.equals(GCCLanguage.ID)) {// cdt40
				atu.accept(new OpenMPCASTVisitor(includes, fileName, allowPrefixOnlyMatch, msr));
			} else {
				// Attempt to handle Fortran
				// Instantiate using reflection to avoid static Photran
				// dependencies
				try {
					Class<?> c = Class.forName("org.eclipse.ptp.pldt.openmp.core.actions.AnalyseOpenMPFortranHandler"); //$NON-NLS-1$
					Method method = c.getMethod("run", String.class, ITranslationUnit.class, String.class, ScanReturn.class); //$NON-NLS-1$
					method.invoke(c.newInstance(), languageID, tu, fileName, msr);
				} catch (Exception e) {
					System.err.println("RunAnalyseOpenMPcommandHandler.doArtifactAnalysis: Photran not installed"); //$NON-NLS-1$
				}
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		IResource res = tu.getResource();
		IFile file = null;
		if (res instanceof IFile) {
			file = (IFile) res;
		} else {
			System.out.println("RunAnalyseOpenMP.doArtifactAnalysis, file cast won't work..."); //$NON-NLS-1$
		}
		// Find the OpenMP #pragmas
		if (atu != null) { // not for Fortran
			processOpenMPPragmas(msr, atu, file);
		}
		return msr;
	}

	/**
	 * Special processing to find #pragmas, since the CDT AST does not normally
	 * include them.<br>
	 * Also adds the "OpenMP Problems"
	 * 
	 * @param msr
	 * @param astTransUnit
	 * @param iFile
	 */
	protected void processOpenMPPragmas(OpenMPScanReturn msr, IASTTranslationUnit astTransUnit, IFile iFile) {
		OpenMPAnalysisManager omgr = new OpenMPAnalysisManager(astTransUnit, iFile);
		PASTNode[] pList = omgr.getPAST();

		for (int i = 0; i < pList.length; i++) {// length local=3271; remote 4 (!!)
			PASTNode temp=pList[i];
			String tempStr=temp.getRawSignature();
			// local: will be a PASTOMPPragma node;   remote: will be a PASTPragma node.
			// So workaround is to accept a PASTPragma node here so we can handle remote files.
			// Need to investigate what this does to further analysis e.g. concurrency analysis.
			if (pList[i] instanceof PASTPragma) {// was PASTOMPPragma

				PASTPragma pop = (PASTPragma) pList[i];
				if (traceOn)
					System.out.println("found #pragma, line " + pop.getStartingLine()); //$NON-NLS-1$
				SourceInfo si = getSourceInfo(pop, Artifact.PRAGMA);
				String shortName=pop.getContent();
				if(shortName.length()==0) { 
					shortName="#pragma"; // HACK: workaround for remote files where getContent() is always empty.
					// The same reason why this is empty is also (I think) why it's not a PASTOMPPragma node.
					// PASTOMPFactory.parse() always finds empty token first on a remote file, so aborts.
				}
				Artifact a = new Artifact(pop.getFilename(), pop.getStartingLine(), pop.getStartLocation(), shortName,
						OPENMP_DIRECTIVE, si, pop);
				msr.addArtifact(a);
			}
		}

		msr.addProblems(OpenMPErrorManager.getCurrentErrorManager().getErrors());
	}

	/**
	 * Get exact source locational info for a function call
	 * 
	 * @param pastNode
	 * @param constructType
	 * @return
	 */
	private SourceInfo getSourceInfo(PASTNode pastNode, int constructType) {
		SourceInfo sourceInfo = null;
		IASTNodeLocation[] locations = pastNode.getNodeLocations();
		if (locations.length == 1) {
			IASTFileLocation astFileLocation = null;
			if (locations[0] instanceof IASTFileLocation) {
				astFileLocation = (IASTFileLocation) locations[0];
				sourceInfo = new SourceInfo();
				sourceInfo.setStartingLine(astFileLocation.getStartingLineNumber());
				sourceInfo.setStart(astFileLocation.getNodeOffset());
				sourceInfo.setEnd(astFileLocation.getNodeOffset() + astFileLocation.getNodeLength());
				sourceInfo.setConstructType(constructType);
			}
		}
		return sourceInfo;
	}

	/**
	 * processResults - override from RunAnalyse base, to process both pragma
	 * artifacts and problems
	 */
	@Override
	protected void processResults(ScanReturn results, IResource resource) {
		assert (results instanceof OpenMPScanReturn);

		OpenMPScanReturn osr = (OpenMPScanReturn) results;

		// This is for the openmp pragma view
		List<Artifact> artifacts = osr.getOpenMPList();
		visitor.visitFile(resource, artifacts);

		// remove problems
		removeProblemMarkers(resource);

		// DPP - put in stuff for problems view
		// Just subclass scanreturn and create markers for problems view here
		List<OpenMPError> problems = osr.getProblems();
		if (traceOn)
			System.out.println("RunAnalyseOpenMP.processResults, have " + problems.size() + " problems."); //$NON-NLS-1$ //$NON-NLS-2$
		try {
			for (Iterator<OpenMPError> i = problems.iterator(); i.hasNext();)
				processProblem(i.next(), resource);
		} catch (CoreException e) {
			System.out.println("RunAnalysisOpenMP.processResults exception: " //$NON-NLS-1$
					+ e);
			e.printStackTrace();
		}
	}

	/**
	 * Create problem marker which will put a problem on the OpenMP problems
	 * view
	 * 
	 * @param problem
	 *            - OpenMPError
	 * @param resource
	 *            - IResource
	 * @throws CoreException
	 */
	private void processProblem(OpenMPError problem, IResource resource) throws CoreException {
		// build all the attributes
		Map attrs = new HashMap();
		attrs.put(ProblemMarkerAttrIds.DESCRIPTION, problem.getDescription());
		attrs.put(ProblemMarkerAttrIds.RESOURCE, problem.getFilename());
		attrs.put(ProblemMarkerAttrIds.INFOLDER, problem.getPath());
		attrs.put(ProblemMarkerAttrIds.LOCATION, new Integer(problem.getLineno()));
		// used to reference problem if need
		attrs.put(ProblemMarkerAttrIds.PROBLEMOBJECT, problem);

		// create the marker all at once, so get ONLY a single resourceChange
		// event.
		MarkerUtilities.createMarker(resource, attrs, ProblemMarkerAttrIds.MARKER_ERROR_ID);

	}

	/**
	 * Remove the OpenMP problem markers currently set on a resource.
	 * 
	 * @param resource
	 *            - IResource
	 */
	private void removeProblemMarkers(IResource resource) {
		try {
			resource.deleteMarkers(ProblemMarkerAttrIds.MARKER_ERROR_ID, false, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			System.out.println(e);
			System.out.println(e.toString());
			System.out.println("Problem deleting markers on OMP Problems: " //$NON-NLS-1$
					+ resource.getProjectRelativePath());
		}
	}

	@Override
	protected List<String> getIncludePath() {
		return OpenMPPlugin.getDefault().getIncludeDirs();
	}

	@Override
	protected void activateArtifactView() {
		ViewActivator.activateView(OpenMPPlugin.VIEW_ID);
	}

	@Override
	protected void activateProblemsView() {
		ViewActivator.activateView(PvPlugin.VIEW_ID);
	}

}
