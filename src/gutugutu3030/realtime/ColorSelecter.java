package gutugutu3030.realtime;

import processing.app.*;
import processing.core.*;
import javax.swing.JFrame;
import java.awt.event.*;


public class ColorSelecter extends PApplet {
	int h, s, v;
	int offset_x, offset_y;
	int clickEnable = -1;
	MirrorThread2 mt;

	public ColorSelecter(MirrorThread2 mt) {
		this.mt = mt;
	}


	public void setup() {
		size(350, 300);
		colorMode(HSB, 256);
		h = 0;
		s = 0;
		v = 255;
		offset_x = 23;
		offset_y = 23;
	}

	public void draw() {
		try {
			background(200);
			switch (clickEnable) {
			case 1:
				s = mouseX - offset_x;
				v = 255 + offset_y - mouseY;
				if (s < 0)
					s = 0;
				if (s > 255)
					s = 255;
				if (v < 0)
					v = 0;
				if (v > 255)
					v = 255;
				break;
			case 2:
				h = mouseY - offset_y;
				if (h < 0)
					h = 0;
				if (h > 255)
					h = 255;
				println(h);
				break;
			}
			loadPixels();
			for (int i = 0; i < 256; i++) {
				for (int j = 0; j < 256; j++) {
					stroke(h, i, 255 - j);
					pixels[offset_x + i + (offset_y + j) * width] = HSV2RGB(h,
							i, 255 - j);
					// line(offset_x+i,offset_y+j,offset_x+i,offset_y+j);
				}
			}
			updatePixels();
			for (int i = 0; i < 256; i++) {
				stroke(i, 255, 255);
				line(offset_x * 2 + 256, offset_y + i, offset_x * 2 + 256 + 30,
						offset_y + i);
			}
			fill(255);
			stroke(0);
			rect(offset_x + s - 4, offset_y + 255 - v - 4, 8, 8);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void mousePressed() {
		if (mouseY >= offset_y && mouseY <= offset_y + 255) {
			if (mouseX >= offset_x && mouseX <= offset_x + 255) {
				s = mouseX - offset_x;
				v = 255 + offset_y - mouseY;
				clickEnable = 1;
				return;
			}
			if (mouseX >= offset_x * 2 + 256
					&& mouseX <= offset_x * 2 + 256 + 30) {
				h = mouseY - offset_y;
				clickEnable = 2;
				return;
			}
		}
	}

	public void mouseReleased() {
		clickEnable = -1;
	}

	public void setHSV(int h, int s, int v) {
		this.h = h;
		this.s = s;
		this.v = v;
	}

	public int[] getColor() {
		if (clickEnable != 1)
			return null;
		float h = this.h / 255.0f;
		float s = this.s / 255.0f;
		float v = this.v / 255.0f;
		float r = v;
		float g = v;
		float b = v;
		if (s > 0.0) {
			h *= 6.0;
			final int i = (int) h;
			final float f = h - (float) i;
			switch (i) {
			default:
			case 0:
				g *= 1 - s * (1 - f);
				b *= 1 - s;
				break;
			case 1:
				r *= 1 - s * f;
				b *= 1 - s;
				break;
			case 2:
				r *= 1 - s;
				b *= 1 - s * (1 - f);
				break;
			case 3:
				r *= 1 - s;
				g *= 1 - s * f;
				break;
			case 4:
				r *= 1 - s * (1 - f);
				g *= 1 - s;
				break;
			case 5:
				g *= 1 - s;
				b *= 1 - s * f;
				break;
			}
		}
		int rgb[] = { (int) (r * 255), (int) (g * 255), (int) (b * 255) };
		return rgb;
	}

	int HSV2RGB(int h1, int s1, int v1) {
		float h = h1 / 255.0f;
		float s = s1 / 255.0f;
		float v = v1 / 255.0f;
		float r = v;
		float g = v;
		float b = v;
		if (s > 0.0) {
			h *= 6.0;
			final int i = (int) h;
			final float f = h - (float) i;
			switch (i) {
			default:
			case 0:
				g *= 1 - s * (1 - f);
				b *= 1 - s;
				break;
			case 1:
				r *= 1 - s * f;
				b *= 1 - s;
				break;
			case 2:
				r *= 1 - s;
				b *= 1 - s * (1 - f);
				break;
			case 3:
				r *= 1 - s;
				g *= 1 - s * f;
				break;
			case 4:
				r *= 1 - s * (1 - f);
				g *= 1 - s;
				break;
			case 5:
				g *= 1 - s;
				b *= 1 - s * f;
				break;
			}
		}
		int R = (int) (r * 255);
		int G = (int) (g * 255);
		int B = (int) (b * 255);
		return (R << 16) + (G << 8) + B;
	}
}
