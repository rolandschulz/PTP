package org.eclipse.ptp.internal.rdt.core.typehierarchy;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.internal.rdt.core.index.IndexQueries;

public class TypeHierarchyUtil {

	public static IBinding findTypeBinding(IBinding memberBinding) {
		try {
			if (memberBinding instanceof IEnumerator) {
				IType type= ((IEnumerator) memberBinding).getType();
				if (type instanceof IBinding) {
					return (IBinding) type;
				}
			}
			else if (memberBinding instanceof ICPPMember) {
				return ((ICPPMember) memberBinding).getClassOwner();
			}
			else if (memberBinding instanceof IField) {
				return ((IField) memberBinding).getCompositeTypeOwner();
			}
		} catch (DOMException e) {
			// don't log problem bindings
		}
		return null;
	}

	public static boolean isValidInput(IBinding binding) {
		if (isValidTypeInput(binding)
				|| binding instanceof ICPPMember
				|| binding instanceof IEnumerator
				|| binding instanceof IField) {
			return true;
		}
		return false;
	}

	public static boolean isValidTypeInput(IBinding binding) {
		if (binding instanceof ICompositeType
				|| binding instanceof IEnumeration 
				|| binding instanceof ITypedef) {
			return true;
		}
		return false;
	}

	public static ICElement findDeclaration(ICProject project, IIndex index, IASTName name, IBinding binding, IIndexLocationConverter converter) 
			throws CoreException {
		if (name != null && name.isDefinition()) {
			return IndexQueries.getCElementForName(project, index, name, converter);
		}
	
		ICElement[] elems= IndexQueries.findAllDefinitions(index, binding, converter, project);
		if (elems.length > 0) {
			return elems[0];
		}
		return IndexQueries.findAnyDeclaration(index, project, binding, converter);
	}

	public static ICElement findDefinition(ICProject project, IIndex index, IASTName name, IBinding binding, IIndexLocationConverter converter) 
			throws CoreException {
		if (name != null && name.isDefinition()) {
			return IndexQueries.getCElementForName(project, index, name, converter);
		}
	
		ICElement[] elems= IndexQueries.findAllDefinitions(index, binding, converter, project);
		if (elems.length > 0) {
			return elems[0];
		}
		return IndexQueries.findAnyDeclaration(index, project, binding, converter);
	}

}
