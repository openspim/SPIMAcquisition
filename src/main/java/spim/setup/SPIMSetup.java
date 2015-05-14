package spim.setup;

import ij.IJ;

import java.util.Map;
import java.util.EnumMap;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.micromanager.utils.ReportingUtils;

import mmcorej.CMMCore;
import mmcorej.DeviceType;
import mmcorej.StrVector;
import mmcorej.TaggedImage;

public class SPIMSetup {
	public static enum SPIMDevice {
		STAGE_X ("X Stage"),
		STAGE_Y ("Y Stage"),
		STAGE_Z ("Z Stage"),
		STAGE_THETA ("Angle"),
		LASER1 ("Laser"),
		LASER2 ("Laser (2)"),
                DAC1 ("ch1(V)"),
                DAC2 ("ch2(V)"),
                DAC3 ("ch3(V)"),
                DAC4 ("ch4(V)"),
                DAC5 ("ch5(V)"),
                DAC6 ("ch6(V)"),
                DACSHUTTER ("DAC blanking"),
                EMISSIONWHEEL ("FW(Pos)"),
		CAMERA1 ("Camera"),
		CAMERA2 ("Camera (2)"),
		SYNCHRONIZER ("Synchronizer");

		private String text;
		private SPIMDevice(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}
	}

        public static enum SPIMIllumination {
            DAC, LASER;
        }

	private Map<SPIMDevice, Device> deviceMap;
	private CMMCore core;
        private SPIMIllumination illumination;

	public SPIMSetup(CMMCore core) {
		this.core = core;

		deviceMap = new EnumMap<SPIMDevice, Device>(SPIMDevice.class);
	}

	public void debugLog() {
		IJ.log("SPIM Setup " + this.toString() + ":");
		for(Map.Entry<SPIMDevice, Device> entr : deviceMap.entrySet())
			IJ.log(" " + entr.getKey() + " => " + (entr.getValue() != null ? "\"" + entr.getValue().getLabel() + "\" (" + entr.getValue().getDeviceName() + " / " + entr.getValue().toString() + ")" : "None"));
	}

	/*
	 * Some methods which analyze the setup.
	 */
	public boolean isConnected(SPIMDevice type) {
		if (deviceMap.get(type) != null) {
			try {
				return strVecContains(core.getLoadedDevicesOfType(deviceMap.get(type).getMMType()), deviceMap.get(type).getLabel());
			} catch (Throwable t) {
				ReportingUtils.logError(t, "SPIMAcquisition checking connection");
				return false;
			}
		} else {
			return false;
		}
	}

	public boolean hasZStage() {
		return isConnected(SPIMDevice.STAGE_Z);
	}

	public boolean hasXYStage() {
		return isConnected(SPIMDevice.STAGE_X) && isConnected(SPIMDevice.STAGE_Y);
	}

	public boolean has3DStage() {
		return hasZStage() && hasXYStage();
	}

	public boolean hasAngle() {
		return isConnected(SPIMDevice.STAGE_THETA);
	}

	public boolean has4DStage() {
		return has3DStage() && hasAngle();
	}

         public boolean hasDAC(){
            return !(getDACChannel(1)  == null) || !(getDACChannel(2) == null) || !(getDACChannel(3) == null) ||
                    !(getDACChannel(4) == null) || !(getDACChannel(5) == null) || !(getDACChannel(6) == null);
        }


	public boolean isMinimalMicroscope() {
		return hasZStage() && isConnected(SPIMDevice.CAMERA1);
	}

	public boolean is3DMicroscope() {
		return has3DStage() && isConnected(SPIMDevice.CAMERA1);
	}

	public boolean isMinimalSPIM() {
		return is3DMicroscope() && hasAngle() && isConnected(SPIMDevice.LASER1);
	}

	/*
	 * Some generic getters. The class might not stay backed by an EnumMap, so
	 * these may be more important in the future.
	 */
	public Device getDevice(SPIMDevice device) {
		return deviceMap.get(device);
	}
	
