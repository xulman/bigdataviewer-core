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
package bdv.ui.convertersetupeditor;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import java.util.function.Supplier;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

import net.miginfocom.swing.MigLayout;

import org.scijava.listeners.Listeners;

import bdv.ui.UIUtils;
import bdv.ui.rangeslider.RangeSlider;
import bdv.util.ui.sliders.AdjustableBoundsRangeSlider;
import bdv.util.BoundedRange;

/**
 * A {@code JPanel} with a range slider, min/max spinners, and a range bounds
 * display (for setting {@code ConverterSetup} display range).
 *
 * @author Tobias Pietzsch
 */
class BoundedRangePanel extends JPanel
{
	private Supplier< JPopupMenu > popup;

	public interface ChangeListener
	{
		void boundedRangeChanged();
	}

	private double lowValue,highValue;
	private double lowBound,highBound;

	/**
	 * The range slider.
	 */
	private final RangeSlider originalRangeSlider;
	private final AdjustableBoundsRangeSlider rangeSlider;

	/**
	 * The minimum spinner.
	 */
	private final JSpinner minSpinner;

	/**
	 * The maximum spinner.
	 */
	private final JSpinner maxSpinner;

	private final JLabel upperBoundLabel;

	private final JLabel lowerBoundLabel;

	private final Listeners.List< ChangeListener > listeners = new Listeners.SynchronizedList<>();

	/**
	 * Whether the range reflects a set of sources all having the same range
	 */
	private boolean isConsistent = true;

	/**
	 * Panel background if range reflects a set of sources all having the same range
	 */
	private Color consistentBg = Color.WHITE;

	/**
	 * Panel background if range reflects a set of sources with different ranges
	 */
	private Color inConsistentBg = Color.WHITE;

	public BoundedRangePanel()
	{
		this( new BoundedRange( 0, 1, 0, 0.5 ) );
	}

	public BoundedRangePanel( final BoundedRange range )
	{
		setLayout( new MigLayout( "ins 5 5 5 10, fillx, filly, hidemode 3", "[][grow][][]", "[]0[]" ) );
		updateColors();

		minSpinner = new JSpinner( new SpinnerNumberModel( 0.0, 0.0, 65535.0, 5.0 ) );
		maxSpinner = new JSpinner( new SpinnerNumberModel( 65535.0, 0.0, 65535.0, 5.0 ) );
		originalRangeSlider = new RangeSlider( 0, (int)Math.floor(range.getMaxBound()-range.getMinBound()) );
		upperBoundLabel = new JLabel();
		lowerBoundLabel = new JLabel();
		rangeSlider = new AdjustableBoundsRangeSlider(originalRangeSlider, minSpinner,maxSpinner, lowerBoundLabel,upperBoundLabel);

		setupMinSpinner();
		setupMaxSpinner();
		setupRangeSlider();
		setupBoundLabels();
		setupPopupMenu();

		this.add( minSpinner, "sy 2" );
		this.add( originalRangeSlider, "growx, sy 2" );
		this.add( maxSpinner, "sy 2" );
		this.add( upperBoundLabel, "right, wrap" );
		this.add( lowerBoundLabel, "right" );

		updateThisFromRange( range );
		setRange( range );
	}

	private void updateThisFromRange(final BoundedRange range) {
		lowValue = range.getMin();
		highValue = range.getMax();
		lowBound = range.getMinBound();
		highBound = range.getMaxBound();
	}

	@Override
	public void setEnabled( final boolean enabled )
	{
		super.setEnabled( enabled );
		if ( minSpinner != null )
			minSpinner.setEnabled( enabled );
		if ( originalRangeSlider != null )
			originalRangeSlider.setEnabled( enabled );
		if ( maxSpinner != null )
			maxSpinner.setEnabled( enabled );
		if ( upperBoundLabel != null )
			upperBoundLabel.setEnabled( enabled );
		if ( lowerBoundLabel != null )
			lowerBoundLabel.setEnabled( enabled );
	}

	@Override
	public void updateUI()
	{
		super.updateUI();
		updateColors();
		if ( !isConsistent )
			setBackground( inConsistentBg );
		if ( popup != null )
		{
			final JPopupMenu menu = popup.get();
			if ( menu != null )
				SwingUtilities.updateComponentTreeUI( menu );
		}
		if ( upperBoundLabel != null )
			updateBoundLabelFonts();
	}

