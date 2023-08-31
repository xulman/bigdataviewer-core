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
package bdv.util.ui;

import bdv.ui.rangeslider.RangeSlider;
import bdv.util.ui.sliders.AbstractAdjustableSliderBasedControl;
import bdv.util.ui.sliders.AdjustableBoundsSlider;
import bdv.util.ui.sliders.AdjustableBoundsRangeSlider;
import javax.swing.*;
import java.awt.*;

public class DemoSliders {

	public static final int MINBOUND = 0;
	public static final int MAXBOUND = 500;
	public static final int LOWVALUE = 30;
	public static final int HIGHVALUE = 170;

	public static AdjustableBoundsSlider sliderTestPanel(final JPanel frame) {
		final GridBagLayout gridBagLayout = new GridBagLayout();
		frame.setLayout( gridBagLayout );

		final GridBagConstraints c = new GridBagConstraints();
		c.weighty = 0.3;
		c.fill = GridBagConstraints.BOTH;

		//row
		c.gridy = 0;
		c.gridx = 0;
		c.weightx = 0.1;
		frame.add( new JButton("no op"), c);
		c.gridx = 1;
		c.weightx = 0.8;
		frame.add( new JButton("no op"), c);
		c.gridx = 2;
		c.weightx = 0.1;
		frame.add( new JButton("no op"), c);

		//row
		c.weightx = 0;
		c.gridy = 1;
		c.gridx = 0;
		frame.add( new JButton("no op"), c);
		//x=1 is missing for now
		c.gridx = 2;
		frame.add( new JButton("no op"), c);

		//row
		c.gridy = 2;
		c.gridx = 0;
		frame.add( new JButton("no op"), c);
		//x=1 is missing for now
		c.gridx = 2;
		frame.add( new JButton("no op"), c);

		final JPanel placeHolder = new JPanel();
		c.gridy = 1;
		c.gridx = 1;
		frame.add(placeHolder, c);
		//
		AdjustableBoundsSlider slider
				= AdjustableBoundsSlider.createAndPlaceHere(placeHolder, LOWVALUE, MINBOUND,MAXBOUND);

		final JLabel msg = new JLabel("Current slider value: "+slider.getValue());
		c.gridy = 2;
		c.gridx = 1;
		frame.add(msg, c);
		//
		slider.addChangeListener(l -> {
			msg.setText("Current slider value: "+slider.getValue());
			System.out.print('.');
		});

		return slider;
	}


	public static AdjustableBoundsRangeSlider rangeSliderTestPanel_ownLayoutOfControls(final JPanel frame) {
		final GridBagLayout gridBagLayout = new GridBagLayout();
		frame.setLayout( gridBagLayout );

		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;

		//set to the current wanted range
		RangeSlider slider = new RangeSlider(MINBOUND,MAXBOUND);
		slider.setValue(LOWVALUE);
		slider.setUpperValue(HIGHVALUE);

		JSpinner lowValueSpinner = new JSpinner(
				AbstractAdjustableSliderBasedControl.createAppropriateSpinnerModel(LOWVALUE) );
		JSpinner highValueSpinner = new JSpinner(
				AbstractAdjustableSliderBasedControl.createAppropriateSpinnerModel(HIGHVALUE) );

		JLabel minBoundInfo = new JLabel(String.valueOf( slider.getMinimum() ));
		JLabel maxBoundInfo = new JLabel(String.valueOf( slider.getMaximum() ));

		c.insets = new Insets(5,10,5,10);
		c.gridy = 0;
		c.weightx = 0.05;
		c.gridx = 0;
		frame.add(minBoundInfo, c);
		c.weightx = 0.1;
		c.gridx = 1;
		frame.add(lowValueSpinner, c);
		c.weightx = 0.7;
		c.gridx = 2;
		frame.add(new JLabel("  <------------>  "), c);
		c.weightx = 0.1;
		c.gridx = 3;
		frame.add(highValueSpinner, c);
		c.weightx = 0.05;
		c.gridx = 4;
		frame.add(maxBoundInfo, c);

		c.gridy = 1;
		c.weightx = 0.9;
		c.gridx = 0;
		c.gridwidth = 5;
		frame.add(slider, c);

		AdjustableBoundsRangeSlider ctrl = new AdjustableBoundsRangeSlider(slider,
				lowValueSpinner, highValueSpinner,
				minBoundInfo, maxBoundInfo );

		JLabel msg = new JLabel("Values are "+slider.getValue()+" and "+slider.getUpperValue());
		ctrl.addChangeListener(l -> msg.setText("Values are "+slider.getValue()+" and "+slider.getUpperValue()) );
		//
		c.gridy = 2;
		c.gridx = 0;
		c.gridwidth = 3;
		c.weightx = 0.2;
		frame.add(msg, c);

		return ctrl;
	}


