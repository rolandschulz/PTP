/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.fdt.internal.core.parser;

import org.eclipse.fdt.core.parser.ParserFactoryError;
import org.eclipse.fdt.core.parser.ParserMode;
import org.eclipse.fdt.core.parser.extension.ExtensionDialect;
import org.eclipse.fdt.core.parser.extension.IASTFactoryExtension;
import org.eclipse.fdt.core.parser.extension.IParserExtension;
import org.eclipse.fdt.core.parser.extension.IParserExtensionFactory;
import org.eclipse.fdt.internal.core.parser.ast.GCCASTExtension;

/**
 * @author jcamelon
 */
public class ParserExtensionFactory implements IParserExtensionFactory {

	private final ExtensionDialect dialect;

	/**
	 * @param dialect
	 */
	public ParserExtensionFactory(ExtensionDialect dialect) {
		this.dialect = dialect;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.extension.IParserExtensionFactory#createParserExtension()
	 */
	public IParserExtension createParserExtension() throws ParserFactoryError  
	{
		if( dialect == ExtensionDialect.GCC )
			return new GCCParserExtension();
		throw new ParserFactoryError( ParserFactoryError.Kind.BAD_DIALECT );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.extension.IParserExtensionFactory#createASTExtension(org.eclipse.fdt.core.parser.ParserMode)
	 */
	public IASTFactoryExtension createASTExtension(ParserMode mode) {
		if( dialect == ExtensionDialect.GCC )
			return GCCASTExtension.createExtension( mode );
		throw new ParserFactoryError( ParserFactoryError.Kind.BAD_DIALECT );
	}

}
