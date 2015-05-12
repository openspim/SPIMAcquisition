/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spim.setup;

import mmcorej.CMMCore;
import mmcorej.DeviceType;
import org.micromanager.utils.ReportingUtils;

/**
 *
 * @author winfrees
 */
public class DAC extends Device {

    static {
		Device.installFactory(new Factory() {
			@Override
			public Device manufacture(CMMCore core, String label) {
				return new DAC(core, label);
			}
		}, "*", SPIMSetup.SPIMDevice.DAC1, SPIMSetup.SPIMDevice.DAC2, SPIMSetup.SPIMDevice.DAC3, SPIMSetup.SPIMDevice.DAC4, SPIMSetup.SPIMDevice.DAC5, SPIMSetup.SPIMDevice.DAC6);
	}
    
    public DAC(CMMCore core, String label) {
        super(core, label);
    }
    
    protected String oldShutter;

    @Override
    public DeviceType getMMType() {
        return DeviceType.SignalIODevice;
    }
    
    	/**
	 * Store the current MM shutter, for operations that require replacing the
	 * default shutter (i.e. get/setShutterOpen).
	 * 
	 * @throws Exception null etc
	 */
	protected void pushShutter() throws Exception {
		oldShutter = core.getShutterDevice();
	}

	/**
	 * Restore the stored MM shutter.
	 * 
	 * @throws Exception null etc.
	 */
	protected void popShutter() throws Exception {
		if (oldShutter == null)
			throw new Exception("No shutter stored!");

		core.setShutterDevice(oldShutter);
		oldShutter = null;
	}
    
    public void setPoweredOn(boolean open) {
		try {
			pushShutter();
			core.setShutterOpen(open);
			popShutter();
		} catch (Exception e) {
			ReportingUtils.logError(e, "Couldn't open/close shutter " + label);
		}
	}

	/**
	 * Get the laser's power status.
	 * 
	 * @return True of the laser is on, false if off.
	 */
	public boolean getPoweredOn() {
		try {
			pushShutter();
			boolean open = core.getShutterOpen();
			popShutter();
			return open;
		} catch (Exception e) {
			ReportingUtils.logError(e, "Couldn't get shutter status for " + label);
			return false;
		}
	}

	/**
	 * Set the laser's power in Watts.
	 * 
     * @param channel channel to change
     * @param volts new voltage
	 */
	public void setVoltage(int channel, double volts) throws UnsupportedOperationException, IllegalArgumentException {
		if(hasProperty("Volts"))
			setProperty("Volts", volts);
		else
			throw new UnsupportedOperationException();
	}

	/**
	 * Get the laser's power in Watts.
	 * 
	 * @return Current laser power in Watts.
	 */
	public double getVoltage() {
		if(hasProperty("Volts"))
			return getPropertyDouble("Volts");
		else
			return 0.0;
	}

	/**
	 * Return the minimum laser power in Watts. May be 0.
	 * 
	 * @return Minimum active laser power in Watts.
	 */
//	public double getMinVolts() {
//		if(hasProperty("Min"))
//			return getPropertyDouble("Minimum Laser Power");
//		else
//			return 0.0;
//	}

	/**
	 * Return the maximum laser power in Watts.
	 * 
	 * @return Maximum active laser power in Volts.
	 */
	public double getMaxVolt() {
		if(hasProperty("MaxVolt"))
			return getPropertyDouble("MaxVolt");
		else
			return 0.05;  //haha not zero
	}
    
}
