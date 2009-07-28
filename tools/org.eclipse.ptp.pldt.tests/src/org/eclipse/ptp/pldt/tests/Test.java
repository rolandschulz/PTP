/**
 * 
 */
package org.eclipse.ptp.pldt.tests;

import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.pldt.mpi.core.MpiIDs;
import org.eclipse.ptp.pldt.mpi.core.actions.RunAnalyseMPIcommandHandler;


/**
 * @author beth
 *
 */
public class Test extends PldtBaseTestFramework{


	
	public void testMPIartifacts() throws Exception {
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

		// ICElement covers folders and translationunits
		final ICElement ce = (ICElement) ((IAdaptable) file).getAdapter(ICElement.class);
		// assertNotNull(ce);
		List<String> includes = Arrays.asList(new String[] {includePath});
		RunAnalyseMPIcommandHandler racm = new RunAnalyseMPIcommandHandler();
		
		racm.runResource(new NullProgressMonitor(), ce, 0, includes);
		
		IMarker[] markers=file.findMarkers(MpiIDs.MARKER_ID, true, IResource.DEPTH_INFINITE);
		assertNotNull(markers);
		System.out.println("numMarkers: "+markers.length);
		for (int i = 0; i < markers.length; i++) {
			IMarker marker = markers[i];
			System.out.println(i+ " marker: "+marker.toString());
			
			
		}
		
		
//		protected boolean runResource(IProgressMonitor monitor, ICElement ce,
//				int indent, List<String> includes) throws InterruptedException {
	}
}
