package smarthome.model;

/**
 * Defines the possible states of smart home devices.
 * General states for all device types are defined in this enum.
 * Device-specific states can be defined as enums in subclasses.
 */
public enum DeviceState {
    OFF,            // Device is off
    ON,             // Device is on
    STANDBY,        // Device is in standby mode
    ERROR,          // Device is in error state
    MAINTENANCE,    // Device is in maintenance mode
    LOW_POWER,      // Device is in low power mode
    STARTING,       // Device is starting
    STOPPING,       // Device is stopping
    ACTIVE,         // Device is actively being used
    SUSPENDED,      // Device is suspended
    LOCKED          // Device is locked
}