/**********************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.mpi.analysis.analysis;

import java.util.LinkedList;
import java.util.ListIterator;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTBreakStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTCompoundStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTDeclarationStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTDoStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTExpressionStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTForStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTIfStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTInitializerExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTName;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTSwitchStatement;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTUnaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTWhileStatement;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraph;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraphNode;

public class GotoEliminator {

	private ICallGraph cg_;
	private LinkedList gotoList_;
	private LinkedList labelList_;

	private ICallGraphNode currentNode_;

	private int switch_count = 0;

	public GotoEliminator(ICallGraph cg) {
		cg_ = cg;
		gotoList_ = null;
		labelList_ = null;
		currentNode_ = null;
	}

	public void run() {
		for (ICallGraphNode node = cg_.botEntry(); node != null; node = node.botNext()) {
			currentNode_ = node;
			normalizeCompoundStmt(node.getFuncDef().getBody());
			GotoLabelCollector glc = new GotoLabelCollector(node);
			gotoList_ = glc.getGotoList();
			labelList_ = glc.getLabelList();
			if (gotoList_.size() == 0)
				continue;

			gotoPreprocessing();
			labelPreprocessing();
			while (gotoList_.size() > 0) {
				IASTGotoStatement gotoS = (IASTGotoStatement) gotoList_.remove();
				IASTLabelStatement label = findLabel(gotoS);
				if (label == null) {
					System.out.println("Empty goto label!"); //$NON-NLS-1$
					continue;
				}

				while (!isDirectedRelated(gotoS, label) && !isSibling(gotoS, label)) {
					moveOutward(gotoS);
				}
				if (isDirectedRelated(gotoS, label)) {
					if (Level(gotoS) > Level(label)) {
						while (Level(gotoS) > Level(label))
							moveOutward(gotoS);
					}
					else {
						while (!Offset(gotoS, label))
							lifting(gotoS);
						while (Level(gotoS) < Level(label))
							moveInward(gotoS);
					}
				}

				eliminatingGoto(gotoS);
			}

			while (labelList_.size() > 0) {
				IASTLabelStatement label = (IASTLabelStatement) labelList_.remove();
				eliminatingStatement(label);
			}
		}
	}

	private void labelPreprocessing() {
		for (ListIterator li = labelList_.listIterator(); li.hasNext();) {
			IASTLabelStatement label = (IASTLabelStatement) li.next();
			IASTName labelname = label.getName();
			String labelvar = "goto_" + labelname.toString(); //$NON-NLS-1$
			createDeclaration(labelvar);
			setLabelVariable(labelvar, label);
		}
	}

	private void gotoPreprocessing() {
		for (ListIterator li = gotoList_.listIterator(); li.hasNext();) {
			IASTGotoStatement gotoS = (IASTGotoStatement) li.next();
			if (!(gotoS.getParent() instanceof IASTIfStatement)) {
				IASTLiteralExpression condE = new CASTLiteralExpression();
				condE.setValue("true"); //$NON-NLS-1$

				IASTIfStatement ifS = new CASTIfStatement();
				ifS.setConditionExpression(condE);
				condE.setParent(ifS);
				ifS.setThenClause(gotoS);
				ifS.setElseClause(null);
				gotoS.setParent(ifS);

				replaceStatement(gotoS, ifS);
			}
		}
	}

	private void createDeclaration(String var) {
		IASTSimpleDeclaration decl = new CASTSimpleDeclaration();

		IASTSimpleDeclSpecifier declSpec = new CASTSimpleDeclSpecifier();
		declSpec.setConst(false);
		declSpec.setVolatile(false);
		declSpec.setInline(false);
		declSpec.setStorageClass(IASTDeclSpecifier.sc_unspecified);
		declSpec.setType(IASTSimpleDeclSpecifier.t_int);
		declSpec.setLong(false);
		declSpec.setUnsigned(false);
		declSpec.setSigned(true);
		declSpec.setShort(false);

		decl.setDeclSpecifier(declSpec);
		declSpec.setParent(decl);
		declSpec.setPropertyInParent(IASTSimpleDeclaration.DECL_SPECIFIER);
		IASTDeclarator[] declarators = new IASTDeclarator[1];
		IASTName name = new CASTName(var.toCharArray());
		declarators[0].setName(name);
		IASTLiteralExpression init = new CASTLiteralExpression();
		init.setValue("0"); //$NON-NLS-1$
		IASTInitializerExpression initE = new CASTInitializerExpression();
		initE.setExpression(init);
		declarators[0].setInitializer(initE);

		decl.addDeclarator(declarators[0]);
		declarators[0].setParent(decl);
		declarators[0].setPropertyInParent(IASTSimpleDeclaration.DECLARATOR);

		IASTDeclarationStatement declStmt = new CASTDeclarationStatement();
		declStmt.setDeclaration(decl);

		IASTCompoundStatement body = (IASTCompoundStatement) currentNode_.getFuncDef().getBody();
		IASTStatement[] stmts = body.getStatements();
		body.addStatement(stmts[stmts.length - 1]);
		for (int i = stmts.length - 1; i > 0; i--) {
			stmts[i] = stmts[i - 1];
		}
		stmts[0] = declStmt;
		stmts[0].setParent(stmts[1].getParent());
		stmts[0].setPropertyInParent(stmts[1].getPropertyInParent());
	}

	private void setLabelVariable(String var, IASTLabelStatement label) {
		IASTName name = new CASTName(var.toCharArray());
		IASTIdExpression id = new CASTIdExpression();
		id.setName(name);
		name.setParent(id);

		IASTLiteralExpression value = new CASTLiteralExpression();
		value.setValue("0"); //$NON-NLS-1$

		IASTBinaryExpression biE = new CASTBinaryExpression();
		biE.setOperand1(id);
		biE.setOperand2(value);
		id.setParent(biE);
		value.setParent(biE);
		biE.setOperator(IASTBinaryExpression.op_assign);

		IASTExpressionStatement exprStmt = new CASTExpressionStatement();
		exprStmt.setExpression(biE);
		biE.setParent(exprStmt);

		IASTCompoundStatement compStmt = new CASTCompoundStatement();
		if (label.getNestedStatement() != null) {
			IASTStatement nestedStmt = label.getNestedStatement();
			compStmt.setParent(nestedStmt.getParent());
			compStmt.setPropertyInParent(nestedStmt.getPropertyInParent());
			compStmt.addStatement(exprStmt);
			compStmt.addStatement(nestedStmt);
		}
		else {
			compStmt.addStatement(exprStmt);
			compStmt.setParent(label);
		}
		label.setNestedStatement(compStmt);
	}

	private IASTLabelStatement findLabel(IASTGotoStatement gotoS) {
		IASTName labelname = gotoS.getName();
		for (ListIterator li = labelList_.listIterator(); li.hasNext();) {
			IASTLabelStatement s = (IASTLabelStatement) li.next();
			if (s.getName().toString().equals(labelname.toString()))
				return s;
		}
		return null;
	}

	private boolean isSibling(IASTNode s1, IASTNode s2) {
		IASTNode p1 = s1.getParent();
		IASTNode p2 = s2.getParent();
		if (p1 == p2 && p1 instanceof IASTCompoundStatement)
			return true;
		else
			return false;
	}

	private boolean isDirectedRelated(IASTGotoStatement gotoS,
			IASTLabelStatement labelS) {
		IASTNode parent;
		parent = gotoS.getParent();
		while (!(parent instanceof IASTFunctionDefinition)) {
			if (isSibling(labelS, parent))
				return true;
			else
				parent = parent.getParent();
		}
		parent = labelS.getParent();
		while (!(parent instanceof IASTFunctionDefinition)) {
			if (isSibling(gotoS, parent))
				return true;
			else
				parent = parent.getParent();
		}
		return false;
	}

	/*
	 * Delete redundantly nested compoundStatement e.g. {{{s}}} --> {s} e.g.
	 * {{s1}{s2}} --> {s1 s2}
	 */
	private void normalizeCompoundStmt(IASTStatement stmt) {
		if (stmt instanceof IASTCompoundStatement) {
			IASTCompoundStatement newCompS = new CASTCompoundStatement();
			IASTStatement stmts[] = ((IASTCompoundStatement) stmt).getStatements();
			for (int i = 0; i < stmts.length; i++) {
				normalizeCompoundStmt(stmts[i]);
				if (stmts[i] instanceof IASTCompoundStatement) {
					IASTCompoundStatement subCompS = (IASTCompoundStatement) stmts[i];
					IASTStatement subStmts[] = subCompS.getStatements();
					for (int j = 0; j < subStmts.length; j++) {
						newCompS.addStatement(subStmts[j]);
						subStmts[j].setParent(newCompS); // TODO: check here
					}
				} else {
					newCompS.addStatement(stmts[i]);
					stmts[i].setParent(newCompS); // TODO: check here
				}
			}
			replaceStatement(stmt, newCompS);
		}
		else if (stmt instanceof IASTDoStatement) {
			normalizeCompoundStmt(((IASTDoStatement) stmt).getBody());
		}
		else if (stmt instanceof IASTForStatement) {
			normalizeCompoundStmt(((IASTForStatement) stmt).getBody());
		}
		else if (stmt instanceof IASTIfStatement) {
			normalizeCompoundStmt(((IASTIfStatement) stmt).getThenClause());
			normalizeCompoundStmt(((IASTIfStatement) stmt).getElseClause());
		}
		else if (stmt instanceof IASTSwitchStatement) {
			normalizeCompoundStmt(((IASTSwitchStatement) stmt).getBody());
		}
		else if (stmt instanceof IASTWhileStatement) {
			normalizeCompoundStmt(((IASTWhileStatement) stmt).getBody());
		}
	}

	/* TODO: check the order of replace and parent setting */
	private void moveOutward(IASTGotoStatement gotoS) {
		String labelname = "goto_" + gotoS.getName().toString(); //$NON-NLS-1$
		IASTNode parent = gotoS.getParent().getParent();
		IASTNode body = gotoS.getParent();
		IASTIfStatement gotoIfStmt = (IASTIfStatement) body;

		while (!(parent instanceof IASTFunctionDefinition)) {

			if (parent instanceof IASTDoStatement ||
					parent instanceof IASTForStatement ||
					parent instanceof IASTWhileStatement ||
					parent instanceof IASTSwitchStatement) {
				IASTCompoundStatement newbody = new CASTCompoundStatement();
				if (body instanceof IASTCompoundStatement) {
					IASTStatement[] stmts = ((IASTCompoundStatement) body).getStatements();
					int index;
					for (index = 0; index < stmts.length; index++) {
						if (stmts[index] == gotoIfStmt)
							break;
					}

					for (int j = 0; j < index; j++) {
						newbody.addStatement(stmts[j]);
					}
					IASTStatement assignS = makeGotoIfCondStoreStmt(gotoIfStmt.getConditionExpression(), labelname);
					newbody.addStatement(assignS);

					IASTName condname = new CASTName(labelname.toCharArray());
					IASTIdExpression condE = new CASTIdExpression();
					condE.setName(condname);
					condname.setParent(condE);

					IASTIfStatement ifS = new CASTIfStatement();
					ifS.setConditionExpression(condE);
					IASTBreakStatement breakS = new CASTBreakStatement();
					ifS.setThenClause(breakS);
					ifS.setElseClause(null);
					condE.setParent(ifS);
					breakS.setParent(ifS);

					newbody.addStatement(ifS);

					for (int j = index + 1; j < stmts.length; j++) {
						newbody.addStatement(stmts[j]);
					}
				}
				else if (body == gotoIfStmt) {
					IASTStatement assignS = makeGotoIfCondStoreStmt(gotoIfStmt.getConditionExpression(), labelname);
					newbody.addStatement(assignS);

					IASTName condname = new CASTName(labelname.toCharArray());
					IASTIdExpression condE = new CASTIdExpression();
					condE.setName(condname);
					condname.setParent(condE);

					IASTIfStatement ifS = new CASTIfStatement();
					ifS.setConditionExpression(condE);
					IASTBreakStatement breakS = new CASTBreakStatement();
					ifS.setThenClause(breakS);
					ifS.setElseClause(null);
					condE.setParent(ifS);
					breakS.setParent(ifS);

					newbody.addStatement(ifS);
				}

				if (parent instanceof IASTDoStatement) {
					((CASTDoStatement) parent).replace(body, newbody);
				} else if (parent instanceof IASTForStatement) {
					((CASTForStatement) parent).replace(body, newbody);
				} else if (parent instanceof IASTWhileStatement) {
					((CASTWhileStatement) parent).replace(body, newbody);
				} else if (parent instanceof IASTSwitchStatement) {
					((CASTSwitchStatement) parent).replace(body, newbody);
				}

				IASTStatement newIfStmt = makeNewGotoIfStmt(labelname, gotoS);
				appendStatement(newIfStmt, (IASTStatement) parent);

				break;
			}
			else if (parent instanceof IASTIfStatement) {
				if (body instanceof IASTCompoundStatement) {
					IASTStatement[] stmts = ((IASTCompoundStatement) body).getStatements();
					int index;
					for (index = 0; index < stmts.length; index++) {
						if (stmts[index] == gotoIfStmt)
							break;
					}

					IASTCompoundStatement newbody = new CASTCompoundStatement();
					for (int j = 0; j < index; j++) {
						newbody.addStatement(stmts[j]);
					}
					IASTStatement assignS = makeGotoIfCondStoreStmt(gotoIfStmt.getConditionExpression(), labelname);
					newbody.addStatement(assignS);

					if (index < stmts.length - 1) { // some statements after the
													// goto
						IASTCompoundStatement ifbody = new CASTCompoundStatement();
						for (int j = index + 1; j < stmts.length; j++) {
							ifbody.addStatement(stmts[j]);
						}
						IASTName condname = new CASTName(labelname.toCharArray());
						IASTIdExpression condid = new CASTIdExpression();
						condid.setName(condname);
						condname.setParent(condid);

						IASTUnaryExpression condE = new CASTUnaryExpression();
						condE.setOperand(condid);
						condE.setOperator(IASTUnaryExpression.op_not);
						condid.setParent(condE);

						IASTIfStatement ifStmt = new CASTIfStatement();
						ifStmt.setConditionExpression(condE);
						ifStmt.setThenClause(ifbody);
						ifStmt.setElseClause(null);
						condE.setParent(ifStmt);
						ifbody.setParent(ifStmt);

						newbody.addStatement(ifStmt);
					}

					((CASTIfStatement) parent).replace(body, newbody);
				}
				else if (body == gotoIfStmt) {
					IASTStatement assignS = makeGotoIfCondStoreStmt(gotoIfStmt.getConditionExpression(), labelname);
					((CASTIfStatement) parent).replace(gotoIfStmt, assignS);
				}

				IASTStatement newifStmt = makeNewGotoIfStmt(labelname, gotoS);
				appendStatement(newifStmt, (IASTStatement) parent);

				break;
			}
			else {
				body = parent;
				parent = body.getParent();
			}
		}
	}

	private void moveInward(IASTGotoStatement gotoS) {
		IASTIfStatement gotoIfStmt = (IASTIfStatement) gotoS.getParent();
		String labelname = "goto_" + gotoS.getName().toString(); //$NON-NLS-1$
		IASTLabelStatement label = findLabel(gotoS);
		IASTStatement enclosingStmt = null;
		IASTStatement clause = null;
		IASTNode parent = label.getParent();
		IASTNode temp = label;
		while (!(parent instanceof IASTFunctionDefinition)) {
			if (parent instanceof IASTDoStatement ||
					parent instanceof IASTForStatement ||
					parent instanceof IASTWhileStatement ||
					parent instanceof IASTSwitchStatement ||
					parent instanceof IASTIfStatement) {
				enclosingStmt = (IASTStatement) parent;
				if (parent instanceof IASTIfStatement)
					clause = (IASTStatement) temp;
			}
			if (parent == gotoIfStmt.getParent())
				break;
			else {
				temp = parent;
				parent = temp.getParent();
			}
		}

		if (enclosingStmt instanceof IASTDoStatement ||
				enclosingStmt instanceof IASTForStatement ||
				enclosingStmt instanceof IASTWhileStatement) {
			/* parent should be a CompoundStatement */
			int gotoindex = -1;
			int loopindex = -1;
			IASTStatement[] stmts = ((IASTCompoundStatement) parent).getStatements();
			for (int i = 0; i < stmts.length; i++) {
				if (stmts[i] == gotoIfStmt)
					gotoindex = i;
				else if (stmts[i] == enclosingStmt)
					loopindex = i;
			}

			IASTCompoundStatement newParentStmt = new CASTCompoundStatement();

			for (int i = 0; i < gotoindex; i++) {
				newParentStmt.addStatement(stmts[i]);
				stmts[i].setParent(newParentStmt);
			}
			IASTStatement assignS = makeGotoIfCondStoreStmt(gotoIfStmt.getConditionExpression(), labelname);
			newParentStmt.addStatement(assignS);
			assignS.setParent(newParentStmt);

			if (loopindex - gotoindex > 1) {
				IASTName condname = new CASTName(labelname.toCharArray());
				IASTIdExpression condid = new CASTIdExpression();
				condid.setName(condname);
				condname.setParent(condid);
				IASTUnaryExpression condE = new CASTUnaryExpression();
				condE.setOperand(condid);
				condE.setOperator(IASTUnaryExpression.op_not);
				condid.setParent(condE);

				IASTCompoundStatement ifbody = new CASTCompoundStatement();
				for (int i = gotoindex + 1; i < loopindex; i++) {
					ifbody.addStatement(stmts[i]);
					stmts[i].setParent(ifbody);
				}

				IASTIfStatement newIfStmt = new CASTIfStatement();
				newIfStmt.setConditionExpression(condE);
				newIfStmt.setThenClause(ifbody);
				newIfStmt.setElseClause(null);
				condE.setParent(newIfStmt);
				ifbody.setParent(newIfStmt);

				newParentStmt.addStatement(newIfStmt);
				newIfStmt.setParent(newParentStmt);
			}

			IASTName loopcondname = new CASTName(labelname.toCharArray());
			IASTIdExpression loopcondid = new CASTIdExpression();
			loopcondid.setName(loopcondname);
			loopcondname.setParent(loopcondid);

			IASTStatement loopbody = null;
			if (enclosingStmt instanceof IASTDoStatement) {
				IASTDoStatement doloop = (IASTDoStatement) enclosingStmt;
				IASTBinaryExpression newCondE = new CASTBinaryExpression();
				IASTExpression oldCondE = doloop.getCondition();
				newCondE.setOperand1(oldCondE);
				newCondE.setOperand2(loopcondid);
				newCondE.setOperator(IASTBinaryExpression.op_logicalOr);
				oldCondE.setParent(newCondE);
				loopcondid.setParent(newCondE);
				((CASTDoStatement) doloop).replace(oldCondE, newCondE);
				loopbody = doloop.getBody();
			} else if (enclosingStmt instanceof IASTForStatement) {
				IASTForStatement forloop = (IASTForStatement) enclosingStmt;
				IASTBinaryExpression newCondE = new CASTBinaryExpression();
				IASTExpression oldCondE = forloop.getConditionExpression();
				newCondE.setOperand1(oldCondE);
				newCondE.setOperand2(loopcondid);
				newCondE.setOperator(IASTBinaryExpression.op_logicalOr);
				oldCondE.setParent(newCondE);
				loopcondid.setParent(newCondE);
				((CASTForStatement) forloop).replace(oldCondE, newCondE);
				loopbody = forloop.getBody();
			} else if (enclosingStmt instanceof IASTWhileStatement) {
				IASTWhileStatement whileloop = (IASTWhileStatement) enclosingStmt;
				IASTBinaryExpression newCondE = new CASTBinaryExpression();
				IASTExpression oldCondE = whileloop.getCondition();
				newCondE.setOperand1(oldCondE);
				newCondE.setOperand2(loopcondid);
				newCondE.setOperator(IASTBinaryExpression.op_logicalOr);
				oldCondE.setParent(newCondE);
				loopcondid.setParent(newCondE);
				((CASTWhileStatement) whileloop).replace(oldCondE, newCondE);
				loopbody = whileloop.getBody();
			}

			IASTCompoundStatement newloopbody = new CASTCompoundStatement();
			IASTStatement newGotoS = makeNewGotoIfStmt(labelname, gotoS);
			newloopbody.addStatement(newGotoS);
			newGotoS.setParent(newloopbody);
			if (loopbody instanceof IASTCompoundStatement) {
				IASTStatement[] bodyStmts = ((IASTCompoundStatement) loopbody).getStatements();
				for (int i = 0; i < bodyStmts.length; i++) {
					newloopbody.addStatement(bodyStmts[i]);
					bodyStmts[i].setParent(newloopbody);
				}
			}
			else if (loopbody == label) {
				newloopbody.addStatement(label);
				label.setParent(newloopbody);
			}

			if (enclosingStmt instanceof IASTDoStatement) {
				((CASTDoStatement) enclosingStmt).replace(loopbody, newloopbody);
			} else if (enclosingStmt instanceof IASTForStatement) {
				((CASTForStatement) enclosingStmt).replace(loopbody, newloopbody);
			} else if (enclosingStmt instanceof IASTWhileStatement) {
				((CASTWhileStatement) enclosingStmt).replace(loopbody, newloopbody);
			}

			newParentStmt.addStatement(enclosingStmt);
			enclosingStmt.setParent(newParentStmt);

			for (int i = loopindex + 1; i < stmts.length; i++) {
				newParentStmt.addStatement(stmts[i]);
				stmts[i].setParent(newParentStmt);
			}
			replaceStatement((IASTStatement) parent, newParentStmt);

		}
		else if (enclosingStmt instanceof IASTIfStatement) {
			/* parent must be a compoundStatement */
			IASTStatement[] stmts = ((IASTCompoundStatement) parent).getStatements();
			int gotoindex = -1;
			int ifindex = -1;
			for (int i = 0; i < stmts.length; i++) {
				if (stmts[i] == gotoIfStmt)
					gotoindex = i;
				else if (stmts[i] == enclosingStmt)
					ifindex = i;
			}

			IASTCompoundStatement newParentStmt = new CASTCompoundStatement();

			for (int i = 0; i < gotoindex; i++) {
				newParentStmt.addStatement(stmts[i]);
				stmts[i].setParent(newParentStmt);
			}
			IASTStatement assignS = makeGotoIfCondStoreStmt(gotoIfStmt.getConditionExpression(), labelname);
			newParentStmt.addStatement(assignS);
			assignS.setParent(newParentStmt);

			if (ifindex - gotoindex > 1) {
				IASTName condname = new CASTName(labelname.toCharArray());
				IASTIdExpression condid = new CASTIdExpression();
				condid.setName(condname);
				condname.setParent(condid);
				IASTUnaryExpression condE = new CASTUnaryExpression();
				condE.setOperand(condid);
				condE.setOperator(IASTUnaryExpression.op_not);
				condid.setParent(condE);

				IASTCompoundStatement ifbody = new CASTCompoundStatement();
				for (int i = gotoindex + 1; i < ifindex; i++) {
					ifbody.addStatement(stmts[i]);
					stmts[i].setParent(ifbody);
				}

				IASTIfStatement newIfStmt = new CASTIfStatement();
				newIfStmt.setConditionExpression(condE);
				newIfStmt.setThenClause(ifbody);
				newIfStmt.setElseClause(null);
				condE.setParent(newIfStmt);
				ifbody.setParent(newIfStmt);

				newParentStmt.addStatement(newIfStmt);
				newIfStmt.setParent(newParentStmt);
			}

			IASTExpression oldIfCond = ((IASTIfStatement) enclosingStmt).getConditionExpression();
			IASTName ifcondname = new CASTName(labelname.toCharArray());
			IASTIdExpression ifcondid = new CASTIdExpression();
			ifcondid.setName(ifcondname);
			ifcondname.setParent(ifcondid);

			IASTUnaryExpression ifcondPart1 = new CASTUnaryExpression();
			ifcondPart1.setOperand(ifcondid);
			ifcondPart1.setOperator(IASTUnaryExpression.op_not);
			ifcondid.setParent(ifcondPart1);

			IASTBinaryExpression newIfCond = new CASTBinaryExpression();
			newIfCond.setOperator(IASTBinaryExpression.op_logicalAnd);
			newIfCond.setOperand1(ifcondPart1);
			newIfCond.setOperand2(oldIfCond);
			ifcondPart1.setParent(newIfCond);
			oldIfCond.setParent(newIfCond);

			((CASTIfStatement) enclosingStmt).replace(oldIfCond, newIfCond);

			IASTCompoundStatement newClause = new CASTCompoundStatement();
			IASTStatement newGotoS = makeNewGotoIfStmt(labelname, gotoS);
			newClause.addStatement(newGotoS);
			newGotoS.setParent(newClause);
			if (clause instanceof IASTCompoundStatement) {
				IASTStatement[] clauseStmts = ((IASTCompoundStatement) clause).getStatements();
				for (int i = 0; i < clauseStmts.length; i++) {
					newClause.addStatement(clauseStmts[i]);
					clauseStmts[i].setParent(newClause);
				}
			}
			else if (clause == label) {
				newClause.addStatement(label);
				label.setParent(newClause);
			}

			((CASTIfStatement) enclosingStmt).replace(clause, newClause);

			newParentStmt.addStatement(enclosingStmt);
			enclosingStmt.setParent(newParentStmt);

			for (int i = ifindex + 1; i < stmts.length; i++) {
				newParentStmt.addStatement(stmts[i]);
				stmts[i].setParent(newParentStmt);
			}
			replaceStatement((IASTStatement) parent, newParentStmt);
		}
		else if (enclosingStmt instanceof IASTSwitchStatement) {
			/* parent must be a compoundStatement */
			IASTStatement[] stmts = ((IASTCompoundStatement) parent).getStatements();
			int gotoindex = -1;
			int switchindex = -1;
			IASTCaseStatement caseStmt = null;
			boolean casefound = false;

			for (int i = 0; i < stmts.length; i++) {
				if (stmts[i] == gotoIfStmt)
					gotoindex = i;
				else if (stmts[i] == enclosingStmt) {
					switchindex = i;
					casefound = true;
				}
				else if (stmts[i] instanceof IASTCaseStatement && !casefound) {
					caseStmt = (IASTCaseStatement) stmts[i];
				}
			}

			IASTCompoundStatement newParentStmt = new CASTCompoundStatement();

			for (int i = 0; i < gotoindex; i++) {
				newParentStmt.addStatement(stmts[i]);
				stmts[i].setParent(newParentStmt);
			}
			IASTStatement assignS = makeGotoIfCondStoreStmt(gotoIfStmt.getConditionExpression(), labelname);
			newParentStmt.addStatement(assignS);
			assignS.setParent(newParentStmt);

			/* making new if statement */
			IASTName condname = new CASTName(labelname.toCharArray());
			IASTIdExpression condid = new CASTIdExpression();
			condid.setName(condname);
			condname.setParent(condid);
			IASTUnaryExpression condE = new CASTUnaryExpression();
			condE.setOperand(condid);
			condE.setOperator(IASTUnaryExpression.op_not);
			condid.setParent(condE);

			IASTCompoundStatement ifThenBody = new CASTCompoundStatement();
			for (int i = gotoindex + 1; i < switchindex; i++) {
				ifThenBody.addStatement(stmts[i]);
				stmts[i].setParent(ifThenBody);
			}

			String switchvar = "t_switch_" + switch_count; //$NON-NLS-1$
			switch_count++;
			createDeclaration(switchvar); /* TODO: fix type information */
			IASTName switchCondAssignName = new CASTName(switchvar.toCharArray());
			IASTIdExpression switchCondAssignID = new CASTIdExpression();
			switchCondAssignID.setName(switchCondAssignName);
			switchCondAssignName.setParent(switchCondAssignID);
			IASTExpression oldSwitchCondE = ((IASTSwitchStatement) enclosingStmt).getControllerExpression();

			IASTBinaryExpression switchCondAssignE = new CASTBinaryExpression();
			switchCondAssignE.setOperand1(switchCondAssignID);
			switchCondAssignE.setOperand2(oldSwitchCondE);
			switchCondAssignE.setOperator(IASTBinaryExpression.op_assign);
			switchCondAssignID.setParent(switchCondAssignE);
			oldSwitchCondE.setParent(switchCondAssignE);

			IASTExpressionStatement switchCondAssignStmt = new CASTExpressionStatement();
			switchCondAssignStmt.setExpression(switchCondAssignE);
			switchCondAssignE.setParent(switchCondAssignStmt);

			ifThenBody.addStatement(switchCondAssignStmt);
			switchCondAssignStmt.setParent(ifThenBody);

			IASTName elseSwitchCondName = new CASTName(switchvar.toCharArray());
			IASTIdExpression elseSwitchCondID = new CASTIdExpression();
			elseSwitchCondID.setName(elseSwitchCondName);
			elseSwitchCondName.setParent(elseSwitchCondID);

			IASTBinaryExpression elseSwitchCondE = new CASTBinaryExpression();
			elseSwitchCondE.setOperator(IASTBinaryExpression.op_assign);
			elseSwitchCondE.setOperand1(elseSwitchCondID);
			elseSwitchCondE.setOperand2(caseStmt.getExpression());
			elseSwitchCondID.setParent(elseSwitchCondE);
			caseStmt.getExpression().setParent(elseSwitchCondE);

			IASTExpressionStatement elseSwitchCondStmt = new CASTExpressionStatement();
			elseSwitchCondStmt.setExpression(elseSwitchCondE);
			elseSwitchCondE.setParent(elseSwitchCondStmt);

			IASTIfStatement newIfStmt = new CASTIfStatement();
			newIfStmt.setConditionExpression(condE);
			newIfStmt.setThenClause(ifThenBody);
			newIfStmt.setElseClause(elseSwitchCondStmt);
			condE.setParent(newIfStmt);
			ifThenBody.setParent(newIfStmt);

			newParentStmt.addStatement(newIfStmt);
			newIfStmt.setParent(newParentStmt);
			/* end making new if statement */

			/* make new switch condition */
			IASTName switchCondName = new CASTName(switchvar.toCharArray());
			IASTIdExpression switchCondID = new CASTIdExpression();
			switchCondID.setName(switchCondName);
			switchCondName.setParent(switchCondID);

			((CASTSwitchStatement) enclosingStmt).replace(oldSwitchCondE, switchCondID);
			/* end make new switch condition */

			/* make new switch body */
			IASTCompoundStatement newSwitchBody = new CASTCompoundStatement();
			IASTStatement oldSwitchBody = ((IASTSwitchStatement) enclosingStmt).getBody();
			IASTStatement[] oldSwitchBodyStmts = ((IASTCompoundStatement) oldSwitchBody).getStatements();

			for (int i = 0; i < oldSwitchBodyStmts.length; i++) {
				if (oldSwitchBodyStmts[i] != caseStmt) {
					newSwitchBody.addStatement(oldSwitchBodyStmts[i]);
					oldSwitchBodyStmts[i].setParent(newSwitchBody);
				} else {
					newSwitchBody.addStatement(caseStmt);
					caseStmt.setParent(newSwitchBody);
					IASTStatement newGotoS = makeNewGotoIfStmt(labelname, gotoS);
					newSwitchBody.addStatement(newGotoS);
					newGotoS.setParent(newSwitchBody);
				}
			}

			((CASTSwitchStatement) enclosingStmt).replace(oldSwitchBody, newSwitchBody);

			newParentStmt.addStatement(enclosingStmt);
			enclosingStmt.setParent(newParentStmt);

			for (int i = switchindex + 1; i < stmts.length; i++) {
				newParentStmt.addStatement(stmts[i]);
				stmts[i].setParent(newParentStmt);
			}
			replaceStatement((IASTStatement) parent, newParentStmt);
		}
	}

	private void lifting(IASTGotoStatement gotoS) {
		IASTIfStatement gotoIfStmt = (IASTIfStatement) gotoS.getParent();
		String labelname = "goto_" + gotoS.getName().toString(); //$NON-NLS-1$
		IASTLabelStatement label = findLabel(gotoS);
		IASTStatement enclosingStmt = null;

		IASTNode parent = label.getParent();
		while (!(parent instanceof IASTFunctionDefinition)) {
			if (parent instanceof IASTDoStatement ||
					parent instanceof IASTForStatement ||
					parent instanceof IASTWhileStatement ||
					parent instanceof IASTSwitchStatement ||
					parent instanceof IASTIfStatement) {
				enclosingStmt = (IASTStatement) parent;
			}
			if (parent == gotoIfStmt.getParent())
				break;
			else {
				parent = parent.getParent();
			}
		}

		/* parent should be a CompoundStatement */
		IASTStatement[] stmts = ((IASTCompoundStatement) parent).getStatements();
		int labelindex = -1;
		int gotoindex = -1;
		for (int i = 0; i < stmts.length; i++) {
			if (stmts[i] == enclosingStmt)
				labelindex = i;
			else if (stmts[i] == gotoIfStmt)
				gotoindex = i;
		}

		IASTCompoundStatement newParentStmt = new CASTCompoundStatement();
		for (int i = 0; i < labelindex; i++) {
			newParentStmt.addStatement(stmts[i]);
			stmts[i].setParent(newParentStmt);
		}

		/* make the do loop */
		IASTCompoundStatement loopbody = new CASTCompoundStatement();
		IASTStatement ifStmt = makeNewGotoIfStmt(labelname, gotoS);
		loopbody.addStatement(ifStmt);
		ifStmt.setParent(loopbody);

		for (int i = labelindex; i < gotoindex; i++) {
			loopbody.addStatement(stmts[i]);
			stmts[i].setParent(loopbody);
		}

		IASTStatement gotoIfCondStmt = makeGotoIfCondStoreStmt(gotoIfStmt.getConditionExpression(), labelname);
		loopbody.addStatement(gotoIfCondStmt);
		gotoIfCondStmt.setParent(loopbody);

		IASTName condname = new CASTName(labelname.toCharArray());
		IASTIdExpression condID = new CASTIdExpression();
		condID.setName(condname);
		condname.setParent(condID);

		IASTDoStatement doStmt = new CASTDoStatement();
		doStmt.setBody(loopbody);
		doStmt.setCondition(condID);
		loopbody.setParent(doStmt);
		condID.setParent(doStmt);
		/* end make the do loop */

		newParentStmt.addStatement(doStmt);
		doStmt.setParent(newParentStmt);

		for (int i = gotoindex + 1; i < stmts.length; i++) {
			newParentStmt.addStatement(stmts[i]);
			stmts[i].setParent(newParentStmt);
		}

		replaceStatement((IASTStatement) parent, newParentStmt);
	}

	private void eliminatingGoto(IASTGotoStatement gotoS) {
		/*
		 * gotoS and label should have the same parent, and the parent should be
		 * a compoundStatement
		 */
		IASTLabelStatement label = findLabel(gotoS);
		IASTIfStatement gotoIfStmt = (IASTIfStatement) gotoS.getParent();
		IASTCompoundStatement parent = (IASTCompoundStatement) gotoS.getParent();
		IASTStatement[] stmts = parent.getStatements();
		int gotoindex = -1;
		int labelindex = -1;
		for (int i = 0; i < stmts.length; i++) {
			if (stmts[i] == gotoIfStmt)
				gotoindex = i;
			else if (stmts[i] == label)
				labelindex = i;
		}

		IASTCompoundStatement newCompS = new CASTCompoundStatement();
		if (gotoindex < labelindex) {
			for (int i = 0; i < gotoindex; i++) {
				newCompS.addStatement(stmts[i]);
				stmts[i].setParent(newCompS);
			}
			if (labelindex - gotoindex > 1) {
				IASTCompoundStatement newIfBody = new CASTCompoundStatement();
				for (int i = gotoindex + 1; i < labelindex; i++) {
					newIfBody.addStatement(stmts[i]);
					stmts[i].setParent(newIfBody);
				}
				IASTUnaryExpression newCondE = new CASTUnaryExpression();
				newCondE.setOperator(IASTUnaryExpression.op_not);
				newCondE.setOperand(gotoIfStmt.getConditionExpression());
				gotoIfStmt.getConditionExpression().setParent(newCondE);

				IASTIfStatement newIfS = new CASTIfStatement();
				newIfS.setConditionExpression(newCondE);
				newIfS.setThenClause(newIfBody);
				newIfS.setElseClause(null);
				newIfBody.setParent(newIfS);
				newCondE.setParent(newIfS);

				newCompS.addStatement(newIfS);
				newIfS.setParent(newCompS);
			}

			for (int i = labelindex; i < stmts.length; i++) {
				newCompS.addStatement(stmts[i]);
				stmts[i].setParent(newCompS);
			}
		}
		else { // gotoS occurs later than label
			for (int i = 0; i < labelindex; i++) {
				newCompS.addStatement(stmts[i]);
				stmts[i].setParent(newCompS);
			}

			IASTCompoundStatement loopbody = new CASTCompoundStatement();
			loopbody.addStatement(label);
			label.setParent(loopbody);
			for (int i = labelindex + 1; i < gotoindex; i++) {
				loopbody.addStatement(stmts[i]);
				stmts[i].setParent(loopbody);
			}

			IASTDoStatement newLoopS = new CASTDoStatement();
			newLoopS.setBody(loopbody);
			newLoopS.setCondition(gotoIfStmt.getConditionExpression());
			loopbody.setParent(newLoopS);
			gotoIfStmt.getConditionExpression().setParent(newLoopS);

			newCompS.addStatement(newLoopS);
			newLoopS.setParent(newCompS);

			for (int i = gotoindex + 1; i < stmts.length; i++) {
				newCompS.addStatement(stmts[i]);
				stmts[i].setParent(newCompS);
			}
		}
		replaceStatement(parent, newCompS);
	}

	private void eliminatingStatement(IASTStatement stmt) {
		if (stmt instanceof IASTCompoundStatement) {
			IASTStatement[] stmts = ((IASTCompoundStatement) stmt).getStatements();
			int validStmt = 0;
			for (int i = 0; i < stmts.length; i++) {
				if (stmts[i] != null)
					validStmt++;
			}
			if (validStmt == 0) { // no valid statement
				replaceStatement(stmt, null);
				eliminatingStatement((IASTStatement) stmt.getParent());
			}
			else if (validStmt < stmts.length) { // some statements are not
													// valid
				IASTCompoundStatement newCompS = new CASTCompoundStatement();
				for (int i = 0; i < stmts.length; i++) {
					if (stmts[i] != null) {
						newCompS.addStatement(stmts[i]);
						stmts[i].setParent(newCompS);
					}
				}
				replaceStatement(stmt, newCompS);
			}

		} else if (stmt instanceof IASTDoStatement) {
			IASTDoStatement doS = (IASTDoStatement) stmt;
			if (doS.getBody() == null && doS.getCondition() == null) {
				replaceStatement(stmt, null);
				eliminatingStatement((IASTStatement) stmt.getParent());
			}
		} else if (stmt instanceof IASTForStatement) {

		} else if (stmt instanceof IASTIfStatement) {
			IASTIfStatement ifS = (IASTIfStatement) stmt;
			if (ifS.getThenClause() == null && ifS.getElseClause() == null) {
				replaceStatement(stmt, null);
				eliminatingStatement((IASTStatement) stmt.getParent());
			} else if (ifS.getThenClause() == null && ifS.getElseClause() != null) {
				IASTIfStatement newIfS = new CASTIfStatement();
				IASTUnaryExpression newCondE = new CASTUnaryExpression();
				newCondE.setOperator(IASTUnaryExpression.op_not);
				newCondE.setOperand(ifS.getConditionExpression());
				ifS.getConditionExpression().setParent(newCondE);

				newIfS.setConditionExpression(newCondE);
				newIfS.setThenClause(ifS.getElseClause());
				newIfS.setElseClause(null);
				ifS.getElseClause().setParent(newIfS);
				newCondE.setParent(newIfS);

				replaceStatement(ifS, newIfS);
			}

		} else if (stmt instanceof IASTLabelStatement) {
			IASTLabelStatement labelS = (IASTLabelStatement) stmt;
			IASTStatement nestedS = labelS.getNestedStatement();
			if (nestedS == null) {
				replaceStatement(stmt, null);
			} else {
				replaceStatement(stmt, nestedS);
			}
			eliminatingStatement((IASTStatement) stmt.getParent());
		} else if (stmt instanceof IASTSwitchStatement) {

		} else if (stmt instanceof IASTWhileStatement) {

		} else {

		}
	}

	/* return statement "labelname = cond;" */
	private IASTStatement makeGotoIfCondStoreStmt(IASTExpression cond, String labelname) {
		IASTName assignName = new CASTName(labelname.toCharArray());
		IASTIdExpression assignID = new CASTIdExpression();
		assignID.setName(assignName);
		assignName.setParent(assignID);

		IASTBinaryExpression assignE = new CASTBinaryExpression();
		assignE.setOperand1(assignID);
		assignE.setOperand2(cond);
		assignE.setOperator(IASTBinaryExpression.op_assign);
		assignID.setParent(assignE);
		cond.setParent(assignE);

		IASTExpressionStatement assignS = new CASTExpressionStatement();
		assignS.setExpression(assignE);
		assignE.setParent(assignS);

		return assignS;
	}

	private IASTStatement makeNewGotoIfStmt(String labelname,
			IASTGotoStatement gotoS) {
		IASTName condname = new CASTName(labelname.toCharArray());
		IASTIdExpression condE = new CASTIdExpression();
		condE.setName(condname);
		condname.setParent(condE);

		IASTIfStatement newifStmt = new CASTIfStatement();
		newifStmt.setConditionExpression(condE);
		newifStmt.setThenClause(gotoS);
		newifStmt.setElseClause(null);
		condE.setParent(newifStmt);
		gotoS.setParent(newifStmt);

		return newifStmt;
	}

	private void replaceStatement(IASTStatement oldS, IASTStatement newS) {
		IASTNode parent = oldS.getParent();
		if (parent instanceof IASTCompoundStatement) {
			CASTCompoundStatement compS = (CASTCompoundStatement) parent;
			compS.replace(oldS, newS);
		}
		else if (parent instanceof IASTDoStatement) {
			CASTDoStatement doS = (CASTDoStatement) parent;
			doS.replace(oldS, newS);
		}
		else if (parent instanceof IASTForStatement) {
			CASTForStatement forS = (CASTForStatement) parent;
			forS.replace(oldS, newS);
		}
		else if (parent instanceof IASTIfStatement) {
			CASTIfStatement ifS = (CASTIfStatement) parent;
			ifS.replace(oldS, newS);
		}
		else if (parent instanceof IASTSwitchStatement) {
			CASTSwitchStatement switchS = (CASTSwitchStatement) parent;
			switchS.replace(oldS, newS);
		}
		else if (parent instanceof IASTWhileStatement) {
			CASTWhileStatement whileS = (CASTWhileStatement) parent;
			whileS.replace(oldS, newS);
		}
	}

	/* append "stmt" right after the "prev" */
	private void appendStatement(IASTStatement stmt, IASTStatement prev) {
		IASTCompoundStatement newcompS = new CASTCompoundStatement();
		newcompS.addStatement(prev);
		newcompS.addStatement(stmt);
		prev.setParent(newcompS);
		stmt.setParent(newcompS);
		replaceStatement(prev, newcompS);
	}

	private int Level(IASTStatement stmt) {
		int count = 0;
		IASTNode parent = stmt.getParent();
		while (!(parent instanceof IASTFunctionDefinition)) {
			if (parent instanceof IASTIfStatement ||
					parent instanceof IASTForStatement ||
					parent instanceof IASTDoStatement ||
					parent instanceof IASTWhileStatement ||
					parent instanceof IASTSwitchStatement)
				count++;
			parent = parent.getParent();
		}
		return count;
	}

	/* return true if gotoS has lower offset than label, false otherwise */
	private boolean Offset(IASTGotoStatement gotoS, IASTLabelStatement label) {
		OffsetCalculator oc = new OffsetCalculator(currentNode_, gotoS, label);
		return oc.gotoBFlabel();
	}

	class GotoLabelCollector extends ASTVisitor {
		private ICallGraphNode node_;
		private LinkedList<IASTStatement> gotoList_;
		private LinkedList<IASTStatement> labelList_;

		public GotoLabelCollector(ICallGraphNode node) {
			node_ = node;
			gotoList_ = new LinkedList<IASTStatement>();
			labelList_ = new LinkedList<IASTStatement>();
		}

		public void run() {
			this.shouldVisitDeclarations = true;
			this.shouldVisitStatements = true;
			IASTStatement body = node_.getFuncDef().getBody();
			body.accept(this);
		}

		public int visit(IASTStatement stmt) {
			if (stmt instanceof IASTGotoStatement)
				gotoList_.add(stmt);
			else if (stmt instanceof IASTLabelStatement)
				labelList_.add(stmt);
			return PROCESS_CONTINUE;
		}

		public LinkedList getGotoList() {
			return gotoList_;
		}

		public LinkedList getLabelList() {
			return labelList_;
		}
	}

	class OffsetCalculator extends ASTVisitor {
		private ICallGraphNode node_;
		private IASTGotoStatement gotoS_;
		private IASTLabelStatement labelS_;
		private int gotoMet;
		private int labelMet;
		private int count;

		public OffsetCalculator(ICallGraphNode node, IASTGotoStatement gotoS, IASTLabelStatement label) {
			node_ = node;
			gotoS_ = gotoS;
			labelS_ = label;
			gotoMet = -1;
			labelMet = -1;
			count = 0;
		}

		public boolean gotoBFlabel() {
			this.shouldVisitStatements = true;
			IASTStatement body = node_.getFuncDef().getBody();
			body.accept(this);
			if (gotoMet == -1 || labelMet == -1) {
				System.out.println("Goto or label not found!"); //$NON-NLS-1$
			} else if (gotoMet > 1 || labelMet > 1) {
				System.out.println("Multiple gotos and labels are met!"); //$NON-NLS-1$
			}
			return gotoMet < labelMet;
		}

		public int visit(IASTStatement stmt) {
			if (stmt instanceof IASTGotoStatement && stmt == gotoS_) {
				gotoMet = count;
				count++;
			} else if (stmt instanceof IASTLabelStatement && stmt == labelS_) {
				labelMet = count;
				count++;
			}
			return PROCESS_CONTINUE;
		}
	}
}
