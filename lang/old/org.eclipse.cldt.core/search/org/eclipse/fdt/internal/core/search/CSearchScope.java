/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.fdt.internal.core.search;

import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.fdt.core.CProjectNature;
import org.eclipse.fdt.core.model.CModelException;
import org.eclipse.fdt.core.model.ICContainer;
import org.eclipse.fdt.core.model.ICElement;
import org.eclipse.fdt.core.model.ICProject;
import org.eclipse.fdt.core.model.IMember;
import org.eclipse.fdt.core.search.ICSearchScope;

public class CSearchScope implements ICSearchScope {

	private ArrayList elements;
	/* The paths of the resources in this search scope*/
	private IPath[] paths;
	private boolean[] pathWithSubFolders;
	private int pathsCount;
	
	private IPath[] enclosingProjects;
	
	public CSearchScope() {
		super();
		this.initialize();
	}
	
	protected void initialize() {
		this.paths = new IPath[1];
		this.pathWithSubFolders = new boolean[1];
		this.pathsCount = 0;
		this.enclosingProjects = new IPath[0];
	}

	private void addEnclosingProject(IPath path) {
	  int length = this.enclosingProjects.length;
	  for (int i = 0; i < length; i++) {
		  if (this.enclosingProjects[i].equals(path)) return;
	  }
	  System.arraycopy(
		  this.enclosingProjects,
		  0,
		  this.enclosingProjects = new IPath[length+1],
		  0,
		  length);
	  this.enclosingProjects[length] = path;
    }

	public void add(ICProject cProject, boolean includesPrereqProjects, HashSet visitedProjects) throws CModelException {
		IProject project = cProject.getProject();
		if (!project.isAccessible() || !visitedProjects.add(project)) return;
	
		this.addEnclosingProject(project.getFullPath());
		ICElement[] projChildren = cProject.getChildren();
		for (int i=0; i< projChildren.length; i++){
			this.add(projChildren[i]);
		}
					
		if (includesPrereqProjects){
			IProject[] refProjects=null;
			try {
				refProjects = project.getReferencedProjects();
			} catch (CoreException e) {
			}
			for (int i=0; i<refProjects.length; i++){
				ICProject cProj= (ICProject)refProjects[i].getAdapter(ICElement.class);
				if (cProj != null){
					this.add(cProj, true, visitedProjects);
				}	
			}
		  }
   }
   /**
	* @param project
	* @param b
	* @param set
	*/
   public void add(IProject project, boolean includesPrereqProjects, HashSet visitedProjects) throws CModelException {
	
		if (!project.isAccessible() || !visitedProjects.add(project)) return;
		
		IProjectDescription projDesc = null;
		try {
			projDesc = project.getDescription();
		} catch (CoreException e) {}
		
		if (projDesc == null)
			return;
			
		String[] natures = projDesc.getNatureIds();
		
		boolean flag = false;
		for (int i=0; i< natures.length; i++){
			if (natures[i].equals(CProjectNature.C_NATURE_ID)){	
			  flag=true;
			  break;
			} 
		}
		
		if (!flag)
		 //CNature not found; not a FDT project
		 return;
		 
		this.addEnclosingProject(project.getFullPath());
	
		if (includesPrereqProjects){
			IProject[] refProjects=null;
			try {
				refProjects = project.getReferencedProjects();
			} catch (CoreException e) {
			}
			for (int i=0; i<refProjects.length; i++){
				ICProject cProj= (ICProject)refProjects[i].getAdapter(ICElement.class);
				if (cProj != null){
					this.add(cProj, true, visitedProjects);
				}	
			}
		 }  
   }
   /**
    * Adds the given path to this search scope. Remember if subfolders need to be included as well.
    */
   private void add(IPath path, boolean withSubFolders) {
	  if (this.paths.length == this.pathsCount) {
		  System.arraycopy(
			  this.paths,
			  0,
			  this.paths = new IPath[this.pathsCount * 2],
			  0,
			  this.pathsCount);
		  System.arraycopy(
			  this.pathWithSubFolders,
			  0,
			  this.pathWithSubFolders = new boolean[this.pathsCount * 2],
			  0,
			  this.pathsCount);
	  }
	  this.paths[this.pathsCount] = path;
	  this.pathWithSubFolders[this.pathsCount++] = withSubFolders; 
   }

   public boolean encloses(String resourcePathString) {
	  IPath resourcePath = new Path(resourcePathString);
	  return this.encloses(resourcePath);
   }
   /**
    * Returns whether this search scope encloses the given path.
    */
   private boolean encloses(IPath path) {
	  for (int i = 0; i < this.pathsCount; i++) {
		  if (this.pathWithSubFolders[i]) {
			  if (this.paths[i].isPrefixOf(path)) {
				  return true;
			  }
		  } else {
			  // if not looking at subfolders, this scope encloses the given path 
			  // if this path is a direct child of the scope's resource
			  // or if this path is the scope's resource 
			  IPath scopePath = this.paths[i];
			  if (scopePath.isPrefixOf(path) 
				  && ((scopePath.segmentCount() == path.segmentCount() - 1)
					  || (scopePath.segmentCount() == path.segmentCount()))) {
				  return true;
			  }
		  }
	  }
	  return false;
   }

   public boolean encloses(ICElement element) {
	   if (this.elements != null) {
	 	  for (int i = 0, length = this.elements.size(); i < length; i++) {
	  		  ICElement scopeElement = (ICElement)this.elements.get(i);
	 		  ICElement searchedElement = element;
			  while (searchedElement != null) {
				  if (searchedElement.equals(scopeElement)) {
					  return true;
				  } 
				  searchedElement = searchedElement.getParent();
			  }
		  }
		  return false;
	   }
	   return this.encloses(this.fullPath(element));
   }

   public IPath[] enclosingProjects() {
 	  return this.enclosingProjects;
   }
  
   private IPath fullPath(ICElement element) {
 	  return element.getPath();
   }

   public void add(ICElement element) {
		switch (element.getElementType()) {
		case ICElement.C_PROJECT:
			// a workspace scope should be used
		break; 
		case ICElement.C_CCONTAINER:
			add( (ICContainer) element );
		break;
		default:
			if (element instanceof IMember) {
				if (this.elements == null) {
					this.elements = new ArrayList();
				}
				this.elements.add(element);
			}
			//Add the element to paths 
			this.add(this.fullPath(element), true);
			
			ICElement parent = element.getParent();
			while (parent != null && !(parent instanceof ICProject)) {
				parent = parent.getParent();
			}
			if (parent instanceof ICProject) {
				this.addEnclosingProject(parent.getCProject().getProject().getFullPath());
			}
		}
	}

   	public void add( ICContainer container ){
   		ICElement [] children = null;
		try {
			children = container.getChildren();
		} catch (CModelException e) {
			return;
		}
		for( int i = 0; i < children.length; i++ ){
   			add( children[i] );
   		}	
   	}
	/**
	 * @param finalPath
	 */
	public void addFile(IPath filePath, IProject fileProject) {
		//Add the file 
		this.add(filePath, true);
		//Add the files' containing project - unless it's an external file
		//in which case this is null
		if (fileProject != null){
			this.addEnclosingProject(fileProject.getFullPath());
		}
	
	}

}
