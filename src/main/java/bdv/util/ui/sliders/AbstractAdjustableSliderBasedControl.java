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

import javax.swing.SpinnerNumberModel;
import javax.swing.SpinnerModel;
import javax.swing.JSpinner;
import javax.swing.JSlider;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;

/**
 * A common basis for {@code JSlider}-based controls whose sliding range
 * can be adjusted directly using the slider itself, with modifier key
 * and mouse drag. No additional (GUI) control is required to give a user
 * the possibility to adjust the slider range.
 *
 * Such slider, see {@link AbstractAdjustableSliderBasedControl()}, expects
 * a two informative {@code JLabel} items in which it echos the current
 * range boundaries, one {@code JLabel} for low/min bound and one for high/max.
 *
 * Furthermore, {@code JSpinner} is requested using which slider's current value
 * is explicitly expressed (visible to the user), and which can also control the
 * slider itself. To devise such {@code JSpinner}, that is especially compatible
 * with this slider, one can use {@link AbstractAdjustableSliderBasedControl#createAppropriateSpinnerModel(int)}.
 *
 * This slider's range adjusting happens by dragging a mouse horizontally over
 * either left-half of the slider area (to control low/min boundary) or right-half
 * (for high/max boundary). That said, mouse pointer's horizontal position is considered
 * and its range is translated into how much the respective boundary should change.
 * The mapping from mouse-move-delta to boundary-change-delta, essentially a "mouse
 * sensitivity profile", is controlled with the attribute {@link AbstractAdjustableSliderBasedControl#boundarySetter}
 * and {@link BoundaryValuesProvider}. Three "profiles" are predefined, namely linear,
 * quadratric and cubic (used by default). The later offer more "drastic" change of
 * the boundary as mouse is dragged further away; user first adjust magnitude-wise
 * the boundary, and then, in another dragging-session, fine-tunes it.
 *
 * @author Vladimir Ulman
 */
public abstract class AbstractAdjustableSliderBasedControl {

	/** represents the Control key, which is changeable and shared among all such controls
	 * (to warrant all are controlled the same way) */
	public static int CONTROL_KEY_keycode = 17;
	//TODO: fix the hardcoded ctrl key in EventHandler::mouseEntered()

	/** represents the left mouse button, which is changeable and shared among all such controls
	 * (to warrant all are controlled the same way) */
	public static int MOUSE_BUTTON_code = 1;
	//TODO: fix the hardcoded L mouse button in EventHandler::mouseEntered()

	/** an own mouse movement to boundary change "scaler", aka "mouse sensitivity
	 * profile"; this is intentionally available as of per-slider basis so that
	 * every instance (control GUI element) can exhibit different "sensitivity" */
	public BoundaryValuesProvider boundarySetter = BOUNDARY_SETTER_CUBE_FUN;

	// ================================= mouse move sensitivity setters =================================
	public interface BoundaryValuesProvider {
		int boundaryDeltaOnThisMouseMove(final int mouseDeltaInPx);
	}
	public final static BoundaryValuesProvider BOUNDARY_SETTER_IDENTITY_FUN = mouseDeltaInPx -> mouseDeltaInPx;
	public final static BoundaryValuesProvider BOUNDARY_SETTER_SQUARE_FUN = mouseDeltaInPx -> {
		float d = (float)mouseDeltaInPx/4.f;
		d *= d;
		return mouseDeltaInPx > 0 ? (int)d : (int)-d;
	};
	public final static BoundaryValuesProvider BOUNDARY_SETTER_CUBE_FUN = mouseDeltaInPx -> {
		float d = (float)mouseDeltaInPx/25.f;
		d *= d*d;
		return (int)d;
	};

	/**
	 * The same as {@link AbstractAdjustableSliderBasedControl#createAppropriateSpinnerModel(int, int)}
	 * but with a default step-size.
	 */
	public static SpinnerNumberModel createAppropriateSpinnerModel(int withThisCurrentValue) {
		return createAppropriateSpinnerModel(withThisCurrentValue, 20);
	}

	/**
	 * Helper factory method to eventually create a {@code Spinner} from the returned
	 * {@code SpinnerNumberModel} that can operate on the same range of values.
	 *
	 * @param withThisCurrentValue  initial value of the spinner
	 * @param withThisStep          step-size of the spinner
	 * @return                      spinner model appropriate for these sliders
	 */
	public static SpinnerNumberModel createAppropriateSpinnerModel(int withThisCurrentValue,
	                                                               int withThisStep) {
		return new SpinnerNumberModel(withThisCurrentValue, MIN_BOUND_LIMIT, MAX_BOUND_LIMIT, withThisStep);
	}

	// ================================= initialization =================================
	protected final JSlider slider;
	protected final JLabel lowBoundInfo;
	protected final JLabel highBoundInfo;
	protected final SpinnerNumberModel spinner;
	protected boolean isControlModeHighlighting = false;

