package net.tiertagger.models;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class PlayerTierData {
    
    private static final Gson gson = new Gson();
    
    private String uuid;
    private String username;
    private Map<String, GamemodeTier> gamemodes;
    private LocalDateTime cachedAt;
    private String region;
    private int points;
    private int overall;
    
    public PlayerTierData() {
        this.gamemodes = new HashMap<>();
    }
    
    public PlayerTierData(String uuid, String username) {
        this();
        this.uuid = uuid;
        this.username = username;
    }
    
    public String getBestTier() {
        if (gamemodes.isEmpty()) {
            return "UNRANKED";
        }
        
        GamemodeTier bestTier = null;
        int bestTierValue = Integer.MAX_VALUE;
        
        for (GamemodeTier tier : gamemodes.values()) {
            if (tier.getTier() > 0) {
                int tierValue = calculateTierValue(tier.getTier());
                if (tierValue < bestTierValue) {
                    bestTierValue = tierValue;
                    bestTier = tier;
                }
            }
        }
        
        return bestTier != null ? formatTier(bestTier.getTier()) : "UNRANKED";
    }
    
    public String getTierForGamemode(String gamemode) {
        GamemodeTier tier = gamemodes.get(gamemode.toLowerCase());
        if (tier != null && tier.getTier() > 0) {
            return formatTier(tier.getTier());
        }
        return "UNRANKED";
    }
    
    private int calculateTierValue(int tier) {
        return tier;
    }
    
    private String formatTier(int tier) {
        switch (tier) {
            case 1: return "HT1";
            case 2: return "LT1";
            case 3: return "HT2";
            case 4: return "LT2";
            case 5: return "HT3";
            case 6: return "LT3";
            case 7: return "HT4";
            case 8: return "LT4";
            case 9: return "HT5";
            case 10: return "LT5";
            default: return "UNRANKED";
        }
    }
    
    public static PlayerTierData fromMCTiersJson(String jsonString) {
        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
        
        PlayerTierData data = new PlayerTierData();
        data.uuid = json.get("uuid").getAsString();
        data.username = json.get("name").getAsString();
        data.region = json.has("region") ? json.get("region").getAsString() : "";
        data.points = json.has("points") ? json.get("points").getAsInt() : 0;
        data.overall = json.has("overall") ? json.get("overall").getAsInt() : 0;
        
        if (json.has("rankings")) {
            JsonObject rankings = json.getAsJsonObject("rankings");
            for (String gamemode : rankings.keySet()) {
                JsonObject gamemodeData = rankings.getAsJsonObject(gamemode);
                GamemodeTier tier = new GamemodeTier(
                    gamemodeData.get("tier").getAsInt(),
                    gamemodeData.get("pos").getAsInt(),
                    gamemodeData.get("peak_tier").getAsInt(),
                    gamemodeData.get("peak_pos").getAsInt(),
                    gamemodeData.get("attained").getAsLong(),
                    gamemodeData.get("retired").getAsBoolean()
                );
                data.gamemodes.put(gamemode, tier);
            }
        }
        
        return data;
    }
    
    public static PlayerTierData fromSouthTiersJson(String jsonString) {
        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
        
        PlayerTierData data = new PlayerTierData();
        
        if (json.get("success").getAsBoolean()) {
            JsonObject dataObj = json.getAsJsonObject("data");
            data.username = dataObj.get("jogador").getAsString();
            String ranking = dataObj.get("ranking").getAsString();
            
            int tierValue = parseSouthTierRanking(ranking);
            GamemodeTier tier = new GamemodeTier(tierValue, 0, tierValue, 0, System.currentTimeMillis() / 1000, false);
            data.gamemodes.put("vanilla", tier);
        }
        
        return data;
    }
    
    private static int parseSouthTierRanking(String ranking) {
        String lowerRanking = ranking.toLowerCase();
        if (lowerRanking.contains("high tier 1")) return 1;
        if (lowerRanking.contains("low tier 1")) return 2;
        if (lowerRanking.contains("high tier 2")) return 3;
        if (lowerRanking.contains("low tier 2")) return 4;
        if (lowerRanking.contains("high tier 3")) return 5;
        if (lowerRanking.contains("low tier 3")) return 6;
        if (lowerRanking.contains("high tier 4")) return 7;
        if (lowerRanking.contains("low tier 4")) return 8;
        if (lowerRanking.contains("high tier 5")) return 9;
        if (lowerRanking.contains("low tier 5")) return 10;
        return 0;
    }
    
    public String toJson() {
        return gson.toJson(this);
    }
    
    public static PlayerTierData fromJson(String json) {
        return gson.fromJson(json, PlayerTierData.class);
    }
    
    public String getUuid() {
        return uuid;
    }
    
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public Map<String, GamemodeTier> getGamemodes() {
        return gamemodes;
    }
    
    public void setGamemodes(Map<String, GamemodeTier> gamemodes) {
        this.gamemodes = gamemodes;
    }
    
    public LocalDateTime getCachedAt() {
        return cachedAt;
    }
    
    public void setCachedAt(LocalDateTime cachedAt) {
        this.cachedAt = cachedAt;
    }
    
    public String getRegion() {
        return region;
    }
    
    public void setRegion(String region) {
        this.region = region;
    }
    
    public int getPoints() {
        return points;
    }
    
    public void setPoints(int points) {
        this.points = points;
    }
    
    public int getOverall() {
        return overall;
    }
    
    public void setOverall(int overall) {
        this.overall = overall;
    }
    
    public static class GamemodeTier {
        private int tier;
        private int position;
        private int peakTier;
        private int peakPosition;
        private long attained;
        private boolean retired;
        
        public GamemodeTier(int tier, int position, int peakTier, int peakPosition, long attained, boolean retired) {
            this.tier = tier;
            this.position = position;
            this.peakTier = peakTier;
            this.peakPosition = peakPosition;
            this.attained = attained;
            this.retired = retired;
        }
        
        public int getTier() {
            return tier;
        }
        
        public void setTier(int tier) {
            this.tier = tier;
        }
        
        public int getPosition() {
            return position;
        }
        
        public void setPosition(int position) {
            this.position = position;
        }
        
        public int getPeakTier() {
            return peakTier;
        }
        
        public void setPeakTier(int peakTier) {
            this.peakTier = peakTier;
        }
        
        public int getPeakPosition() {
            return peakPosition;
        }
        
        public void setPeakPosition(int peakPosition) {
            this.peakPosition = peakPosition;
        }
        
        public long getAttained() {
            return attained;
        }
        
        public void setAttained(long attained) {
            this.attained = attained;
        }
        
        public boolean isRetired() {
            return retired;
        }
        
        public void setRetired(boolean retired) {
            this.retired = retired;
        }
    }
}