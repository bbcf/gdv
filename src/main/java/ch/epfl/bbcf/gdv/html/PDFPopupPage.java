package ch.epfl.bbcf.gdv.html;

import org.apache.wicket.markup.html.WebPage;

import ch.epfl.bbcf.gdv.config.Configuration;
import ch.epfl.bbcf.gdv.html.utility.DocumentInlineFrame;
import ch.epfl.bbcf.gdv.html.utility.PDFRessource;

public class PDFPopupPage extends WebPage{
	public PDFPopupPage(){
		setRenderBodyOnly(true);
		add(new DocumentInlineFrame("_pdf", new PDFRessource(Configuration.getGdvWorkingDir()+"/pdf/"+"test.pdf")));
	}
}