/*******************************************************************************
 * Copyright (c) 2000, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Sergey Prigogin, Google
 *     Anton Leherbauer (Wind River Systems)
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.ui.editor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.formatter.CodeFormatter;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterOptions;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.formatter.AbortFormatting;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.ptp.internal.rdt.core.formatter.RemoteDefaultCodeFormatterOptions;
import org.eclipse.ptp.rdt.core.services.IRDTServiceConstants;
import org.eclipse.ptp.rdt.ui.UIPlugin;
import org.eclipse.ptp.rdt.ui.serviceproviders.IIndexServiceProvider2;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.text.edits.TextEdit;

/**
 * @author Vivian Kong
 * see CCodeFormatter
 */
public class RemoteCCodeFormatter extends CodeFormatter {
	private RemoteDefaultCodeFormatterOptions preferences;
	private Map<String, ?> options;

	public RemoteCCodeFormatter() {
		this(DefaultCodeFormatterOptions.getDefaultSettings());
	}

	public RemoteCCodeFormatter(DefaultCodeFormatterOptions preferences) {
		this(preferences, null);
	}

	public RemoteCCodeFormatter(DefaultCodeFormatterOptions defaultCodeFormatterOptions, Map<String, ?> options) {
		setOptions(options);
		if (defaultCodeFormatterOptions != null) {
			preferences.set(defaultCodeFormatterOptions.getMap());
		}
	}

	public RemoteCCodeFormatter(Map<String, ?> options) {
		this(null, options);
	}
	
	@Override
	public String createIndentationString(final int indentationLevel) {
		if (indentationLevel < 0) {
			throw new IllegalArgumentException();
		}

		int tabs= 0;
		int spaces= 0;
		switch (preferences.tab_char) {
		case DefaultCodeFormatterOptions.SPACE:
			spaces= indentationLevel * preferences.tab_size;
			break;

		case DefaultCodeFormatterOptions.TAB:
			tabs= indentationLevel;
			break;

		case DefaultCodeFormatterOptions.MIXED:
			int tabSize= preferences.tab_size;
			int spaceEquivalents= indentationLevel * preferences.indentation_size;
			tabs= spaceEquivalents / tabSize;
			spaces= spaceEquivalents % tabSize;
			break;

		default:
			return EMPTY_STRING;
		}

		if (tabs == 0 && spaces == 0) {
			return EMPTY_STRING;
		}
		StringBuffer buffer= new StringBuffer(tabs + spaces);
		for (int i= 0; i < tabs; i++) {
			buffer.append('\t');
		}
		for (int i= 0; i < spaces; i++) {
			buffer.append(' ');
		}
		return buffer.toString();
	}

	@Override
	public void setOptions(Map<String, ?> options) {
		if (options != null) {
			this.options= options;
			Map<String, String> formatterPrefs= new HashMap<String, String>(options.size());
			for (String key : options.keySet()) {
				Object value= options.get(key);
				if (value instanceof String) {
					formatterPrefs.put(key, (String) value);
				}
			}
			preferences= new RemoteDefaultCodeFormatterOptions(formatterPrefs);
		} else {
			this.options= CCorePlugin.getOptions();
			preferences= RemoteDefaultCodeFormatterOptions.getDefaultSettings();
		}
	}
	
	@Override
	public TextEdit format(int kind, String source, int offset, int length, int indentationLevel, String lineSeparator) {
		preferences.initial_indentation_level = indentationLevel;
		return format(kind, source, new IRegion[] { new Region(offset, length) }, lineSeparator)[0];
	}

//	/*
//	 * @see org.eclipse.cdt.core.formatter.CodeFormatter#format(int, java.lang.String, int, int, int, java.lang.String)
//	 */
//	@Override
//	public TextEdit format_old(int kind, String source, int offset, int length, int indentationLevel, String lineSeparator) {
//		TextEdit edit= null;
//		ITranslationUnit tu= (ITranslationUnit)options.get(DefaultCodeFormatterConstants.FORMATTER_TRANSLATION_UNIT);
//		if (tu == null) {
//			IFile file= (IFile)options.get(DefaultCodeFormatterConstants.FORMATTER_CURRENT_FILE);
//			if (file != null) {
//				tu= (ITranslationUnit)CoreModel.getDefault().create(file);
//			}
//		}
//		if (lineSeparator != null) {
//			this.preferences.line_separator = lineSeparator;
//		} else {
//			//for remote projects
//			this.preferences.line_separator = "\n"; //$NON-NLS-1$
//		}
//		this.preferences.initial_indentation_level = indentationLevel;
//		
//		
//		
//		if (tu != null) {
//			IRemoteCodeFormattingService codeFormattingService = getCodeFormattingService(tu.getCProject().getProject());
//			try {
//				return codeFormattingService.computeCodeFormatting(tu, source, this.preferences, offset, length, new NullProgressMonitor());
//			} catch (CoreException e) {
//				UIPlugin.log(e);
//			}
//		}
//		return edit;
//	}
	
	@Override
	public TextEdit[] format(int kind, String source, IRegion[] regions, String lineSeparator) {
		TextEdit[] edits= new TextEdit[regions.length];
		if (lineSeparator != null) {
			preferences.line_separator = lineSeparator;
		} else {
			preferences.line_separator = System.getProperty("line.separator"); //$NON-NLS-1$
		}

		ITranslationUnit tu = getTranslationUnit(source);
		
		if (tu != null) {
			IRemoteCodeFormattingService codeFormattingService = getCodeFormattingService(tu.getCProject().getProject());
			try {
				for (int i = 0; i < regions.length; i++) {
					IRegion region = regions[i];
					edits[i] = codeFormattingService.computeCodeFormatting(tu, source, this.preferences, region.getOffset(), region.getLength(), new NullProgressMonitor());
				}
			} catch (CoreException e) {
				UIPlugin.log(e);
			}
		}
		return edits;
	}
	

	
	private ITranslationUnit getTranslationUnit(String source) {
		ITranslationUnit tu= (ITranslationUnit) options.get(DefaultCodeFormatterConstants.FORMATTER_TRANSLATION_UNIT);
		if (tu == null) {
			IFile file= (IFile) options.get(DefaultCodeFormatterConstants.FORMATTER_CURRENT_FILE);
			if (file != null) {
				tu= (ITranslationUnit) CoreModel.getDefault().create(file);
			}
		}
		if (tu != null && source != null) {
			try {
				// Create a private working copy and set it contents to source.
				if (tu.isWorkingCopy())
					tu = ((IWorkingCopy) tu).getOriginalElement();
				tu = tu.getWorkingCopy();
				tu.getBuffer().setContents(source);
			} catch (CModelException e) {
				throw new AbortFormatting(e);
			}
		}
		return tu;
	}
	
	private IRemoteCodeFormattingService getCodeFormattingService(IProject project) {
		IServiceModelManager smm = ServiceModelManager.getInstance();
		IServiceConfiguration serviceConfig = smm.getActiveConfiguration(project);
		IService indexingService = smm.getService(IRDTServiceConstants.SERVICE_C_INDEX);
		IServiceProvider serviceProvider = serviceConfig.getServiceProvider(indexingService);
		if (!(serviceProvider instanceof IIndexServiceProvider2)) {
			return null;
		}
		IRemoteCodeFormattingService service = ((IIndexServiceProvider2) serviceProvider).getRemoteCodeFormattingService();
		return service;
	}
}
