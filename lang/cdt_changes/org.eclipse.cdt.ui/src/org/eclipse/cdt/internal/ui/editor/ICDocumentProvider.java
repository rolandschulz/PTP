package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.ui.texteditor.IDocumentProvider;

public interface ICDocumentProvider extends IDocumentProvider {

	/*
	 * @see org.eclipse.jdt.internal.ui.javaeditor.ICompilationUnitDocumentProvider#getWorkingCopy(java.lang.Object)
	 */
	public abstract IWorkingCopy getWorkingCopy(Object element);

	/*
	 * @see org.eclipse.jdt.internal.ui.javaeditor.ICompilationUnitDocumentProvider#shutdown()
	 */
	public abstract void shutdown();

}