/*******************************************************************************
 * Copyright (c) 2007, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    Anton Leherbauer (Wind River Systems)
 *    IBM Corporation
 *    Sergey Prigogin (Google)
 *******************************************************************************/

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.text.contentassist.DOMCompletionProposalComputer
 * Version: 1.32
 */

package org.eclipse.ptp.internal.rdt.core.contentassist;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionStyleMacroParameter;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorFunctionStyleMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroDefinition;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.ICFunctionScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.core.parser.util.IContentAssistMatcher;
import org.eclipse.cdt.internal.core.dom.parser.c.CBuiltinParameter;
import org.eclipse.cdt.internal.core.dom.parser.c.CBuiltinVariable;
import org.eclipse.cdt.internal.core.dom.parser.c.CImplicitFunction;
import org.eclipse.cdt.internal.core.dom.parser.c.CImplicitTypedef;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBuiltinParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBuiltinVariable;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPImplicitFunction;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPImplicitMethod;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPImplicitTypedef;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.AccessContext;
import org.eclipse.cdt.internal.core.parser.util.ContentAssistMatcherFactory;

/**
 * Searches the DOM (both the AST and the index) for completion proposals.
 */
public class CompletionProposalComputer {
	public List<Proposal> computeCompletionProposals(
			RemoteContentAssistInvocationContext context,
			IASTCompletionNode completionNode, String prefix) {

		List<Proposal> proposals = new LinkedList<Proposal>();
		
		if(context.isInPreprocessorDirective()) {
			if (!context.inPreprocessorKeyword()) {
				// add only macros
				if (prefix.length() == 0) {
					String computedPrefix= context.computeIdentifierPrefix().toString();
					if (computedPrefix != null) {
						prefix= computedPrefix;
					}
				}
				addMacroProposals(context, prefix, proposals);
			}
		} else {
			boolean handleMacros= false;
			IASTName[] names = completionNode.getNames();

			for (IASTName name : names) {
				if (name.getTranslationUnit() == null)
					// The node isn't properly hooked up, must have backtracked out of this node
					continue;
				
				IASTCompletionContext astContext = name.getCompletionContext();
				if (astContext == null) {
					continue;
				} else if (astContext instanceof IASTIdExpression
						|| astContext instanceof IASTNamedTypeSpecifier) {
					// handle macros only if there is a prefix
					handleMacros = prefix.length() > 0;
				}
				
				IBinding[] bindings = astContext.findBindings(
						name, !context.isContextInformationStyle());
				
				if (bindings != null) {
					AccessContext accessibilityContext = new AccessContext(name);
					for (IBinding binding : bindings) {
						if (accessibilityContext.isAccessible(binding))
							handleBinding(binding, context, prefix, astContext, proposals);
					}
				}
			}

			if (handleMacros)
				addMacroProposals(context, prefix, proposals);
		}
		
		return proposals;
	}

	private void addMacroProposals(RemoteContentAssistInvocationContext context, String prefix, List<Proposal> proposals) {
		
		IASTCompletionNode completionNode = context.getCompletionNode();
		addMacroProposals(context, prefix, proposals, completionNode.getTranslationUnit()
				.getMacroDefinitions());
		addMacroProposals(context, prefix, proposals, completionNode.getTranslationUnit()
				.getBuiltinMacroDefinitions());
	}
	private void addMacroProposals(RemoteContentAssistInvocationContext context, String prefix,
			List<Proposal> proposals, IASTPreprocessorMacroDefinition[] macros) {
		if (macros != null) {

			char[] prefixChars= prefix.toCharArray();
			final boolean matchPrefix= !context.isContextInformationStyle();
			
			if (matchPrefix) {
				IContentAssistMatcher matcher = ContentAssistMatcherFactory.getInstance().createMatcher(prefixChars);
			
				for (int i = 0; i < macros.length; ++i) {
					final char[] macroName= macros[i].getName().toCharArray();
					if (matcher.match(macroName)) {
						handleMacro(macros[i], context, prefix, proposals);
					}
				}
			}else{
				
				for (int i = 0; i < macros.length; ++i) {
					final char[] macroName= macros[i].getName().toCharArray();
					if (CharArrayUtils.equals(macroName, 0, macroName.length, prefixChars, true)) {
						handleMacro(macros[i], context, prefix, proposals);
					}
				}
			}
		}
	}
	
