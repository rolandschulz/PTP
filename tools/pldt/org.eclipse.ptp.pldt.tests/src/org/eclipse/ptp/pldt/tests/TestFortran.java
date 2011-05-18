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

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.pldt.internal.common.IDs;
import org.eclipse.ptp.pldt.mpi.core.actions.RunAnalyseMPIcommandHandler;
import org.eclipse.ptp.pldt.mpi.internal.core.MpiIDs;
import org.eclipse.ptp.pldt.openmp.core.actions.RunAnalyseOpenMPcommandHandler;
import org.eclipse.ptp.pldt.openmp.core.internal.OpenMPIDs;

/**
 * @author Beth Tibbitts
 * 
 */
public class TestFortran extends PldtBaseTestFramework {

	@SuppressWarnings("unused")
	public void testMPIartifacts_Fortran() throws Exception {
		System.out.println("\n==> " + getMethodName() + "()...");
		IFile file = importFile("resources", "HelloFortranMPI.f90");
		assertNotNull(file);
		// include file really not needed for Fortran but needed as arg to runResource
		IFile mpiInclude = importFile("resources/includes", "mpi.h");
		assertNotNull(mpiInclude);
		String tempPath = mpiInclude.getFullPath().toOSString();// includePath= /RegressionTestProject/mpi.h
		System.out.println("tempPath=" + tempPath);
		String includePath = mpiInclude.getWorkspace().getRoot().getRawLocation().toOSString()
				+ mpiInclude.getFullPath().toFile().getAbsolutePath();
		System.out.println("includePath= " + includePath);

		assertTrue(file instanceof IAdaptable);

		CoreModel.getDefault().getCModel().makeConsistent(new NullProgressMonitor());// jeff

		assertNotNull(cproject);
		ICElement ce = cproject.findElement(file.getFullPath());

		// ICElement covers folders and translationunits
		// final ICElement ce = (ICElement) ((IAdaptable)file).getAdapter(ICElement.class);
		assertNotNull(ce);
		List<String> includes = Arrays.asList(new String[] { includePath });
		RunAnalyseMPIcommandHandler racm = new RunAnalyseMPIcommandHandler();

		racm.runResource(new NullProgressMonitor(), ce, 0, includes);

		IMarker[] markers = file.findMarkers(MpiIDs.MARKER_ID, true, IResource.DEPTH_INFINITE);
		assertNotNull(markers);
		System.out.println("numMarkers: " + markers.length);
		String temp = "{";
		for (int i = 0; i < markers.length; i++) {
			IMarker marker = markers[i];
			int lineNo = (Integer) marker.getAttribute(IMarker.LINE_NUMBER);
			System.out.println(i + " marker: lineNo " + lineNo + " name: " + marker.getAttribute(IDs.NAME));
			temp += "\"" + marker.getAttribute(IDs.NAME) + "\",";

		}
		temp += "}";
		System.out.println(temp);

		assertEquals(23, markers.length);

		IMarker m = markers[0];
		String name = m.getAttribute(IDs.NAME).toString();
		for (int i = 0; i < markers.length; i++) {

		}

		int[] expectedLinenos = { 29, 29, 29, 44, 44, 44, 45, 63, 70, 73, 73, 76, 76, 82, 82, 83, 87, 87, 88, 96, 96, 96, 106 };
		String[] expectedMpiTypes = { "MPI_BCAST", "MPI_INTEGER",
				"MPI_COMM_WORLD", "MPI_REDUCE", "MPI_DOUBLE_PRECISION",
				"MPI_SUM", "MPI_COMM_WORLD", "MPI_STATUS_SIZE", "MPI_INIT",
				"MPI_COMM_RANK", "MPI_COMM_WORLD", "MPI_COMM_SIZE",
				"MPI_COMM_WORLD", "MPI_SEND", "MPI_CHARACTER",
				"MPI_COMM_WORLD", "MPI_RECV", "MPI_CHARACTER",
				"MPI_COMM_WORLD", "MPI_BCAST", "MPI_CHARACTER",
				"MPI_COMM_WORLD", "MPI_FINALIZE" };

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

	}

	public void testOpenMPartifacts_Fortran() throws Exception {
		System.out.println("\n==> " + getMethodName() + "()...");
		IFile file = importFile("resources", "openMP.f90");
		assertNotNull(file);
		// include file really not needed for Fortran but needed as arg to runResource
		IFile mpiInclude = importFile("resources/includes", "mpi.h");
		assertNotNull(mpiInclude);
		String tempPath = mpiInclude.getFullPath().toOSString();// includePath= /RegressionTestProject/mpi.h
		System.out.println("tempPath=" + tempPath);
		String includePath = mpiInclude.getWorkspace().getRoot().getRawLocation().toOSString()
				+ mpiInclude.getFullPath().toFile().getAbsolutePath();
		System.out.println("includePath= " + includePath);

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

		IMarker[] markers = file.findMarkers(OpenMPIDs.MARKER_ID, true, IResource.DEPTH_INFINITE);
		assertNotNull(markers);
		System.out.println("numMarkers: " + markers.length);
		for (int i = 0; i < markers.length; i++) {
			IMarker marker = markers[i];
			int lineNo = (Integer) marker.getAttribute(IMarker.LINE_NUMBER);
			System.out.println(i + " marker: lineNo " + lineNo + " name: " + marker.getAttribute(IDs.NAME));
		}
		// the only test of "correctness" now is the number of markers it finds
		int expectedNum = 4;
		System.out.println("found " + markers.length + " markers, expected " + expectedNum);
		assertEquals(expectedNum, markers.length);

		// See other tests for ideas on how to test more details in the markers found.

	}
}
