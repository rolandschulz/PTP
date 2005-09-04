/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.fdt.managedbuilder.core.makegen;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.String;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.makegen.IManagedDependencyGenerator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.IPath;
import org.eclipse.fdt.core.FortranLanguage;
// import org.eclipse.fdt.internal.core.f95parser.FortranProcessor;
// import org.eclipse.fdt.internal.core.f95parser.ILexer;
// import org.eclipse.fdt.internal.core.f95parser.Terminal;
// import org.eclipse.fdt.internal.core.f95parser.Token;

/**
 * @since 2.0
 */
public class DefaultFortranDependencyCalculator implements IManagedDependencyGenerator {

	/*
	 * Return a list of the names of all modules used by a file
	 */
	private String[] findUsedModuleNames(File file) {
		ArrayList names = new ArrayList();
		try {
		/*
			InputStream in = new BufferedInputStream(new FileInputStream(file));
			ILexer lexer = FortranProcessor.createLexerFor(in, file.getName());
			for (Token thisToken = lexer.yylex(), lastToken = null;
			     thisToken.getTerminal() != Terminal.END_OF_INPUT;
			     lastToken = thisToken, thisToken = lexer.yylex())
			{
				if (lastToken != null
						      && lastToken.getTerminal() == Terminal.T_USE
					          && thisToken.getTerminal() == Terminal.T_IDENT)
				{
					names.add(thisToken.getText());
				}
			}
		*/
		}
		catch (Exception e) {
			return new String[0];
		}
		return (String[]) names.toArray(new String[names.size()]);
	}
	
	/*
	 * Return a list of the names of all modules defined in a file
	 */
	private String[] findModuleNames(File file) {
		ArrayList names = new ArrayList();
		try {
			/*
			InputStream in = new BufferedInputStream(new FileInputStream(file));
			ILexer lexer = FortranProcessor.createLexerFor(in, file.getName());
			for (Token thisToken = lexer.yylex(), lastToken = null, tokenBeforeLast = null;
			     thisToken.getTerminal() != Terminal.END_OF_INPUT;
			     tokenBeforeLast = lastToken, lastToken = thisToken, thisToken = lexer.yylex())
			{
				if (lastToken != null
				    && lastToken.getTerminal() == Terminal.T_MODULE
				    && thisToken.getTerminal() == Terminal.T_IDENT)
				{
					if (tokenBeforeLast != null && tokenBeforeLast.getTerminal() == Terminal.T_END) {
						continue;
					}
					names.add(thisToken.getText());
				}
			}
			*/
		}
		catch (Exception e) {
			return new String[0];
		}
		return (String[]) names.toArray(new String[names.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderDependencyCalculator#findDependencies(org.eclipse.core.resources.IResource)
	 */
	public IResource[] findDependencies(IResource resource, IProject project) {
		IResource[] defDep = new IResource[1];
		ArrayList dependencies = new ArrayList();
		Collection fortranContentTypes = new FortranLanguage().getRegisteredContentTypeIds();
		
		IPath path = resource.getLocation();
		path = resource.getRawLocation();
		// add dependency on self
		defDep[0] = resource;
		dependencies.add(resource);

		File file = resource.getLocation().toFile();
		boolean isFortranFile = false;
		try {
			IContentType ct = CCorePlugin.getContentType(project, file.getCanonicalPath());
			if (ct != null) {
				isFortranFile = fortranContentTypes.contains(ct.toString());
			}
			if (!isFortranFile) return defDep;
	
			String[] usedNames = findUsedModuleNames(file);
			if (usedNames.length == 0) return defDep;
			
			IResource[] resources = project.members();	
			for (int ir = 0; ir < resources.length; ir++) {
				if (resources[ir].equals(resource)) continue;
				if (resources[ir].getType() == IResource.FILE) {
					File projectFile = resources[ir].getLocation().toFile();
					isFortranFile = false;

					ct = CCorePlugin.getContentType(project, projectFile.getCanonicalPath());
					if (ct == null) continue;
					
					isFortranFile = fortranContentTypes.contains(ct.toString());
					if (!isFortranFile) continue;
		
					String[] modules = findModuleNames(projectFile);
					for (int iu = 0; iu < usedNames.length; iu++) {
						boolean foundDependency = false;
						for (int im = 0; im < modules.length; im++) {
							if (usedNames[iu].equals(modules[im])) {
								dependencies.add(resources[ir]);
								foundDependency = true;
								break;
							}
						}
						if (foundDependency) break;
					}
				}
			}		
		}
		catch (Exception e)
		{
			return defDep;
		}
		
		return (IResource[]) dependencies.toArray(new IResource[dependencies.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderDependencyCalculator#getCalculatorType()
	 */
	public int getCalculatorType() {
		return TYPE_EXTERNAL;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.makegen.IManagedBuilderDependencyCalculator#getDependencyCommand()
	 */
	public String getDependencyCommand(IResource resource, IManagedBuildInfo info) {
		/* 
		 * The type of this IManagedDependencyGenerator is TYPE_EXTERNAL,
		 * so implement findDependencies() rather than getDependencyCommand().
		 * */
		return null;
	}

}