	private void handleMacro(IASTPreprocessorMacroDefinition macro, RemoteContentAssistInvocationContext context, String prefix, List<Proposal> proposals) {
		final String macroName = macro.getName().toString();
		final int baseRelevance= computeBaseRelevance(prefix, macroName);

		CompletionType type = new CompletionType(ICElement.C_MACRO);
		
		if (macro instanceof IASTPreprocessorFunctionStyleMacroDefinition) {
			IASTPreprocessorFunctionStyleMacroDefinition functionMacro = (IASTPreprocessorFunctionStyleMacroDefinition)macro;
			
			StringBuilder repStringBuff = new StringBuilder();
			repStringBuff.append(macroName);
			repStringBuff.append('(');
			
			StringBuilder args = new StringBuilder();

			IASTFunctionStyleMacroParameter[] params = functionMacro.getParameters();
			if (params != null) {
				for (int i = 0; i < params.length; ++i) {
					if (i > 0)
						args.append(", "); //$NON-NLS-1$
					args.append(params[i].getParameter());
				}
			}
			String argString = args.toString();
			
			StringBuilder descStringBuff = new StringBuilder(repStringBuff.toString());
			descStringBuff.append(argString);
			descStringBuff.append(')');
			
			repStringBuff.append(')');
			String repString = repStringBuff.toString();
			String descString = descStringBuff.toString();
			
			Proposal proposal = createProposal(repString, descString, prefix.length(), type, baseRelevance + RelevanceConstants.MACRO_TYPE_RELEVANCE, context);
			if (!context.isContextInformationStyle()) {
				if (argString.length() > 0) {
					proposal.setCursorPosition(repString.length() - 1);
				} else {
					proposal.setCursorPosition(repString.length());
				}
			}
			
			if (argString.length() > 0) {
				RemoteProposalContextInformation info = new RemoteProposalContextInformation(type, descString, argString);
				info.setContextInformationPosition(context.getContextInformationOffset());
				proposal.setContextInformation(info);
			}
			
			proposals.add(proposal);
		} else
			proposals.add(createProposal(macroName, macroName, prefix.length(), type, baseRelevance + RelevanceConstants.MACRO_TYPE_RELEVANCE, context));
	}
	
	protected void handleBinding(IBinding binding,
			RemoteContentAssistInvocationContext cContext,
			String prefix,
			IASTCompletionContext astContext, List<Proposal> proposals) {
		if ((binding instanceof CPPImplicitFunction
				|| binding instanceof CPPImplicitTypedef
				|| binding instanceof CPPBuiltinVariable
				|| binding instanceof CPPBuiltinParameter
				|| binding instanceof CImplicitFunction
				|| binding instanceof CImplicitTypedef
				|| binding instanceof CBuiltinVariable
				|| binding instanceof CBuiltinParameter)
				&& !(binding instanceof CPPImplicitMethod)) {
			return;
		}
		
		if (!isAnonymousBinding(binding)) {
			final String name = binding.getName();
			final int baseRelevance= computeBaseRelevance(prefix, name);
			if (binding instanceof ICPPClassType) {
				handleClass((ICPPClassType) binding, astContext, cContext, baseRelevance, proposals);
			} else if (binding instanceof IFunction) {
				handleFunction((IFunction)binding, cContext, baseRelevance, proposals);
			} else if (binding instanceof IVariable) {
					handleVariable((IVariable) binding, cContext, baseRelevance, proposals);
			} else if (!cContext.isContextInformationStyle()) {
				if (binding instanceof ITypedef) {
					proposals.add(createProposal(name, name, getElementType(binding), baseRelevance + RelevanceConstants.TYPEDEF_TYPE_RELEVANCE, cContext));
				} else if (binding instanceof ICPPNamespace) {
					handleNamespace((ICPPNamespace) binding, astContext, cContext, baseRelevance, proposals);
				} else if (binding instanceof IEnumeration) {
					proposals.add(createProposal(name, name, getElementType(binding), baseRelevance + RelevanceConstants.ENUMERATION_TYPE_RELEVANCE, cContext));
				} else if (binding instanceof IEnumerator) {
					proposals.add(createProposal(name, name, getElementType(binding), baseRelevance + RelevanceConstants.ENUMERATOR_TYPE_RELEVANCE, cContext));
				} else {
					proposals.add(createProposal(name, name, getElementType(binding), baseRelevance + RelevanceConstants.DEFAULT_TYPE_RELEVANCE, cContext));
				}
			}
		}
	}
	
