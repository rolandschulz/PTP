/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on May 28, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.cldt.core.index;

import org.eclipse.cldt.core.CommonLanguageCore;
import org.eclipse.cldt.core.FortranCorePlugin;
import org.eclipse.cldt.internal.core.search.indexing.IndexManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * @author bgheorgh
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Indexer {
	private static Indexer indexer = null;
	private static IndexManager manager= FortranCorePlugin.getDefault().getCoreModel().getIndexManager();
	
	public static boolean indexEnabledOnAllProjects(){
		IProject[] projects= CommonLanguageCore.getWorkspace().getRoot().getProjects();
		boolean allEnabled = true;
		for (int i=0; i<projects.length; i++){
		  if (!indexEnabledOnProject(projects[i])){
		  	allEnabled=false;
		  	break;
		  }
		}
		
		return allEnabled;
	}
	
	public static boolean indexEnabledOnAnyProjects(){
		IProject[] projects= CommonLanguageCore.getWorkspace().getRoot().getProjects();
		boolean allEnabled = false;
		for (int i=0; i<projects.length; i++){
		  if (!projects[i].isOpen())
		  	continue;
		  
		  if (indexEnabledOnProject(projects[i])){
		  	allEnabled=true;
		  	break;
		  }
		}
		
		return allEnabled;
	}
	
	public static boolean indexEnabledOnProject(IProject project){
		boolean allEnabled = true;
		
		try {
			Boolean indexValue = (Boolean) project.getSessionProperty(IndexManager.activationKey);
			if (indexValue != null){
				if(!indexValue.booleanValue()){
					allEnabled = false;
				}
			}
			else {
				if (!manager.isIndexEnabled(project)){
					allEnabled=false;
				}
			}
			
		} catch (CoreException e) {}
		
		return allEnabled;
	}
}
