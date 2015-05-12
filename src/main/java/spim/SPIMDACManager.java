/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spim;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.micromanager.utils.ReportingUtils;
import spim.setup.SPIMSetup;

/**
 *
 * @author winfrees
 */
public class SPIMDACManager extends JPanel implements ItemListener {

    private final SPIMSetup setup;
    private JFrame display;

    protected SteppedSlider DAC1Slider, DAC2Slider, DAC3Slider, DAC4Slider, DAC5Slider, DAC6Slider;

    private JComboBox filterwheelstate;
    private JComboBox filterwheelspeed;
    private JLabel state;
    private JLabel speed;
    private String[] wheelstates;
    private String[] wheelspeed = {"0", "1", "2", "3", "4", "5"};

    public SPIMDACManager(SPIMSetup config) {
        this.setup = config;
        try {
            this.wheelstates = setup.getFilterWheel().getStates();
        } catch (Exception ex) {
            Logger.getLogger(SPIMDACManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        JPanel lasers = LayoutUtils.titled("AOTF Lasers (DAC)", new JPanel(new GridLayout(6, 2, 0, 2)));

                //IJ.log("Making DAC Slidders...");
        DAC1Slider = new SteppedSlider("DAC1 Voltage:", 0, setup.getDACChannel(1).getMaxVolt(), 0.1, setup.getDACChannel(1).getVoltage(), SteppedSlider.LABEL_LEFT | SteppedSlider.INCREMENT_BUTTONS) {
            @Override
            public void valueChanged() {
                try {
                    setup.getDACChannel(1).setProperty("Volts", getValue());
                } catch (Exception e) {
                    ReportingUtils.logError(e);
                }
            }
        };

        DAC2Slider = new SteppedSlider("DAC2 Voltage:", 0, setup.getDACChannel(2).getMaxVolt(), 0.1, setup.getDACChannel(2).getVoltage(), SteppedSlider.LABEL_LEFT | SteppedSlider.INCREMENT_BUTTONS) {
            @Override
            public void valueChanged() {
                try {
                    setup.getDACChannel(2).setProperty("Volts", getValue());
                } catch (Exception e) {
                    ReportingUtils.logError(e);
                }
            }
        };

        DAC3Slider = new SteppedSlider("DAC3 Voltage:", 0, setup.getDACChannel(3).getMaxVolt(), 0.1, setup.getDACChannel(3).getVoltage(), SteppedSlider.LABEL_LEFT | SteppedSlider.INCREMENT_BUTTONS) {
            @Override
            public void valueChanged() {
                try {
                    setup.getDACChannel(3).setProperty("Volts", getValue());
                } catch (Exception e) {
                    ReportingUtils.logError(e);
                }
            }
        };

        DAC4Slider = new SteppedSlider("DAC4 Voltage:", 0, setup.getDACChannel(4).getMaxVolt(), 0.1, setup.getDACChannel(4).getVoltage(), SteppedSlider.LABEL_LEFT | SteppedSlider.INCREMENT_BUTTONS) {
            @Override
            public void valueChanged() {
                try {
                    setup.getDACChannel(4).setProperty("Volts", getValue());
                } catch (Exception e) {
                    ReportingUtils.logError(e);
                }
            }
        };

        DAC5Slider = new SteppedSlider("DAC5 Voltage:", 0, setup.getDACChannel(5).getMaxVolt(), 0.1, setup.getDACChannel(5).getVoltage(), SteppedSlider.LABEL_LEFT | SteppedSlider.INCREMENT_BUTTONS) {
            @Override
            public void valueChanged() {
                try {
                    setup.getDACChannel(5).setProperty("Volts", getValue());
                } catch (Exception e) {
                    ReportingUtils.logError(e);
                }
            }
        };

        DAC6Slider = new SteppedSlider("DAC6 Voltage:", 0, setup.getDACChannel(6).getMaxVolt(), 0.1, setup.getDACChannel(6).getVoltage(), SteppedSlider.LABEL_LEFT | SteppedSlider.INCREMENT_BUTTONS) {
            @Override
            public void valueChanged() {
                try {
                    setup.getDACChannel(6).setProperty("Volts", getValue());
                } catch (Exception e) {
                    ReportingUtils.logError(e);
                }
            }
        };

                   //IJ.log("Adding DAC Slidders...");
        JPanel filterwheels = LayoutUtils.titled("Filter Wheels", new JPanel(new GridLayout(3, 4, 0, 2)));

        state = new JLabel("Filterwheel Position: ");
        filterwheelstate = new JComboBox(wheelstates);
        filterwheelstate.setName("state");
        filterwheelstate.addItemListener(this);
        filterwheelstate.setPreferredSize(new Dimension(30, 100));

        speed = new JLabel("Filterwheel Speed: ");
        filterwheelspeed = new JComboBox(wheelspeed);
        filterwheelspeed.setName("speed");
        filterwheelspeed.addItemListener(this);
        filterwheelspeed.setPreferredSize(new Dimension(30, 100));

        lasers.add(DAC1Slider);
        lasers.add(DAC2Slider);
        lasers.add(DAC3Slider);
        lasers.add(DAC4Slider);
        lasers.add(DAC5Slider);
        lasers.add(DAC6Slider);

        filterwheels.add(state);
        filterwheels.add(filterwheelstate);
        filterwheels.add(speed);
        filterwheels.add(filterwheelspeed);

        add(lasers);
        add(filterwheels);

               // IJ.log("Enabling DAC GUI...");
        DAC1Slider.setEnabled(setup.isConnected(SPIMSetup.SPIMDevice.DAC1));
        DAC2Slider.setEnabled(setup.isConnected(SPIMSetup.SPIMDevice.DAC2));
        DAC3Slider.setEnabled(setup.isConnected(SPIMSetup.SPIMDevice.DAC3));
        DAC4Slider.setEnabled(setup.isConnected(SPIMSetup.SPIMDevice.DAC4));
        DAC5Slider.setEnabled(setup.isConnected(SPIMSetup.SPIMDevice.DAC5));
        DAC6Slider.setEnabled(setup.isConnected(SPIMSetup.SPIMDevice.DAC6));

    }

    @Override
    public void setVisible(boolean show) {
        if (display == null) {
            display = new JFrame("DAC Manager");
            display.setPreferredSize(new Dimension(600, 450));
            display.add(this);
            display.pack();
        }

        display.setVisible(show);
    }

    @Override
    public void setEnabled(boolean b) {
            //super.setEnabled(b);

        DAC1Slider.setEnabled(b);
        DAC2Slider.setEnabled(b);
        DAC3Slider.setEnabled(b);
        DAC4Slider.setEnabled(b);
        DAC5Slider.setEnabled(b);
        DAC6Slider.setEnabled(b);
        filterwheelstate.setEnabled(b);
        filterwheelspeed.setEnabled(b);

    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        try {
            if (e.getSource() == filterwheelstate) {
                setup.getFilterWheel().setState(filterwheelstate.getSelectedIndex());
            } else {
                setup.getFilterWheel().setSpeed(filterwheelspeed.getSelectedIndex(), filterwheelstate.getSelectedIndex());
            }
        } catch (Exception ex) {
            Logger.getLogger(SPIMDACManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
