package org.openaudible.feeds.rss;

import org.openaudible.Audible;
import org.openaudible.audible.AudibleUtils;
import org.openaudible.books.Book;
import org.semper.reformanda.syndication.rss.Enclosure;
import org.semper.reformanda.syndication.rss.Item;
import org.semper.reformanda.syndication.rss.MimeType;
import org.semper.reformanda.syndication.rss.itunes.ItunesImage;
import org.semper.reformanda.syndication.rss.itunes.YesNo;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

public class BookToRSSItem {

	public static Item create(Audible a, File mp3File, Book b) throws MalformedURLException, URISyntaxException {
		Date d = null;

		String dateString = b.getRelease_date();
		d = AudibleUtils.parseDate(dateString);

		URL u1 = new URL("http://www.theTestPodcast.com/epidosdes/1");
		URI mp3URL = new URI("");

		final Item item = new Item()
				.setGuid(u1)
				.setTitle(b.getFullTitle())
				.setDescription(b.getDescription())
				.setAuthor(b.getAuthor())
				// .setSubtitle(b.set"The One That Made You Wish You Never Liked Start Wars in the First Place")
				.setSummary(b.getSummary())
				.setDuration(b.getDuration())
				.setIsClosedCaptioned(YesNo.NO)
				.setEnclosure(new Enclosure()
						.setLength(mp3File.length())
						.setType(MimeType.AUDIO_MPEG_MPG)
						.setUrl(mp3URL));

		if (d != null)
			item.setPubDate(d);

		final ItunesImage itemImage = new ItunesImage().setHref("http://www.theTestPodcast.com/images/episode1.png");
		item.setImage(itemImage);

		return item;
	}

}
