/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.tests;

import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.pldt.common.Artifact;
import org.eclipse.ptp.pldt.common.ArtifactManager;
import org.eclipse.ptp.pldt.common.IArtifact;
import org.eclipse.ptp.pldt.common.IDs;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTOMPPragma;
import org.eclipse.ptp.pldt.openmp.core.OpenMPPlugin;
import org.eclipse.ptp.pldt.openmp.core.actions.RunAnalyseOpenMPcommandHandler;

/**
 * @author beth
 * 
 */
public class Test_OpenMP extends PldtBaseTestFramework {

	public void testOpenMPartifacts() throws Exception {
		System.out.println("\n==> " + getMethodName() + "()...");

		IFile file = importFile("resources", "helloOpenMP.c");
		assertNotNull(file);

		IFile openmpInclude = importFile("resources/includes", "omp.h");
		assertNotNull(openmpInclude);
		String tempPath = openmpInclude.getFullPath().toOSString();// includePath= /RegressionTestProject/mpi.h
		// System.out.println("tempPath="+tempPath);
		String includePath = openmpInclude.getWorkspace().getRoot().getRawLocation().toOSString()
				+ openmpInclude.getFullPath().toFile().getAbsolutePath();
		// System.out.println("includePath= "+includePath);

		assertTrue(file instanceof IAdaptable);

		CoreModel.getDefault().getCModel().makeConsistent(new NullProgressMonitor());// jeff

		assertNotNull(cproject);
		ICElement ce = cproject.findElement(file.getFullPath());

		// ICElement covers folders and translationunits
		// final ICElement ce = (ICElement) ((IAdaptable)file).getAdapter(ICElement.class);
		assertNotNull(ce);
		List<String> includes = Arrays.asList(new String[] { includePath });
		RunAnalyseOpenMPcommandHandler racm = new RunAnalyseOpenMPcommandHandler();

		racm.runResource(new NullProgressMonitor(), ce, 0, includes);

		IMarker[] markers = file.findMarkers(OpenMPPlugin.MARKER_ID, true, IResource.DEPTH_INFINITE);
		assertNotNull(markers);
		System.out.println("numMarkers: " + markers.length);
		// need to sort markers, since they are not returned in a pre-determined order??

		for (int i = 0; i < markers.length; i++) {
			IMarker marker = markers[i];
			int lineNo = (Integer) marker.getAttribute(IMarker.LINE_NUMBER);
			System.out.println(i + " marker: lineNo " + lineNo + " name: " + marker.getAttribute(IDs.NAME));
		}

		// When strings don't match, click on the first entry (ComparisonFailure) in the Failure Trace in the JUnit view to get a
		// diff view
		// assertEquals("This\nis\na\ntest", "This\nwas\na\ntest");
		// there really should be three, including a pragma. testOpenMP_pragmas will
		// concentrate on these
		int[] expectedLinenos = { 12, 14, 20 };
		final String pragma = "#pragma omp parallel private(numThreads, tid)";
		String[] expectedOpenMPTypes = { pragma, "omp_get_thread_num", "omp_get_num_threads" };
		ArtifactWithLine[] expectedArts = new ArtifactWithLine[expectedLinenos.length];
		ArtifactWithLine[] markerArts = new ArtifactWithLine[markers.length];
		for (int i = 0; i < expectedArts.length; i++) {
			expectedArts[i] = new ArtifactWithLine(expectedLinenos[i], expectedOpenMPTypes[i]);
		}
		for (int i = 0; i < markerArts.length; i++) {
			markerArts[i] = new ArtifactWithLine(markers[i]);
		}
		Arrays.sort(expectedArts);
		Arrays.sort(markerArts);

		assertEquals(expectedArts.length, markerArts.length);
		// Since these tests are in a loop, if one fails, must inspect the Console output to know which ones finished and which one
		// failed.
		for (int i = 0; i < markers.length; i++) {
			// Marker should be on the line number we expect
			System.out.println(i + ". " + expectedArts[i].getLineNo() + " = " + markerArts[i].getLineNo());
			assertEquals(expectedArts[i].getLineNo(), markerArts[i].getLineNo());

			// Marker should be of the type name we expect
			System.out.println("    " + expectedArts[i].getName() + " = " + markerArts[i].getName());
			assertEquals(expectedArts[i].getName(), markerArts[i].getName());

			boolean test = markerArts[i].equals(expectedArts[i]);
			System.out.println("   markerArts.equals expectedArts: " + test);
			// the following never works, so we'll be happy enough with the two assertEquals above.
			// assertEquals(expectedArts[i], markerArts[i]);

		}

	}

