/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spim.setup;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import mmcorej.CMMCore;
import mmcorej.DeviceType;
import mmcorej.StrVector;

/**
 *
 * @author winfrees
 */
public class FilterWheel extends Device {

    static {
		Device.installFactory(new Factory() {
			@Override
			public Device manufacture(CMMCore core, String label) {
				return new FilterWheel(core, label);
			}
		}, "*", SPIMSetup.SPIMDevice.EMISSIONWHEEL);
	}
    
    public FilterWheel(CMMCore core, String label) {
        super(core, label);
    }
    
    public String getPosition(){		
        if(hasProperty("State"))
			return getProperty("State");
		else
			return "NA";
                          }
    
    public void setPosition(int state) throws UnsupportedOperationException, IllegalArgumentException {
    	if(hasProperty("State"))
            setProperty("State", state);
	else
			throw new UnsupportedOperationException();
    }
    
    public void setSpeed(int speed, int state)throws UnsupportedOperationException, IllegalArgumentException {
    if(hasProperty("Speed")){
			setProperty("Speed", speed);
                        setProperty("State", state);
    }else{
			throw new UnsupportedOperationException();}}
    
    //TO DO for comboboxes
    public String[] getStates() throws Exception{
        
        StrVector states = new StrVector();
        
        states = core.getStateLabels(label);
        
        return states.toArray();}
    
    public void setState(int state){
        
        try {
            core.setState(label, state);
        } catch (Exception ex) {
            Logger.getLogger(FilterWheel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    

    @Override
    public DeviceType getMMType() {
       return DeviceType.StateDevice;
    }
    
    
    
}
