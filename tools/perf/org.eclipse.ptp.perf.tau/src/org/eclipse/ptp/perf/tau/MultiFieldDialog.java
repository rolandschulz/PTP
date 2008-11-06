package org.eclipse.ptp.perf.tau;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class MultiFieldDialog extends Dialog {

	private String[] queries;
	private String[] values;
	private Text[] texts;
	
	MultiFieldDialog(Shell parent) {
		super(parent);
	}
	
	public MultiFieldDialog(Shell parent, String[] queries){
		super(parent);
		this.queries=queries;
	}
	
	protected Control createDialogArea(Composite parent){
		
		Composite comp = (Composite) super.createDialogArea(parent);

		GridLayout layout=(GridLayout)comp.getLayout();
		layout.numColumns=2;
		
		Label label;
		texts=new Text[queries.length];
		for(int i=0;i<queries.length;i++){
			label=new Label(comp,SWT.NULL);
			label.setText(queries[i]);
			texts[i]=new Text(comp,SWT.SINGLE|SWT.BORDER);
		}
		return comp;
	}
	
	protected void buttonPressed(int buttonId){
		if(buttonId==Window.OK){
			values=new String[queries.length];
			for(int i=0;i<queries.length;i++){
				values[i]=texts[i].getText();
			}
		}
		super.buttonPressed(buttonId);
	}
	
	public String[] getValues(){
		return values;
	}

}
