/**
 * The classes in this package perform name binding analysis on Fortran programs and store the
 * results in the VPG database.  The entrypoint to the analysis is
 * {@link Binder#bind(org.eclipse.photran.core.IFortranAST, org.eclipse.core.resources.IFile)}.
 * <b>This analysis should only be invoked by
 * {@link org.eclipse.photran.internal.core.vpg.PhotranVPGBuilder};</b> clients should retrieve
 * the computed name binding information by calling
 * {@link org.eclipse.photran.internal.core.lexer.Token#resolveBinding()}.
 * <p>
 * For more information, please see the <i>Photran Developer's Guide</i> in the
 * org.eclipse.photran-dev-docs project.
 */
package org.eclipse.photran.internal.core.analysis.binding;