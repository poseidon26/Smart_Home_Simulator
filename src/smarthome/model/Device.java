package smarthome.model;

import fsm.FSM;

/**
 * Abstract class representing a device in the smart home system.
 * Each device contains its own FSM and can change its state.
 */
public abstract class Device {
    protected String id;
    protected String name;
    protected DeviceType type;
    protected boolean isOn;
    protected FSM<DeviceState, DeviceEvent> fsm;
    protected double energyConsumption; // in kWh units
    
    /**
     * Creates a device.
     *
     * @param id Unique identifier of the device
     * @param name User-friendly name of the device
     * @param type Type of the device
     */
    public Device(String id, String name, DeviceType type) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.isOn = false;
        this.energyConsumption = 0.0;
        initializeFSM();
    }
    
    /**
     * Initializes the device's FSM. Must be implemented by subclasses.
     */
    protected abstract void initializeFSM();
    
    /**
     * Updates the device and processes events.
     *
     * @param event Event to be processed
     * @return Whether a state change occurred
     */
    public boolean processEvent(DeviceEvent event) {
        boolean changed = fsm.processEvent(event);
        updateDevice();
        return changed;
    }
    
    /**
     * Updates the device's state according to the FSM.
     * Can be overridden by subclasses when necessary.
     */
    protected void updateDevice() {
        DeviceState currentState = fsm.getCurrentState();
        isOn = currentState != DeviceState.OFF;
    }
    
    /**
     * Calculates the instantaneous energy consumption of the device.
     * Must be implemented by subclasses.
     *
     * @return Energy consumption (kWh)
     */
    public abstract double calculateEnergyConsumption();
    
    /**
     * Reports the current status of the device.
     *
     * @return Information about the device's status
     */
    public String getStatusReport() {
        return String.format(
            "Device: %s (ID: %s, Type: %s)\n" +
            "State: %s\n" +
            "Power: %s\n" +
            "Energy Consumption: %.2f kWh",
            name, id, type, fsm.getCurrentState(), isOn ? "ON" : "OFF", energyConsumption
        );
    }
    
    // Getter and Setter methods
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public DeviceType getType() {
        return type;
    }
    
    public boolean isOn() {
        return isOn;
    }
    
    public DeviceState getCurrentState() {
        return fsm.getCurrentState();
    }
    
    public double getEnergyConsumption() {
        return energyConsumption;
    }
    
    @Override
    public String toString() {
        return String.format("%s (%s): %s", name, type, fsm.getCurrentState());
    }
}