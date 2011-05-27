package ch.epfl.bbcf.gdv.utility;

public class Filter {

	private String[] patterns;
	private boolean matches;
	private boolean noFilter;
	
	public Filter(String filter){
		if(filter==null){
			noFilter = true;
			matches = true;
		}
		else {
			noFilter = false;
			patterns = filter.toLowerCase().split("\\s");
			matches = false;
		}
		
	}

	public boolean test(String str){
		if(!noFilter){
			matches = false;
			for(String pattern : patterns){
				if(str.toLowerCase().contains(pattern)){
					matches = true;
				}
			}
		}
		return matches;
		
	}
	/**
	 * @param matches the matches to set
	 */
	public void setMatches(boolean matches) {
		this.matches = matches;
	}

	/**
	 * @return the matches
	 */
	public boolean matches() {
		return matches;
	}

	/**
	 * @param patterns the patterns to set
	 */
	public void setPatterns(String[] patterns) {
		this.patterns = patterns;
	}

	/**
	 * @return the patterns
	 */
	public String[] getPatterns() {
		return patterns;
	}
}
