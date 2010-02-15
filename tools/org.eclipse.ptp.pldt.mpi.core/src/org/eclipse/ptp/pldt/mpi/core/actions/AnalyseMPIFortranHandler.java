package org.eclipse.ptp.pldt.mpi.core.actions;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.photran.internal.cdtinterface.core.FortranLanguage;
import org.eclipse.photran.internal.core.lexer.LexerFactory;
import org.eclipse.photran.internal.core.lexer.SourceForm;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.Parser;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.mpi.core.analysis.MpiFortranASTVisitor;

public class AnalyseMPIFortranHandler {
	public void run(String languageID, ITranslationUnit tu, String fileName, ScanReturn msr) {
		if (languageID.equals(FortranLanguage.LANGUAGE_ID)){
			System.out.println("found fortran!");
			IResource res = tu.getUnderlyingResource();
			if (!(res instanceof IFile)) throw new IllegalStateException();
			IFile file = (IFile)res;
			
			try {
				ASTExecutableProgramNode ast = new Parser().parse(LexerFactory.createLexer(file, SourceForm.AUTO_DETECT_SOURCE_FORM, true));
				ast.accept(new MpiFortranASTVisitor(fileName, msr));
			} catch (Exception e) {
				e.printStackTrace(); // TODO
			}
		}
	}
}