	private boolean isAnonymousBinding(IBinding binding) {
		char[] name= binding.getNameCharArray();
		return name.length == 0 || name[0] == '{';
	}

	private void handleClass(ICPPClassType classType, IASTCompletionContext astContext, RemoteContentAssistInvocationContext context, int baseRelevance, List<Proposal> proposals) {
		if (context.isContextInformationStyle()) {
			ICPPConstructor[] constructors = classType.getConstructors();
			for (ICPPConstructor constructor : constructors) {
				handleFunction(constructor, context, baseRelevance, proposals);
			}
		} else {
			int relevance= 0;
			switch(classType.getKey()) {
			case ICPPClassType.k_class:
				relevance= RelevanceConstants.CLASS_TYPE_RELEVANCE;
				break;
			case ICompositeType.k_struct:
				relevance= RelevanceConstants.STRUCT_TYPE_RELEVANCE;
				break;
			case ICompositeType.k_union:
				relevance= RelevanceConstants.UNION_TYPE_RELEVANCE;
				break;
			}
			if (astContext instanceof IASTName && !(astContext instanceof ICPPASTQualifiedName)) {
				IASTName name= (IASTName)astContext;
				if (name.getParent() instanceof IASTDeclarator) {
					proposals.add(createProposal(classType.getName()+"::", classType.getName(), getElementType(classType), baseRelevance + relevance, context)); //$NON-NLS-1$
				}
			}
			proposals.add(createProposal(classType.getName(), classType.getName(), getElementType(classType), baseRelevance + RelevanceConstants.CLASS_TYPE_RELEVANCE, context));
		}
	}
	
	private void handleFunction(IFunction function, RemoteContentAssistInvocationContext context, int baseRelevance, List<Proposal> proposals) {	
		CompletionType type = getElementType(function);
		
		StringBuilder repStringBuff = new StringBuilder();
		repStringBuff.append(function.getName());
		repStringBuff.append('(');
		
		StringBuilder dispargs = new StringBuilder(); // for the dispargString
        StringBuilder idargs = new StringBuilder();   // for the idargString
        boolean hasArgs = true;
		String returnTypeStr = null;
		IParameter[] params = function.getParameters();
		if (params != null) {
			for (int i = 0; i < params.length; ++i) {
				IType paramType = params[i].getType();
				if (i > 0) {
		            dispargs.append(',');
		            idargs.append(',');
		        }

				dispargs.append(ASTTypeUtil.getType(paramType, false));
		        idargs.append(ASTTypeUtil.getType(paramType, false));
				String paramName = params[i].getName();
				if (paramName != null && paramName.length() > 0) {
					dispargs.append(' ');
					dispargs.append(paramName);
				}
			}
		
			if (function.takesVarArgs()) {
				if (params.length > 0) {
					dispargs.append(',');
					idargs.append(',');
				}
				dispargs.append("..."); //$NON-NLS-1$
				idargs.append("..."); //$NON-NLS-1$
			} else if (params.length == 0) { // force the void in
				dispargs.append("void"); //$NON-NLS-1$
				idargs.append("void"); //$NON-NLS-1$
			}
		}
		IFunctionType functionType = function.getType();
		if (functionType != null) {
			IType returnType = functionType.getReturnType();
			if (returnType != null)
				returnTypeStr = ASTTypeUtil.getType(returnType, false);
		}
		hasArgs = ASTTypeUtil.functionTakesParameters(function);
        
        String dispargString = dispargs.toString();
        String idargString = idargs.toString();
        String contextDispargString = hasArgs ? dispargString : null;
        StringBuilder dispStringBuff = new StringBuilder(repStringBuff.toString());
		dispStringBuff.append(dispargString);
        dispStringBuff.append(')');
        if (returnTypeStr != null && returnTypeStr.length() > 0) {
            dispStringBuff.append(" : "); //$NON-NLS-1$
            dispStringBuff.append(returnTypeStr);
        }
        String dispString = dispStringBuff.toString();

        StringBuilder idStringBuff = new StringBuilder(repStringBuff.toString());
        idStringBuff.append(idargString);
        idStringBuff.append(')');
        String idString = idStringBuff.toString();
		
        repStringBuff.append(')');
        String repString = repStringBuff.toString();

        final int relevance = function instanceof ICPPMethod ? RelevanceConstants.METHOD_TYPE_RELEVANCE : RelevanceConstants.FUNCTION_TYPE_RELEVANCE;
        Proposal proposal = createProposal(repString, dispString, idString, context.getCompletionNode().getLength(), type, baseRelevance + relevance, context);
		if (!context.isContextInformationStyle()) {
			int cursorPosition = hasArgs ? (repString.length() - 1) : repString.length();
			proposal.setCursorPosition(cursorPosition);
		}
		
		if (contextDispargString != null) {
			RemoteProposalContextInformation info = new RemoteProposalContextInformation(type, dispString, contextDispargString);
			info.setContextInformationPosition(context.getContextInformationOffset());
			proposal.setContextInformation(info);
		}
		
		proposals.add(proposal);
	}
	
