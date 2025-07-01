package com.uraniumcraft.achievements;

public class Achievement {
    private final String id;
    private final String name;
    private final String description;
    private final AchievementCategory category;
    private final int experienceReward;
    private final boolean secret;
    
    public Achievement(String id, String name, String description, AchievementCategory category, int experienceReward, boolean secret) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.experienceReward = experienceReward;
        this.secret = secret;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public AchievementCategory getCategory() {
        return category;
    }
    
    public int getExperienceReward() {
        return experienceReward;
    }
    
    public boolean isSecret() {
        return secret;
    }
}
