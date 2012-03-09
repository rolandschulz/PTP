/**********************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.analysis.PAST;

import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.parser.IToken;
import org.eclipse.cdt.internal.core.dom.parser.ASTInternal;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ptp.pldt.common.util.Utility;
import org.eclipse.ptp.pldt.openmp.analysis.OpenMPError;
import org.eclipse.ptp.pldt.openmp.analysis.OpenMPErrorManager;
import org.eclipse.ptp.pldt.openmp.analysis.dictionary.Dictionary;
import org.eclipse.ptp.pldt.openmp.analysis.dictionary.Symbol;
import org.eclipse.ptp.pldt.openmp.analysis.parser.OpenMPScanner;
import org.eclipse.ptp.pldt.openmp.analysis.parser.OpenMPToken;

/**
 * Factory to convert PASTPragma-->PASTOMPPragma
 * 
 * @author pazel
 * 
 */
@SuppressWarnings("restriction")
public class PASTOMPFactory {
	protected PASTPragma pragma_ = null;
	protected PASTOMPPragma ompPragma_ = null;
	protected IASTTranslationUnit ast_ = null;
	protected Dictionary dictionary_ = null;

	private static final boolean traceOn = false;

	protected OpenMPScanner scanner_ = null;
	protected OpenMPToken token_ = null;

	// protected ScannerCallbackManager callbackManager_ = null;

	/**
	 * Factory used only by this class
	 * 
	 * @param pragma
	 *            - PASTPragma
	 */
	protected PASTOMPFactory(PASTPragma pragma, IASTTranslationUnit ast, Dictionary dictionary) {
		pragma_ = pragma;
		scanner_ = new OpenMPScanner(pragma_.getContent());
		ast_ = ast;
		dictionary_ = dictionary;

		// experiment();

		if (traceOn)
			readTokens();

		// otherinit(null);
	}

	@SuppressWarnings("unused")
	private void experiment() {
		IASTFileLocation loc = pragma_.getFileLocation();
		if (loc != null) {
			IDocument document = Utility.getDocument(loc.getFileName());
			if (document != null) {
				try {
					String txt = document.get(pragma_.getLocalOffset(), pragma_.getLength());
					System.out.println(txt);
				} catch (Exception e) {
				}
			}
		}
	}

	/**
	 * Return either omp or non-omp pragma
	 * 
	 * @return
	 */
	protected PASTPragma retrievePragma() {
		return (ompPragma_ != null ? ompPragma_ : pragma_);
	}

	/**
	 * Factory for making the PASTOMPPragma structure if possible
	 * 
	 * @param pragma
	 *            : PASTPragma
	 * @param ast
	 *            : IASTTranslationUnit
	 * @param dictionary
	 *            : Dictionary
	 * @return: PASTPragma (or PASTOMPPragma if do-able)
	 */
	public static PASTPragma makePASTOMP(PASTPragma pragma, IASTTranslationUnit ast, Dictionary dictionary) {
		PASTOMPFactory factory = new PASTOMPFactory(pragma, ast, dictionary);
		factory.parse();
		factory.locateRegion();
		return factory.retrievePragma();
	}

	/**
	 * Parse the pragma context for OMP
	 * 
	 * @return boolean
	 */
	protected boolean parse() {
		// The first two tokens should be # and pragma
		OpenMPToken tok = nextToken();
		if (tok == null) {
			if (traceOn)
				System.out.println("PASTOMPFactory.parser()..null token, ignored.");
			return false; // robustly handle empty tokens
		}
		if (tok.getType() != OpenMPScanner.mpPound)
			return false;
		tok = nextToken();
		if (tok.getType() != OpenMPScanner.mpPragma)
			return false;

		// if next is not omp - this is not an openmp directive
		if (nextToken().getType() != OpenMPScanner.mpOmp)
			return false;

		// Construct the OpenMP pragma
		ompPragma_ = new PASTOMPPragma(pragma_);

		// The next token sets the type
		nextToken();
		OpenMPToken typeToken = token_; // determines type, used for error
										// message
		if (token_ == null)
			return false;
		ompPragma_.setOMPType(setOMPType(token_.getType()));

		if (token_ == null)
			return false;

		switch (ompPragma_.getOMPType()) {
		case PASTOMPPragma.OmpParallel:
			completeParallel();
			break;
		case PASTOMPPragma.OmpFor:
			completeFor();
			break;
		case PASTOMPPragma.OmpParallelFor:
			completeParallelFor();
			break;
		case PASTOMPPragma.OmpSections:
			completeSections();
			break;
		case PASTOMPPragma.OmpParallelSections:
			completeParallelSections();
			break;
		case PASTOMPPragma.OmpSingle:
			completeSingle();
			break;
		case PASTOMPPragma.OmpMaster:
			break;
		case PASTOMPPragma.OmpCritical:
			break;
		case PASTOMPPragma.OmpBarrier:
			break;
		case PASTOMPPragma.OmpAtomic:
			break;
		case PASTOMPPragma.OmpSection:
			break;
		case PASTOMPPragma.OmpFlush:
			completeFlush();
			break;
		case PASTOMPPragma.OmpOrdered:
			break;
		case PASTOMPPragma.OmpThreadPrivate:
			completeThreadPrivate();
			break;
		case PASTOMPPragma.OmpUnknown:
			String typeString = (typeToken != null ? typeToken.getImage() : "");
			handleProblem("Unexpected token '" + typeString + "'", OpenMPError.ERROR);
			break;
		}

		// all remaining tokens are bogus
		while (token_ != null) {
			handleProblem("Unexpected token '" + token_.getImage() + "'", OpenMPError.ERROR);
			nextToken();
		}

		return true;
	}

