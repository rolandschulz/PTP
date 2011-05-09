/**********************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.analysis.PAST;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.ExpansionOverlapsBoundaryException;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * Preprocessor node, pseudo for IASTNode, used for analysis
 * 
 * @author pazel
 * 
 */
public abstract class PASTNode implements IASTNode
{

	protected ASTNode astNode_ = null;
	protected boolean compiled_ = false;

	// Location information
	protected int startingLine_ = -1;
	protected int startLocation_ = -1;
	protected int endLocation_ = -1;
	protected String filename_ = "";

	/**
	 * PASTNode - Constructor
	 * 
	 * @param astNode
	 *            : LocationMap.ScannerASTNode
	 */
	public PASTNode(ASTNode astNode)
	{
		astNode_ = astNode;

		getLocationInfo();
	}

	/**
	 * PASTNode - Copy Constructor
	 * 
	 * @param n
	 */
	public PASTNode(PASTNode n)
	{
		astNode_ = n.astNode_;
		compiled_ = n.compiled_;
		startingLine_ = n.startingLine_;
		startLocation_ = n.startLocation_;
		filename_ = n.filename_;
	}

	/**
	 * getLocationInfo - Line number, offset, etc
	 * 
	 */
	protected void getLocationInfo()
	{
		IASTNodeLocation[] locations = astNode_.getNodeLocations();
		filename_ = astNode_.getContainingFilename();
		if (locations.length == 1) {
			IASTFileLocation astFileLocation = null;
			if (locations[0] instanceof IASTFileLocation) {
				astFileLocation = (IASTFileLocation) locations[0];
				startingLine_ = astFileLocation.getStartingLineNumber();
				startLocation_ = astFileLocation.getNodeOffset();
				endLocation_ = astFileLocation.getNodeOffset() + astFileLocation.getNodeLength();
			}
		}
	}

	/**
	 * getFilename - accessor
	 * 
	 * @return String
	 */
	public String getFilename() {
		return filename_;
	}

	/**
	 * getStartingLine - accessor
	 * 
	 * @return int
	 */
	public int getStartingLine() {
		return startingLine_;
	}

	/**
	 * getStartLocation - accessor
	 * 
	 * @return int
	 */
	public int getStartLocation() {
		return startLocation_;
	}

	/**
	 * getEndLocation - accessor
	 * 
	 * @return int
	 */
	public int getEndLocation() {
		return endLocation_;
	}

	/**
	 * getType - descriptive text for type of node
	 * 
	 * @return String
	 */
	public abstract String getType();

	/**
	 * setCompiled - setter
	 * 
	 * @param compiled
	 *            : boolean
	 */
	public void setCompiled(boolean compiled)
	{
		compiled_ = compiled;
	}

	/**
	 * isCompiled - accessor
	 * 
	 * @return boolean
	 */
	public boolean isCompiled()
	{
		return compiled_;
	}

	/**
	 * delegated methods for interface IASTNode
	 */
	public boolean accept(ASTVisitor visitor) {
		return astNode_.accept(visitor);
	}

	/** @since cdt40 */
	public boolean contains(IASTNode node) {
		return astNode_.contains(node);
	}

	public String getContainingFilename() {
		return astNode_.getContainingFilename();
	}

	public IASTFileLocation getFileLocation() {
		return astNode_.getFileLocation();
	}

	public IASTNodeLocation[] getNodeLocations() {
		return astNode_.getNodeLocations();
	}

	public IASTNode getParent() {
		return astNode_.getParent();
	}

	public ASTNodeProperty getPropertyInParent() {
		return astNode_.getPropertyInParent();
	}

	public String getRawSignature() {
		return astNode_.getRawSignature();
	}

	public IASTTranslationUnit getTranslationUnit() {
		return astNode_.getTranslationUnit();
	}

	public void setParent(IASTNode node) {
		astNode_.setParent(node);
	}

	public void setPropertyInParent(ASTNodeProperty property) {
		astNode_.setPropertyInParent(property);
	}

	/**
	 * CDT6.0 implement IASTNode.copy()
	 */
	public IASTNode copy() {
		return astNode_.copy();
	}

	/**
	 * CDT6.0 implement IASTNode.getChildren()
	 */
	public IASTNode[] getChildren() {
		return astNode_.getChildren();
	}

	/**
	 * CDT6.0 implement IASTNode.getSyntax()
	 */
	public IToken getSyntax() throws ExpansionOverlapsBoundaryException {
		return astNode_.getSyntax();

	}

	/**
	 * CDT6.0 implement IASTNode.getLeadingSyntax()
	 */
	public IToken getLeadingSyntax() throws ExpansionOverlapsBoundaryException {
		return astNode_.getLeadingSyntax();
	}

	/**
	 * CDT6.0 implement IASTNode.getTrailingSyntax()
	 */
	public IToken getTrailingSyntax() throws ExpansionOverlapsBoundaryException {
		return astNode_.getTrailingSyntax();
	}

	/**
	 * CDT6.0 implement IASTNode.isActive()
	 */
	public boolean isActive() {
		return astNode_.isActive();

	}

	/**
	 * CDT6.0 implement IASTNode.isFrozen()
	 */
	public boolean isFrozen() {
		return astNode_.isFrozen();

	}

}
