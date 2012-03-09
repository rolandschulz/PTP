/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.rdt.core.miners;


import java.io.Serializable;
import java.util.List;
import java.util.Stack;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElseStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorEndifStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfdefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfndefStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;

/**
 * Remote implementation of a {@link ICFoldingStructureProvider}.
 * <p>
 * Derived from DefaultCFoldingStructureProvider.
 * </p>
 */
public class RemoteFoldingRegionsHandler  {
	
	/**
	 * Representation of a preprocessor code branch.
	 */
	public static class Branch extends ModifiableRegion implements Serializable {
		private static final long serialVersionUID = 1L;
		private final boolean fTaken;
		public final String fCondition;
		public boolean fInclusive;

		Branch(int offset, boolean taken, String key) {
			this(offset, 0, taken, key);
		}

		Branch(int offset, int length, boolean taken, String key) {
			super(offset, length);
			fTaken= taken;
			fCondition= key;
		}

		public void setEndOffset(int endOffset) {
			setLength(endOffset - getOffset());
		}

		public boolean taken() {
			return fTaken;
		}

		public void setInclusive(boolean inclusive) {
			fInclusive= inclusive;
		}
	}
	
	/**
	 * Implementation of <code>IRegion</code> that can be reused
	 * by setting the offset and the length.
	 */
	private static class ModifiableRegion implements Serializable {
		private static final long serialVersionUID = 1L;
		private int length;
		private int offset;
		
		ModifiableRegion() {
		}
		
		ModifiableRegion(int offset, int length) {
			setLength(length);
			setOffset(offset);
		}
		
		public void setLength(int length) {
			this.length = length;
		}
		
		public void setOffset(int offset) {
			this.offset = offset;
		}
		
		public int getOffset() {
			return offset;
		}
		
		public int getLength() {
			return length;
		}
	}
	
	public static class StatementRegion extends ModifiableRegion {
		public final String function;
		public int level;
		public boolean inclusive;
		public StatementRegion(String function, int level) {
			this.function= function; 
			this.level= level;
		}
	}

	
	/**
	 * A visitor to collect compound statement positions.
	 *
	 * @since 5.0
	 */
	public final class StatementVisitor extends ASTVisitor {
		{
			shouldVisitStatements = true;
			shouldVisitDeclarations = true;
		}
		private final Stack<StatementRegion> fStatements;
		int fLevel= 0;
		String fFunction= ""; //$NON-NLS-1$

		StatementVisitor(Stack<StatementRegion> statements) {
			fStatements = statements;
			includeInactiveNodes = true;
		}