	private void handleVariable(IVariable variable, RemoteContentAssistInvocationContext context, int baseRelevance, List<Proposal> proposals) {
		StringBuilder repStringBuff = new StringBuilder();
		repStringBuff.append(variable.getName());
		
		String returnTypeStr = "<unknown>"; //$NON-NLS-1$
		IType varType = variable.getType();
		if (varType != null)
			returnTypeStr = ASTTypeUtil.getType(varType, false);
        
        StringBuilder dispStringBuff = new StringBuilder(repStringBuff.toString());
        if (returnTypeStr != null) {
            dispStringBuff.append(" : "); //$NON-NLS-1$
            dispStringBuff.append(returnTypeStr);
        }
        String dispString = dispStringBuff.toString();

        StringBuilder idStringBuff = new StringBuilder(repStringBuff.toString());
        String idString = idStringBuff.toString();
		
        String repString = repStringBuff.toString();

		CompletionType type = getElementType(variable);
		final int relevance = isLocalVariable(variable) 
			? RelevanceConstants.LOCAL_VARIABLE_TYPE_RELEVANCE
			: isField(variable) 
				? RelevanceConstants.FIELD_TYPE_RELEVANCE
				: RelevanceConstants.VARIABLE_TYPE_RELEVANCE;
		Proposal proposal = createProposal(repString, dispString, idString, context.getCompletionNode().getLength(), type, baseRelevance + relevance, context);
		proposals.add(proposal);
	}
	
	private static boolean isField(IVariable variable) {
		return variable instanceof IField;
	}

	private static boolean isLocalVariable(IVariable variable) {
		try {
			return isLocalScope(variable.getScope());
		} catch (DOMException exc) {
			return false;
		}
	}

