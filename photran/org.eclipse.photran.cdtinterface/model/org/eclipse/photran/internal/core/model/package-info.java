/**
 * This package contains classes implementing Fortran extensions to the C Model.
 * <p>
 * The model of a Fortran source file is displayed in the Outline view as well as in the Fortran
 * Projects view.
 * <p>
 * The <i>C Model</i> (a CDT data structure which Photran inherits) is a tree representing a C
 * workspace.  Projects are just beneath the root, and they have folders and translation units
 * (files) beneath them. The model also knows the high-level structure of each translation unit.
 * E.g., it knows the modules, functions, classes (C++), and fields (C++) in each file, but does not
 * know about individual statements, local variables, etc.  Thus, the C Model is <i>not</i> a full
 * abstract syntax tree; it is a much coarser tree which only represents the highest-level,
 * organizational constructs in a file.
 * 
 * @see org.eclipse.cdt.core.model.IContributedModelBuilder
 */
package org.eclipse.photran.internal.core.model;