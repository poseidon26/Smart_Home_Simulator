package smarthome.simulation;

import smarthome.model.Device;
import smarthome.model.DeviceEvent;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Class defining a simulation scenario.
 * A scenario includes which devices are in the simulation and scheduled events.
 */
public class Scenario {
    private String name;
    private String description;
    private List<Device> devices = new ArrayList<>();
    private List<ScheduledEvent> scheduledEvents = new ArrayList<>();
    
    /**
     * Creates a simulation scenario.
     * 
     * @param name Scenario name
     * @param description Scenario description
     */
    public Scenario(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    /**
     * Adds a device to the scenario.
     * 
     * @param device Device to add
     * @return This scenario instance
     */
    public Scenario addDevice(Device device) {
        devices.add(device);
        return this;
    }
    
    /**
     * Adds a scheduled event to the scenario.
     * 
     * @param time Event time
     * @param deviceId ID of the device to which the event will be applied
     * @param event Event type
     * @return This scenario instance
     */
    public Scenario scheduleEvent(LocalTime time, String deviceId, DeviceEvent event) {
        scheduledEvents.add(new ScheduledEvent(time, deviceId, event));
        return this;
    }
    
    /**
     * Gets the events scheduled for the specified time.
     * 
     * @param currentTime Current time
     * @return List of events scheduled for the specified time
     */
    public List<ScheduledEvent> getEventsAt(LocalTime currentTime) {
        List<ScheduledEvent> events = new ArrayList<>();
        
        for (ScheduledEvent event : scheduledEvents) {
            // Check if the event time is the same as the current time (ignoring seconds)
            if (event.getTime().getHour() == currentTime.getHour() && 
                event.getTime().getMinute() == currentTime.getMinute()) {
                events.add(event);
            }
        }
        
        return events;
    }
    
    /**
     * Gets a device by its ID.
     * 
     * @param deviceId Device ID
     * @return Device with the specified ID or null if not found
     */
    public Device getDeviceById(String deviceId) {
        for (Device device : devices) {
            if (device.getId().equals(deviceId)) {
                return device;
            }
        }
        return null;
    }
    
    // Getter methods
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public List<Device> getDevices() {
        return devices;
    }
    
    public List<ScheduledEvent> getScheduledEvents() {
        return scheduledEvents;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append(String.format("Scenario: %s%n", name));
        sb.append(String.format("Description: %s%n", description));
        sb.append(String.format("Devices (%d):%n", devices.size()));
        
        for (Device device : devices) {
            sb.append(String.format("  - %s%n", device));
        }
        
        sb.append(String.format("Scheduled Events (%d):%n", scheduledEvents.size()));
        
        for (ScheduledEvent event : scheduledEvents) {
            sb.append(String.format("  - %s%n", event));
        }
        
        return sb.toString();
    }
    
    /**
     * Class representing a scheduled event.
     */
    public static class ScheduledEvent {
        private LocalTime time;
        private String deviceId;
        private DeviceEvent event;
        
        /**
         * Creates a scheduled event.
         * 
         * @param time Event time
         * @param deviceId Device ID
         * @param event Event type
         */
        public ScheduledEvent(LocalTime time, String deviceId, DeviceEvent event) {
            this.time = time;
            this.deviceId = deviceId;
            this.event = event;
        }
        
        public LocalTime getTime() {
            return time;
        }
        
        public String getDeviceId() {
            return deviceId;
        }
        
        public DeviceEvent getEvent() {
            return event;
        }
        
        @Override
        public String toString() {
            return String.format("%s - Device %s: %s", time, deviceId, event);
        }
    }
}