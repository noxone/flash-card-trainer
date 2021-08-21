package org.olafneumann.trainer.action;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

class GoogleTextToSpeechProvider implements TextToSpeechProvider {
	private static final int MAX_BYTE_COUNT = 10 * 1024 * 1024; // 10 MB
	private static final Charset CHARSET = StandardCharsets.UTF_8;
	private static final String URL_PATTERN = "https://translate.google.com/translate_tts?ie=$(encoding)&tl=$(locale)&client=tw-ob&q=$(search)";

	private final Map<GoogleTtsKey, byte[]> speeches = new HashMap<>();

	@Override
	public boolean say(final Locale locale, final String text) {
		final GoogleTtsKey key = new GoogleTtsKey(locale, text);
		final byte[] speech = getSpeech(key);
		if (speech != null) {
			TextToSpeech.playMp3(new ByteArrayInputStream(speech));
			return true;
		}
		return false;
	}

	private byte[] getSpeech(final GoogleTtsKey key) {
		if (getUsedBytes() > MAX_BYTE_COUNT) {
			speeches.clear();
		}
		if (!speeches.containsKey(key)) {
			final byte[] data = getVocaleData(key);
			if (data != null) {
				speeches.put(key, data);
			}
		}
		return speeches.get(key);
	}

	private byte[] getVocaleData(final GoogleTtsKey key) {
		final URL url = getTtsUrl(key);
		if (url == null) {
			return null;
		}
		try {
			return download(url);
		} catch (final IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private int getUsedBytes() {
		int counter = 0;
		for (final byte[] data : speeches.values()) {
			counter += data.length;
		}
		return counter;
	}

	private static URL getTtsUrl(final GoogleTtsKey key) {
		String urlString;
		try {
			urlString = URL_PATTERN//
					.replace("$(encoding)", CHARSET.name())//
					.replace("$(locale)", key.getLanguage())//
					.replace("$(search)", URLEncoder.encode(key.getText(), CHARSET.name()));
		} catch (final UnsupportedEncodingException e) {
			throw new UncheckedIOException(e);
		}

		try {
			return new URL(urlString);
		} catch (final MalformedURLException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static byte[] download(final URL url) throws IOException {
		try (Socket socket = new Socket("translate.google.com", 80)) {
			final BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
			out.write(("GET " + url.getFile() + " HTTP/1.0\r\n").getBytes());
			out.write(("Host: " + url.getHost() + "\r\n").getBytes());
			out.write("Connection: close\r\n".getBytes());
			out.write("\r\n".getBytes());
			out.flush();
			return extractContent(new ByteArrayInputStream(read(new BufferedInputStream(socket.getInputStream()))));
		}
	}

	private static byte[] extractContent(final InputStream in) throws IOException {
		while (!"".equals(readLine(in))) {
			;
		}
		return read(in);
	}

	private static String readLine(final InputStream in) throws IOException {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int l = -1;
		int b = -1;
		for (;;) {
			b = in.read();
			if (b == -1) {
				throw new RuntimeException("End of stream too early!");
			}
			if (b == '\n' && l == '\r') {
				final byte[] buffer = bos.toByteArray();
				return new String(buffer, 0, buffer.length - 1);
			}
			l = b;
			bos.write(b);
		}
	}

	private static void copy(final InputStream in, final OutputStream out) throws IOException {
		try {
			final byte[] buffer = new byte[1024];
			int len;
			while ((len = in.read(buffer)) != -1) {
				out.write(buffer, 0, len);
			}
		} finally {
			try {
				in.close();
			} catch (final IOException e) {
			}
			try {
				out.close();
			} catch (final IOException e) {
			}
		}
	}

	private static byte[] read(final InputStream in) throws IOException {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		copy(in, bos);
		return bos.toByteArray();
	}

	private static class GoogleTtsKey {
		private final String language;
		private final String text;

		public GoogleTtsKey(final Locale locale, final String text) {
			this(locale.getLanguage(), text);
		}

		public GoogleTtsKey(final String language, final String text) {
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
			return Objects.hash(language, text);
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final GoogleTtsKey other = (GoogleTtsKey) obj;
			if (!Objects.equals(language, other.language)) {
				return false;
			}
			if (!Objects.equals(text, other.text)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return "GoogleTtsKey [language=" + language + ", text='" + text + "']";
		}

	}
}
