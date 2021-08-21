package org.olafneumann.trainer.data;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public enum WriteMode {
	ZippedAnnotationalSettings("Packed annotational settings", "pas"), //
	XML("XML", "xml"), //
	ZippedXML("Compressed XML", "cml"), //
	JavaObject("POJO serialization", "pojo"), //
	AnnotationalSettings("Annotational Settings", "ans"), //
	Text("Text", "txt"), //
	;

	private final String text;
	private final String extension;
	private final FileFilter filter = new WriteModeFileFilter();

	WriteMode(final String text, final String extension) {
		this.text = text;
		this.extension = extension.toLowerCase();
	}

	public String getText() {
		return text;
	}

	public String getExtension() {
		return extension;
	}

	public FileFilter getFileChooseFileFilter() {
		return filter;
	}

	public boolean appliesToExtensionOf(final File file) {
		return file.getName().toLowerCase().endsWith("." + extension);
	}

	private class WriteModeFileFilter extends FileFilter {
		@Override
		public boolean accept(final File file) {
			return file.isDirectory() || file.isFile() && file.getName().toLowerCase().endsWith("." + extension);
		}

		@Override
		public String getDescription() {
			return text + " (*." + extension + ")";
		}
	}

	public static WriteMode getMode(final FileFilter filter) {
		for (final WriteMode mode : values()) {
			if (mode.filter == filter) {
				return mode;
			}
		}
		return null;
	}

	public static WriteMode getMode(final File file) {
		String name = file.getName();
		if (name.contains(".")) {
			name = name.substring(name.indexOf(".") + 1);
		}
		return getMode(name);
	}

	public static WriteMode getMode(String extension) {
		extension = extension.toLowerCase();
		for (final WriteMode mode : values()) {
			if (mode.extension.equals(extension)) {
				return mode;
			}
		}
		return null;
	}

	private static final AllSupportedFilesFileFilter allFilesFilter = new AllSupportedFilesFileFilter();

	public static FileFilter getAllSupportedFilesFilter() {
		return allFilesFilter;
	}

	private static class AllSupportedFilesFileFilter extends FileFilter {
		@Override
		public boolean accept(final File file) {
			for (final WriteMode mode : values()) {
				if (mode.getFileChooseFileFilter().accept(file)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public String getDescription() {
			final StringBuilder sb = new StringBuilder();
			sb.append("All supported file types (");
			for (final WriteMode mode : values()) {
				sb.append("*.").append(mode.extension).append(", ");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.deleteCharAt(sb.length() - 1);
			sb.append(")");
			return sb.toString();
		}
	}
}