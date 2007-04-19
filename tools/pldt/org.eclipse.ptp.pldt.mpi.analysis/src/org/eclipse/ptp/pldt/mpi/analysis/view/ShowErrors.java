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

package org.eclipse.ptp.pldt.mpi.analysis.view;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.util.SourceInfo;
import org.eclipse.ptp.pldt.common.util.ViewActivater;
import org.eclipse.ptp.pldt.mpi.analysis.IDs;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.BarrierTable.BarrierInfo;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPIBarrierMatching.ErrorMessage;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPIBarrierMatching.PathNode;

public class ShowErrors {
	protected List<ErrorMessage> errors_;
	protected int counter = 0;
	
	public ShowErrors(List<ErrorMessage> errors){
		this.errors_ = errors;
	}
	
	public void run(){
		IWorkspaceRoot wsResource = ResourcesPlugin.getWorkspace().getRoot();
		try {
			int depth = IResource.DEPTH_INFINITE;
			wsResource.deleteMarkers(IDs.errorMarkerID, false, depth);

        } catch (CoreException e) {
            System.out.println("RM: exception deleting markers.");
            e.printStackTrace();
        }
		
        //TODO: change this visitor
		MPIArtifactMarkingVisitor visitor = new MPIArtifactMarkingVisitor(IDs.errorMarkerID);
		
		/* Two kinds of Artifacts: 
		 * (1) errorous conditions
		 * (2) barriers in counter examples
		 */
		
		for(Iterator<ErrorMessage> i = errors_.iterator(); i.hasNext();){
			ErrorMessage err = i.next();
			
			/* Error condition */
			counter ++;
			int condID = counter;
			ScanReturn sr = new ScanReturn();
			String funcName = err.getFuncName();
			String fileName = err.getFileName();
			SourceInfo sourceInfo = err.getSourceInfo();
			ArtifactWithParent ea = new ArtifactWithParent(fileName, 
					sourceInfo.getStartingLine(), 1, 
					funcName, "Errorous Condition", sourceInfo, 0, condID, "Error", 0);
			sr.addArtifact(ea);
        	visitor.visitFile(err.getResource(), sr.getArtifactList());
        	
        	/* Path 1 (and 2) */
        	counter ++;
        	int path1ID = counter;
        	String path1name = (String)null;
        	if(err.getLength1() == -1){
        		if(err.getPath2() != null)
        			path1name = "Path 1 (dynamic number of barriers)";
        		else
        			path1name = "Loop (dynamic number of barriers)";
        	} else {
        		path1name = "Path 1 (" + err.getLength1() + " barrier(s))";
        	}
        	sr = new ScanReturn();
        	ea = new ArtifactWithParent("", 0, 0, "", "Counter Example",
        			err.getPath1SourceInfo(), condID, path1ID, path1name, 0);
        	sr.addArtifact(ea);
        	
        	int path2ID = 0;
        	if(err.getPath2() != null){
	    		counter ++;
	    		path2ID = counter;
	           	String path2name = (String)null;
	        	if(err.getLength2() == -1){
	        		path2name = "Path 2 (dynamic number of barriers)";
	        	} else {
	        		path2name = "Path 2 (" + err.getLength2() + " barrier(s))";
	        	}
	    		ea = new ArtifactWithParent("", 0, 0, "", "Counter Example",
	    				err.getPath2SourceInfo(), condID, path2ID, path2name, 0);
	    		sr.addArtifact(ea);
	        	}
        	visitor.visitFile(err.getResource(), sr.getArtifactList());

        	/* Counter example content */
        	for(Iterator<PathNode> ii = err.getPath1().iterator(); ii.hasNext();){
        		PathNode pn = ii.next();
        		BarrierInfo barrier = pn.getBarrier();
        		counter ++;
				sr = new ScanReturn();
	        	sourceInfo = barrier.getSourceInfo();
	        	fileName = barrier.getFileName();
	        	funcName = barrier.getEnclosingFunc();
	        	String barrierName = (String)null;
	        	if(pn.isRepeat())
	        		barrierName = "Barrier " + (barrier.getID() -4) + "(*)";
	        	else
	        		barrierName = "Barrier " + (barrier.getID() -4);
	        	ArtifactWithParent a = new ArtifactWithParent(fileName,
	        			sourceInfo.getStartingLine(), 1, funcName, 
	        			"Barrier", sourceInfo, path1ID, counter, barrierName, 
	        			barrier.getID()-4);
	        	sr.addArtifact(a);
	        	visitor.visitFile(barrier.getResource(), sr.getArtifactList());
        	}
        	
        	if(err.getPath2() == null) continue;
        	for(Iterator<PathNode> ii = err.getPath2().iterator(); ii.hasNext();){
        		PathNode pn = ii.next();
        		BarrierInfo barrier = pn.getBarrier();
        		counter ++;
				sr = new ScanReturn();
	        	sourceInfo = barrier.getSourceInfo();
	        	fileName = barrier.getFileName();
	        	funcName = barrier.getEnclosingFunc();
	        	String barrierName = (String)null;
	        	if(pn.isRepeat())
	        		barrierName = "Barrier " + (barrier.getID() -4) + "(*)";
	        	else
	        		barrierName = "Barrier " + (barrier.getID() -4);
	        	ArtifactWithParent a = new ArtifactWithParent(fileName,
	        			sourceInfo.getStartingLine(), 1, funcName,
	        			"Barrier", sourceInfo, path2ID, counter, barrierName,
	        			barrier.getID()-4);
	        	sr.addArtifact(a);
	        	visitor.visitFile(barrier.getResource(), sr.getArtifactList());
        	}
		}

        // Done creating markers, now show the view
        ViewActivater.activateView(IDs.matchingSetViewID);
	}

}
