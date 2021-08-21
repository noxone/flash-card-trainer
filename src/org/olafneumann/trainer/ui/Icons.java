package org.olafneumann.trainer.ui;

import java.awt.Image;
import java.awt.MediaTracker;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;

public enum Icons {
	// iconset:CrystalClear
	// https://www.iconfinder.com/search/?q=iconset:CrystalClear
	LOGO("file-manager_32.png"), //$NON-NLS-1$
	CLOSE("stop.png"), //$NON-NLS-1$
	SHOW_HIDE("showhide.png"), //$NON-NLS-1$
	NEXT("next.png"), //$NON-NLS-1$
	CHECK("check.png"), //$NON-NLS-1$
	CHECK_SMALL("check_small.png"), //$NON-NLS-1$
	CLEAR("clear_16.png"), //$NON-NLS-1$
	PRINT("print_16.png"), //$NON-NLS-1$
	// https://www.iconfinder.com/icons/3246/arrow_play_player_icon#size=16
	PLAY("play_16.png"), //$NON-NLS-1$
	ARROW_DOWN("downarrow.png"), //$NON-NLS-1$
	TaeglichChinesisch("tc.png"), // //$NON-NLS-1$
	Google("google_16.png"), // //$NON-NLS-1$
	Leo("leo_de_zh.png"), // //$NON-NLS-1$
	YellowBridge("yellowbridge_16.png"), // //$NON-NLS-1$
	Langenscheidt("langenscheid.ico"), // //$NON-NLS-1$
	//
	BUTTON_ADD("btnAdd.png"), //$NON-NLS-1$
	BUTTON_LOAD("btnLoad.png"), //$NON-NLS-1$
	BUTTON_REMOVE("btnRemove.png"), //$NON-NLS-1$
	BUTTON_SAVE("btnSave.png"), //$NON-NLS-1$
	BUTTON_RESET("btnReset.png"), //$NON-NLS-1$
	BUTTON_PRINT("btnPrint.png"), //$NON-NLS-1$
	BUTTON_TRAINING("btnTraining.png"), //$NON-NLS-1$
	BUTTON_BACK("btnBack.png"),//
	BUTTON_FORWARD("btnForward.png"),//
	;

	private final ImageIcon icon;
	private final List<Image> imageList = new ArrayList<Image>();

	private Icons(String filename) {
		filename = "gfx/" + filename;
		this.icon = loadImageIcon(filename);
		imageList.addAll(loadImageList(filename));
		if (imageList.isEmpty())
			imageList.add(icon.getImage());
	}

	public ImageIcon getImageIcon() {
		return icon;
	}

	public Image getImage() {
		return icon.getImage();
	}

	public List<Image> getImageList() {
		return imageList;
	}

	private List<Image> loadImageList(String filename) {
		List<Image> images = new ArrayList<Image>();
		String extension = filename;
		if (extension.contains("."))
			extension = extension.substring(extension.lastIndexOf('.'));
		else
			extension = "";

		String prefix = filename.substring(0, filename.length() - extension.length());

		if (Character.isDigit(prefix.charAt(prefix.length() - 1))) {
			while (Character.isDigit(prefix.charAt(prefix.length() - 1))) {
				prefix = prefix.substring(0, prefix.length() - 1);
			}
			if (prefix.charAt(prefix.length() - 1) == '_') {
				for (int size : new int[] { 16, 22, 32, 48, 64, 128, 256 }) {
					String newFilename = prefix + size + extension;
					if (new File(newFilename).exists()) {
						ImageIcon icon = loadImageIcon(newFilename);
						try {
							while (icon.getImageLoadStatus() == MediaTracker.LOADING) {
								Thread.sleep(50);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						images.add(icon.getImage());
					}
				}
			}
		}
		return images;
	}

	private static ImageIcon loadImageIcon(String filename) {
		try {
			return new ImageIcon(Icons.class.getResource("/" + filename)); //$NON-NLS-1$
		} catch (Exception e) {
			return new ImageIcon(filename);
		}
	}
}
