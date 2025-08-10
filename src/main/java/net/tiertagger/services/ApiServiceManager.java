package net.tiertagger.services;

import net.tiertagger.TierTaggerPlugin;
import net.tiertagger.config.ConfigurationManager;
import net.tiertagger.models.PlayerTierData;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.util.Timeout;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ApiServiceManager {
    
    private final TierTaggerPlugin plugin;
    private final CloseableHttpAsyncClient httpClient;
    
    private static final String MCTIERS_BASE_URL = "https://api.uku3lig.net/tiers/profile/";
    private static final String SOUTH_TIERS_BASE_URL = "http://too-butler.gl.at.ply.gg:1247/api/profile/";
    
    public ApiServiceManager(TierTaggerPlugin plugin) {
        this.plugin = plugin;
        this.httpClient = HttpAsyncClients.createDefault();
        this.httpClient.start();
    }
    
    public CompletableFuture<PlayerTierData> fetchPlayerTierData(String playerIdentifier, boolean isUuid) {
        ConfigurationManager.ApiProvider provider = plugin.getConfigurationManager().getApiProvider();
        
        switch (provider) {
            case MCTIERS:
                return fetchFromMCTiers(playerIdentifier, isUuid);
            case SOUTH_TIERS:
                return fetchFromSouthTiers(playerIdentifier, isUuid);
            default:
                return CompletableFuture.completedFuture(null);
        }
    }
    
    private CompletableFuture<PlayerTierData> fetchFromMCTiers(String playerIdentifier, boolean isUuid) {
        CompletableFuture<PlayerTierData> future = new CompletableFuture<>();
        
        String uuid = isUuid ? playerIdentifier : getUuidFromUsername(playerIdentifier);
        if (uuid == null) {
            future.complete(null);
            return future;
        }
        
        String url = MCTIERS_BASE_URL + uuid.replace("-", "");
        
        if (plugin.getConfigurationManager().isLogApiRequests()) {
            plugin.getLogger().info("Fetching tier data from MCTiers: " + url);
        }
        
        SimpleHttpRequest request = SimpleRequestBuilder.get(url)
                .build();
        
        httpClient.execute(request, new FutureCallback<SimpleHttpResponse>() {
            @Override
            public void completed(SimpleHttpResponse response) {
                try {
                    if (response.getCode() == 200) {
                        String responseBody = response.getBodyText();
                        PlayerTierData tierData = PlayerTierData.fromMCTiersJson(responseBody);
                        future.complete(tierData);
                    } else {
                        if (plugin.getConfigurationManager().isDebugEnabled()) {
                            plugin.getLogger().warning("MCTiers returned status: " + response.getCode());
                        }
                        future.complete(null);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error parsing MCTiers response: " + e.getMessage());
                    future.complete(null);
                }
            }
            
            @Override
            public void failed(Exception ex) {
                plugin.getLogger().warning("Failed to fetch from MCTiers: " + ex.getMessage());
                future.complete(null);
            }
            
            @Override
            public void cancelled() {
                future.complete(null);
            }
        });
        
        return future;
    }
    
    private CompletableFuture<PlayerTierData> fetchFromSouthTiers(String playerIdentifier, boolean isUuid) {
        CompletableFuture<PlayerTierData> future = new CompletableFuture<>();
        
        String username = isUuid ? getUsernameFromUuid(playerIdentifier) : playerIdentifier;
        if (username == null) {
            future.complete(null);
            return future;
        }
        
        String url = SOUTH_TIERS_BASE_URL + username;
        
        if (plugin.getConfigurationManager().isLogApiRequests()) {
            plugin.getLogger().info("Fetching data from South Tiers: " + url);
        }
        
        SimpleHttpRequest request = SimpleRequestBuilder.get(url)
                .build();
        
        httpClient.execute(request, new FutureCallback<SimpleHttpResponse>() {
            @Override
            public void completed(SimpleHttpResponse response) {
                try {
                    if (response.getCode() == 200) {
                        String responseBody = response.getBodyText();
                        PlayerTierData tierData = PlayerTierData.fromSouthTiersJson(responseBody);
                        if (isUuid) {
                            tierData.setUuid(playerIdentifier);
                        }
                        future.complete(tierData);
                    } else {
                        if (plugin.getConfigurationManager().isDebugEnabled()) {
                            plugin.getLogger().warning("South Tiers returned status: " + response.getCode());
                        }
                        future.complete(null);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error parsing South Tiers response: " + e.getMessage());
                    future.complete(null);
                }
            }
            
            @Override
            public void failed(Exception ex) {
                plugin.getLogger().warning("Failed to fetch from South Tiers: " + ex.getMessage());
                future.complete(null);
            }
            
            @Override
            public void cancelled() {
                future.complete(null);
            }
        });
        
        return future;
    }
    
    private String getUuidFromUsername(String username) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        String url = "https://api.mojang.com/users/profiles/minecraft/" + username;
        SimpleHttpRequest request = SimpleRequestBuilder.get(url)
                .build();
        
        httpClient.execute(request, new FutureCallback<SimpleHttpResponse>() {
            @Override
            public void completed(SimpleHttpResponse response) {
                try {
                    if (response.getCode() == 200) {
                        String responseBody = response.getBodyText();
                        com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(responseBody).getAsJsonObject();
                        String uuid = json.get("id").getAsString();
                        String formattedUuid = uuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
                        future.complete(formattedUuid);
                    } else {
                        future.complete(null);
                    }
                } catch (Exception e) {
                    future.complete(null);
                }
            }
            
            @Override
            public void failed(Exception ex) {
                future.complete(null);
            }
            
            @Override
            public void cancelled() {
                future.complete(null);
            }
        });
        
        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            return null;
        }
    }
    
    private String getUsernameFromUuid(String uuid) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        String url = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.replace("-", "");
        SimpleHttpRequest request = SimpleRequestBuilder.get(url)
                .build();
        
        httpClient.execute(request, new FutureCallback<SimpleHttpResponse>() {
            @Override
            public void completed(SimpleHttpResponse response) {
                try {
                    if (response.getCode() == 200) {
                        String responseBody = response.getBodyText();
                        com.google.gson.JsonObject json = com.google.gson.JsonParser.parseString(responseBody).getAsJsonObject();
                        String username = json.get("name").getAsString();
                        future.complete(username);
                    } else {
                        future.complete(null);
                    }
                } catch (Exception e) {
                    future.complete(null);
                }
            }
            
            @Override
            public void failed(Exception ex) {
                future.complete(null);
            }
            
            @Override
            public void cancelled() {
                future.complete(null);
            }
        });
        
        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            return null;
        }
    }
    
    public void shutdown() {
        try {
            httpClient.close();
        } catch (Exception e) {
            plugin.getLogger().warning("Error closing HTTP client: " + e.getMessage());
        }
    }
}