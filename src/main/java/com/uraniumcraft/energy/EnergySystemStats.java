package com.uraniumcraft.energy;

/**
 * Статистика всей энергосистемы
 */
public class EnergySystemStats {
    
    private final int totalNetworks;
    private final int totalGenerators;
    private final int totalConsumers;
    private final int totalStorages;
    private final int totalCables;
    private final int totalGeneration;
    private final int totalDemand;
    private final int totalStored;
    private final int totalCapacity;
    
    public EnergySystemStats(int totalNetworks, int totalGenerators, int totalConsumers,
                           int totalStorages, int totalCables, int totalGeneration,
                           int totalDemand, int totalStored, int totalCapacity) {
        this.totalNetworks = totalNetworks;
        this.totalGenerators = totalGenerators;
        this.totalConsumers = totalConsumers;
        this.totalStorages = totalStorages;
        this.totalCables = totalCables;
        this.totalGeneration = totalGeneration;
        this.totalDemand = totalDemand;
        this.totalStored = totalStored;
        this.totalCapacity = totalCapacity;
    }
    
    /**
     * Получает общую эффективность системы
     */
    public double getOverallEfficiency() {
        if (totalDemand == 0) return 100.0;
        int totalAvailable = totalGeneration + totalStored;
        return Math.min(100.0, (double) totalAvailable / totalDemand * 100);
    }
    
    /**
     * Получает процент заполнения хранилищ
     */
    public double getStoragePercent() {
        if (totalCapacity == 0) return 0.0;
        return (double) totalStored / totalCapacity * 100;
    }
    
    /**
     * Получает статус системы
     */
    public String getSystemStatus() {
        double efficiency = getOverallEfficiency();
        
        if (efficiency >= 100) {
            return "Избыток энергии";
        } else if (efficiency >= 90) {
            return "Стабильная работа";
        } else if (efficiency >= 70) {
            return "Умеренная нагрузка";
        } else if (efficiency >= 50) {
            return "Высокая нагрузка";
        } else {
            return "Критическое состояние";
        }
    }
    
    // Геттеры
    public int getTotalNetworks() { return totalNetworks; }
    public int getTotalGenerators() { return totalGenerators; }
    public int getTotalConsumers() { return totalConsumers; }
    public int getTotalStorages() { return totalStorages; }
    public int getTotalCables() { return totalCables; }
    public int getTotalGeneration() { return totalGeneration; }
    public int getTotalDemand() { return totalDemand; }
    public int getTotalStored() { return totalStored; }
    public int getTotalCapacity() { return totalCapacity; }
}
