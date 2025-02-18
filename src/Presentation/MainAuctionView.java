package Presentation;

import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class MainAuctionView extends GridPane {
    public Button anbieten;
    public Button bieten;
    public Button liste;
    public Button info;
    public Button abbrechen;

    public MainAuctionView() {
        anbieten = new Button("anbieten");
        TextField price = new TextField("Angebotpreise");
        TextField time = new TextField("Zeit");
        TextField file = new TextField("File up for auction");
        HBox Anbieten = new HBox(price, time, file, anbieten);

        bieten = new Button("bieten");
        TextField offer = new TextField("Preis");
        TextField seller = new TextField("Verkaufer");
        TextField sellingFile = new TextField("File");
        HBox Bieten = new HBox(offer, seller, sellingFile, bieten);

        liste = new Button("liste");

        info = new Button("info");

        abbrechen = new Button("abbrechen");
        TextField fileListed = new TextField("File you want to pull down from listing");
        HBox Abbrechen = new HBox(fileListed, abbrechen);

        VBox box = new VBox(Anbieten, Bieten, liste, info, Abbrechen);
        this.getChildren().add(box);
    }
}
