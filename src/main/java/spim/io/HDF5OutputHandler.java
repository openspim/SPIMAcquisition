package spim.io;

import bdv.export.ExportMipmapInfo;
import bdv.export.ProposeMipmaps;
import bdv.export.WriteSequenceToHdf5;
import bdv.img.hdf5.Hdf5ImageLoader;
import bdv.img.hdf5.Partition;
import bdv.spimdata.SequenceDescriptionMinimal;
import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import ij.ImagePlus;
import ij.process.ImageProcessor;

import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.registration.ViewRegistration;
import mpicbg.spim.data.registration.ViewRegistrations;
import mpicbg.spim.data.sequence.Angle;
import mpicbg.spim.data.sequence.Channel;
import mpicbg.spim.data.sequence.FinalVoxelDimensions;
import mpicbg.spim.data.sequence.Illumination;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.TimePoints;
import mpicbg.spim.data.sequence.ViewId;
import net.imglib2.FinalDimensions;
import net.imglib2.realtransform.AffineTransform3D;
import spim.io.imgloader.ProcessorStackImgLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * HDF5OutputHandler for the SPIMAcquisition
 */
public class HDF5OutputHandler implements OutputHandler, Thread.UncaughtExceptionHandler
{
	final private File outputDirectory;
	final private int tSize, xSize, ySize;

	private int angleSize;
	private int channelSize;
	private int illuminationSize;


	// Each row can have different depth
	private int[] zSizes;
	private double[] zStepSize;

	private long pixelDepth;
	private double pixelSizeUm;

	// Image property collections
	final HashMap< Integer, BasicViewSetup > setups = new HashMap< Integer, BasicViewSetup >();
	final HashMap< ViewId, Partition > viewIdToPartition = new HashMap< ViewId, Partition >();


	// Image loader hashmap. Access angle -> timepoint order.
	final Map< Integer, HashMap< Integer, ProcessorStackImgLoader > > imgLoaders = Collections.synchronizedMap( new HashMap< Integer, HashMap< Integer, ProcessorStackImgLoader > >() );
	final ArrayList<Thread> finalizers = new ArrayList< Thread >();

	ArrayList<Partition> hdf5Partitions;
	ArrayList<TimePoint> timePoints;
	String baseFilename;

	public HDF5OutputHandler( File outDir, int xSize, int ySize, int tSize, int angleSize )
	{
		if(outDir == null || !outDir.exists() || !outDir.isDirectory())
			throw new IllegalArgumentException("Null path specified: " + outDir.toString());

		this.outputDirectory = outDir;
		this.xSize = xSize;
		this.ySize = ySize;

		this.angleSize = angleSize;
		this.zSizes = new int[angleSize];
		this.zStepSize = new double[angleSize];
		this.tSize = tSize;

		this.pixelDepth = 8;
		this.pixelSizeUm = 0.043;
	}

	public void init(String datasetName)
	{
		ArrayList<ViewId> viewIds = new ArrayList< ViewId >();
		ArrayList<BasicViewSetup> viewSetups = new ArrayList< BasicViewSetup >();
		timePoints = new ArrayList<TimePoint>();

		// Setup ViewID
		for(int t = 0; t < tSize; t++)
		{
			timePoints.add( new TimePoint( t ) );
			for(int v = 0; v < angleSize; v++)
			{
				viewIds.add(new ViewId( t, v ));
			}
		}

		// Setup ViewSetup
		for(int i = 0; i < angleSize; i++)
		{
			imgLoaders.put( i, new HashMap< Integer, ProcessorStackImgLoader >() );

			String punit = "um";
			final FinalVoxelDimensions voxelSize = new FinalVoxelDimensions( punit, pixelSizeUm, pixelSizeUm, zStepSize[i] );
			final FinalDimensions size = new FinalDimensions( new int[]{ xSize, ySize, zSizes[i] } );

			final BasicViewSetup setup = new BasicViewSetup( i, "" + i, size, voxelSize );
			setup.setAttribute( new Angle( i ) );

			// Currently, Illumination and Channel sizes are assumed to be 1
			// TODO: if there are more channels and illuminations, deal with them here.
			setup.setAttribute( new Illumination( 0 ) );
			setup.setAttribute( new Channel( 0 ) );

			setups.put( i, setup );
			viewSetups.add( setup );
		}

		// Output file setup
		baseFilename = outputDirectory.getAbsolutePath() + "/" + datasetName + ".h5";
		String basename = baseFilename;
		if ( basename.endsWith( ".h5" ) )
			basename = basename.substring( 0, basename.length() - ".h5".length() );

		// HDF5 Partitions setup
		hdf5Partitions = Partition.split( timePoints, viewSetups, 1, 1, basename );

		for( ViewId viewId : viewIds )
			for ( Partition p : hdf5Partitions )
				if( p.contains( viewId ) )
					viewIdToPartition.put( viewId, p );
	}

