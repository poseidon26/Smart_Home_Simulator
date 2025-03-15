package smarthome.devices;

import fsm.FSM;
import smarthome.model.Device;
import smarthome.model.DeviceEvent;
import smarthome.model.DeviceState;
import smarthome.model.DeviceType;

/**
 * Class representing a smart thermostat.
 * The thermostat can measure temperature and control heating/cooling systems.
 */
public class Thermostat extends Device {
    private double targetTemperature;
    private double currentTemperature;
    private ThermostatMode mode;
    private static final double DEFAULT_TEMPERATURE = 22.0; // Celsius
    private static final double DEFAULT_ENERGY_RATE = 0.5; // kWh
    
    /**
     * Defines thermostat operating modes.
     */
    public enum ThermostatMode {
        HEAT,    // Heating mode
        COOL,    // Cooling mode
        AUTO,    // Automatic mode (both heating and cooling)
        FAN_ONLY, // Fan only mode
        OFF      // Off
    }
    
    /**
     * Creates a thermostat with default settings.
     * 
     * @param id Thermostat ID
     * @param name Thermostat name
     */
    public Thermostat(String id, String name) {
        super(id, name, DeviceType.THERMOSTAT);
        this.targetTemperature = DEFAULT_TEMPERATURE;
        this.currentTemperature = DEFAULT_TEMPERATURE;
        this.mode = ThermostatMode.OFF;
    }
    
    @Override
    protected void initializeFSM() {
        // Initialize the Thermostat FSM
        fsm = new FSM<>(DeviceState.OFF);
        
        // Basic state transitions
        fsm.addTransition(DeviceState.OFF, DeviceEvent.POWER_ON, DeviceState.STARTING)
           .addTransition(DeviceState.STARTING, DeviceEvent.ACTIVATE, DeviceState.ON)
           .addTransition(DeviceState.ON, DeviceEvent.POWER_OFF, DeviceState.STOPPING)
           .addTransition(DeviceState.STOPPING, DeviceEvent.DEACTIVATE, DeviceState.OFF)
           .addTransition(DeviceState.ON, DeviceEvent.ENTER_STANDBY, DeviceState.STANDBY)
           .addTransition(DeviceState.STANDBY, DeviceEvent.EXIT_STANDBY, DeviceState.ON)
           .addTransition(DeviceState.ON, DeviceEvent.ERROR_DETECTED, DeviceState.ERROR)
           .addTransition(DeviceState.ERROR, DeviceEvent.ERROR_RESOLVED, DeviceState.ON)
           .addTransition(DeviceState.ON, DeviceEvent.ENTER_LOW_POWER, DeviceState.LOW_POWER)
           .addTransition(DeviceState.LOW_POWER, DeviceEvent.EXIT_LOW_POWER, DeviceState.ON);
        
        // Thermostat-specific states
        fsm.addStateChangeListener((oldState, newState, event) -> {
            System.out.printf("Thermostat '%s' state changed: %s -> %s due to %s%n",
                    getName(), oldState, newState, event);
            
            // Update mode based on state change
            if (newState == DeviceState.OFF) {
                setMode(ThermostatMode.OFF);
            } else if (newState == DeviceState.ON && oldState == DeviceState.STARTING) {
                setMode(ThermostatMode.AUTO); // Switch to automatic mode when turned on
            }
        });
    }
    
    @Override
    protected void updateDevice() {
        super.updateDevice();
        
        // Update energy consumption based on device state
        energyConsumption = calculateEnergyConsumption();
        
        // Simulate real temperature control
        if (isOn() && mode != ThermostatMode.OFF) {
            adjustTemperature();
        }
    }
    
    /**
     * Adjusts the temperature according to the current state.
     * This method simulates the operation of a real thermostat.
     */
    private void adjustTemperature() {
        double temperatureDiff = targetTemperature - currentTemperature;
        
        switch (mode) {
            case HEAT:
                if (temperatureDiff > 0) {
                    // Heating needed
                    currentTemperature += Math.min(0.5, temperatureDiff);
                }
                break;
                
            case COOL:
                if (temperatureDiff < 0) {
                    // Cooling needed
                    currentTemperature += Math.max(-0.5, temperatureDiff);
                }
                break;
                
            case AUTO:
                // In automatic mode, both heating and cooling can be done
                if (Math.abs(temperatureDiff) > 0.5) {
                    currentTemperature += Math.signum(temperatureDiff) * 0.5;
                }
                break;
                
            case FAN_ONLY:
                // Only air circulation, temperature doesn't change
                break;
                
            case OFF:
                // No heating/cooling, slowly change towards natural ambient temperature
                // (In this example, we use a fixed "ambient" temperature for simplicity)
                double ambientTemp = 20.0;
                currentTemperature += (ambientTemp - currentTemperature) * 0.1;
                break;
        }
    }
    
    @Override
    public double calculateEnergyConsumption() {
        if (!isOn()) {
            return 0.0;
        }
        
        double baseConsumption = 0.1; // Standby consumption
        
        switch (mode) {
            case HEAT:
                // Heating typically consumes more energy
                return baseConsumption + (Math.max(0, targetTemperature - currentTemperature) * 0.2);
                
            case COOL:
                // Cooling also consumes significant energy
                return baseConsumption + (Math.max(0, currentTemperature - targetTemperature) * 0.3);
                
            case AUTO:
                // If actively heating or cooling
                double tempDiff = Math.abs(targetTemperature - currentTemperature);
                return baseConsumption + (tempDiff > 0.5 ? tempDiff * 0.25 : 0);
                
            case FAN_ONLY:
                // Only the fan is running
                return baseConsumption + 0.1;
                
            case OFF:
            default:
                return 0.0;
        }
    }
    
    /**
     * Sets the target temperature of the thermostat.
     * 
     * @param temperature New target temperature (Celsius)
     */
    public void setTargetTemperature(double temperature) {
        this.targetTemperature = temperature;
        
        if (isOn()) {
            // Process temperature change event
            processEvent(DeviceEvent.TEMPERATURE_CHANGE);
        }
    }
    
    /**
     * Sets the operating mode of the thermostat.
     * 
     * @param mode New operating mode
     */
    public void setMode(ThermostatMode mode) {
        if (this.mode != mode) {
            this.mode = mode;
            
            if (mode == ThermostatMode.OFF) {
                if (isOn()) {
                    processEvent(DeviceEvent.POWER_OFF);
                }
            } else {
                if (!isOn()) {
                    processEvent(DeviceEvent.POWER_ON);
                }
            }
        }
    }
    
    // Getter methods
    
    public double getTargetTemperature() {
        return targetTemperature;
    }
    
    public double getCurrentTemperature() {
        return currentTemperature;
    }
    
    public ThermostatMode getMode() {
        return mode;
    }
    
    @Override
    public String getStatusReport() {
        return String.format(
            "%s\n" +
            "Mode: %s\n" +
            "Current Temperature: %.1f°C\n" +
            "Target Temperature: %.1f°C",
            super.getStatusReport(), mode, currentTemperature, targetTemperature
        );
    }
}