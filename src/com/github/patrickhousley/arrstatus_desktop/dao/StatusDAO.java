package com.github.patrickhousley.arrstatus_desktop.dao;

import com.github.patrickhousley.arrstatus_desktop.model.ServerRegion;
import com.github.patrickhousley.arrstatus_desktop.model.StatusModel;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 * Data Access Object used to retrieve and parse status of ARR servers from the
 * ARRStatus.com website.
 * @author Patrick Housley
 * @version 24AUG2013
 */
public class StatusDAO {
    private static final String dataServerURL =
            "http://arrstatus.com/";
    private static final String dataAccessURL =
            "resources/status5.html";
    private static final String dataImageURL = 
            "resources/images/";
    private static ConcurrentMap<String, StatusModel> currentStatus = null;
    private static String lastError = null;
    private static final Lock updating = new ReentrantLock();
    private static final Condition updated = updating.newCondition();
    
    /**
     * Force the current status of all ARR servers to refresh.
     * @throws InterruptedException any time the main thread is interrupted
     * while waiting for a refresh to complete
     */
    public static void refresh() throws InterruptedException {
        updating.lock();
        try {
            (new Thread() {
                @Override
                public void run() {
                    try {
                        updating.lock();
                        
                        currentStatus = null;
                        URL connURL = new URL(dataServerURL + dataAccessURL);
                        URLConnection conn = connURL.openConnection();
                        StringBuilder result = new StringBuilder();
                        
                        try (BufferedReader output = new BufferedReader(
                                new InputStreamReader(conn.getInputStream()))) {
                            String line;
                            
                            while((line = output.readLine()) != null) {
                                result.append(line);
                            }
                        }
                        
                        if (result.length() == 0) {
                            lastError = "No response received from ARRStatus.com.";
                        } else {
                            processStatus(result.toString());
                        }
                    } catch (MalformedURLException ex) {
                        lastError = "Error retrieving data. Malformed URL Error.";
                    } catch (IOException ex) {
                        lastError = "Error retrieving data. Data IO Exception.";
                    } catch (ParserException ex) {
                        lastError = "Error parsing data. Malformed Data Exception";
                    } finally {
                        updated.signalAll();
                        updating.unlock();
                    }
                }
            }).start();
            
            updated.await();
        } finally {
            updating.unlock();
        }
    }
    
    /**
     * Retrieve the currently stored status of the ARR servers.
     * @return server statuses
     * @throws IllegalStateException when attempting to retrieve the status of
     * the servers after the retrieval of said status failed or was never
     * attempted.
     */
    public static HashMap<String, StatusModel> getStatus() {
        updating.lock();
        try {
            if (currentStatus == null) {
                throw new IllegalStateException("Status not currently available.");
            }
            
            return new HashMap<>(currentStatus);
        } finally {
            updating.unlock();
        }
    }
    
    /**
     * Retrieve the error, if any, that was encountered during the last refresh
     * attempt. If no error was encountered, null will be returned.
     * @return error during last refresh attempt
     */
    public static String lastError() {
        return lastError;
    }
    
    /**
     * Attempts to process the response from the ARRStatus.com website
     * containing the status of all servers.
     * @param result HTML from ARRStatus.com website data update address
     * @throws ParserException anytime the HTML parser is unable to parse the
     * HTML returned by the ARRStatus.com website data update address
     */
    private static void processStatus(String result) throws ParserException {
        ConcurrentMap<String, StatusModel> statuses = new ConcurrentHashMap<>();
        Parser parser = new Parser();
        String[] servers = result.split(";");
        for (int i = 0; i < servers.length; i++) {
            parser.setInputHTML(servers[i]);
            NodeList list = parser.extractAllNodesThatMatch(
                    new NodeClassFilter(TableColumn.class));
            
            StatusModel status = new StatusModel();
                
            TableColumn td = (TableColumn) list.elementAt(0);
            String name = td.getStringText();
            status.setName(name);

            td = (TableColumn) list.elementAt(1);
            status.setPing(td.getStringText());

            td = (TableColumn) list.elementAt(2);
            ImageTag state = (ImageTag) td.getChild(0);
            status.setStatusImage(dataServerURL + state.getImageURL());
            
            status.setServerImage(dataServerURL + dataImageURL + name + ".png");
            
            if (i < 3) {
                status.setRegion(ServerRegion.NONE);
            } else if (i < 23) {
                status.setRegion(ServerRegion.JAPAN);
            } else if (i < 38) {
                status.setRegion(ServerRegion.NORTHAMERICA);
            } else if (i < 43) {
                status.setRegion(ServerRegion.EUROPE);
            } else if (i < 48) {
                status.setRegion(ServerRegion.JAPAN_LEGACY);
            } else if (i < 52) {
                status.setRegion(ServerRegion.NORTHAMERICA_LEGACY);
            } else if (i == 52) {
                status.setRegion(ServerRegion.EUROPE_LEGACY);
            }

            statuses.put(name, status);
        }
        
        currentStatus = statuses;
    }
}
