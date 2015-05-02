package gutugutu3030.realtime;

import java.io.File;
import java.io.PrintWriter;

import javax.swing.JFrame;

import java.awt.*;
import java.io.FileWriter;
import java.util.regex.*;
import java.awt.event.*;

import processing.app.*;
import antlr.RecognitionException;
import processing.mode.java.preproc.*;

public class MirrorThread2 extends Thread {
	boolean running = true;
	RealtimeTweakEditor editor;
	String tempAppletAddtion;
	RealtimeTweakMode mode;
	String presantCode = null;
	int playCode_index = -1;
	Pattern colorChanger;
	ColorSelecter cs;
	JFrame colorframe;
	boolean test = false;
	PdePreprocessor preproc;

	public MirrorThread2(RealtimeTweakMode mode) {
		// TODO 自動生成されたコンストラクター・スタブ
		this.mode = mode;
		tempAppletAddtion = mode.readTXT(mode.modePath("../tempapplet.txt"));
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
		preproc = new PdePreprocessor("TempPApplet");
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
			// editorが今参照しているインデックスを記録
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

			String bigCode = "";
			bigCode += tempAppletAddtion;
			int bigCount = 0;
			for (String sc : playCode) {
				bigCode += sc + "\n";
			}
			try {
				File java = new File(mode.modePath("TempPApplet.java"));
				PrintWriter stream = new PrintWriter(new FileWriter(java));
				PreprocessorResult result;
				try {
					result = preproc.write(stream, bigCode.toString());
				} finally {
					stream.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	int tweaktype = -1;
	int selectedLine = -1;
	String presant_selected = null;// 色選択用
	String presant_selected1 = null, presant_selected2 = null;// 数字選択用
	int selectednum = -1;// 数字選択用

	public void selecterClose() {
		System.out.println("呼び出し");
		colorframe.setVisible(false);
		tweaktype = -1;
	}

	public void tweak() {
		if (editor == null)
			return;
		// System.out.println("rttp "+editor.rtTextArea.rttp.dragtype);
		if (tweaktype != -1) {
			switch (tweaktype) {
			case 0:// 色の変更
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