	/**
	 * Set the type of OpenMP statement based on keyword
	 * 
	 * @param t
	 * @return Note: always exit with the current token being the next to
	 *         process, i.e. call nextToken()
	 */
	protected int setOMPType(int t) {
		nextToken(); // advance to next token
		switch (t) {
		case OpenMPScanner.mpParallel:
			if (token_ == null)
				return PASTOMPPragma.OmpParallel;
			if (token_.getType() == OpenMPScanner.mpFor) {
				nextToken();
				return PASTOMPPragma.OmpParallelFor;
			} else if (token_.getType() == OpenMPScanner.mpSections) {
				nextToken();
				return PASTOMPPragma.OmpParallelSections;
			}
			return PASTOMPPragma.OmpParallel;
		case OpenMPScanner.mpFor:
			return PASTOMPPragma.OmpFor;
		case OpenMPScanner.mpSections:
			return PASTOMPPragma.OmpSections;
		case OpenMPScanner.mpSection:
			return PASTOMPPragma.OmpSection;
		case OpenMPScanner.mpSingle:
			return PASTOMPPragma.OmpSingle;
		case OpenMPScanner.mpMaster:
			return PASTOMPPragma.OmpMaster;
		case OpenMPScanner.mpCritical:
			return PASTOMPPragma.OmpCritical;
		case OpenMPScanner.mpBarrier:
			return PASTOMPPragma.OmpBarrier;
		case OpenMPScanner.mpAtomic:
			return PASTOMPPragma.OmpAtomic;
		case OpenMPScanner.mpFlush:
			return PASTOMPPragma.OmpFlush;
		case OpenMPScanner.mpOrdered:
			return PASTOMPPragma.OmpOrdered;
		case OpenMPScanner.mpThreadPrivate:
			return PASTOMPPragma.OmpThreadPrivate;
		}
		return PASTOMPPragma.OmpUnknown;
	}

	/**
	 * Parse a list of identifiers, e.g. as from shared(...)
	 * 
	 * @return OpenMPToken []
	 */
	protected OpenMPToken[] getIdentifierList() {
		LinkedList<OpenMPToken> l = new LinkedList<OpenMPToken>();

		if (token_ == null)
			return null;
		// if (token_.getType()!=IToken.tLPAREN) return null;

		// look for lists like a,b,c
		boolean commaNext = false;
		nextToken();
		while (token_ != null) {
			if (commaNext) {
				if (token_.getType() != IToken.tCOMMA)
					break;
				else {
					commaNext = false;
					nextToken();
					continue;
				}
			}
			// whatever it is, add to the list
			l.add(token_);
			commaNext = true;
			nextToken();
		}

		// build the list
		OpenMPToken[] ompl = new OpenMPToken[l.size()];
		int count = 0;
		for (Iterator<OpenMPToken> i = l.iterator(); i.hasNext();) {
			ompl[count++] = i.next();
			// check to see if in dictionary
			Symbol[] symbols = dictionary_.getSymbolsFor(ompl[count - 1].getImage());
			if (symbols.length == 0) {
				handleProblem("Undefined symbol '" + ompl[count - 1].getImage() + "'", OpenMPError.ERROR);
			} else {
				// try to find at least one (non-global that is in same scope as
				// pragma
				boolean found = false;
				for (int j = 0; j < symbols.length; j++) {
					IASTNode fctn = symbols[j].getDefiningFunction();
					if (fctn == null || !(fctn instanceof IASTFunctionDefinition))
						continue;
					if (isSymbolRelevant(symbols[j])) {
						found = true;
						break;
					}
				}
				if (!found)
					handleProblem("Symbol out of scope: '" + ompl[count - 1].getImage() + "'", OpenMPError.ERROR);
			}
		}

		// rule: always leave one ahead
		nextToken();

		return ompl;
	}

