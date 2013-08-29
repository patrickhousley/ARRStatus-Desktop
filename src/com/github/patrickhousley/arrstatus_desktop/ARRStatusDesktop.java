package com.github.patrickhousley.arrstatus_desktop;

import com.github.patrickhousley.arrstatus_desktop.status.StatusController;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Desktop version of the ARRStatus.com website.
 * @author Patrick Housley
 * @version 24AUG2013
 */
public class ARRStatusDesktop extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        StatusController sc = new StatusController(stage);
        
        /*try {
            StatusDAO.refresh();
            HashMap<String, StatusModel> statuses = StatusDAO.getStatus();
            System.out.println("Got status");
            System.exit(0);
        } catch (InterruptedException ie) {
            System.out.println("Interrupted.");
        }*/
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}