package com.github.patrickhousley.arrstatus_desktop.model;

import java.util.Objects;

/**
 * Data Model used to store status of a server.
 * @author Patrick Housley
 * @version 24AUG2013
 */
public class StatusModel {
    private String name;
    private String ping;
    private String statusImage;
    private String serverImage;
    private ServerRegion region;

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the ping
     */
    public String getPing() {
        return ping;
    }

    /**
     * @param ping the ping to set
     */
    public void setPing(String ping) {
        this.ping = ping;
    }

    /**
     * @return the status image url
     */
    public String getStatusImage() {
        return statusImage;
    }

    /**
     * @param image url for status image
     */
    public void setStatusImage(String image) {
        this.statusImage = image;
    }
    
    /**
     * @return the server image url
     */
    public String getServerImage() {
        return serverImage;
    }

    /**
     * @param image url for server image
     */
    public void setServerImage(String image) {
        this.serverImage = image;
    }
    
    /**
     * @return the region
     */
    public ServerRegion getRegion() {
        return region;
    }

    /**
     * @param region the region to set
     */
    public void setRegion(ServerRegion region) {
        this.region = region;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.name);
        hash = 37 * hash + Objects.hashCode(this.ping);
        hash = 37 * hash + Objects.hashCode(this.statusImage);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StatusModel other = (StatusModel) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.ping, other.ping)) {
            return false;
        }
        if (!Objects.equals(this.statusImage, other.statusImage)) {
            return false;
        }
        return true;
    }
}
