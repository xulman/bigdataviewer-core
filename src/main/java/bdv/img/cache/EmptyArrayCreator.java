package bdv.img.cache;

import net.imglib2.cache.img.AccessFlags;
import net.imglib2.cache.img.PrimitiveType;
import net.imglib2.img.basictypeaccess.volatiles.VolatileAccess;
import net.imglib2.img.basictypeaccess.volatiles.VolatileArrayDataAccess;

/**
 * Produce empty, invalid {@link VolatileAccess} to be used as placeholder
 * invalid cell data.
 * <p>
 * For common types, it should be created through the static helper method
 * {@link #get(PrimitiveType, AccessFlags...)} to get the desired primitive type
 * and dirty / non-dirty variant.
 * </p>
 *
 * @param <A>
 *            access type
 *
 * @author Tobias Pietzsch
 */
public interface EmptyArrayCreator< A >
{
	public A getEmptyArray( final long numEntities );

	public static < A extends VolatileArrayDataAccess< A > > EmptyArrayCreator< A > get(
			final PrimitiveType primitiveType,
			final AccessFlags ... flags )
	{
		return DefaultEmptyArrayCreator.get( primitiveType, flags );
	}
}
