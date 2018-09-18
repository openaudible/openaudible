package org.openaudible.util;

import org.openaudible.progress.IProgressTask;

import java.io.*;

public class CopyWithProgress {
	final static int bufSize = 64 * 1024;
	
	public static String byteCountToString(long l) {
		long k = l / 1024;
		long m = l / (1024 * 1024);
		
		if (m == 0) {
			if (k < 9)
				return "" + l + " bytes";
			return "" + k + "K";
		}
		if (m > 1024) {
			return "" + m + "M";
		}
		if (m > 2) {
			double d = l / (double) (1024 * 1024);
			String out = Double.toString(d);
			if (out.length() > 6)
				return out.substring(0, 6) + "M";
			return out + "M";
		}
		return "" + k + "K";
	}
	
	
	public static long copyWithProgress(final IProgressTask p, final File in, final File out) throws IOException {
		final String totalBytes = byteCountToString(in.length());
		
		ByteReporter e = bytesRead -> {
			p.setSubTask(byteCountToString(bytesRead) + " of " + totalBytes + " " + out.getName());
			if (p.wasCanceled())
				throw new IOException("Canceled");
		};
		
		FileInputStream fis = null;
		FileOutputStream fos = null;
		boolean success = false;
		try {
			fis = new FileInputStream(in);
			fos = new FileOutputStream(out);
			
			long b = copyWithProgress(e, 300, fis, fos);
			success = true;
			return b;
		} finally {
			if (fis != null)
				fis.close();
			if (fos != null)
				fos.close();
			if (!success)
				out.delete();
			
		}
		
	}
	
	public static long copyWithProgress(ByteReporter t, long reportInterval, InputStream fis, OutputStream os) throws IOException {
		long nextReport = 0;
		long read = 0;
		
		byte buffer[] = new byte[bufSize];
		while (true) {
			int read_size = bufSize;
			int nbytes = fis.read(buffer, 0, read_size);
			if (nbytes == -1)
				break;
			read += nbytes;
			os.write(buffer, 0, nbytes);
			
			if (System.currentTimeMillis() > nextReport) {
				t.bytesCopied(read);
				nextReport = System.currentTimeMillis() + reportInterval;
			}
		}
		
		return read;
	}
	
	public interface ByteReporter {
		void bytesCopied(long total) throws IOException; // throw IOException if/when canceled by user.
	}
	
}
