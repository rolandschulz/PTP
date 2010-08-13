/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/


package org.eclipse.ptp.internal.rdt.core.index;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.rdt.core.resources.RemoteNature;
import org.eclipse.ptp.rdt.core.services.IRDTServiceConstants;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.ServiceModelManager;

public class IndexBuildSequenceController implements IResourceChangeListener {

	public static String INDEX_AFTER_BUILD_OPTION_KEY = "INDEX_AFTER_BUILD"; //$NON-NLS-1$
	
	public static String INDEX_STATUS_KEY = "INDEX_STATUS"; //$NON-NLS-1$
	public static String BUILD_STATUS_KEY = "BUILD_STATUS"; //$NON-NLS-1$

	public static final String TRUE = String.valueOf(true);
	public static final String FALSE = String.valueOf(false);
	
	public static String STATUS_NEVERRUN = "never_run"; //$NON-NLS-1$
	public static String STATUS_RUNNING = "running"; //$NON-NLS-1$
	public static String STATUS_INCOMPLETE = "incomplete"; //$NON-NLS-1$
	public static String STATUS_COMPLETE = "complete"; //$NON-NLS-1$

	// To make a build complete, must follow this build sequence, build_finish,
	// buildOutput_scanned -> build_complete
	public static String BUILD_SEQUENCE_STATUS_FINISH = "build_finish"; //$NON-NLS-1$
	public static String BUILD_SEQUENCE_STATUS_SCANNED = "build_sanned"; //$NON-NLS-1$

	private static Map<String, IndexBuildSequenceController> projectStatusMap = new HashMap<String, IndexBuildSequenceController>();

	private IProject project;

	private String runtimeBuildStatus;

