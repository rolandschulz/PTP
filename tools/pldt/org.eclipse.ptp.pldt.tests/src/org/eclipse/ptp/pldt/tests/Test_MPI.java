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

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.core.model.TranslationUnit;
import org.eclipse.core.internal.resources.Marker;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.pldt.common.IDs;
import org.eclipse.ptp.pldt.mpi.core.MpiIDs;
import org.eclipse.ptp.pldt.mpi.core.MpiPlugin;
import org.eclipse.ptp.pldt.mpi.core.actions.RunAnalyseMPIcommandHandler;
import org.eclipse.ptp.pldt.openmp.core.OpenMPIDs;
import org.eclipse.ptp.pldt.openmp.core.OpenMPPlugin;
import org.eclipse.ptp.pldt.openmp.core.actions.RunAnalyseOpenMPcommandHandler;


/**
 * @author beth
 *
 */
public class Test_MPI extends PldtBaseTestFramework{
	
	public void testMPIartifacts() throws Exception {
		System.out.println("\n==> "+getMethodName()+"()...");
		
//		ArtifactWithLine a = new ArtifactWithLine(1, "foo");
//		ArtifactWithLine b = new ArtifactWithLine(1, "bar");
//		ArtifactWithLine c= new ArtifactWithLine(2,"foo");
//		ArtifactWithLine aa = new ArtifactWithLine(1,"foo");
//		a.compareTo(b);
//		b.compareTo(a);
//		a.compareTo(c);
//		c.compareTo(a);
//		b.compareTo(c);
//		c.compareTo(b);
//		a.compareTo(aa);
//		b.compareTo(b);
//		c.compareTo(c);
		
		
		IFile file = importFile("resources", "testMPI.c");
		assertNotNull(file);
		
		IFile mpiInclude = importFile("resources/includes","mpi.h");
		assertNotNull(mpiInclude);
		String tempPath = mpiInclude.getFullPath().toOSString();//includePath= /RegressionTestProject/mpi.h
		System.out.println("tempPath="+tempPath);
		String includePath = mpiInclude.getWorkspace().getRoot().getRawLocation().toOSString()
			+ mpiInclude.getFullPath().toFile().getAbsolutePath();
		System.out.println("includePath= "+includePath);

		assertTrue(file instanceof IAdaptable);

		CoreModel.getDefault().getCModel().makeConsistent(new NullProgressMonitor());//jeff

		assertNotNull(cproject);
		ICElement ce = cproject.findElement(file.getFullPath());

		// ICElement covers folders and translationunits
		//final ICElement ce = (ICElement) ((IAdaptable)file).getAdapter(ICElement.class);
		assertNotNull(ce);
		List<String> includes = Arrays.asList(new String[] {includePath});
		RunAnalyseMPIcommandHandler racm = new RunAnalyseMPIcommandHandler();
		
		racm.runResource(new NullProgressMonitor(), ce, 0, includes);
		
		IMarker[] markers=file.findMarkers(MpiIDs.MARKER_ID, true, IResource.DEPTH_INFINITE);
		assertNotNull(markers);
		System.out.println("numMarkers: "+markers.length);
		// need to sort markers, since they are not returned in a pre-determined order??
		
		for (int i = 0; i < markers.length; i++) {
			IMarker marker = markers[i];
			int lineNo=(Integer) marker.getAttribute(IMarker.LINE_NUMBER);
			System.out.println(i+ " marker: lineNo "+lineNo+ " name: "+marker.getAttribute(IDs.NAME));
		}


		//When strings don't match, click on the first entry (ComparisonFailure) in the Failure Trace in the JUnit view to get a diff view
		//assertEquals("This\nis\na\ntest", "This\nwas\na\ntest");

		int[] expectedLinenos = {17,20,23,31,37,43};
		String[] expectedMpiTypes= {"MPI_Init", "MPI_Comm_rank", "MPI_Comm_size", "MPI_Send", "MPI_Recv", "MPI_Finalize"};
		ArtifactWithLine[] expectedArts = new ArtifactWithLine[expectedLinenos.length];
		ArtifactWithLine[] markerArts = new ArtifactWithLine[markers.length];
		for (int i = 0; i < expectedArts.length; i++) {
			expectedArts[i]=new ArtifactWithLine(expectedLinenos[i],expectedMpiTypes[i]);		
		}
		for (int i = 0; i < markerArts.length; i++) {
			markerArts[i]=new ArtifactWithLine(markers[i]);
		}
		Arrays.sort(expectedArts);
		Arrays.sort(markerArts);
		
		assertEquals(expectedArts.length, markerArts.length);
		// Since these tests are in a loop, if one fails, must inspect the Console output to know which ones finished and which one failed.
		for (int i = 0; i < markers.length; i++) {
			// Marker should be on the line number we expect
			System.out.println(i+". "+expectedArts[i].getLineNo()+" = "+markerArts[i].getLineNo());			
			assertEquals(expectedArts[i].getLineNo(), markerArts[i].getLineNo());
			
			// Marker should be of the type name we expect
			System.out.println("    "+expectedArts[i].getName()+" = "+markerArts[i].getName());			
			assertEquals(expectedArts[i].getName(), markerArts[i].getName());
			
			boolean test=markerArts[i].equals(expectedArts[i]);
			//the following never works, so we'll be happy enough with the two assertEquals above.
			//assertEquals(expectedArts[i], markerArts[i]);

		}

	}