	/**
	 * See if symbol is a local variable in scope to pragma
	 * 
	 * @param symbol
	 *            - Symbol
	 * @return boolean
	 */
	protected boolean isSymbolRelevant(Symbol symbol) {
		if (traceOn)
			System.out.println("Symbol: " + symbol.getName() + "  PASTOMPFactory.isSymbolRelevant()");
		IASTNode node = null;
		try {
			// node=symbol.getScope().getPhysicalNode(); // no longer in CDT 4.0
			// BRT replacement for getPhysicalNode() for CDT 4.0
			// The following probably isn't an ideal solution (using Discouraged
			// access methods)
			// but seems to work for now.
			// alternatively I tried implementing: symbol.getPhysicalNode() but
			// could not get the same answer from there.

			IScope scope = symbol.getScope();
			// see: http://dev.eclipse.org/mhonarc/lists/cdt-dev/msg08653.html
			// Another alternative would be to cast to CScope and do
			// cScope.getPhysicalNode() from there.
			node = ASTInternal.getPhysicalNodeOfScope(scope);

		} catch (Exception e) {
			return false;
		}

		Utility.Location l = Utility.getLocation(node);
		int nodeOffset = (l != null ? l.getLow() : 0); // 728
		int nodeEndset = (l != null ? l.getHigh() : 0); // 745
		if (traceOn)
			System.out.println("node: " + node.getRawSignature() + " nodeOffset: " + nodeOffset + " nodeEndset= " + nodeEndset);

		int pOffset = pragma_.getLocalOffset(); // 822
		int pEndset = pOffset + pragma_.getLength() - 1; // 864
		if (traceOn)
			System.out.println(("pragma pOffset= " + pOffset + " pEndset= " + pEndset));

		boolean tf = ((nodeEndset < pOffset || pEndset < nodeOffset) ? false : true); // false
		if (!tf)
			return tf;

		// See if the declaration succeeds the pragma
		Utility.Location dl = Utility.getLocation(symbol.getDeclarator());
		if (dl == null)
			return false;
		if (traceOn)
			System.out.println("dl.getLow()=" + dl.getLow() + " pOffset=" + pOffset + " > is: " + (dl.getLow() > pOffset));

		return (dl.getLow() > pOffset ? false : true);
	}

	/**
	 * Acquire the next token
	 * 
	 * @return OpenMPToken
	 */
	protected OpenMPToken nextToken() {
		// Following in case of backup
		if (token_ != null && token_.getNext() != null) {
			token_ = token_.getNext();
			return token_;
		}

		// chain to last one and move on
		OpenMPToken token = scanner_.nextToken();
		if (token_ != null)
			token_.setNext(token);
		token_ = token;
		return token_;
	}

	/**
	 * Get current token so as to mark (in code) where we were
	 * 
	 * @return OpenMPToken
	 */
	protected OpenMPToken mark() {
		if (token_ == null)
			token_ = nextToken();
		return token_;
	}

	/**
	 * Reset token queue (nextToken() get one after this one)
	 * 
	 * @param token
	 */
	protected void backupTo(OpenMPToken token) {
		token_ = token;
	}

	/**
	 * Test the parser
	 * 
	 */
	private void readTokens() {
		OpenMPScanner scanner = new OpenMPScanner(pragma_.getContent());

		OpenMPToken token = null;
		do {
			token = scanner.nextToken();
			if (token != null)
				System.out.println("Token:" + token.getImage() + " type=" + token.getType());
		} while (token != null);
	}

	/**
	 * Complete the parsing of #pragma omp parallel
	 * 
	 */
	private void completeParallel() {
		boolean bShared = false;
		boolean bPrivate = false;
		boolean bFirstPrivate = false;
		boolean bDefault = false;
		boolean bReduction = false;
		boolean bCopyin = false;
		boolean bIf = false;
		boolean bNumthreads = false;

		while (token_ != null) {
			switch (token_.getType()) {
			case OpenMPScanner.mpShared:
				if (!bShared)
					bShared = setShared();
				break;
			case OpenMPScanner.mpPrivate:
				if (!bPrivate)
					bPrivate = setPrivate();
				break;
			case OpenMPScanner.mpFirstprivate:
				if (!bFirstPrivate)
					bFirstPrivate = setFirstPrivate();
				break;
			case OpenMPScanner.mpDefault:
				if (!bDefault)
					bDefault = setDefault();
				break;
			case OpenMPScanner.mpReduction:
				if (!bReduction)
					bReduction = setReduction();
				break;
			case OpenMPScanner.mpCopyin:
				if (!bCopyin)
					bCopyin = setCopyin();
				break;
			case OpenMPScanner.mpIf:
				if (!bIf)
					bIf = setIf();
				break;
			case OpenMPScanner.mpNumthreads:
				if (!bNumthreads)
					bNumthreads = setNumThreads();
				break;
			default:
				handleProblem("Unexpected token " + token_.getImage(), OpenMPError.ERROR);
				nextToken();
				break;
			}
		}
	}

