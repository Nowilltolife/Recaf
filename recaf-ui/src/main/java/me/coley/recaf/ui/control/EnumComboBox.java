package me.coley.recaf.ui.control;

import javafx.beans.binding.ObjectBinding;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;
import me.coley.recaf.ui.util.Lang;
import me.coley.recaf.util.Translatable;

/**
 * ComboBox for enumeration types.
 *
 * @param <E>
 * 		Enumeration type.
 *
 * @author Matt
 */
public class EnumComboBox<E extends Enum<?>> extends ComboBox<E> {
	/**
	 * @param type
	 * 		Enumeration type.
	 * @param initial
	 * 		Initial selection.
	 */
	public EnumComboBox(Class<E> type, E initial) {
		super(FXCollections.observableArrayList(type.getEnumConstants()));
		setValue(initial);
		if (Translatable.class.isAssignableFrom(type)) {
			converterProperty().bind(new ObjectBinding<>() {
				{
					bind(Lang.languageProperty());
				}

				@Override
				protected StringConverter<E> computeValue() {
					return new StringConverter<>() {
						@Override
						public String toString(E object) {
							return Lang.get(((Translatable) object).getTranslationKey());
						}

						@Override
						public E fromString(String string) {
							throw new UnsupportedOperationException("Not implemented");
						}
					};
				}
			});
		}
	}
}