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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ptp.pldt.common.IDs;
import org.eclipse.ptp.pldt.mpi.analysis.actions.RunAnalyseMPIAnalysiscommandHandler;
import org.eclipse.ptp.pldt.tests.PldtBaseTestFramework.ArtifactWithLine;

/**
 * 
 * Test MPI Barrier Analysis
 * 
 * Current status of tests: 29 April 2010
 * 
 * testMPI_barriers_tiny():
 * OK, this very trivial test case of two barriers does match, and no error is found
 * 
 * testMPI_barriers_helloBarrier
 * (not working) Barriers are not found to be in the same set, and a barrier error is found when it should not.
 * 
 * testMPI_barriers_error()
 * (not working) does not detect an error, when it should.
 * 
 * Most changes are in org.eclipse.ptp.pldt.mpi.analysis.analysis package
 * most significant changes are in MPIMVAnalaysis.java
 * notation of my initials (BRT) accompany most changes made by me, and notes
 * regarding these problems.
 * See also https://bugs.eclipse.org/bugs/show_bug.cgi?id=306064 Barrier Analysis bug
 * 
 * @author beth
 * 
 */
public class Test_MPI_barriers extends PldtBaseTestFramework {

	public void testMPI_barriers_tiny() throws Exception {
		System.out.println("\n==> " + getMethodName() + "()...");
		int[] expectedMarkerLocn = { 7, 10 };
		BarrierSetBasics bsb = new BarrierSetBasics(2, 2);// 2 sets, 2 members in each
		barrierBase("testMPIbarriersTiny.c", expectedMarkerLocn, bsb);
	}

	public void testMPI_barriers_helloBarrier() throws Exception {
		System.out.println("\n==> " + getMethodName() + "()...");
		int[] expectedMarkerLocn = { 33, 42 };
		BarrierSetBasics bsb = new BarrierSetBasics(2, 2);
		barrierBase("testMPIbarriers.c", expectedMarkerLocn, bsb);
	}

	public void testMPI_barriers_helloBarrierFnCall() throws Exception {
		System.out.println("\n==> " + getMethodName() + "()...");
		int[] expectedMarkerLocn = { 6, 36 };
		BarrierSetBasics bsb = new BarrierSetBasics(2, 2);
		barrierBase("testMPIbarriersFnCall.c", expectedMarkerLocn, bsb);
	}

	public void testMPI_barriers_error() throws Exception {
		System.out.println("\n==> " + getMethodName() + "()...");
		int[] expectedMarkerLocn = { 33 };
		BarrierSetBasics bsb = new BarrierSetBasics(1, 1, true);
		barrierBase("testMPIbarriersErr.c", expectedMarkerLocn, bsb);
	}

	public void barrierBase(String filename, int[] expectedLinenos, BarrierSetBasics bsb) throws Exception {

		IFile file = importFile("resources", filename);
		assertNotNull(file);

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
		RunAnalyseMPIAnalysiscommandHandler racm = new RunAnalyseMPIAnalysiscommandHandler();

		// racm.runResource(new NullProgressMonitor(), ce, 0, includes);
		// =================================
		IStructuredSelection selection = new StructuredSelection(file);
		final boolean reportErrors = false;
		boolean error = racm.analyseBarriers(selection, reportErrors);
		System.out.println("Barrier error?=" + error + "; expected " + bsb.getError());
		if (bsb.getError()) {
			assertTrue("Expected to find barrier error in " + filename, error);
		}
		else {
			assertFalse("No barrier errors should be found in " + filename, error);
		}

		String barrierMarkerID = org.eclipse.ptp.pldt.mpi.analysis.IDs.barrierMarkerID;

		// =================================
		IMarker[] markers = file.findMarkers(barrierMarkerID, true, IResource.DEPTH_INFINITE);
		int expectedNumber = expectedLinenos.length;
		assertNotNull(expectedNumber + " Barrier Markers should be found", markers);
		System.out.println("numMarkers: " + markers.length + "    number expected: " + expectedNumber);
		assertEquals(expectedNumber + " barrier markers should be found on " + filename, expectedNumber, markers.length);

		// ////////////
		ArtifactWithLine[] expectedArts = new ArtifactWithLine[expectedLinenos.length];
		ArtifactWithLine[] markerArts = new ArtifactWithLine[markers.length];
		for (int i = 0; i < expectedArts.length; i++) {
			expectedArts[i] = new ArtifactWithLine(expectedLinenos[i], "barrier");
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
			// /
			// IMarker marker = markers[i];
			// //showMarker(marker);
			// int lineNo=(Integer) marker.getAttribute(IMarker.LINE_NUMBER);
			// System.out.println(i+ " marker: lineNo "+lineNo+
			// " name: "+marker.getAttribute(IDs.NAME)+"   expectedLocn: "+expectedLinenos[i]);
			// assertEquals("expected barrier marker locn",expectedLinenos[i], lineNo);
		}

		barrierSetTest(filename, file, bsb);

	}

