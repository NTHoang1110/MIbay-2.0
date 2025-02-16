package Application;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import Bussiness.Auction;
import Bussiness.Bid;
import Bussiness.Function;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class MainApp extends Application {
    public static final int CLIPORT = 12345;
    public static final String BROADCAST_ADDRESS = "255.255.255.255";
    public static final int BROADCAST_PORT = 6000;
    public static final Map<String, Auction> auctions = new ConcurrentHashMap<>();
    public static final Map<String, Bid> bids = new ConcurrentHashMap<>();
    private static int balance;

    @Override
    public void start(Stage primaryStage) {
        Function engine = new Function();
        Pane root = new Pane();
        Scene scene = new Scene(root, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static int getBalance(){
        return balance;
    }

    public static void setBalance(int newBalance){
        balance = newBalance;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

