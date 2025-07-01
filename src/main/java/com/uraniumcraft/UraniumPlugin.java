package com.uraniumcraft;

import com.uraniumcraft.achievements.AchievementManager;
import com.uraniumcraft.centrifuge.CentrifugeManager;
import com.uraniumcraft.commands.*;
import com.uraniumcraft.effects.AnimatedVisualEffects;
import com.uraniumcraft.energy.ArmorCharger;
import com.uraniumcraft.items.AdvancedItems;
import com.uraniumcraft.items.AdvancedResearchItems;
import com.uraniumcraft.items.GuideBook;
import com.uraniumcraft.items.UraniumItems;
import com.uraniumcraft.laboratory.EnhancedLaboratoryManager;
import com.uraniumcraft.laboratory.LaboratoryManager;
import com.uraniumcraft.laboratory.ResearchTerminalManager;
import com.uraniumcraft.listeners.*;
import com.uraniumcraft.radiation.RadiationManager;
import com.uraniumcraft.recipes.AdvancedRecipes;
import com.uraniumcraft.recipes.TabletRecipes;
import com.uraniumcraft.recipes.UraniumRecipes;
import com.uraniumcraft.stats.PlayerStats;
import com.uraniumcraft.teleporter.TeleportTerminalManager;
import com.uraniumcraft.teleporter.TeleporterManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class UraniumPlugin extends JavaPlugin {
    
    private static UraniumPlugin instance;
    
    // Managers
    private RadiationManager radiationManager;
    private LaboratoryManager laboratoryManager;
    private EnhancedLaboratoryManager enhancedLaboratoryManager;
    private CentrifugeManager centrifugeManager;
    private TeleporterManager teleporterManager;
    private TeleportTerminalManager teleportTerminalManager;
    private ResearchTerminalManager researchTerminalManager;
    private AchievementManager achievementManager;
    private PlayerStats playerStats;
    private ArmorCharger armorCharger;
    
    @Override
    public void onEnable() {
        instance = this;
        
        getLogger().info("UraniumCraft загружается...");
        
        // Сохранение конфигурации по умолчанию
        saveDefaultConfig();
        
        // Инициализация менеджеров
        initializeManagers();
        
        // Регистрация предметов
        registerItems();
        
        // Регистрация рецептов
        registerRecipes();
        
        // Регистрация команд
        registerCommands();
        
        // Регистрация слушателей событий
        registerListeners();
        
        // Запуск периодических задач
        startPeriodicTasks();
        
        // Инициализация анимированных эффектов
        AnimatedVisualEffects.initialize(this);
        
        getLogger().info("UraniumCraft успешно загружен!");
        getLogger().info("Версия: " + getDescription().getVersion());
        getLogger().info("Автор: " + getDescription().getAuthors());
        
        // Приветственное сообщение с анимацией
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.broadcastMessage("§a§l[UraniumCraft] §r§eПлагин успешно загружен!");
                Bukkit.broadcastMessage("§a§l[UraniumCraft] §r§bДобро пожаловать в мир атомных технологий!");
            }
        }.runTaskLater(this, 20L);
    }
    
    @Override
    public void onDisable() {
        getLogger().info("UraniumCraft выгружается...");
        
        // Остановка всех анимированных эффектов
        AnimatedVisualEffects.cleanup();
        
        // Сохранение данных
        saveAllData();
        
        getLogger().info("UraniumCraft успешно выгружен!");
        instance = null;
    }
    
    private void initializeManagers() {
        getLogger().info("Инициализация менеджеров...");
        
        radiationManager = new RadiationManager(this);
        laboratoryManager = new LaboratoryManager();
        enhancedLaboratoryManager = new EnhancedLaboratoryManager(this);
        centrifugeManager = new CentrifugeManager();
        teleporterManager = new TeleporterManager();
        teleportTerminalManager = new TeleportTerminalManager(this);
        researchTerminalManager = new ResearchTerminalManager(this, enhancedLaboratoryManager);
        achievementManager = new AchievementManager();
        playerStats = new PlayerStats(this);
        armorCharger = new ArmorCharger(this);
        
        getLogger().info("Менеджеры инициализированы!");
    }
    
    private void registerItems() {
        getLogger().info("Регистрация предметов...");
        
        UraniumItems.registerItems();
        AdvancedItems.registerItems();
        AdvancedResearchItems.registerItems();
        GuideBook.registerItems();
        
        getLogger().info("Предметы зарегистрированы!");
    }
    
    private void registerRecipes() {
        getLogger().info("Регистрация рецептов...");
        
        UraniumRecipes.registerRecipes();
        AdvancedRecipes.registerRecipes();
        TabletRecipes.registerRecipes();
        
        getLogger().info("Рецепты зарегистрированы!");
    }
    
    private void registerCommands() {
        getLogger().info("Регистрация команд...");
        
        getCommand("uranium").setExecutor(new UraniumMainCommand(this));
        getCommand("giveuranium").setExecutor(new GiveUraniumCommand());
        getCommand("uraniumguide").setExecutor(new UraniumGuideCommand(this));
        getCommand("enhancedlab").setExecutor(new EnhancedLaboratoryCommand(this, enhancedLaboratoryManager));
        getCommand("teleporter").setExecutor(new TeleporterCommand(this));
        getCommand("terminal").setExecutor(new TerminalCommand(this));
        getCommand("tablet").setExecutor(new TabletCommand(this));
        getCommand("laboratory").setExecutor(new LaboratoryCommand(this));
        getCommand("uraniumtp").setExecutor(new TpCommand(this));
        getCommand("uraniumadmin").setExecutor(new AdminCommand(this));
        
        getLogger().info("Команды зарегистрированы!");
    }
    
    private void registerListeners() {
        getLogger().info("Регистрация слушателей событий...");
        
        // Основные слушатели
        getServer().getPluginManager().registerEvents(new UraniumListeners(this, laboratoryManager, radiationManager, achievementManager), this);
        getServer().getPluginManager().registerEvents(new UniversalListener(this), this);
        getServer().getPluginManager().registerEvents(new UraniumListener(this), this);
        getServer().getPluginManager().registerEvents(new RadiationListener(this), this);
        getServer().getPluginManager().registerEvents(new PowerArmorListener(this), this);
        
        // GUI слушатели
        getServer().getPluginManager().registerEvents(new GUIListener(this, laboratoryManager), this);
        getServer().getPluginManager().registerEvents(new MainGUIListener(this), this);
        getServer().getPluginManager().registerEvents(new CentrifugeGUIListener(this, centrifugeManager), this);
        getServer().getPluginManager().registerEvents(new ResearchTerminalListener(this, enhancedLaboratoryManager), this);
        getServer().getPluginManager().registerEvents(new EnhancedLaboratoryListener(this, enhancedLaboratoryManager), this);
        getServer().getPluginManager().registerEvents(new TeleporterListener(this), this);
        getServer().getPluginManager().registerEvents(new TerminalListener(this), this);
        getServer().getPluginManager().registerEvents(new TabletListener(this), this);
        getServer().getPluginManager().registerEvents(new LaboratoryListener(this, laboratoryManager), this);
        
        // Специализированные слушатели
        getServer().getPluginManager().registerEvents(new AdvancedResearchItemsListener(this), this);
        getServer().getPluginManager().registerEvents(new AdvancedItemsListener(this), this);
        getServer().getPluginManager().registerEvents(new AchievementListener(this, achievementManager), this);
        getServer().getPluginManager().registerEvents(new StatsTrackingListener(this), this);
        getServer().getPluginManager().registerEvents(new NaturalGameplayListener(this, achievementManager), this);
        
        // Анимированные слушатели
        getServer().getPluginManager().registerEvents(new AnimatedItemsListener(this), this);
        
        getLogger().info("Слушатели событий зарегистрированы!");
    }
    
    private void startPeriodicTasks() {
        getLogger().info("Запуск периодических задач...");
        
        // Задача обновления радиации (каждые 5 секунд)
        new BukkitRunnable() {
            @Override
            public void run() {
                radiationManager.updateRadiation();
            }
        }.runTaskTimer(this, 0L, 100L);
        
        // Задача работы центрифуг (каждые 10 секунд)
        new BukkitRunnable() {
            @Override
            public void run() {
                // centrifugeManager.processCentrifuges();
            }
        }.runTaskTimer(this, 0L, 200L);
        
        // Задача работы лабораторий (каждые 30 секунд)
        new BukkitRunnable() {
            @Override
            public void run() {
                // laboratoryManager.processLaboratories();
                // enhancedLaboratoryManager.processLaboratories();
            }
        }.runTaskTimer(this, 0L, 600L);
        
        // Задача зарядки брони (каждую минуту)
        new BukkitRunnable() {
            @Override
            public void run() {
                armorCharger.chargeAllArmor();
            }
        }.runTaskTimer(this, 0L, 1200L);
        
        // Задача сохранения данных (каждые 5 минут)
        new BukkitRunnable() {
            @Override
            public void run() {
                saveAllData();
            }
        }.runTaskTimer(this, 0L, 6000L);
        
        // Задача обновления статистики (каждые 2 минуты)
        new BukkitRunnable() {
            @Override
            public void run() {
                playerStats.updateAllStats();
            }
        }.runTaskTimer(this, 0L, 2400L);
        
        // Задача анимированных эффектов окружения (каждые 3 секунды)
        new BukkitRunnable() {
            @Override
            public void run() {
                updateEnvironmentalEffects();
            }
        }.runTaskTimer(this, 0L, 60L);
        
        getLogger().info("Периодические задачи запущены!");
    }
    
    private void saveAllData() {
        try {
            radiationManager.saveData();
            playerStats.saveData();
            getLogger().info("Автосохранение данных выполнено успешно!");
        } catch (Exception e) {
            getLogger().severe("Ошибка при автосохранении данных: " + e.getMessage());
        }
    }
    
    private void updateEnvironmentalEffects() {
        // Обновление анимированных эффектов в мире
        Bukkit.getOnlinePlayers().forEach(player -> {
            // Проверяем радиацию и показываем эффекты
            int radiation = radiationManager.getRadiation(player.getUniqueId());
            if (radiation > 50) {
                // AnimatedVisualEffects.playRadiationEffect(player.getLocation());
            }
        });
    }
    
    // Геттеры для менеджеров
    public static UraniumPlugin getInstance() {
        return instance;
    }
    
    public RadiationManager getRadiationManager() {
        return radiationManager;
    }
    
    public LaboratoryManager getLaboratoryManager() {
        return laboratoryManager;
    }
    
    public EnhancedLaboratoryManager getEnhancedLaboratoryManager() {
        return enhancedLaboratoryManager;
    }
    
    public CentrifugeManager getCentrifugeManager() {
        return centrifugeManager;
    }
    
    public TeleporterManager getTeleporterManager() {
        return teleporterManager;
    }
    
    public TeleportTerminalManager getTeleportTerminalManager() {
        return teleportTerminalManager;
    }
    
    public ResearchTerminalManager getResearchTerminalManager() {
        return researchTerminalManager;
    }
    
    public AchievementManager getAchievementManager() {
        return achievementManager;
    }
    
    public PlayerStats getPlayerStats() {
        return playerStats;
    }
    
    public ArmorCharger getArmorCharger() {
        return armorCharger;
    }
}
