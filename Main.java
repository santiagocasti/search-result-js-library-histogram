package jsLibHistogram;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Main {

    public static void main(String[] args) {

        // load file with page source after searching "scalablecapital" on Google
        // feel free to open the file with a browser to see how it looks
        // can replace the file with any other just search Google, view source HTMP and copy to a file
        Path path = FileSystems.getDefault().getPath("./src/jsLibHistogram/exampleResult.html");
        UrlMatcher matcher = new UrlMatcher(UrlMatcher.NO_GOOGLE_NO_IMG);
        try {
            Files.lines(path).parallel().forEach(matcher::matchLine);
        } catch (Exception e){
            Printer.println("#ERROR# Couldn't read file: "+e.getMessage());
            Printer.println("#-----# Path tried: "+path.toString());
            return;
        }

        if (matcher.getMatches().isEmpty()) {
            Printer.println("#ERROR# Didn't find any valid URL in the source file.");
            return;
        }

        // fetch results for the URLs matched initially
        ArrayList<UrlCount> urlCounts = fetchResults(matcher.getMatches());

        // get the top 8 javascript libraries
        UrlCount result[] = getTopKResults(urlCounts, 5);

        // print the results
        printResults(result);
    }

    protected static void printResults(UrlCount[] result) {
        Printer.println("-- Results --");
        Printer.println("# Occur.\tJS File - (full URL)");
        String jsLibrary;
        for (int i = result.length-1; i>=0; --i) {
            jsLibrary = result[i].url.substring(result[i].url.lastIndexOf("/")+1);
            Printer.println(result[i].count + "\t\t\t" + jsLibrary + " - ("+result[i].url+")");
        }
        Printer.println("-------------");
    }

    protected static UrlCount[] getTopKResults(ArrayList<UrlCount> urlCounts, int k) {
        Printer.print("Processing results: ");
        long start = System.nanoTime();

        // put the URL counts in a hash map to map URLs to counts and get total count per URL
        // complexities: time O(n), space O(n)
        HashMap<String, UrlCount> map = new HashMap<String, UrlCount>();
        UrlCount value;
        for (UrlCount url : urlCounts) {

            if (map.containsKey(url.url)) {
                value = map.get(url.url);
                url.count += value.count;
            }

            map.put(url.url, url);
        }

        // create a min heap to get top N results
        PriorityQueue<UrlCount> minHeap = new PriorityQueue<UrlCount>(new Comparator<UrlCount>(){
            public int compare(UrlCount first, UrlCount second) {
                return first.count - second.count;
            }
        });

        // add values to the heap and remove root when size > k
        // complexities: time O(n), space O(k)
        for (UrlCount count : map.values()) {
            minHeap.add(count);
            if (minHeap.size() > k) {
                minHeap.poll();
            }
        }

        // turn min heap into array and return it
        // complexities: time O(k), space O(k)
        UrlCount result[] = new UrlCount[k];
        int i = 0;
        while (!minHeap.isEmpty()) {
            result[i++] = minHeap.poll();
        }

        long end = System.nanoTime();
        Printer.println(String.format("%f sec",nanoToSec(end-start)));

        return result;
    }

    protected static ArrayList<UrlCount> fetchResults(HashSet<UrlCount> urls) {
        Printer.println("Fetching results: ('.' = success, 'x' = failure)");
        long start = System.nanoTime();

        ResourceFetcher rf = new ResourceFetcher();
        ArrayList<UrlCount> resultList = urls
                .stream() // Stream processing from Java 8 :D
                .parallel() // parallel execution of the HTTP requests
                .map(rf::getJsLibs) // for each match, retrieve results and match .js URLS in results
                .collect(ArrayList::new, List::addAll, List::addAll); // combine all of them in an ArrayList

        long end = System.nanoTime();
        Printer.println(String.format(" %f sec",nanoToSec(end-start)));
        return resultList;
    }

    protected static float nanoToSec(long ns) {
        return (float)ns / 1000000000;
    }

}
