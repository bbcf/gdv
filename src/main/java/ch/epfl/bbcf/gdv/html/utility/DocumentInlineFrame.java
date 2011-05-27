package ch.epfl.bbcf.gdv.html.utility;

import org.apache.wicket.IResourceListener;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.util.string.Strings;

import ch.epfl.bbcf.gdv.config.Application;

/**
 * Implementation of an <a href="http://www.w3.org/TR/REC-html40/present/frames.html#h-16.5">inline
 * frame</a> component. Must be used with an iframe (&lt;iframe src...) element. The src attribute
 * will be generated. Its is suitable for displaying <em>generated contend</em> like PDF, EXCEL, WORD, 
 * etc.
 * 
 * @author Ernesto Reinaldo Barreiro
 * 
 */

public class DocumentInlineFrame extends WebMarkupContainer implements IResourceListener 
{
	private static final long serialVersionUID = 1L;
	
	
	private IResourceListener resourceListener;

	/**
	 * Constructor receiving an IResourceListener..
	 * 
	 * @param id
	 * @param resourceListener
	 */
	public DocumentInlineFrame(final String id, IResourceListener resourceListener){
		super(id);
		this.resourceListener = resourceListener;
	}

	/**
	 * Gets the url to use for this link.
	 * 
	 * @return The URL that this link links to
	 */
	protected CharSequence getURL(){
		return urlFor(IResourceListener.INTERFACE);
	}

	/**
	 * Handles this frame's tag.
	 * 
	 * @param tag
	 *            the component tag
	 * @see org.apache.wicket.Component#onComponentTag(ComponentTag)
	 */
	@Override
	protected final void onComponentTag(final ComponentTag tag){
		checkComponentTag(tag, "iframe");
		// Set href to link to this frame's frameRequested method
		CharSequence url = getURL();
		// generate the src attribute
		tag.put("src", Strings.replaceAll(url, "&", "&amp;"));
		super.onComponentTag(tag);
	}

	@Override
	protected boolean getStatelessHint(){	
		return false;
	}
	
	public void onResourceRequested() {
		this.resourceListener.onResourceRequested();
	}
}