	public void barrierSetTest(String filename, IFile file, BarrierSetBasics bsb) throws CoreException {
		IMarker[] markers;
		// Barrier Sets
		final String barrierSetMarkerID = org.eclipse.ptp.pldt.mpi.analysis.IDs.matchingSetMarkerID;
		final String barrierParentID = org.eclipse.ptp.pldt.mpi.analysis.IDs.parentIDAttr;
		final String barrierMyID = org.eclipse.ptp.pldt.mpi.analysis.IDs.myIDAttr;

		markers = file.findMarkers(barrierSetMarkerID, true, IResource.DEPTH_INFINITE);
		// assertNotNull("2 Barrier Markers should be found",markers);

		int[] expectedMarkerLocn2 = { 7, 10, 7, 10, 7, 10 };// actual values are unused? 6 members: 2 parents, two child nodes each?

		int expectedLen = expectedMarkerLocn2.length;
		System.out.println("===============Barrier Sets: numMarkers: " + markers.length + "  expected: " + expectedLen);
		// assertEquals(expectedLen+" barrier markers should be found on "+filename,expectedLen, markers.length);

		// int expectedRoots=2;
		// get parent root markers (parentID=0)
		List<IMarker> setRoots = new ArrayList<IMarker>();
		for (int i = 0; i < markers.length; i++) {
			IMarker marker = markers[i];
			int lineNo = (Integer) marker.getAttribute(IMarker.LINE_NUMBER);

			int parentID = ((Integer) marker.getAttribute(barrierParentID)).intValue();
			if (parentID == 0) {
				setRoots.add(marker);
				System.out.println("found set starting at line " + lineNo);
			}
		}
		System.out.println("num barrier sets found: " + setRoots.size() + "    num expected: " + bsb.getNumSets());
		assertEquals("Number of barrier sets", bsb.getNumSets(), setRoots.size());

		// list what's in each set
		for (Iterator iterator = setRoots.iterator(); iterator.hasNext();) {
			IMarker iMarker = (IMarker) iterator.next();
			int setParentID = ((Integer) iMarker.getAttribute(barrierMyID)).intValue();
			int setParentLineNo = ((Integer) iMarker.getAttribute(IMarker.LINE_NUMBER)).intValue();
			System.out.println("set parentID: " + setParentID + " lineNo: " + setParentLineNo);
			ArrayList<IMarker> barrierSet = new ArrayList<IMarker>();
			for (int i = 0; i < markers.length; i++) {
				IMarker setMemberMarker = markers[i];
				int parentID = ((Integer) setMemberMarker.getAttribute(barrierParentID)).intValue();
				int memberID = ((Integer) setMemberMarker.getAttribute(barrierMyID)).intValue();
				if (parentID == setParentID) {
					int lineNo = (Integer) setMemberMarker.getAttribute(IMarker.LINE_NUMBER);
					System.out.println("  set member: lineNo: " + lineNo + "  parentID: " + parentID + " memberMyID: " + memberID);
					barrierSet.add(setMemberMarker);
				}
			}
			System.out.println("Barrier set with parentID " + setParentID + " expected " + bsb.numInEachSet + " members and found "
					+ barrierSet.size() + " members.");
		}
		// System.out.println("Barrier Sets: expect "+expectedRoots+"; found "+setRoots.size());
		// assertEquals("Expected "+expectedRoots+" barrier set roots", expectedRoots,setRoots.size());
		// // for each set, inspect what we expect in the set
		// for (int i = 0; i < markers.length; i++) {
		// IMarker marker = markers[i];
		// int lineNo=(Integer) marker.getAttribute(IMarker.LINE_NUMBER);
		//
		// int parentID = ((Integer) marker.getAttribute(barrierParentID)).intValue();
		// System.out.println(i+ " marker: lineNo "+lineNo+ " name: "+marker.getAttribute(IDs.NAME)
		// +" parent="+parentID);
		//
		// //assertEquals("expected barrier marker locn",expectedMarkerLocn2[i], lineNo);
		// }
		System.out.println("end barrier set inspection. ");
	}

	String showMarker(IMarker marker) {
		return showMarker(marker, "");
	}

	String showMarker(IMarker marker, String title) {
		StringBuffer buf = new StringBuffer(" ");
		buf.append("Marker: " + title);
		Map map = null;

		try {
			map = marker.getAttributes();
			Set keyset = map.keySet();
			System.out.println("Marker has " + keyset.size() + " values");
			for (Iterator iterator = keyset.iterator(); iterator.hasNext();) {
				String key = (String) iterator.next();
				Object obj = marker.getAttribute(key);
				String value = obj.toString();
				System.out.println("  " + key + ": " + value);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * Holds the basic parameters of what we expect to find in a Barrier Set. <br>
	 * Admittedly lightweight for now
	 * 
	 * @author beth
	 * 
	 */
	class BarrierSetBasics {

		int numSets;
		int numInEachSet; // note: all sets don't have to be the same size but in our very simple test cases so far, this is true
		boolean hasError = false;

		BarrierSetBasics(int numSets, int numInEachSet) {
			this.numSets = numSets;
			this.numInEachSet = numInEachSet;

		}

		BarrierSetBasics(int numSets, int numInEachSet, boolean hasError) {
			this(numSets, numInEachSet);
			this.hasError = hasError;

		}

		public int getNumSets() {
			return numSets;
		}

		public int getNumInEachSet() {
			return numInEachSet;
		}

		public boolean getError() {
			return hasError;
		}
	}

}
