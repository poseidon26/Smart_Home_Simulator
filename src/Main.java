import smarthome.simulation.SmartHomeSimulator;

public class Main {
    public static void main(String[] args) {
        System.out.println("Smart Home Automation Simulator v1.0");
        System.out.println("Based on Finite State Machine (FSM) Principles");
        
        SmartHomeSimulator simulator = new SmartHomeSimulator();
        simulator.startSimulation();
    }
}