		@Override
		public int visit(IASTStatement statement) {
			++fLevel;
			// if it's not part of the displayed - file, we don't need it
			if (!statement.isPartOfTranslationUnitFile())
				return PROCESS_SKIP;// we neither need its descendants
			try {
				StatementRegion mr;
				IASTFileLocation fl;
				if (statement instanceof IASTIfStatement) {
					IASTIfStatement ifstmt = (IASTIfStatement) statement;
					fl = ifstmt.getFileLocation();
					if (fl==null) return PROCESS_CONTINUE;
					int ifOffset= fl.getNodeOffset();
					IASTStatement thenStmt;
					mr = createRegion();
					thenStmt = ifstmt.getThenClause();
					if (thenStmt==null) return PROCESS_CONTINUE;
					fl = thenStmt.getFileLocation();
					mr.setLength(fl.getNodeOffset() + fl.getNodeLength() - ifOffset);
					mr.setOffset(ifOffset);
					mr.inclusive = !(thenStmt instanceof IASTCompoundStatement);
					IASTStatement elseStmt;
					elseStmt = ifstmt.getElseClause();
					if (elseStmt == null) {
						mr.inclusive = true;
						fStatements.push(mr);
						return PROCESS_CONTINUE;
					}
					IASTFileLocation elseStmtLocation = elseStmt.getFileLocation();
					mr.inclusive = mr.inclusive || fl.getEndingLineNumber() < elseStmtLocation.getStartingLineNumber();
					if (elseStmt instanceof IASTIfStatement) {
						fStatements.push(mr);
						return PROCESS_CONTINUE;
					}
					fStatements.push(mr);
					mr = createRegion();
					mr.setLength(elseStmtLocation.getNodeLength());
					mr.setOffset(elseStmtLocation.getNodeOffset());
					mr.inclusive = true;
					fStatements.push(mr);
					return PROCESS_CONTINUE;
				}
				mr = createRegion();
				mr.inclusive = true;
				if (statement instanceof IASTDoStatement)
					mr.inclusive = false;
				if (statement instanceof IASTSwitchStatement) {
					IASTStatement switchstmt = ((IASTSwitchStatement)statement).getBody();
					if (switchstmt instanceof IASTCompoundStatement) {
						IASTStatement[] stmts = ((IASTCompoundStatement)switchstmt).getStatements();
						boolean pushedMR = false;
						for (IASTStatement tmpstmt : stmts) {
							StatementRegion tmpmr;
							if (!(tmpstmt instanceof IASTCaseStatement || tmpstmt instanceof IASTDefaultStatement)) {
								if (!pushedMR) return PROCESS_SKIP;
								IASTFileLocation tmpfl = tmpstmt.getFileLocation();
								tmpmr = fStatements.peek();
								tmpmr.setLength(tmpfl.getNodeLength()+tmpfl.getNodeOffset()-tmpmr.getOffset());
								if (tmpstmt instanceof IASTBreakStatement) pushedMR = false;
								continue;
							}
							IASTFileLocation tmpfl;
							tmpmr = createRegion();
							tmpmr.level= fLevel+1;
							tmpmr.inclusive = true;
							if (tmpstmt instanceof IASTCaseStatement) {
								IASTCaseStatement casestmt = (IASTCaseStatement) tmpstmt;
								tmpfl = casestmt.getExpression().getFileLocation();
								tmpmr.setOffset(tmpfl.getNodeOffset());
								tmpmr.setLength(tmpfl.getNodeLength());
							} else if (tmpstmt instanceof IASTDefaultStatement) {
								IASTDefaultStatement defstmt = (IASTDefaultStatement) tmpstmt;
								tmpfl = defstmt.getFileLocation();
								tmpmr.setOffset(tmpfl.getNodeOffset()+tmpfl.getNodeLength());
								tmpmr.setLength(0);
							}
							fStatements.push(tmpmr);
							pushedMR = true;
						}
					}
				}
				if (statement instanceof IASTForStatement
						|| statement instanceof IASTWhileStatement
						|| statement instanceof IASTDoStatement
						|| statement instanceof IASTSwitchStatement) {
					fl = statement.getFileLocation();
					mr.setLength(fl.getNodeLength());
					mr.setOffset(fl.getNodeOffset());
					fStatements.push(mr);
				}
				return PROCESS_CONTINUE;
			} catch (Exception e) {
				return PROCESS_ABORT;
			}
		}

		@Override
		public int leave(IASTStatement statement) {
			--fLevel;
			return PROCESS_CONTINUE;
		}

		@Override
		public int visit(IASTDeclaration declaration) {
			if (!declaration.isPartOfTranslationUnitFile())
				return PROCESS_SKIP;// we neither need its descendants
			if (declaration instanceof IASTFunctionDefinition) {
				final IASTFunctionDeclarator declarator = ((IASTFunctionDefinition)declaration).getDeclarator();
				if (declarator != null) {
					fFunction= new String(ASTQueries.findInnermostDeclarator(declarator).getName().toCharArray());
					fLevel= 0;
				}
			}
			return PROCESS_CONTINUE;
		}

