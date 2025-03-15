package smarthome.simulation;

import smarthome.devices.Light;
import smarthome.devices.Thermostat;
import smarthome.devices.Thermostat.ThermostatMode;
import smarthome.environment.HomeEnvironment;
import smarthome.model.Device;
import smarthome.model.DeviceEvent;

import java.awt.Color;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Main class managing the smart home simulation.
 */
public class SmartHomeSimulator {
    private HomeEnvironment environment;
    private List<Scenario> scenarios;
    private Scenario currentScenario;
    private boolean isRunning;
    private int simulationSpeed = 1; // 1x real time
    
    /**
     * Initializes the smart home simulator.
     */
    public SmartHomeSimulator() {
        this.environment = new HomeEnvironment();
        this.scenarios = new ArrayList<>();
        this.isRunning = false;
        
        // Create sample scenarios
        createSampleScenarios();
    }
    
    /**
     * Starts the simulation.
     */
    public void startSimulation() {
        isRunning = true;
        showMainMenu();
    }
    
    /**
     * Displays the main menu and processes user input.
     */
    private void showMainMenu() {
        Scanner scanner = new Scanner(System.in);
        
        while (isRunning) {
            System.out.println("\n==== Smart Home Simulator ====");
            System.out.println("1. List Scenarios");
            System.out.println("2. Run Scenario");
            System.out.println("3. Create New Scenario");
            System.out.println("4. View Environment Status");
            System.out.println("5. View Device Status");
            System.out.println("6. Send Event to Device");
            System.out.println("7. Fast Forward Time");
            System.out.println("8. Quit");
            System.out.print("Select an option: ");
            
            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }
            
            switch (choice) {
                case 1:
                    listScenarios();
                    break;
                    
                case 2:
                    runScenario(scanner);
                    break;
                    
                case 3:
                    createNewScenario(scanner);
                    break;
                    
                case 4:
                    viewEnvironmentStatus();
                    break;
                    
                case 5:
                    viewDeviceStatus(scanner);
                    break;
                    
                case 6:
                    sendEventToDevice(scanner);
                    break;
                    
                case 7:
                    fastForwardTime(scanner);
                    break;
                    
                case 8:
                    isRunning = false;
                    System.out.println("Exiting Smart Home Simulator. Goodbye!");
                    break;
                    
                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }
        }
    }
    
    /**
     * Lists all available scenarios.
     */
    private void listScenarios() {
        System.out.println("\n==== Available Scenarios ====");
        
        if (scenarios.isEmpty()) {
            System.out.println("No scenarios available. Create a new scenario first.");
            return;
        }
        
        for (int i = 0; i < scenarios.size(); i++) {
            Scenario scenario = scenarios.get(i);
            System.out.printf("%d. %s - %s (Devices: %d, Events: %d)%n",
                    i + 1, scenario.getName(), scenario.getDescription(),
                    scenario.getDevices().size(), scenario.getScheduledEvents().size());
        }
    }
    
    /**
     * Runs a selected scenario.
     */
    private void runScenario(Scanner scanner) {
        if (scenarios.isEmpty()) {
            System.out.println("No scenarios available. Create a new scenario first.");
            return;
        }
        
        listScenarios();
        System.out.print("Select a scenario number: ");
        
        int scenarioIndex;
        try {
            scenarioIndex = Integer.parseInt(scanner.nextLine()) - 1;
            
            if (scenarioIndex < 0 || scenarioIndex >= scenarios.size()) {
                System.out.println("Invalid scenario number.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return;
        }
        
        currentScenario = scenarios.get(scenarioIndex);
        System.out.println("Selected scenario: " + currentScenario.getName());
        
        System.out.print("Enter simulation duration (minutes): ");
        int durationMinutes;
        try {
            durationMinutes = Integer.parseInt(scanner.nextLine());
            
            if (durationMinutes <= 0) {
                System.out.println("Duration must be positive.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return;
        }
        
        simulateScenario(currentScenario, durationMinutes);
    }
    
    /**
     * Simulates the selected scenario for the specified duration.
     */
    private void simulateScenario(Scenario scenario, int durationMinutes) {
        System.out.printf("Starting simulation of scenario '%s' for %d minutes%n",
                scenario.getName(), durationMinutes);
        
        LocalDateTime startTime = environment.getCurrentTime();
        LocalDateTime endTime = startTime.plusMinutes(durationMinutes);
        
        // Set up the initial environment
        for (Device device : scenario.getDevices()) {
            System.out.println("Initializing device: " + device.getName());
        }
        
        System.out.println("\n==== Simulation Started ====");
        
        // Simulation loop
        while (environment.getCurrentTime().isBefore(endTime)) {
            // Advance time by 1 minute (adjusted by simulation speed)
            environment.advanceTime(1 * simulationSpeed);
            
            LocalTime currentTime = environment.getCurrentTime().toLocalTime();
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            
            // Process scheduled events
            List<Scenario.ScheduledEvent> eventsNow = scenario.getEventsAt(currentTime);
            
            if (!eventsNow.isEmpty()) {
                System.out.printf("[%s] Processing %d scheduled events%n",
                        currentTime.format(timeFormatter), eventsNow.size());
                
                for (Scenario.ScheduledEvent event : eventsNow) {
                    Device device = scenario.getDeviceById(event.getDeviceId());
                    
                    if (device != null) {
                        System.out.printf("[%s] Device '%s' processing event: %s%n",
                                currentTime.format(timeFormatter), device.getName(), event.getEvent());
                        
                        device.processEvent(event.getEvent());
                        environment.updateFromDevice(device);
                    } else {
                        System.out.printf("[%s] WARNING: Device with ID %s not found%n",
                                currentTime.format(timeFormatter), event.getDeviceId());
                    }
                }
            }
            
            // Simulate the passage of time
            try {
                Thread.sleep(1000 / simulationSpeed); // 1 second per minute of simulation time
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        System.out.println("\n==== Simulation Completed ====");
        System.out.println("Final environment status:");
        System.out.println(environment.getEnvironmentReport());
        
        System.out.println("\nDevice statuses:");
        for (Device device : scenario.getDevices()) {
            System.out.println("\n" + device.getStatusReport());
        }
    }
    
    /**
     * Creates a new scenario based on user input.
     */
    private void createNewScenario(Scanner scanner) {
        System.out.println("\n==== Create New Scenario ====");
        
        System.out.print("Enter scenario name: ");
        String name = scanner.nextLine();
        
        System.out.print("Enter scenario description: ");
        String description = scanner.nextLine();
        
        Scenario newScenario = new Scenario(name, description);
        
        boolean adding = true;
        while (adding) {
            System.out.println("\n1. Add Thermostat");
            System.out.println("2. Add Light");
            System.out.println("3. Add Event");
            System.out.println("4. Done");
            System.out.print("Select an option: ");
            
            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }
            
            switch (choice) {
                case 1:
                    addThermostatToScenario(scanner, newScenario);
                    break;
                    
                case 2:
                    addLightToScenario(scanner, newScenario);
                    break;
                    
                case 3:
                    addEventToScenario(scanner, newScenario);
                    break;
                    
                case 4:
                    adding = false;
                    break;
                    
                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }
        }
        
        if (!newScenario.getDevices().isEmpty()) {
            scenarios.add(newScenario);
            System.out.println("New scenario created: " + newScenario.getName());
        } else {
            System.out.println("Scenario creation canceled. No devices were added.");
        }
    }
    
    /**
     * Adds a thermostat to a scenario.
     */
    private void addThermostatToScenario(Scanner scanner, Scenario scenario) {
        System.out.println("\n==== Add Thermostat ====");
        
        System.out.print("Enter thermostat ID: ");
        String id = scanner.nextLine();
        
        if (scenario.getDeviceById(id) != null) {
            System.out.println("A device with this ID already exists in the scenario.");
            return;
        }
        
        System.out.print("Enter thermostat name: ");
        String name = scanner.nextLine();
        
        Thermostat thermostat = new Thermostat(id, name);
        
        // Set initial target temperature
        System.out.print("Enter initial target temperature (°C): ");
        try {
            double targetTemp = Double.parseDouble(scanner.nextLine());
            thermostat.setTargetTemperature(targetTemp);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Using default temperature.");
        }
        
        // Set initial mode
        System.out.println("Select initial mode:");
        System.out.println("1. Heat");
        System.out.println("2. Cool");
        System.out.println("3. Auto");
        System.out.println("4. Fan Only");
        System.out.println("5. Off");
        System.out.print("Select an option: ");
        
        try {
            int modeChoice = Integer.parseInt(scanner.nextLine());
            
            switch (modeChoice) {
                case 1:
                    thermostat.setMode(ThermostatMode.HEAT);
                    break;
                    
                case 2:
                    thermostat.setMode(ThermostatMode.COOL);
                    break;
                    
                case 3:
                    thermostat.setMode(ThermostatMode.AUTO);
                    break;
                    
                case 4:
                    thermostat.setMode(ThermostatMode.FAN_ONLY);
                    break;
                    
                case 5:
                    thermostat.setMode(ThermostatMode.OFF);
                    break;
                    
                default:
                    System.out.println("Invalid option. Using default mode (OFF).");
                    break;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Using default mode (OFF).");
        }
        
        scenario.addDevice(thermostat);
        System.out.println("Thermostat added: " + thermostat.getName());
    }
    
    /**
     * Adds a light to a scenario.
     */
    private void addLightToScenario(Scanner scanner, Scenario scenario) {
        System.out.println("\n==== Add Light ====");
        
        System.out.print("Enter light ID: ");
        String id = scanner.nextLine();
        
        if (scenario.getDeviceById(id) != null) {
            System.out.println("A device with this ID already exists in the scenario.");
            return;
        }
        
        System.out.print("Enter light name: ");
        String name = scanner.nextLine();
        
        System.out.print("Is the light dimmable? (y/n): ");
        boolean isDimmable = scanner.nextLine().toLowerCase().startsWith("y");
        
        System.out.print("Can the light change color? (y/n): ");
        boolean isColorChangeable = scanner.nextLine().toLowerCase().startsWith("y");
        
        Light light = new Light(id, name, isDimmable, isColorChangeable);
        
        // Set initial state
        System.out.print("Should the light be on initially? (y/n): ");
        boolean initiallyOn = scanner.nextLine().toLowerCase().startsWith("y");
        
        if (initiallyOn) {
            light.turnOn();
            
            // If dimmable, set brightness
            if (isDimmable) {
                System.out.print("Enter initial brightness (0-100): ");
                try {
                    int brightness = Integer.parseInt(scanner.nextLine());
                    light.setBrightness(brightness);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Using default brightness.");
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage() + " Using default brightness.");
                }
            }
            
            // If color changeable, set color
            if (isColorChangeable) {
                System.out.println("Select initial color:");
                System.out.println("1. White");
                System.out.println("2. Red");
                System.out.println("3. Green");
                System.out.println("4. Blue");
                System.out.println("5. Warm White");
                System.out.print("Select an option: ");
                
                try {
                    int colorChoice = Integer.parseInt(scanner.nextLine());
                    
                    Color color;
                    switch (colorChoice) {
                        case 1:
                            color = Color.WHITE;
                            break;
                            
                        case 2:
                            color = Color.RED;
                            break;
                            
                        case 3:
                            color = Color.GREEN;
                            break;
                            
                        case 4:
                            color = Color.BLUE;
                            break;
                            
                        case 5:
                            color = new Color(255, 224, 189); // Warm white
                            break;
                            
                        default:
                            System.out.println("Invalid option. Using default color (White).");
                            color = Color.WHITE;
                            break;
                    }
                    
                    try {
                        light.setColor(color);
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Using default color (White).");
                }
            }
        }
        
        scenario.addDevice(light);
        System.out.println("Light added: " + light.getName());
    }
    
    /**
     * Adds an event to a scenario.
     */
    private void addEventToScenario(Scanner scanner, Scenario scenario) {
        System.out.println("\n==== Add Event ====");
        
        if (scenario.getDevices().isEmpty()) {
            System.out.println("No devices available in this scenario. Add a device first.");
            return;
        }
        
        // List available devices
        System.out.println("Available devices:");
        for (Device device : scenario.getDevices()) {
            System.out.printf("  - %s (ID: %s, Type: %s)%n",
                    device.getName(), device.getId(), device.getType());
        }
        
        // Select device
        System.out.print("Enter device ID: ");
        String deviceId = scanner.nextLine();
        
        Device device = scenario.getDeviceById(deviceId);
        if (device == null) {
            System.out.println("Device not found. Please try again.");
            return;
        }
        
        // Enter event time
        System.out.print("Enter event time (HH:MM): ");
        String timeStr = scanner.nextLine();
        
        LocalTime eventTime;
        try {
            eventTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            System.out.println("Invalid time format. Please use HH:MM format.");
            return;
        }
        
        // Select event type
        System.out.println("Select event type:");
        DeviceEvent[] events = DeviceEvent.values();
        
        for (int i = 0; i < events.length; i++) {
            System.out.printf("%d. %s%n", i + 1, events[i]);
        }
        
        System.out.print("Select an option: ");
        
        int eventChoice;
        try {
            eventChoice = Integer.parseInt(scanner.nextLine()) - 1;
            
            if (eventChoice < 0 || eventChoice >= events.length) {
                System.out.println("Invalid event number.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
            return;
        }
        
        DeviceEvent selectedEvent = events[eventChoice];
        
        // Add the event to the scenario
        scenario.scheduleEvent(eventTime, deviceId, selectedEvent);
        System.out.printf("Event added: %s - Device %s: %s%n",
                eventTime, device.getName(), selectedEvent);
    }
    
    /**
     * Displays the current environment status.
     */
    private void viewEnvironmentStatus() {
        System.out.println("\n==== Environment Status ====");
        System.out.println(environment.getEnvironmentReport());
    }
    
    /**
     * Displays the status of a selected device.
     */
    private void viewDeviceStatus(Scanner scanner) {
        if (currentScenario == null || currentScenario.getDevices().isEmpty()) {
            System.out.println("No devices available. Run a scenario first.");
            return;
        }
        
        System.out.println("\n==== Device Status ====");
        
        // List available devices
        System.out.println("Available devices:");
        for (Device device : currentScenario.getDevices()) {
            System.out.printf("  - %s (ID: %s, Type: %s)%n",
                    device.getName(), device.getId(), device.getType());
        }
        
        // Select device
        System.out.print("Enter device ID (or press Enter for all): ");
        String deviceId = scanner.nextLine();
        
        if (deviceId.isEmpty()) {
            // Show all devices
            for (Device device : currentScenario.getDevices()) {
                System.out.println("\n" + device.getStatusReport());
            }
        } else {
            // Show specific device
            Device device = currentScenario.getDeviceById(deviceId);
            
            if (device == null) {
                System.out.println("Device not found. Please try again.");
                return;
            }
            
            System.out.println(device.getStatusReport());
        }
    }
    
    /**
     * Sends an event to a selected device.
     */
    private void sendEventToDevice(Scanner scanner) {
        if (currentScenario == null || currentScenario.getDevices().isEmpty()) {
            System.out.println("No devices available. Run a scenario first.");
            return;
        }
        
        System.out.println("\n==== Send Event to Device ====");
        
        // List available devices
        System.out.println("Available devices:");
        for (Device device : currentScenario.getDevices()) {
            System.out.printf("  - %s (ID: %s, Type: %s)%n",
                    device.getName(), device.getId(), device.getType());
        }
        
        // Select device
        System.out.print("Enter device ID: ");
        String deviceId = scanner.nextLine();
        
        Device device = currentScenario.getDeviceById(deviceId);
        if (device == null) {
            System.out.println("Device not found. Please try again.");
            return;
        }
        
        // Select event type or specific action
        System.out.println("Select action:");
        
        if (device instanceof Light) {
            Light light = (Light) device;
            
            System.out.println("1. Turn On");
            System.out.println("2. Turn Off");
            
            if (light.isDimmable()) {
                System.out.println("3. Set Brightness");
            }
            
            if (light.isColorChangeable()) {
                System.out.println("4. Change Color");
            }
            
            System.out.println("5. Send Event");
            
            System.out.print("Select an option: ");
            
            try {
                int actionChoice = Integer.parseInt(scanner.nextLine());
                
                switch (actionChoice) {
                    case 1:
                        light.turnOn();
                        System.out.println("Light turned on.");
                        break;
                        
                    case 2:
                        light.turnOff();
                        System.out.println("Light turned off.");
                        break;
                        
                    case 3:
                        if (light.isDimmable()) {
                            System.out.print("Enter brightness (0-100): ");
                            try {
                                int brightness = Integer.parseInt(scanner.nextLine());
                                light.setBrightness(brightness);
                                System.out.println("Brightness set to " + brightness + "%");
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid input. Please enter a number.");
                            } catch (IllegalArgumentException e) {
                                System.out.println(e.getMessage());
                            }
                        } else {
                            System.out.println("This light is not dimmable.");
                        }
                        break;
                        
                    case 4:
                        if (light.isColorChangeable()) {
                            System.out.println("Select color:");
                            System.out.println("1. White");
                            System.out.println("2. Red");
                            System.out.println("3. Green");
                            System.out.println("4. Blue");
                            System.out.println("5. Warm White");
                            
                            System.out.print("Select an option: ");
                            
                            try {
                                int colorChoice = Integer.parseInt(scanner.nextLine());
                                
                                Color color;
                                switch (colorChoice) {
                                    case 1:
                                        color = Color.WHITE;
                                        break;
                                        
                                    case 2:
                                        color = Color.RED;
                                        break;
                                        
                                    case 3:
                                        color = Color.GREEN;
                                        break;
                                        
                                    case 4:
                                        color = Color.BLUE;
                                        break;
                                        
                                    case 5:
                                        color = new Color(255, 224, 189); // Warm white
                                        break;
                                        
                                    default:
                                        System.out.println("Invalid option.");
                                        return;
                                }
                                
                                try {
                                    light.setColor(color);
                                    System.out.println("Color changed.");
                                } catch (IllegalArgumentException e) {
                                    System.out.println(e.getMessage());
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid input. Please enter a number.");
                            }
                        } else {
                            System.out.println("This light does not support color change.");
                        }
                        break;
                        
                    case 5:
                        sendGenericEvent(scanner, device);
                        break;
                        
                    default:
                        System.out.println("Invalid option.");
                        break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        } else if (device instanceof Thermostat) {
            Thermostat thermostat = (Thermostat) device;
            
            System.out.println("1. Set Target Temperature");
            System.out.println("2. Change Mode");
            System.out.println("3. Send Event");
            
            System.out.print("Select an option: ");
            
            try {
                int actionChoice = Integer.parseInt(scanner.nextLine());
                
                switch (actionChoice) {
                    case 1:
                        System.out.print("Enter target temperature (°C): ");
                        try {
                            double targetTemp = Double.parseDouble(scanner.nextLine());
                            thermostat.setTargetTemperature(targetTemp);
                            System.out.println("Target temperature set to " + targetTemp + "°C");
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input. Please enter a number.");
                        }
                        break;
                        
                    case 2:
                        System.out.println("Select mode:");
                        System.out.println("1. Heat");
                        System.out.println("2. Cool");
                        System.out.println("3. Auto");
                        System.out.println("4. Fan Only");
                        System.out.println("5. Off");
                        
                        System.out.print("Select an option: ");
                        
                        try {
                            int modeChoice = Integer.parseInt(scanner.nextLine());
                            
                            ThermostatMode mode;
                            switch (modeChoice) {
                                case 1:
                                    mode = ThermostatMode.HEAT;
                                    break;
                                    
                                case 2:
                                    mode = ThermostatMode.COOL;
                                    break;
                                    
                                case 3:
                                    mode = ThermostatMode.AUTO;
                                    break;
                                    
                                case 4:
                                    mode = ThermostatMode.FAN_ONLY;
                                    break;
                                    
                                case 5:
                                    mode = ThermostatMode.OFF;
                                    break;
                                    
                                default:
                                    System.out.println("Invalid option.");
                                    return;
                            }
                            
                            thermostat.setMode(mode);
                            System.out.println("Mode changed to " + mode);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input. Please enter a number.");
                        }
                        break;
                        
                    case 3:
                        sendGenericEvent(scanner, device);
                        break;
                        
                    default:
                        System.out.println("Invalid option.");
                        break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        } else {
            sendGenericEvent(scanner, device);
        }
        
        // Update environment based on device changes
        environment.updateFromDevice(device);
    }
    
    /**
     * Sends a generic event to a device.
     */
    private void sendGenericEvent(Scanner scanner, Device device) {
        // Select event type
        System.out.println("Select event type:");
        DeviceEvent[] events = DeviceEvent.values();
        
        for (int i = 0; i < events.length; i++) {
            System.out.printf("%d. %s%n", i + 1, events[i]);
        }
        
        System.out.print("Select an option: ");
        
        try {
            int eventChoice = Integer.parseInt(scanner.nextLine()) - 1;
            
            if (eventChoice < 0 || eventChoice >= events.length) {
                System.out.println("Invalid event number.");
                return;
            }
            
            DeviceEvent selectedEvent = events[eventChoice];
            boolean processed = device.processEvent(selectedEvent);
            
            if (processed) {
                System.out.println("Event processed: " + selectedEvent);
            } else {
                System.out.println("Event did not cause a state change.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
        }
    }
    
    /**
     * Advances the simulation time.
     */
    private void fastForwardTime(Scanner scanner) {
        System.out.println("\n==== Fast Forward Time ====");
        System.out.println("Current time: " + environment.getCurrentTime());
        
        System.out.print("Enter minutes to advance: ");
        
        try {
            int minutes = Integer.parseInt(scanner.nextLine());
            
            if (minutes <= 0) {
                System.out.println("Minutes must be positive.");
                return;
            }
            
            environment.advanceTime(minutes);
            System.out.println("Time advanced to " + environment.getCurrentTime());
            
            // Update environment based on time change
            if (currentScenario != null) {
                for (Device device : currentScenario.getDevices()) {
                    environment.updateFromDevice(device);
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
        }
    }
    
    /**
     * Creates sample scenarios for demonstration.
     */
    private void createSampleScenarios() {
        // Morning routine scenario
        Scenario morningScenario = new Scenario(
                "Morning Routine",
                "Simulates the smart home behavior during morning hours"
        );
        
        Light kitchenLight = new Light("L1", "Kitchen Light", true, true);
        Light bedroomLight = new Light("L2", "Bedroom Light", true, false);
        Thermostat thermostat = new Thermostat("T1", "Main Thermostat");
        
        morningScenario.addDevice(kitchenLight)
                      .addDevice(bedroomLight)
                      .addDevice(thermostat);
        
        // Schedule events for morning
        morningScenario.scheduleEvent(LocalTime.of(6, 0), "L2", DeviceEvent.POWER_ON)
                      .scheduleEvent(LocalTime.of(6, 30), "T1", DeviceEvent.POWER_ON)
                      .scheduleEvent(LocalTime.of(6, 31), "T1", DeviceEvent.ACTIVATE)
                      .scheduleEvent(LocalTime.of(7, 0), "L1", DeviceEvent.POWER_ON)
                      .scheduleEvent(LocalTime.of(8, 0), "L2", DeviceEvent.POWER_OFF)
                      .scheduleEvent(LocalTime.of(9, 0), "L1", DeviceEvent.POWER_OFF);
        
        scenarios.add(morningScenario);
        
        // Evening relaxation scenario
        Scenario eveningScenario = new Scenario(
                "Evening Relaxation",
                "Simulates the smart home behavior during evening hours"
        );
        
        Light livingRoomLight = new Light("L3", "Living Room Light", true, true);
        Thermostat eveningThermostat = new Thermostat("T2", "Living Room Thermostat");
        
        eveningScenario.addDevice(livingRoomLight)
                      .addDevice(eveningThermostat);
        
        // Schedule events for evening
        eveningScenario.scheduleEvent(LocalTime.of(18, 0), "L3", DeviceEvent.POWER_ON)
                      .scheduleEvent(LocalTime.of(18, 1), "T2", DeviceEvent.POWER_ON)
                      .scheduleEvent(LocalTime.of(18, 2), "T2", DeviceEvent.ACTIVATE)
                      .scheduleEvent(LocalTime.of(22, 0), "L3", DeviceEvent.ENTER_LOW_POWER)
                      .scheduleEvent(LocalTime.of(23, 0), "L3", DeviceEvent.POWER_OFF)
                      .scheduleEvent(LocalTime.of(23, 30), "T2", DeviceEvent.ENTER_LOW_POWER);
        
        scenarios.add(eveningScenario);
        
        // Energy saving scenario
        Scenario energySavingScenario = new Scenario(
                "Energy Saving Mode",
                "Demonstrates how devices behave in energy saving mode"
        );
        
        Light ecoLight1 = new Light("L4", "Eco Light 1", true, false);
        Light ecoLight2 = new Light("L5", "Eco Light 2", true, false);
        Thermostat ecoThermostat = new Thermostat("T3", "Eco Thermostat");
        
        energySavingScenario.addDevice(ecoLight1)
                           .addDevice(ecoLight2)
                           .addDevice(ecoThermostat);
        
        // Schedule events for energy saving
        energySavingScenario.scheduleEvent(LocalTime.of(8, 0), "L4", DeviceEvent.POWER_ON)
                           .scheduleEvent(LocalTime.of(8, 1), "L5", DeviceEvent.POWER_ON)
                           .scheduleEvent(LocalTime.of(8, 2), "T3", DeviceEvent.POWER_ON)
                           .scheduleEvent(LocalTime.of(8, 3), "T3", DeviceEvent.ACTIVATE)
                           .scheduleEvent(LocalTime.of(9, 0), "L4", DeviceEvent.ENTER_LOW_POWER)
                           .scheduleEvent(LocalTime.of(9, 1), "L5", DeviceEvent.ENTER_LOW_POWER)
                           .scheduleEvent(LocalTime.of(9, 2), "T3", DeviceEvent.ENTER_LOW_POWER)
                           .scheduleEvent(LocalTime.of(17, 0), "L4", DeviceEvent.EXIT_LOW_POWER)
                           .scheduleEvent(LocalTime.of(17, 1), "L5", DeviceEvent.EXIT_LOW_POWER)
                           .scheduleEvent(LocalTime.of(17, 2), "T3", DeviceEvent.EXIT_LOW_POWER)
                           .scheduleEvent(LocalTime.of(22, 0), "L4", DeviceEvent.POWER_OFF)
                           .scheduleEvent(LocalTime.of(22, 1), "L5", DeviceEvent.POWER_OFF)
                           .scheduleEvent(LocalTime.of(22, 2), "T3", DeviceEvent.POWER_OFF);
        
        scenarios.add(energySavingScenario);
    }
    
    /**
     * Sets the simulation speed.
     * 
     * @param speed Simulation speed multiplier
     */
    public void setSimulationSpeed(int speed) {
        if (speed <= 0) {
            throw new IllegalArgumentException("Simulation speed must be positive");
        }
        this.simulationSpeed = speed;
    }
}