	//internal shortcuts: maximum possible slider's range
	static final int MIN_BOUND_LIMIT = 0;
	static final int MAX_BOUND_LIMIT = 65535;
	static protected void checkAgainstBoundsOrThrow(final int checkedValue, final String semanticsMsg) {
		if (checkedValue < MIN_BOUND_LIMIT || checkedValue > MAX_BOUND_LIMIT)
			throw new IllegalArgumentException("Required "+semanticsMsg+" is outside the slider's maximum range.");
	}

	public AbstractAdjustableSliderBasedControl(final JSlider manageThisSlider,
	                                            final JSpinner associatedValueSpinner,
	                                            final JLabel associatedLowBoundLabel,
	                                            final JLabel associatedHighBoundLabel) {
		slider = manageThisSlider;
		lowBoundInfo = associatedLowBoundLabel;
		highBoundInfo = associatedHighBoundLabel;

		sliderBgInitialColor = slider.getBackground();
		lowBoundFgInitialColor = lowBoundInfo.getForeground();
		highBoundFgInitialColor = highBoundInfo.getForeground();

		final SpinnerModel m = associatedValueSpinner.getModel();
		if (! (m instanceof SpinnerNumberModel))
			throw new IllegalArgumentException("The provided spinner is expected to be of the type SpinnerNumberModel.");
		spinner = (SpinnerNumberModel)m; //NB: safe to cast...

		//add tooltip but only if there's none already
		if (slider.getToolTipText() == null) {
			slider.setToolTipText("FIRST press and hold Ctrl, and ONLY THEN press and hold left-mouse-button while dragging the mouse horizontally to adjust the sliding range.");
		}

		//listeners setup: make sure the slider follows values set in the associated spinner
		spinner.addChangeListener(l -> {
			int value = spinner.getNumber().intValue();
			//spinner may be set with arbitrary value, assure it's within slider's range
			value = Math.max(slider.getMinimum(), Math.min(value, slider.getMaximum()));
			slider.setValue(value);
			//if slider was actually a rangeSlider, it may have failed due to the upperValue;
			//in general, we check now if slider did what we asked it for and if not, it must
			//have had its reasons (would have invalidated its model), so we accept it and
			//learn where it ended up and synchronize (in the following row) to that value too
			if (slider.getValue() < value) value = slider.getValue();
			spinner.setValue(value); //make sense only if the original value was outside the slider's range
		});

		//listeners setup: forwarder to the associated spinner and also
		//to client listeners (for which it triggers only on truly relevant slider changes)
		slider.addChangeListener(event -> {
			//NB: assuming that slider value can never get outside slider's range (no tests here)
			spinner.setValue(slider.getValue());
			if (!isInControllingMode) tellListenersThatSliderHasChanged(event);
		});

		//listeners setup: managing slider's limits
		final EventHandler handler = new EventHandler();
		slider.addKeyListener(handler);
		slider.addMouseListener(handler);
		slider.addMouseMotionListener(handler);

		//when mouse cursor moves outside the area of the slider, the slider's
		//listeners are no longer triggered (until the cursor is back again);
		//but since the slider can become disabled (grayed-out) this way, and not
		//turned back (enabled) until the mouse comes back again, we listen
		//for mouse moves (using which it is possible monitor the ctrl modifier status)
		//in the largest enclosing area, by finding the furthest parent...
		Component c = slider;
		while (c.getParent() != null) c = c.getParent();
		c.addMouseMotionListener(handler);
	}

	public JSlider getSlider() {
		return slider;
	}

	/** only a shortcut to getSlider().getValue() */
	public int getValue() {
		return slider.getValue();
	}

	/**
	 * API to programmatically adjust the slider's range, the slider value is
	 * "squeezed in between" if need be
	 * @param lowBound    the new low/min boundary
	 * @param highBound   the new high/max boundary
	 */
	public void setSlidingRange(int lowBound, int highBound) {
		checkAgainstBoundsOrThrow(lowBound, "new MIN bound");
		checkAgainstBoundsOrThrow(highBound, "new MAX bound");
		if (lowBound > highBound)
			throw new IllegalArgumentException("MIN bound ("+lowBound
					+") shouldn't be greater than MAX bound ("+highBound+")");

		//keep now the future value of the slider
		final int value = Math.max(lowBound, Math.min(slider.getValue(), highBound));

		slider.setMinimum(lowBound);
		slider.setMaximum(highBound);
		lowBoundInfo.setText(String.valueOf(lowBound));
		highBoundInfo.setText(String.valueOf(highBound));

		slider.setValue(value); //to make sure the slider "has fixed" its value the way we wanted
	}

	// ================================= execution: managing slider thumbs =================================
	//only for derived classes...
	protected int originalSliderValue = -1; //aka before-dragging-value

