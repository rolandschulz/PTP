/**
 * This package contains the interfaces implemented by various Fortran refactorings.
 * <p>
 * Generally speaking, refactorings are referenced directly by their class name.
 * This is true for most of Photran's code.
 * <p>
 * However, at UIUC, we have some alternate implementations of some refactorings,
 * and we reuse the test suite from the traditional implementation; therefore, the
 * test suites use the interfaces to refer to the refactoring.
 */
package org.eclipse.photran.internal.core.refactoring.interfaces;