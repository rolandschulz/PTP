/**********************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.openmp.core.actions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.pldt.common.Artifact;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.actions.RunAnalyseHandlerBase;
import org.eclipse.ptp.pldt.common.util.SourceInfo;
import org.eclipse.ptp.pldt.common.util.ViewActivater;
import org.eclipse.ptp.pldt.openmp.analysis.OpenMPAnalysisManager;
import org.eclipse.ptp.pldt.openmp.analysis.OpenMPError;
import org.eclipse.ptp.pldt.openmp.analysis.OpenMPErrorManager;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTNode;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTOMPPragma;
import org.eclipse.ptp.pldt.openmp.core.OpenMPArtifactMarkingVisitor;
import org.eclipse.ptp.pldt.openmp.core.OpenMPPlugin;
import org.eclipse.ptp.pldt.openmp.core.OpenMPScanReturn;
import org.eclipse.ptp.pldt.openmp.core.analysis.OpenMPCASTVisitor;
import org.eclipse.ptp.pldt.openmp.ui.pv.PvPlugin;
import org.eclipse.ptp.pldt.openmp.ui.pv.views.ProblemMarkerAttrIds;
import org.eclipse.ui.texteditor.MarkerUtilities;

/**
 * @author tibbitts
 *
 */
public class RunAnalyseOpenMPcommandHandler extends RunAnalyseHandlerBase {
	private static final String OPENMP_DIRECTIVE = "OpenMP directive";
	private static final boolean traceOn=false;
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
	public ScanReturn doArtifactAnalysis(final ITranslationUnit tu,
			final List<String> includes) {
		OpenMPScanReturn msr = new OpenMPScanReturn();
		final String fileName = tu.getElementName();
		IASTTranslationUnit atu = null;
		ILanguage lang;
		try {
			lang = tu.getLanguage();

			atu = tu.getAST();
			if (lang.getId().equals(GCCLanguage.ID)) {// cdt40
				atu.accept(new OpenMPCASTVisitor(includes, fileName, msr));
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		IResource res = tu.getResource();
		IFile file = null;
		if (res instanceof IFile) {
			file = (IFile) res;
		}
		else{ 
			System.out.println("RunAnalyseOpenMP.doArtifactAnalysis, file cast won't work...");
		}
		// Find the OpenMP #pragmas
		processOpenMPPragmas(msr, atu, file);
		return msr;
	}
	/**
	 * Special processing to find #pragmas, since the CDT AST
	 * does not normally include them.<br>
	 * Also adds the "OpenMP Problems"  
	 * @param msr
	 * @param astTransUnit
	 * @param iFile
	 */
	protected void processOpenMPPragmas(OpenMPScanReturn msr,
			IASTTranslationUnit astTransUnit, IFile iFile) {
		OpenMPAnalysisManager omgr = new OpenMPAnalysisManager(astTransUnit,
				iFile);
		PASTNode[] pList = omgr.getPAST();

		for (int i = 0; i < pList.length; i++) {
			if (pList[i] instanceof PASTOMPPragma) {
				PASTOMPPragma pop = (PASTOMPPragma) pList[i];
				SourceInfo si = getSourceInfo(pop, 0);
				Artifact a = new Artifact(pop.getFilename(), pop
						.getStartingLine(), pop.getStartLocation(), pop
						.getContent(), OPENMP_DIRECTIVE, si, pop);
				msr.addArtifact(a);
			}
		}

		msr.addProblems(OpenMPErrorManager.getCurrentErrorManager()
						.getErrors());
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
				sourceInfo.setStartingLine(astFileLocation
						.getStartingLineNumber());
				sourceInfo.setStart(astFileLocation.getNodeOffset());
				sourceInfo.setEnd(astFileLocation.getNodeOffset()
						+ astFileLocation.getNodeLength());
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
		if(traceOn)System.out.println("RunAnalyseOpenMP.processResults, have "+problems.size()+ " problems.");
		try {
			for (Iterator<OpenMPError> i = problems.iterator(); i.hasNext();)
				processProblem((OpenMPError) i.next(), resource);
		} catch (CoreException e) {
			System.out.println("RunAnalysisOpenMP.processResults exception: "
					+ e);
			e.printStackTrace();
		}
	}
	/**
	 * Create problem marker which will put a problem on the OpenMP problems view
	 * 
	 * @param problem -
	 *            OpenMPError
	 * @param resource -
	 *            IResource
	 * @throws CoreException
	 */
	private void processProblem(OpenMPError problem, IResource resource)
			throws CoreException {
		// build all the attributes
		Map attrs = new HashMap();
		attrs.put(ProblemMarkerAttrIds.DESCRIPTION, problem.getDescription());
		attrs.put(ProblemMarkerAttrIds.RESOURCE, problem.getFilename());
		attrs.put(ProblemMarkerAttrIds.INFOLDER, problem.getPath());
		attrs.put(ProblemMarkerAttrIds.LOCATION, new Integer(problem
				.getLineno()));
		// used to reference problem if need
		attrs.put(ProblemMarkerAttrIds.PROBLEMOBJECT, problem);

		// create the marker all at once, so get ONLY a single resourceChange
		// event.
		MarkerUtilities.createMarker(resource, attrs,
				ProblemMarkerAttrIds.MARKER_ERROR_ID);

	}
	/**
	 * Remove the OpenMP problem markers currently set on a resource.
	 * 
	 * @param resource -
	 *            IResource
	 */
	private void removeProblemMarkers(IResource resource) {
		try {
			resource.deleteMarkers(ProblemMarkerAttrIds.MARKER_ERROR_ID, false,
					IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			System.out.println(e);
			System.out.println(e.toString());
			System.out.println("Problem deleting markers on OMP Problems: "
					+ resource.getProjectRelativePath());
		}
	}


	protected List<String> getIncludePath() {
		return OpenMPPlugin.getDefault().getIncludeDirs();
	}

	protected void activateArtifactView() {
		ViewActivater.activateView(OpenMPPlugin.VIEW_ID);
	}

	protected void activateProblemsView() {
		ViewActivater.activateView(PvPlugin.VIEW_ID);
	}	
	 

}
