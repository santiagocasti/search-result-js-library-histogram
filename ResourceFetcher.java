package jsLibHistogram;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;

public class ResourceFetcher {

	public HashSet<UrlCount> getJsLibs(UrlCount url){

		UrlMatcher matcher = new UrlMatcher(UrlMatcher.ONLY_JS);
        try{
        	String s;
            BufferedReader reader = this.getBufferedReader(url.url);

            while ((s = reader.readLine()) != null) {
				matcher.matchLine(s);
            }
			reader.close();
			Printer.print(".");
        } catch (Exception e){
            Printer.print("x");
        }

        return matcher.getMatches();
	}

	protected BufferedReader getBufferedReader(String urlString) throws IOException {
		URL url = new URL(urlString);
		InputStream ins;
		// support https and http URLs
		if (urlString.startsWith("https")){
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
			ins = con.getInputStream();
		} else {
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			ins = con.getInputStream();
		}

		InputStreamReader isr = new InputStreamReader(ins);
		return new BufferedReader(isr);
	}

}
