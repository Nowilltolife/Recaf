package me.coley.recaf.decompile.fallback;

import me.coley.cafedude.classfile.ClassFile;
import me.coley.cafedude.io.ClassFileReader;
import me.coley.recaf.BuildConfig;
import me.coley.recaf.code.ClassInfo;
import me.coley.recaf.decompile.DecompileOption;
import me.coley.recaf.decompile.Decompiler;
import me.coley.recaf.decompile.fallback.model.ClassModel;
import me.coley.recaf.workspace.Workspace;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;

/**
 * A basic decompiler implementation that generates a rough outline of the class.
 * The intent is to be used as a fallback option if other decompilers fail.
 *
 * @author Matt Coley
 */
public class FallbackDecompiler extends Decompiler {
	private static final String VERSION = "1X:" + BuildConfig.GIT_REVISION;

	/**
	 * New basic decompiler instance.
	 */
	public FallbackDecompiler() {
		super("Fallback", VERSION);
	}

	@Override
	protected String decompileImpl(Map<String, DecompileOption<?>> options, Workspace workspace, ClassInfo info) {
		try {
			// TODO: Resilience measures Handle funky unicode escapes
			ClassFile classFile = new ClassFileReader().read(applyInterceptors(info.getValue()));
			ClassModel model = new ClassModel(classFile);
			return model.print();
		} catch (Throwable t) {
			StringWriter trace = new StringWriter();
			t.printStackTrace(new PrintWriter(trace));
			return "// Could not parse class: " + info.getName() + "\n// " +
					trace.toString().replace("\n", "\n// ");
		}
	}

	@Override
	protected Map<String, DecompileOption<?>> createDefaultOptions() {
		return Collections.emptyMap();
	}
}