	/**
	 * Complete parsing #paragma omp for
	 * 
	 */
	private void completeFor() {
		boolean bPrivate = false;
		boolean bFirstPrivate = false;
		boolean bLastPrivate = false;
		boolean bReduction = false;
		boolean bOrdered = false;
		boolean bSchedule = false;
		boolean bNowait = false;

		while (token_ != null) {
			switch (token_.getType()) {
			case OpenMPScanner.mpPrivate:
				if (!bPrivate)
					bPrivate = setPrivate();
				break;
			case OpenMPScanner.mpFirstprivate:
				if (!bFirstPrivate)
					bFirstPrivate = setFirstPrivate();
				break;
			case OpenMPScanner.mpLastprivate:
				if (!bLastPrivate)
					bLastPrivate = setLastPrivate();
				break;
			case OpenMPScanner.mpReduction:
				if (!bReduction)
					bReduction = setReduction();
				break;
			case OpenMPScanner.mpOrdered:
				if (!bOrdered) {
					ompPragma_.setOrdered(true);
					bOrdered = true;
				}
				nextToken();
				break;
			case OpenMPScanner.mpSchedule:
				if (!bSchedule)
					bSchedule = setSchedule();
				break;
			case OpenMPScanner.mpNowait:
				if (!bNowait) {
					ompPragma_.setNoWait(true);
					bNowait = true;
				}
				nextToken();
				break;
			default:
				handleProblem("Unexpected token " + token_.getImage(), OpenMPError.ERROR);
				nextToken();
				break;
			}
		}
	}

	/**
	 * Complete parsing #paragma omp parallel for
	 * 
	 */
	private void completeParallelFor() {
		boolean bShared = false;
		boolean bPrivate = false;
		boolean bFirstPrivate = false;
		boolean bLastPrivate = false;
		boolean bDefault = false;
		boolean bReduction = false;
		boolean bCopyin = false;
		boolean bIf = false;
		boolean bOrdered = false;
		boolean bSchedule = false;

		while (token_ != null) {
			switch (token_.getType()) {
			case OpenMPScanner.mpShared:
				if (!bShared)
					bShared = setShared();
				break;
			case OpenMPScanner.mpPrivate:
				if (!bPrivate)
					bPrivate = setPrivate();
				break;
			case OpenMPScanner.mpFirstprivate:
				if (!bFirstPrivate)
					bFirstPrivate = setFirstPrivate();
				break;
			case OpenMPScanner.mpLastprivate:
				if (!bLastPrivate)
					bLastPrivate = setLastPrivate();
				break;
			case OpenMPScanner.mpDefault:
				if (!bDefault)
					bDefault = setDefault();
				break;
			case OpenMPScanner.mpReduction:
				if (!bReduction)
					bReduction = setReduction();
				break;
			case OpenMPScanner.mpCopyin:
				if (!bCopyin)
					bCopyin = setCopyin();
				break;
			case OpenMPScanner.mpIf:
				if (!bIf)
					bIf = setIf();
				break;
			case OpenMPScanner.mpOrdered:
				if (!bOrdered) {
					ompPragma_.setOrdered(true);
					bOrdered = true;
				}
				nextToken();
				break;
			case OpenMPScanner.mpSchedule:
				if (!bSchedule)
					bSchedule = setSchedule();
				break;
			default:
				handleProblem("Unexpected token " + token_.getImage(), OpenMPError.ERROR);
				nextToken();
				break;
			}
		}
	}