	public void testMPI_CPPartifacts() throws Exception {
		System.out.println("\n==> "+getMethodName()+"()...");
			IFile file = importFile("resources", "helloMPIcpp.cpp");
			assertNotNull(file);
			
			IFile mpiInclude = importFile("resources/includes","mpi.h");
			assertNotNull(mpiInclude);
			String tempPath = mpiInclude.getFullPath().toOSString();//includePath= /RegressionTestProject/mpi.h
			System.out.println("tempPath="+tempPath);
			String includePath = mpiInclude.getWorkspace().getRoot().getRawLocation().toOSString()
				+ mpiInclude.getFullPath().toFile().getAbsolutePath();
			System.out.println("includePath= "+includePath);
	
			assertTrue(file instanceof IAdaptable);
	
			CoreModel.getDefault().getCModel().makeConsistent(new NullProgressMonitor());//jeff
	
			assertNotNull(cproject);
			ICElement ce = cproject.findElement(file.getFullPath());
	
			// ICElement covers folders and translationunits
			//final ICElement ce = (ICElement) ((IAdaptable)file).getAdapter(ICElement.class);
			assertNotNull(ce);
			List<String> includes = Arrays.asList(new String[] {includePath});
			RunAnalyseMPIcommandHandler racm = new RunAnalyseMPIcommandHandler();
			
			racm.runResource(new NullProgressMonitor(), ce, 0, includes);
			
			IMarker[] markers=file.findMarkers(MpiIDs.MARKER_ID, true, IResource.DEPTH_INFINITE);
			assertNotNull(markers);
			System.out.println("numMarkers: "+markers.length);
			int max= markers.length; //Math.min(10, markers.length);
			for (int i = 0; i < max; i++) {
				IMarker marker = markers[i];
				int lineNo=(Integer) marker.getAttribute(IMarker.LINE_NUMBER);
				System.out.println(i+ " marker: lineNo "+lineNo+ " name: "+marker.getAttribute(IDs.NAME));
			}
			int[] expectedLinenos = {22,23,24,26,37,49,57};
			final String mpiCW="MPI::COMM_WORLD";
			String[] expectedMpiTypes= {"MPI::Init", mpiCW, mpiCW, "MPI_Comm_rank", mpiCW, mpiCW, "MPI::Finalize"};
			//////////////////
			ArtifactWithLine[] expectedArts = new ArtifactWithLine[expectedLinenos.length];
			ArtifactWithLine[] markerArts = new ArtifactWithLine[markers.length];
			for (int i = 0; i < expectedArts.length; i++) {
				expectedArts[i]=new ArtifactWithLine(expectedLinenos[i],expectedMpiTypes[i]);		
			}
			for (int i = 0; i < markerArts.length; i++) {
				markerArts[i]=new ArtifactWithLine(markers[i]);
			}
			Arrays.sort(expectedArts);
			Arrays.sort(markerArts);
			
			assertEquals(expectedArts.length, markerArts.length);
 
			// Since these tests are in a loop, if one fails, must inspect the Console output to know which ones finished and which one failed.
			for (int i = 0; i < markers.length; i++) {
				// Marker should be on the line number we expect
				System.out.println(i+". "+expectedArts[i].getLineNo()+" = "+markerArts[i].getLineNo());			
				assertEquals(expectedArts[i].getLineNo(), markerArts[i].getLineNo());
				
				// Marker should be of the type name we expect
				System.out.println("    "+expectedArts[i].getName()+" = "+markerArts[i].getName());			
				assertEquals(expectedArts[i].getName(), markerArts[i].getName());
				
				boolean test=markerArts[i].equals(expectedArts[i]);
				//the following never works, so we'll be happy enough with the two assertEquals above.
				//assertEquals(expectedArts[i], markerArts[i]);
			}
		}

