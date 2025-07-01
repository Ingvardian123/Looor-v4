package com.uraniumcraft.energy;

import org.bukkit.Location;

import java.util.*;

public class EnergyNetwork {
    
    private final UUID id;
    private final Set<Location> generators;
    private final Set<Location> consumers;
    private final Set<Location> storages;
    private final Set<Location> cables;
    private int totalGeneration;
    private int totalDemand;
    private int totalStoredEnergy;
    private int totalCapacity;
    private double efficiency;
    private String status;
    private List<String> alerts;
    
    public EnergyNetwork() {
        this.id = UUID.randomUUID();
        this.generators = new HashSet<>();
        this.consumers = new HashSet<>();
        this.storages = new HashSet<>();
        this.cables = new HashSet<>();
        this.totalGeneration = 0;
        this.totalDemand = 0;
        this.totalStoredEnergy = 0;
        this.totalCapacity = 1000;
        this.efficiency = 1.0;
        this.status = "ONLINE";
        this.alerts = new ArrayList<>();
    }
    
    public UUID getId() { return id; }
    public int getSize() { return generators.size() + consumers.size() + storages.size() + cables.size(); }
    public Set<Location> getGenerators() { return generators; }
    public Set<Location> getConsumers() { return consumers; }
    public Set<Location> getStorages() { return storages; }
    public Set<Location> getCables() { return cables; }
    public int getTotalGeneration() { return totalGeneration; }
    public int getTotalDemand() { return totalDemand; }
    public int getTotalStoredEnergy() { return totalStoredEnergy; }
    public int getTotalCapacity() { return totalCapacity; }
    public double getNetworkEfficiency() { return efficiency; }
    public String getNetworkStatus() { return status; }
    public List<String> getAlerts() { return alerts; }
    
    public void setTotalGeneration(int totalGeneration) { this.totalGeneration = totalGeneration; }
    public void setTotalDemand(int totalDemand) { this.totalDemand = totalDemand; }
    public void setTotalStoredEnergy(int totalStoredEnergy) { this.totalStoredEnergy = totalStoredEnergy; }
    public void setTotalCapacity(int totalCapacity) { this.totalCapacity = totalCapacity; }
    public void setEfficiency(double efficiency) { this.efficiency = efficiency; }
    public void setStatus(String status) { this.status = status; }
    public void setAlerts(List<String> alerts) { this.alerts = alerts; }
}
