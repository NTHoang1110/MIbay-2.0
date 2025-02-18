package Presentation;

import Bussiness.Function;
import javafx.scene.control.Button;

public class MainAuctionController {
    Button anbieten;
    Button bieten;
    Button liste;
    Button info;
    Button abbrechen;

    MainAuctionView mainView;

    public MainAuctionController(Function engine) {
        mainView = new MainAuctionView();

        this.anbieten = mainView.anbieten;
        this.bieten = mainView.bieten;
        this.info = mainView.info;
        this.liste = mainView.liste;
        this.abbrechen = mainView.abbrechen;

        init();
    }

    public void init() {
        anbieten.setOnAction(e -> System.out.println("anbieten gedruckt"));
        bieten.setOnAction(e -> System.out.println("bieten gedruckt"));
        liste.setOnAction(e -> System.out.println("liste gedruckt"));
        info.setOnAction(e -> System.out.println("info gedruckt"));
        abbrechen.setOnAction(e -> System.out.println("abbrechen gedruckt"));
    }

    public MainAuctionView getMainAuctionView() {
        return mainView;
    }

}