	protected void storeSliderThumbsPositions() {
		originalSliderValue = slider.getValue();
	}
	protected void fixupSliderThumbsPositions() {
		//make sure that the slider is not unnecessarily changing its value while adjusting
		//its boundary, which may not be always possible (as the boundary is allowed to move
		//irrespective of what the slider value was)
		if (originalSliderValue < slider.getMinimum()) slider.setValue(slider.getMinimum());
		else if (originalSliderValue > slider.getMaximum()) slider.setValue(slider.getMaximum());
		else slider.setValue(originalSliderValue);
	}
	protected boolean didSliderThumbsChangedPositions() {
		return slider.getValue() != originalSliderValue;
	}

	// ================================= execution: internal state =================================
	private boolean isControlKeyPressed = false;
	private boolean isMouseLBpressed = false;
	private boolean isMouseOverSlider = false;
	private boolean isInControllingMode = false;
	private int initialMousePosition = 0;
	private int initialBoundaryValue = 0;
	private boolean isMinBoundaryControlled = false;

	public boolean isInControllingMode() {
		return isInControllingMode;
	}

	public void setControllingModeHighlight(boolean newValue) {
		isControlModeHighlighting = newValue;
	}
	public boolean getControllingModeHighlight() {
		return isControlModeHighlighting;
	}

	private final Color sliderBgInitialColor;
	private final Color lowBoundFgInitialColor;
	private final Color highBoundFgInitialColor;
	public static Color HIGHLIGHT_COLOR_SLIDER = new Color(255, 120, 120);
	public static Color HIGHLIGHT_COLOR_LABELS = new Color(255, 0, 0);

	private void highlightLowBound() {
		slider.setBackground(HIGHLIGHT_COLOR_SLIDER);
		lowBoundInfo.setForeground(HIGHLIGHT_COLOR_LABELS);
	}
	private void highlightHighBound() {
		slider.setBackground(HIGHLIGHT_COLOR_SLIDER);
		highBoundInfo.setForeground(HIGHLIGHT_COLOR_LABELS);
	}
	private void highlightNothing() {
		slider.setBackground(sliderBgInitialColor);
		lowBoundInfo.setForeground(lowBoundFgInitialColor);
		highBoundInfo.setForeground(highBoundFgInitialColor);
	}

	private boolean isSliderDisabled = false;
	private void disableSlider() {
		slider.setEnabled(false);
		isSliderDisabled = true;
	}
	private void enableSlider() {
		if (isSliderDisabled) {
			slider.setEnabled(true);
			isSliderDisabled = false;
			slider.grabFocus();
		}
	}
	private void enableSlider(final MouseEvent e) {
		enableSlider( (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) > 0 );
	}
	private void enableSlider(final boolean isCtrlPressed) {
		if (!isCtrlPressed) enableSlider();
	}

	// ================================= execution: events handling =================================

