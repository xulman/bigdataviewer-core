package bdv.util.ui.sliders;

import bdv.ui.rangeslider.RangeSlider;

import javax.swing.*;
import java.awt.*;

public class AdjustableBoundsRangeSlider extends AbstractAdjustableSliderBasedControl {
	protected final RangeSlider rangeSlider;
	protected final SpinnerNumberModel highSpinner;

	public AdjustableBoundsRangeSlider(final RangeSlider manageThisSlider,
	                                   final JSpinner associatedLowValueSpinner,
	                                   final JSpinner associatedHighValueSpinner,
	                                   final JLabel associatedLowBound,
	                                   final JLabel associatedHighBound) {
		super(manageThisSlider, associatedLowValueSpinner, associatedLowBound, associatedHighBound);
		this.rangeSlider = manageThisSlider;

		final SpinnerModel m = associatedHighValueSpinner.getModel();
		if (! (m instanceof SpinnerNumberModel))
			throw new IllegalArgumentException("The provided spinner for high-value is expected to be of the type SpinnerNumberModel.");
		highSpinner = (SpinnerNumberModel)m; //NB: safe to cast...

		//listeners setup: make sure the slider follows values set in the associated spinner
		highSpinner.addChangeListener(l -> {
			int value = highSpinner.getNumber().intValue();
			//spinner may be set with arbitrary value, assure it's within slider's range
			value = Math.max(rangeSlider.getMinimum(), Math.min(value, rangeSlider.getMaximum()));
			rangeSlider.setUpperValue(value);
			//was it blocked by the lowerValue?
			if (rangeSlider.getUpperValue() > value) value = rangeSlider.getUpperValue();
			highSpinner.setValue(value); //make sense only if the original value was outside the slider's range
		});

		//listeners setup: forwarder also to the associated high-value spinner
		rangeSlider.addChangeListener(event -> {
			highSpinner.setValue(rangeSlider.getUpperValue());
		});
	}

	public RangeSlider getRangeSlider() {
		return rangeSlider;
	}

	public int getUpperValue() {
		return rangeSlider.getUpperValue();
	}

	// ================================= execution: managing slider thumbs =================================
	protected int originalSliderUpperValue = -1; //aka before-dragging-value
	@Override
	protected void storeSliderThumbsPositions() {
		super.storeSliderThumbsPositions();
		originalSliderUpperValue = rangeSlider.getUpperValue();
	}
	@Override
	protected void fixupSliderThumbsPositions() {
		super.fixupSliderThumbsPositions();
		if (originalSliderUpperValue < rangeSlider.getMinimum()) rangeSlider.setUpperValue(rangeSlider.getMinimum());
		else if (originalSliderUpperValue > rangeSlider.getMaximum()) rangeSlider.setUpperValue(rangeSlider.getMaximum());
		else rangeSlider.setUpperValue(originalSliderUpperValue);
	}
	@Override
	protected boolean didSliderThumbsChangedPositions() {
		boolean lowChanged = super.didSliderThumbsChangedPositions();
		boolean highChanged = rangeSlider.getUpperValue() != originalSliderUpperValue;
		return lowChanged || highChanged;
	}

	// ================================= convenience builder with GUI arrangement =================================
	public static AdjustableBoundsRangeSlider createAndPlaceHere(final Container intoThisComponent,
	                                                             final int initialLowValue,
	                                                             final int initialHighValue,
	                                                             final int initialLowBoundary,
	                                                             final int initialHighBoundary) {
		checkAgainstBoundsOrThrow(initialLowBoundary, "MIN bound");
		checkAgainstBoundsOrThrow(initialHighBoundary, "MAX bound");
		if (initialLowValue < initialLowBoundary || initialLowValue > initialHighBoundary)
			throw new IllegalArgumentException("Refuse to create slider showing \"low\" value that's outside the slider's min and max range.");
		if (initialHighValue < initialLowBoundary || initialHighValue > initialHighBoundary)
			throw new IllegalArgumentException("Refuse to create slider showing \"high\" value that's outside the slider's min and max range.");

		final GridBagLayout gridBagLayout = new GridBagLayout();
		intoThisComponent.setLayout( gridBagLayout );

		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		final Insets defaultInset = c.insets;

		//set to the current wanted range
		RangeSlider slider = new RangeSlider(initialLowBoundary, initialHighBoundary);
		slider.setValue(initialLowValue);
		slider.setUpperValue(initialHighValue);
		//
		JSpinner lowSpinner = new JSpinner(
				AbstractAdjustableSliderBasedControl.createAppropriateSpinnerModel(initialLowValue) );
		JSpinner highSpinner = new JSpinner(
				AbstractAdjustableSliderBasedControl.createAppropriateSpinnerModel(initialHighValue) );
		//
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
		intoThisComponent.add(lowSpinner, c);
		c.weightx = 0.85;
		c.gridx = 1;
		c.insets = new Insets(defaultInset.top, 5, defaultInset.bottom, 5);
		intoThisComponent.add(slider, c);
		c.insets = defaultInset;
		c.weightx = 0.05;
		c.gridx = 2;
		intoThisComponent.add(highSpinner, c);
		c.gridheight = 1;
		c.gridx = 3;
		c.insets = new Insets(defaultInset.top, 5, defaultInset.bottom, defaultInset.right);
		intoThisComponent.add(highBoundInformer, c);
		c.gridy = 1;
		intoThisComponent.add(lowBoundInformer, c);

		return new AdjustableBoundsRangeSlider(slider,lowSpinner,highSpinner,lowBoundInformer,highBoundInformer);
	}
}
