package smarthome.environment;

import smarthome.model.Device;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Class representing the smart home environment.
 * This class simulates environmental conditions inside and outside the house
 * (temperature, light, humidity, etc.) and time.
 */
public class HomeEnvironment {
    private double insideTemperature;
    private double outsideTemperature;
    private double humidity;
    private double lightLevel; // 0.0 (dark) - 1.0 (very bright)
    private boolean isDaylight;
    private boolean isRaining;
    private boolean isOccupied; // Is someone at home?
    private LocalDateTime currentTime;
    private Map<String, Object> environmentalData = new HashMap<>();
    
    /**
     * Creates a home environment with default values.
     */
    public HomeEnvironment() {
        this.insideTemperature = 22.0; // Celsius
        this.outsideTemperature = 15.0; // Celsius
        this.humidity = 50.0; // %
        this.lightLevel = 0.7;
        this.isDaylight = true;
        this.isRaining = false;
        this.isOccupied = true;
        this.currentTime = LocalDateTime.now();
    }
    
    /**
     * Advances time and updates environmental conditions.
     * 
     * @param minutesToAdvance Number of minutes to advance
     */
    public void advanceTime(int minutesToAdvance) {
        // Advance time
        currentTime = currentTime.plusMinutes(minutesToAdvance);
        
        // Update day/night condition
        int hour = currentTime.getHour();
        isDaylight = hour >= 6 && hour < 20;
        
        // Update light level based on time of day
        if (isDaylight) {
            // During day hours, light level varies
            if (hour < 8) {
                // Early morning
                lightLevel = 0.3 + (hour - 6) * 0.1;
            } else if (hour < 18) {
                // Daytime
                lightLevel = 0.7 + Math.sin((hour - 8) * Math.PI / 10) * 0.3;
            } else {
                // Evening
                lightLevel = 0.7 - (hour - 18) * 0.2;
            }
        } else {
            // Night time
            lightLevel = 0.1;
        }
        
        // Randomly update weather (for demo purposes)
        if (Math.random() < 0.01) {
            isRaining = !isRaining;
        }
        
        // Simulate outdoor temperature fluctuations
        outsideTemperature += (Math.random() - 0.5) * 0.2;
        
        // Indoor temperature is affected by outdoor temperature
        insideTemperature += (outsideTemperature - insideTemperature) * 0.02;
    }
    
    /**
     * Updates the environment based on changes in a device.
     * 
     * @param device Device affecting the environment
     */
    public void updateFromDevice(Device device) {
        switch (device.getType()) {
            case THERMOSTAT:
                // Thermostats affect inside temperature
                double thermEffect = (device.isOn()) ? 0.1 : 0.0;
                insideTemperature = insideTemperature * (1 - thermEffect) + 
                        getEnvironmentalData("targetTemperature", 22.0) * thermEffect;
                break;
                
            case AIR_CONDITIONER:
                // Air conditioners affect both temperature and humidity
                if (device.isOn()) {
                    insideTemperature = insideTemperature * 0.9 + 
                            getEnvironmentalData("targetTemperature", 22.0) * 0.1;
                    humidity = humidity * 0.9 + 45.0 * 0.1; // Reduce humidity slightly
                }
                break;
                
            case LIGHT:
                // Lights affect the light level
                if (device.isOn()) {
                    lightLevel = Math.min(1.0, lightLevel + 0.2);
                }
                break;
                
            default:
                // Other devices might have their specific effects
                break;
        }
    }
    
    /**
     * Sets environmental data by key.
     * 
     * @param key Data key
     * @param value Data value
     */
    public void setEnvironmentalData(String key, Object value) {
        environmentalData.put(key, value);
    }
    
    /**
     * Gets environmental data by key.
     * 
     * @param key Data key
     * @return Data value or null if not found
     */
    public Object getEnvironmentalData(String key) {
        return environmentalData.get(key);
    }
    
    /**
     * Convenience method to get environmental data with a default value.
     */
    @SuppressWarnings("unchecked")
    private <T> T getEnvironmentalData(String key, T defaultValue) {
        return (T) environmentalData.getOrDefault(key, defaultValue);
    }
    
    // Getters and setters
    
    public double getInsideTemperature() {
        return insideTemperature;
    }
    
    public void setInsideTemperature(double insideTemperature) {
        this.insideTemperature = insideTemperature;
    }
    
    public double getOutsideTemperature() {
        return outsideTemperature;
    }
    
    public double getHumidity() {
        return humidity;
    }
    
    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }
    
    public double getLightLevel() {
        return lightLevel;
    }
    
    public boolean isDaylight() {
        return isDaylight;
    }
    
    public boolean isRaining() {
        return isRaining;
    }
    
    public boolean isOccupied() {
        return isOccupied;
    }
    
    public void setOccupied(boolean occupied) {
        isOccupied = occupied;
    }
    
    public LocalDateTime getCurrentTime() {
        return currentTime;
    }
    
    /**
     * Gets a report of the current environmental conditions.
     * 
     * @return Environment status report
     */
    public String getEnvironmentReport() {
        return String.format(
            "Environment Status at %s\n" +
            "-------------------------\n" +
            "Indoor Temperature: %.1f°C\n" +
            "Outdoor Temperature: %.1f°C\n" +
            "Humidity: %.1f%%\n" +
            "Light Level: %.2f\n" +
            "Time of Day: %s\n" +
            "Weather: %s\n" +
            "Occupied: %s",
            currentTime.toString(),
            insideTemperature,
            outsideTemperature,
            humidity,
            lightLevel,
            isDaylight ? "Day" : "Night",
            isRaining ? "Raining" : "Clear",
            isOccupied ? "Yes" : "No"
        );
    }
}