	/**
	 * Complete parsing #pragma omp parallel sections
	 * 
	 */
	private void completeParallelSections() {
		boolean bShared = false;
		boolean bPrivate = false;
		boolean bFirstPrivate = false;
		boolean bLastPrivate = false;
		boolean bDefault = false;
		boolean bReduction = false;
		boolean bCopyin = false;
		boolean bIf = false;

		while (token_ != null) {
			switch (token_.getType()) {
			case OpenMPScanner.mpShared:
				if (!bShared)
					bShared = setShared();
				break;
			case OpenMPScanner.mpPrivate:
				if (!bPrivate)
					bPrivate = setPrivate();
				break;
			case OpenMPScanner.mpFirstprivate:
				if (!bFirstPrivate)
					bFirstPrivate = setFirstPrivate();
				break;
			case OpenMPScanner.mpLastprivate:
				if (!bLastPrivate)
					bLastPrivate = setLastPrivate();
				break;
			case OpenMPScanner.mpDefault:
				if (!bDefault)
					bDefault = setDefault();
				break;
			case OpenMPScanner.mpReduction:
				if (!bReduction)
					bReduction = setReduction();
				break;
			case OpenMPScanner.mpCopyin:
				if (!bCopyin)
					bCopyin = setCopyin();
				break;
			case OpenMPScanner.mpIf:
				if (!bIf)
					bIf = setIf();
				break;
			default:
				handleProblem("Unexpected token " + token_.getImage(), OpenMPError.ERROR);
				nextToken();
				break;
			}
		}
	}

	/**
	 * Complete parse of #pragma omp sections
	 * 
	 */
	private void completeSections() {
		boolean bPrivate = false;
		boolean bFirstPrivate = false;
		boolean bLastPrivate = false;
		boolean bReduction = false;
		boolean bNowait = false;

		while (token_ != null) {
			switch (token_.getType()) {
			case OpenMPScanner.mpPrivate:
				if (!bPrivate)
					bPrivate = setPrivate();
				break;
			case OpenMPScanner.mpFirstprivate:
				if (!bFirstPrivate)
					bFirstPrivate = setFirstPrivate();
				break;
			case OpenMPScanner.mpLastprivate:
				if (!bLastPrivate)
					bLastPrivate = setLastPrivate();
				break;
			case OpenMPScanner.mpReduction:
				if (!bReduction)
					bReduction = setReduction();
				break;
			case OpenMPScanner.mpNowait:
				if (!bNowait) {
					ompPragma_.setNoWait(true);
					bNowait = true;
				}
				nextToken();
				break;
			default:
				handleProblem("Unexpected token " + token_.getImage(), OpenMPError.ERROR);
				nextToken();
				break;
			}
		}

	}

	/**
	 * Complete options for the #pragma omp single
	 * 
	 */
	private void completeSingle() {
		boolean bPrivate = false;
		boolean bFirstPrivate = false;
		boolean bCopyPrivate = false;
		boolean bNowait = false;

		while (token_ != null) {
			switch (token_.getType()) {
			case OpenMPScanner.mpPrivate:
				if (!bPrivate)
					bPrivate = setPrivate();
				break;
			case OpenMPScanner.mpFirstprivate:
				if (!bFirstPrivate)
					bFirstPrivate = setFirstPrivate();
				break;
			case OpenMPScanner.mpCopyprivate:
				if (!bCopyPrivate)
					bCopyPrivate = setCopyPrivate();
				break;
			case OpenMPScanner.mpNowait:
				if (!bNowait) {
					ompPragma_.setNoWait(true);
					bNowait = true;
				}
				nextToken();
				break;
			default:
				handleProblem("Unexpected token " + token_.getImage(), OpenMPError.ERROR);
				nextToken();
				break;
			}
		}

	}

	/**
	 * Complete the options for the #pragma omp flush
	 * 
	 */
	private void completeFlush() {
		if (token_ == null || token_.getType() != IToken.tLPAREN)
			return;

		OpenMPToken[] list = getIdentifierList();

		ompPragma_.setPrivateList(list);
	}

	/**
	 * Complete the options for the #pragma omp threadprivate
	 * 
	 */
	private void completeThreadPrivate() {
		if (token_ == null || token_.getType() != IToken.tLPAREN)
			return;

		OpenMPToken[] list = getIdentifierList();

		ompPragma_.setThreadPrivateList(list);
	}

	/**
	 * Translate the type of reduction operato
	 * 
	 * @return int (that PASTOMPPragma understands)
	 */
	private int getReductionOperator() {
		switch (token_.getType()) {
		case IToken.tPLUS:
			return PASTOMPPragma.OmpOpPlus;
		case IToken.tSTAR:
			return PASTOMPPragma.OmpOpMult;
		case IToken.tMINUS:
			return PASTOMPPragma.OmpOpMinus;
		case IToken.tAMPER:
			return PASTOMPPragma.OmpOpBAnd;
		case IToken.tXOR:
			return PASTOMPPragma.OmpOpBXor;
		case IToken.tBITOR:
			return PASTOMPPragma.OmpOpBOr;
		case IToken.tAND:
			return PASTOMPPragma.OmpOpLAnd;
		case IToken.tOR:
			return PASTOMPPragma.OmpOpLOr;
		default:
			return PASTOMPPragma.OmpOpUnknown;
		}
	}

