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
package bdv.export;

import ch.systemsx.cisd.hdf5.HDF5IntStorageFeatures;
import ch.systemsx.cisd.hdf5.IHDF5Writer;

/**
 * A connection interface between HDF5 library calls and BDV (image) data feeders.
 * The HDF5-side implements this interface in HDF5Access*java files/classes.
 * The BDV-side implements this interface in Hdf5BlockWriterThread.java.
 */
interface IHDF5Access
{
	public void writeMipmapDescription( final int setupIdPartition, final ExportMipmapInfo mipmapInfo );

	public void createAndOpenDataset( final String path, long[] dimensions, int[] cellDimensions, HDF5IntStorageFeatures features );

	/**
	 * This is the main method that pushes image/voxel data into the HDF5 file.
	 * There needs to be multiple clones of it, each for specific voxel types,
	 * as on the HDF5-side this should end up as different HDF5 library calls.
	 *
	 * The parameter 'data' is assumed to be an array of Java basic data type, in fact
	 * any basic type that can be covered/mapped/wrapped with imglibs2.NativeType.
	 */
	public void writeBlockWithOffset( final Object data, final long[] blockDimensions, final long[] offset );

	public void closeDataset();

	public void close();

	// this is for sharing with Hdf5ImageLoader for loopback loader when exporting
	public IHDF5Writer getIHDF5Writer();
}
