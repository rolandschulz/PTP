/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/ 
package org.eclipse.ptp.internal.rdt.core.model;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.core.model.CElementDeltaBuilder;
import org.eclipse.cdt.internal.core.model.ReconcileWorkingCopyOperation;
import org.eclipse.cdt.internal.core.model.WorkingCopy;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ptp.rdt.core.resources.RemoteNature;

/**
 * Reconcile a working copy and signal the changes through a delta.
 * @author vkong
 *
 */
public class RemoteReconcileWorkingCopyOperation extends ReconcileWorkingCopyOperation {
	
	boolean forceProblemDetection;
	boolean fComputeAST;

	public RemoteReconcileWorkingCopyOperation(ICElement workingCopy,
			boolean computeAST, boolean forceProblemDetection) {
		super(workingCopy, computeAST, forceProblemDetection);
		fComputeAST= computeAST;
		this.forceProblemDetection = forceProblemDetection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.ReconcileWorkingCopyOperation#executeOperation()
	 */
	@Override
	protected void executeOperation() throws CModelException {
		
		//if this was executed by a local project by mistake 
		if (!RemoteNature.hasRemoteNature(getWorkingCopy().getCProject().getProject())) {
			super.executeOperation();
			return;
		}
		
		if (fMonitor != null){
			if (fMonitor.isCanceled())
				throw new OperationCanceledException();
			fMonitor.beginTask("element.reconciling", 10); //$NON-NLS-1$
		}
	
		WorkingCopy workingCopy = getWorkingCopy();
		boolean wasConsistent = workingCopy.isConsistent();
		CElementDeltaBuilder deltaBuilder = null;
	
		try {
			if (!wasConsistent || forceProblemDetection || fComputeAST) {
				// create the delta builder (this remembers the current content of the tu)
				deltaBuilder = new CElementDeltaBuilder(workingCopy);
		
				
				// update the element infos with the content of the working copy
				// it parses and produces an AST and create the model using the AST
				RemoteModelWorkingCopy rmWorkingCopy = null;
				if(workingCopy.getFile() == null)
					rmWorkingCopy = new RemoteModelWorkingCopy(workingCopy, false);
				else
					rmWorkingCopy = new RemoteModelWorkingCopy(workingCopy);
					
				rmWorkingCopy.makeConsistent(fMonitor,false);
								
				if (deltaBuilder != null) {
					deltaBuilder.buildDeltas();

					// register the deltas
					if (deltaBuilder.getDelta() != null) {
						if (!wasConsistent || forceProblemDetection || deltaBuilder.getDelta().getAffectedChildren().length > 0) {
							addReconcileDelta(workingCopy, deltaBuilder.getDelta());
						}
					}
				}
			}
	
			if (fMonitor != null) fMonitor.worked(2);
			
		} finally {
			if (fMonitor != null) fMonitor.done();
		}
	}	
}