	public void setDevice(SPIMDevice type, String label) {
		try {
			if(label == null || label.length() <= 0)
				deviceMap.put(type, null);
			else
				deviceMap.put(type, Device.createDevice(core, type, label));
		} catch (Exception e) {
			ReportingUtils.logError(e, "Trying to exchange " + (getDevice(type) != null ? getDevice(type).getLabel() : "(null)") + " with " + label);
		}
	}

        public void setIllumination(SPIMIllumination illumination){
            this.illumination = illumination;
        }

        public SPIMIllumination getIllumination(){
            return illumination;
        }

        public boolean isDACIllumination(){
            if(illumination == SPIMIllumination.LASER){
                return false;
            }else{
                return true;
            }
        }

	public Stage getXStage() {
		return (Stage) deviceMap.get(SPIMDevice.STAGE_X);
	}

	public Stage getYStage() {
		return (Stage) deviceMap.get(SPIMDevice.STAGE_Y);
	}

	public Stage getZStage() {
		return (Stage) deviceMap.get(SPIMDevice.STAGE_Z);
	}

	public Stage getThetaStage() {
		return (Stage) deviceMap.get(SPIMDevice.STAGE_THETA);
	}

	public Laser getLaser() {
		return (Laser) deviceMap.get(SPIMDevice.LASER1);
	}


        public DAC getDACChannel(int i) {
            switch(i){
                case 1:
                return (DAC) deviceMap.get(SPIMDevice.DAC1);
                case 2:
                return (DAC) deviceMap.get(SPIMDevice.DAC2);
                case 3:
                return (DAC) deviceMap.get(SPIMDevice.DAC3);
                case 4:
                return (DAC) deviceMap.get(SPIMDevice.DAC4);
                case 5:
                return (DAC) deviceMap.get(SPIMDevice.DAC5);
                case 6:
                return (DAC) deviceMap.get(SPIMDevice.DAC6);
                default:
                return null;
            }
            }

        public Shutter getDACShutter(){
            return (Shutter) deviceMap.get(SPIMDevice.DACSHUTTER);
        }

        public FilterWheel getFilterWheel(){
                return (FilterWheel) deviceMap.get(SPIMDevice.EMISSIONWHEEL);
        }


	public Camera getCamera() {
		return (Camera) deviceMap.get(SPIMDevice.CAMERA1);
	}

	public Device getSynchronizer() {
		return deviceMap.get(SPIMDevice.SYNCHRONIZER);
	}

	public TaggedImage snapImage() {
		if(!core.getAutoShutter() && getLaser() != null)
			getLaser().setPoweredOn(true);

		TaggedImage ret = getCamera().snapImage();

		if(!core.getAutoShutter() && getLaser() != null)
			getLaser().setPoweredOn(false);

		return ret;
	}

	/*
	 * Some convenience methods for positioning the setup's 4D stage.
	 */

	/**
	 * Navigates the stage to the specified quadruplet. Null parameters mean no
	 * change.
	 * 
	 * @param x New position of the X stage
	 * @param y New position of the Y stage
	 * @param z New position of the Z stage
	 * @param t New position of the theta stage
	 */
	public void setPosition(Double x, Double y, Double z, Double t) {
		if (!has3DStage())
			return;

		if (x != null)
			getXStage().setPosition(x);

		if (y != null)
			getYStage().setPosition(y);

		if (z != null)
			getZStage().setPosition(z);

		if (t != null)
			getThetaStage().setPosition(t);
	}

	/**
	 * Reposition the stage to the specified coordinates and angle.
	 * 
	 * @param xyz New translational position of the stage
	 * @param t New position of the theta stage
	 */
	public void setPosition(Vector3D xyz, Double t) {
		setPosition(xyz.getX(), xyz.getY(), xyz.getZ(), t);
	}

