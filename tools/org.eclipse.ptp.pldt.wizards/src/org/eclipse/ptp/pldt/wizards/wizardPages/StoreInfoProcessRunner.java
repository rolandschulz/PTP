/*******************************************************************************
 * Copyright (c) 2007 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corp. - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.wizards.wizardPages;

import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author tibbitts
 *
 */
public class StoreInfoProcessRunner extends ProcessRunner {
	/** key to use in MBSCustomPage data for storing project type found in template */
	public static final String PROJECT_TYPE_KEY="projectType";
	public static final String PROJECT_TYPE_VALUE_MPI="mpi_project";
	public static final String PROJECT_TYPE_VALUE_OPENMP="openMP_project";
	private boolean wizTraceOn=MPIProjectWizardPage.wizardTraceOn;

	/**
	 * 
	 */
	public StoreInfoProcessRunner() {
		if(wizTraceOn)System.out.println("StoreInfoProcessRunner.ctor()");
	}

	/**
	 * Process runner to execute with information obtained from StoreInfo template process
	 * @see org.eclipse.cdt.core.templateengine.process.ProcessRunner#process(org.eclipse.cdt.core.templateengine.TemplateCore, org.eclipse.cdt.core.templateengine.process.ProcessArgument[], java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void process(TemplateCore template, ProcessArgument[] args,
			String processId, IProgressMonitor monitor)
			throws ProcessFailureException {
		if(wizTraceOn)System.out.println("StoreInfoProcessRunner.process()...");
		String projectName = args[0].getSimpleValue();
		if(wizTraceOn)System.out.println("  projectName="+projectName);
		Object pto = args[1];
		
		String projectType=pto.toString();
		if(wizTraceOn)System.out.println("  projectType="+projectType);
		
		//MPSCustomPageManager.addPageData
		String pageID=MPIProjectWizardPage.PAGE_ID;
		MBSCustomPageManager.addPageProperty(pageID, PROJECT_TYPE_KEY, projectType);
		MBSCustomPageManager.getPageProperty("pageID", "propID");
		//MBSCustomPageManager.setPageHideStatus("pageID", status)

	}

	/**
	 * This method Adds the File to the corresponding Project.
	 */
	public void process2(TemplateCore template, ProcessArgument[] args, String processId, IProgressMonitor monitor) throws ProcessFailureException {
		String projectName = args[0].getSimpleValue();
		ProcessArgument file = args[1];
		
	}

}
