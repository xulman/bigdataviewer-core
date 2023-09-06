/*-
 * #%L
 * BigDataViewer core classes with minimal dependencies.
 * %%
 * Copyright (C) 2012 - 2023 BigDataViewer developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package bdv.util.ui.sliders;

import javax.swing.UIManager;
import javax.swing.JSpinner;
import javax.swing.JSlider;
import javax.swing.JLabel;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Font;

/**
 * The implementation of the single-thumb (plain, simple, normal) horizontal slider
 * with associated spinner and two informative labels that, besides linking these
 * four GUI elements and governing their co-operation, especially allows to modify
 * the slider's range using the slider itself (with keyboard modifier key and
 * mouse dragging). {@link AbstractAdjustableSliderBasedControl Read further here.}
 *
 * @author Vladimir Ulman
 */
public class AdjustableBoundsSlider extends AbstractAdjustableSliderBasedControl {
	public AdjustableBoundsSlider(final JSlider manageThisSlider,
	                              final JSpinner associatedValueSpinner,
	                              final JLabel associatedLowBound,
	                              final JLabel associatedHighBound) {
		super(manageThisSlider, associatedValueSpinner, associatedLowBound, associatedHighBound);
	}

	// ================================= convenience builder with GUI arrangement =================================

	/**
	 * Creates, places and layouts the relevant controls to into the given component,
	 * and returns
	 * @param intoThisComponent    a Swing component into which the slider's ensemble is placed
	 * @param initialValue         position the slider's thumb to this value
	 * @param initialLowBoundary   use this low/min boundary (slider's range)
	 * @param initialHighBoundary  use this high/max boundary (slider's range)
	 * @return                     the controlling object to which one hook up listeners
	 */
	public static AdjustableBoundsSlider createAndPlaceHere(final Container intoThisComponent,
	                                                        final int initialValue,
	                                                        final int initialLowBoundary,
	                                                        final int initialHighBoundary) {
		checkAgainstBoundsOrThrow(initialLowBoundary, "MIN bound");
		checkAgainstBoundsOrThrow(initialHighBoundary, "MAX bound");
		if (initialValue < initialLowBoundary || initialValue > initialHighBoundary)
			throw new IllegalArgumentException("Refuse to create slider showing value that's outside the slider's min and max range.");

		final GridBagLayout gridBagLayout = new GridBagLayout();
		intoThisComponent.setLayout( gridBagLayout );

		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		final Insets defaultInset = c.insets;

		//set to the current wanted range
		JSlider slider = new JSlider(JSlider.HORIZONTAL, initialLowBoundary, initialHighBoundary, initialValue);
		JSpinner spinner = new JSpinner(
				AbstractAdjustableSliderBasedControl.createAppropriateSpinnerModel(initialValue) );
		JLabel lowBoundInformer = new JLabel(String.valueOf(initialLowBoundary));
		JLabel highBoundInformer = new JLabel(String.valueOf(initialHighBoundary));

		//from bigdataviewer-core/src/main/java/bdv/ui/convertersetupeditor/BoundedRangePanel.java,
		//method updateBoundLabelFonts(), L283
		final Font labelFont = UIManager.getFont( "Label.font" );
		final Font font = new Font( labelFont.getName(), labelFont.getStyle(), 10 );
		lowBoundInformer.setFont( font );
		highBoundInformer.setFont( font );

		c.gridheight = 2;
		c.gridy = 0;
		c.weightx = 0.05;
		c.gridx = 0;
		intoThisComponent.add(spinner, c);
		c.weightx = 0.9;
		c.gridx = 1;
		c.insets = new Insets(defaultInset.top, 5, defaultInset.bottom, 5);
		intoThisComponent.add(slider, c);
		c.insets = defaultInset;
		c.gridheight = 1;
		c.weightx = 0.05;
		c.gridx = 2;
		c.insets = new Insets(defaultInset.top, 5, defaultInset.bottom, defaultInset.right);
		intoThisComponent.add(highBoundInformer, c);
		c.gridy = 1;
		intoThisComponent.add(lowBoundInformer, c);

		return new AdjustableBoundsSlider(slider,spinner,lowBoundInformer,highBoundInformer);
	}
}
