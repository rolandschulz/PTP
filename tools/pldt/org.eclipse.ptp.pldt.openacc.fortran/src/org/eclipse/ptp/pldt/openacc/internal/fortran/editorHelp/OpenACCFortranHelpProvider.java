/**********************************************************************
 * Copyright (c) 2012 University of Illinois and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jeff Overbey (UIUC) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openacc.internal.fortran.editorHelp;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.help.IHelpResource;
import org.eclipse.photran.ui.IFortranAPIHelpProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Fortran API help for OpenACC directives and procedures.
 * 
 * @author Jeff Overbey
 */
public class OpenACCFortranHelpProvider implements IFortranAPIHelpProvider {
	/** Plug-in ID for the main (non-Fortran) OpenACC plug-in, which contains HTML documentation. */
	private static final String OPENACC_PLUGIN_ID = "org.eclipse.ptp.pldt.openacc"; //$NON-NLS-1$

	/** Pattern matching the start of an OpenACC directive */
	private static final Pattern OPENACC_DIRECTIVE_PREFIX_PATTERN = Pattern
			.compile("(^[Cc*]|[ \\t]*!)\\$acc([ \\t]+end)?([ \\t]+parallel)?[ \\t]*"); //$NON-NLS-1$

	/** Format string for an OpenACC directive.  %s is replaced with a directive name. */
	private static final String OPENACC_DIRECTIVE_REGEX_FORMAT =
			"(^[Cc*]|[ \\t]*!)\\$acc([ \\t]+end)?([ \\t]+parallel)?[ \\t]+%s.*"; //$NON-NLS-1$

	private final Set<String> procedures = new HashSet<String>(32);
	private final Set<String> directives = new HashSet<String>(32);

	/** Constructor */
	public OpenACCFortranHelpProvider() {
		// Section numbers from the OpenACC specification: "The OpenACC Application Programming Interface, Version 1.0"
		procedures.add("acc_get_num_devices"); // Section 3.2.1 //$NON-NLS-1$
		procedures.add("acc_set_device_type"); // 3.2.2 //$NON-NLS-1$
		procedures.add("acc_get_device_type"); // 3.2.3 //$NON-NLS-1$
		procedures.add("acc_set_device_num"); // 3.2.4 //$NON-NLS-1$
		procedures.add("acc_get_device_num"); // 3.2.5 //$NON-NLS-1$
		procedures.add("acc_async_test"); // 3.2.6 //$NON-NLS-1$
		procedures.add("acc_async_test_all"); // 3.2.7 //$NON-NLS-1$
		procedures.add("acc_async_wait"); // 3.2.8 //$NON-NLS-1$
		procedures.add("acc_async_wait_all"); // 3.2.9 //$NON-NLS-1$
		procedures.add("acc_init"); // 3.2.10 //$NON-NLS-1$
		procedures.add("acc_shutdown"); // 3.2.11 //$NON-NLS-1$
		procedures.add("acc_on_device"); // 3.2.12 //$NON-NLS-1$
		procedures.add("acc_malloc"); // 3.2.13 //$NON-NLS-1$
		procedures.add("acc_free"); // 3.2.14 //$NON-NLS-1$

		directives.add("parallel"); // 2.4.1 //$NON-NLS-1$
		directives.add("kernels"); // 2.4.2 //$NON-NLS-1$
		directives.add("data"); // 2.5 //$NON-NLS-1$
		directives.add("host_data"); // 2.6 //$NON-NLS-1$
		directives.add("loop"); // 2.5 //$NON-NLS-1$
		directives.add("cache"); // 2.9 //$NON-NLS-1$
		directives.add("declare"); // 2.11 //$NON-NLS-1$
		directives.add("update"); // 2.12.1 //$NON-NLS-1$
		directives.add("wait"); // 2.12.2 //$NON-NLS-1$
	}

	@Override
	public IHelpResource[] getHelpResources(ITextEditor fortranEditor, String apiName, String precedingText) {
		final String fname = apiName.toLowerCase();
		if (procedures.contains(fname)) {
			return getHelpResourceForFilename(fname);
		} else if (OPENACC_DIRECTIVE_PREFIX_PATTERN.matcher(precedingText).find()) {
			if (directives.contains(fname)) {
				return getHelpResourceForFilename("pragma_acc_" + fname); //$NON-NLS-1$
			} else {
				for (String directive : directives) {
					if (precedingText.matches(String.format(OPENACC_DIRECTIVE_REGEX_FORMAT, directive))) {
						return getHelpResourceForFilename("pragma_acc_" + directive); //$NON-NLS-1$
					}
				}
			}
		}
		return null;
	}

	private IHelpResource[] getHelpResourceForFilename(final String filename) {
		return new IHelpResource[] { new IHelpResource() {
			@Override
			public String getHref() {
				return String.format("/%s/html/%s.html", OPENACC_PLUGIN_ID, filename); //$NON-NLS-1$
			}

			@Override
			public String getLabel() {
				return filename;
			}
		} };
	}
}
