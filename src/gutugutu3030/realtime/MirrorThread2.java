package gutugutu3030.realtime;

import java.io.File;
import javax.swing.JFrame;
import java.awt.*;
import java.io.FileWriter;
import java.util.regex.*;
import java.awt.event.*;
import processing.app.*;

public class MirrorThread2 extends Thread {
	boolean running = true;
	RealtimeTweakEditor editor;
	String tempAppletAddtion;
	RealtimeTweakMode mode;
	String presantCode = null;
	int playCode_index = -1;
	Pattern doubleToFloat, colorChanger;
	ColorSelecter cs;
	JFrame colorframe;
	boolean test = false;

	public MirrorThread2(RealtimeTweakMode mode) {
		this.mode = mode;
		tempAppletAddtion = mode.readTXT(mode.modePath("../tempapplet.txt"));
		doubleToFloat = Pattern
				.compile("(?:^|[^\\.\\d])(\\d+\\.\\d+)(?:[^\\.\\d]|$)");
		colorChanger = Pattern.compile("^(background|fill|stroke)$");
		colorframe = new JFrame("color");
		Insets insets = colorframe.getInsets();
		colorframe.setSize(400 + insets.left + insets.right, 350 + insets.top
				+ insets.bottom);
		cs = new ColorSelecter(this);
		cs.init();
		cs.resize(colorframe.getSize());
		colorframe.getContentPane().add(cs);
		colorframe.setResizable(false);
		colorframe.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				colorframe.setVisible(false);
				tweaktype = -1;
			}
		});
	}

	public void setEditor(RealtimeTweakEditor editor) {
		this.editor = editor;
	}

	public void run() {
		while (running) {
			String playCode[] = mode.playCode;
			if (editor == null || playCode == null) {
				try {
					Thread.sleep(100);
				} catch (Exception e) {
				}
				continue;
			}
			tweak();
			if (presantCode != null && presantCode.equals(editor.getText())) {
				try {
					Thread.sleep(100);
				} catch (Exception e) {
				}
				continue;
			}
			presantCode = new String(editor.getText());
			//recording index reffered editor
			{
				int tmp_playCode_index = -1;
				for (int i = 0; i < playCode.length; i++) {
					if (playCode[i].equals(presantCode)) {
						tmp_playCode_index = i;
						break;
					}
				}
				if (tmp_playCode_index != -1) {
					playCode_index = tmp_playCode_index;
				} else {
					playCode[playCode_index] = presantCode;
				}
			}
			// System.out.println("playCode_index:" + playCode_index);

			File file = new File(mode.modePath("TempPApplet.java"));
			try {
				FileWriter filewriter = new FileWriter(file);
				// FileWriter testr = new FileWriter(new File(
				// mode.modePath("test.txt")));
				// testr.write(editor.getText());
				// testr.close();

				// edit imports
				StringBuilder getText = new StringBuilder();
				{
					// SketchCode codes[] = editor.getSketch().getCode();
					for (String sss : playCode) {
						String imports[] = sss.split(";");// .getProgram().split(";");
						for (String s : imports) {
							if (s.indexOf("import ") != -1) {
								filewriter.write(s + ";");
							} else if (s == imports[imports.length - 1]) {
								getText.append(s);
							} else {
								getText.append(s);
								getText.append(";");
								// getText+=s+";";
							}
						}
					}
				}

				// filewriter.write(tempAppletAddtion.replace("XXXXX",
				// editor.getSketch().getName()));
				filewriter.write(tempAppletAddtion.replace("XXXXX", "PApplet"));

				// double to float
				Matcher m = doubleToFloat.matcher(getText);
				StringBuffer sb = new StringBuffer();
				while (m.find()) {
					getText.insert(m.end() - 1, "f");
				}

				String str[] = new String(getText).split("\n");
				for (String s1 : str) {
					boolean public_flg = false;
					if (ifContein(s1, "setup()"))
						public_flg = true;
					if (ifContein(s1, "draw()"))
						public_flg = true;
					if (ifContein(s1, "mousePressed()"))
						public_flg = true;
					if (ifContein(s1, "mouseReleased()"))
						public_flg = true;
					if (ifContein(s1, "keyPressed()"))
						public_flg = true;
					if (ifContein(s1, "keyReleased()"))
						public_flg = true;
					if (ifContein(s1, "windowClosing("))
						public_flg = true;
					if (ifContein(s1, "String", "toString()"))
						public_flg = true;
					if (public_flg) {
						filewriter.write(" public ");
					}
					filewriter.write(s1 + "\n");
				}
				filewriter.write("}");
				filewriter.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(100);
			} catch (Exception e) {
			}
		}
	}

	int tweaktype = -1;
	int selectedLine = -1;
	String presant_selected = null;// for selecting color
	String presant_selected1 = null, presant_selected2 = null;// for selecting number
	int selectednum = -1;// for selecting number

	public void selecterClose() {
		//System.out.println("呼び出し");
		colorframe.setVisible(false);
		tweaktype = -1;
	}

	public void tweak() {
		if (editor == null)
			return;
		// System.out.println("rttp "+editor.rtTextArea.rttp.dragtype);
		if (tweaktype != -1) {
			switch (tweaktype) {
			case 0:// change color
				int c[] = cs.getColor();
				if (c != null) {

					editor.setLineText(selectedLine, presant_selected + "("
							+ c[0] + "," + c[1] + "," + c[2] + ");\n");
				}
				break;
			}
			return;
		}
		String selected = editor.getSelectedText();
		if (selected == null) {
			return;
		}
		// 色
		if (colorChanger.matcher(selected).find()) {
			int index = editor.getCaretOffset();
			for (int i = 0, n = editor.getLineCount(); i < n; i++) {
				if (index <= editor.getLineStopOffset(i)) {
					selectedLine = i;
					break;
				}
			}
			editor.setSelectedText(selected);
			presant_selected = selected;
			colorframe.setVisible(true);
			tweaktype = 0;
			return;
		}
	}

	boolean ifContein(String s1, String str1, String str2) {
		int index1 = s1.indexOf(str2), index2 = s1.indexOf(str1);
		return index1 != -1 && index2 != -1 && index1 > index2;
	}

	boolean ifContein(String s1, String str) {
		return ifContein(s1, "void", str);
	}
}
