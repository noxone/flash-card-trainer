package org.olafneumann.trainer.action;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

class GoogleTextToSpeechProvider implements TextToSpeechProvider {
	private static final int MAX_BYTE_COUNT = 10 * 1024 * 1024; // 10 MB
	private static final Charset CHARSET = StandardCharsets.UTF_8;
	private static final String URL_PATTERN = "https://translate.google.com/translate_tts?ie=$(encoding)&tl=$(locale)&client=tw-ob&q=$(search)";

	private Map<GoogleTtsKey, byte[]> speeches = new HashMap<GoogleTtsKey, byte[]>();

	@Override
	public boolean say(Locale locale, String text) {
		GoogleTtsKey key = new GoogleTtsKey(locale, text);
		byte[] speech = getSpeech(key);
		if (speech != null) {
			TextToSpeech.playMp3(new ByteArrayInputStream(speech));
			return true;
		} else {
			return false;
		}
	}

	private byte[] getSpeech(GoogleTtsKey key) {
		if (getUsedBytes() > MAX_BYTE_COUNT)
			speeches.clear();
		if (!speeches.containsKey(key)) {
			byte[] data = getVocaleData(key);
			if (data != null) {
				speeches.put(key, data);
			}
		}
		return speeches.get(key);
	}

	private byte[] getVocaleData(GoogleTtsKey key) {
		URL url = getTtsUrl(key);
		if (url != null) {
			try {
				return download(url);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}

	private int getUsedBytes() {
		int counter = 0;
		for (byte[] data : speeches.values())
			counter += data.length;
		return counter;
	}

	private static URL getTtsUrl(GoogleTtsKey key) {
		String urlString = URL_PATTERN//
				.replace("$(encoding)", CHARSET.name())//
				.replace("$(locale)", key.getLanguage())//
				.replace("$(search)", URLEncoder.encode(key.getText(), CHARSET));

		try {
			return new URL(urlString);
		} catch (MalformedURLException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static byte[] download(URL url) throws IOException {
		Socket socket = new Socket("translate.google.com", 80);
		try {
			BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
			out.write(("GET " + url.getFile() + " HTTP/1.0\r\n").getBytes());
			out.write(("Host: " + url.getHost() + "\r\n").getBytes());
			out.write(("Connection: close\r\n").getBytes());
			out.write(("\r\n").getBytes());
			out.flush();
			return extractContent(new ByteArrayInputStream(read(new BufferedInputStream(socket.getInputStream()))));
		} finally {
			socket.close();
		}
	}

	private static byte[] extractContent(InputStream in) throws IOException {
		while (!"".equals(readLine(in)))
			;
		return read(in);
	}

	private static String readLine(InputStream in) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int l = -1;
		int b = -1;
		for (;;) {
			b = in.read();
			if (b == -1)
				throw new RuntimeException("End of stream too early!");
			if (b == '\n' && l == '\r') {
				byte[] buffer = bos.toByteArray();
				return new String(buffer, 0, buffer.length - 1);
			} else {
				l = b;
				bos.write(b);
			}
		}
	}

	private static void copy(InputStream in, OutputStream out) throws IOException {
		try {
			byte[] buffer = new byte[1024];
			int len;
			while ((len = in.read(buffer)) != -1)
				out.write(buffer, 0, len);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
			}
			try {
				out.close();
			} catch (IOException e) {
			}
		}
	}

	private static byte[] read(InputStream in) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		copy(in, bos);
		return bos.toByteArray();
	}

	private static class GoogleTtsKey {
		private final String language;
		private final String text;

		public GoogleTtsKey(Locale locale, String text) {
			this(locale.getLanguage(), text);
		}

		public GoogleTtsKey(String language, String text) {
			this.language = language;
			this.text = text;
		}

		public String getLanguage() {
			return language;
		}

		public String getText() {
			return text;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((language == null) ? 0 : language.hashCode());
			result = prime * result + ((text == null) ? 0 : text.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			GoogleTtsKey other = (GoogleTtsKey) obj;
			if (language == null) {
				if (other.language != null)
					return false;
			} else if (!language.equals(other.language))
				return false;
			if (text == null) {
				if (other.text != null)
					return false;
			} else if (!text.equals(other.text))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "GoogleTtsKey [language=" + language + ", text='" + text + "']";
		}

	}
}
