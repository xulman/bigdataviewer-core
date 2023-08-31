package bdv.util.ui.sliders;

import javax.swing.*;
import java.awt.*;

public class AdjustableBoundsSlider extends AbstractAdjustableSliderBasedControl {
	public AdjustableBoundsSlider(final JSlider manageThisSlider,
	                              final JSpinner associatedValueSpinner,
	                              final JLabel associatedLowBound,
	                              final JLabel associatedHighBound) {
		super(manageThisSlider, associatedValueSpinner, associatedLowBound, associatedHighBound);
	}

	// ================================= convenience builder with GUI arrangement =================================
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
