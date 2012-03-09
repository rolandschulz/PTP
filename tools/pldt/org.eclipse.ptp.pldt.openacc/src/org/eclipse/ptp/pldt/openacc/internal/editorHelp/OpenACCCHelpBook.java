/**********************************************************************
 * Copyright (c) 2007, 2010, 2011 IBM Corporation and University of Illinois.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jeff Overbey (Illinois) - adaptation to OpenACC
 *******************************************************************************/
package org.eclipse.ptp.pldt.openacc.internal.editorHelp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;

import org.eclipse.ptp.pldt.common.editorHelp.CHelpBookImpl;
import org.eclipse.ptp.pldt.openacc.internal.Activator;
import org.eclipse.ptp.pldt.openacc.internal.messages.Messages;

/**
 * Initializes the OpenACC help book (which provides hover help, dynamic help, and content assist for OpenACC API functions in CDT)
 * with a comprehensive list of OpenACC API functions.
 * 
 * @author Jeff Overbey
 * 
 * @see OpenACCCHelpInfoProvider
 */
public class OpenACCCHelpBook extends CHelpBookImpl {

	private static final String TITLE = Messages.OpenACCCHelpBook_Title;

	/**
	 * Constructor. Invoked exclusively from {@link OpenACCCHelpInfoProvider}.
	 */
	OpenACCCHelpBook() {
		super(Activator.getPluginId());

		func("acc_get_num_devices", // OpenACC Application Programming Interface, Version 1.0, Section 3.2.1 //$NON-NLS-1$
				Messages.OpenACCCHelpBook_Description_acc_get_num_devices, // Short description for hover help
				"int", // Return type //$NON-NLS-1$
				"acc_device_t"); // Arguments //$NON-NLS-1$
		func("acc_set_device_type", // 3.2.2 //$NON-NLS-1$
				Messages.OpenACCCHelpBook_Description_acc_set_device_type, "void", //$NON-NLS-1$
				"acc_device_t"); //$NON-NLS-1$
		func("acc_get_device_type", // 3.2.3 //$NON-NLS-1$
				Messages.OpenACCCHelpBook_Description_acc_get_device_type, "acc_device_t", //$NON-NLS-1$
				""); //$NON-NLS-1$
		func("acc_set_device_num", // 3.2.4 //$NON-NLS-1$
				Messages.OpenACCCHelpBook_Description_acc_set_device_num, "void", //$NON-NLS-1$
				"int, acc_device_t"); //$NON-NLS-1$
		func("acc_get_device_num", // 3.2.5 //$NON-NLS-1$
				Messages.OpenACCCHelpBook_Description_acc_get_device_num, "int", //$NON-NLS-1$
				"acc_device_t"); //$NON-NLS-1$
		func("acc_async_test", // 3.2.6 //$NON-NLS-1$
				Messages.OpenACCCHelpBook_Description_acc_async_test, "int", //$NON-NLS-1$
				"int"); //$NON-NLS-1$
		func("acc_async_test_all", // 3.2.7 //$NON-NLS-1$
				Messages.OpenACCCHelpBook_Description_acc_async_test_all, "int", //$NON-NLS-1$
				""); //$NON-NLS-1$
		func("acc_async_wait", // 3.2.8 //$NON-NLS-1$
				Messages.OpenACCCHelpBook_Description_acc_async_wait, "void", //$NON-NLS-1$
				"int"); //$NON-NLS-1$
		func("acc_async_wait_all", // 3.2.9 //$NON-NLS-1$
				Messages.OpenACCCHelpBook_Description_acc_async_wait_all, "void", //$NON-NLS-1$
				""); //$NON-NLS-1$
		func("acc_init", // 3.2.10 //$NON-NLS-1$
				Messages.OpenACCCHelpBook_Description_acc_init, "void", //$NON-NLS-1$
				"acc_device_type"); //$NON-NLS-1$
		func("acc_shutdown", // 3.2.11 //$NON-NLS-1$
				Messages.OpenACCCHelpBook_Description_acc_shutdown, "void", //$NON-NLS-1$
				"acc_device_t"); //$NON-NLS-1$
		func("acc_on_device", // 3.2.12 //$NON-NLS-1$
				Messages.OpenACCCHelpBook_Description_acc_on_device, "int", //$NON-NLS-1$
				"acc_device_t"); //$NON-NLS-1$
		func("acc_malloc", // 3.2.13 //$NON-NLS-1$
				Messages.OpenACCCHelpBook_Description_acc_malloc, "void*", //$NON-NLS-1$
				"size_t"); //$NON-NLS-1$
		func("acc_free", // 3.2.14 //$NON-NLS-1$
				Messages.OpenACCCHelpBook_Description_acc_free, "void", //$NON-NLS-1$
				"void*"); //$NON-NLS-1$

		setTitle(TITLE);
	}