	private IndexBuildSequenceController(IProject project) {
		this.project = project;
		intializeProjectStatus();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	private void intializeProjectStatus() {
		String index_after_build_option=null;
		String index_status = null;
		String build_status = null;
		try {
			index_after_build_option = project.getPersistentProperty(
					new QualifiedName("", //$NON-NLS-1$
							INDEX_AFTER_BUILD_OPTION_KEY));
			index_status = project.getPersistentProperty(
					new QualifiedName("", //$NON-NLS-1$
							INDEX_STATUS_KEY));
			build_status = project.getPersistentProperty(
					new QualifiedName("", //$NON-NLS-1$
							BUILD_STATUS_KEY));
	
			if (index_after_build_option == null) {
				setIndexAfterBuildOption();
			}
			if (index_status == null) {

				project.setPersistentProperty(
						new QualifiedName("", //$NON-NLS-1$
								INDEX_STATUS_KEY), STATUS_NEVERRUN);

			}
			if (build_status == null) {
				project.setPersistentProperty(
						new QualifiedName("", //$NON-NLS-1$
								BUILD_STATUS_KEY), STATUS_NEVERRUN);
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static IndexBuildSequenceController getIndexBuildSequenceController(IProject p) {
		String projectName = p.getName();
		IndexBuildSequenceController ps = projectStatusMap.get(projectName);
		if (ps == null) {

			ps = new IndexBuildSequenceController(p);
			projectStatusMap.put(projectName, ps);

		}
		return ps;

	}

	

	public String getRuntimeBuildStatus() {
		return runtimeBuildStatus;
	}

	
	public void setFinalBuildStatus() {
		if (this.runtimeBuildStatus == null) {
			setBuildCompleted();
		} else if (this.runtimeBuildStatus.equals(STATUS_INCOMPLETE)) {
			setBuildInCompleted();
		}
		//clear runtime build status
		runtimeBuildStatus=null;
	}

	public void setRuntimeBuildStatus(String runtimeBuildStatus) {
		this.runtimeBuildStatus = runtimeBuildStatus;
	}

	/*
	 * status_property_key will be INDEX_STATUS_KEY or BUILD_STATUS_KEY
	 * expect_status one of STATUS
	 */
	public boolean checkStatus(String status_property_key, String expect_status) {
		String status = null;

		try {
			status = project.getPersistentProperty(
					new QualifiedName("", //$NON-NLS-1$
							status_property_key));
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (status != null) {
			return status.equals(expect_status);
		} else {
			return false;
		}

	}
	
	public void setIndexAfterBuildOption() {
		setStatus(INDEX_AFTER_BUILD_OPTION_KEY, TRUE);
	}
	
	public void unsetIndexAfterBuildOption() {
		setStatus(INDEX_AFTER_BUILD_OPTION_KEY, FALSE);
	}
	public boolean isBuildServiceEnabled(){
		IService buildService = ServiceModelManager.getInstance().getService(IRDTServiceConstants.SERVICE_BUILD);
		IServiceConfiguration serviceConfig = ServiceModelManager.getInstance().getActiveConfiguration(project);
		if(serviceConfig.isDisabled(buildService)){
			return false;
		}else{
			return true;
		}
	}
	public boolean isIndexAfterBuildSet(){
		
		//if(isBuildServiceEnabled()){
		      return checkStatus(INDEX_AFTER_BUILD_OPTION_KEY, TRUE);
		//}else{
			//return false;
		//}
	}

	public boolean isIndexNeverRun() {
		return checkStatus(INDEX_STATUS_KEY, STATUS_NEVERRUN);
	}

	public boolean isIndexRunning() {
		return checkStatus(INDEX_STATUS_KEY, STATUS_RUNNING);
	}

	public boolean isIndexInCompleted() {
		return checkStatus(INDEX_STATUS_KEY, STATUS_INCOMPLETE);
	}

	public boolean isIndexCompleted() {
		return checkStatus(INDEX_STATUS_KEY, STATUS_COMPLETE);
	}

	public boolean isBuildNeverRun() {
		return checkStatus(BUILD_STATUS_KEY, STATUS_NEVERRUN);
	}

	public boolean isBuildRunning() {
		return checkStatus(BUILD_STATUS_KEY, STATUS_RUNNING);
	}

	public boolean isBuildInCompleted() {
		return checkStatus(BUILD_STATUS_KEY, STATUS_INCOMPLETE);
	}

	public boolean isBuildCompleted() {
		return checkStatus(BUILD_STATUS_KEY, STATUS_COMPLETE);
	}

	public void setIndexCompleted() {
		setStatus(INDEX_STATUS_KEY, STATUS_COMPLETE);
	}

	public void setIndexInCompleted() {
		setStatus(INDEX_STATUS_KEY, STATUS_INCOMPLETE);
	}

	public void setIndexRunning() {
		setStatus(INDEX_STATUS_KEY, STATUS_RUNNING);
	}

	public void setBuildCompleted() {
		setStatus(BUILD_STATUS_KEY, STATUS_COMPLETE);
	}

	public void setBuildInCompleted() {
		setStatus(BUILD_STATUS_KEY, STATUS_INCOMPLETE);
	}
	
	/**
	 * we don't want to clean build to update a never run status
	 */
	public void setBuildInCompletedForCleanBuild() {
		if(!isBuildNeverRun()){
			setStatus(BUILD_STATUS_KEY, STATUS_INCOMPLETE);
		}
		
	}

	public void setBuildRunning() {
		setStatus(BUILD_STATUS_KEY, STATUS_RUNNING);
	}

	/*
	 * status_property_key will be INDEX_STATUS_KEY or BUILD_STATUS_KEY
	 * expect_status one of STATUS
	 */
	public void setStatus(String status_property_key, String status_to_set) {
		try {
			project.setPersistentProperty(new QualifiedName("", //$NON-NLS-1$
					status_property_key), status_to_set);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * public void setBuildStatus(String status){
	 * if(status.equals(BUILD_SEQUENCE_STATUS_FINISH)){ build_sequence_status =
	 * BUILD_SEQUENCE_STATUS_FINISH; }
	 * if(status.equals(BUILD_SEQUENCE_STATUS_SCANNED)){
	 * if(build_sequence_status.equals(BUILD_SEQUENCE_STATUS_FINISH)){
	 * build_sequence_status = BUILD_SEQUENCE_STATUS_SCANNED;
	 * setStatus(BUILD_STATUS_KEY, STATUS_COMPLETE);
	 * if(start_index_after_build_complete){ startIndex(); } } } }
	 */
	/***
	 * 
	 * 
	 * 
	 * 
	 * ri=reindex, iu=index update, optional ri = prompt user that build is incompleted, provide option if they want to preceed build.
	 * IF=index following build
	 * 
	 * 
	 * 							index neverRun		index running			index incompleted		index completed
	 * build disabled             as usual				as usual				as usual				as usual
	 * consider as never build    iu -> ri
	 * 
	 * build never run           skip iu, no ri case
	 *
	 * build running           must be 1st build		
	 * no need this state	   run IF, skip iu          skip IF,                skip IF                 skip IF
	 *                         provide user a           iu, ri as usual         iu, ri as usual         iu, ri as usual
	 *                         warning message 
	 *                         if build is 
	 *                         incomplete 
	 *  
	 * build incomplete ->     optional ri,				optional ri, 			optional ri, 		    optional ri
	 *                         skip iu                  iu as usual				iu as usual				iu as usual
	 * 
	 * build complete          should never be  		ri, iu as usual         as usual				as usual
	 *                         this case             
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */

	/**
	 * since we run index after first time build, so the first time building
	 * must be at status of build-running and index-never-run. we want to skip
	 * index update (check when do index update) and run index after build
	 * (check in builder) in this case
	 */
	/*
	public boolean isBeforeOrUnderFirstTimeBuilding() {
		if (isBuildNeverRun()) {
			return true;
		}
		if (isBuildRunning() && isIndexNeverRun()) {
			return true;
		}
		return false;
	}
	*/
	/**
	 * we call invoke index before update build status, so if the build status is never run at that time,
	 * it must a first build attempt.
	 * @return
	 */
	public boolean shouldRunIndexFollowingBuild(){
		return isBuildRunning();
	}

	/**
	 * This case should never happen since we run index for first completed build.
	 * But in case it happens, we will turn index update to reindex.
	 * @return
	 */
	public boolean shouldTurnIndexUpdateToReindex(){
		return isIndexNeverRun()&&isBuildCompleted();
	}
	/**
	 * if index is not done yet, we will skip index update. The only exception is if build is completed, but this case should
	 * never happen since we run index for first completed build.
	 * @return
	 */
	public boolean shouldSkipIndexUpdate(){
		
		if(isBuildRunning()){
			return true;
		}
		if (isIndexNeverRun()){
			
			if(isBuildCompleted()){
				return false;
			}
			return true;
			
			
		}
		return false;
	}
	
	/**
	 * this is called from reindex autio after user choose to continue index after an optional index prompt.
	 * the build must be failed at the time.
	 */
	public void setBuildIncompleteIfItIsRunning(){
		if(isBuildRunning()){
			//todo check if build is still running.
			setBuildInCompleted();
		}
	}
	/**
	 * skip reindex during the first time build, when build is never run and index is never run.
	 * 
	 * this case would alos match if user run an optional reindex(build is never run and index is never run) and select to 
	 * continue do index, to solve this conflicts, we will turn index status to running after user selects continue to do index.
	 * @return
	 */
	public boolean shouldSkipReindex()
	{
		if(isBuildRunning()){
			return true;
		}
		//the case of build is disable during project creation.
		return isBuildNeverRun()&&isIndexNeverRun();
	}
	/*
	 * should be called from reindex action
	 * return true if build is never run or incomplete
	 * 
	 */
	public boolean isOptionalIndex() {
		return !isBuildCompleted();
	}
	
	
	/**
	 * precondition to call this function, 
	 * isIndexAfterBuildSet() ==true
	 */
	public void invokeIndex(){
		
		
		
		Job job = new Job("Indexing Job"){ //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				
				if(shouldRunIndexFollowingBuild()){
					setFinalBuildStatus();
					if(isBuildCompleted()){
						//for indexing following first time build, only run it after a completed build.
						ICProject cProject = CModelManager.getDefault().getCModel().getCProject(project);
						CCorePlugin.getIndexManager().reindex(cProject);
						//System.out.println("running invoke indexing job. "); //$NON-NLS-1$
					}
					
				}else{
					//System.out.println("not running index job, just update build status. "); //$NON-NLS-1$
					setFinalBuildStatus();
				}
		        return Status.OK_STATUS;
		    }

		};
		ISchedulingRule rule = ResourcesPlugin.getWorkspace().getRoot();
    	job.setRule(rule);
    	job.schedule();
    	
    	//System.out.println("schedule invoke indexing job. "); //$NON-NLS-1$
	
		
	}

	private void deleteProjectStaus(String projectName) {
		projectStatusMap.remove(projectName);
	}


	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getSource() instanceof IWorkspace) {
			
			IResource resource = event.getResource();
			if (event.getType() == IResourceChangeEvent.PRE_DELETE) {
				if (resource.getType() == IResource.PROJECT
						&& RemoteNature.hasRemoteNature((IProject) resource)) {
					deleteProjectStaus(resource.getName());
				}
			}
			

		}
	}

}
