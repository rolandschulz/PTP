package org.eclipse.ptp.internal.ui.search;

import java.text.MessageFormat;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;

/**
 * @author Clement
 *
 */
public class PSearchResult extends AbstractTextSearchResult {
    private PSearchQuery query;
	//private static final Match[] NO_MATCHES = new Match[0];

	public PSearchResult(PSearchQuery query){
		this.query = query;
	}		
	
	public String getText() {
		int matchCount= getMatchCount();
		String format= null;
		if (matchCount == 1)
			format = query.getSingularLabel();
		else
			format = query.getPluralLabelPattern();
		return MessageFormat.format(format, new Object[] { new Integer(matchCount) });
	}

	public String getTooltip() {
		return getText();
	}
	public ImageDescriptor getImageDescriptor() {
		return query.getImageDescriptor();
	}
	public ISearchQuery getQuery() {
		return query;
	}
	public String getLabel() {
		int matches = getMatchCount();
		String label = null;
		if (matches == 1)
			return query.getSingularLabel();
		else
			label = query.getPluralLabelPattern();

		return MessageFormat.format(label, new Object[]{new Integer(matches)});
	}
		
	public IEditorMatchAdapter getEditorMatchAdapter() {
		return null;
	}	
	public IFileMatchAdapter getFileMatchAdapter() {
		return null;
	}	
}
