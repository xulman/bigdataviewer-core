/*
 * #%L
 * BigDataViewer core classes with minimal dependencies
 * %%
 * Copyright (C) 2012 - 2016 Tobias Pietzsch, Stephan Saalfeld, Stephan Preibisch,
 * Jean-Yves Tinevez, HongKee Moon, Johannes Schindelin, Curtis Rueden, John Bogovic
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
package net.imglib2.type.volatiles;

import net.imglib2.Volatile;
import net.imglib2.img.NativeImg;
import net.imglib2.img.NativeImgFactory;
import net.imglib2.img.basictypeaccess.ShortAccess;
import net.imglib2.img.basictypeaccess.volatiles.VolatileShortAccess;
import net.imglib2.img.basictypeaccess.volatiles.array.VolatileShortArray;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.util.Fraction;

/**
 * A {@link Volatile} variant of {@link ShortType}. It uses an
 * underlying {@link ShortType} that maps into a
 * {@link VolatileShortAccess}.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class VolatileShortType extends AbstractVolatileNativeRealType< ShortType, VolatileShortType >
{
	final protected NativeImg< ?, ? extends VolatileShortAccess > img;

	private static class WrappedShortType extends ShortType
	{
		public WrappedShortType( final NativeImg<?, ? extends ShortAccess> img )
		{
			super( img );
		}

		public WrappedShortType( final ShortAccess access )
		{
			super( access );
		}

		public void setAccess( final ShortAccess access )
		{
			dataAccess = access;
		}
	}

	// this is the constructor if you want it to read from an array
	public VolatileShortType( final NativeImg< ?, ? extends VolatileShortAccess > img )
	{
		super( new WrappedShortType( img ), false );
		this.img = img;
	}

	// this is the constructor if you want to specify the dataAccess
	public VolatileShortType( final VolatileShortAccess access )
	{
		super( new WrappedShortType( access ), access.isValid() );
		this.img = null;
	}

	// this is the constructor if you want it to be a variable
	public VolatileShortType( final short value )
	{
		this( new VolatileShortArray( 1, true ) );
		set( value );
	}

	// this is the constructor if you want it to be a variable
	public VolatileShortType()
	{
		this( ( short ) 0 );
	}

	public void set( final short value )
	{
		get().set( value );
	}

	@Override
	public void updateContainer( final Object c )
	{
		final VolatileShortAccess a = img.update( c );
		( (WrappedShortType) t ).setAccess( a );
		setValid( a.isValid() );
	}

	@Override
	public NativeImg< VolatileShortType, ? extends VolatileShortAccess > createSuitableNativeImg( final NativeImgFactory< VolatileShortType > storageFactory, final long[] dim )
	{
		// create the container
		@SuppressWarnings( "unchecked" )
		final NativeImg< VolatileShortType, ? extends VolatileShortAccess > container = ( NativeImg< VolatileShortType, ? extends VolatileShortAccess > ) storageFactory.createShortInstance( dim, new Fraction() );

		// create a Type that is linked to the container
		final VolatileShortType linkedType = new VolatileShortType( container );

		// pass it to the NativeContainer
		container.setLinkedType( linkedType );

		return container;
	}

	@Override
	public VolatileShortType duplicateTypeOnSameNativeImg()
	{
		return new VolatileShortType( img );
	}

	@Override
	public VolatileShortType createVariable()
	{
		return new VolatileShortType();
	}

	@Override
	public VolatileShortType copy()
	{
		final VolatileShortType v = createVariable();
		v.set( this );
		return v;
	}
}
