package Presentation;

import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

public class MainAuctionView extends BorderPane {
    public Button anbieten;
    public Button bieten;
    public Button liste;
    public Button info;
    public Button abbrechen;

    public MainAuctionView() {
        anbieten = new Button("anbieten");
        bieten = new Button("bieten");
        liste = new Button("liste");
        info = new Button("info");
        abbrechen = new Button("abbrechen");

        this.getChildren().addAll(anbieten, bieten, liste, info, abbrechen);
    }
}
