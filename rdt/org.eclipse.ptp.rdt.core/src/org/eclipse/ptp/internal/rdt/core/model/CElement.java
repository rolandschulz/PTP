/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/ 

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.core/model
 * Class: org.eclipse.cdt.internal.core.model.ext.CElementHandle
 * Version: 1.10
 */

package org.eclipse.ptp.internal.rdt.core.model;

import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICElementVisitor;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ptp.rdt.core.RDTLog;

public abstract class CElement implements ICElement, Serializable {
	private static final long serialVersionUID = 1L;

	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	
	protected CElementInfo fInfo;
	protected ICElement fParent;
	protected String fName;
	protected int fType;

	protected URI fLocation;
	protected IPath fPath;

	protected ICProject fCProject;
	
	public CElement(ICElement parent, int type, String name) {
		fParent = parent;
		fType = type;
		fName = name;
	}
	
	public void accept(ICElementVisitor visitor) throws CoreException {
	}

	public boolean exists() {
		return true;
	}

	public ICElement getAncestor(int ancestorType) {
		ICElement parent = getParent();
		while (parent != null) {
			if (parent.getElementType() == ancestorType) {
				return parent;
			}
			parent = parent.getParent();
		}
		return null;
	}

	public ICModel getCModel() {
		return null;
	}

	public ICProject getCProject() {
		return fCProject;
	}

	public String getElementName() {
		return fName;
	}

	public int getElementType() {
		return fType;
	}

	public URI getLocationURI() {
		return fLocation;
	}

	public ICElement getParent() {
		return fParent;
	}

	public IPath getPath() {
		return fPath;
	}

	public IResource getResource() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		if (fLocation != null) {
			IFile[] files = root.findFilesForLocationURI(fLocation);
			if (files.length > 0) {
				return files[0];
			}
		}
		if (fPath != null) {
			IFile[] files = root.findFilesForLocation(fPath);
			if (files.length > 0) {
				return files[0];
			}
		}
		return null;
	}

	public IResource getUnderlyingResource() {
		return null;
	}

	public boolean isReadOnly() {
		return true;
	}

	public boolean isStructureKnown() throws CModelException {
		return false;
	}
	
	public String getHandleIdentifier() {
		return null;
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		return null;
	}

	public CElementInfo getElementInfo() {
		if (fInfo == null) {
			fInfo = new CElementInfo(this);
		}
		return fInfo;
	}

	protected List<ICElement> internalGetChildren() {
		return Collections.emptyList();
	}
	
	protected String[] extractParameterTypes(IFunction func) throws DOMException {
		IParameter[] params= func.getParameters();
		String[] parameterTypes= new String[params.length];
		for (int i = 0; i < params.length; i++) {
			IParameter param = params[i];
			parameterTypes[i]= ASTTypeUtil.getType(param.getType());
		}
		if (parameterTypes.length == 1 && parameterTypes[0].equals("void")) { //$NON-NLS-1$
			return EMPTY_STRING_ARRAY;
		}
		return parameterTypes;
	}
	
	protected String[] extractTemplateParameterTypes(ICPPTemplateDefinition template) throws DOMException {
		ICPPTemplateParameter[] parameters = template.getTemplateParameters();
		String[] types = new String[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			ICPPTemplateParameter parameter = parameters[i];
			types[i] = parameter.getName();
		}
		return types; 
	}
	
	protected ASTAccessVisibility getVisibility(IBinding binding) {
		if (binding instanceof ICPPMember) {
			ICPPMember member= (ICPPMember) binding;
			try {
				switch (member.getVisibility()) {
				case ICPPMember.v_private:
					return ASTAccessVisibility.PRIVATE;
				case ICPPMember.v_protected:
					return ASTAccessVisibility.PROTECTED;
				case ICPPMember.v_public:
					return ASTAccessVisibility.PUBLIC;
				}
			} catch (DOMException e) {
				RDTLog.logError(e);
			}
		}
		return ASTAccessVisibility.PUBLIC;
	}
	
	public void setLocationURI(URI location) {
		fLocation = location;
	}
	
	public void setPath(IPath path) {
		if (path == null || path instanceof Path) {
			fPath = path;
		} else {
			fPath = new Path(path.toPortableString());
		}
	}
	
	public void setCProject(ICProject project) {
		fCProject = project;
	}
	
	@Override
	public String toString() {
		return "[" + getCProject() + "]" + getElementName() + " " + getClass().getName();  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ICElement)) {
			return false;
		}
		ICElement other = (ICElement) o;
		String name = other.getElementName();
		if (!fName.equals(name)) {
			return false;
		}
		if (fType != other.getElementType()) {
			return false;
		}
		URI location = other.getLocationURI();
		if (fLocation != null) {
			if (!fLocation.equals(location)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		int hash = fName.hashCode();
		hash += 31 * fType;
		if (fLocation != null) {  
			hash += 47 * fLocation.hashCode();
		}
		return hash;
	}
}