	/**
	 * Reposition the stage to the specified coordinates.
	 * 
	 * @param xyz New translational position of the stage
	 */
	public void setPosition(Vector3D xyz) {
		setPosition(xyz, null);
	}

	/**
	 * Gets the position of the stage as a vector.
	 * 
	 * @return Current stage position
	 */
	public Vector3D getPosition() {
		if (!has3DStage())
			return Vector3D.ZERO;

		return new Vector3D(getXStage().getPosition(), getYStage().getPosition(), getZStage().getPosition());
	}

	public double getAngle() {
		return getThetaStage().getPosition();
	}

	public CMMCore getCore() {
		return core;
	}

	public static SPIMSetup createDefaultSetup(CMMCore core) {
		SPIMSetup setup = new SPIMSetup(core);

		try {
			for (SPIMDevice dev : SPIMDevice.values())
				setup.deviceMap.put(dev, setup.constructIfValid(dev, setup.getDefaultDeviceLabel(dev)));
		} catch (Exception e) {
			ReportingUtils.logError(e, "Couldn't build default setup.");
			return null;
		}

		return setup;
	}

	public String getDefaultDeviceLabel(SPIMDevice dev) throws Exception {
		switch (dev) {
		case STAGE_X:
		case STAGE_Y:
			return core.getXYStageDevice();

		case STAGE_Z:
			return core.getFocusDevice();

		case STAGE_THETA:
			// TODO: In my ideal stage setup (three unique linear stages) this
			// wouldn't work.
			// The X and Y stages would also be StageDevices. I haven't thought
			// of a workaround yet. :(
			return labelOfSecondary(DeviceType.StageDevice, core.getFocusDevice());

		case LASER1:
			return core.getShutterDevice();

		case LASER2:
			// TODO: This might not be exact -- Arduino might end up showing up
			// as a shutter.
                        // SW Note: it does.
			return labelOfSecondary(DeviceType.ShutterDevice, core.getShutterDevice());
                case DAC1:
                        // TODO: need a more elegant way of dealing with the labels
                        // These are HARDWIRED to the default MM config as set in the
                        // updated arduino firmware-BAD
			return "Arduino-DAC1";
                case DAC2:
                        // TODO: need a more elegant way of dealing with the labels
			return "Arduino-DAC2";
                case DAC3:
                        // TODO: need a more elegant way of dealing with the labels
			return "Arduino-DAC3";
                case DAC4:
                        // TODO: need a more elegant way of dealing with the labels
			return "Arduino-DAC4";
                case DAC5:
                        // TODO: need a more elegant way of dealing with the labels
			return "Arduino-DAC5";
                case DAC6:
                        // TODO: need a more elegant way of dealing with the labels
			return "Arduino-DAC6";
                case DACSHUTTER:
			return core.getShutterDevice();
                case EMISSIONWHEEL:
                        // TODO: need a more elegant way of dealing with the labels
                        return "Wheel-A";
		case CAMERA1:
			return core.getCameraDevice();

		case CAMERA2:
			return labelOfSecondary(DeviceType.CameraDevice, core.getCameraDevice());
		case SYNCHRONIZER:
                        // TODO: not implemented yet through Arduino
			return null;
		default:
			return null;
		}
	}

	private Device constructIfValid(SPIMDevice type, String label) throws Exception {
		if (label != null && label.length() > 0)
			return Device.createDevice(core, type, label);
		else
			return null;
	}

	private String labelOfSecondary(DeviceType mmtype, String except) throws Exception {
		String other = null;

		for (String s : core.getLoadedDevicesOfType(mmtype)) {
			if (!s.equals(except)) {
				if (other == null)
					other = s;
				else
					return null;
			}
		}

		return other;
	}

	private static boolean strVecContains(StrVector vec, String check) {
		for (String str : vec)
			if (str.equals(check))
				return true;

		return false;
	}
}