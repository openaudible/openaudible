package org.openaudible.convert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp4.Mp4FileReader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.mp4.Mp4FieldKey;
import org.jaudiotagger.tag.mp4.Mp4Tag;
import org.jaudiotagger.tag.mp4.Mp4TagField;
import org.jaudiotagger.tag.mp4.field.Mp4TagCoverField;
import org.openaudible.Audible;
import org.openaudible.books.Book;
import org.openaudible.books.BookElement;
import org.openaudible.util.SimpleProcess;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public enum AAXParser {
	instance;    // Singleton
	private static final Log LOG = LogFactory.getLog(AAXParser.class);
	int parseCount = 0, aaxBigger = 0, aaxSmaller = 0, aaxSame = 0;
	
	public static void main(String s[]) {
		
		try {
			File aaxFile = new File("D:\\Audible\\AAX\\BK_HARP_003839.AAX");
			File imageFile = new File("D:\\Audible\\test\\BK_HARP_003839.JPG");
			
			Book b = instance.parseAAX(aaxFile, imageFile, CoverImageAction.doNothing);
			
			Mp4FileReader reader = new Mp4FileReader();
			
			AudioFile audiofile = reader.read(aaxFile);
			Mp4Tag tag = (Mp4Tag) audiofile.getTag();
			System.err.println(audiofile + " tag=" + tag);
			
			ArrayList<String> idList = new ArrayList<>();
			
			Iterator<TagField> fields = tag.getFields();
			System.err.println(fields instanceof Iterator);
			while (fields.hasNext()) {
				TagField f = fields.next();
				idList.add(f.getId());
				
			}
			
			HashSet<FieldKey> generic = new HashSet<>();
			HashSet<Mp4FieldKey> mp4 = new HashSet<>();
			
			for (FieldKey fk : FieldKey.values()) {
				Mp4TagField o = tag.getFirstField(fk);
				if (o != null) {
					generic.add(fk);
				}
			}
			
			for (Mp4FieldKey fk : Mp4FieldKey.values()) {
				Mp4TagField o = tag.getFirstField(fk);
				if (o != null) {
					mp4.add(fk);
				}
			}
			
			System.err.println("generic Keys:" + generic.size() + " total: " + tag.getFieldCount());
			
			for (FieldKey fk : generic) {
				System.err.println("FieldKey." + fk.name() + ",");
			}
			
			System.err.println("mp4 Keys:" + mp4.size() + " total: " + tag.getFieldCount());
			
			for (Mp4FieldKey fk : mp4) {
				System.err.println("Mp4FieldKey." + fk.name() + ",");
			}
			
			System.err.println("ids...");
			
			for (String id : idList) {
				Mp4FieldKey fk = findMp4FieldKey(id);
				System.err.println("id=" + id + ", fk=" + fk + ", value=" + tag.getFirst(id));
			}
			
		} catch (Throwable th) {
			th.printStackTrace();
		}
		
	}
	
	private static Mp4FieldKey findMp4FieldKey(String id) {
		for (Mp4FieldKey fk : Mp4FieldKey.values()) {
			if (fk.getFieldName().equals(id))
				return fk;
		}
		return null;
	}
	
	public Book parseAAX(File aaxFile, File imageDest, CoverImageAction imageAction)
			throws IOException, CannotReadException, TagException, ReadOnlyFileException, InvalidAudioFrameException, InterruptedException {
		
		Book b = new Book();
		parseCount++;
		
		Mp4FileReader reader = new Mp4FileReader();
		AudioFile audiofile = reader.read(aaxFile);
		Mp4Tag tag = (Mp4Tag) audiofile.getTag();
		tagsToBook(tag, b);
		
		ffmpeg(b, aaxFile);
		
		if (imageDest == null && Audible.instance != null) {
			// hack.
			imageDest = Audible.instance.getImageFileDest(b);
			
		}
		
		if (imageDest != null) {
			
			
			Mp4TagCoverField tt = (Mp4TagCoverField) tag.getFirstField("covr");
			if (tt != null) {
				boolean writeImageToDisk = false;
				long aaxImageSize = tt.getDataSize();
				long existImageSize = imageDest.length();
				if (aaxImageSize > existImageSize)
					aaxBigger++;
				else if (aaxImageSize < existImageSize)
					aaxSmaller++;
				else
					aaxSame++;
				
				switch (imageAction) {
					case doNothing:
						break;
					case replaceImage:
						writeImageToDisk = true;
						break;
					case useBiggerImage:
						if (aaxImageSize > existImageSize) {
							writeImageToDisk = true;
						}
						
						break;
					case saveInDirectory:
						if (imageDest == null || !imageDest.isDirectory())
							throw new IOException("saveInDirectory doesn't have directory");
						
						imageDest = new File(imageDest, b.getProduct_id() + ".jpg");
						if (!imageDest.exists())
							writeImageToDisk = true;
						
						
						break;
					default:
						assert (false);
						break;
					
				}
				
				if (writeImageToDisk)
					Files.write(Paths.get(imageDest.getAbsolutePath()), tt.getData());
			}
		}
		
		return b;
	}
	
	public ArrayList<Chapter> ffmpeg(Book b, File f) throws IOException, InterruptedException {
		
		if (f == null)
			f = Audible.instance.getAAXFileDest(b);
		
		ArrayList<String> args = new ArrayList<>();
		args.add(FFMPEG.getExecutable());
		args.add("-i");
		args.add(f.getAbsolutePath());
		args.add("-f");
		args.add("ffmetadata");
		args.add("-");
		
		SimpleProcess ffmpeg = new SimpleProcess(args);
		SimpleProcess.Results results = ffmpeg.getResults();
		if (LOG.isTraceEnabled()) {
			System.err.println(results.getErrorString());
			System.err.println(results.getOutputString());
		}
		
		long millis = parseDurationToMS(results.getErrorString().split("\n"));
		if (millis > 0) {
			String time = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
					TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
					TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
			
			b.setDuration(time);
			
		}
		return parseChapterData(results.getOutputString().split("\n"));
		
	}
	
	private long parseDurationToMS(String lines[]) {
		for (String l : lines) {
			// 		  Duration: 31:43:24.67, start: 0.000000, bitrate: 64 kb/s
			l = l.trim();
			
			if (l.startsWith("Duration: ")) {
				int end = l.indexOf(',');
				if (end == -1)
					end = l.length();
				String time = l.substring(l.indexOf(':') + 1, end).trim();
				String units[] = time.split(":");
				
				assert (units.length == 3);
				
				if (units.length == 3) {
					int hours = Integer.valueOf(units[0]);
					int minutes = Integer.valueOf(units[1]);
					float seconds = Float.valueOf(units[2]);
					assert (minutes >= 0 && minutes < 60);
					assert (seconds >= 0 && seconds < 60);
					long ms = hours * 3600000L + minutes * 60000L + Math.round(seconds * 1000.0);
					return ms;
				}
			}
		}
		
		return 0;
	}
	
	private ArrayList<Chapter> parseChapterData(String lines[]) {
		String timebase = "TIMEBASE=";
		String start = "START=";
		String end = "END=";
		String title = "title=";
		
		Chapter cur = null;
		ArrayList<Chapter> list = new ArrayList<>();
		
		for (String l : lines) {
			if (l.equals("[CHAPTER]")) {
				if (cur != null) {
					list.add(cur);
				}
				cur = new Chapter();
				continue;
			}
			if (cur != null) {
				if (l.startsWith(timebase)) {
					cur.timebase = l.substring(timebase.length());
				}
				if (l.startsWith(start)) {
					cur.start = l.substring(start.length());
				}
				if (l.startsWith(end)) {
					cur.end = l.substring(end.length());
				}
				if (l.startsWith(title)) {
					cur.title = l.substring(title.length());
				}
			}
			
		}
		
		if (cur != null)
			list.add(cur);
		
		for (Chapter c : list) {
			if (c.timebase == null || c.start == null || end == null || title == null) {
				LOG.debug("Unexpected Chapter data:" + c.timebase + " " + c.start + " " + c.end + " " + c.title);
			}
			
		}
		
		return list;
	}
	
	public boolean parseBook(Book b) {
		File aaxFile = Audible.instance.getAAXFileDest(b);
		if (!aaxFile.exists())
			return false;
		
		try {
			Mp4FileReader reader = new Mp4FileReader();
			AudioFile audiofile = reader.read(aaxFile);
			Mp4Tag tag = (Mp4Tag) audiofile.getTag();
			LOG.info("" + audiofile + ", tag=" + tag);
			tagsToBook(tag, b);
			
		} catch (Throwable th) {
			th.printStackTrace();
			
			LOG.debug("Error parsing aax: " + aaxFile.getAbsolutePath(), th);
			return false;
		}
		return true;
		
	}
	
	public String stats() {
		return "ParseAAX: parseCount=" + parseCount + " aaxBigger=" + aaxBigger + " aaxSmaller=" + aaxSmaller + " aaxSame=" + aaxSame;
	}
	
	public void update(Book book) throws Exception {
		updateBookFromAAX(book, CoverImageAction.replaceImage);
	}
	
	public void updateBookFromAAX(Book book, CoverImageAction imageAction) throws Exception {
		File aaxFile = Audible.instance.getAAXFileDest(book);
		if (!aaxFile.exists())
			return;
		
		if (true) {
			ffmpeg(book, aaxFile);
		}
		
		File imageDest = Audible.instance.getImageFileDest(book);
		Book aaxBook = parseAAX(aaxFile, imageDest, imageAction);
		if (!aaxBook.getProduct_id().equals(book.getProduct_id()))
			throw new Exception("product id mismatch for " + book + " and " + aaxBook);
		for (BookElement e : BookElement.values()) {
			String value = aaxBook.get(e);
			if (value.length() > 0) {
				book.set(e, value);
			}
		}
	}
	
	private void mergeItem(Book book, Book aaxBook, BookElement e) {
		String s1 = book.get(e);
		String s2 = aaxBook.get(e);
		String result = mergeItem(s1, s2);
		book.set(e, result);
		
	}
	
	// s1 = original value
	// s2 = new value from aax
	private String mergeItem(String s1, String s2) {
		if (s1 == null)
			s1 = "";
		if (s2 == null)
			s2 = "";
		if (s1.equals(s2))
			return s1; // normal case..
		
		int l1 = s1.length();
		int l2 = s2.length();
		if (l1 > l2) {
			System.err.println("Using old value:\n  " + s1 + " not\n  " + s2);
			return s1;
		} else {
			if (s1.length() > 0)
				System.err.println("Update aax value: " + s2 + " from " + s1);
			return s2;
		}
	}
	
	
	public void tagsToBook(Mp4Tag tag, Book b) {
		if (LOG.isTraceEnabled())
			LOG.info(tag);
		
		b.set(BookElement.author, getValue(tag, "©ART"));
		b.setNarratedBy(getValue(tag, "©nrt"));
		b.setFullTitle(getValue(tag, "©nam"));
		b.setShortTitle(getValue(tag, "@sti"));
		b.setRelease_date(getValue(tag, "rldt"));
		b.setPublisher(getValue(tag, "©pub"));
		b.setProduct_id(getValue(tag, "prID"));
		b.setSummary(getValue(tag, "©des"));
		b.setDescription(getValue(tag, "©cmt"));
		b.setGenre(getValue(tag, "©gen"));
		b.setShortTitle(getValue(tag, "@sti"));
		b.setCopyright(getValue(tag, "cprt"));
		b.setAsin(getValue(tag, "CDEK"));
		
		
	}

	/*
    id=©nam, fk=TITLE, value=10% Happier: How I Tamed the Voice in My Head, Reduced Stress Without Losing My Edge, and Found a Self-Help That Actually Works (Unabridged)
			id=©ART, fk=ARTIST, value=Dan Harris
			id=aART, fk=ALBUM_ARTIST, value=Dan Harris
			id=©alb, fk=ALBUM, value=10% Happier (Unabridged)
			id=©gen, fk=GENRE_CUSTOM, value=Audiobook
			id=prID, fk=null, value=BK_HARP_003839
			id=©cmt, fk=COMMENT, value=Dan Harris embarks on an unexpected, hilarious, and deeply skeptical odyssey through the strange worlds of spirituality and self-help....
			id=©des, fk=null, value=Nightline anchor Dan Harris embarks on an unexpected, hilarious, and deeply skeptical odyssey through the strange worlds of spirituality and self-help, and discovers a way to get happier that is truly achievable. After having a nationally televised panic attack on Good Morning America, Dan Harris knew he had to make some changes. A lifelong nonbeliever, he found himself on a bizarre adventure, involving a disgraced pastor, a mysterious self-help guru, and a gaggle of brain scientists. Eventually, Harris realized that the source of his problems was the very thing he always thought was his greatest asset: the incessant, insatiable voice in his head, which had both propelled him through the ranks of a hyper-competitive business and also led him to make the profoundly stupid decisions that provoked his on-air freak-out. We all have a voice in our head. It's what has us losing our temper unnecessarily, checking our email compulsively, eating when we're not hungry, and fixating on the past and the future at the expense of the present. Most of us would assume we're stuck with this voice that there's nothing we can do to rein it in but Harris stumbled upon an effective way to do just that. It's a far cry from the miracle cures peddled by the self-help swamis he met; instead, it's something he always assumed to be either impossible or useless: meditation. After learning about research that suggests meditation can do everything from lower your blood pressure to essentially rewire your brain, Harris took a deep dive into the underreported world of CEOs, scientists, and even marines who are now using it for increased calm, focus, and happiness. 10% Happier takes listeners on a ride from the outer reaches of neuroscience to the inner sanctum of network news to the bizarre fringes of America's spiritual scene, and leaves them with a takeaway that could actually change their lives. 
			id=cprt, fk=COPYRIGHT, value=&#169;2014 Daniel Benjamin Harris; (P)2014 HarperCollinsPublishers
			id=©pub, fk=null, value=HarperAudio
			id=©day, fk=DAY, value=2014
			id=©nrt, fk=null, value=Dan Harris
			id=CDEK, fk=null, value=B00I8NRAE0
			id=CDET, fk=null, value=ADBL
			id=VERS, fk=null, value=1
			id=GUID, fk=null, value=A3STJMQ53CY8GP
			id=AACR, fk=null, value=CRxxx
			id=@sti, fk=null, value=10% Happier (Unabridged)
			id=rldt, fk=null, value=11-MAR-2014
			id=covr, fk=ARTWORK, value=COVERART_JPEG:18636bytes
	 */
	
	public String getValue(Mp4Tag t, String id) {
		String out = t.getFirst(id);
		return out;
	}
	
	public enum CoverImageAction {
		replaceImage, useBiggerImage, doNothing, saveInDirectory
	}
	
	class Chapter {
		String timebase;
		String start;
		String end;
		String title;
	}
	
}