	/**
	 * Translate the kind of schedule
	 * 
	 * @return int (that PASTOMPPragma understands)
	 */
	private int getScheduleKind() {
		switch (token_.getType()) {
		case OpenMPScanner.mpStatic:
			return PASTOMPPragma.OmpSKStatic;
		case OpenMPScanner.mpDynamic:
			return PASTOMPPragma.OmpSKDynamic;
		case OpenMPScanner.mpGuided:
			return PASTOMPPragma.OmpSKGuided;
		case OpenMPScanner.mpRuntime:
			return PASTOMPPragma.OmpSKRuntime;
		default:
			return PASTOMPPragma.OmpSKUnknown;
		}
	}

	/**
	 * Get the schedule expression
	 * 
	 * @return OpenMPToken []
	 */
	private OpenMPToken[] getExpression() {
		LinkedList<OpenMPToken> l = new LinkedList<OpenMPToken>();
		int parenCt = 1;

		nextToken();
		while (token_ != null) {
			if (token_.getType() == IToken.tRPAREN) {
				parenCt--;
				if (parenCt == 0)
					break;
				else {
					l.add(token_);
				} // end of schedule clause
			} else if (token_.getType() == IToken.tCOMMA) {
				if (parenCt == 1)
					break;
				else
					l.add(token_); // another way to exit
			} else if (token_.getType() == IToken.tLPAREN) {
				parenCt++;
				l.add(token_);
			} else
				l.add(token_);
			nextToken();
		}

		OpenMPToken[] list = new OpenMPToken[l.size()];
		for (int i = 0; i < l.size(); i++)
			list[i] = l.get(i);

		nextToken(); // move ahead
		return list;
	}

	private boolean setIf() {
		nextToken();
		OpenMPToken[] list = getExpression();
		ompPragma_.setIfExpression(list);
		return true;
	}

	private boolean setPrivate() {
		nextToken();
		OpenMPToken[] list = getIdentifierList();
		ompPragma_.setPrivateList(list);
		return true;
	}

	private boolean setFirstPrivate() {
		nextToken();
		OpenMPToken[] list = getIdentifierList();
		ompPragma_.setFirstPrivateList(list);
		return true;
	}

	private boolean setLastPrivate() {
		nextToken();
		OpenMPToken[] list = getIdentifierList();
		ompPragma_.setLastPrivateList(list);
		return true;
	}

	private boolean setShared() {
		nextToken();
		OpenMPToken[] list = getIdentifierList();
		ompPragma_.setSharedList(list);
		return true;
	}

	private boolean setCopyin() {
		nextToken();
		OpenMPToken[] list = getIdentifierList();
		ompPragma_.setCopyinList(list);
		return true;
	}

	private boolean setCopyPrivate() {
		nextToken();
		OpenMPToken[] list = getIdentifierList();
		ompPragma_.setCopyPrivateList(list);
		return true;
	}

	private boolean setDefault() {
		boolean shared = false;
		nextToken();
		if (token_.getType() == IToken.tLPAREN) {
			nextToken();
			if (token_.getType() == OpenMPScanner.mpShared)
				shared = true;
			else if (token_.getType() == OpenMPScanner.mpNone)
				shared = false;
			else
				return false;
			nextToken(); // get the paren
			if (token_ == null || token_.getType() != IToken.tRPAREN)
				return false;
			ompPragma_.setDefault(shared ? PASTOMPPragma.OmpShared : PASTOMPPragma.OmpNone);
			nextToken();
			return true;
		}
		return false;
	}

	private boolean setReduction() {
		nextToken();
		if (token_.getType() == IToken.tLPAREN) {
			nextToken();
			int ro = getReductionOperator();
			nextToken();
			if (token_ != null && token_.getType() == IToken.tCOLON) {
				OpenMPToken[] rlist = getIdentifierList();
				ompPragma_.setReductionOperator(ro);
				ompPragma_.setReductionList(rlist);
				return true;
			}
			return false;
		}
		return false;
	}

	private boolean setNumThreads() {
		nextToken();
		if (token_.getType() == IToken.tLPAREN) {
			OpenMPToken[] expr = getExpression();
			if (token_ == null || token_.getType() != IToken.tRPAREN)
				return false;
			ompPragma_.setNumThreadsExpr(expr);
			return true;
		}
		return false;
	}