    private static boolean isLocalScope(IScope scope) {
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

	private void handleNamespace(ICPPNamespace namespace,
			IASTCompletionContext astContext,
			RemoteContentAssistInvocationContext cContext,
			int baseRelevance,
			List<Proposal> proposals) {
		if (astContext instanceof ICPPASTQualifiedName) {
			IASTCompletionContext parent = ((ICPPASTQualifiedName) astContext)
					.getCompletionContext();
			handleNamespace(namespace, parent, cContext, baseRelevance, proposals);
			return;
		}
		
		StringBuilder repStringBuff = new StringBuilder();
		repStringBuff.append(namespace.getName());
		
		if (!(astContext instanceof ICPPASTUsingDeclaration)
				&& !(astContext instanceof ICPPASTUsingDirective)) {
			repStringBuff.append("::"); //$NON-NLS-1$
		}
		
		String repString = repStringBuff.toString();
		proposals.add(createProposal(repString, namespace.getName(), getElementType(namespace), baseRelevance + RelevanceConstants.NAMESPACE_TYPE_RELEVANCE, cContext));
	}
	
	private Proposal createProposal(String repString, String dispString, CompletionType type,
			int relevance, RemoteContentAssistInvocationContext context) {
		return createProposal(repString, dispString, null, context.getCompletionNode().getLength(), type,
				relevance, context);
	}
	
	private Proposal createProposal(String repString, String dispString, int prefixLength, CompletionType type, int relevance, RemoteContentAssistInvocationContext context) {
		return createProposal(repString, dispString, null, prefixLength, type, relevance, context);
	}
	
	private Proposal createProposal(String repString, String dispString, String idString, int prefixLength, CompletionType type, int relevance, RemoteContentAssistInvocationContext context) {
		int parseOffset = context.getParseOffset();
		int invocationOffset = context.getInvocationOffset();
		boolean doReplacement = !context.isContextInformationStyle();
		
		int repLength = doReplacement ? prefixLength : 0;
		int repOffset = doReplacement ? parseOffset - repLength : invocationOffset;
		repString = doReplacement ? repString : ""; //$NON-NLS-1$
		
		return new Proposal(repString, repOffset, repLength, type, dispString, idString, relevance);
	}

	private CompletionType getElementType(IBinding binding) {
		if (binding instanceof ITypedef) {
			return new CompletionType(ICElement.C_TYPEDEF);
		} else if (binding instanceof ICompositeType) {
			if (((ICompositeType)binding).getKey() == ICPPClassType.k_class || binding instanceof ICPPClassTemplate)
				return new CompletionType(ICElement.C_CLASS);
			else if (((ICompositeType)binding).getKey() == ICompositeType.k_struct)
				return new CompletionType(ICElement.C_STRUCT);
			else if (((ICompositeType)binding).getKey() == ICompositeType.k_union)
				return new CompletionType(ICElement.C_UNION);
		} else if (binding instanceof ICPPMethod) {
			switch (((ICPPMethod)binding).getVisibility()) {
			case ICPPMember.v_private:
				return new CompletionType(ICElement.C_METHOD, Visibility.Private);
			case ICPPMember.v_protected:
				return new CompletionType(ICElement.C_METHOD, Visibility.Protected);
			default:
				return new CompletionType(ICElement.C_METHOD, Visibility.Public);
			}
		} else if (binding instanceof IFunction) {
			return new CompletionType(ICElement.C_FUNCTION);
		} else if (binding instanceof ICPPField) {
			switch (((ICPPField)binding).getVisibility()) {
			case ICPPMember.v_private:
				return new CompletionType(ICElement.C_FIELD, Visibility.Private);
			case ICPPMember.v_protected:
				return new CompletionType(ICElement.C_FIELD, Visibility.Protected);
			default:
				return new CompletionType(ICElement.C_FIELD, Visibility.Public);
			}
		} else if (binding instanceof IField) {
			return new CompletionType(ICElement.C_FIELD, Visibility.Public);
		} else if (binding instanceof IVariable) {
			return new CompletionType(ICElement.C_VARIABLE);
		} else if (binding instanceof IEnumeration) {
			return new CompletionType(ICElement.C_ENUMERATION);
		} else if (binding instanceof IEnumerator) {
			return new CompletionType(ICElement.C_ENUMERATOR);
		} else if (binding instanceof ICPPNamespace) {
			return new CompletionType(ICElement.C_NAMESPACE);
		} else if (binding instanceof ICPPFunctionTemplate) {
			return new CompletionType(ICElement.C_FUNCTION);
		} else if (binding instanceof ICPPUsingDeclaration) {
			IBinding[] delegates = ((ICPPUsingDeclaration)binding).getDelegates();
			if (delegates.length > 0)
				return getElementType(delegates[0]);
		}
		return null;
	}
	
	/**
	 * Compute base relevance depending on quality of name / prefix match.
	 * 
	 * @param prefix  the completion prefix
	 * @param match  the matching identifier
	 * @return a relevance value indicating the quality of the name match
	 */
	protected int computeBaseRelevance(String prefix, String match) {
		int baseRelevance= RelevanceConstants.DEFAULT_TYPE_RELEVANCE;
		boolean caseMatch= prefix.length() > 0 && match.startsWith(prefix);
		if (caseMatch) {
			baseRelevance += RelevanceConstants.CASE_MATCH_RELEVANCE;
		}
		boolean exactNameMatch= match.equalsIgnoreCase(prefix);
		if (exactNameMatch) {
			baseRelevance += RelevanceConstants.EXACT_NAME_MATCH_RELEVANCE;
		}
		return baseRelevance;
	}
}
