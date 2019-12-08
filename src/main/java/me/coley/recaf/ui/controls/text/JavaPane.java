package me.coley.recaf.ui.controls.text;

import me.coley.recaf.compiler.JavacCompiler;
import me.coley.recaf.compiler.TargetVersion;
import me.coley.recaf.control.gui.GuiController;
import me.coley.recaf.parse.source.SourceCode;
import me.coley.recaf.ui.controls.text.model.Languages;
import me.coley.recaf.util.ClassUtil;
import me.coley.recaf.util.LangUtil;
import me.coley.recaf.workspace.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Java-focused text editor.
 *
 * @author Matt
 */
public class JavaPane extends TextPane {
	public static final int HOVER_ERR_TIME = 50;
	public static final int HOVER_DOC_TIME = 700;
	private final JavaErrorHandling errHandler = new JavaErrorHandling(this);
	private final JavaResource resource;
	private SourceCode code;
	private JavaDocHandling docHandler;

	/**
	 * @param controller
	 * 		Controller to act on.
	 * @param resource
	 * 		Resource containing the code.
	 */
	public JavaPane(GuiController controller, JavaResource resource) {
		super(controller, Languages.find("java"));
		this.resource = resource;
		setOnCodeChange(text -> errHandler.onCodeChange(text, () -> {
			code = new SourceCode(resource, getText());
			code.analyze(controller.getWorkspace());
			docHandler = new JavaDocHandling(this, controller, code);
		}));
	}

	@Override
	protected boolean hasError(int line) {
		return errHandler.hasError(line);
	}

	@Override
	protected String getLineComment(int line) {
		return errHandler.getLineComment(line);
	}

	@Override
	public void setText(String text) {
		if (!canCompile())
			text = LangUtil.translate("ui.bean.class.recompile.unsupported") + text;
		super.setText(text);
	}

	/**
	 * @param name
	 * 		Quantified class name to compile.
	 *
	 * @return Recompiled code.
	 */
	public byte[] save(String name) {
		if (!canCompile())
			throw new UnsupportedOperationException("Recompilation not supported in read-only mode");
		int version = ClassUtil.getVersion(resource.getClasses().get(name));
		JavacCompiler javac = new JavacCompiler();
		javac.setClassPath(getClassPath());
		javac.addUnit(name, getText());
		javac.options().lineNumbers = true;
		javac.options().variables = true;
		javac.options().sourceName = true;
		javac.options().setTarget(TargetVersion.fromClassMajor(version));
		javac.setCompileListener(errHandler);
		if (javac.compile())
			return javac.getUnitCode(name);
		else
			throw new IllegalStateException("Failed compile");
	}

	/**
	 * @return Classpath from workspace.
	 */
	private List<String> getClassPath() {
		List<String> path = new ArrayList<>();
		add(path, controller.getWorkspace().getPrimary());
		for (JavaResource resource : controller.getWorkspace().getLibraries())
			add(path, resource);
		return path;
	}

	/**
	 * @param path
	 * 		Classpath to build on.
	 * @param resource
	 * 		Resource to add.
	 */
	private void add(List<String> path, JavaResource resource) {
		if (resource instanceof FileSystemResource) {
			FileSystemResource fsr = (FileSystemResource) resource;
			path.add(fsr.getFile().getAbsolutePath());
		} else if (resource instanceof DeferringResource) {
			JavaResource deferred = ((DeferringResource) resource).getBacking();
			add(path, deferred);
		}
	}

	private boolean canCompile() {
		return codeArea.isEditable();
	}
}