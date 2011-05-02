/**********************************************************************
 * Copyright (c) 2006,2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.analysis;

import java.util.Set;
import java.util.Stack;

import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.dom.IASTServiceProvider;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ptp.pldt.common.util.Utility;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTElif;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTElse;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTEndif;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTFactory;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTIf;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTIfdef;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTIfndef;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTNode;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTOMPFactory;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTOMPPragma;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTPragma;
import org.eclipse.ptp.pldt.openmp.analysis.dictionary.Dictionary;
import org.eclipse.ptp.pldt.openmp.analysis.dictionary.DictionaryFactory;
import org.eclipse.ptp.pldt.openmp.analysis.ompcfg.factory.FileConcurrencyAnalysis;
import org.eclipse.ptp.pldt.openmp.analysis.ompcfg.factory.FileStatementMap;
import org.eclipse.ptp.pldt.openmp.analysis.ompcfg.factory.FunctionConcurrencyAnalysis;

/**
 * @author pazel
 */
public class OpenMPAnalysisManager
{
	protected IASTTranslationUnit astTransUnit_ = null;
	protected IFile iFile_ = null;
	protected PASTNode[] past_ = null;
	protected PASTOMPPragma[] ompPragmas_ = null;

	protected FileConcurrencyAnalysis fileAnalysis_ = null;
	protected FunctionConcurrencyAnalysis[] analyses_ = null;

	protected FileStatementMap fileMap_ = null;
	private static final boolean traceOn = false;

	protected Dictionary dictionary_ = null;
	protected OpenMPErrorManager errorManager_ = new OpenMPErrorManager();

	protected static OpenMPAnalysisManager currentManager_ = null;

	/**
	 * Constructor called by eclipse
	 * 
	 * @param file
	 * @throws OpenMPAnalysisException
	 */
	public OpenMPAnalysisManager(final IFile iFile) throws OpenMPAnalysisException
	{
		try {
			astTransUnit_ = CDOM.getInstance().getASTService().getTranslationUnit(iFile,
					CDOM.getInstance().getCodeReaderFactory(CDOM.PARSE_SAVED_RESOURCES));
		} catch (IASTServiceProvider.UnsupportedDialectException e) {
			throw new OpenMPAnalysisException(e.toString());
		}
		iFile_ = iFile;
		init();

		currentManager_ = this;
	}

	/**
	 * Constructor used by testing mechanism
	 * 
	 * @param astTransUnit
	 */
	public OpenMPAnalysisManager(IASTTranslationUnit astTransUnit, IFile iFile)
	{
		astTransUnit_ = astTransUnit;
		iFile_ = iFile;
		init();
		currentManager_ = this;
	}

	/**
	 * Get the last analysis manager built
	 * 
	 * @return OpenMPAnalysisManager
	 */
	public static OpenMPAnalysisManager getCurrentManager()
	{
		return currentManager_;
	}

	/**
	 * main logic invoked by both constructors
	 * 
	 */
	private void init()
	{
		// Note: throughout all this, it is best to use the same AST instance, i.e.
		// don't re-gen it, as references to nodes may be made by various components
		// that should hold up under equality.
		buildPreprocessorAST();

		buildDictionary();

		buildOMPPragmas();

		buildOMPConcurrencyAnalysis();

		buildFileMap();

		// buildDomoCDT();
	}

	/**
	 * Build all the nodes, compute which compiled
	 * 
	 */
	private void buildPreprocessorAST()
	{
		IASTPreprocessorStatement[] plist = astTransUnit_.getAllPreprocessorStatements();

		past_ = new PASTNode[plist.length];

		for (int i = 0; i < plist.length; i++)
			past_[i] = PASTFactory.PASTFactoryMake(plist[i]);

		// Set the compiled flags to show live and dead preprocessor statements
		computeCompiled();

	}

	/**
	 * Build the "variable" dictionary
	 * 
	 */
	private void buildDictionary()
	{
		dictionary_ = DictionaryFactory.buildDictionary(astTransUnit_);
	}

	// private void buildDomoCDT()
	// {
	// CDTTranslateToCAst ctrans = new CDTAST2CAstTranslator(new IASTNodeFactory_c());
	// CAstEntity entity = ctrans.translate(astTransUnit_);
	//
	// PrintWriter printWriter= new PrintWriter(System.out);
	//
	// CAstPrinter.printTo(entity, printWriter);
	// printWriter.flush();
	//
	// }