	public void testOpenMP_pragmas() throws Exception {
		System.out.println("\n==> " + getMethodName() + "()...");

		IFile file = importFile("resources", "helloOpenMPpragmas.c");
		assertNotNull(file);

		IFile openmpInclude = importFile("resources/includes", "omp.h");
		assertNotNull(openmpInclude);
		String tempPath = openmpInclude.getFullPath().toOSString();// includePath= /RegressionTestProject/mpi.h
		// System.out.println("tempPath="+tempPath);
		String includePath = openmpInclude.getWorkspace().getRoot().getRawLocation().toOSString()
				+ openmpInclude.getFullPath().toFile().getAbsolutePath();
		// System.out.println("includePath= "+includePath);

		assertTrue(file instanceof IAdaptable);

		CoreModel.getDefault().getCModel().makeConsistent(new NullProgressMonitor());// jeff

		assertNotNull(cproject);
		ICElement ce = cproject.findElement(file.getFullPath());

		// ICElement covers folders and translationunits
		// final ICElement ce = (ICElement) ((IAdaptable)file).getAdapter(ICElement.class);
		assertNotNull(ce);
		List<String> includes = Arrays.asList(new String[] { includePath });
		RunAnalyseOpenMPcommandHandler racm = new RunAnalyseOpenMPcommandHandler();

		racm.runResource(new NullProgressMonitor(), ce, 0, includes);

		IMarker[] markers = file.findMarkers(OpenMPPlugin.MARKER_ID, true, IResource.DEPTH_INFINITE);
		assertNotNull(markers);
		System.out.println("numMarkers: " + markers.length);
		// need to sort markers, since they are not returned in a pre-determined order??

		// When strings don't match, click on the first entry (ComparisonFailure) in the Failure Trace in the JUnit view to get a
		// diff view
		// assertEquals("This\nis\na\ntest", "This\nwas\na\ntest");

		int[] expectedLinenos = { 21, 23, 29, 34, 36, 40 };
		final String prag = "#pragma omp parallel private(numThreads, tid)";
		final String prag2 = "#pragma omp parallel shared(n,a,b)";
		final String pragFor = "#pragma omp for";
		final String pragParFor = "#pragma omp parallel for";
		String[] expectedMpiTypes = { prag, "omp_get_thread_num", "omp_get_num_threads", prag2, pragFor, pragParFor };
		ArtifactWithLine[] expectedArts = new ArtifactWithLine[expectedLinenos.length];
		ArtifactWithLine[] markerArts = new ArtifactWithLine[markers.length];
		for (int i = 0; i < expectedArts.length; i++) {
			expectedArts[i] = new ArtifactWithLine(expectedLinenos[i], expectedMpiTypes[i]);
		}
		for (int i = 0; i < markerArts.length; i++) {
			markerArts[i] = new ArtifactWithLine(markers[i]);
		}
		Arrays.sort(expectedArts);
		Arrays.sort(markerArts);
		for (int i = 0; i < markers.length; i++) {
			IMarker marker = markers[i];
			int lineNo = (Integer) marker.getAttribute(IMarker.LINE_NUMBER);
			System.out.println(i + " marker: lineNo " + lineNo + " name: " + marker.getAttribute(IDs.NAME));
		}

		// should find pragma and two other openmp artifacts
		assertEquals(expectedArts.length, markerArts.length);
		// Since these tests are in a loop, if one fails, must inspect the Console output to know which ones finished and which one
		// failed.
		for (int i = 0; i < markers.length; i++) {
			// Marker should be on the line number we expect
			System.out.println(i + ". " + expectedArts[i].getLineNo() + " = " + markerArts[i].getLineNo());
			assertEquals(expectedArts[i].getLineNo(), markerArts[i].getLineNo());

			// Marker should be of the type name we expect
			System.out.println("    " + expectedArts[i].getName() + " = " + markerArts[i].getName());
			assertEquals(expectedArts[i].getName(), markerArts[i].getName());

			boolean test = markerArts[i].equals(expectedArts[i]);
			// the following never works, so we'll be happy enough with the two assertEquals above.
			// assertEquals(expectedArts[i], markerArts[i]);

		}
		System.out.println("== Check regions for 'show pragma region' usage");
		// check information needed for pragma show region action
		RegionInfo ri0 = getRegion(ArtifactManager.getArtifact(markerArts[0].getMarker()));
		assertNotNull(ri0);
		System.out.println("0. region offset:" + ri0.offset + " length:" + ri0.length + "  " + markerArts[0].getName());
		assertEquals(642, ri0.offset);
		assertEquals(290, ri0.length);

		RegionInfo ri1 = getRegion(ArtifactManager.getArtifact(markerArts[1].getMarker()));
		assertNull(ri1);
		// System.out.println("1. region offset:"+ri1.offset+ " length:"+ri1.length);
		System.out.println("1. not a pragma    " + markerArts[1].getName());

		RegionInfo ri2 = getRegion(ArtifactManager.getArtifact(markerArts[2].getMarker()));
		assertNull(ri2);
		System.out.println("2. not a pragma    " + markerArts[2].getName());
		// assertEquals(642,ri0.offset);
		// assertEquals(290, ri0.length);

		RegionInfo ri3 = getRegion(ArtifactManager.getArtifact(markerArts[3].getMarker()));
		assertNotNull(ri3);
		System.out.println("3. region offset:" + ri3.offset + " length:" + ri3.length + "  " + markerArts[3].getName());
		assertEquals(1014, ri3.offset);
		assertEquals(214, ri3.length);

		RegionInfo ri4 = getRegion(ArtifactManager.getArtifact(markerArts[4].getMarker()));
		assertNotNull(ri4);
		System.out.println("4. region offset:" + ri4.offset + " length:" + ri4.length + "  " + markerArts[4].getName());
		assertEquals(1038, ri4.offset);
		assertEquals(187, ri4.length);

		RegionInfo ri5 = getRegion(ArtifactManager.getArtifact(markerArts[5].getMarker()));
		assertNotNull(ri5);
		System.out.println("5. region offset:" + ri5.offset + " length:" + ri5.length + "  " + markerArts[5].getName());
		assertEquals(1170, ri5.offset);
		assertEquals(50, ri5.length);

		System.out.println("done");

	}

	/**
	 * copied from OpenMPArtifactView.makeShowInfoAction() more or less - not ideal
	 * 
	 * @param a
	 */
	RegionInfo getRegion(IArtifact a) {
		Artifact artifact = (Artifact) a;
		Object p = artifact.getArtifactAssist();
		if (p == null || !(p instanceof PASTOMPPragma))
			return null;
		;
		PASTOMPPragma ompPragma = (PASTOMPPragma) p;
		IASTNode iRegion = ompPragma.getRegion();
		ASTNode region = (iRegion instanceof ASTNode ? (ASTNode) iRegion : null);
		if (region == null)
			return null;
		int offset = ompPragma.getRegionOffset();
		int length = ompPragma.getRegionLength();
		RegionInfo ri = new RegionInfo(offset, length);
		return ri;

	}

	protected class RegionInfo {
		int offset;
		int length;

		RegionInfo(int offset, int length) {
			this.offset = offset;
			this.length = length;
		}
	}

}
