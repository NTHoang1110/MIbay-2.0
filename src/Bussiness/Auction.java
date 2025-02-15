package Bussiness;

import java.time.LocalTime;

public class Auction {
    String fileName;
    int minPrice;
    LocalTime expiryTime;
    String seller;
    String filePath;
    String highestBidder;
    int highestBid;
    boolean ongoing = false;
    boolean canceled = false;

    public Auction(String fileName, int minPrice, LocalTime expiryTime, String seller) {
        this.fileName = fileName;
        this.minPrice = minPrice;
        this.expiryTime = expiryTime;
        this.seller = seller;
        this.highestBid = minPrice;
        this.highestBidder = null;
        this.ongoing = true;
    }
}
