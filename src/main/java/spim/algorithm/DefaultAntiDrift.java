package spim.algorithm;

import ij.process.ImageProcessor;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.logging.Logger;

/**
 * The DefaultAntiDrift class provides PhaseCorrelation method for AntiDrift.
 */
public class DefaultAntiDrift extends AbstractAntiDrift
{
	Logger log = Logger.getLogger(DefaultAntiDrift.class.getName());
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
	}

	@Override public void addXYSlice( ImageProcessor ip )
	{
		latest.addXYSlice( ip );
	}

	@Override public void finishStack()
	{
		if(first == null)
			first = latest;

		Vector3D suggested = latest.correlateAndAverage(first);

		log.info( suggested.toString() );

		setLastCorrection( suggested );
	}

	@Override public void updateOffset( Vector3D offset )
	{
		offset = offset.add(lastCorrection);
		setLastCorrection( offset );
	}
}