	private void updateColors()
	{
		consistentBg = UIManager.getColor( "Panel.background" );
		inConsistentBg = UIUtils.mix( consistentBg, Color.red, 0.9 );
	}

	public void setConsistent( final boolean isConsistent )
	{
		this.isConsistent = isConsistent;
		setBackground( isConsistent ? consistentBg : inConsistentBg );
	}

	@Override
	public void setBackground( final Color bg )
	{
		super.setBackground( bg );
		if ( minSpinner != null )
			minSpinner.setBackground( bg );
		if ( originalRangeSlider != null )
			originalRangeSlider.setBackground( bg );
		if ( maxSpinner != null )
			maxSpinner.setBackground( bg );
		if ( upperBoundLabel != null )
			upperBoundLabel.setBackground( bg );
		if ( lowerBoundLabel != null )
			lowerBoundLabel.setBackground( bg );
	}

	private static class UnboundedNumberEditor extends JSpinner.NumberEditor
	{
		public UnboundedNumberEditor( final JSpinner spinner )
		{
			super( spinner );
			final JFormattedTextField ftf = getTextField();
			final DecimalFormat format = ( DecimalFormat ) ( ( NumberFormatter ) ftf.getFormatter() ).getFormat();
			final NumberFormatter formatter = new NumberFormatter( format );
			formatter.setValueClass( spinner.getValue().getClass() );
			final DefaultFormatterFactory factory = new DefaultFormatterFactory( formatter );
			ftf.setFormatterFactory( factory );
		}
	}

	private void setupMinSpinner()
	{
		UIUtils.setPreferredWidth( minSpinner, 70 );
		minSpinner.setEditor( new UnboundedNumberEditor( minSpinner ) );
	}

	private void setupMaxSpinner()
	{
		UIUtils.setPreferredWidth( maxSpinner, 70 );
		maxSpinner.setEditor( new UnboundedNumberEditor( maxSpinner ) );
	}

	private void setupRangeSlider()
	{
		UIUtils.setPreferredWidth( originalRangeSlider, 50 );

		rangeSlider.addValuesChangedListener(e -> {
				lowValue = rangeSlider.getValue();
				highValue = rangeSlider.getUpperValue();
				notifyListeners();
		} );
		rangeSlider.addBoundsChangedListener(e -> {
				lowBound = rangeSlider.getRangeSlider().getMinimum();
				highBound = rangeSlider.getRangeSlider().getMaximum();
				notifyListeners();
		} );

		originalRangeSlider.addComponentListener( new ComponentAdapter()
		{
			@Override
			public void componentResized( final ComponentEvent e )
			{
				updateNumberFormat();
			}
		} );
	}

	private void setupBoundLabels()
	{
		upperBoundLabel.setBorder( new EmptyBorder( 0, 0, 0, 0 ) );
		lowerBoundLabel.setBorder( new EmptyBorder( 0, 0, 0, 0 ) );
		updateBoundLabelFonts();
	}

	private void updateBoundLabelFonts()
	{
		final Font labelFont = UIManager.getFont( "Label.font" );
		final Font font = new Font( labelFont.getName(), labelFont.getStyle(), 10 );
		upperBoundLabel.setFont( font );
		lowerBoundLabel.setFont( font );
	}

	private void setupPopupMenu()
	{
		final MouseListener ml = new MouseAdapter()
		{
			@Override
			public void mousePressed( final MouseEvent e )
			{
				if ( e.isPopupTrigger() ||
						( e.getButton() == MouseEvent.BUTTON1 && e.getX() > upperBoundLabel.getX() ) )
					doPop( e );
			}

			@Override
			public void mouseReleased( final MouseEvent e )
			{
				if ( e.isPopupTrigger() )
					doPop( e );
			}

			private void doPop( final MouseEvent e )
			{
				if ( isEnabled() && popup != null )
				{
					final JPopupMenu menu = popup.get();
					if ( menu != null )
						menu.show( e.getComponent(), e.getX(), e.getY() );
				}
			}
		};
		this.addMouseListener( ml );
		//rangeSlider.addMouseListener( ml ); //TODO what is the popup here?
	}