		@Override
		public int leave(IASTDeclaration declaration) {
			if (declaration instanceof IASTFunctionDefinition) {
				fFunction= ""; //$NON-NLS-1$
			}
			return PROCESS_CONTINUE;
		}

		private StatementRegion createRegion() {
			return new StatementRegion(fFunction, fLevel);
		}
	}
	
	public StatementVisitor createStatementVisitor(Stack<StatementRegion> iral) {
		return new StatementVisitor(iral);
	}
	
	/**
	 * Computes folding structure for preprocessor branches for the given AST.
	 * 
	 * @param ast
	 */
	void computePreprocessorFoldingStructure(IASTTranslationUnit ast, int docSize, List<Branch> branches) {
		Stack<Branch> branchStack = new Stack<Branch>();

		IASTPreprocessorStatement[] preprocStmts = ast.getAllPreprocessorStatements();

		for (IASTPreprocessorStatement statement : preprocStmts) {
			if (!statement.isPartOfTranslationUnitFile()) {
				// preprocessor directive is from a different file
				continue;
			}
			IASTNodeLocation stmtLocation= statement.getFileLocation();
			if (stmtLocation == null) {
				continue;
			}
			if (statement instanceof IASTPreprocessorIfStatement) {
				IASTPreprocessorIfStatement ifStmt = (IASTPreprocessorIfStatement)statement;
				branchStack.push(new Branch(stmtLocation.getNodeOffset(), ifStmt.taken(), "#if " + new String(ifStmt.getCondition()))); //$NON-NLS-1$
			} else if (statement instanceof IASTPreprocessorIfdefStatement) {
				IASTPreprocessorIfdefStatement ifdefStmt = (IASTPreprocessorIfdefStatement)statement;
				branchStack.push(new Branch(stmtLocation.getNodeOffset(), ifdefStmt.taken(), "#ifdef " + new String(ifdefStmt.getCondition()))); //$NON-NLS-1$
			} else if (statement instanceof IASTPreprocessorIfndefStatement) {
				IASTPreprocessorIfndefStatement ifndefStmt = (IASTPreprocessorIfndefStatement)statement;
				branchStack.push(new Branch(stmtLocation.getNodeOffset(), ifndefStmt.taken(), "#ifndef " + new String(ifndefStmt.getCondition()))); //$NON-NLS-1$
			} else if (statement instanceof IASTPreprocessorElseStatement) {
				if (branchStack.isEmpty()) {
					// #else without #if
					continue;
				}
				Branch branch= branchStack.pop();
				IASTPreprocessorElseStatement elseStmt = (IASTPreprocessorElseStatement)statement;
				branchStack.push(new Branch(stmtLocation.getNodeOffset(), elseStmt.taken(), branch.fCondition));
				branch.setEndOffset(stmtLocation.getNodeOffset());
				branches.add(branch);
			} else if (statement instanceof IASTPreprocessorElifStatement) {
				if (branchStack.isEmpty()) {
					// #elif without #if
					continue;
				}
				Branch branch= branchStack.pop();
				IASTPreprocessorElifStatement elifStmt = (IASTPreprocessorElifStatement) statement;
				branchStack.push(new Branch(stmtLocation.getNodeOffset(), elifStmt.taken(), branch.fCondition));
				branch.setEndOffset(stmtLocation.getNodeOffset());
				branches.add(branch);
			} else if (statement instanceof IASTPreprocessorEndifStatement) {
				if (branchStack.isEmpty()) {
					// #endif without #if
					continue;
				}
				Branch branch= branchStack.pop();
				branch.setEndOffset(stmtLocation.getNodeOffset() + stmtLocation.getNodeLength());
				branch.setInclusive(true);
				branches.add(branch);
			}
		}

		if (!branchStack.isEmpty()) {
			// unterminated #if
			Branch branch= branchStack.pop();
			branch.setEndOffset(docSize);
			branch.setInclusive(true);
			branches.add(branch);
		}
	}
	
}


