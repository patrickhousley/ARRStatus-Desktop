package com.github.patrickhousley.arrstatus_desktop.model;

/**
 *
 * @author housleyp
 */
public enum ServerRegion {
    /**
     * Represents servers that do not belong to a region such as the Login server.
     */
    NONE("NONE", ""),
    /**
     * Represents all servers found in the Japan region.
     */
    JAPAN("JA", "Japan"),
    /**
     * Represents all servers found in the North America region.
     */
    NORTHAMERICA("NA", "North America"),
    /**
     * Represents all servers found in the Europe region.
     */
    EUROPE("EU", "Europe"),
    /**
     * Represents all legacy servers found in the Japan region.
     */
    JAPAN_LEGACY("JAL", "Japan Legacy"),
    /**
     * Represents all legacy servers found in the North America region.
     */
    NORTHAMERICA_LEGACY("NAL", "North America Legacy"),
    /**
     * Represents all legacy servers found in the Europe region.
     */
    EUROPE_LEGACY("EUL", "Europe Legacy");
    
    private String code;
    private String description;
    
    private ServerRegion(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * Get current regions code.
     * @return current regions code
     */
    public String getCode() {
        return this.code;
    }
    
    /**
     * Get the display description for the current server region.
     * @return current regions description
     */
    public String getDescription() {
        return this.description;
    }
    
    /**
     * Convert a region code to a ServiceRegion object.
     * @param code region code to convert
     * @return ServiceRegion object for given code or null
     */
    public static ServerRegion codeToRegion(String code) {
        for(ServerRegion sr : ServerRegion.values()) {
            if (sr.getCode().equals(code)) {
                return sr;
            }
        }
        
        return null;
    }
    
    @Override
    public String toString() {
        return getDescription();
    }
}
