package org.olafneumann.trainer.ui;

import java.text.CollationElementIterator;
import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.Locale;

import javax.swing.RowFilter;

import org.olafneumann.trainer.data.TrainerItem;
import org.olafneumann.trainer.data.TrainerModelInput;

class CollatorTrainerItemFilter extends RowFilter<ListTableModel<TrainerItem>, TrainerItem> {
	private final RuleBasedCollator[] collators;
	private final String text;
	private final TrainerModelInput[] inputs;

	public CollatorTrainerItemFilter(TrainerModelInput[] inputs, String text) {
		this.text = text;
		this.inputs = inputs;
		collators = new RuleBasedCollator[inputs.length];
		for (int i = 0; i < collators.length; ++i) {
			collators[i] = (RuleBasedCollator) Collator.getInstance(inputs[i].getLocale());
			collators[i].setStrength(Collator.SECONDARY);
		}
	}

	@Override
	public boolean include(Entry<? extends ListTableModel<TrainerItem>, ? extends TrainerItem> entry) {
		TrainerItem item = (TrainerItem) entry.getValue(0);
		String[] values = item.getValues();
		for (int index = 0; index < inputs.length; ++index) {
			if (contains(values[index], text, inputs[index].getLocale())) {
				return true;
			}
		}
		boolean allEmpty = true;
		for (String value : values) {
			allEmpty &= value.trim().length() == 0;
		}
		return allEmpty;
	}

	/**
	 * Test whether one string contains another
	 * 
	 * @param s1
	 *            the containing string
	 * @param s2
	 *            the contained string
	 * @return true iff s1 contains s2
	 */
	private boolean contains(String string1, String string2, Locale locale) {
		return collationContains(getCollationElementIterator(string1, locale),
				getCollationElementIterator(string2, locale), null, false);
	}

	private CollationElementIterator getCollationElementIterator(String string, Locale locale) {
		RuleBasedCollator collator = (RuleBasedCollator) Collator.getInstance(locale);
		collator.setStrength(Collator.PRIMARY);
		return (collator).getCollationElementIterator(string);
	}

	/**
	 * Determine whether one string contains another, under the terms of a given
	 * collating sequence. If operation=ENDSWITH, the match must be at the end
	 * of the first string.
	 * 
	 * @param s0
	 *            iterator over the collation elements of the containing string
	 * @param s1
	 *            iterator over the collation elements of the contained string
	 * @param offsets
	 *            may be null, but if it is supplied, it must be an array of two
	 *            integers which, if the function returns true, will contain the
	 *            start position of the first matching substring, and the offset
	 *            of the first character after the first matching substring.
	 *            This is not available for operation=endswith
	 * @return true if the first string contains the second
	 */
	private boolean collationContains(CollationElementIterator s0, CollationElementIterator s1, int[] offsets,
			boolean matchAtEnd) {
		int e0, e1;
		do {
			e1 = s1.next();
		} while (e1 == 0);
		if (e1 == -1) {
			return true;
		}
		e0 = -1;
		while (true) {
			// scan the first string to find a matching character
			while (e0 != e1) {
				do {
					e0 = s0.next();
				} while (e0 == 0);
				if (e0 == -1) {
					// hit the end, no match
					return false;
				}
			}
			// matched first character, note the position of the possible match
			int start = s0.getOffset();
			if (collationStartsWith(s0, s1)) {
				if (matchAtEnd) {
					do {
						e0 = s0.next();
					} while (e0 == 0);
					if (e0 == -1) {
						// the match is at the end
						return true;
					}
					// else ignore this match and keep looking
				} else {
					if (offsets != null) {
						offsets[0] = start - 1;
						offsets[1] = s0.getOffset();
					}
					return true;
				}
			}
			// reset the position and try again
			s0.setOffset(start);

			// workaround for a difference between JDK 1.4.0 and JDK 1.4.1
			if (s0.getOffset() != start) {
				// JDK 1.4.0 takes this path
				s0.next();
			}
			s1.reset();
			e0 = -1;
			do {
				e1 = s1.next();
			} while (e1 == 0);
			// loop round to try again
		}
	}

	private boolean collationStartsWith(CollationElementIterator s0, CollationElementIterator s1) {
		while (true) {
			int e0, e1;
			do {
				e1 = s1.next();
			} while (e1 == 0);
			if (e1 == -1) {
				return true;
			}
			do {
				e0 = s0.next();
			} while (e0 == 0);
			if (e0 != e1) {
				return false;
			}
		}
	}
}
