/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core.model;

import java.util.LinkedList;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IEnumeration;
import org.eclipse.cdt.core.model.IEnumerator;
import org.eclipse.cdt.core.model.IField;
import org.eclipse.cdt.core.model.IFunction;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.IFunctionTemplate;
import org.eclipse.cdt.core.model.IFunctionTemplateDeclaration;
import org.eclipse.cdt.core.model.IInclude;
import org.eclipse.cdt.core.model.IMacro;
import org.eclipse.cdt.core.model.IMethod;
import org.eclipse.cdt.core.model.IMethodDeclaration;
import org.eclipse.cdt.core.model.IMethodTemplate;
import org.eclipse.cdt.core.model.IMethodTemplateDeclaration;
import org.eclipse.cdt.core.model.INamespace;
import org.eclipse.cdt.core.model.IStructure;
import org.eclipse.cdt.core.model.IStructureDeclaration;
import org.eclipse.cdt.core.model.IStructureTemplate;
import org.eclipse.cdt.core.model.IStructureTemplateDeclaration;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.ITypeDef;
import org.eclipse.cdt.core.model.IUsing;
import org.eclipse.cdt.core.model.IVariable;
import org.eclipse.cdt.core.model.IVariableDeclaration;

public class ModelAdapter {
	/**
	 * Returns a serializable version of the given <code>ICElement<code>.
	 * This method can perform shallow or arbitrarily deep copies.
	 * 
	 * @param <T>
	 * @param parent
	 * @param element
	 * @param depth 0, for a shallow copy; -1 for a deep copy; any other
	 *        number specifies the maximum depth of the copy
	 * @return a serializable version of the given <code>ICElement</code>.
	 * @throws CModelException
	 */
	@SuppressWarnings("unchecked")
	public static <T extends ICElement> T adaptElement(Parent parent, T element, int depth, boolean adaptAncestors) throws CModelException {
		if (parent == null && adaptAncestors) {
			parent = adaptAncestors(element);
		}
		
		Parent result;
		switch (element.getElementType()) {
		case ICElement.C_ENUMERATION:
			result = new Enumeration(parent, (IEnumeration) element);
			break;
		case ICElement.C_ENUMERATOR:
			result = new Enumerator(parent, (IEnumerator) element);
			break;
		case ICElement.C_FIELD:
			result = new Field(parent, (IField) element);
			break;
		case ICElement.C_FUNCTION:
			result = new Function(parent, (IFunction) element);
			break;
		case ICElement.C_FUNCTION_DECLARATION:
			result = new FunctionDeclaration(parent, (IFunctionDeclaration) element);
			break;
		case ICElement.C_INCLUDE:
			result = new Include(parent, (IInclude) element);
			break;
		case ICElement.C_MACRO:
			result = new Macro(parent, (IMacro) element);
			break;
		case ICElement.C_METHOD:
			result = new Method(parent, (IMethod) element);
			break;
		case ICElement.C_METHOD_DECLARATION:
			result = new MethodDeclaration(parent, (IMethodDeclaration) element);
			break;
		case ICElement.C_NAMESPACE:
			result = new Namespace(parent, (INamespace) element);
			break;
		case ICElement.C_STRUCT:
		case ICElement.C_CLASS:
		case ICElement.C_UNION:
			result = new Structure(parent, (IStructure) element);
			break;
		case ICElement.C_STRUCT_DECLARATION:
		case ICElement.C_CLASS_DECLARATION:
		case ICElement.C_UNION_DECLARATION:
			result = new StructureDeclaration(parent, (IStructureDeclaration) element);
			break;
		case ICElement.C_TEMPLATE_STRUCT:
		case ICElement.C_TEMPLATE_CLASS:
		case ICElement.C_TEMPLATE_UNION:
			result = new StructureTemplate(parent, (IStructureTemplate) element);
			break;
		case ICElement.C_TEMPLATE_STRUCT_DECLARATION:
		case ICElement.C_TEMPLATE_CLASS_DECLARATION:
		case ICElement.C_TEMPLATE_UNION_DECLARATION:
			result = new StructureTemplateDeclaration(parent, (IStructureTemplateDeclaration) element);
			break;
		case ICElement.C_TEMPLATE_FUNCTION:
			result = new FunctionTemplate(parent, (IFunctionTemplate) element);
			break;
		case ICElement.C_TEMPLATE_FUNCTION_DECLARATION:
			result = new FunctionTemplateDeclaration(parent, (IFunctionTemplateDeclaration) element);
			break;
		case ICElement.C_TEMPLATE_METHOD:
			result = new MethodTemplate(parent, (IMethodTemplate) element);
			break;
		case ICElement.C_TEMPLATE_METHOD_DECLARATION:
			result = new MethodTemplateDeclaration(parent, (IMethodTemplateDeclaration) element);
			break;
		case ICElement.C_TEMPLATE_VARIABLE:
			result = new VariableTemplate(parent, (IVariable) element);
			break;
		case ICElement.C_TYPEDEF:
			result = new TypeDef(parent, (ITypeDef) element);
			break;
		case ICElement.C_USING:
			result = new Using(parent, (IUsing) element);
			break;
		case ICElement.C_VARIABLE:
			result = new Variable(parent, (IVariable) element);
			break;
		case ICElement.C_VARIABLE_DECLARATION:
			result = new VariableDeclaration(parent, (IVariableDeclaration) element);
			break;
		case ICElement.C_UNIT:
			ITranslationUnit tu = (ITranslationUnit) element;
			if(tu.isWorkingCopy())
				result = new WorkingCopy(parent, tu, tu.getContents());
			else
				result = new TranslationUnit(parent, tu);
			break;
		case ICElement.C_PROJECT:
			result = new CProject(element.getElementName());
			break;
		default:
			return null;
		}
		
		if (parent != null) {
			parent.addChild(result);
		}
		
		ICProject project = element.getCProject();
		if(project != null)
			result.setCProject(new CProject(project.getElementName()));
		
		result.setLocationURI(element.getLocationURI());
		
		if(element instanceof IHasManagedLocation) {
			IHasManagedLocation hml = (IHasManagedLocation) element;
			result.setManagedLocation(hml.getManagedLocation());
		}
		
		if(element instanceof IHasRemotePath) {
			IHasRemotePath hml = (IHasRemotePath) element;
			result.setRemotePath(hml.getRemotePath());
		}
		
		result.setPath(element.getPath());
		
		if (depth != 0) {
			for (ICElement child : result.internalGetChildren()) {
				result.addChild((CElement) adaptElement(result, child, depth - 1, adaptAncestors));
			}
		}
		return (T) result;
	}
	
	static Parent adaptAncestors(ICElement element) throws CModelException {
		LinkedList<ICElement> ancestors = new LinkedList<ICElement>();  
		ICElement current = element.getParent();
		while (current != null) {
			ancestors.addFirst(current);
			current = current.getParent();
		}
		
		Parent parent = null;
		for (ICElement ancestor : ancestors) {
			ICElement adapted = adaptElement(parent, ancestor, 0, false);
			if (!(adapted instanceof Parent)) {
				// Skip over ICElements we can't adapt
				continue;
			}
			parent = (Parent) adapted;
		}
		return parent;
	}
	
}
