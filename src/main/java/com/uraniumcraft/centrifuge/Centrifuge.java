package com.uraniumcraft.centrifuge;

import com.uraniumcraft.effects.VisualEffects;
import com.uraniumcraft.items.UraniumItems;
import com.uraniumcraft.UraniumPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Centrifuge {
    private final UUID ownerId;
    private final Location location;
    private final UraniumPlugin plugin;
    private boolean isProcessing;
    private long processStartTime;
    private final long processDuration = 5 * 60 * 1000; // 5 минут
    private Map<Material, Integer> inputMaterials;
    private Map<Material, Integer> outputMaterials;
    private int processCount;
    private BukkitTask processingTask;
    private BukkitTask effectsTask;
    
    public Centrifuge(UUID ownerId, Location location, UraniumPlugin plugin) {
        this.ownerId = ownerId;
        this.location = location.clone();
        this.plugin = plugin;
        this.isProcessing = false;
        this.processStartTime = 0;
        this.inputMaterials = new HashMap<>();
        this.outputMaterials = new HashMap<>();
        this.processCount = 0;
        
        // Строим структуру центрифуги
        buildCentrifugeStructure();
    }
    
    private void buildCentrifugeStructure() {
        World world = location.getWorld();
        if (world == null) return;
        
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        // Основание центрифуги (3x3)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) {
                    // Центральный блок - диспенсер
                    world.getBlockAt(x + dx, y, z + dz).setType(Material.DISPENSER);
                } else {
                    // Основание из железных блоков
                    world.getBlockAt(x + dx, y, z + dz).setType(Material.IRON_BLOCK);
                }
            }
        }
        
        // Стенки центрифуги
        world.getBlockAt(x - 1, y + 1, z - 1).setType(Material.IRON_BLOCK);
        world.getBlockAt(x + 1, y + 1, z - 1).setType(Material.IRON_BLOCK);
        world.getBlockAt(x - 1, y + 1, z + 1).setType(Material.IRON_BLOCK);
        world.getBlockAt(x + 1, y + 1, z + 1).setType(Material.IRON_BLOCK);
        
        // Стеклянные стенки для наблюдения
        world.getBlockAt(x, y + 1, z - 1).setType(Material.GLASS);
        world.getBlockAt(x, y + 1, z + 1).setType(Material.GLASS);
        world.getBlockAt(x - 1, y + 1, z).setType(Material.GLASS);
        world.getBlockAt(x + 1, y + 1, z).setType(Material.GLASS);
        
        // Верхняя часть
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) {
                    // Центральный блок - воронка для загрузки
                    world.getBlockAt(x + dx, y + 2, z + dz).setType(Material.HOPPER);
                } else {
                    // Крыша из железных блоков
                    world.getBlockAt(x + dx, y + 2, z + dz).setType(Material.IRON_BLOCK);
                }
            }
        }
        
        // Энергетические блоки
        world.getBlockAt(x - 2, y + 1, z).setType(Material.REDSTONE_BLOCK);
        world.getBlockAt(x + 2, y + 1, z).setType(Material.REDSTONE_BLOCK);
        world.getBlockAt(x, y + 1, z - 2).setType(Material.REDSTONE_BLOCK);
        world.getBlockAt(x, y + 1, z + 2).setType(Material.REDSTONE_BLOCK);
        
        // Освещение
        world.getBlockAt(x, y + 3, z).setType(Material.GLOWSTONE);
        
        // Контрольные панели
        world.getBlockAt(x - 2, y, z - 1).setType(Material.CRAFTING_TABLE);
        world.getBlockAt(x - 2, y, z + 1).setType(Material.FURNACE);
        world.getBlockAt(x + 2, y, z - 1).setType(Material.ENCHANTING_TABLE);
        world.getBlockAt(x + 2, y, z + 1).setType(Material.ANVIL);
    }
    
    public boolean isValidStructure() {
        World world = location.getWorld();
        if (world == null) return false;
        
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        // Проверяем основные компоненты
        return world.getBlockAt(x, y, z).getType() == Material.DISPENSER &&
               world.getBlockAt(x, y + 2, z).getType() == Material.HOPPER &&
               world.getBlockAt(x - 1, y, z - 1).getType() == Material.IRON_BLOCK &&
               world.getBlockAt(x + 1, y, z + 1).getType() == Material.IRON_BLOCK;
    }
    
    public boolean startCentrifuge(Player player) {
        if (isProcessing) {
            player.sendMessage(ChatColor.RED + "Центрифуга уже работает!");
            return false;
        }
        
        if (!ownerId.equals(player.getUniqueId()) && !player.hasPermission("uraniumcraft.admin")) {
            player.sendMessage(ChatColor.RED + "Это не ваша центрифуга!");
            return false;
        }
        
        // Получаем материалы из инвентаря игрока
        Map<Material, Integer> materials = extractMaterialsFromPlayer(player);
        
        if (materials.isEmpty()) {
            player.sendMessage(ChatColor.RED + "У вас нет подходящих материалов для центрифугирования!");
            player.sendMessage(ChatColor.YELLOW + "Подходящие материалы: руды, древние обломки");
            return false;
        }
        
        return startCentrifugation(materials);
    }
    
    private Map<Material, Integer> extractMaterialsFromPlayer(Player player) {
        Map<Material, Integer> materials = new HashMap<>();
        
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && isCentrifugableMaterial(item.getType())) {
                materials.put(item.getType(), materials.getOrDefault(item.getType(), 0) + item.getAmount());
                player.getInventory().remove(item);
            }
        }
        
        return materials;
    }
    
    public boolean startCentrifugation(Map<Material, Integer> materials) {
        if (isProcessing) {
            return false;
        }
        
        // Проверяем, есть ли подходящие материалы для центрифугирования
        if (!hasValidMaterials(materials)) {
            return false;
        }
        
        // Начинаем процесс
        this.inputMaterials = new HashMap<>(materials);
        this.isProcessing = true;
        this.processStartTime = System.currentTimeMillis();
        this.processCount++;
        
        // Рассчитываем выходные материалы
        calculateOutputMaterials();
        
        // Запускаем визуальные эффекты
        startProcessingEffects();
        
        // Уведомляем игрока
        Player owner = Bukkit.getPlayer(ownerId);
        if (owner != null) {
            owner.sendMessage(ChatColor.GREEN + "🔄 Центрифугирование начато!");
            owner.sendMessage(ChatColor.YELLOW + "⏱️ Процесс займёт 5 минут...");
            owner.sendMessage(ChatColor.GRAY + "Материалы обрабатываются: " + formatMaterials(inputMaterials));
            owner.playSound(owner.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.2f);
        }
        
        // Запускаем таймер завершения
        processingTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (isProcessing && System.currentTimeMillis() - processStartTime >= processDuration) {
                    completeProcess();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
        
        return true;
    }
    
    private boolean hasValidMaterials(Map<Material, Integer> materials) {
        // Проверяем наличие материалов, которые можно центрифугировать
        for (Material material : materials.keySet()) {
            if (isCentrifugableMaterial(material)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isCentrifugableMaterial(Material material) {
        return material == Material.IRON_ORE || material == Material.DEEPSLATE_IRON_ORE ||
               material == Material.GOLD_ORE || material == Material.DEEPSLATE_GOLD_ORE ||
               material == Material.COPPER_ORE || material == Material.DEEPSLATE_COPPER_ORE ||
               material == Material.COAL_ORE || material == Material.DEEPSLATE_COAL_ORE ||
               material == Material.REDSTONE_ORE || material == Material.DEEPSLATE_REDSTONE_ORE ||
               material == Material.LAPIS_ORE || material == Material.DEEPSLATE_LAPIS_ORE ||
               material == Material.DIAMOND_ORE || material == Material.DEEPSLATE_DIAMOND_ORE ||
               material == Material.EMERALD_ORE || material == Material.DEEPSLATE_EMERALD_ORE ||
               material == Material.NETHER_QUARTZ_ORE || material == Material.NETHER_GOLD_ORE ||
               material == Material.ANCIENT_DEBRIS;
    }
    
    private void calculateOutputMaterials() {
        outputMaterials.clear();
        
        // Базовые продукты центрифугирования - УРАН!
        int totalOres = inputMaterials.values().stream().mapToInt(Integer::intValue).sum();
        outputMaterials.put(Material.GUNPOWDER, Math.max(1, totalOres / 4)); // Урановая пыль
        
        // Дополнительные продукты в зависимости от входных материалов
        for (Map.Entry<Material, Integer> entry : inputMaterials.entrySet()) {
            Material input = entry.getKey();
            int amount = entry.getValue();
            
            switch (input) {
                case IRON_ORE:
                case DEEPSLATE_IRON_ORE:
                    outputMaterials.put(Material.IRON_INGOT, amount * 2);
                    outputMaterials.put(Material.IRON_NUGGET, amount * 4);
                    break;
                    
                case GOLD_ORE:
                case DEEPSLATE_GOLD_ORE:
                case NETHER_GOLD_ORE:
                    outputMaterials.put(Material.GOLD_INGOT, amount * 2);
                    outputMaterials.put(Material.GOLD_NUGGET, amount * 6);
                    break;
                    
                case COPPER_ORE:
                case DEEPSLATE_COPPER_ORE:
                    outputMaterials.put(Material.COPPER_INGOT, amount * 3);
                    outputMaterials.put(Material.RAW_COPPER, amount * 2);
                    break;
                    
                case COAL_ORE:
                case DEEPSLATE_COAL_ORE:
                    outputMaterials.put(Material.COAL, amount * 3);
                    outputMaterials.put(Material.CHARCOAL, amount * 2);
                    break;
                    
                case REDSTONE_ORE:
                case DEEPSLATE_REDSTONE_ORE:
                    outputMaterials.put(Material.REDSTONE, amount * 6);
                    outputMaterials.put(Material.GLOWSTONE_DUST, amount);
                    break;
                    
                case LAPIS_ORE:
                case DEEPSLATE_LAPIS_ORE:
                    outputMaterials.put(Material.LAPIS_LAZULI, amount * 8);
                    outputMaterials.put(Material.BLUE_DYE, amount * 2);
                    break;
                    
                case DIAMOND_ORE:
                case DEEPSLATE_DIAMOND_ORE:
                    outputMaterials.put(Material.DIAMOND, amount * 2);
                    outputMaterials.put(Material.COAL, amount * 4);
                    break;
                    
                case EMERALD_ORE:
                case DEEPSLATE_EMERALD_ORE:
                    outputMaterials.put(Material.EMERALD, amount * 2);
                    outputMaterials.put(Material.GREEN_DYE, amount * 3);
                    // Больше урановой пыли от изумрудной руды (урановая руда)
                    outputMaterials.put(Material.GUNPOWDER, outputMaterials.getOrDefault(Material.GUNPOWDER, 0) + amount * 3);
                    break;
                    
                case NETHER_QUARTZ_ORE:
                    outputMaterials.put(Material.QUARTZ, amount * 4);
                    outputMaterials.put(Material.GLOWSTONE_DUST, amount * 2);
                    break;
                    
                case ANCIENT_DEBRIS:
                    outputMaterials.put(Material.NETHERITE_SCRAP, amount * 2);
                    outputMaterials.put(Material.GOLD_NUGGET, amount * 8);
                    // Больше урановой пыли от древних обломков
                    outputMaterials.put(Material.GUNPOWDER, outputMaterials.getOrDefault(Material.GUNPOWDER, 0) + amount * 4);
                    break;
            }
        }
        
        // Всегда добавляем урановые слитки и обеднённый уран
        if (totalOres >= 8) {
            outputMaterials.put(Material.PRISMARINE_SHARD, Math.max(1, totalOres / 8)); // Урановые слитки
        }
        if (totalOres >= 16) {
            outputMaterials.put(Material.CLAY_BALL, Math.max(1, totalOres / 16)); // Обеднённый уран
        }
        
        // Шанс на редкие материалы
        if (Math.random() < 0.1) { // 10% шанс
            outputMaterials.put(Material.PRISMARINE_CRYSTALS, 1); // Обогащённый уран
        }
        if (Math.random() < 0.05) { // 5% шанс
            outputMaterials.put(Material.END_CRYSTAL, 1); // Урановая капсула
        }
    }
    
    private void startProcessingEffects() {
        effectsTask = new BukkitRunnable() {
            int ticks = 0;
            
            @Override
            public void run() {
                if (!isProcessing) {
                    cancel();
                    return;
                }
                
                // Проверяем, не завершился ли процесс
                if (System.currentTimeMillis() - processStartTime >= processDuration) {
                    cancel();
                    return;
                }
                
                World world = location.getWorld();
                if (world == null) {
                    cancel();
                    return;
                }
                
                // Визуальные эффекты работы центрифуги - оптимизированные
                try {
                    // Вращающиеся частицы - уменьшено количество
                    if (ticks % 2 == 0) { // Каждый второй тик
                        double angle = ticks * 0.3;
                        for (int i = 0; i < 4; i++) { // Уменьшено с 8 до 4
                            double particleAngle = angle + i * (Math.PI / 2);
                            double x = Math.cos(particleAngle) * 1.5;
                            double z = Math.sin(particleAngle) * 1.5;
                            Location particleLoc = location.clone().add(x, 1 + Math.sin(ticks * 0.1) * 0.3, z);
                            
                            world.spawnParticle(Particle.REDSTONE, particleLoc, 1, 0, 0, 0, 0,
                                new Particle.DustOptions(org.bukkit.Color.YELLOW, 0.8f));
                        }
                    }
                    
                    // Центральный столб энергии - реже
                    if (ticks % 5 == 0) {
                        world.spawnParticle(Particle.END_ROD, 
                            location.clone().add(0, 1.5, 0), 1, 0.1, 0.1, 0.1, 0.02);
                    }
                    
                    // Электрические разряды - реже
                    if (ticks % 40 == 0) {
                        world.spawnParticle(Particle.ELECTRIC_SPARK, 
                            location.clone().add(0, 1, 0), 4, 1, 1, 1, 0.1);
                    }
                    
                    // Звуковые эффекты - реже
                    if (ticks % 80 == 0) {
                        world.playSound(location, Sound.BLOCK_BEACON_AMBIENT, 0.2f, 1.5f);
                    }
                    
                    // Интенсивные эффекты - реже
                    if (ticks % 120 == 0) {
                        // Кольцо частиц - уменьшено количество
                        for (int deg = 0; deg < 360; deg += 30) {
                            double radians = Math.toRadians(deg);
                            double x = Math.cos(radians) * 2;
                            double z = Math.sin(radians) * 2;
                            world.spawnParticle(Particle.ENCHANTMENT_TABLE,
                                location.clone().add(x, 1, z), 1, 0, 0, 0, 0.1);
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Ошибка в эффектах центрифуги: " + e.getMessage());
                }
                
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 4L); // Увеличен интервал с 2L до 4L
    }
    
    private void completeProcess() {
        isProcessing = false;
        
        // Останавливаем эффекты
        if (effectsTask != null) {
            effectsTask.cancel();
            effectsTask = null;
        }
        
        World world = location.getWorld();
        if (world != null) {
            // Эффекты завершения - оптимизированные
            world.spawnParticle(Particle.EXPLOSION_LARGE, location.clone().add(0, 1, 0), 1);
            world.spawnParticle(Particle.FIREWORKS_SPARK, location.clone().add(0, 1.5, 0), 15, 1, 1, 1, 0.2);
            world.spawnParticle(Particle.TOTEM, location.clone().add(0, 1, 0), 10, 0.8, 0.8, 0.8, 0.1);
            
            world.playSound(location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);
            world.playSound(location, Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.5f);
        }
        
        // Выдаём результаты игроку
        Player owner = Bukkit.getPlayer(ownerId);
        if (owner != null && owner.isOnline()) {
            owner.sendMessage(ChatColor.GREEN + "✅ Центрифугирование завершено!");
            owner.sendMessage(ChatColor.YELLOW + "🎁 Получены материалы:");
            
            // Оптимизированная выдача предметов
            for (Map.Entry<Material, Integer> entry : outputMaterials.entrySet()) {
                ItemStack result = createResultItem(entry.getKey(), entry.getValue());
                String displayName = getResultDisplayName(entry.getKey());
                
                // Добавляем предмет в инвентарь
                Map<Integer, ItemStack> leftover = owner.getInventory().addItem(result);
                if (!leftover.isEmpty()) {
                    // Если инвентарь полон, выбрасываем на землю
                    for (ItemStack item : leftover.values()) {
                        world.dropItemNaturally(owner.getLocation(), item);
                    }
                    owner.sendMessage(ChatColor.YELLOW + "⚠ Некоторые предметы выброшены на землю - инвентарь полон!");
                }
                
                owner.sendMessage(ChatColor.GRAY + "  • " + ChatColor.WHITE + displayName + 
                    ChatColor.GRAY + " x" + entry.getValue());
            }
            
            // Добавляем радиацию игроку
            plugin.getRadiationManager().addRadiation(owner, 15);
            owner.sendMessage(ChatColor.RED + "☢️ Получена радиация: +15");
            owner.sendMessage(ChatColor.YELLOW + "💡 Используйте защитные средства при работе с ураном!");
            
            // Статистика
            plugin.getPlayerStats(owner.getUniqueId()).incrementCentrifugeUses();
        }
        
        // Очищаем данные процесса
        inputMaterials.clear();
        outputMaterials.clear();
    }

    private ItemStack createResultItem(Material material, int amount) {
        ItemStack result;

        // Создаём специальные урановые предметы
        switch (material) {
            case GUNPOWDER:
                result = UraniumItems.createUraniumDust();
                result.setAmount(amount);
                break;
            case PRISMARINE_SHARD:
                result = UraniumItems.createUraniumIngot(amount);
                break;
            case END_CRYSTAL:
                result = UraniumItems.getItem("uranium_capsule");
                result.setAmount(amount);
                break;
            default:
                result = new ItemStack(material, amount);
                break;
        }

        return result;
    }

    private String getResultDisplayName(Material material) {
        switch (material) {
            case GUNPOWDER: return "Урановая пыль";
            case PRISMARINE_SHARD: return "Урановые слитки";
            case END_CRYSTAL: return "Урановая капсула";
            default: return getMaterialDisplayName(material);
        }
    }
    
    public void stopCentrifuge() {
        if (processingTask != null) {
            processingTask.cancel();
        }
        if (effectsTask != null) {
            effectsTask.cancel();
        }
        isProcessing = false;
    }
    
    public double getProgress() {
        if (!isProcessing) {
            return 0.0;
        }
        
        long elapsed = System.currentTimeMillis() - processStartTime;
        return Math.min(100.0, (double) elapsed / processDuration * 100.0);
    }
    
    public long getRemainingTime() {
        if (!isProcessing) {
            return 0;
        }
        
        long elapsed = System.currentTimeMillis() - processStartTime;
        return Math.max(0, processDuration - elapsed);
    }
    
    public String getRemainingTimeString() {
        long remaining = getRemainingTime();
        if (remaining <= 0) {
            return "Завершено";
        }
        
        long seconds = remaining / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        return String.format("%d:%02d", minutes, seconds);
    }
    
    private String formatMaterials(Map<Material, Integer> materials) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        
        for (Map.Entry<Material, Integer> entry : materials.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(getMaterialDisplayName(entry.getKey())).append(" x").append(entry.getValue());
            first = false;
        }
        
        return sb.toString();
    }
    
    private String getMaterialDisplayName(Material material) {
        switch (material) {
            case IRON_ORE: return "Железная руда";
            case DEEPSLATE_IRON_ORE: return "Глубинная железная руда";
            case GOLD_ORE: return "Золотая руда";
            case DEEPSLATE_GOLD_ORE: return "Глубинная золотая руда";
            case COPPER_ORE: return "Медная руда";
            case DEEPSLATE_COPPER_ORE: return "Глубинная медная руда";
            case COAL_ORE: return "Угольная руда";
            case DEEPSLATE_COAL_ORE: return "Глубинная угольная руда";
            case REDSTONE_ORE: return "Руда красного камня";
            case DEEPSLATE_REDSTONE_ORE: return "Глубинная руда красного камня";
            case LAPIS_ORE: return "Лазуритовая руда";
            case DEEPSLATE_LAPIS_ORE: return "Глубинная лазуритовая руда";
            case DIAMOND_ORE: return "Алмазная руда";
            case DEEPSLATE_DIAMOND_ORE: return "Глубинная алмазная руда";
            case EMERALD_ORE: return "Изумрудная руда";
            case DEEPSLATE_EMERALD_ORE: return "Глубинная изумрудная руда";
            case NETHER_QUARTZ_ORE: return "Кварцевая руда";
            case NETHER_GOLD_ORE: return "Адская золотая руда";
            case ANCIENT_DEBRIS: return "Древние обломки";
            case IRON_INGOT: return "Железные слитки";
            case GOLD_INGOT: return "Золотые слитки";
            case COPPER_INGOT: return "Медные слитки";
            case COAL: return "Уголь";
            case REDSTONE: return "Красная пыль";
            case LAPIS_LAZULI: return "Лазурит";
            case DIAMOND: return "Алмазы";
            case EMERALD: return "Изумруды";
            case QUARTZ: return "Кварц";
            default: return material.name().toLowerCase().replace("_", " ");
        }
    }
    
    // Геттеры
    public UUID getOwner() { return ownerId; }
    public Location getLocation() { return location; }
    public boolean isProcessing() { return isProcessing; }
    public int getProcessCount() { return processCount; }
    public Map<Material, Integer> getInputMaterials() { return new HashMap<>(inputMaterials); }
    public Map<Material, Integer> getOutputMaterials() { return new HashMap<>(outputMaterials); }
}