	/**
	 * Internal aggregated handler of various mouse and keyboard events
	 * using which the slider understands if it is in {@link AbstractAdjustableSliderBasedControl#isInControllingMode()}
	 * and whether it should be reading out mouse positions, modify its bounds etc.
	 */
	protected class EventHandler
	implements KeyListener, MouseListener, MouseMotionListener {
		@Override
		public void keyPressed(KeyEvent keyEvent) {
			if (keyEvent.getKeyCode() == CONTROL_KEY_keycode) {
				if (!isMouseLBpressed) {
					isControlKeyPressed = true;
					disableSlider();
				}
			}
		}

		@Override
		public void keyReleased(KeyEvent keyEvent) {
			if (keyEvent.getKeyCode() == CONTROL_KEY_keycode) {
				isControlKeyPressed = false;
				enableSlider();
				if (isInControllingMode) {
					if (isControlModeHighlighting) highlightNothing();
					tellListenersThatWeEndedAdjustingMode();
				}
				isInControllingMode = false;
			}
		}

		@Override
		public void mousePressed(MouseEvent mouseEvent) {
			if (mouseEvent.getButton() == MOUSE_BUTTON_code) {
				//update the ctrl key flag, but here it is not allowed to turn it on even when ctrl is pressed
				isControlKeyPressed &= (mouseEvent.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) > 0;
				isMouseLBpressed = true;
				if (isControlKeyPressed) {
					isInControllingMode = true;

					//store the initial state now--at the beginning of the dragging
					initialMousePosition = mouseEvent.getXOnScreen();
					isMinBoundaryControlled = ((float) mouseEvent.getX() / (float) slider.getWidth()) < 0.5f;
					initialBoundaryValue = isMinBoundaryControlled ? slider.getMinimum() : slider.getMaximum();
					storeSliderThumbsPositions();

					if (isControlModeHighlighting) {
						if (isMinBoundaryControlled) highlightLowBound(); else highlightHighBound();
					}
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent mouseEvent) {
			enableSlider(mouseEvent);
			if (mouseEvent.getButton() == MOUSE_BUTTON_code) {
				isMouseLBpressed = false;
				if (isInControllingMode) {
					if (isControlModeHighlighting) highlightNothing();
					tellListenersThatWeEndedAdjustingMode();
				}
				isInControllingMode = false;
			}
		}

		@Override
		public void mouseDragged(MouseEvent mouseEvent) {
			//during the dragging, the mouse may get outside the elem in which case
			//the elem is no longer able to monitor is ctrl status, so we have to
			//take care of it explicitly here
			isControlKeyPressed &= (mouseEvent.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) > 0;
			if (!isControlKeyPressed) {
				enableSlider();
				if (isInControllingMode) {
					if (isControlModeHighlighting) highlightNothing();
					tellListenersThatWeEndedAdjustingMode();
				}
				isInControllingMode = false;
			}

			if (isInControllingMode) {
				int deltaMove = mouseEvent.getXOnScreen() - initialMousePosition;
				int newSliderValue = initialBoundaryValue + boundarySetter.boundaryDeltaOnThisMouseMove(deltaMove);
				if (isMinBoundaryControlled) {
					//make sure the value is within the min model limits,
					newSliderValue = Math.max(MIN_BOUND_LIMIT, Math.min(newSliderValue, MAX_BOUND_LIMIT));
					//and set min only if it is not beyond (greater than) the max boundary
					if (newSliderValue < slider.getMaximum()) {
						slider.setMinimum(newSliderValue);
						lowBoundInfo.setText(String.valueOf(newSliderValue));
					}
				} else {
					//right part
					//make sure the value is within the max model limits,
					newSliderValue = Math.max(MIN_BOUND_LIMIT, Math.min(newSliderValue, MAX_BOUND_LIMIT));
					//and set max only if it is not beyond (lesser than) the min boundary
					if (newSliderValue > slider.getMinimum()) {
						slider.setMaximum(newSliderValue);
						highBoundInfo.setText(String.valueOf(newSliderValue));
					}
				}
				fixupSliderThumbsPositions();
			}
		}

		@Override
		public void mouseEntered(MouseEvent mouseEvent) {
			//during the mouse dragging, we might have gotten out of the slider area;
			//when outside, the keyboard and mouse buttons change might have changed but
			//this object is not aware of it (as its listeners couldn't be triggered);
			//
			//now, when the mouse pointer is coming back, we have to reset the statuses
			isControlKeyPressed = (mouseEvent.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) > 0;
			enableSlider(isControlKeyPressed);
			isMouseLBpressed = (mouseEvent.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) > 0;
			final boolean shouldBeInControllingMode = isControlKeyPressed && isMouseLBpressed;
			if (isInControllingMode && !shouldBeInControllingMode) {
				//conditions no longer satisfied to continue in the controlling mode, so
				//"exit sequence" is here; note that no similar check is here to enable
				//the controlling mode 'cause this is not how the mode should be started
				if (isControlModeHighlighting) highlightNothing();
				tellListenersThatWeEndedAdjustingMode();
			}
			isInControllingMode &= shouldBeInControllingMode;
		}

		@Override
		public void keyTyped(KeyEvent keyEvent) { /* intentionally empty */ }

		@Override
		public void mouseClicked(MouseEvent mouseEvent) { /* intentionally empty */ }

		@Override
		public void mouseExited(MouseEvent mouseEvent) { /* intentionally empty */ }

		@Override
		public void mouseMoved(MouseEvent mouseEvent) {
			//update the ctrl key flag, but here it is not allowed to turn it on even when ctrl is pressed
			isControlKeyPressed &= (mouseEvent.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) > 0;
			enableSlider(isControlKeyPressed);
			isMouseOverSlider =
					mouseEvent.getX() > 0 && mouseEvent.getX() < slider.getWidth()
					&& mouseEvent.getY() > 0 && mouseEvent.getY() < slider.getHeight();
		}
	}

	// ================================= execution: listeners =================================
	protected final java.util.List<ChangeListener> listeners = new ArrayList<>(10);

	public void addChangeListener(final ChangeListener listener) {
		listeners.add(listener);
	}

	public void removeChangeListener(final ChangeListener listener) {
		listeners.remove(listener);
	}

	protected void tellListenersThatSliderHasChanged(final ChangeEvent event) {
		listeners.forEach(listener -> listener.stateChanged(event));
	}

	protected void tellListenersThatWeEndedAdjustingMode() {
		//...but only when we really have changed the value before and after the adjustment
		if (didSliderThumbsChangedPositions()) {
			tellListenersThatSliderHasChanged(new ChangeEvent(slider));
		}
	}
}
