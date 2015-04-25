package gutugutu3030.realtime;

import java.lang.reflect.Field;
import java.net.*;
import java.io.*;
import java.util.*;

import org.omg.CORBA.PUBLIC_MEMBER;

import processing.mode.java.*;
import processing.mode.java.runner.*;
import processing.app.*;

public class RealtimeTweakMode extends JavaMode {

	String compiler_setup, compiler_draw, compiler_etc, jdk_path;
	MirrorThread2 mt;
	String playCode[]=null;//code during execution

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


		examplesFolder = Base.getContentFile("modes/java/examples");
		referenceFolder = Base.getContentFile("modes/java/reference");


		//prepared code traded written code by
		compiler_setup = readTXT(modePath("../compiler_setup.txt"));
		compiler_draw = readTXT(modePath("../compiler_draw.txt"));
		compiler_etc = readTXT(modePath("../compiler_etc.txt"));
		jdk_path = readTXT(modePath("../jdk_path.txt"));
		compiler_setup=compiler_setup.replace("JDK_PATH", jdk_path.replace("\n", ""));
		mt = new MirrorThread2(this);
		mt.start();
	}

	public String getTitle() {
		return "tweak+";
	}

	@Override
	public Editor createEditor(Base base, String path, EditorState state) {
		return new RealtimeTweakEditor(base, path, state, this);
	}

	public Runner handleRun(Sketch sketch, RunnerListener listener)
			throws SketchException {
		// presentation mode not supported
		SketchCode[] code = sketch.getCode();
		playCode=new String[code.length];
		for(int i=0;i<code.length;i++){
			playCode[i]=code[i].getProgram();
		}

		// edit setup()
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
		// edit void draw()
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

		// edit 
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
		System.out.println("pass:"+folder.getAbsolutePath());
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
		StringBuilder str = new StringBuilder();
		BufferedReader br = null;
		try {
			File file = new File(path);
			br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				str.append(line);
				str.append('\n');
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
			}
		}
		// return str;
		return new String(str);
	}

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
