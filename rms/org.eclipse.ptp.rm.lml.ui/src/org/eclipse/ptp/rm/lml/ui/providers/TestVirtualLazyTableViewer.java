package org.eclipse.ptp.rm.lml.ui.providers;

import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A simple TableViewer to demonstrate usage of an ILazyContentProvider. You can
 * compare this snippet to the Snippet029VirtualTableViewer to see the small but
 * needed difference.
 * 
 * @author Tom Schindl <tom.schindl@bestsolution.at>
 * 
 */
public class TestVirtualLazyTableViewer {
	private class MyContentProvider implements ILazyContentProvider {
		private TableViewer viewer;
		private MyModel[] elements;

		public MyContentProvider(TableViewer viewer) {
			this.viewer = viewer;
		}

		public void dispose() {

		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			this.elements = (MyModel[]) newInput;
		}

		public void updateElement(int index) {
			viewer.replace(elements[index], index);
		}

	}

	public class MyModel {
		public int counter;

		public MyModel(int counter) {
			this.counter = counter;
		}

		public String toString() {
			return "Item " + this.counter;
		}
	}

	public TestVirtualLazyTableViewer(Shell shell) {
		final TableViewer v = new TableViewer(shell, SWT.VIRTUAL);
		v.setLabelProvider(new LabelProvider());
		v.setContentProvider(new MyContentProvider(v));
		v.setUseHashlookup(true);
		MyModel[] model = createModel();
		v.setInput(model);
		v.setItemCount(model.length); // This is the difference when using a
		// ILazyContentProvider

		v.getTable().setLinesVisible(true);
	}

	private MyModel[] createModel() {
		MyModel[] elements = new MyModel[10000];

		for (int i = 0; i < 10000; i++) {
			elements[i] = new MyModel(i);
		}

		return elements;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		new TestVirtualLazyTableViewer(shell);
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();

	}

}
