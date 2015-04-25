package gutugutu3030.realtime;

import java.io.File;
import java.io.FileWriter;

import processing.app.*;
import processing.mode.java.*;
import processing.app.syntax.JEditTextArea;
import processing.app.syntax.PdeTextAreaDefaults;

public class RealtimeTweakEditor extends JavaEditor {
	public RealtimeTweakMode realtimeMode;

	String tempAppletAddtion;

	protected RealtimeTweakEditor(Base base, String path, EditorState state,
			Mode mode) {
		super(base, path, state, mode);
		((RealtimeTweakMode) mode).mt.setEditor(this);
	}
}
