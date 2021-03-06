package spim.algorithm;

import ij.process.ImageProcessor;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.logging.Logger;

/**
 * The DefaultAntiDrift class provides PhaseCorrelation method for AntiDrift.
 */
public class DefaultAntiDrift extends AbstractAntiDrift
{
//	Logger log = Logger.getLogger(DefaultAntiDrift.class.getName());
	/**
	 * Instantiates a new DefaultAntiDrift class using PhaseCorrelation.
	 */
	public DefaultAntiDrift()
	{
		setLastCorrection( Vector3D.ZERO );
	}

	@Override public void startNewStack()
	{
		latest = new Projections();

		if(first == null)
			first = latest;
	}

	@Override public void addXYSlice( ImageProcessor ip )
	{
		latest.addXYSlice( ip );
	}

	@Override public Vector3D finishStack()
	{
		Vector3D suggested = latest.correlateAndAverage(first);

		ij.IJ.log( "Suggested offset: " + suggested.toString() );

		return suggested;
	}

	@Override public Vector3D updateOffset( Vector3D offset )
	{
		Vector3D lastOffset = offset.add(lastCorrection);

		ij.IJ.log( "Last offset: " + lastOffset );

		return lastOffset;
	}
}
