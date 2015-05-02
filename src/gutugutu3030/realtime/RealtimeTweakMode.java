package gutugutu3030.realtime;

import java.lang.reflect.Field;
import java.net.*;
import java.io.*;
import java.util.*;

import org.omg.CORBA.PUBLIC_MEMBER;

import processing.mode.java.*;
import processing.mode.java.runner.*;
import processing.app.*;

/**
 * Mode Template for extending Java mode in Processing IDE 2.0 or later.
 *
 */
public class RealtimeTweakMode extends JavaMode {

	String compiler_setup, compiler_draw, compiler_etc, jdk_path;
	MirrorThread2 mt;
	String playCode[]=null;//実行時のコード

	public RealtimeTweakMode(Base base, File folder) {
		super(base, folder);

		for (Mode m : base.getModeList()) {
			if (m.getClass() == JavaMode.class) {
				JavaMode jMode = (JavaMode) m;
				librariesFolder = jMode.getLibrariesFolder();
				rebuildLibraryList();
				break;
			}
		}

		// Fetch examples and reference from java mode
		examplesFolder = Base.getContentFile("modes/java/examples");
		referenceFolder = Base.getContentFile("modes/java/reference");

		compiler_setup = readTXT(modePath("../compiler_setup.txt"));
		compiler_draw = readTXT(modePath("../compiler_draw.txt"));
		compiler_etc = readTXT(modePath("../compiler_etc.txt"));
		jdk_path = readTXT(modePath("../jdk_path.txt"));
		compiler_setup=compiler_setup.replace("JDK_PATH", jdk_path.replace("\n", ""));
		mt = new MirrorThread2(this);
		mt.start();
	}

	/**
	 * Return the pretty/printable/menu name for this mode. This is separate
	 * from the single word name of the folder that contains this mode. It could
	 * even have spaces, though that might result in sheer madness or total
	 * mayhem.
	 */
	@Override
	public String getTitle() {
		return "tweak+";
	}

	/**
	 * Create a new editor associated with this mode.
	 */

	@Override
	public Editor createEditor(Base base, String path, EditorState state) {
		return new RealtimeTweakEditor(base, path, state, this);
	}

	/**
	 * Returns the default extension for this editor setup.
	 */
	/*
	 * @Override public String getDefaultExtension() { return null; }
	 */

	/**
	 * Returns a String[] array of proper extensions.
	 */
	/*
	 * @Override public String[] getExtensions() { return null; }
	 */

