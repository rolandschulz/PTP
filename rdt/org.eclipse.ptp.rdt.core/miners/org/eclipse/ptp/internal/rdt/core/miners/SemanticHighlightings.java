/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems) - Adapted for CDT
 *******************************************************************************/

package org.eclipse.ptp.internal.rdt.core.miners;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.ILabel;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.ICExternalBinding;
import org.eclipse.cdt.core.dom.ast.c.ICFunctionScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateNonTypeParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateParameter;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.core.index.IIndexName;

import org.eclipse.cdt.internal.core.dom.parser.cpp.OverloadableOperator;
import org.eclipse.ptp.internal.rdt.core.miners.PositionCollector.SemanticToken;


/**
 * Semantic highlightings.
 * Derived from JDT.
 * 
 * @since 4.0
 */
public class SemanticHighlightings {
	/**
	 * Semantic highlightings
	 */
	private static SemanticHighlighting[] fgSemanticHighlightings;

	/**
	 * Semantic highlighting for static fields.
	 */
	private static final class StaticFieldHighlighting extends SemanticHighlighting {
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		@Override
		public boolean consumes(SemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTName) {
				IASTName name= (IASTName)node;
				if (name instanceof ICPPASTQualifiedName && name.isReference()) {
					return false;
				}
				IBinding binding= token.getBinding();
				if (binding instanceof IField && !(binding instanceof IProblemBinding)) {
					try {
						return ((IField)binding).isStatic();
					} catch (Error e) /* PDOMNotImplementedError */ {
						// ignore
					}
				}
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for fields.
	 */
	private static final class FieldHighlighting extends SemanticHighlighting {
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		@Override
		public boolean consumes(SemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTName) {
				IASTName name= (IASTName)node;
				if (name instanceof ICPPASTQualifiedName && name.isReference()) {
					return false;
				}
				IBinding binding= token.getBinding();
				if (binding instanceof IField) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for method declarations.
	 */
	private static final class MethodDeclarationHighlighting extends SemanticHighlighting {
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		@Override
		public boolean consumes(SemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTName) {
				IASTName name= (IASTName)node;
				if (!name.isReference()) {
					IBinding binding= token.getBinding();
					if (binding instanceof ICPPMethod) {
						return true;
					} else if (binding instanceof IProblemBinding) {
						// try to be derive from AST
						node= name.getParent();
						while (node instanceof IASTName) {
							node= node.getParent();
						}
						if (node instanceof ICPPASTFunctionDeclarator) {
							if (name instanceof ICPPASTQualifiedName) {
								ICPPASTQualifiedName qName= (ICPPASTQualifiedName)name;
								IASTName[] names= qName.getNames();
								if (names.length > 1) {
									if (names[names.length - 2].getBinding() instanceof ICPPClassType) {
										return true;
									}
								}
							} else {
								while (node != token.getRoot() && !(node.getParent() instanceof IASTDeclSpecifier)) {
									node= node.getParent();
								}
								if (node instanceof ICPPASTCompositeTypeSpecifier) {
									return true;
								}
							}
						}
					}
				}
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for static method invocations.
	 */
	private static final class StaticMethodInvocationHighlighting extends SemanticHighlighting {
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		@Override
		public boolean consumes(SemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTName) {
				IASTName name= (IASTName)node;
				if (name instanceof ICPPASTQualifiedName) {
					return false;
				}
				if (!name.isReference()) {
					return false;
				}
				IBinding binding= token.getBinding();
				if (binding instanceof ICPPMethod && !(binding instanceof IProblemBinding)) {
					try {
						return ((ICPPMethod)binding).isStatic();
					} catch (Error e) /* PDOMNotImplementedError */ {
						// ignore
					}
				}
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for methods.
	 */
	private static final class MethodHighlighting extends SemanticHighlighting {
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		@Override
		public boolean consumes(SemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTImplicitName)
				return false;
			if (node instanceof IASTName) {
				IASTName name= (IASTName)node;
				if (name instanceof ICPPASTQualifiedName && name.isReference()) {
					return false;
				}
				IBinding binding= token.getBinding();
				if (binding instanceof ICPPMethod) {
					return true;
				}
			}
			return false;
		}

	}

	/**
	 * Semantic highlighting for function declarations.
	 */
	private static final class FunctionDeclarationHighlighting extends SemanticHighlighting {
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		@Override
		public boolean consumes(SemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTName) {
				IASTName name= (IASTName)node;
				if (name.isDeclaration()) {
					IBinding binding= token.getBinding();
					if (binding instanceof IFunction 
							&& !(binding instanceof ICPPMethod)) {
						return true;
					} else if (binding instanceof IProblemBinding) {
						// try to derive from AST
						if (name instanceof ICPPASTQualifiedName) {
							return false;
						}
						node= name.getParent();
						while (node instanceof IASTName) {
							node= node.getParent();
						}
						if (node instanceof IASTFunctionDeclarator) {
							while (node != token.getRoot() && !(node.getParent() instanceof IASTDeclSpecifier)) {
								node= node.getParent();
							}
							if (node instanceof ICPPASTCompositeTypeSpecifier) {
								return false;
							}
							return true;
						}
					}
				}
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for functions.
	 */
	private static final class FunctionHighlighting extends SemanticHighlighting {
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		@Override
		public boolean consumes(SemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTImplicitName)
				return false;
			if (node instanceof IASTName) {
				IASTName name= (IASTName)node;
				if (name instanceof ICPPASTQualifiedName && name.isReference()) {
					return false;
				}
				IBinding binding= token.getBinding();
				if (binding instanceof IFunction && !(binding instanceof ICPPMethod)) {
					return true;
				}
			}
			return false;
		}
	}
	
	/**
	 * Semantic highlighting for local variable declarations.
	 */
	private static final class LocalVariableDeclarationHighlighting extends SemanticHighlighting {
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		@Override
		public boolean consumes(SemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTName) {
				IASTName name= (IASTName)node;
				if (name.isDeclaration()) {
					IBinding binding= token.getBinding();
					if (binding instanceof IVariable
							&& !(binding instanceof IField)
							&& !(binding instanceof IParameter)
							&& !(binding instanceof IProblemBinding)) {
						try {
							IScope scope= binding.getScope();
							if (LocalVariableHighlighting.isLocalScope(scope)) {
								return true;
							}
						} catch (DOMException exc) {
						} catch (Error e) /* PDOMNotImplementedError */ {
							// ignore
						}
					}
				}
			}
			return false;
		}

}

	/**
	 * Semantic highlighting for local variables.
	 */
	private static final class LocalVariableHighlighting extends SemanticHighlighting {
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		@Override
		public boolean consumes(SemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTName) {
				IASTName name= (IASTName)node;
				if (name.isReference()) {
					IBinding binding= token.getBinding();
					if (binding instanceof IVariable
							&& !(binding instanceof IField)
							&& !(binding instanceof IParameter)
							&& !(binding instanceof IProblemBinding)) {
						try {
							IScope scope= binding.getScope();
							if (isLocalScope(scope)) {
								return true;
							}
						} catch (DOMException exc) {
						} catch (Error e) /* PDOMNotImplementedError */ {
							// ignore
						}
					}
				}
			}
			return false;
		}

	    public static boolean isLocalScope(IScope scope) {
	        while (scope != null) {
	            if (scope instanceof ICPPFunctionScope ||
	                    scope instanceof ICPPBlockScope ||
	                    scope instanceof ICFunctionScope) {
	                return true;
	            }
	            try {
	                scope= scope.getParent();
	            } catch (DOMException e) {
	                scope= null;
	            }
	        }
	        return false;
	    }
}

	/**
	 * Semantic highlighting for global variables.
	 */
	private static final class GlobalVariableHighlighting extends SemanticHighlighting {
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		@Override
		public boolean consumes(SemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTName) {
				IASTName name= (IASTName)node;
				if (name instanceof ICPPASTQualifiedName) {
					return false;
				}
				IBinding binding= token.getBinding();
				if (binding instanceof IVariable
						&& !(binding instanceof IField)
						&& !(binding instanceof IParameter)
						&& !(binding instanceof ICPPTemplateNonTypeParameter)
						&& !(binding instanceof IProblemBinding)) {
					try {
						IScope scope= binding.getScope();
						if (!LocalVariableHighlighting.isLocalScope(scope)) {
							return true;
						}
					} catch (DOMException exc) {

					} catch (Error e) /* PDOMNotImplementedError */ {
						// ignore
					}
				}
			}
			return false;
		}
		
	}

	/**
	 * Semantic highlighting for parameter variables.
	 */
	private static final class ParameterVariableHighlighting extends SemanticHighlighting {
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		@Override
		public boolean consumes(SemanticToken token) {
			IBinding binding= token.getBinding();
			if (binding instanceof IParameter) {
				return true;
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for template parameters.
	 */
	private static final class TemplateParameterHighlighting extends SemanticHighlighting {
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		@Override
		public boolean consumes(SemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTName) {
				IBinding binding= token.getBinding();
				if (binding instanceof ICPPTemplateParameter) {
					return true;
				}
			}
			return false;
		}
	}
	
	/**
	 * Semantic highlighting for classes.
	 */
	private static final class ClassHighlighting extends SemanticHighlighting {
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		@Override
		public boolean consumes(SemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof ICPPASTQualifiedName || node instanceof ICPPASTTemplateId) {
				return false;
			}
			if (node instanceof IASTName) {
				IBinding binding= token.getBinding();
				if (binding instanceof ICPPClassType) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Semantic highlighting for enums.
	 */
	private static final class EnumHighlighting extends SemanticHighlighting {
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		@Override
		public boolean consumes(SemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTName) {
				IBinding binding= token.getBinding();
				if (binding instanceof IEnumeration) {
					return true;
				}
			}
			return false;
		}
	}
	
	/**
	 * Semantic highlighting for macro references.
	 */
	private static final class MacroReferenceHighlighting extends SemanticHighlighting {
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		@Override
		public boolean consumes(SemanticToken token) {
			IBinding binding= token.getBinding();
			if (binding instanceof IMacroBinding) {
				IASTName name= (IASTName)token.getNode();
				if (name.isReference()) {
					return true;
				}
			}
			return false;
		}
	}
	
	/**
	 * Semantic highlighting for macro definitions.
	 */
	private static final class MacroDefinitionHighlighting extends SemanticHighlighting {
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		@Override
		public boolean consumes(SemanticToken token) {
			IBinding binding= token.getBinding();
			if (binding instanceof IMacroBinding) {
				IASTName name= (IASTName)token.getNode();
				if (!name.isReference()) {
					return true;
				}
			}
			return false;
		}
	}
	
	/**
	 * Semantic highlighting for typedefs.
	 */
	private static final class TypedefHighlighting extends SemanticHighlighting {
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		@Override
		public boolean consumes(SemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTName) {
				IASTName name= (IASTName)node;
				if (name instanceof ICPPASTQualifiedName) {
					return false;
				}
				IBinding binding= token.getBinding();
				if (binding instanceof ITypedef) {
					return true;
				}
			}
			return false;
		}
	}
	
	/**
	 * Semantic highlighting for namespaces.
	 */
	private static final class NamespaceHighlighting extends SemanticHighlighting {
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		@Override
		public boolean consumes(SemanticToken token) {
			IBinding binding= token.getBinding();
			if (binding instanceof ICPPNamespace) {
				return true;
			}
			return false;
		}
	}
	
	/**
	 * Semantic highlighting for labels.
	 */
	private static final class LabelHighlighting extends SemanticHighlighting {
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		@Override
		public boolean consumes(SemanticToken token) {
			IBinding binding= token.getBinding();
			if (binding instanceof ILabel) {
				return true;
			}
			return false;
		}
	}
	
	/**
	 * Semantic highlighting for enumerators.
	 */
	private static final class EnumeratorHighlighting extends SemanticHighlighting {
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		@Override
		public boolean consumes(SemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTName) {
				IASTName name= (IASTName)node;
				if (name instanceof ICPPASTQualifiedName) {
					return false;
				}
				IBinding binding= token.getBinding();
				if (binding instanceof IEnumerator) {
					return true;
				}
			}
			return false;
		}
	}
	
	/**
	 * Semantic highlighting for problems.
	 */
	private static final class ProblemHighlighting extends SemanticHighlighting {
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		@Override
		public boolean consumes(SemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTProblem) {
				return true;
			}
			IBinding binding= token.getBinding();
			if (binding instanceof IProblemBinding) {
				return true;
			}
			return false;
		}
	}
	
	/**
	 * Semantic highlighting for external SDK references.
	 */
	private static final class ExternalSDKHighlighting extends SemanticHighlighting {
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		@Override
		public boolean consumes(SemanticToken token) {
			IASTNode node= token.getNode();
			if (node instanceof IASTName) {
				IASTName name= (IASTName)node;
				if (name instanceof ICPPASTQualifiedName) {
					return false;
				}
				if (name instanceof IASTImplicitName) {
					return false;
				}
				if (name.isReference()) {
					IBinding binding= token.getBinding();
					IIndex index= token.getRoot().getIndex();
					return isExternalSDKReference(binding, index);
				}
			}
			return false;
		}

		private boolean isExternalSDKReference(IBinding binding, IIndex index) {
			if (binding instanceof IFunction) {
				try {
					if (binding instanceof IIndexBinding) {
						if (((IIndexBinding) binding).isFileLocal()) {
							return false;
						}
					}
					else if (!(binding instanceof ICExternalBinding)) {
						return false;
					}
					IIndexName[] decls= index.findNames(binding, IIndex.FIND_DECLARATIONS | IIndex.SEARCH_ACROSS_LANGUAGE_BOUNDARIES);
					for (IIndexName decl : decls) {
						IIndexFile indexFile= decl.getFile();
						if (indexFile != null && indexFile.getLocation().getFullPath() != null) {
							return false;
						}
					}
					if (decls.length != 0) {
						return true;
					}
				} catch (CoreException exc) {
					return false;
				}
			}
			return false;
		}
	}
	
	/**
	 * Semantic highlighting for functions.
	 */
	private static final class OverloadedOperatorHighlighting extends SemanticHighlighting {
		/*
		 * @see org.eclipse.cdt.internal.ui.editor.SemanticHighlighting#consumes(org.eclipse.cdt.internal.ui.editor.SemanticToken)
		 */
		@Override
		public boolean consumes(SemanticToken token) {
			IASTNode node = token.getNode();
			// so far we only have implicit names for overloaded operators and destructors, so this works
			if(node instanceof IASTImplicitName) {
				IASTImplicitName name = (IASTImplicitName) node;
				IBinding binding = name.resolveBinding();
				if(binding instanceof ICPPMethod  && !(binding instanceof IProblemBinding) && ((ICPPMethod)binding).isImplicit()) {
					return false;
				}
				char[] chars = name.toCharArray();
				if(chars[0] == '~' || OverloadableOperator.isNew(chars) || OverloadableOperator.isDelete(chars)) {
					return false;
				}
				return true;
			}
			return false;
		}
	}
	

	/**
	 * @return The semantic highlightings, the order defines the precedence of matches, the first match wins.
	 */
	public static SemanticHighlighting[] getSemanticHighlightings() {
		if (fgSemanticHighlightings == null)
			fgSemanticHighlightings= new SemanticHighlighting[] {
				new MacroReferenceHighlighting(),  // before all others!
				new ProblemHighlighting(),
				new ExternalSDKHighlighting(),
				new ClassHighlighting(),
				new StaticFieldHighlighting(),
				new FieldHighlighting(),  // after all other fields
				new MethodDeclarationHighlighting(),
				new StaticMethodInvocationHighlighting(),
				new ParameterVariableHighlighting(),  // before local variables
				new LocalVariableDeclarationHighlighting(),
				new LocalVariableHighlighting(),
				new GlobalVariableHighlighting(),
				new TemplateParameterHighlighting(), // before template arguments!
				new OverloadedOperatorHighlighting(), // before both method and function
				new MethodHighlighting(), // before types to get ctors
				new EnumHighlighting(),
				new MacroDefinitionHighlighting(),
				new FunctionDeclarationHighlighting(),
				new FunctionHighlighting(),
				new TypedefHighlighting(),
				new NamespaceHighlighting(),
				new LabelHighlighting(),
				new EnumeratorHighlighting(),
			};
		return fgSemanticHighlightings;
	}

	
	/**
	 * Do not instantiate
	 */
	private SemanticHighlightings() {
	}
}