	public static AdjustableBoundsRangeSlider rangeSliderTestPanel_defaultLayoutOfControls(final JPanel frame) {
		final GridBagLayout gridBagLayout = new GridBagLayout();
		frame.setLayout( gridBagLayout );

		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.1;

		c.insets = new Insets(5,10,5,10);
		c.gridy = 0;
		c.gridx = 0;
		JPanel placeHolder = new JPanel();
		frame.add(placeHolder, c);

		AdjustableBoundsRangeSlider ctrl = AdjustableBoundsRangeSlider
				.createAndPlaceHere(placeHolder, LOWVALUE,HIGHVALUE, MINBOUND,MAXBOUND);

		c.gridy = 1;
		c.gridx = 0;
		JLabel msg = new JLabel("Values are "+ctrl.getValue()+" and "+ctrl.getUpperValue());
		ctrl.addChangeListener(l -> msg.setText("Values are "+ctrl.getValue()+" and "+ctrl.getUpperValue()) );
		frame.add(msg, c);

		return ctrl;
	}


	public static void main(String[] args) {
		JFrame frame = new JFrame("Adjustable Sliders Demo");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Container c = frame.getContentPane();

		c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));

		JPanel panel = new JPanel();
		AdjustableBoundsSlider sliderA = sliderTestPanel(panel);
		c.add(panel);

		c.add(new JSeparator());
		c.add(new JSeparator());
		c.add(new JSeparator());

		panel = new JPanel();
		AdjustableBoundsRangeSlider sliderB = rangeSliderTestPanel_ownLayoutOfControls(panel);
		c.add(panel);

		c.add(new JSeparator());
		c.add(new JSeparator());
		c.add(new JSeparator());

		panel = new JPanel();
		AdjustableBoundsRangeSlider sliderC = rangeSliderTestPanel_defaultLayoutOfControls(panel);
		c.add(panel);

		c.add(new JSeparator());
		c.add(new JSeparator());
		c.add(new JSeparator());

		panel = new JPanel();
		JCheckBox highlight = new JCheckBox("Enable visual aid", true);
		highlight.setBackground(AbstractAdjustableSliderBasedControl.HIGHLIGHT_COLOR_SLIDER);
		JSpinner left = new JSpinner(new SpinnerNumberModel(0, 0,65000, 100));
		JSpinner right = new JSpinner(new SpinnerNumberModel(65000, 0,65000, 100));
		JButton button = new JButton("Set these sliding ranges in all sliders");
		panel.add(highlight, BorderLayout.LINE_START);
		panel.add(left);
		panel.add(button, BorderLayout.CENTER);
		panel.add(right, BorderLayout.LINE_END);
		c.add(panel);

		//sync with the initial state of the checkbox
		sliderA.setControllingModeHighlight(true);
		sliderB.setControllingModeHighlight(true);
		sliderC.setControllingModeHighlight(true);

		highlight.addActionListener(l -> {
			final boolean newState = highlight.isSelected();
			sliderA.setControllingModeHighlight(newState);
			sliderB.setControllingModeHighlight(newState);
			sliderC.setControllingModeHighlight(newState);
		});

		button.addActionListener(l -> {
			int minBound = (int)left.getValue();
			int maxBound = (int)right.getValue();
			System.out.println("Setting min-max to "+minBound+" <-> "+maxBound);

			sliderA.setSlidingRange(minBound,maxBound);
			sliderB.setSlidingRange(minBound,maxBound);
			sliderC.setSlidingRange(minBound,maxBound);
		});

		frame.pack();
		frame.setVisible(true);
	}
}