	/**
	 * Get array of file/directory names that needn't be copied during "Save
	 * As".
	 */
	/*
	 * @Override public String[] getIgnorable() { return null; }
	 */
	public Runner handleRun(Sketch sketch, RunnerListener listener)
			throws SketchException {
		// mt.start();
		// 普通にrunボタンが押された場合
		SketchCode[] code = sketch.getCode();
		playCode=new String[code.length];
		for(int i=0;i<code.length;i++){
			playCode[i]=code[i].getProgram();
		}

		// setup()に追記
		int stack = 0;
		int setupflg = -1;
		big: for (int i = 0; i < code.length; i++) {
			String c = code[i].getProgram();
			int tmp = c.indexOf("setup()");
			if (tmp != -1 && (tmp == 0 || c.charAt(tmp - 1) == ' '))
				setupflg = tmp;
			for (int j = 0, n = c.length(); j < n; j++) {
				char cc = c.charAt(j);
				switch (cc) {
				case '{':
					if (setupflg != -1 && setupflg < j)
						stack++;
					break;
				case '}':
					if (setupflg != -1 && setupflg < j) {
						stack--;
						if (stack == 0) {
//							code[i].setProgram(new String(
//									new StringBuilder(c)
//											.insert(j,
//													new StringBuilder(
//															"targetFile = \"")
//															.append(modePath("")
//																	.replace(
//																			"\\",
//																			"\\\\"))
//															.append("\"+ targetClass + \".java\";targetFolder=\"")
//															.append(modePath("")
//																	.replace(
//																			"\\",
//																			"\\\\"))
//															.append("\";")
//															.append(compiler_setup))));

							code[i].setProgram(new String(
									new StringBuilder(c)
											.insert(j,
													new StringBuilder(
															"targetFile = \"")
															.append(modePath(""))
															.append("\"+ targetClass + \".java\";targetFolder=\"")
															.append(modePath(""))
															.append("\";")
															.append(compiler_setup))));
							break big;
						}
					}
					break;
				}
			}
		}
		// draw()の置換
		stack = 0;
		setupflg = -1;
		big: for (int i = 0; i < code.length; i++) {
			String c = code[i].getProgram();
			int tmp = c.indexOf("draw()");
			if (tmp != -1 && (tmp == 0 || c.charAt(tmp - 1) == ' '))
				setupflg = tmp;
			for (int j = 0, n = c.length(); j < n; j++) {
				char cc = c.charAt(j);
				switch (cc) {
				case '{':
					if (setupflg != -1 && setupflg < j)
						stack++;
					break;
				case '}':
					if (setupflg != -1 && setupflg < j) {
						stack--;
						if (stack == 0) {
							code[i].setProgram(new StringBuilder(c)
									.delete(setupflg, j)
									.insert(setupflg, compiler_draw).toString());
							break big;
						}
					}
					break;
				}
			}
		}

		// そのたもろもろ
		String c = code[0].getProgram();
		String header = compiler_etc;// "import javax.tools.JavaCompiler;import javax.tools.ToolProvider;import java.io.File;import java.lang.reflect.*;import java.net.URL;import java.net.URLClassLoader;String targetClass = \"TempPApplet\";String targetMethod = \"draw\";String targetFile, targetFolder;JavaCompiler compiler;ClassLoader loader;PApplet apapapapapa;class MirrorThread extends Thread {PApplet thisapapapapapa;boolean running=true;MirrorThread(PApplet apapapapapa) {thisapapapapapa=apapapapapa;}void run() {while (running) {try {int ret = compiler.run(null, null, null, new String[] { targetFile});if (ret == 0) {loader = URLClassLoader.newInstance(new URL[] { new File(targetFolder).toURI().toURL()}, this.getClass().getClassLoader());Class<?> clazz = Class.forName(targetClass, true, loader);if (clazz != null) {Method method = clazz.getMethod(targetMethod);Object instance=clazz.newInstance();clazz.getField(\"apa\").set(instance, apapapapapa);apapapapapa=(PApplet)instance;clazz.getField(\"g\").set(apapapapapa, g);clazz.getField(\"width\").set(apapapapapa, width);clazz.getField(\"height\").set(apapapapapa, height);clazz.getField(\"displayWidth\").set(apapapapapa, width);clazz.getField(\"displayHeight\").set(apapapapapa, height);clazz.getField(\"sketchPath\").set(apapapapapa, sketchPath);println(\"更新\");}}}catch(Exception e) {e.printStackTrace();}try{Thread.sleep(99);}catch(Exception e) {e.printStackTrace();}}}}";
		code[0].setProgram(header + c);
		File file = new File(modePath("start.java"));

		try {
			FileWriter filewriter = new FileWriter(file);
			for (SketchCode ccc : code) {
				filewriter.write(ccc.getProgram()+"\n");
			}
			filewriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		JavaBuild build = new JavaBuild(sketch);
		String appletClassName = build.build(false);
		if (appletClassName != null) {
			final Runner runtime = new Runner(build, listener);
			new Thread(new Runnable() {
				public void run() {
					runtime.launch(false);
				}
			}).start();
			return runtime;
		}
		return null;
	}

	/*
	 * public Runner handlePresent(Sketch sketch, RunnerListener listener)
	 * throws SketchException { //全画面モードでrunボタンが押された場合
	 * System.out.println("hundlepresent"); return super.handlePresent(sketch,
	 * listener); }
	 */

	private boolean isSketchModified(Sketch sketch) {
		for (SketchCode sc : sketch.getCode()) {
			if (sc.isModified()) {
				return true;
			}
		}
		return false;
	}

	public String modePath(String str) {
		return folder.getAbsolutePath().replace("\\","/")+"/mode/"
				+ str;
	}

	public String readTXT(String path) {
		// String str="";
		StringBuilder str = new StringBuilder();
		BufferedReader br = null;
		try {
			File file = new File(path);
			br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				// str += line + "\n";
				str.append(line);
				str.append('\n');
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				// ストリームは必ず finally で close します。
				br.close();
			} catch (IOException e) {
			}
		}
		// return str;
		return new String(str);
	}

	/**
	 * Retrieve the ClassLoader for JavaMode. This is used by Compiler to load
	 * ECJ classes. Thanks to Ben Fry.
	 *
	 * @return the class loader from java mode
	 */
	@Override
	public ClassLoader getClassLoader() {
		for (Mode m : base.getModeList()) {
			if (m.getClass() == JavaMode.class) {
				JavaMode jMode = (JavaMode) m;
				return jMode.getClassLoader();
			}
		}
		return null; // badness
	}
}
