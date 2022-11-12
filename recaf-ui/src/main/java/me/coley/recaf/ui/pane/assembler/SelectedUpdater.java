package me.coley.recaf.ui.pane.assembler;

import javafx.beans.value.ObservableValue;
import me.coley.recaf.assemble.ast.Element;
import me.coley.recaf.assemble.ast.Unit;
import me.coley.recaf.assemble.ast.arch.AbstractDefinition;
import me.coley.recaf.assemble.pipeline.AssemblerPipeline;
import me.coley.recaf.util.logging.DebuggingLogger;
import me.coley.recaf.util.logging.Logging;

/**
 * @author Nowilltolife
 */
public class SelectedUpdater {
	private static final DebuggingLogger logger = Logging.get(SelectedUpdater.class);
	private final AssemblerPipeline pipeline;

	/**
	 * @param pipeline
	 * 		Assembler pipeline.
	 */
	public SelectedUpdater(AssemblerPipeline pipeline) {
		this.pipeline = pipeline;
	}

	/**
	 * @param caretObserver
	 * 		Updatable caret position.
	 */
	public void addCaretPositionListener(ObservableValue<Integer> caretObserver) {
		caretObserver.addListener((observable, priorIndex, currentIndex) -> {
			Unit unit = pipeline.getUnit();
			if (unit == null)
				return;
			if (!unit.isClass())
				return;

			Element child = unit.getDefinition().getChildAt(currentIndex);
			if (child == null)
				return;
			if (child instanceof AbstractDefinition) {
				AbstractDefinition childDefinition = (AbstractDefinition) child;
				// Finally, update the child
				logger.debugging(l -> l.info("Selected child definition in class: {} {}",
						childDefinition.getName(), childDefinition.getDesc()));
				unit.getDefinitionAsClass().setCurrentDefinition(childDefinition);
			}
		});
	}
}