package smarthome.devices;

import fsm.FSM;
import smarthome.model.Device;
import smarthome.model.DeviceEvent;
import smarthome.model.DeviceState;
import smarthome.model.DeviceType;

import java.awt.Color;

/**
 * Class representing a smart lighting system.
 * The light can be turned on and off, brightness can be adjusted, and color can be changed.
 */
public class Light extends Device {
    private int brightness; // between 0-100
    private Color color;
    private boolean isDimmable;
    private boolean isColorChangeable;
    private static final int DEFAULT_BRIGHTNESS = 80;
    private static final Color DEFAULT_COLOR = Color.WHITE;
    private static final double MAX_ENERGY_CONSUMPTION = 0.06; // kWh
    
    /**
     * Creates a light with default settings (white, fixed).
     * 
     * @param id Light ID
     * @param name Light name
     */
    public Light(String id, String name) {
        this(id, name, false, false);
    }
    
    /**
     * Creates a light with customizable settings.
     * 
     * @param id Light ID
     * @param name Light name
     * @param isDimmable Can it be dimmed?
     * @param isColorChangeable Can the color be changed?
     */
    public Light(String id, String name, boolean isDimmable, boolean isColorChangeable) {
        super(id, name, DeviceType.LIGHT);
        this.brightness = DEFAULT_BRIGHTNESS;
        this.color = DEFAULT_COLOR;
        this.isDimmable = isDimmable;
        this.isColorChangeable = isColorChangeable;
    }
    
    @Override
    protected void initializeFSM() {
        // Initialize the Light FSM
        fsm = new FSM<>(DeviceState.OFF);
        
        // Basic state transitions
        fsm.addTransition(DeviceState.OFF, DeviceEvent.POWER_ON, DeviceState.ON)
           .addTransition(DeviceState.ON, DeviceEvent.POWER_OFF, DeviceState.OFF)
           .addTransition(DeviceState.ON, DeviceEvent.ENTER_LOW_POWER, DeviceState.LOW_POWER)
           .addTransition(DeviceState.LOW_POWER, DeviceEvent.EXIT_LOW_POWER, DeviceState.ON)
           .addTransition(DeviceState.ON, DeviceEvent.ERROR_DETECTED, DeviceState.ERROR)
           .addTransition(DeviceState.ERROR, DeviceEvent.ERROR_RESOLVED, DeviceState.ON);
        
        // State change listener
        fsm.addStateChangeListener((oldState, newState, event) -> {
            System.out.printf("Light '%s' state changed: %s -> %s due to %s%n",
                    getName(), oldState, newState, event);
            
            // Update light settings according to state change
            if (newState == DeviceState.OFF) {
                // When turned off
            } else if (newState == DeviceState.ON && oldState == DeviceState.OFF) {
                // When turned on
            } else if (newState == DeviceState.LOW_POWER) {
                // When entering low power mode
                setBrightness(Math.min(30, brightness)); // Reduce brightness
            }
        });
    }
    
    @Override
    protected void updateDevice() {
        super.updateDevice();
        
        // Update energy consumption based on device state
        energyConsumption = calculateEnergyConsumption();
    }
    
    @Override
    public double calculateEnergyConsumption() {
        if (!isOn()) {
            return 0.0;
        }
        
        // Calculate energy consumption based on brightness and device state
        double brightnessRatio = brightness / 100.0;
        
        if (fsm.getCurrentState() == DeviceState.LOW_POWER) {
            // Uses less energy in low power mode
            return MAX_ENERGY_CONSUMPTION * brightnessRatio * 0.5;
        } else {
            return MAX_ENERGY_CONSUMPTION * brightnessRatio;
        }
    }
    
    /**
     * Adjusts the brightness of the light.
     * 
     * @param brightness Brightness level (0-100)
     * @throws IllegalArgumentException If the light is not dimmable
     */
    public void setBrightness(int brightness) {
        if (!isDimmable && brightness != 0 && brightness != 100) {
            throw new IllegalArgumentException("This light is not dimmable");
        }
        
        if (brightness < 0 || brightness > 100) {
            throw new IllegalArgumentException("Brightness must be between 0 and 100");
        }
        
        this.brightness = brightness;
        
        // If light is completely turned off
        if (brightness == 0 && isOn()) {
            processEvent(DeviceEvent.POWER_OFF);
        } 
        // If light is turned on
        else if (brightness > 0 && !isOn()) {
            processEvent(DeviceEvent.POWER_ON);
        }
        // If brightness is low and light is on
        else if (brightness < 30 && isOn() && fsm.getCurrentState() != DeviceState.LOW_POWER) {
            processEvent(DeviceEvent.ENTER_LOW_POWER);
        }
        // If brightness is increased and in low power mode
        else if (brightness >= 30 && fsm.getCurrentState() == DeviceState.LOW_POWER) {
            processEvent(DeviceEvent.EXIT_LOW_POWER);
        }
    }
    
    /**
     * Sets the color of the light.
     * 
     * @param color New color
     * @throws IllegalArgumentException If the light does not support color change
     */
    public void setColor(Color color) {
        if (!isColorChangeable) {
            throw new IllegalArgumentException("This light does not support color change");
        }
        
        this.color = color;
    }
    
    /**
     * Turns on the light.
     */
    public void turnOn() {
        if (!isOn()) {
            processEvent(DeviceEvent.POWER_ON);
        }
    }
    
    /**
     * Turns off the light.
     */
    public void turnOff() {
        if (isOn()) {
            processEvent(DeviceEvent.POWER_OFF);
        }
    }
    
    // Getter methods
    
    public int getBrightness() {
        return brightness;
    }
    
    public Color getColor() {
        return color;
    }
    
    public boolean isDimmable() {
        return isDimmable;
    }
    
    public boolean isColorChangeable() {
        return isColorChangeable;
    }
    
    @Override
    public String getStatusReport() {
        String colorHex = String.format("#%02x%02x%02x", 
                color.getRed(), color.getGreen(), color.getBlue());
        
        return String.format(
            "%s\n" +
            "Brightness: %d%%\n" +
            "Color: %s\n" +
            "Features: %s%s",
            super.getStatusReport(), brightness, colorHex,
            isDimmable ? "Dimmable " : "",
            isColorChangeable ? "ColorChangeable" : ""
        );
    }
}