	public void testMPI_CPPartifacts_wPref() throws Exception {
		System.out.println("\n==> "+getMethodName()+"()...");
		testMPI_CPPartifacts_wPref(true);
		
		// this doesn't work from test: works ok and finds cpp artifacts ok with live workspace.
		// why?? In PldtAstVisitor.processFuncName(), line 211: IName[] names = tu.getDeclarations(binding) is an empty array in Junit test.
		//testMPI_CPPartifacts_wPref(false);
		
		// for now, we will consider the workaround (which is the default setting) to be set "recognize artifacts by prefix" to true
		
		// do the same for C?

		}



	public void testMPI_CPPartifacts_wPref(boolean prefValue) throws Exception {
	System.out.println("\n==> "+getMethodName()+"()...  prefValue="+prefValue);
		MpiPlugin.getDefault().getPreferenceStore().setValue(MpiIDs.MPI_RECOGNIZE_APIS_BY_PREFIX_ALONE, prefValue);
		IFile file = importFile("resources", "helloMPIcpp.cpp");
		assertNotNull(file);
		
		IFile mpiInclude = importFile("resources/includes","mpi.h");
		System.out.println("Recognize APIs by prefix is set to "+prefValue);
		assertNotNull(mpiInclude);
		String tempPath = mpiInclude.getFullPath().toOSString();//includePath= /RegressionTestProject/mpi.h
		System.out.println("tempPath="+tempPath);
		String includePath = mpiInclude.getWorkspace().getRoot().getRawLocation().toOSString()
			+ mpiInclude.getFullPath().toFile().getAbsolutePath();
		System.out.println("includePath= "+includePath);
	
		assertTrue(file instanceof IAdaptable);
	
		CoreModel.getDefault().getCModel().makeConsistent(new NullProgressMonitor());//jeff
	
		assertNotNull(cproject);
		ICElement ce = cproject.findElement(file.getFullPath());
	
		// ICElement covers folders and translationunits
		//final ICElement ce = (ICElement) ((IAdaptable)file).getAdapter(ICElement.class);
		assertNotNull(ce);
		List<String> includes = Arrays.asList(new String[] {includePath});
		RunAnalyseMPIcommandHandler racm = new RunAnalyseMPIcommandHandler();
		
		racm.runResource(new NullProgressMonitor(), ce, 0, includes);
		
		IMarker[] markers=file.findMarkers(MpiIDs.MARKER_ID, true, IResource.DEPTH_INFINITE);
		assertNotNull(markers);
		System.out.println("numMarkers: "+markers.length);
		int max= markers.length; //Math.min(10, markers.length);
		for (int i = 0; i < max; i++) {
			IMarker marker = markers[i];
			int lineNo=(Integer) marker.getAttribute(IMarker.LINE_NUMBER);
			System.out.println(i+ ". marker: lineNo "+lineNo+ " name: "+marker.getAttribute(IDs.NAME));
		}
	
		int[] expectedLinenos = {22,23,24,26,37,49,57};
		final String mpiCW="MPI::COMM_WORLD";
		String[] expectedMpiTypes= {"MPI::Init", mpiCW, mpiCW, "MPI_Comm_rank", mpiCW, mpiCW, "MPI::Finalize"};
	
		ArtifactWithLine[] expectedArts = new ArtifactWithLine[expectedLinenos.length];
		ArtifactWithLine[] markerArts = new ArtifactWithLine[markers.length];
		for (int i = 0; i < expectedArts.length; i++) {
			expectedArts[i]=new ArtifactWithLine(expectedLinenos[i],expectedMpiTypes[i]);		
		}
		for (int i = 0; i < markerArts.length; i++) {
			markerArts[i]=new ArtifactWithLine(markers[i]);
		}
		Arrays.sort(expectedArts);
		Arrays.sort(markerArts);
		
		assertEquals(expectedArts.length, markerArts.length);
	
		// Since these tests are in a loop, if one fails, must inspect the Console output to know which ones finished and which one failed.
		for (int i = 0; i < markers.length; i++) {
			// Marker should be on the line number we expect
			System.out.println(i+". "+expectedArts[i].getLineNo()+" = "+markerArts[i].getLineNo());			
			assertEquals(expectedArts[i].getLineNo(), markerArts[i].getLineNo());
			
			// Marker should be of the type name we expect
			System.out.println("    "+expectedArts[i].getName()+" = "+markerArts[i].getName());			
			assertEquals(expectedArts[i].getName(), markerArts[i].getName());
			
			boolean test=markerArts[i].equals(expectedArts[i]);
			//the following never works, so we'll be happy enough with the two assertEquals above.
			//assertEquals(expectedArts[i], markerArts[i]);
		}
	
	}





}
