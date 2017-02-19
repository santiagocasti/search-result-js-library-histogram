package jsLibHistogram;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlMatcher {

	// match only .js terminated URLs
	public static int ONLY_JS = 1;
	// match URLs without .jpg, .png, or any google/gstatic in URL
	public static int NO_GOOGLE_NO_IMG = 2;

	protected HashSet<UrlCount> matches;
	protected boolean includeImages = true;
	protected boolean includeGoogle = true;
	protected boolean onlyJs;

	// Cache patterns, take time to compile
	protected Pattern jsPattern;
	protected Pattern gPattern;
	protected Pattern imgPattern;
	protected Pattern urlPattern;

	public UrlMatcher(int mode) {

		if (mode == ONLY_JS) {
			onlyJs = true;
			includeGoogle = false;
			includeImages = false;
		} else if (mode == NO_GOOGLE_NO_IMG) {
			includeGoogle = false;
			includeImages = false;
		}

		this.matches = new HashSet<UrlCount>();
	}

	public HashSet<UrlCount> getMatches() {
		return this.matches;
	}

	public void matchLine(String s) {
		if (urlPattern == null) {
			// this can be improved by using something like Apache Commons UrlValidator
			urlPattern = Pattern.compile("(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", Pattern.DOTALL);
		}
		Matcher matcher = urlPattern.matcher(s);
		String matchedString;

		while (matcher.find()) {
			matchedString = matcher.group();
			if (this.shouldInclude(matchedString)) {
				matches.add(new UrlCount(matchedString, 1));
			}
		}
	}

	protected boolean shouldInclude(String match) {
		if (this.onlyJs) {
			return this.matchesJs(match);
		}

		if ((this.includeImages || !this.matchesImg(match)) &&
			(this.includeGoogle || !this.matchesGoogle(match))) {

			if (!this.matchesDot(match)) {
				// validate that URLs have at least one '.' character
				// this was because of the special case of Google
				// having strings like this in the HTML code: https://de.<b>scalable.capital</b>/presse
				// that would match 'https://de' which will cause a timeout when fetching
				return false;
			}

			return true;
		}


		return false;
	}

	protected boolean matchesJs(String s) {
		if (jsPattern == null) {
			jsPattern = Pattern.compile("(.js)$", Pattern.DOTALL);
		}
		Matcher jsMatcher = jsPattern.matcher(s);
		return jsMatcher.find();
	}

	protected boolean matchesGoogle(String s) {
		if (gPattern == null) {
			gPattern = Pattern.compile("(google|gstatic)", Pattern.DOTALL);
		}
		Matcher gMatcher = gPattern.matcher(s);
		return gMatcher.find();
	}

	protected boolean matchesImg(String s) {
		if (imgPattern == null) {
			imgPattern = Pattern.compile("(.jpg|.png)$");
		}
		Matcher imgMatcher = imgPattern.matcher(s);
		return imgMatcher.find();
	}

	protected boolean matchesDot(String s) {
		if (jsPattern == null) {
			jsPattern = Pattern.compile("([.])", Pattern.DOTALL);
		}
		Matcher jsMatcher = jsPattern.matcher(s);
		return jsMatcher.find();
	}

}