	private boolean setSchedule() {
		nextToken();
		if (token_.getType() == IToken.tLPAREN) {
			nextToken();
			int kind = getScheduleKind();
			nextToken();
			if (token_ != null && token_.getType() == IToken.tCOMMA) {
				OpenMPToken[] expr = getExpression();
				ompPragma_.setScheduleKind(kind);
				ompPragma_.setChunkExpression(expr);
				return true;
			}
			return false;
		}
		return false;
	}

	// We are presently unsure of what to do with all the following stuff:
	// protected static final ScannerProblemFactory spf = new
	// ScannerProblemFactory();
	// protected ScannerCallbackManager callbackManager;
	// protected static char[] EMPTY_CHAR_ARRAY = new char[0];
	//
	// protected void otherinit(ISourceElementRequestor requestor)
	// {
	// callbackManager = new ScannerCallbackManager(new
	// NullSourceElementRequestor());
	// }

	/**
	 * handleProblem
	 * 
	 * @param description
	 *            - String
	 * @param severity
	 *            - int
	 */
	protected void handleProblem(String description, int severity) {
		OpenMPError error = new OpenMPError(description, pragma_.getContainingFilename(), pragma_.getStartingLine(), severity);
		OpenMPErrorManager.getCurrentErrorManager().addError(error);
		ompPragma_.addProblem(error); // we really don't need this, but may be
										// useful later
	}

	/**
	 * lFind the associated region to the current pragma
	 * 
	 */
	private void locateRegion() {
		if (ompPragma_ == null)
			return;

		switch (ompPragma_.getOMPType()) {
		// followed by structured region
		case PASTOMPPragma.OmpParallel:
		case PASTOMPPragma.OmpSections:
		case PASTOMPPragma.OmpSection:
		case PASTOMPPragma.OmpParallelSections:
		case PASTOMPPragma.OmpSingle:
		case PASTOMPPragma.OmpMaster:
		case PASTOMPPragma.OmpCritical:
		case PASTOMPPragma.OmpOrdered:
			determineRegion(STRUCTURED_BLOCK, ompPragma_);
			break;

		// Must be followed by FOR
		case PASTOMPPragma.OmpFor:
		case PASTOMPPragma.OmpParallelFor:
			determineRegion(FOR_BLOCK, ompPragma_);
			break;

		// Stands alone
		case PASTOMPPragma.OmpBarrier:
		case PASTOMPPragma.OmpFlush:
		case PASTOMPPragma.OmpThreadPrivate:
			determineRegion(LOCATION_ONLY, ompPragma_);
			break;

		// Followed by expression
		case PASTOMPPragma.OmpAtomic:
			determineRegion(EXPRESSION_BLOCK, ompPragma_);
			break;
		case PASTOMPPragma.OmpUnknown:
			break;
		}
	}

	public static final int STRUCTURED_BLOCK = RegionDeterminationVisitor.STRUCTURED_BLOCK;
	public static final int FOR_BLOCK = RegionDeterminationVisitor.FOR_BLOCK;
	public static final int EXPRESSION_BLOCK = RegionDeterminationVisitor.EXPRESSION_BLOCK;
	public static final int LOCATION_ONLY = RegionDeterminationVisitor.LOCATION_ONLY;

	/**
	 * Determine that code region affiliated with a pragma & the peer node
	 * 
	 * @param type
	 *            - int (see constants above)
	 * @param ompPragma
	 *            - PASTOMPPragma
	 */
	protected void determineRegion(int type, PASTOMPPragma ompPragma) {
		RegionDeterminationVisitor rdv = new RegionDeterminationVisitor(type, ompPragma);
		ast_.accept(rdv);

		// Ensure that region for structured block is a compound statement
		if (type == STRUCTURED_BLOCK) {
			IASTNode region = ompPragma.getRegion();
			if (region == null || !(region instanceof IASTCompoundStatement))
				handleProblem("Pragma expects structured block to follow it", OpenMPError.ERROR);
		} else if (type == FOR_BLOCK) {
			IASTNode region = ompPragma.getRegion();
			if (region == null || !(region instanceof IASTForStatement))
				handleProblem("Pragma expects for loop to follow it", OpenMPError.ERROR);
		}
	}

	/**
	 * RegionDeterminationVisitor is used to traverse AST to find region
	 */
	protected class RegionDeterminationVisitor extends ASTVisitor {
		protected int searchType_ = STRUCTURED_BLOCK;
		protected PASTOMPPragma oPragma_ = null;
		protected int pragmaLine_ = 0;
		protected int pragmaLocation_ = 0;
		protected int pragmaLength_ = 0;

