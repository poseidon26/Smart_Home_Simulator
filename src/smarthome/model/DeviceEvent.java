package smarthome.model;

/**
 * Defines the events that smart home devices can process.
 * These events are triggers that change the states of devices.
 */
public enum DeviceEvent {
    POWER_ON,           // Turn on the device
    POWER_OFF,          // Turn off the device
    ENTER_STANDBY,      // Enter standby mode
    EXIT_STANDBY,       // Exit standby mode
    ERROR_DETECTED,     // Error detected
    ERROR_RESOLVED,     // Error resolved
    START_MAINTENANCE,  // Enter maintenance mode
    END_MAINTENANCE,    // Exit maintenance mode
    ENTER_LOW_POWER,    // Enter low power mode
    EXIT_LOW_POWER,     // Exit low power mode
    ACTIVATE,           // Activate the device
    DEACTIVATE,         // Deactivate the device
    LOCK,               // Lock the device
    UNLOCK,             // Unlock the device
    USER_INTERACTION,   // User interaction
    SCHEDULED_EVENT,    // Scheduled event
    TEMPERATURE_CHANGE, // Temperature change
    MOTION_DETECTED,    // Motion detected
    DOOR_OPEN,          // Door opened
    DOOR_CLOSE,         // Door closed
    NETWORK_CONNECTED,  // Connected to network
    NETWORK_DISCONNECTED // Network connection lost
}