/*******************************************************************************
 * Copyright (c) 2008, 2011 IBM Corporation and others.
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
 * Class: org.eclipse.cdt.internal.core.model.ext.CElementHandleFactory
 * Version: 1.17
 */

package org.eclipse.ptp.internal.rdt.core.model;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplatePartialSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;

public class BindingAdapter {
	/**
	 * Returns an <code>ICElement</code> that corresponds to the given
	 * <code>IBinding</code>.
	 * 
	 * @param parent
	 * @param binding
	 * @param region the offset and length of the name corresponding to the
	 *        <code>IBinding</code>.
	 * @param definition <code>true</code>, if the <code>IBinding</code>
	 *        represents a definition.  <code>false</code> otherwise.
	 * @return an <code>ICElement</code> that corresponds to the given
	 * <code>IBinding</code>.
	 * @throws CModelException
	 * @throws DOMException
	 */
	public static ICElement adaptBinding(ITranslationUnit unit, IBinding binding, int offset, int length, boolean definition) throws CModelException, DOMException {
		Parent parent = adaptParent(unit, binding);
		if(parent == null)
			return null;
		
		SourceManipulation element = null;
		
		if (binding instanceof ICPPMethod) {
			if (binding instanceof ICPPFunctionTemplate) {
				element = definition 
					? new MethodTemplate(parent, (ICPPMethod) binding, (ICPPTemplateDefinition) binding)
					: new MethodTemplateDeclaration(parent, (ICPPMethod) binding, (ICPPTemplateDefinition) binding);
			} else {
				element = definition 
					? new Method(parent, (ICPPMethod) binding)
					: new MethodDeclaration(parent, (ICPPMethod) binding);
			}
		}	
		else if (binding instanceof IFunction) {
			if (binding instanceof ICPPFunctionTemplate) {
				element = definition 
					? new FunctionTemplate(parent, (IFunction) binding, (ICPPTemplateDefinition) binding)
					: new FunctionTemplateDeclaration(parent, (IFunction) binding, (ICPPTemplateDefinition) binding);
			} else if (binding instanceof ICPPFunctionTemplate) {
				element= definition 
				? new FunctionTemplate(parent, (IFunction) binding, (ICPPFunctionTemplate) binding)
				: new FunctionTemplateDeclaration(parent, (IFunction) binding, (ICPPFunctionTemplate) binding);
			} else {
				element = definition 
					? new Function(parent, (IFunction) binding)
					: new FunctionDeclaration(parent, (IFunction) binding);
			}
		}
		else if (binding instanceof IField) {
			element = new Field(parent, (IField) binding);
		}
		else if (binding instanceof IVariable) {
			if (binding instanceof IParameter) {
				return null;
			}
			element = new Variable(parent, (IVariable) binding);
		}
		else if (binding instanceof IEnumeration) {
			element = new Enumeration(parent, (IEnumeration) binding);
		}
		else if (binding instanceof IEnumerator) {
			element = new Enumerator(parent, (IEnumerator) binding);
		}
		else if (binding instanceof ICompositeType) {
			element= createHandleForComposite(parent, (ICompositeType) binding);
		}
		else if (binding instanceof ICPPNamespace) {
			element = new Namespace(parent, (ICPPNamespace) binding);
		}
		else if (binding instanceof ITypedef) {
			element = new TypeDef(parent, (ITypedef) binding);
		}
		if (element != null) {
			parent.addChild(element);
			
			if(unit != null) {
				element.setLocationURI(unit.getLocationURI());
				if(unit instanceof IHasManagedLocation) {
					IHasManagedLocation hml = (IHasManagedLocation) element;
					element.setManagedLocation(hml.getManagedLocation());
				}
				
				if(unit instanceof IHasRemotePath) {
					IHasRemotePath hml = (IHasRemotePath) element;
					element.setRemotePath(hml.getRemotePath());
				}
				element.setPath(unit.getPath());
			}
			
			element.setIdPos(offset, length);
			
			ICProject project = unit.getCProject();
//			if (project instanceof CProject) {
				element.setCProject(project);
//			}
		}
		return element;
	}
	
	private static Parent adaptParent(ITranslationUnit tu, IBinding binding) throws DOMException {
		Parent parent;
		if (tu instanceof TranslationUnit) {
			parent = (Parent) tu;
		} else if (tu == null) {
			throw new IllegalArgumentException();
			//parent = new TranslationUnit(null, "", null); //$NON-NLS-1$
		}
		else {
			parent = new TranslationUnit(null, tu);
		}
		
		
		IBinding parentBinding= binding.getOwner();
		if (parentBinding == null) {
			IScope scope= binding.getScope();
			if (scope != null && scope.getKind() == EScopeKind.eLocal) {
				return null;
			}
			return parent;
		}		
		
		if (parentBinding instanceof IEnumeration) {
			Parent grandParent= adaptParent(tu, parentBinding);
			if (parentBinding instanceof ICPPEnumeration && parentBinding.getNameCharArray().length > 0) {
				if (grandParent != null) {
					return new Enumeration(grandParent, (ICPPEnumeration) parentBinding);
				}
			} else {
				return grandParent;
			}
		}

		if (parentBinding instanceof ICPPNamespace) {
			char[] scopeName= parentBinding.getNameCharArray();
			// skip unnamed namespace
			if (scopeName.length == 0) {
				return adaptParent(tu, parentBinding);
			} 
			Parent grandParent= adaptParent(tu, parentBinding);
			if (grandParent == null) 
				return null;
			return new Namespace(grandParent, (ICPPNamespace) parentBinding);
		} 
		
		if (parentBinding instanceof ICompositeType) {
			Parent grandParent= adaptParent(tu, parentBinding);
			if (grandParent != null) {
				return createHandleForComposite(grandParent, (ICompositeType) parentBinding);
			}
		}
		return null;
	}

	public static ICElement adaptBinding(ITranslationUnit parent, IBinding binding, boolean definition) throws CModelException, DOMException {
		return adaptBinding(parent, binding, -1, -1, definition);
	}
	
	private static SourceManipulation createHandleForComposite(Parent parent, ICompositeType classBinding)
			throws DOMException {
		if (classBinding instanceof ICPPClassTemplatePartialSpecialization) {
			return new StructureTemplate(parent, (ICPPClassTemplatePartialSpecialization) classBinding);
		}
		if (classBinding instanceof ICPPClassTemplate) {
			return new StructureTemplate(parent, (ICPPClassTemplate) classBinding);
		}
		if (classBinding instanceof ICPPClassSpecialization) {
			ICPPClassSpecialization spec= (ICPPClassSpecialization) classBinding;
			ICPPClassType orig= spec.getSpecializedBinding();
			if (orig instanceof ICPPClassTemplate) {
				return new StructureTemplate(parent, (ICPPClassSpecialization) classBinding, (ICPPClassTemplate) orig);
			}
		}
		return new Structure(parent, classBinding);
	}
}
