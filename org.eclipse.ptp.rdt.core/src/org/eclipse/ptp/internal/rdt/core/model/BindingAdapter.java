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
 * Class: org.eclipse.cdt.internal.core.model.ext.CElementHandleFactory
 * Version: 1.11
 */

package org.eclipse.ptp.internal.rdt.core.model;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
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
import org.eclipse.cdt.core.dom.ast.c.ICCompositeTypeScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateScope;
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
		Parent parent = adapt(unit, binding.getScope());
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
			if (binding instanceof ICPPClassTemplate) {
				element = new StructureTemplate(parent, (ICompositeType) binding, (ICPPTemplateDefinition) binding);
			}
			else {
				element = new Structure(parent, (ICompositeType) binding);
			}
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
				element.setPath(unit.getPath());
			}
			
			element.setIdPos(offset, length);
			
			ICProject project = unit.getCProject();
			if (project instanceof CProject) {
				element.setCProject(project);
			}
		}
		return element;
	}
	
	private static Parent adapt(ITranslationUnit tu, IScope scope) throws DOMException {
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
		
		if (scope == null) {
			return parent;
		}
		
		IName scopeName= scope.getScopeName();
		if (scopeName == null) {
			if (scope.getParent() == null) {
				return parent;
			} 
			if (scope instanceof ICPPTemplateScope) {
				return adapt(tu, scope.getParent());
			}
			return null; // unnamed namespace
		}

		Parent parentElement= adapt(tu, scope.getParent());
		if (parentElement == null) {
			return null;
		}

		Parent element;
		if (scope instanceof ICPPClassScope) {
			ICPPClassType type= ((ICPPClassScope) scope).getClassType();
			element= new Structure(parentElement, type);
		}
		else if (scope instanceof ICCompositeTypeScope) {
			ICompositeType type= ((ICCompositeTypeScope) scope).getCompositeType();
			element= new Structure(parentElement, type);
		}
		else if (scope instanceof ICPPBlockScope) {
			return null;
		}
		else if (scope instanceof ICPPNamespaceScope) {
			element= new Namespace(parentElement, new String(scopeName.getSimpleID()));
		} else {
			element = parentElement;
		}
		return element;
	}

	public static ICElement adaptBinding(ITranslationUnit parent, IBinding binding, boolean definition) throws CModelException, DOMException {
		return adaptBinding(parent, binding, -1, -1, definition);
	}

}