	public int getAngleSize()
	{
		return angleSize;
	}

	public void setAngleSize( int angleSize )
	{
		this.angleSize = angleSize;
	}

	public int getChannelSize()
	{
		return channelSize;
	}

	public void setChannelSize( int channelSize )
	{
		this.channelSize = channelSize;
	}

	public int getIlluminationSize()
	{
		return illuminationSize;
	}

	public void setIlluminationSize( int illuminationSize )
	{
		this.illuminationSize = illuminationSize;
	}

	public int[] getzSizes()
	{

		return zSizes;
	}

	public void setzSizes( int[] zSizes )
	{
		this.zSizes = zSizes;
	}

	public double[] getzStepSize()
	{
		return zStepSize;
	}

	public void setzStepSize( double[] zStepSize )
	{
		this.zStepSize = zStepSize;
	}

	public long getPixelDepth()
	{
		return pixelDepth;
	}

	public void setPixelDepth( long pixelDepth )
	{
		this.pixelDepth = pixelDepth;
	}

	public double getPixelSizeUm()
	{
		return pixelSizeUm;
	}

	public void setPixelSizeUm( double pixelSizeUm )
	{
		this.pixelSizeUm = pixelSizeUm;
	}



	@Override public ImagePlus getImagePlus() throws Exception
	{
		return null;
	}

	@Override public void beginStack( int time, int angle ) throws Exception
	{
//		ij.IJ.log("BeginStack:");
//		ij.IJ.log("    Time "+ time);
//		ij.IJ.log("    Angle: "+ angle);
		imgLoaders.get(angle).put(time, new ProcessorStackImgLoader( outputDirectory, viewIdToPartition, setups.get( angle ),
				time, xSize, ySize, zSizes[angle] ) );
		imgLoaders.get(angle).get(time).start();
	}

	@Override public void processSlice( int time, int angle, ImageProcessor ip, double X, double Y, double Z, double theta, double deltaT ) throws Exception
	{
//		ij.IJ.log("Time "+ time + " Angle: "+ angle );
		imgLoaders.get(angle).get(time).process( ip );
	}

	@Override public void finalizeStack( final int time, final int angle ) throws Exception
	{
		Thread finalizer = new Thread ( new Runnable()
		{
			@Override public void run()
			{
				imgLoaders.get(angle).get(time).finalize();
			}
		});

		finalizers.add( finalizer );
		finalizer.start();
	}

	@Override public void finalizeAcquisition( boolean b ) throws Exception
	{
		for(Thread thread : finalizers)
			thread.join();
		finalizers.clear();

		final SequenceDescriptionMinimal seq = new SequenceDescriptionMinimal( new TimePoints( timePoints ), setups, null, null );

		TreeMap< Integer, ExportMipmapInfo > perSetupExportMipmapInfo = new TreeMap< Integer, ExportMipmapInfo >(  );

		for(Integer key: setups.keySet())
		{
			perSetupExportMipmapInfo.put( key, ProposeMipmaps.proposeMipmaps( setups.get( key ) ) );
		}

		WriteSequenceToHdf5.writeHdf5PartitionLinkFile( seq, perSetupExportMipmapInfo, hdf5Partitions, new File( baseFilename ) );


		final ArrayList< ViewRegistration > registrations = new ArrayList< ViewRegistration >();
		for(int i = 0; i < angleSize; i++)
		{
			// create SourceTransform from the images calibration
			final AffineTransform3D sourceTransform = new AffineTransform3D();
			sourceTransform.set( pixelSizeUm, 0, 0, 0, 0, pixelSizeUm, 0, 0, 0, 0, zStepSize[i], 0 );

			for ( int t = 0; t < tSize; ++t )
				registrations.add( new ViewRegistration( t, i, sourceTransform ) );
		}

		seq.setImgLoader( new Hdf5ImageLoader( new File(baseFilename), hdf5Partitions, seq, false ) );
		final File basePath = outputDirectory;
		final SpimDataMinimal spimData = new SpimDataMinimal( basePath, seq, new ViewRegistrations( registrations ) );

		try
		{
			new XmlIoSpimDataMinimal().save( spimData, basePath + "/dataset.xml" );
		}
		catch ( final Exception e )
		{
			throw new RuntimeException( e );
		}


		// Clean up the all the temporary files
		for(int i = 0; i < angleSize; i++)
		{
			for ( int t = 0; t < tSize; t++ )
			{
				// Delete all the temp files when the acquisition is successfully done.
				//if(b) imgLoaders.get( i ).get( t ).cleanFiles();
				imgLoaders.get( i ).remove( t );
			}
			imgLoaders.remove( i );
		}

		imgLoaders.clear();
	}

	@Override public void uncaughtException( Thread thread, Throwable throwable )
	{

	}
}
