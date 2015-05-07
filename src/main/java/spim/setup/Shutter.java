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
public class Shutter extends Device {

    static {
		Device.installFactory(new Device.Factory() {
			@Override
			public Device manufacture(CMMCore core, String label) {
				return new Shutter(core, label);
			}
		}, "*", SPIMSetup.SPIMDevice.DACSHUTTER);
	}
    
    //TODO: Figure out a better way to querry the arduino peripherals, Its
    //kludgy.
    
    public Shutter(CMMCore core, String label) {
        super(core, label);    
        try {
            //DAC Shutter uses the digital pins of Arduino 
            // which are used by the switch peripheral  and defaults to 0,
            //set to all on here.
            core.setProperty("Arduino-Switch","State" , 63);
        } catch (Exception ex) {
            ReportingUtils.logError(ex, "Couldn't set arduino switch " + label);
        }
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
	 * @throws Exception null etc
	 */
	protected void popShutter() throws Exception {
		if (oldShutter == null)
			throw new Exception("No shutter stored!");

		core.setShutterDevice(oldShutter);
		oldShutter = null;
	}
    
    public void setShutterOpen(boolean open) {
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
	public boolean getShutterOpen() {
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



    
}

