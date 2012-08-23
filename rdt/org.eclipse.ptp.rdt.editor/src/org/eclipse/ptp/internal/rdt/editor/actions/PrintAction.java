/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.rdt.editor.actions;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.JFaceTextUtil;
import org.eclipse.ptp.internal.rdt.editor.RemoteCEditor;
import org.eclipse.ptp.internal.rdt.editor.preferences.HeaderFooterProposalProvider;
import org.eclipse.ptp.internal.rdt.editor.preferences.PrintPreferencePage;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextPrintOptions;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Display;

/**
 * This class handles printing for the editor (overrides default TextEditor printing to provide
 * headers, footers and the ability to print in a different font than that being used for display.
 * @author batthish
 *
 */
public class PrintAction extends Action {

	private RemoteCEditor _editor;

	/**
	 * The print action to run
	 * @param print			the original editor printing action
	 * @param remoteCEditor	the editor
	 */
	public PrintAction(IAction print, RemoteCEditor remoteCEditor) {
		super(print.getText(), print.getImageDescriptor());
		setActionDefinitionId(print.getActionDefinitionId());
		_editor = remoteCEditor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		StyledText widget = _editor.getViewer().getTextWidget();
		Printer printer = getPrinter();
		if (printer != null) {
			Font oldFont = widget.getFont();
			Font newFont = PrintPreferencePage.getSavedFont();
			
			// make sure that the editor does not change during the print process.
			widget.setRedraw(false);
			widget.setFont(newFont);

			// styled text only gives a left margin and top margin, the following calculates the number of characters
			// to use for a right margin of 1 inch and a bottom margin of 1 inch
			Point screenDPI = widget.getDisplay().getDPI();
			GC gc = new GC(printer);
			gc.setFont(widget.getFont());
			int charWidth = Math.max(gc.getFontMetrics().getAverageCharWidth(), gc.getCharWidth('W')); 	// W is usually the widest char....
			Point padding = new Point(screenDPI.x/charWidth, screenDPI.y/gc.getFontMetrics().getHeight());
			gc.dispose();
			
			// do the printing
			Runnable r = widget.print(printer, getStyleOptions(padding));
			Display display = Display.getCurrent();
			display.syncExec(r);
			
			//cleanup
			widget.setFont(oldFont);
			widget.setRedraw(true);
			widget.redraw();
			printer.dispose();
			newFont.dispose();
		}
	}

	/**
	 * Retrieves the printer object to print with
	 * @return the printer, or null if cancel is pressed in the print dialog 
	 */
	private Printer getPrinter() {
		Display display = Display.getCurrent();
		if (display != null) {
			PrintDialog dialog = new PrintDialog(display.getActiveShell());
			PrinterData data = dialog.open();
			if (data != null) {
				return new Printer(data);
			}
		}
		return null;

	}

	/**
	 * Retrieves the printing options from the preference page
	 * @param padding	the padding
	 * @return the StyledTextPrintOptions needed for printing
	 */
	private StyledTextPrintOptions getStyleOptions(Point padding) {
		StyledTextPrintOptions options = new StyledTextPrintOptions();

		// left margin
		StringBuilder pad  = new StringBuilder();
		for (int i=0; i<padding.x; i++) 
			pad.append(' ');

		options.footer = doSubstitutions(PrintPreferencePage.getFooter())+pad;
		options.header = doSubstitutions(PrintPreferencePage.getHeader())+pad;
		
		// bottom margin - does not work, so commented out
		//pad.delete(0, pad.length());
		//for (int i=0; i<padding.y; i++)
		//	pad.append('\n');
		//options.footer+=pad;
		
		options.jobName = _editor.getTitle();
		options.printTextFontStyle = true;
		options.printTextForeground = true;
		options.printTextBackground = true;

		options.printLineNumbers = PrintPreferencePage.getPrintLineNumbers();
		if (options.printLineNumbers) {
			options.lineLabels = new String[_editor.getViewer().getTextWidget().getLineCount()];
			for (int i = 0; i < options.lineLabels.length; i++)
				options.lineLabels[i] = String.valueOf(JFaceTextUtil.widgetLine2ModelLine(_editor.getViewer(), i) + 1);
		}
		return options;
	}

	/**
	 * Handles the necessary substitutions needed for the header and footer
	 * @param text	the header or footer text
	 * @return	the text with the appropriate variable values substituted
	 */
	private String doSubstitutions(String text) {
		//handle date
		text = text.replace(HeaderFooterProposalProvider.DATE, DateFormat.getDateInstance(DateFormat.LONG).format(new Date()));
		//handle time
		text = text.replace(HeaderFooterProposalProvider.TIME, DateFormat.getTimeInstance(DateFormat.LONG).format(new Date()));
		//handle file name
		text = text.replace(HeaderFooterProposalProvider.FILE, _editor.getTitle());
		return text;
	}

}
