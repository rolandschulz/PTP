package org.eclipse.fdt.internal.ui.fview;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.ui.views.framelist.TreeFrame;
import org.eclipse.ui.views.framelist.TreeViewerFrameSource;

public class FortranViewFrameSource extends TreeViewerFrameSource {
        private FortranView cview;

        protected TreeFrame createFrame(Object input) {
                TreeFrame frame = super.createFrame(input);
                frame.setToolTipText(cview.getToolTipText(input));
                return frame;
        }
        /**
         * Also updates the title of the packages explorer
         */
        protected void frameChanged(TreeFrame frame) {
                super.frameChanged(frame);
                cview.updateTitle();
        }
        public FortranViewFrameSource(FortranView cview) {
                super(cview.getViewer());
                this.cview = cview;
        }
}