	/**
	 * Run {@link OpenACCCHelpBook} as a command line Java application to generate the HTML files for the html/ directory.
	 */
	public static void main(String[] args) throws IOException {
		System.out.print("Enter directory to write HTML files to: "); //$NON-NLS-1$
		System.out.flush();
		final String dir = new BufferedReader(new InputStreamReader(System.in)).readLine();
		if (dir != null && !dir.equals("")) { //$NON-NLS-1$
			writeIndexHTMLFile(dir);
			writeIndividualHTMLFiles(dir);
		}
		System.out.println("All files written."); //$NON-NLS-1$
	}

	private static void writeIndexHTMLFile(final String dir) throws IOException {
		String filename = dir + File.separator + "index.html"; //$NON-NLS-1$
		System.out.println("Writing " + filename); //$NON-NLS-1$
		final Writer out = new FileWriter(filename);
		out.write("<html>\n"); //$NON-NLS-1$
		out.write("<head><title>OpenACC&trade; Application Programming Interface Version 1.0</title></head>\n"); //$NON-NLS-1$
		out.write("<body>\n"); //$NON-NLS-1$
		out.write("<h2>OpenACC&trade; Application Programming Interface<br/>\n"); //$NON-NLS-1$
		out.write("    <small><small>Version 1.0</small></small></h2>\n"); //$NON-NLS-1$
		new OpenACCCHelpBook() {
			@Override
			public void func(String fname, String desc, String retType, String args) {
				try {
					out.write(String.format("<a href=\"%s.html\">%s</a><br/>\n", fname, fname)); //$NON-NLS-1$
				} catch (final IOException e) {
					throw new Error(e);
				}
			}
		};
		out.write("</body>\n"); //$NON-NLS-1$
		out.write("</html>\n"); //$NON-NLS-1$
		out.close();
	}

	private static void writeIndividualHTMLFiles(final String dir) {
		new OpenACCCHelpBook() {
			@Override
			public void func(String fname, String desc, String retType, String args) {
				final String filename = dir + File.separator + fname + ".html"; //$NON-NLS-1$
				System.out.println("Writing " + filename); //$NON-NLS-1$
				try {
					final Writer out = new FileWriter(filename);
					out.write("<html>\n"); //$NON-NLS-1$
					out.write(String.format("<head><title>%s</title></head>\n", fname)); //$NON-NLS-1$
					out.write("<body>\n"); //$NON-NLS-1$
					out.write(String.format("<h2>%s</h2>\n", fname)); //$NON-NLS-1$
					out.write(String
							.format("<p style=\"line-height: 150%%;\"><tt>%s <b>%s</b>(%s)</tt></p>\n", colorize(retType), fname, colorize(args))); //$NON-NLS-1$
					out.write(String.format("<dl><dd>%s</dd></dl>\n", insertTrademarks(formatConstantsAndFunctionNames(desc)))); //$NON-NLS-1$
					out.write("<br/>\n"); //$NON-NLS-1$
					out.write("<table width=\"100%\" border=\"0\"><tr>\n"); //$NON-NLS-1$
					out.write("<td align=\"left\"><font size=\"2\"><a href=\"index.html\">API Index</a></font></td>\n"); //$NON-NLS-1$
					out.write("<td align=\"right\"><font color=\"#C0C0C0\" size=\"1\">OpenACC&trade; Application Programming Interface Version 1.0</font></td>\n"); //$NON-NLS-1$
					out.write("</tr></table>\n"); //$NON-NLS-1$
					out.write("</body>\n"); //$NON-NLS-1$
					out.write("</html>\n"); //$NON-NLS-1$
					out.close();
				} catch (final IOException e) {
					throw new Error(e);
				}
			}

			private String colorize(String args) {
				return args.replace("void", "<b><font color=\"#931a68\">void</font></b>") //$NON-NLS-1$ //$NON-NLS-2$
						.replace("int", "<b><font color=\"#931a68\">int</font></b>"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			private String formatConstantsAndFunctionNames(String desc) {
				return desc.replaceAll("(acc_[a-z_]+\\(?\\)?)", "<tt>$1</tt>"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			private String insertTrademarks(String description) {
				return description.replace("OpenACC", "OpenACC&trade;"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		};
	}
}
