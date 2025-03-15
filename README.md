# Smart Home Simulator

A Java-based Smart Home Simulator built on Finite State Machine (FSM) principles, allowing the simulation of various smart devices and home automation scenarios.

## Overview

This application simulates a smart home environment where various IoT devices (lights, thermostats, etc.) operate according to predefined states and transitions. The core of the simulation is based on a generic Finite State Machine implementation that manages device states and transitions based on events.

## Features

- **Finite State Machine Core**: A robust, reusable FSM implementation that can be used with any state and event types
- **Smart Device Simulations**:
  - **Lights**: Control on/off states, brightness levels, and colors for smart lighting
  - **Thermostats**: Simulate temperature control with various modes (heating, cooling, etc.)
- **Home Environment Modeling**: Simulates home conditions like temperature, humidity, and occupancy
- **Scenario-based Simulation**: Create and run predefined scenarios to test device behaviors
- **Interactive Console Interface**: Control the simulation through a user-friendly command-line interface
- **Time-based Simulation**: Simulate the passage of time and scheduled events

## Architecture

The project is organized into the following packages:

- **fsm**: Contains the generic Finite State Machine implementation
- **smarthome.model**: Core data models for smart home devices
- **smarthome.devices**: Implementations of various smart home devices
- **smarthome.environment**: Simulation of the home environment conditions
- **smarthome.simulation**: Management of the simulation, scenarios, and user interface

## Technical Details

- **Language**: Java
- **Design Patterns**: State Pattern, Observer Pattern
- **Architecture**: Object-Oriented, Event-Driven

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 8 or higher
- Any Java IDE (IntelliJ IDEA, Eclipse, etc.)

### Running the Application

1. Clone this repository
2. Open the project in your preferred Java IDE
3. Run the `Main.java` file
4. Follow the on-screen instructions to interact with the simulation

## Sample Usage

The simulator provides several predefined scenarios to demonstrate the capabilities:

1. **Morning Routine**: Simulates device activities during morning hours
2. **Evening Relaxation**: Adjusts lighting and temperature for evening comfort
3. **Energy Saving Mode**: Demonstrates how devices adapt to conserve energy

You can also create custom scenarios through the interactive interface.

## Contributing

Contributions are welcome! Feel free to submit issues or pull requests.

## License

This project is licensed under the MIT License - see the LICENSE file for details.