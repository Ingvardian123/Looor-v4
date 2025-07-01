package com.uraniumcraft.energy;

import java.util.List;
import java.util.UUID;

/**
 * Информация об энергосети для отображения
 */
public class EnergyNetworkInfo {
    
    private final String networkId;
    private final int totalComponents;
    private final int generators;
    private final int consumers;
    private final int storages;
    private final int cables;
    private final int totalGeneration;
    private final int totalDemand;
    private final int totalStored;
    private final int totalCapacity;
    private final double efficiency;
    private final String status;
    private final List<String> alerts;
    
    public EnergyNetworkInfo(EnergyNetwork network) {
        this.networkId = network.getId().toString().substring(0, 8);
        this.totalComponents = network.getSize();
        this.generators = network.getGenerators().size();
        this.consumers = network.getConsumers().size();
        this.storages = network.getStorages().size();
        this.cables = network.getCables().size();
        this.totalGeneration = network.getTotalGeneration();
        this.totalDemand = network.getTotalDemand();
        this.totalStored = network.getTotalStoredEnergy();
        this.totalCapacity = network.getTotalCapacity();
        this.efficiency = network.getNetworkEfficiency();
        this.status = network.getNetworkStatus();
        this.alerts = network.getAlerts();
    }
    
    // Геттеры
    public String getNetworkId() { return networkId; }
    public int getTotalComponents() { return totalComponents; }
    public int getGenerators() { return generators; }
    public int getConsumers() { return consumers; }
    public int getStorages() { return storages; }
    public int getCables() { return cables; }
    public int getTotalGeneration() { return totalGeneration; }
    public int getTotalDemand() { return totalDemand; }
    public int getTotalStored() { return totalStored; }
    public int getTotalCapacity() { return totalCapacity; }
    public double getEfficiency() { return efficiency; }
    public String getStatus() { return status; }
    public List<String> getAlerts() { return alerts; }
}