	/**
	 * Build the OMP style pragmas
	 * 
	 */
	private void buildOMPPragmas()
	{
		int pCount = 0;
		IDocument pragmaDoc = null;
		String filename = "";

		for (int i = 0; i < past_.length; i++) {
			if (past_[i] instanceof PASTPragma) {
				PASTPragma pragma = (PASTPragma) past_[i];
				//
				// For each of these, get the content
				//
				String newFileName = ((PASTPragma) past_[i]).getContainingFilename();
				if (!filename.equals(newFileName)) {
					filename = newFileName;
					pragmaDoc = Utility.getDocument(filename);
				}
				if (pragmaDoc != null) {
					try {
						String content = pragmaDoc.get(pragma.getLocalOffset(), pragma.getLength());
						pragma.setContent(content);
					} catch (Exception e) {
					}
				}

				past_[i] = PASTOMPFactory.makePASTOMP((PASTPragma) past_[i], astTransUnit_, dictionary_);
				// if successful, would be an omp
				if (past_[i] instanceof PASTOMPPragma)
					pCount++;
			}
		}

		// build the pragma list
		int index = 0;
		ompPragmas_ = new PASTOMPPragma[pCount];
		for (int i = 0; i < past_.length; i++) {
			if (past_[i] instanceof PASTOMPPragma)
				ompPragmas_[index++] = (PASTOMPPragma) past_[i];
		}

		if (traceOn)
			printPASTResults();
	}

	/**
	 * Build concurrency analysis
	 * 
	 */
	private void buildOMPConcurrencyAnalysis()
	{
		fileAnalysis_ = new FileConcurrencyAnalysis(astTransUnit_,
				iFile_,
				getOMPPragmas());
		/*
		 * DefVisitor dv = new DefVisitor(astTransUnit_, iFile_, getOMPPragmas());
		 * analyses_ = dv.buildAnalyses();
		 * 
		 * // build the phase analysis
		 * for(int i=0; i<analyses_.length; i++) {
		 * FunctionConcurrencyAnalysis oca = analyses_[i];
		 * oca.doPhaseAnalysis();
		 * if (traceOn) oca.printAnalysis(System.out);
		 * }
		 */
	}

	/**
	 * Build the location-->stmt/expr map
	 * 
	 */
	protected void buildFileMap()
	{
		// Note - important that we use SAME AST for all analysis and lookup
		fileMap_ = new FileStatementMap(astTransUnit_);
		fileMap_.buildMap();
	}

	/**
	 * Get the pragma AST tree
	 * 
	 * @return PASTNode []
	 */
	public PASTNode[] getPAST()
	{
		return past_;
	}

	/**
	 * Return a list of PASTOMPPragmas
	 * 
	 * @return
	 */
	public PASTOMPPragma[] getOMPPragmas()
	{
		return ompPragmas_;
	}

	/**
	 * Return IASTTranslationUnit
	 * 
	 * @return IASTTranslationUnit
	 */
	public IASTTranslationUnit getTU()
	{
		return astTransUnit_;
	}

	/**
	 * Accessor to the file map for analyzed file
	 * 
	 * @return FileStatementMap
	 */
	public FileStatementMap getFileMap()
	{
		return fileMap_;
	}

	/**
	 * Get the analysis for each function
	 * 
	 * @return FunctionConcurrencyAnalysis []
	 */
	public FunctionConcurrencyAnalysis[] getAnalyses()
	{
		return analyses_;
	}

	/**
	 * Get all nodes concurrent to given node
	 * 
	 * @param node
	 *            - IASTNode
	 * @return Set
	 */
	public Set getNodesConcurrentTo(IASTNode node)
	{
		return fileAnalysis_.getNodesConcurrentTo(node);
	}

	/**
	 * Prints out results for debugging
	 * 
	 */
	protected void printPASTResults()
	{
		// output the results
		for (int i = 0; i < past_.length; i++) {
			System.out.print(past_[i].getType() + " " + (past_[i].isCompiled() ? "compiled" : "not compiled"));
			System.out.print(" at (" + past_[i].getFilename() + "," + past_[i].getStartingLine() + "," +
					past_[i].getStartLocation() + "," + past_[i].getEndLocation() + ") ");
			if (past_[i] instanceof PASTOMPPragma) {
				int t = ((PASTOMPPragma) past_[i]).getOMPType();
				System.out.println(" OMPPragma type=" + t);
			}
			else
				System.out.println("");
		}
	}

