package Bussiness;

public class Bid {
    String seller;
    int bid;
    String fileName;
    boolean won = false;

    public Bid(String seller, int bid, String fileName) {
        this.seller = seller;
        this.bid = bid;
        this.fileName = fileName;
    }
}
