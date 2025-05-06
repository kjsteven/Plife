package com.example.plife.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Secure configuration helper class that loads sensitive credentials
 * from a config file that should not be checked into version control
 */
public class SecureConfig {
    private static final String TAG = "SecureConfig";
    private static final String CONFIG_FILE = "secure_config.json";
    
    private static Map<String, String> configMap = new HashMap<>();
    private static boolean isLoaded = false;

    /**
     * Load secure configuration from assets
     * @param context Application context
     * @return true if config loaded successfully
     */
    public static boolean loadConfig(Context context) {
        if (isLoaded) {
            return true;
        }
        
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(CONFIG_FILE);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            
            JSONObject jsonObject = new JSONObject(stringBuilder.toString());
            
            // Load SMTP settings
            JSONObject smtpConfig = jsonObject.optJSONObject("smtp");
            if (smtpConfig != null) {
                configMap.put("smtp.username", smtpConfig.optString("username", ""));
                configMap.put("smtp.password", smtpConfig.optString("password", ""));
                configMap.put("smtp.host", smtpConfig.optString("host", "smtp.gmail.com"));
                configMap.put("smtp.port", smtpConfig.optString("port", "587"));
            }
            
            isLoaded = true;
            return true;
            
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error loading secure configuration: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get a configuration value
     * @param key Configuration key
     * @return Configuration value or empty string if not found
     */
    public static String getConfig(String key) {
        return configMap.getOrDefault(key, "");
    }
    
    /**
     * Get SMTP username
     * @return SMTP username or empty string if not configured
     */
    public static String getSmtpUsername() {
        return getConfig("smtp.username");
    }
    
    /**
     * Get SMTP password
     * @return SMTP password or empty string if not configured
     */
    public static String getSmtpPassword() {
        return getConfig("smtp.password");
    }
    
    /**
     * Get SMTP host
     * @return SMTP host or default gmail host
     */
    public static String getSmtpHost() {
        return getConfig("smtp.host");
    }
    
    /**
     * Get SMTP port
     * @return SMTP port or default port
     */
    public static String getSmtpPort() {
        return getConfig("smtp.port");
    }
}