	/**
	 * Compute which statements compiled
	 * 
	 */
	private void computeCompiled()
	{
		Stack context = new Stack();
		boolean currentEvaluation = true;
		int currentLevel = 0;
		int lastNonIgnoredIf = currentLevel;
		context.push(new CompiledContext(currentLevel, currentEvaluation));

		int ifcount = 0;
		int endifcount = 0;

		int i = 0;
		for (i = 0; i < past_.length; i++) {
			PASTNode node = past_[i];
			if (node == null) {
				int stopHere = 0;
			}
			if (currentEvaluation) {
				if (isIf(node)) {
					currentEvaluation = ifDecision(node);
					currentLevel++;
					lastNonIgnoredIf++;
					context.push(new CompiledContext(currentLevel, currentEvaluation));
					ifcount++;
				}
				else if (isElse(node)) {
					currentEvaluation = false;
					if (context.empty())
						break;
					((CompiledContext) (context.peek())).setEvaluation(currentEvaluation);
				}
				else if (isElseif(node)) {
					CompiledContext cc = ((CompiledContext) (context.peek()));
					cc.setWasTrue(true);
					currentEvaluation = false;
					// Don't bump the level
					cc.setEvaluation(false);
				}
				else if (isEnd(node)) {
					if (context.empty())
						break;
					context.pop();
					currentLevel--;
					lastNonIgnoredIf--;
					if (context.empty())
						break;
					currentEvaluation = ((CompiledContext) (context.peek())).getEvaluation();
					endifcount++;
				}
				node.setCompiled(true);
			}
			else { // current evaluation == false
				if (currentLevel == lastNonIgnoredIf) {
					if (isIf(node)) {
						currentLevel++;
						node.setCompiled(false);
						ifcount++;
					}
					else if (isElse(node)) {
						CompiledContext cc = ((CompiledContext) (context.peek()));
						if (cc.wasTrue())
							currentEvaluation = false;
						else
							currentEvaluation = true;
						if (context.empty())
							break;
						((CompiledContext) (context.peek())).setEvaluation(currentEvaluation);
						node.setCompiled(true); // means was not ignored - had effect
					}
					else if (isElseif(node)) {
						CompiledContext cc = ((CompiledContext) (context.peek()));
						if (cc.wasTrue())
							currentEvaluation = false;
						else {
							currentEvaluation = ifDecision(node);
							if (currentEvaluation)
								cc.setWasTrue(true);
						}
						// Don't bump the level
						cc.setEvaluation(currentEvaluation);
					}
					else if (isEnd(node)) {
						context.pop();
						currentLevel--;
						lastNonIgnoredIf--;
						if (context.empty())
							break;
						currentEvaluation = ((CompiledContext) (context.peek())).getEvaluation();
						node.setCompiled(currentEvaluation);
						endifcount++;
					}
					else
						node.setCompiled(false);
				}
				else {
					if (isIf(node)) {
						currentLevel++;
						ifcount++;
					}
					else if (isEnd(node)) {
						currentLevel--;
						endifcount++;
					}
					node.setCompiled(false);
				}
			}

		}
	}

	/**
	 * Determine if node is a branch
	 * 
	 * @param node
	 * @return
	 */
	private boolean isIf(PASTNode node)
	{
		return (node instanceof PASTIf) || (node instanceof PASTIfdef) || (node instanceof PASTIfndef);
	}

	/**
	 * Retrieve the compile decision of the 'if' condition
	 * 
	 * @param node
	 * @return
	 */
	private boolean ifDecision(PASTNode node)
	{
		if (node instanceof PASTIf)
			return ((PASTIf) node).taken();
		else if (node instanceof PASTIfdef)
			return ((PASTIfdef) node).taken();
		else if (node instanceof PASTIfndef)
			return ((PASTIfndef) node).taken();
		else
			return false;
	}

	/**
	 * Determine if the node is of 'else' kind
	 * 
	 * @param node
	 * @return
	 */
	private boolean isElse(PASTNode node)
	{
		return (node instanceof PASTElse);
	}

	/**
	 * Determine if the node is of 'elseif' kind
	 * 
	 * @param node
	 * @return
	 */
	private boolean isElseif(PASTNode node)
	{
		return (node instanceof PASTElif);
	}

	/**
	 * Determine if node is of 'end' kind
	 * 
	 * @param node
	 * @return
	 */
	private boolean isEnd(PASTNode node)
	{
		return (node instanceof PASTEndif);
	}

	/**
	 * Stack object used for evaluating "compiled" state of PP's
	 */
	private static class CompiledContext
	{
		private boolean evaluation_ = false;
		private int nestedLevel_ = 0;
		private boolean wasTrue_ = false; // used for elif situations

		public CompiledContext(int nestedLevel, boolean evaluation)
		{
			evaluation_ = evaluation;
			nestedLevel_ = nestedLevel;
		}

		public boolean getEvaluation()
		{
			return evaluation_;
		}

		public void setEvaluation(boolean evaluation)
		{
			evaluation_ = evaluation;
		}

		public int getNestedLevel()
		{
			return nestedLevel_;
		}

		public boolean wasTrue() {
			return wasTrue_;
		}

		public void setWasTrue(boolean tf) {
			wasTrue_ = tf;
		}
	}
}