	private synchronized void updateNumberFormat()
	{
//		if ( userDefinedNumberFormat )
//			return;

		final int sw = originalRangeSlider.getWidth();
		if ( sw > 0 )
		{
			final double vrange = highBound - lowBound;
			final int digits = ( int ) Math.ceil( Math.log10( sw / vrange ) );

			blockUpdates = true;

			JSpinner.NumberEditor numberEditor = ( ( JSpinner.NumberEditor ) minSpinner.getEditor() );
			numberEditor.getFormat().setMaximumFractionDigits( digits );
			numberEditor.stateChanged( new ChangeEvent( minSpinner ) );

			numberEditor = ( ( JSpinner.NumberEditor ) maxSpinner.getEditor() );
			numberEditor.getFormat().setMaximumFractionDigits( digits );
			numberEditor.stateChanged( new ChangeEvent( maxSpinner ) );

			blockUpdates = false;
		}
	}

	private synchronized void updateRange( final BoundedRange newRange )
	{
		if ( !blockUpdates )
			setRange( newRange );
	}

	private boolean blockUpdates = false;

	public synchronized void setRange( final BoundedRange range )
	{
		blockUpdates = true;

		updateThisFromRange( range );
		rangeSlider.setSlidingRange((int)lowBound, (int)highBound);
		rangeSlider.setRange((int)lowValue, (int)highValue);

		final double frac = Math.max(
				Math.abs( Math.round( lowBound ) - lowBound ),
				Math.abs( Math.round( highBound ) - highBound ) );
		final String format = frac > 0.005 ? "%.2f" : "%.0f";
		upperBoundLabel.setText( String.format( format, highBound ) );
		lowerBoundLabel.setText( String.format( format, lowBound ) );
		this.invalidate();

		blockUpdates = false;

		notifyListeners();
	}

	private synchronized void notifyListeners() {
		listeners.list.forEach( ChangeListener::boundedRangeChanged );
	}

	public BoundedRange getRange()
	{
		return new BoundedRange(lowBound,highBound, lowValue,highValue);
	}

	public Listeners< ChangeListener > changeListeners()
	{
		return listeners;
	}

	public void setPopup( final Supplier< JPopupMenu > popup )
	{
		this.popup = popup;
	}

	public void shrinkBoundsToRange()
	{
		final BoundedRange range = getRange();
		updateRange( range.withMinBound( range.getMin() ).withMaxBound( range.getMax() ) );
	}

	public void setBoundsDialog()
	{
		final JPanel panel = new JPanel( new MigLayout( "fillx", "[][grow]", "" ) );
		final JSpinner minSpinner = new JSpinner( new SpinnerNumberModel( 0.0, 0.0, 1.0, 1.0 ) );
		final JSpinner maxSpinner = new JSpinner( new SpinnerNumberModel( 0.0, 0.0, 1.0, 1.0 ) );
		minSpinner.setEditor( new UnboundedNumberEditor( minSpinner ) );
		maxSpinner.setEditor( new UnboundedNumberEditor( maxSpinner ) );
		minSpinner.setValue( lowBound );
		maxSpinner.setValue( highBound );
		minSpinner.addChangeListener( e -> {
			final double value = ( Double ) minSpinner.getValue();
			if ( value > ( Double ) maxSpinner.getValue() )
				maxSpinner.setValue( value );
		} );
		maxSpinner.addChangeListener( e -> {
			final double value = ( Double ) maxSpinner.getValue();
			if ( value < ( Double ) minSpinner.getValue() )
				minSpinner.setValue( value );
		} );
		panel.add( "right", new JLabel( "min" ) );
		panel.add( "growx, wrap", minSpinner );
		panel.add( "right", new JLabel( "max" ) );
		panel.add( "growx", maxSpinner );
		final int result = JOptionPane.showConfirmDialog( null, panel, "Set Bounds", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE );
		if ( result == JOptionPane.YES_OPTION )
		{
			final double min = ( Double ) minSpinner.getValue();
			final double max = ( Double ) maxSpinner.getValue();
			updateRange( getRange().withMinBound( min ).withMaxBound( max ) );
		}
	}
}
