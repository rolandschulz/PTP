/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Nov 29, 2004
 */
package org.eclipse.fdt.internal.core.dom.parser.cpp;


import org.eclipse.fdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.fdt.core.dom.ast.DOMException;
import org.eclipse.fdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.fdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.fdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.fdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.fdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.fdt.core.dom.ast.IASTCastExpression;
import org.eclipse.fdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.fdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.fdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.fdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.fdt.core.dom.ast.IASTDeclaration;
import org.eclipse.fdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.fdt.core.dom.ast.IASTDeclarator;
import org.eclipse.fdt.core.dom.ast.IASTDoStatement;
import org.eclipse.fdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.fdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.fdt.core.dom.ast.IASTExpression;
import org.eclipse.fdt.core.dom.ast.IASTExpressionList;
import org.eclipse.fdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.fdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.fdt.core.dom.ast.IASTFieldReference;
import org.eclipse.fdt.core.dom.ast.IASTForStatement;
import org.eclipse.fdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.fdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.fdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.fdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.fdt.core.dom.ast.IASTIdExpression;
import org.eclipse.fdt.core.dom.ast.IASTIfStatement;
import org.eclipse.fdt.core.dom.ast.IASTInitializer;
import org.eclipse.fdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.fdt.core.dom.ast.IASTInitializerList;
import org.eclipse.fdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.fdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.fdt.core.dom.ast.IASTName;
import org.eclipse.fdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.fdt.core.dom.ast.IASTNode;
import org.eclipse.fdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.fdt.core.dom.ast.IASTPointer;
import org.eclipse.fdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.fdt.core.dom.ast.IASTProblem;
import org.eclipse.fdt.core.dom.ast.IASTProblemHolder;
import org.eclipse.fdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.fdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.fdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.fdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.fdt.core.dom.ast.IASTStatement;
import org.eclipse.fdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.fdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.fdt.core.dom.ast.IASTTypeId;
import org.eclipse.fdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.fdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.fdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.fdt.core.dom.ast.IArrayType;
import org.eclipse.fdt.core.dom.ast.IBasicType;
import org.eclipse.fdt.core.dom.ast.IBinding;
import org.eclipse.fdt.core.dom.ast.ICompositeType;
import org.eclipse.fdt.core.dom.ast.IEnumeration;
import org.eclipse.fdt.core.dom.ast.IFunction;
import org.eclipse.fdt.core.dom.ast.IFunctionType;
import org.eclipse.fdt.core.dom.ast.ILabel;
import org.eclipse.fdt.core.dom.ast.IParameter;
import org.eclipse.fdt.core.dom.ast.IPointerType;
import org.eclipse.fdt.core.dom.ast.IProblemBinding;
import org.eclipse.fdt.core.dom.ast.IQualifierType;
import org.eclipse.fdt.core.dom.ast.IScope;
import org.eclipse.fdt.core.dom.ast.IType;
import org.eclipse.fdt.core.dom.ast.ITypedef;
import org.eclipse.fdt.core.dom.ast.IVariable;
import org.eclipse.fdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTFunctionTryBlockDeclarator;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTNamespaceAlias;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTPointerToMember;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTTranslationUnit;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTTypenameExpression;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTVisitor;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTWhileStatement;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPCompositeBinding;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPFunctionScope;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.fdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.fdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.fdt.core.dom.ast.gnu.cpp.IGPPASTSimpleDeclSpecifier;
import org.eclipse.fdt.core.parser.util.ArrayUtil;
import org.eclipse.fdt.core.parser.util.CharArrayUtils;
import org.eclipse.fdt.internal.core.dom.parser.ITypeContainer;
import org.eclipse.fdt.internal.core.dom.parser.ProblemBinding;

/**
 * @author aniefer
 */
public class CPPVisitor implements ICPPASTVisitor {

	/**
	 * @param name
	 */
	public static IBinding createBinding(IASTName name) {
		IASTNode parent = name.getParent();
		IBinding binding = null;
		if( parent instanceof IASTNamedTypeSpecifier  ||
		    parent instanceof ICPPASTQualifiedName    ||
			parent instanceof ICPPASTBaseSpecifier 	  ||
			parent instanceof ICPPASTConstructorChainInitializer ) 
		{
			binding = CPPSemantics.resolveBinding( name ); 
			if( binding instanceof IProblemBinding && parent instanceof ICPPASTQualifiedName ){
				//if( ((IProblemBinding)binding).getID() == IProblemBinding.SEMANTIC_NAME_NOT_FOUND ){
					parent = parent.getParent();
				//}
			} else {
				return binding;
			}
		} 
		if( parent instanceof IASTIdExpression ){
			return resolveBinding( parent );
		} else if( parent instanceof ICPPASTFieldReference ){
			return resolveBinding( parent );
		} else if( parent instanceof ICPPASTCompositeTypeSpecifier ){
			return createBinding( (ICPPASTCompositeTypeSpecifier) parent );
		} else if( parent instanceof IASTDeclarator ){
			return createBinding( (IASTDeclarator) parent );
		} else if( parent instanceof ICPPASTElaboratedTypeSpecifier ){
			return createBinding( (ICPPASTElaboratedTypeSpecifier) parent );
		} else if( parent instanceof IASTDeclaration ){
			return createBinding( (IASTDeclaration) parent );
		} else if( parent instanceof IASTEnumerationSpecifier ){
		    return createBinding( (IASTEnumerationSpecifier) parent );
		} else if( parent instanceof IASTEnumerator ){
		    return createBinding( (IASTEnumerator) parent );
		} else if( parent instanceof IASTGotoStatement ){
		    return createBinding( (IASTGotoStatement) parent );
		} else if( parent instanceof IASTLabelStatement ){
		    return createBinding( (IASTLabelStatement) parent );
		}
		
		return null;
	}
	
	private static IBinding createBinding( IASTGotoStatement gotoStatement ) {
	    ICPPFunctionScope functionScope = (ICPPFunctionScope) getContainingScope( gotoStatement );
	    IASTName name = gotoStatement.getName();
	    IBinding binding;
        try {
            binding = functionScope.getBinding( name, false );
            if( binding == null ){
    	        binding = new CPPLabel( name );
    	        functionScope.addName( name );
    	    }
        } catch ( DOMException e ) {
            binding = e.getProblem();
        }
        
	    return binding;
	}
	
	private static IBinding createBinding( IASTLabelStatement labelStatement ) {
	    ICPPFunctionScope functionScope = (ICPPFunctionScope) getContainingScope( labelStatement );
	    IASTName name = labelStatement.getName();
	    IBinding binding;
        try {
            binding = functionScope.getBinding( name, false );
            if( binding == null ){
    	        binding = new CPPLabel( name );
    	        functionScope.addName( name );
    	    } else {
    	        ((CPPLabel)binding).setLabelStatement( name );
    	    }
        } catch ( DOMException e ) {
            binding = e.getProblem();
        }
        
	    return binding;
	}
	
    private static IBinding createBinding( IASTEnumerator enumerator ) {
        ICPPScope scope = (ICPPScope) getContainingScope( enumerator );
        IBinding enumtor;
        try {
            enumtor = scope.getBinding( enumerator.getName(), false );
            if( enumtor == null ){
                enumtor = new CPPEnumerator( enumerator.getName() );
                scope.addName( enumerator.getName() );
            }
        } catch ( DOMException e ) {
            enumtor = e.getProblem();
        }
        
        return enumtor;
    }


    private static IBinding createBinding( IASTEnumerationSpecifier specifier ) {
        ICPPScope scope = (ICPPScope) getContainingScope( specifier );
        IBinding enumeration;
        try {
            enumeration = scope.getBinding( specifier.getName(), false );
            if( enumeration == null ){
                enumeration = new CPPEnumeration( specifier.getName() );
                scope.addName( specifier.getName() );
            }
        } catch ( DOMException e ) {
            enumeration = e.getProblem();
        }
        
        return enumeration;
    }

    private static IBinding createBinding( ICPPASTElaboratedTypeSpecifier elabType ){
	    IASTNode parent = elabType.getParent();
	    IBinding binding = null;
	    boolean mustBeSimple = true;
	    boolean isFriend = false;
	    if( parent instanceof IASTSimpleDeclaration ){
	        IASTDeclarator [] dtors = ((IASTSimpleDeclaration)parent).getDeclarators();
	        ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) ((IASTSimpleDeclaration)parent).getDeclSpecifier();
	        isFriend = declSpec.isFriend() && dtors.length == 0;
	        if( dtors.length > 0 || isFriend ){
	        	binding = CPPSemantics.resolveBinding( elabType.getName() );
	        	mustBeSimple = !isFriend;
	        } else {
	        	mustBeSimple = false;
	        }
	    } else if( parent instanceof IASTParameterDeclaration || 
	    		   parent instanceof IASTDeclaration ||
				   parent instanceof IASTTypeId )
	    {
	    	binding = CPPSemantics.resolveBinding( elabType.getName() );
	    }
	    
		if( binding != null && 
		    (!(binding instanceof IProblemBinding) ||((IProblemBinding)binding).getID() != IProblemBinding.SEMANTIC_NAME_NOT_FOUND) )
		{
			return binding;
    	}
		
		//7.1.5.3-2 ... If name lookup does not find a declaration for the name, the elaborated-type-specifier is ill-formed
		//unless it is of the simple form class-key identifier
	    if( mustBeSimple && elabType.getName() instanceof ICPPASTQualifiedName )
	    	return binding;
	    
		ICPPScope scope = (ICPPScope) getContainingScope( elabType );
		
