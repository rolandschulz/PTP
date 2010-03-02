/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Photran modifications
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 
/**
 * Edited by Matthew Michelotti
 * 
 * Overview of changes:
 * -may have changed import statements
 * -Commented out sections of code which may significantly
 *  depend on the CDT version. May have deprecated a number of methods
 *  with commented code.
 * -added unimplemented methods to conform with new versions of
 *  CDT interfaces. Deprecated these methods.
 */
package org.eclipse.photran.internal.core.preprocessor.c;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTCompletionContext;
//import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
//import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
//import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTNode;
//import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
//import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
//import org.eclipse.cdt.internal.core.dom.Linkage;
//import org.eclipse.cdt.core.parser.IToken;

/**
 * Models IASTNames as needed for the preprocessor statements and macro expansions.
 * @since 5.0
 */
class ASTPreprocessorName extends ASTPreprocessorNode implements IASTName {
	private final char[] fName;
	private final IBinding fBinding;
	public ASTPreprocessorName(IASTNode parent, ASTNodeProperty property, int startNumber, int endNumber, char[] name, IBinding binding) {
		super(parent, property, startNumber, endNumber);
		fName= name;
		fBinding= binding;
	}

	public IBinding resolveBinding() {
		return fBinding;
	}
	public IBinding getBinding() {
		return fBinding;
	}
    @Deprecated
    //functionality removed for stability with CDT version
	public ILinkage getLinkage() {
		//final IASTTranslationUnit tu= getTranslationUnit();
		//return tu == null ? Linkage.NO_LINKAGE : tu.getLinkage();
		return null;
	}
	public IASTCompletionContext getCompletionContext() {
		return null;
	}
	public boolean isDeclaration() {
		return false;
	}
	public boolean isDefinition() {
		return false;
	}
	public boolean isReference() {
		return false;
	}
	public char[] toCharArray() {
		return fName;
	}    	
	@Override
	public String toString() {
		return new String(fName);
	}
	public void setBinding(IBinding binding) {assert false;}

	public int getRoleOfName(boolean allowResolution) {
		return IASTNameOwner.r_unclear;
	}
	public IASTName getLastName() {
		return this;
	}

    @Deprecated
    //method added to conform with CDT interface
	public IASTName copy() {
		// TODO Auto-generated method stub
		return null;
	}

    @Deprecated
    //method added to conform with CDT interface
	public char[] getLookupKey() {
		// TODO Auto-generated method stub
		return null;
	}

    @Deprecated
    //method added to conform with CDT interface
	public IBinding getPreBinding() {
		// TODO Auto-generated method stub
		return null;
	}

    @Deprecated
    //method added to conform with CDT interface
	public IBinding resolvePreBinding() {
		// TODO Auto-generated method stub
		return null;
	}

    @Deprecated
    //method added to conform with CDT interface
	public char[] getSimpleID() {
		// TODO Auto-generated method stub
		return null;
	}

}

/*class ASTPreprocessorDefinition extends ASTPreprocessorName {
	public ASTPreprocessorDefinition(IASTNode parent, ASTNodeProperty property, int startNumber,
			int endNumber, char[] name, IBinding binding) {
		super(parent, property, startNumber, endNumber, name, binding);
	}

	@Override
	public boolean isDefinition() {
		return true;
	}
	
	@Override
	public int getRoleOfName(boolean allowResolution) {
		return IASTNameOwner.r_definition;
	}
}*/


/*class ASTBuiltinName extends ASTPreprocessorDefinition {
	private final IASTFileLocation fFileLocation;

	public ASTBuiltinName(IASTNode parent, ASTNodeProperty property, IASTFileLocation floc, char[] name, IBinding binding) {
		super(parent, property, -1, -1, name, binding);
		fFileLocation= floc;
	}

	@Override
	public boolean contains(IASTNode node) {
		return node==this;
	}

	@Override
	public String getContainingFilename() {
		if (fFileLocation == null) {
			return ""; //$NON-NLS-1$
		}
		return fFileLocation.getFileName();
	}

	@Override
	public IASTFileLocation getFileLocation() {
		return fFileLocation;
	}

	@Override
	public IASTNodeLocation[] getNodeLocations() {
		if (fFileLocation == null) {
			return new IASTNodeLocation[0];
		}
		return new IASTNodeLocation[]{fFileLocation};
	}

	@Override
	public String getRawSignature() {
		if (fFileLocation == null) {
			return ""; //$NON-NLS-1$
		}
		return toString();
	}
}*/

class ASTMacroReferenceName extends ASTPreprocessorName {
	//private ImageLocationInfo fImageLocationInfo;
	
	public ASTMacroReferenceName(IASTNode parent, ASTNodeProperty property, int offset, int endOffset, IMacroBinding macro, ImageLocationInfo imgLocationInfo) {
		super(parent, property, offset, endOffset, macro.getNameCharArray(), macro);
		//fImageLocationInfo= imgLocationInfo;
	}

	@Override
	public int getRoleOfName(boolean allowResolution) {
		return IASTNameOwner.r_unclear;
	}

	@Override
	public boolean isReference() {
		return true;
	}

	/*@Override
	public IASTImageLocation getImageLocation() {
		if (fImageLocationInfo != null) {
			IASTTranslationUnit tu= getTranslationUnit();
			if (tu != null) {
				LocationMap lr= (LocationMap) tu.getAdapter(LocationMap.class);
				if (lr != null) {
					return fImageLocationInfo.createLocation(lr, fImageLocationInfo);
				}
			}
			return null;
		}
		return super.getImageLocation();
	}*/
}
