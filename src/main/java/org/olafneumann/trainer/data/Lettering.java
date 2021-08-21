package org.olafneumann.trainer.data;

import java.awt.Color;
import java.awt.Font;

public class Lettering {
	private Font font;
	private Color color;

	public Lettering(final Font font, final Color color) {
		this.font = font;
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public Font getFont() {
		return font;
	}

	public void setColor(final Color color) {
		this.color = color;
	}

	public void setFont(final Font font) {
		this.font = font;
	}
}