		if( mustBeSimple ){
			//3.3.1-5 ... the identifier is declared in the smallest non-class non-function-prototype scope that contains
			//the declaration
			while( scope instanceof ICPPClassScope || scope instanceof ICPPFunctionScope ){
				try {
					scope = (ICPPScope) scope.getParent();
				} catch (DOMException e1) {
				}
			}
		}
		if( scope instanceof ICPPClassScope && isFriend ){
	        try {
	            while( scope instanceof ICPPClassScope )
	                scope = (ICPPScope) scope.getParent();
            } catch ( DOMException e1 ) {
		    }
		}
        try {
            binding = scope.getBinding( elabType.getName(), false );
            if( binding == null ){
    			if( elabType.getKind() != IASTElaboratedTypeSpecifier.k_enum ){
    				binding = new CPPClassType( elabType.getName() );
    				scope.addName( elabType.getName() );
    			}
    		} else if( binding instanceof ICPPClassType ){
    			((CPPClassType)binding).addDeclaration( elabType );
    		}
        } catch ( DOMException e ) {
            binding = e.getProblem();
        }
        
		return binding;
	}
	private static IBinding createBinding( ICPPASTCompositeTypeSpecifier compType ){
		IASTName name = compType.getName();
		if( name instanceof ICPPASTQualifiedName ){
			IASTName [] ns = ((ICPPASTQualifiedName)name).getNames();
			name = ns[ ns.length - 1 ];
		}
		ICPPScope scope = (ICPPScope) getContainingScope( name );
		IBinding binding;
        try {
            binding = scope.getBinding( compType.getName(), false );
            if( binding == null || !(binding instanceof ICPPClassType) ){
    			binding = new CPPClassType( compType.getName() );
    			scope.addName( compType.getName() );
    		} else {
    			((CPPClassType)binding).addDefinition( compType );
    		}
        } catch ( DOMException e ) {
            binding = e.getProblem();
        }
        
		return binding;
	}
	private static IBinding createBinding( IASTDeclaration declaration ){
		if( declaration instanceof ICPPASTNamespaceDefinition ){
			ICPPASTNamespaceDefinition namespaceDef = (ICPPASTNamespaceDefinition) declaration;
			ICPPScope scope = (ICPPScope) getContainingScope( namespaceDef );
			IBinding binding;
            try {
                binding = scope.getBinding( namespaceDef.getName(), false );
                if( binding == null ){
    				binding = new CPPNamespace( namespaceDef.getName() );
    				scope.addName( namespaceDef.getName() );
    			}
            } catch ( DOMException e ) {
                binding = e.getProblem();
            }
			return binding;
		} else if( declaration instanceof ICPPASTUsingDirective ){
			return CPPSemantics.resolveBinding( ((ICPPASTUsingDirective) declaration).getQualifiedName() );
		} else if( declaration instanceof ICPPASTNamespaceAlias ) {
			ICPPASTNamespaceAlias alias = (ICPPASTNamespaceAlias) declaration;
			return CPPSemantics.resolveBinding( alias.getQualifiedName() );
		}
		
			
		return null;
	}
	private static IBinding createBinding( IASTDeclarator declarator ){
		IASTNode parent = declarator.getParent();
		
		if( parent instanceof IASTTypeId )
		    return CPPSemantics.resolveBinding( declarator.getName() );
		    
		while( declarator.getNestedDeclarator() != null )
			declarator = declarator.getNestedDeclarator();

		IASTName name = declarator.getName();
		if( name instanceof ICPPASTQualifiedName ){
			IASTName [] ns = ((ICPPASTQualifiedName)name).getNames();
			name = ns[ ns.length - 1 ];
		}
		
		while( parent instanceof IASTDeclarator ){
			parent = parent.getParent();
		}
		
		ICPPScope scope = (ICPPScope) getContainingScope( parent );
		if( parent instanceof IASTSimpleDeclaration && scope instanceof ICPPClassScope ){
		    ICPPASTDeclSpecifier declSpec = (ICPPASTDeclSpecifier) ((IASTSimpleDeclaration)parent).getDeclSpecifier();
		    if( declSpec.isFriend() ){
		        try {
                    scope = (ICPPScope) scope.getParent();
                } catch ( DOMException e1 ) {
                }
		    }
		}
		
		ASTNodeProperty prop = parent.getPropertyInParent();
		if( prop == IASTDeclarationStatement.DECLARATION ){
		    //implicit scope, see 6.4-1
		    prop = parent.getParent().getPropertyInParent();
		    if( prop != IASTCompoundStatement.NESTED_STATEMENT )
		    	scope = null;
		}
		
		IBinding binding;
        try {
            binding = ( scope != null ) ? scope.getBinding( declarator.getName(), false ) : null;
        } catch ( DOMException e ) {
            binding = null;
        }
        
        if( declarator instanceof ICPPASTFunctionDeclarator ){
			if( binding != null && binding instanceof IFunction ){
			    IFunction function = (IFunction) binding;
			    IFunctionType ftype;
                try {
                    ftype = function.getType();
                    IType type = createType( declarator );
    			    if( ftype.equals( type ) ){
    			        if( parent instanceof IASTSimpleDeclaration )
    			            ((CPPFunction)function).addDeclaration( (ICPPASTFunctionDeclarator) declarator );
    			        else 
    			            ((CPPFunction)function).addDefinition( (ICPPASTFunctionDeclarator) declarator );
    			        
    			        return function;
    			    }
                } catch ( DOMException e1 ) {
                }
			} 
			if( scope instanceof ICPPClassScope ){
				if( isConstructor( scope, declarator) )
					binding = new CPPConstructor( (ICPPASTFunctionDeclarator) declarator );
				else 
					binding = new CPPMethod( (ICPPASTFunctionDeclarator) declarator );
			} else {
				binding = new CPPFunction( (ICPPASTFunctionDeclarator) declarator );
			}
		} else if( parent instanceof IASTParameterDeclaration ){
			IASTParameterDeclaration param = (IASTParameterDeclaration) parent;
			IASTStandardFunctionDeclarator fDtor = (IASTStandardFunctionDeclarator) param.getParent();
			if( fDtor.getParent() instanceof IASTDeclarator || fDtor.getNestedDeclarator() != null )
			    return null;
			IBinding temp = fDtor.getName().resolveBinding();
			if( temp instanceof IFunction ){
				CPPFunction function = (CPPFunction) temp;
				binding = function.resolveParameter( param );
			}
		} else if( parent instanceof IASTSimpleDeclaration ){
		    
			IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) parent;			
			if( simpleDecl.getDeclSpecifier().getStorageClass() == IASTDeclSpecifier.sc_typedef ){
				binding = new CPPTypedef( declarator.getName() );
			} else {
			    IType t1 = null, t2 = null;
			    
			    if( binding != null && binding instanceof IVariable ){
			        t1 = createType( declarator );
			        try {
                        t2 = ((IVariable)binding).getType();
                    } catch ( DOMException e1 ) {
                    }
			    }
			    if( t1 != null && t2 != null && t1.equals( t2 ) ){
			        ((CPPVariable)binding).addDeclaration( declarator.getName() );
			    } else if( simpleDecl.getParent() instanceof ICPPASTCompositeTypeSpecifier ){
					binding = new CPPField( declarator.getName() ); 
			    } else {
			        binding = new CPPVariable( declarator.getName() );
			    }
			}
		} 

		if( scope != null && binding != null ){
            try {
                scope.addName( name );
            } catch ( DOMException e1 ) {
            }
		}
		
		return binding;
	}

	public static boolean isConstructor( IScope containingScope, IASTDeclarator declarator ){
	    if( containingScope == null || !(containingScope instanceof ICPPClassScope) )
	        return false;
	    
	    ICPPASTCompositeTypeSpecifier clsTypeSpec;
        try {
            clsTypeSpec = (ICPPASTCompositeTypeSpecifier) ((ICPPClassScope)containingScope).getPhysicalNode();
        } catch ( DOMException e ) {
            return false;
        }
        return isConstructor( clsTypeSpec.getName(), declarator );
	}
	public static boolean isConstructor( IASTName parentName, IASTDeclarator declarator ){
	    if( declarator == null      || !(declarator instanceof IASTFunctionDeclarator) )
	        return false;
        
	    IASTName name = declarator.getName();
	    if( name instanceof ICPPASTQualifiedName ){
	        IASTName [] names = ((ICPPASTQualifiedName)name).getNames(); 
	        name = names[ names.length - 1 ];
	    }
	    if( !CharArrayUtils.equals( name.toCharArray(), parentName.toCharArray() ) )
	        return false;
	    
	    IASTDeclSpecifier declSpec = null;
	    IASTNode parent = declarator.getParent();
	    if( parent instanceof IASTSimpleDeclaration ){
	        declSpec = ((IASTSimpleDeclaration)parent).getDeclSpecifier();
	    } else if( parent instanceof IASTFunctionDefinition ){
	        declSpec = ((IASTFunctionDefinition)parent).getDeclSpecifier();
	    }
	    if( declSpec != null && declSpec instanceof IASTSimpleDeclSpecifier ){
	        return ( ((IASTSimpleDeclSpecifier)declSpec).getType() == IASTSimpleDeclSpecifier.t_unspecified ); 
	    }
	    
	    return false;
	    
	}
	
	public static IScope getContainingScope( IASTNode node ){
		if( node == null )
			return null;
		while( node != null ){
			if( node instanceof IASTName ){
				return getContainingScope( (IASTName) node );
			} else if( node instanceof IASTDeclaration ){
				IASTNode parent = node.getParent();
				if( parent instanceof IASTTranslationUnit ){
					return ((IASTTranslationUnit)parent).getScope();
				} else if( parent instanceof IASTDeclarationStatement ){
					return getContainingScope( (IASTStatement) parent );
				} else if( parent instanceof IASTForStatement ){
				    return ((IASTForStatement)parent).getScope();
				} else if( parent instanceof IASTCompositeTypeSpecifier ){
				    return ((IASTCompositeTypeSpecifier)parent).getScope();
				} else if( parent instanceof ICPPASTNamespaceDefinition ) {
					return ((ICPPASTNamespaceDefinition)parent).getScope();
				}
			} else if( node instanceof IASTStatement ){
		        return getContainingScope( (IASTStatement) node ); 
			} else if( node instanceof IASTTypeId ){
				node = node.getParent();
			} else if( node instanceof IASTParameterDeclaration ){
				ICPPASTFunctionDeclarator dtor = (ICPPASTFunctionDeclarator) node.getParent();
				ASTNodeProperty prop = dtor.getPropertyInParent();
				if( prop == IASTSimpleDeclaration.DECLARATOR )
				    return dtor.getFunctionScope();
				else if( prop == IASTFunctionDefinition.DECLARATOR )
				    return ((IASTCompoundStatement)((IASTFunctionDefinition)dtor.getParent()).getBody()).getScope();
			} else if( node instanceof IASTInitializerExpression ){
			    IASTNode parent = node.getParent();
			    while( !(parent instanceof IASTDeclarator) )
			        parent = parent.getParent();
	    	    IASTDeclarator dtor = (IASTDeclarator) parent;
	    	    IASTName name = dtor.getName();
	    	    if( name instanceof ICPPASTQualifiedName ){
	    	        IASTName [] ns = ((ICPPASTQualifiedName)name).getNames();
	    	        return getContainingScope( ns[ ns.length - 1 ] );
	    	    }
			} else if( node instanceof IASTExpression ){
		    	IASTNode parent = node.getParent();
			    if( parent instanceof IASTForStatement ){
			        return ((IASTForStatement)parent).getScope();
			    } else if( parent instanceof IASTCompoundStatement ){
			        return ((IASTCompoundStatement)parent).getScope();
			    } else if( parent instanceof ICPPASTConstructorChainInitializer ){
			    	IASTNode temp = getContainingBlockItem( parent.getParent() );
			    	if( temp instanceof IASTFunctionDefinition ){
			    		IASTCompoundStatement body = (IASTCompoundStatement) ((IASTFunctionDefinition)temp).getBody();
			    		return body.getScope();
			    	}
			    }
		    } 
		    node = node.getParent();
		}
	    return null;
	}
	
	public static IScope getContainingScope( IASTName name ){
		IASTNode parent = name.getParent();
		try {
			if( parent instanceof ICPPASTQualifiedName ){
				IASTName [] names = ((ICPPASTQualifiedName) parent).getNames();
				int i = 0;
				for( ; i < names.length; i++ ){
					if( names[i] == name ) break;
				}
				if( i > 0 ){
					IBinding binding = names[i - 1].resolveBinding();
					if( binding instanceof ICPPClassType ){
						return ((ICPPClassType)binding).getCompositeScope();
					} else if( binding instanceof ICPPNamespace ){
						return ((ICPPNamespace)binding).getNamespaceScope();
					} else if( binding instanceof IProblemBinding ){
						if( binding instanceof ICPPScope )
							return (IScope) binding;
						return new CPPScope.CPPScopeProblem( -1, names[i-1].toCharArray() );
					}
				}
				else if( ((ICPPASTQualifiedName)parent).isFullyQualified() )
				{
				   return parent.getTranslationUnit().getScope();
				}
			} else if( parent instanceof ICPPASTFieldReference ){
				IASTExpression owner = ((ICPPASTFieldReference)parent).getFieldOwner();
				IType type = CPPSemantics.getUltimateType( getExpressionType( owner ), false );
				if( type instanceof ICPPClassType ){
					return ((ICPPClassType) type).getCompositeScope();
				}
			}
		} catch( DOMException e ){
		    return e.getProblem();
		}
		return getContainingScope( parent );
	}

	public static IScope getContainingScope( IASTStatement statement ){
		IASTNode parent = statement.getParent();
		IScope scope = null;
		if( parent instanceof IASTCompoundStatement ){
		    IASTCompoundStatement compound = (IASTCompoundStatement) parent;
		    scope = compound.getScope();
		} else if( parent instanceof IASTForStatement ){
		    scope = ((IASTForStatement)parent).getScope();
		} else if( parent instanceof IASTStatement ){
			scope = getContainingScope( (IASTStatement)parent );
		} else if( parent instanceof IASTFunctionDefinition ){
		    IASTFunctionDeclarator fnDeclarator = ((IASTFunctionDefinition) parent ).getDeclarator();
		    IASTName name = fnDeclarator.getName();
		    if( name instanceof ICPPASTQualifiedName ){
		        IASTName [] ns = ((ICPPASTQualifiedName)name).getNames();
		        name = ns [ ns.length -1 ];
		    }
		    return getContainingScope( name );
		}
		
		if( statement instanceof IASTGotoStatement || statement instanceof IASTLabelStatement ){
		    while( !(parent instanceof IASTFunctionDefinition) ){
		        parent = parent.getParent();
		    }
		    IASTFunctionDefinition fdef = (IASTFunctionDefinition) parent;
		    return ((ICPPASTFunctionDeclarator)fdef.getDeclarator()).getFunctionScope();
		}
		
		return scope;
	}
	
	public static IASTNode getContainingBlockItem( IASTNode node ){
		IASTNode parent = node.getParent();
		if( parent == null )
			return null;
		while( parent != null ) {
			if( parent instanceof IASTDeclaration ){
				IASTNode p = parent.getParent();
				if( p instanceof IASTDeclarationStatement )
					return p;
				return parent;
			} else if( parent instanceof IASTExpression ){
				IASTNode p = parent.getParent();
				if( p instanceof IASTForStatement )
				    return parent;
				if( p instanceof IASTStatement )
					return p;
			} else if ( parent instanceof IASTStatement || parent instanceof IASTTranslationUnit ) {
				return parent;
			} else if( parent instanceof IASTFunctionDeclarator ){
			    return node;
			} else if( parent instanceof IASTEnumerationSpecifier.IASTEnumerator ){
			    return parent;
			}
			node = parent;
			parent = node.getParent();
		}
		return null;
	}
	
	static private IBinding resolveBinding( IASTNode node ){
		IASTName name = null;
		while( node != null ) {
			if( node instanceof IASTIdExpression ){
				name = ((IASTIdExpression) node).getName();
				break;
				//return CPPSemantics.resolveBinding( ((IASTIdExpression)node).getName() );
			} else if( node instanceof ICPPASTFieldReference ){
				name = ((ICPPASTFieldReference)node).getFieldName();
				break;
			} else if( node instanceof IASTFunctionCallExpression ){
				node = ((IASTFunctionCallExpression)node).getFunctionNameExpression();
			} else if( node instanceof IASTUnaryExpression ){
				node = ((IASTUnaryExpression)node).getOperand();
			} else if( node instanceof IASTBinaryExpression ){
				node = ((IASTBinaryExpression)node).getOperand2();
			} else
				node = null;
		}
		if( name != null ){
			IBinding binding = CPPSemantics.resolveBinding( name );
			if( name instanceof ICPPASTQualifiedName ){
				IASTName ns [] = ((ICPPASTQualifiedName)name).getNames();
				name = ns[ ns.length - 1 ];
			}
			((CPPASTName)name).setBinding( binding );
			return binding;
		}
		return null;
	}
	
	public static class CollectProblemsAction extends CPPBaseVisitorAction {
		{
			processDeclarations = true;
			processExpressions = true;
			processStatements = true;
			processTypeIds = true;
		}
		
		private static final int DEFAULT_CHILDREN_LIST_SIZE = 8;
		private IASTProblem[] problems = null;
		int numFound = 0;

		public CollectProblemsAction() {
			problems = new IASTProblem[DEFAULT_CHILDREN_LIST_SIZE];
		}
		
		private void addProblem(IASTProblem problem) {
			if( problems.length == numFound ) // if the found array is full, then double the array
	        {
	            IASTProblem [] old = problems;
	            problems = new IASTProblem[ old.length * 2 ];
	            for( int j = 0; j < old.length; ++j )
	                problems[j] = old[j];
	        }
			problems[numFound++] = problem;
		}
		
	    private IASTProblem[] removeNullFromProblems() {
	    	if (problems[problems.length-1] != null) { // if the last element in the list is not null then return the list
				return problems;			
			} else if (problems[0] == null) { // if the first element in the list is null, then return empty list
				return new IASTProblem[0];
			}
			
			IASTProblem[] results = new IASTProblem[numFound];
			for (int i=0; i<results.length; i++)
				results[i] = problems[i];
				
			return results;
	    }
		
		public IASTProblem[] getProblems() {
			return removeNullFromProblems();
		}
	    
		/* (non-Javadoc)
		 * @see org.eclipse.fdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processDeclaration(org.eclipse.fdt.core.dom.ast.IASTDeclaration)
		 */
		public int processDeclaration(IASTDeclaration declaration) {
			if ( declaration instanceof IASTProblemHolder )
				addProblem(((IASTProblemHolder)declaration).getProblem());

			return PROCESS_CONTINUE;
		}
		/* (non-Javadoc)
		 * @see org.eclipse.fdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processExpression(org.eclipse.fdt.core.dom.ast.IASTExpression)
		 */
		public int processExpression(IASTExpression expression) {
			if ( expression instanceof IASTProblemHolder )
				addProblem(((IASTProblemHolder)expression).getProblem());

			return PROCESS_CONTINUE;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.fdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processStatement(org.eclipse.fdt.core.dom.ast.IASTStatement)
		 */
		public int processStatement(IASTStatement statement) {
			if ( statement instanceof IASTProblemHolder )
				addProblem(((IASTProblemHolder)statement).getProblem());

			return PROCESS_CONTINUE;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.fdt.internal.core.dom.parser.c.CVisitor.CBaseVisitorAction#processTypeId(org.eclipse.fdt.core.dom.ast.IASTTypeId)
		 */
		public int processTypeId(IASTTypeId typeId) {
			if ( typeId instanceof IASTProblemHolder )
				addProblem(((IASTProblemHolder)typeId).getProblem());

			return PROCESS_CONTINUE;
		}
	}
	
	public static class CollectDeclarationsAction extends CPPBaseVisitorAction {
	    private static final int DEFAULT_LIST_SIZE = 8;
		private IASTName [] decls;
		private IBinding binding;
		private int idx = 0;
		private int kind;
		
		private static final int KIND_LABEL  = 1;
		private static final int KIND_OBJ_FN = 2;
		private static final int KIND_TYPE   = 3;
		private static final int KIND_NAMESPACE   = 4;
		private static final int KIND_COMPOSITE = 5;
		
		
		public CollectDeclarationsAction( IBinding binding ){
			this.binding = binding;
			this.decls = new IASTName[ DEFAULT_LIST_SIZE ];
			
			processNames = true;
			if( binding instanceof ILabel )
				kind = KIND_LABEL;
			else if( binding instanceof ICompositeType || 
					 binding instanceof ITypedef || 
					 binding instanceof IEnumeration)
			{
				kind = KIND_TYPE;
			}
			else if( binding instanceof ICPPNamespace) {
				kind = KIND_NAMESPACE;
			}
			else if( binding instanceof ICPPCompositeBinding )
			    kind = KIND_COMPOSITE;
			else 
				kind = KIND_OBJ_FN;
		}
		
		public int processName( IASTName name ){
			if( name instanceof ICPPASTQualifiedName ) return PROCESS_CONTINUE;
			
			ASTNodeProperty prop = name.getPropertyInParent();
			if( prop == ICPPASTQualifiedName.SEGMENT_NAME )
				prop = name.getParent().getPropertyInParent();
			
			switch( kind ){
				case KIND_LABEL:
					if( prop == IASTLabelStatement.NAME )
						break;
					return PROCESS_CONTINUE;
				case KIND_TYPE:
				case KIND_COMPOSITE:
				    if( prop == IASTCompositeTypeSpecifier.TYPE_NAME ||
				        prop == IASTEnumerationSpecifier.ENUMERATION_NAME )
				    {
				        break;
				    } else if( prop == IASTElaboratedTypeSpecifier.TYPE_NAME ){
						IASTNode p = name.getParent().getParent();
						if( p instanceof IASTSimpleDeclaration &&
							((IASTSimpleDeclaration)p).getDeclarators().length == 0 )
						{
							break;
						}
					} else if( prop == IASTDeclarator.DECLARATOR_NAME ){
					    IASTNode p = name.getParent().getParent();
					    if( p instanceof IASTSimpleDeclaration ){
					        IASTDeclSpecifier declSpec = ((IASTSimpleDeclaration)p).getDeclSpecifier();
					        if( declSpec.getStorageClass() == IASTDeclSpecifier.sc_typedef )
					            break;
					    }
					}
        
					if( kind == KIND_TYPE )
					    return PROCESS_CONTINUE;
				case KIND_OBJ_FN:
					if( prop == IASTDeclarator.DECLARATOR_NAME ||
					    prop == IASTEnumerationSpecifier.IASTEnumerator.ENUMERATOR_NAME )
					{
						break;
					}
					return PROCESS_CONTINUE;
				case KIND_NAMESPACE:
					if( prop == ICPPASTNamespaceDefinition.NAMESPACE_NAME ||
					    prop == ICPPASTNamespaceAlias.ALIAS_NAME ) 
					{
						break;
					}					
					return PROCESS_CONTINUE;
			}
			
			if( binding != null )
			{
			    IBinding candidate = name.resolveBinding();

		        boolean found = false;
		        if( binding instanceof ICPPCompositeBinding ){
                    try {
                        found = ArrayUtil.contains( ((ICPPCompositeBinding)binding).getBindings(), candidate ); 
                    } catch ( DOMException e ) {
                    }
			    } else {
			        found = ( binding == candidate );   
			    }
			        
			    if( found ){
					if( decls.length == idx ){
						IASTName [] temp = new IASTName[ decls.length * 2 ];
						System.arraycopy( decls, 0, temp, 0, decls.length );
						decls = temp;
					}
					decls[idx++] = name;
			    }
			}
			return PROCESS_CONTINUE;
		}
		public IASTName[] getDeclarations(){
			if( idx < decls.length ){
				IASTName [] temp = new IASTName[ idx ];
				System.arraycopy( decls, 0, temp, 0, idx );
				decls = temp;
			}
			return decls;
		}

	}
	public static class CollectReferencesAction extends CPPBaseVisitorAction {
		private static final int DEFAULT_LIST_SIZE = 8;
		private IASTName [] refs;
		private IBinding binding;
		private int idx = 0;
		private int kind;
		
		private static final int KIND_LABEL  = 1;
		private static final int KIND_OBJ_FN = 2;
		private static final int KIND_TYPE   = 3;
		private static final int KIND_NAMESPACE   = 4;
		private static final int KIND_COMPOSITE = 5;
		
		
		public CollectReferencesAction( IBinding binding ){
			this.binding = binding;
			this.refs = new IASTName[ DEFAULT_LIST_SIZE ];
			
			processNames = true;
			if( binding instanceof ILabel )
				kind = KIND_LABEL;
			else if( binding instanceof ICompositeType || 
					 binding instanceof ITypedef || 
					 binding instanceof IEnumeration)
			{
				kind = KIND_TYPE;
			}
			else if( binding instanceof ICPPNamespace) {
				kind = KIND_NAMESPACE;
			} else if( binding instanceof ICPPCompositeBinding )
			    kind = KIND_COMPOSITE;
			else 
				kind = KIND_OBJ_FN;
		}
		
		public int processName( IASTName name ){
			if( name instanceof ICPPASTQualifiedName ) return PROCESS_CONTINUE;
			
			ASTNodeProperty prop = name.getPropertyInParent();
			ASTNodeProperty p2 = null;
			if( prop == ICPPASTQualifiedName.SEGMENT_NAME ){
			    p2 = prop;
				prop = name.getParent().getPropertyInParent();
			}
			
			switch( kind ){
				case KIND_LABEL:
					if( prop == IASTGotoStatement.NAME )
						break;
					return PROCESS_CONTINUE;
				case KIND_TYPE:
				case KIND_COMPOSITE:
					if( prop == IASTNamedTypeSpecifier.NAME || 
						prop == ICPPASTPointerToMember.NAME ||
						prop == ICPPASTTypenameExpression.TYPENAME ||
						prop == ICPPASTUsingDeclaration.NAME ||
						prop == ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier.NAME ||
						p2 == ICPPASTQualifiedName.SEGMENT_NAME )
					{
						break;
					} 
					else if( prop == IASTElaboratedTypeSpecifier.TYPE_NAME )
					{
						IASTNode p = name.getParent().getParent();
						if( !(p instanceof IASTSimpleDeclaration) ||
							((IASTSimpleDeclaration)p).getDeclarators().length > 0 )
						{
							break;
						}
					}
					if( kind == KIND_TYPE )
					    return PROCESS_CONTINUE;
				case KIND_OBJ_FN:
					if( prop == IASTIdExpression.ID_NAME || 
						prop == IASTFieldReference.FIELD_NAME || 
						prop == ICPPASTUsingDirective.QUALIFIED_NAME ||
						prop == ICPPASTUsingDeclaration.NAME ||
						prop == IASTFunctionCallExpression.FUNCTION_NAME ||
						prop == ICPPASTUsingDeclaration.NAME ||
						prop == IASTNamedTypeSpecifier.NAME ||
						prop == ICPPASTConstructorChainInitializer.MEMBER_ID)
					{
						break;
					}
					return PROCESS_CONTINUE;
				case KIND_NAMESPACE:
					if( prop == ICPPASTUsingDirective.QUALIFIED_NAME ||
						prop == ICPPASTNamespaceAlias.MAPPING_NAME ||
						prop == ICPPASTUsingDeclaration.NAME ||
						p2 == ICPPASTQualifiedName.SEGMENT_NAME )
					{
						break;
					}					
					return PROCESS_CONTINUE;
			}
			
			if( binding != null ){
			    IBinding potential = name.resolveBinding();
			    IBinding [] bs = null;
			    IBinding candidate = null;
			    int n = -1;
			    if( potential instanceof ICPPCompositeBinding ){
			        try {
                        bs = ((ICPPCompositeBinding)potential).getBindings();
                    } catch ( DOMException e ) {
                        return PROCESS_CONTINUE;
                    }
			        candidate = bs[ ++n ];
			    } else {
			        candidate = potential;
			    }
			        
			    while( candidate != null ) {
			        boolean found = false;
			        if( binding instanceof ICPPCompositeBinding ){
	                    try {
	                        found = ArrayUtil.contains( ((ICPPCompositeBinding)binding).getBindings(), candidate ); 
	                    } catch ( DOMException e ) {
	                    }
				    } else {
				        found = ( binding == candidate );   
				    }
				        
				    if( found ){
						if( refs.length == idx ){
							IASTName [] temp = new IASTName[ refs.length * 2 ];
							System.arraycopy( refs, 0, temp, 0, refs.length );
							refs = temp;
						}
						refs[idx++] = name;
						break;
				    }
				    if( n > -1 && ++n < bs.length ){
				        candidate = bs[n];
				    } else break;
			    }
			}
			return PROCESS_CONTINUE;
		}
		public IASTName[] getReferences(){
			if( idx < refs.length ){
				IASTName [] temp = new IASTName[ idx ];
				System.arraycopy( refs, 0, temp, 0, idx );
				refs = temp;
			}
			return refs;
		}
	}

	private IASTTranslationUnit tu = null;
	public CPPVisitor( ICPPASTTranslationUnit tu ){
	    this.tu = tu; 
	}
	
	public void visitTranslationUnit( BaseVisitorAction action ){
		IASTDeclaration [] decls = tu.getDeclarations();
		for( int i = 0; i < decls.length; i++ ){
			if( !visitDeclaration( decls[i], action ) ) return;
		}
	}

	public boolean visitNamespaceDefinition( ICPPASTNamespaceDefinition namespace, BaseVisitorAction action ){
	    if( action instanceof CPPBaseVisitorAction && ((CPPBaseVisitorAction)action).processNamespaces ){
	        switch( ((CPPBaseVisitorAction)action).processNamespace( namespace ) ){
	            case BaseVisitorAction.PROCESS_ABORT : return false;
	            case BaseVisitorAction.PROCESS_SKIP  : return true;
	            default : break;
	        }
	    }
	        
		if( !visitName( namespace.getName(), action ) ) return false;
		IASTDeclaration [] decls = namespace.getDeclarations();
		for( int i = 0; i < decls.length; i++ ){
			if( !visitDeclaration( decls[i], action ) ) return false;
		}
	   return true;
	}
	/**
	 * @param declaration
	 * @param action
	 * @return
	 */
	public boolean visitDeclaration(IASTDeclaration declaration, BaseVisitorAction action) {
	    if( declaration instanceof ICPPASTNamespaceDefinition )
	        return visitNamespaceDefinition( (ICPPASTNamespaceDefinition) declaration, action );
	    
		if( action.processDeclarations ) {
		    switch( action.processDeclaration( declaration ) ){
	            case BaseVisitorAction.PROCESS_ABORT : return false;
	            case BaseVisitorAction.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
		
		if( declaration instanceof IASTSimpleDeclaration ){
			IASTSimpleDeclaration simple = (IASTSimpleDeclaration) declaration;
			if( !visitDeclSpecifier( simple.getDeclSpecifier(), action ) ) return false;
			IASTDeclarator [] dtors = simple.getDeclarators();
			for( int i = 0; i < dtors.length; i++ ){
				if( !visitDeclarator( dtors[i], action) ) return false;
			}
		} else if( declaration instanceof IASTFunctionDefinition ){
			IASTFunctionDefinition function = (IASTFunctionDefinition) declaration;
			if( !visitDeclSpecifier( function.getDeclSpecifier(), action ) ) return false;
			if( !visitDeclarator( function.getDeclarator(), action ) ) return false;
			if( !visitStatement( function.getBody(), action ) ) return false;
		} else if( declaration instanceof ICPPASTUsingDeclaration ){
			if( !visitName( ((ICPPASTUsingDeclaration)declaration).getName(), action ) ) return false;
		} else if( declaration instanceof ICPPASTUsingDirective ){
			if( !visitName( ((ICPPASTUsingDirective)declaration).getQualifiedName(), action ) ) return false;
		} else if( declaration instanceof ICPPASTNamespaceAlias ){
			ICPPASTNamespaceAlias alias = (ICPPASTNamespaceAlias) declaration;
			if( !visitName( alias.getAlias(), action ) ) return false;
			if( !visitName( alias.getQualifiedName(), action ) ) return false;
		} else if( declaration instanceof ICPPASTLinkageSpecification ){
			IASTDeclaration [] decls = ((ICPPASTLinkageSpecification) declaration).getDeclarations();
			for( int i = 0; i < decls.length; i++ ){
				if( !visitDeclaration( decls[i], action ) ) return false;
			}
		} else if( declaration instanceof ICPPASTTemplateDeclaration ){
			ICPPASTTemplateDeclaration template = (ICPPASTTemplateDeclaration) declaration;
			ICPPASTTemplateParameter [] params = template.getTemplateParameters();
			for( int i = 0; i < params.length; i++ ){
				if( !visitTemplateParameter( params[i], action ) ) return false;
			}
			if( !visitDeclaration( template.getDeclaration(), action ) ) return false;
		} else if( declaration instanceof ICPPASTTemplateSpecialization ){
			if( !visitDeclaration( ((ICPPASTTemplateSpecialization) declaration).getDeclaration(), action ) ) return false;
		}
		return true;
	}

	/**
	 * @param name
	 * @param action
	 * @return
	 */
	public boolean visitName(IASTName name, BaseVisitorAction action) {
		if( action.processNames ){
		    switch( action.processName( name ) ){
	            case BaseVisitorAction.PROCESS_ABORT : return false;
	            case BaseVisitorAction.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
			
		if( name instanceof ICPPASTQualifiedName ){
			IASTName [] names = ((ICPPASTQualifiedName)name).getNames();
			for( int i = 0; i < names.length; i++ ){
				if( i == names.length - 1 ){
					if( names[i].toCharArray().length > 0 && !visitName( names[i], action ) ) return false;
				} else if( !visitName( names[i], action ) ) return false;
			}
		}
		return true;
	}

	/**
	 * @param declSpecifier
	 * @param action
	 * @return
	 */
	public boolean visitDeclSpecifier(IASTDeclSpecifier declSpecifier, BaseVisitorAction action) {
		if( action.processDeclSpecifiers ){
		    switch( action.processDeclSpecifier( declSpecifier ) ){
	            case BaseVisitorAction.PROCESS_ABORT : return false;
	            case BaseVisitorAction.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
			
		if( declSpecifier instanceof ICPPASTCompositeTypeSpecifier ){
			ICPPASTCompositeTypeSpecifier composite = (ICPPASTCompositeTypeSpecifier) declSpecifier;
			if( !visitName( composite.getName(), action ) ) return false;
			ICPPASTBaseSpecifier [] bases = composite.getBaseSpecifiers();
			for( int i = 0; i < bases.length; i++ ) {
				if( !visitBaseSpecifier( bases[i], action ) ) return false;
			}
			IASTDeclaration [] decls = composite.getMembers();
			for( int i = 0; i < decls.length; i++ ){
				if( !visitDeclaration( decls[i], action ) ) return false;
			}
		} else if( declSpecifier instanceof ICPPASTElaboratedTypeSpecifier ){
			if( !visitName( ((ICPPASTElaboratedTypeSpecifier) declSpecifier).getName(), action ) ) return false;
		} else if( declSpecifier instanceof IASTEnumerationSpecifier ){
			IASTEnumerationSpecifier enumeration = (IASTEnumerationSpecifier) declSpecifier;
			if( !visitName( enumeration.getName(), action ) ) return false;
			IASTEnumerator [] etors = enumeration.getEnumerators();
			for( int i = 0; i < etors.length; i++ ){
				if( !visitEnumerator( etors[i], action ) ) return false;
			}
		} else if( declSpecifier instanceof ICPPASTNamedTypeSpecifier ){
			if( !visitName( ((ICPPASTNamedTypeSpecifier)declSpecifier).getName(), action ) ) return false;
		} else if( declSpecifier instanceof IGPPASTSimpleDeclSpecifier ) {
			IASTExpression typeOf = ((IGPPASTSimpleDeclSpecifier)declSpecifier).getTypeofExpression();
			if( typeOf != null )
				if( !visitExpression( typeOf, action ) ) return false;
		}
		return true;
	}

	/**
	 * @param declarator
	 * @param action
	 * @return
	 */
	public boolean visitDeclarator(IASTDeclarator declarator, BaseVisitorAction action) {
		if( action.processDeclarators ){
		    switch( action.processDeclarator( declarator ) ){
	            case BaseVisitorAction.PROCESS_ABORT : return false;
	            case BaseVisitorAction.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
			
		IASTPointerOperator [] ptrs = declarator.getPointerOperators();
		if( ptrs.length > 0 ){
			for( int i = 0; i < ptrs.length; i++ ){
				if( ptrs[i] instanceof ICPPASTPointerToMember ){
					if( !visitName( ((ICPPASTPointerToMember) ptrs[i]).getName(), action ) ) return false;
				}
			}
		}
		
		if( declarator.getPropertyInParent() != IASTTypeId.ABSTRACT_DECLARATOR &&
			declarator.getNestedDeclarator() == null )
		{
			if( !visitName( declarator.getName(), action ) ) return false;
		}
		
		if( declarator.getNestedDeclarator() != null )
			if( !visitDeclarator( declarator.getNestedDeclarator(), action ) ) return false;
		
		if( declarator instanceof ICPPASTFunctionDeclarator ){
			ICPPASTFunctionDeclarator fdtor = (ICPPASTFunctionDeclarator) declarator;
		    IASTParameterDeclaration [] list = fdtor.getParameters();
			for( int i = 0; i < list.length; i++ ){
			    if( !visitParameterDeclaration( list[i], action ) ) return false;
			}
			ICPPASTConstructorChainInitializer [] ctorChain = fdtor.getConstructorChain();
			for( int i = 0; i < ctorChain.length; i++ ){
				if( !visitName( ctorChain[i].getMemberInitializerId(), action ) ) return false;
				if( !visitExpression( ctorChain[i].getInitializerValue(), action ) ) return false;
			}
			IASTTypeId [] typeIds = fdtor.getExceptionSpecification();
			for( int i = 0; i < typeIds.length; i++ ){
				if( !visitTypeId( typeIds[i], action ) ) return false;
			}
			
			if( declarator instanceof ICPPASTFunctionTryBlockDeclarator ){
				ICPPASTCatchHandler [] catchHandlers = ((ICPPASTFunctionTryBlockDeclarator)declarator).getCatchHandlers();
				for( int i = 0; i < catchHandlers.length; i++ ){
					if( !visitStatement( catchHandlers[i], action ) ) return false;
				}
			}
			
		}
		if( declarator instanceof IASTArrayDeclarator ){
			IASTArrayModifier [] mods = ((IASTArrayDeclarator) declarator).getArrayModifiers();
			for( int i = 0; i < mods.length; i++ ){
				if( mods[i].getConstantExpression() != null && !visitExpression( mods[i].getConstantExpression(), action ) ) return false;
			}
		}
		
		if( declarator.getInitializer() != null )
		    if( !visitInitializer( declarator.getInitializer(), action ) ) return false;
		    
		if( declarator instanceof IASTFieldDeclarator )
			if( ! visitExpression( ((IASTFieldDeclarator) declarator).getBitFieldSize(), action ) ) return false;
			
		return true;
	}
	
	private boolean visitIfStatement( IASTIfStatement ifStatement, BaseVisitorAction action ){
		while( ifStatement != null ){
			if( action.processStatements ){
			    switch( action.processStatement( ifStatement ) ){
			        case BaseVisitorAction.PROCESS_ABORT : return false;
			        case BaseVisitorAction.PROCESS_SKIP  : return true;
			        default : break;
			    }
		    }	
		    if( !visitExpression( ifStatement.getCondition(), action ) ) return false;
		    if( !visitStatement( ifStatement.getThenClause(), action ) ) return false;
		    if( ifStatement.getElseClause() != null ){
		    	IASTStatement statement = ifStatement.getElseClause();
		       	if( statement instanceof IASTIfStatement ){
		       		ifStatement = (IASTIfStatement) statement;
		       		continue;
		       	} 
		       	if( !visitStatement( statement, action ) ) return false;
		    }
		    ifStatement = null;
		}
		return true;
	}
	/**
	 * @param body
	 * @param action
	 * @return
	 */
	public boolean visitStatement(IASTStatement statement, BaseVisitorAction action) {
		//handle if's in a non-recursive manner to avoid stack overflows in case of huge number of elses
		if( statement instanceof IASTIfStatement )
			return visitIfStatement( (IASTIfStatement) statement, action );
		
		if( action.processStatements ){
		    switch( action.processStatement( statement ) ){
	            case BaseVisitorAction.PROCESS_ABORT : return false;
	            case BaseVisitorAction.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
			
		if( statement instanceof IASTCompoundStatement ){
			IASTStatement [] list = ((IASTCompoundStatement) statement).getStatements();
			for( int i = 0; i < list.length; i++ ){
			    if( list[i] == null ) break;
				if( !visitStatement( list[i], action ) ) return false;
			}
		} else if( statement instanceof IASTDeclarationStatement ){
			if( !visitDeclaration( ((IASTDeclarationStatement)statement).getDeclaration(), action ) ) return false;
		} else if( statement instanceof IASTExpressionStatement ){
		    if( !visitExpression( ((IASTExpressionStatement)statement).getExpression(), action ) ) return false;
		} else if( statement instanceof IASTCaseStatement ){
		    if( !visitExpression( ((IASTCaseStatement)statement).getExpression(), action ) ) return false;
		} else if( statement instanceof IASTDoStatement ){
		    if( !visitStatement( ((IASTDoStatement)statement).getBody(), action ) ) return false;
		    if( !visitExpression( ((IASTDoStatement)statement).getCondition(), action ) ) return false;
		} else if( statement instanceof IASTGotoStatement ){
		    if( !visitName( ((IASTGotoStatement)statement).getName(), action ) ) return false;
		} else if( statement instanceof IASTLabelStatement ){
		    if( !visitName( ((IASTLabelStatement)statement).getName(), action ) ) return false;
		} else if( statement instanceof IASTReturnStatement ){
		    if( !visitExpression( ((IASTReturnStatement) statement ).getReturnValue(), action ) ) return false;
		} else if( statement instanceof IASTSwitchStatement ){
		    if( !visitExpression( ((IASTSwitchStatement) statement ).getController(), action ) ) return false;
		    if( !visitStatement( ((IASTSwitchStatement) statement ).getBody(), action ) ) return false;
		} else if( statement instanceof IASTWhileStatement ){
		    IASTWhileStatement whileStatement = (IASTWhileStatement) statement;
			if( whileStatement.getCondition() != null && !visitExpression( (whileStatement ).getCondition(), action ) ) return false;
		    if( !visitStatement( (whileStatement ).getBody(), action ) ) return false;
		    if( whileStatement instanceof ICPPASTWhileStatement )
		    {
		    	ICPPASTWhileStatement cppWhile = ((ICPPASTWhileStatement)whileStatement);
		    	if ( cppWhile.getConditionDeclaration() != null && !visitDeclaration( cppWhile.getConditionDeclaration(), action ) ) return false; 
		    }
		} else if( statement instanceof IASTForStatement ){
		    IASTForStatement s = (IASTForStatement) statement;
		    if( s.getInitDeclaration() != null )
		        if( !visitDeclaration( s.getInitDeclaration(), action ) ) return false;
		    if( s.getInitExpression() != null )
		        if( !visitExpression( s.getInitExpression(), action ) ) return false;
		    if( !visitExpression( s.getCondition(), action ) ) return false;
		    if( !visitExpression( s.getIterationExpression(), action ) ) return false;
		    if( !visitStatement( s.getBody(), action ) ) return false;
		} else if( statement instanceof ICPPASTCatchHandler ){
			if( !visitDeclaration( ((ICPPASTCatchHandler) statement).getDeclaration(), action ) ) return false;
			if( !visitStatement( ((ICPPASTCatchHandler) statement).getCatchBody(), action ) ) return false;
		} else if( statement instanceof ICPPASTTryBlockStatement ){
			if( !visitStatement( ((ICPPASTTryBlockStatement)statement).getTryBody(), action ) ) return false;
			ICPPASTCatchHandler [] handlers = ((ICPPASTTryBlockStatement)statement).getCatchHandlers();
			for( int i = 0; i < handlers.length; i++ ){
				if( !visitStatement( handlers[i], action ) ) return false;
			}
		}
		
		return true;
	}

	/**
	 * @param typeOf
	 * @param action
	 * @return
	 */
	public boolean visitExpression(IASTExpression expression, BaseVisitorAction action) {
		if( action.processExpressions ){
		    switch( action.processExpression( expression ) ){
	            case BaseVisitorAction.PROCESS_ABORT : return false;
	            case BaseVisitorAction.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
			
		if( expression instanceof IASTArraySubscriptExpression ){
		    if( !visitExpression( ((IASTArraySubscriptExpression)expression).getArrayExpression(), action ) ) return false;
		    if( !visitExpression( ((IASTArraySubscriptExpression)expression).getSubscriptExpression(), action ) ) return false;
		} else if( expression instanceof IASTBinaryExpression ){
		    if( !visitExpression( ((IASTBinaryExpression)expression).getOperand1(), action ) ) return false;
		    if( !visitExpression( ((IASTBinaryExpression)expression).getOperand2(), action ) ) return false;
		} else if( expression instanceof IASTConditionalExpression){
		    if( !visitExpression( ((IASTConditionalExpression)expression).getLogicalConditionExpression(), action ) ) return false;
		    if( !visitExpression( ((IASTConditionalExpression)expression).getNegativeResultExpression(), action ) ) return false;
		    if( !visitExpression( ((IASTConditionalExpression)expression).getPositiveResultExpression(), action ) ) return false;
		} else if( expression instanceof IASTExpressionList ){
			IASTExpression[] list = ((IASTExpressionList)expression).getExpressions();
			for( int i = 0; i < list.length; i++){
			    if( list[i] == null ) break;
			    if( !visitExpression( list[i], action ) ) return false;
			}
		} else if( expression instanceof IASTFieldReference ){
		    if( !visitExpression( ((IASTFieldReference)expression).getFieldOwner(), action ) ) return false;
		    if( !visitName( ((IASTFieldReference)expression).getFieldName(), action ) ) return false;
		} else if( expression instanceof IASTFunctionCallExpression ){
		    if( !visitExpression( ((IASTFunctionCallExpression)expression).getFunctionNameExpression(), action ) ) return false;
		    if( ((IASTFunctionCallExpression)expression).getParameterExpression() != null )
		    	if( !visitExpression( ((IASTFunctionCallExpression)expression).getParameterExpression(), action ) ) return false;
		} else if( expression instanceof IASTIdExpression ){
		    if( !visitName( ((IASTIdExpression)expression).getName(), action ) ) return false;
		} else if( expression instanceof IASTTypeIdExpression ){
		    if( !visitTypeId( ((IASTTypeIdExpression)expression).getTypeId(), action ) ) return false;
		} else if( expression instanceof IASTCastExpression ){
		    if( !visitTypeId( ((IASTCastExpression)expression).getTypeId(), action ) ) return false;
		    if( !visitExpression( ((IASTCastExpression)expression).getOperand(), action ) ) return false;
		} else if( expression instanceof IASTUnaryExpression ){
		    if( !visitExpression( ((IASTUnaryExpression)expression).getOperand(), action ) ) return false;
		} else if( expression instanceof IGNUASTCompoundStatementExpression ){
		    if( !visitStatement( ((IGNUASTCompoundStatementExpression)expression).getCompoundStatement(), action ) ) return false;
		} else if( expression instanceof ICPPASTDeleteExpression ){
			if( !visitExpression( ((ICPPASTDeleteExpression)expression).getOperand(), action ) )  return false;
		} else if( expression instanceof ICPPASTNewExpression ) {
			ICPPASTNewExpression newExp = (ICPPASTNewExpression) expression;
			if( newExp.getNewPlacement() != null )
				if( !visitExpression( newExp.getNewPlacement(), action ) ) return false;
			if( newExp.getTypeId() != null )
				if( !visitTypeId( newExp.getTypeId(), action ) ) return false;
			IASTExpression [] exps = newExp.getNewTypeIdArrayExpressions();
			for( int i = 0; i < exps.length; i++ ){
				if( !visitExpression( exps[i], action ) ) return false;
			}
			if( newExp.getNewInitializer() != null )
				if( !visitExpression( newExp.getNewInitializer(), action ) ) return false;
		} else if( expression instanceof ICPPASTSimpleTypeConstructorExpression ){
			if( !visitExpression( ((ICPPASTSimpleTypeConstructorExpression)expression).getInitialValue(), action ) ) return false;
		} else if( expression instanceof ICPPASTTypenameExpression ){
			if( !visitName( ((ICPPASTTypenameExpression)expression).getName(), action ) ) return false;
			if( !visitExpression( ((ICPPASTTypenameExpression)expression).getInitialValue(), action ) )  return false;
		}
		return true;	
	}
	
	/**
	 * @param typeId
	 * @param action
	 * @return
	 */
	public boolean visitTypeId(IASTTypeId typeId, BaseVisitorAction action) {
		if( action.processTypeIds ){
		    switch( action.processTypeId( typeId ) ){
	            case BaseVisitorAction.PROCESS_ABORT : return false;
	            case BaseVisitorAction.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
		if( !visitDeclSpecifier( typeId.getDeclSpecifier(), action ) ) return false;
		if( !visitDeclarator( typeId.getAbstractDeclarator(), action ) ) return false;
		return true;
	}

	/**
	 * @param initializer
	 * @param action
	 * @return
	 */
	public boolean visitInitializer(IASTInitializer initializer, BaseVisitorAction action) {
		if( action.processInitializers ){
		    switch( action.processInitializer( initializer ) ){
	            case BaseVisitorAction.PROCESS_ABORT : return false;
	            case BaseVisitorAction.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
			
	    if( initializer instanceof IASTInitializerExpression ){
	        if( !visitExpression( ((IASTInitializerExpression) initializer).getExpression(), action ) ) return false;
	    } else if( initializer instanceof IASTInitializerList ){
	        IASTInitializer [] list = ((IASTInitializerList) initializer).getInitializers();
	        for( int i = 0; i < list.length; i++ ){
	            if( !visitInitializer( list[i], action ) ) return false;
	        }
	    } else if( initializer instanceof ICPPASTConstructorInitializer ){
	    	if( !visitExpression( ((ICPPASTConstructorInitializer) initializer).getExpression(), action ) ) return false;
	    }
	    return true;
	}

	/**
	 * @param enumerator
	 * @param action
	 * @return
	 */
	public boolean visitEnumerator(IASTEnumerator enumerator, BaseVisitorAction action) {
		if( action.processEnumerators ){
		    switch( action.processEnumerator( enumerator ) ){
	            case BaseVisitorAction.PROCESS_ABORT : return false;
	            case BaseVisitorAction.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
			
	    if( !visitName( enumerator.getName(), action ) ) return false;
	    if( enumerator.getValue() != null )
	        if( !visitExpression( enumerator.getValue(), action ) ) return false;
	    return true;
	}

	/**
	 * @param specifier
	 * @param action
	 * @return
	 */
	public boolean visitBaseSpecifier(ICPPASTBaseSpecifier specifier, BaseVisitorAction action) {
		if( action instanceof CPPBaseVisitorAction && ((CPPBaseVisitorAction)action).processBaseSpecifiers ){
		    switch( ((CPPBaseVisitorAction)action).processBaseSpecifier( specifier ) ){
	            case BaseVisitorAction.PROCESS_ABORT : return false;
	            case BaseVisitorAction.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
			
	    if( !visitName( specifier.getName(), action ) ) return false;
	    return true;
	}

	public boolean visitParameterDeclaration( IASTParameterDeclaration parameterDeclaration, BaseVisitorAction action ){
	    if( action.processParameterDeclarations ){
	    	switch( action.processParameterDeclaration( parameterDeclaration ) ){
		        case BaseVisitorAction.PROCESS_ABORT : return false;
		        case BaseVisitorAction.PROCESS_SKIP  : return true;
		        default : break;
		    }
	    }
	    
	    if( !visitDeclSpecifier( parameterDeclaration.getDeclSpecifier(), action ) ) return false;
	    if( !visitDeclarator( parameterDeclaration.getDeclarator(), action ) ) return false;
	    return true;
	}
	
	/**
	 * @param parameter
	 * @param action
	 * @return
	 */
	public boolean visitTemplateParameter(ICPPASTTemplateParameter parameter, BaseVisitorAction action) {
		return true;
	}

	/**
	 * Generate a function type for an implicit function.
	 * NOTE: This does not currectly handle parameters with typedef types.
	 * @param returnType
	 * @param parameters
	 * @return
	 */
	
	public static IFunctionType createImplicitFunctionType( IType returnType, IParameter [] parameters ){
	    IType [] pTypes = new IType[ parameters.length ];
	    IType pt = null;
	    
	    for( int i = 0; i < parameters.length; i++ ){
	        try {
                pt = parameters[i].getType();
            } catch ( DOMException e ) {
                pt = e.getProblem();
            }
	        
	        IType [] temp = new IType[] { (IType) pt.clone() };
	        int lastIdx = 0;
	        while( pt instanceof ITypeContainer){
	            try {
                    pt = ((ITypeContainer)pt).getType();
                } catch ( DOMException e1 ) {
                    pt = e1.getProblem();
                }
	            if( pt instanceof ITypeContainer && !(pt instanceof ITypedef) ){
		            IType t = (IType) pt.clone();
		            ((ITypeContainer) temp[ lastIdx ]).setType( t );
		            temp = (IType[]) ArrayUtil.append( IType.class, temp, t );
		            lastIdx++;
	            } else {
	                temp = (IType[]) ArrayUtil.append( IType.class, temp, pt );
	                lastIdx++;
	                break;
	            }
	        }

	        if( lastIdx > 0 && temp[ lastIdx - 1 ] instanceof IQualifierType ){
	            temp[lastIdx - 1] = temp[lastIdx--];
	            if( lastIdx > 0 ){
	                ITypeContainer cont = (ITypeContainer) temp[ lastIdx - 1 ];
	                cont.setType( temp[ lastIdx ] );
	            }
	        }
	        
	        IType lastType = temp[ 0 ];
	        if( lastType instanceof IArrayType ){
	            try {
                    lastType = new CPPPointerType( ((IArrayType) lastType).getType() );
                } catch ( DOMException e1 ) {
                    lastType = e1.getProblem();
                }
	        } else if( lastType instanceof IFunctionType ){
	            lastType = new CPPPointerType( lastType );
	        }
	        
	        pTypes[i] = lastType; 
	    }
	    
	    return new CPPFunctionType( returnType, pTypes );
	}
	private static IType createType( IType returnType, ICPPASTFunctionDeclarator fnDtor ){
	    IASTParameterDeclaration [] params = fnDtor.getParameters();
	    IType [] pTypes = new IType [ params.length ];
	    IType pt = null;
	    
	    for( int i = 0; i < params.length; i++ ){
	        IASTDeclSpecifier pDeclSpec = params[i].getDeclSpecifier();
	        IASTDeclarator pDtor = params[i].getDeclarator();
	        //8.3.5-3 
	        //Any cv-qualifier modifying a parameter type is deleted.
	        //so only create the base type from the declspec and not the qualifiers
	        pt = getBaseType( pDeclSpec );
	        
	        pt = createType( pt, pDtor );
	        
	        //any parameter of type array of T is adjusted to be pointer to T
	        if( pt instanceof IArrayType ){
	            IArrayType at = (IArrayType) pt;
	            try {
                    pt = new CPPPointerType( at.getType() );
                } catch ( DOMException e ) {
                    pt = e.getProblem();
                }
	        }
	        
	        //any parameter to type function returning T is adjusted to be pointer to function
	        if( pt instanceof IFunctionType ){
	            pt = new CPPPointerType( pt );
	        }
	        
	        pTypes[i] = pt;
	    }
	    
	    returnType = getPointerTypes( returnType, fnDtor );
	    
	    IType type = new CPPFunctionType( returnType, pTypes );
	    IASTDeclarator nested = fnDtor.getNestedDeclarator();
	    if( nested != null ) {
	    	return createType( type, nested );
	    }
	    return type;
	}
	
	/**
	 * @param declarator
	 * @return
	 */
	private static IType createType(IType baseType, IASTDeclarator declarator) {
	    if( declarator instanceof ICPPASTFunctionDeclarator )
	        return createType( baseType, (ICPPASTFunctionDeclarator)declarator );
		
		IType type = baseType;
		type = getPointerTypes( type, declarator );
		if( declarator instanceof IASTArrayDeclarator )
		    type = getArrayTypes( type, (IASTArrayDeclarator) declarator );

	    IASTDeclarator nested = declarator.getNestedDeclarator();
	    if( nested != null ) {
	    	return createType( type, nested );
	    }
	    return type;
	}

	private static IType getPointerTypes( IType type, IASTDeclarator declarator ){
	    IASTPointerOperator [] ptrOps = declarator.getPointerOperators();
		for( int i = 0; i < ptrOps.length; i++ ){
			if( ptrOps[i] instanceof ICPPASTPointerToMember )
				type = new CPPPointerToMemberType( type, (ICPPASTPointerToMember) ptrOps[i] );
		    else if( ptrOps[i] instanceof IASTPointer )
		        type = new CPPPointerType( type, (IASTPointer) ptrOps[i] );
		    else if( ptrOps[i] instanceof ICPPASTReferenceOperator )
		        type = new CPPReferenceType( type );
		}
		return type;
	}
	private static IType getArrayTypes( IType type, IASTArrayDeclarator declarator ){
	    IASTArrayModifier [] mods = declarator.getArrayModifiers();
	    for( int i = 0; i < mods.length; i++ ){
	        type = new CPPArrayType( type );
	    }
	    return type;
	}
	
	/**
	 * @param declarator
	 * @return
	 */
	public static IType createType(IASTDeclarator declarator) {
		IASTDeclSpecifier declSpec = null;
		
		IASTNode node = declarator.getParent();
		while( node instanceof IASTDeclarator ){
			declarator = (IASTDeclarator) node;
			node = node.getParent();
		}
		
		if( node instanceof IASTParameterDeclaration )
			declSpec = ((IASTParameterDeclaration) node).getDeclSpecifier();
		else if( node instanceof IASTSimpleDeclaration )
			declSpec = ((IASTSimpleDeclaration)node).getDeclSpecifier();
		else if( node instanceof IASTFunctionDefinition )
			declSpec = ((IASTFunctionDefinition)node).getDeclSpecifier();
	
		IType type = createType( declSpec );
		type = createType( type, declarator );
		return type;
	}
	/**
	 * @param declSpec
	 * @return
	 */
	public static IType createType(IASTDeclSpecifier declSpec ) {
	    IType type = getBaseType( declSpec );
		
		if( type != null && ( declSpec.isConst() || declSpec.isVolatile() ) ){
		    type = new CPPQualifierType( type, declSpec.isConst(), declSpec.isVolatile() );
		}
		return type;
	}

	private static IType getBaseType( IASTDeclSpecifier declSpec ){
	    IType type = null;
	    if( declSpec instanceof ICPPASTCompositeTypeSpecifier ){
			IBinding binding = ((ICPPASTCompositeTypeSpecifier) declSpec).getName().resolveBinding();
			if( binding instanceof IType) 
				type = (IType) binding;
	    } else if( declSpec instanceof ICPPASTNamedTypeSpecifier ){
	    	IBinding binding = ((ICPPASTNamedTypeSpecifier)declSpec).getName().resolveBinding();
	    	if( binding instanceof IType )
	    		type = (IType) binding;
		} else if( declSpec instanceof ICPPASTElaboratedTypeSpecifier ){
			IBinding binding = ((ICPPASTElaboratedTypeSpecifier)declSpec).getName().resolveBinding();
			if( binding instanceof IType )
				type = (IType) binding;
		} else if( declSpec instanceof IASTEnumerationSpecifier ){
			IBinding binding = ((IASTEnumerationSpecifier)declSpec).getName().resolveBinding();
			if( binding instanceof IType )
				type = (IType) binding;
		} else if( declSpec instanceof ICPPASTSimpleDeclSpecifier ){
			ICPPASTSimpleDeclSpecifier spec = (ICPPASTSimpleDeclSpecifier) declSpec;
			int bits = ( spec.isLong()     ? CPPBasicType.IS_LONG  : 0 ) &
					   ( spec.isShort()    ? CPPBasicType.IS_SHORT : 0 ) &
					   ( spec.isSigned()   ? CPPBasicType.IS_SIGNED: 0 ) &
					   ( spec.isUnsigned() ? CPPBasicType.IS_SHORT : 0 );
			if( spec instanceof IGPPASTSimpleDeclSpecifier ){
				IGPPASTSimpleDeclSpecifier gspec = (IGPPASTSimpleDeclSpecifier) spec;
				bits &= ( gspec.isLongLong() ? GPPBasicType.IS_LONGLONG : 0 );
				type = new GPPBasicType( spec.getType(), bits, getExpressionType(gspec.getTypeofExpression()) );
			} else {
			    type = new CPPBasicType( spec.getType(), bits );
			}
		}
		return type;
	}

	/**
	 * @param expression
	 * @return
	 */
	public static IType getExpressionType(IASTExpression expression) {
		if( expression == null )
			return null;
	    if( expression instanceof IASTIdExpression ){
	        IBinding binding = resolveBinding( expression );
			if( binding instanceof IVariable ){
				try {
                    return ((IVariable)binding).getType();
                } catch ( DOMException e ) {
                    return e.getProblem();
                }
			} else if( binding instanceof IProblemBinding ){
				return (IType) binding;
			} else if( binding instanceof IFunction ){
				try {
					return ((IFunction)binding).getType();
				} catch ( DOMException e ){
					return e.getProblem();
				}
			}
	    } else if( expression instanceof IASTCastExpression ){
	        IASTTypeId id = ((IASTCastExpression)expression).getTypeId();
	        IType type = createType( id.getDeclSpecifier() );
	        return createType( type, id.getAbstractDeclarator() );
	    } else if( expression instanceof ICPPASTLiteralExpression ){
	    	switch( ((ICPPASTLiteralExpression) expression).getKind() ){
	    		case ICPPASTLiteralExpression.lk_this : {
	    			IScope scope = getContainingScope( expression );
	    			try {
	    				IASTNode node = null;
	    				while( scope != null ){
	    					if( scope instanceof ICPPBlockScope ){
	    						node = ((ICPPBlockScope)scope).getPhysicalNode();
	    						if( node.getParent() instanceof IASTFunctionDefinition )
	    							break;
	    					}
	    					scope = scope.getParent();
	    				}
	    				if( node != null && node.getParent() instanceof IASTFunctionDefinition ){
	    					IASTFunctionDefinition def = (IASTFunctionDefinition) node.getParent();
							IASTName fName = def.getDeclarator().getName();
							IBinding binding = fName.resolveBinding();
							if( binding != null && binding instanceof ICPPMethod ){
								ICPPASTFunctionDeclarator dtor = (ICPPASTFunctionDeclarator) def.getDeclarator();
								ICPPClassScope cScope = (ICPPClassScope) binding.getScope();
								IType type = cScope.getClassType();
								if( dtor.isConst() || dtor.isVolatile() )
									type = new CPPQualifierType(type, dtor.isConst(), dtor.isVolatile() );
								type = new CPPPointerType( type );
								return type;
							}
	    				}
	    			} catch (DOMException e) {
					}
	    			break;
	    		}
	    		case ICPPASTLiteralExpression.lk_true :
	    		case ICPPASTLiteralExpression.lk_false:
	    			return new CPPBasicType( ICPPBasicType.t_bool, 0 );
	    		case IASTLiteralExpression.lk_char_constant:
	    			return new CPPBasicType( IBasicType.t_char, 0 );
	    		case IASTLiteralExpression.lk_float_constant:
	    			return new CPPBasicType( IBasicType.t_float, 0 );
	    		case IASTLiteralExpression.lk_integer_constant:
	    			return new CPPBasicType( IBasicType.t_int, 0 );
	    		case IASTLiteralExpression.lk_string_literal:
	    			IType type = new CPPBasicType( IBasicType.t_char, 0 );
	    			type = new CPPQualifierType( type, true, false );
	    			return new CPPPointerType( type );
	    	}
	    	
	    } else if( expression instanceof IASTFunctionCallExpression ){
	        IBinding binding = resolveBinding( expression );
	        if( binding instanceof IFunction ){
	            IFunctionType fType;
                try {
                    fType = ((IFunction)binding).getType();
                    if( fType != null )
    	                return fType.getReturnType();
                } catch ( DOMException e ) {
                    return e.getProblem();
                }
	        } else if( binding instanceof IVariable ){
	        	try {
		        	IType t = ((IVariable)binding).getType();
		        	if( t instanceof IPointerType && ((IPointerType)t).getType() instanceof IFunctionType ){
		        		IFunctionType ftype = (IFunctionType) ((IPointerType)t).getType();
		        		if( ftype != null )
		        			return ftype.getReturnType();
		        	}
	        	} catch( DOMException e ){
	        		return e.getProblem();
	        	} 
	        }
	    } else if( expression instanceof IASTBinaryExpression ){
	        IASTBinaryExpression binary = (IASTBinaryExpression) expression;
	        IType type = getExpressionType( ((IASTBinaryExpression) expression).getOperand2() );
	        if( binary.getOperator() == ICPPASTBinaryExpression.op_pmarrow ||
	            binary.getOperator() == ICPPASTBinaryExpression.op_pmdot )
	        {
	            if( type instanceof ICPPPointerToMemberType ){
	                try {
                        return ((ICPPPointerToMemberType)type).getType();
                    } catch ( DOMException e ) {
                        return e.getProblem();
                    }
	            }
	            return new ProblemBinding( IProblemBinding.SEMANTIC_INVALID_TYPE, new char[0] );
	        }
	        return type;
	    }
	    else if( expression instanceof IASTUnaryExpression )
	    {
			IType type = getExpressionType(((IASTUnaryExpression)expression).getOperand() );
			int op = ((IASTUnaryExpression)expression).getOperator(); 
			if( op == IASTUnaryExpression.op_star && (type instanceof IPointerType || type instanceof IArrayType) ){
			    try {
					return ((ITypeContainer)type).getType();
				} catch (DOMException e) {
					return e.getProblem();
				}
			} else if( op == IASTUnaryExpression.op_amper ){
			    return new CPPPointerType( type );
			}
			return type;
	    } else if( expression instanceof ICPPASTFieldReference ){
			IASTName name = ((ICPPASTFieldReference)expression).getFieldName();
			IBinding binding = name.resolveBinding();
			try {
			    if( binding instanceof IVariable )
                    return ((IVariable)binding).getType();
                else if( binding instanceof IFunction )
				    return ((IFunction)binding).getType();
		    } catch ( DOMException e ) {
		        return e.getProblem();
            }
		}
	    return null;
	}
	
	public static IASTProblem[] getProblems(IASTTranslationUnit tu) {
		CollectProblemsAction action = new CollectProblemsAction();
		tu.getVisitor().visitTranslationUnit(action);
		
		return action.getProblems();
	}

	/**
	 * @param unit
	 * @param binding
	 * @return
	 */
	public static IASTName[] getReferences(IASTTranslationUnit tu, IBinding binding) {
		CollectReferencesAction action = new CollectReferencesAction( binding );
		tu.getVisitor().visitTranslationUnit( action );
		return action.getReferences();
	}
	
	public static IASTName[] getDeclarations( IASTTranslationUnit tu, IBinding binding ){
	    CollectDeclarationsAction action = new CollectDeclarationsAction( binding );
	    tu.getVisitor().visitTranslationUnit( action );
	    return action.getDeclarations();
	}
}
