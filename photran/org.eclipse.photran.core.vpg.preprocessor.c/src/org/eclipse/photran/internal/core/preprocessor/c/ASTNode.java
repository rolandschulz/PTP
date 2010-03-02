/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Photran modifications
 *    IBM Rational Software - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
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

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
//import org.eclipse.cdt.internal.core.parser.scanner.ILocationResolver;
import org.eclipse.cdt.core.parser.IToken;

/**
 * @author jcamelon
 */
public abstract class ASTNode implements IASTNode {

    //private static final IASTNodeLocation[] EMPTY_LOCATION_ARRAY = new IASTNodeLocation[0];

    private IASTNode parent;
    private ASTNodeProperty property;

    private int length;
    private int offset;

    public IASTNode getParent() {
    	return parent;
    }
    
    public void setParent(IASTNode node) {
    	this.parent = node;
    }
    
    public ASTNodeProperty getPropertyInParent() {
    	return property;
    }
    
    public void setPropertyInParent(ASTNodeProperty property) {
    	this.property = property;
    }
    
    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

    public void setOffset(int offset) {
        this.offset = offset;
        //this.locations = null;
    }

    public void setLength(int length) {
        this.length = length;
        //this.locations = null;
    }

    public void setOffsetAndLength(int offset, int length) {
        this.offset = offset;
        this.length = length;
        //this.locations = null;
    }

    public void setOffsetAndLength(ASTNode node) {
        setOffsetAndLength(node.getOffset(), node.getLength());
    }

    //private IASTNodeLocation[] locations = null;
    //private IASTFileLocation fileLocation = null;
    
    @Deprecated
    //functionality removed for stability with CDT version
    public IASTNodeLocation[] getNodeLocations() {
        /*if (locations != null)
            return locations;
        if (length == 0) {
        	locations= EMPTY_LOCATION_ARRAY;
        }
        else {
        	final IASTTranslationUnit tu= getTranslationUnit();
        	if (tu != null) {
        		ILocationResolver l= (ILocationResolver) tu.getAdapter(ILocationResolver.class);
        		if (l != null) {
        			locations= l.getLocations(offset, length);
        		}
        	}
        }
        return locations;*/
    	return null;
    }

    @Deprecated
    //functionality removed for stability with CDT version
    public IASTImageLocation getImageLocation() {
    	/*final IASTTranslationUnit tu= getTranslationUnit();
    	if (tu != null) {
    		ILocationResolver l= (ILocationResolver) tu.getAdapter(ILocationResolver.class);
    		if (l != null) {
    			return l.getImageLocation(offset, length);
    		}
    	}
        return null;*/
    	return null;
    }

    @Deprecated
    //functionality removed for stability with CDT version
    public String getRawSignature() {
    	/*final IASTFileLocation floc= getFileLocation();
        final IASTTranslationUnit ast = getTranslationUnit();
        if (floc != null && ast != null) {
        	ILocationResolver lr= (ILocationResolver) ast.getAdapter(ILocationResolver.class);
        	if (lr != null) {
        		return new String(lr.getUnpreprocessedSignature(getFileLocation()));
        	}
        }
        return ""; //$NON-NLS-1$*/
    	return null;
    }

    public String getContainingFilename() {
    	if (offset <= 0 && (length == 0 || offset < 0)) {
    		final IASTNode parent = getParent();
    		if (parent == null) {
    			if (this instanceof IASTTranslationUnit) {
    				return ((IASTTranslationUnit) this).getFilePath();
    			}
    			return ""; //$NON-NLS-1$
    		}
    		return parent.getContainingFilename();
    	}
        return getTranslationUnit().getContainingFilename(offset);
    }

    @Deprecated
    //functionality removed for stability with CDT version
    public IASTFileLocation getFileLocation() {
        /*if( fileLocation != null )
            return fileLocation;
        if (offset <= 0 && (length == 0 || offset < 0)) {
        	return null;
        }
        IASTTranslationUnit ast = getTranslationUnit();
        if (ast != null) {
        	ILocationResolver lr= (ILocationResolver) ast.getAdapter(ILocationResolver.class);
        	if (lr != null) {
        		fileLocation= lr.getMappedFileLocation(offset, length);
        	}
        	else {
        		// support for old location map
        		fileLocation= ast.flattenLocationsToFile(getNodeLocations());
        	}
        }
        return fileLocation;*/
        return null;
    }

    @Deprecated
    //functionality removed for stability with CDT version
    public boolean isPartOfTranslationUnitFile() {
        /*IASTTranslationUnit ast = getTranslationUnit();
        if (ast != null) {
        	ILocationResolver lr= (ILocationResolver) ast.getAdapter(ILocationResolver.class);
        	if (lr != null) {
        		return lr.isPartOfTranslationUnitFile(offset);
        	}
        }
        return false;*/
    	return false;
    }
    
    public IASTTranslationUnit getTranslationUnit() {
       	return parent != null ? parent.getTranslationUnit() : null;
    }

    public boolean accept(ASTVisitor visitor) {
    	return true;
    }
    
    public boolean contains(IASTNode node) {
    	if (node instanceof ASTNode) {
    		ASTNode astNode= (ASTNode) node;
    		return offset <= astNode.offset && 
    			astNode.offset+astNode.length <= offset+length;
    	}
    	return false;
    }

    @Deprecated
    //method added to conform with CDT interface
	public IASTNode copy() {
		// TODO Auto-generated method stub
		return null;
	}

    @Deprecated
    //method added to conform with CDT interface
	public IASTNode[] getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

    @Deprecated
    //method added to conform with CDT interface
	public IToken getLeadingSyntax() throws ExpansionOverlapsBoundaryException,
			UnsupportedOperationException {
		// TODO Auto-generated method stub
		return null;
	}

    @Deprecated
    //method added to conform with CDT interface
	public IToken getSyntax() throws ExpansionOverlapsBoundaryException {
		// TODO Auto-generated method stub
		return null;
	}

    @Deprecated
    //method added to conform with CDT interface
	public IToken getTrailingSyntax()
			throws ExpansionOverlapsBoundaryException,
			UnsupportedOperationException {
		// TODO Auto-generated method stub
		return null;
	}

    @Deprecated
    //method added to conform with CDT interface
	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}

    @Deprecated
    //method added to conform with CDT interface
	public boolean isFrozen() {
		// TODO Auto-generated method stub
		return false;
	}
}
