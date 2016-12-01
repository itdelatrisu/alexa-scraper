package itdelatrisu.alexascraper;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main {
	/** Visits a category. */
	private static void visit(String category, File dir) throws IOException, InterruptedException {
		final String URL = "http://www.alexa.com/topsites/category;%d/" + category;
		final int PAGE_LIMIT = 20;
		final int SLEEP_MS = 10;

		// write results to file
		File file = new File(dir, String.format("%s-500.csv", category.toLowerCase()));
		PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8.name());

		// visit all subpages
		for (int i = 0; i < PAGE_LIMIT; i++) {
			visit(String.format(URL, i), writer);
			Thread.sleep(SLEEP_MS);
		}
		writer.close();
	}

	/** Visits a page of a category. */
	private static void visit(String url, PrintWriter writer) throws IOException {
		// grab page HTML
		Document dom = Jsoup.connect(url).get();
		Elements list = dom.getElementsByClass("site-listing");
		for (Element e : list) {
			// parse elements
			Document div = Jsoup.parseBodyFragment(e.html());
			String count = div.getElementsByClass("count").first().text();
			String site = div.getElementsByTag("a").first().text().toLowerCase();

			// fix malformed sites
			if (site.startsWith("https://")) {
				site = site.substring("https://".length());
				if (site.startsWith("www."))
					site = site.substring("www.".length());
				if (site.matches("^.*\\.[a-z]+/$"))
					site = site.substring(0, site.length() - 1);
			}

			// write results
			writer.print(count);
			writer.print(',');
			writer.print(site);
			writer.println();
		}
	}

	/** Runs the scraper. */
	public static void main(String[] args) throws Exception {
		// create output directory
		File outputDir = new File("data");
		if (!outputDir.isDirectory() && !outputDir.mkdirs()) {
			System.err.printf("Could not create output directory '%s'.", outputDir.getName());
			System.exit(1);
		}

		// scrape categories
		String[] categories = {"Shopping", "News"};
		for (String category : categories) {
			System.out.printf("Scraping category: %s\n", category);
			visit(category, outputDir);
		}
		System.out.println("Done.");
	}
}
