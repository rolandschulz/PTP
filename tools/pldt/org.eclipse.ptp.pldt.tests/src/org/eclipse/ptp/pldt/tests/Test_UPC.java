/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation
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
import org.eclipse.ptp.pldt.common.IDs;
import org.eclipse.ptp.pldt.mpi.core.MpiIDs;
import org.eclipse.ptp.pldt.mpi.core.MpiPlugin;
import org.eclipse.ptp.pldt.upc.UPCIDs;
import org.eclipse.ptp.pldt.upc.actions.RunAnalyseUPCcommandHandler;

/**
 * @author beth
 * 
 */
public class Test_UPC extends PldtBaseTestFramework {

	public void testUPCartifacts() throws Exception {
		System.out.println("\n==> " + getMethodName() + "()...");

		IFile file = importFile("resources", "testUPC.upc");
		assertNotNull(file);

		IFile upcInclude = importFile("resources/includes", "upc.h");
		assertNotNull(upcInclude);
		String tempPath = upcInclude.getFullPath().toOSString();// includePath= /RegressionTestProject/upc.h
		System.out.println("tempPath=" + tempPath);
		String includePath = upcInclude.getWorkspace().getRoot().getRawLocation().toOSString()
				+ upcInclude.getFullPath().toFile().getAbsolutePath();
		System.out.println("includePath= " + includePath);

		assertTrue(file instanceof IAdaptable);

		CoreModel.getDefault().getCModel().makeConsistent(new NullProgressMonitor());// jeff

		assertNotNull(cproject);
		ICElement ce = cproject.findElement(file.getFullPath());

		// ICElement covers folders and translationunits
		// final ICElement ce = (ICElement) ((IAdaptable)file).getAdapter(ICElement.class);
		assertNotNull(ce);
		List<String> includes = Arrays.asList(new String[] { includePath });
		RunAnalyseUPCcommandHandler racm = new RunAnalyseUPCcommandHandler();

		racm.runResource(new NullProgressMonitor(), ce, 0, includes);

		IMarker[] markers = file.findMarkers(UPCIDs.MARKER_ID, true, IResource.DEPTH_INFINITE);
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

		int[] expectedLinenos = { 77 };
		String[] expectedTypes = { "upc_addrfield" };
		ArtifactWithLine[] expectedArts = new ArtifactWithLine[expectedLinenos.length];
		ArtifactWithLine[] markerArts = new ArtifactWithLine[markers.length];
		for (int i = 0; i < expectedArts.length; i++) {
			expectedArts[i] = new ArtifactWithLine(expectedLinenos[i], expectedTypes[i]);
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

	public void testUPCartifactsWithMacro() throws Exception {
		System.out.println("\n==> " + getMethodName() + "()...");
		MpiPlugin.getDefault().getPreferenceStore().setValue(MpiIDs.MPI_RECOGNIZE_APIS_BY_PREFIX_ALONE, true);
		IFile file = importFile("resources", "testUPCmacro.upc");
		assertNotNull(file);

		IFile upcInclude = importFile("resources/includes", "upc.h");
		assertNotNull(upcInclude);
		String tempPath = upcInclude.getFullPath().toOSString();// includePath= /RegressionTestProject/upc.h
		System.out.println("tempPath=" + tempPath);
		String includePath = upcInclude.getWorkspace().getRoot().getRawLocation().toOSString()
				+ upcInclude.getFullPath().toFile().getAbsolutePath();
		System.out.println("includePath= " + includePath);

		assertTrue(file instanceof IAdaptable);

		CoreModel.getDefault().getCModel().makeConsistent(new NullProgressMonitor());// jeff

		assertNotNull(cproject);
		ICElement ce = cproject.findElement(file.getFullPath());

		// ICElement covers folders and translationunits
		// final ICElement ce = (ICElement) ((IAdaptable)file).getAdapter(ICElement.class);
		assertNotNull(ce);
		List<String> includes = Arrays.asList(new String[] { includePath });
		RunAnalyseUPCcommandHandler racm = new RunAnalyseUPCcommandHandler();

		racm.runResource(new NullProgressMonitor(), ce, 0, includes);

		IMarker[] markers = file.findMarkers(UPCIDs.MARKER_ID, true, IResource.DEPTH_INFINITE);
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

		int[] expectedLinenos = { 25, 26, 28, 28 };
		String[] expectedTypes = { "upc_addrfield", "upc_lock  (FOO)", "upc_something  (CHECK", "upc_string  (CHECK" };
		ArtifactWithLine[] expectedArts = new ArtifactWithLine[expectedLinenos.length];
		ArtifactWithLine[] markerArts = new ArtifactWithLine[markers.length];
		for (int i = 0; i < expectedArts.length; i++) {
			expectedArts[i] = new ArtifactWithLine(expectedLinenos[i], expectedTypes[i]);
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

			// Marker should be of the type name we expect - note macro is fairly long so we only test startsWith
			System.out.println("    " + expectedArts[i].getName() + " = " + markerArts[i].getName());
			assertTrue(markerArts[i].getName().startsWith(expectedArts[i].getName()));

		}

	}
}