		protected int closeness_ = -1;

		// for statement location
		// protected IASTStatement lastStatement_ = null;

		public final static int STRUCTURED_BLOCK = 0;
		public final static int FOR_BLOCK = 1;
		public final static int EXPRESSION_BLOCK = 2;
		public final static int LOCATION_ONLY = 3; // tell me what immediately
													// precedes

		public RegionDeterminationVisitor(int type, PASTOMPPragma ompPragma) {
			searchType_ = type;
			switch (searchType_) {
			case STRUCTURED_BLOCK:
				shouldVisitStatements = true;
				break;
			case FOR_BLOCK:
				shouldVisitStatements = true;
				break;
			case EXPRESSION_BLOCK:
				shouldVisitStatements = true; // we want an expression statement
				break;
			case LOCATION_ONLY:
				shouldVisitStatements = true;
				break;
			}

			oPragma_ = ompPragma;
			pragmaLine_ = oPragma_.getStartingLine();
			pragmaLocation_ = oPragma_.getOffset(); // oPragma_.getStartLocation();
			pragmaLength_ = oPragma_.getLength();
		}

		/**
		 * override function to visit statements implementation NOTE: Region is
		 * first statement following pragma
		 * 
		 * @param statement
		 *            - IASTStatement
		 * @return int
		 */
		@Override
		// remove later
		public int visit(IASTStatement statement) { // BRT debugging here.
													// region is being set
													// wrong, needs to be
													// drilled down more
			ASTNode node = (statement instanceof ASTNode ? (ASTNode) statement : null);
			if (node == null)
				return PROCESS_CONTINUE;

			// ensure the node is in the same file as the pragma
			if (node.getContainingFilename().equals(oPragma_.getFileLocation().getFileName())) {
				int totalOffset = node.getOffset();
				// test use of IASTNode vs ASTNode. compare with
				// Utility.getLocation which uses a combination of the two
				IASTNode inode = node;
				if (traceOn) {
					int ilen = inode.getNodeLocations()[0].getNodeLength();
					int ioff = inode.getNodeLocations()[0].getNodeOffset();
					System.out.println("    ilen=" + ilen + " ioff=" + ioff);
				}

				Utility.Location loc = Utility.getLocation(node);
				assert (loc != null);
				int localOffset = loc.getLow(); // this is the offset local to
												// the file
				int length = loc.getHigh() - loc.getLow() + 1;

				// We look at all nodes that occur before the pragma - 2 cases
				// 1) if the node scope encompases the pragma, pragma is a child
				// of node
				// 2) otherwise we call it a peer (even when it isn't)
				// Corrections occur by continuing for the tightest fit
				// // https://bugs.eclipse.org/bugs/show_bug.cgi?id=253200 fixed
				// (see PASTPragma.getOffset(); this println helps, should be
				// called several times as it zeroes in on the statement closest
				// to the pragma
				if (traceOn)
					System.out.println("totalOffset " + totalOffset + " < pragmaLocation " + pragmaLocation_);// from
																												// ptp20
				if (totalOffset < pragmaLocation_) {
					if (pragmaLocation_ + pragmaLength_ < totalOffset + length) { // encompassing
						oPragma_.setLocation(statement, PASTOMPPragma.ChildProximity);
					} else {
						if (totalOffset + length < pragmaLocation_) {
							int closeness = pragmaLocation_ - (totalOffset + length);
							if (closeness_ == -1 || closeness < closeness_) { // get
																				// closest
																				// statement
								oPragma_.setLocation(statement, PASTOMPPragma.NeighborProximity);
								closeness_ = closeness;
							}
						}
					}
					// keep going to find tightest fit
				}

				// Check if this is the first node after the pragma - if so,
				// could be our region
				if (totalOffset > pragmaLocation_) {
					if (searchType_ != LOCATION_ONLY) {
						// With this we got to the next stmt:
						if (searchType_ == FOR_BLOCK && !(statement instanceof IASTForStatement))
							return PROCESS_ABORT; // error handled in
													// determineRegion
						// Set region information (ref. OpenMPArtifactView to
						// see how used)
						oPragma_.setRegionFilename(node.getContainingFilename());
						oPragma_.setRegionLength(length);
						oPragma_.setRegionOffset(localOffset);
						oPragma_.setRegion(statement);
						if (traceOn)
							System.out.println((searchType_ == FOR_BLOCK ? "(for)" : "(region)") + "pragma at " + pragmaLocation_
									+ " has statement at " + localOffset);
					}
					return PROCESS_ABORT;
				}
			}
			return PROCESS_CONTINUE;
		}

	}

}
