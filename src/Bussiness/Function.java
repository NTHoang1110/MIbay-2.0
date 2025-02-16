package Bussiness;

import java.io.*;
import java.net.*;
import java.time.LocalTime;
import Application.MainApp;

public class Function{
    static Thread requestListener = new Thread(() -> {
        try (DatagramSocket requestSocket = new DatagramSocket(MainApp.BROADCAST_PORT)) {
            String fileNameWon = null;
            int priceWon = 0;
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                requestSocket.receive(packet);
    
                String request = new String(packet.getData(), 0, packet.getLength());
                String[] requestParts = request.split(":");
    
                switch (requestParts[0]) {
                    case "nachricht":
                        System.out.println(requestParts[1] + "!!!!");
                        break;
    
                    case "CHECK_NAME":
                        if (requestParts[1].equals(System.getenv("USER"))) {
                            String response = InetAddress.getLocalHost().getHostAddress();
                            DatagramPacket responsePacket = new DatagramPacket(response.getBytes(),
                                    response.length(),
                                    packet.getAddress(), packet.getPort());
                            requestSocket.send(responsePacket);
                        }
                        break;
    
                    case "MainApp.auctions":
                        StringBuilder auctionsList = new StringBuilder();
                        for (Auction auction : MainApp.auctions.values()) {
                            if (auction.ongoing && !auction.canceled) {
                                auctionsList
                                        .append("Höchstgebot: " + auction.highestBid + " | Bieter: "
                                                + auction.highestBidder + " | Status: "
                                                + (auction.ongoing ? "laufend" : "beendet")
                                                + " | Anbieter: " + auction.seller + " | Datei: " + auction.fileName
                                                + "\n");
                            }
                        }
                        String response = auctionsList.toString();
                        if (response.length() != 0) {
                            DatagramPacket responsePacket = new DatagramPacket(response.getBytes(),
                                    response.length(),
                                    packet.getAddress(), packet.getPort());
                            requestSocket.send(responsePacket);
                        }
                        break;
    
                    case "abbrechen":
                        if (MainApp.auctions.containsKey(requestParts[1])) {
                            MainApp.auctions.remove(requestParts[1]);
                        }
                        if (MainApp.bids.containsKey(requestParts[1])) {
                            MainApp.bids.remove(requestParts[1]);
                        }
                        break;
    
                    case "bieten":
                        String responseMessage = "nachricht:Gebot erfolgreich.";
                        String[] bidParts = requestParts[1].split(";");
                        if (MainApp.auctions.containsKey(bidParts[2])) {
                            if (MainApp.auctions.get(bidParts[2]).ongoing
                                    && (Integer.parseInt(bidParts[1]) > MainApp.auctions.get(bidParts[2]).highestBid)) {
                                MainApp.auctions.get(bidParts[2]).highestBid = Integer.parseInt(bidParts[1]);
                                MainApp.auctions.get(bidParts[2]).highestBidder = bidParts[0];
                            } else {
                                responseMessage = "nachricht:Das Gebot ist zu niedrig oder die Auktion ist beendet.";
                            }
                        } else {
                            responseMessage = "nachricht:Auktion nicht gefunden.";
                        }
                        InetAddress broadcastAddress = InetAddress.getByName(MainApp.BROADCAST_ADDRESS);
                        DatagramPacket repPacket = new DatagramPacket(responseMessage.getBytes(),
                                responseMessage.length(), broadcastAddress,
                                MainApp.BROADCAST_PORT);
                        requestSocket.send(repPacket);
                        break;
    
                    case "gewonnen":
                        fileNameWon = requestParts[1];
                        if (MainApp.bids.containsKey(fileNameWon)) {
                            MainApp.bids.get(fileNameWon).won = true;
                            priceWon = MainApp.bids.get(fileNameWon).bid;
                        }
                        break;
    
                    case "Geld":
                        MainApp.setBalance(MainApp.getBalance() + Integer.parseInt(requestParts[1]));;
                        System.out.println("Geld erhalten: " + requestParts[1]);
                        break;
    
                    case "ended":
                        if (MainApp.bids.containsKey(requestParts[1]) && !MainApp.bids.get(requestParts[1]).won) {
                            MainApp.bids.remove(requestParts[1]);
                        }
                        break;
                    case "File":
                        try (BufferedWriter bw = new BufferedWriter(
                                new FileWriter("dateien/" + fileNameWon, true))) {
                            if(requestParts.length == 1){
                                bw.write("\n");
                            }else{
                                if (requestParts[1].equals("EOF")) {
                                    System.out.println("Datei empfangen und gespeichert in dateien/" + fileNameWon);
                                    bw.close();
                                    sendMoney(requestSocket, fileNameWon, priceWon);
                                    MainApp.bids.remove(fileNameWon);
                                    break;
                                } else{
                                    bw.write(requestParts[1]);
                                    bw.newLine();
                                }
                            }
                        }
                        break;
                    case "neuGebot":
                        String[] otherBid = requestParts[1].split(";");
                        if(!System.getenv("USER").equals(otherBid[0])){
                            MainApp.bids.remove(otherBid[1]);
                        }
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    });
    
    public static void anbieten(int startPrice, int time, String filename) {
        File file = new File("dateien/" + filename);
        if(!file.exists()){
            System.out.println("Datei nicht existiert! Versuch noch mal");
            return;
        }
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(time * 1000);
            socket.setBroadcast(true);
            InetAddress broadcastAddress = InetAddress.getByName(MainApp.BROADCAST_ADDRESS);
    
            String anbieten = "nachricht:" + System.getenv("USER") + " hat " + filename + " in " + time
                    + " Sekunden mit dem Startpreis " + startPrice + " angeboten.";
    
            DatagramPacket packet = new DatagramPacket(anbieten.getBytes(), anbieten.length(), broadcastAddress,
                    MainApp.BROADCAST_PORT);
            socket.send(packet);
    
        } catch (SocketTimeoutException e) {
            System.out.println("Nachricht nicht gesendet oder empfangen: Timeout");
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        Auction auction = new Auction(filename, startPrice, LocalTime.now().plusSeconds(time),
                System.getenv("USER"));
        MainApp.auctions.put(filename, auction);
    
        new Thread(() -> {
            String message;
            String winner = null;
            while (!auction.canceled) {
                if (auction.expiryTime.isBefore(LocalTime.now())) {
                    auction.ongoing = false;
                    if (auction.highestBidder != null) {
                        message = "nachricht:Auktion für " + auction.fileName + " ist beendet. Gewinner ist "
                                + auction.highestBidder + " mit " + auction.highestBid + " ";
                        winner = auction.highestBidder;
                    } else {
                        message = "nachricht:Auktion für " + auction.fileName + " ist beendet. Kein Gewinner.";
                    }
                    try (DatagramSocket socket = new DatagramSocket()) {
                        socket.setSoTimeout(time * 1000);
                        socket.setBroadcast(true);
                        InetAddress broadcastAddress = InetAddress.getByName(MainApp.BROADCAST_ADDRESS);
    
                        DatagramPacket packet = new DatagramPacket(message.getBytes(), message.length(),
                                broadcastAddress,
                                MainApp.BROADCAST_PORT);
                        socket.send(packet);
                    } catch (SocketTimeoutException e) {
                        System.out.println("Nachricht nicht gesendet oder empfangen: Timeout");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (winner != null) {
                        try (DatagramSocket socket = new DatagramSocket()) {
                            socket.setSoTimeout(10000);
                            InetAddress userAddress = InetAddress.getByName(findUser(winner));
                            String bid = "gewonnen:" + auction.fileName;
                            DatagramPacket packet = new DatagramPacket(bid.getBytes(), bid.length(), userAddress,
                                    MainApp.BROADCAST_PORT);
                            socket.send(packet);
    
                            InetAddress broadcastAddress = InetAddress.getByName(MainApp.BROADCAST_ADDRESS);
                            String ended = "ended:" + auction.fileName;
                            DatagramPacket endedPacket = new DatagramPacket(ended.getBytes(), ended.length(),
                                    broadcastAddress,
                                    MainApp.BROADCAST_PORT);
                            socket.send(endedPacket);
                        } catch (SocketTimeoutException e) {
                            System.out.println("Nachricht nicht gesendet oder empfangen: Timeout");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        sendFileToWinner(auction.fileName, winner);
                    }
                    break;
                }
            }
        }).start();
    }
    
    public static void abbrechen(String filename) {
        if (MainApp.auctions.containsKey(filename)) {
            MainApp.auctions.get(filename).canceled = true;
            MainApp.auctions.get(filename).ongoing = false;
            MainApp.auctions.remove(filename);
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setBroadcast(true);
                InetAddress broadcastAddress = InetAddress.getByName(MainApp.BROADCAST_ADDRESS);
    
                String bid = "nachricht:" + System.getenv("USER") + " hat " + filename + " abgebrochen. Kein Gewinner.";
                DatagramPacket packet = new DatagramPacket(bid.getBytes(), bid.length(), broadcastAddress,
                        MainApp.BROADCAST_PORT);
                socket.send(packet);
    
                String cancelBid = "abbrechen:" + filename;
                DatagramPacket cancelPacket = new DatagramPacket(cancelBid.getBytes(), cancelBid.length(),
                        broadcastAddress,
                        MainApp.BROADCAST_PORT);
                socket.send(cancelPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Sie haben keine Auktion mit der Datei: " + filename);
        }
        if (MainApp.bids.containsKey(filename)) {
            MainApp.bids.remove(filename);
        }
    }
    
    public static void liste() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(10000);
            socket.setBroadcast(true);
            InetAddress broadcastAddress = InetAddress.getByName(MainApp.BROADCAST_ADDRESS);
    
            String bid = "MainApp.auctions:";
            DatagramPacket packet = new DatagramPacket(bid.getBytes(), bid.length(), broadcastAddress,
                    MainApp.BROADCAST_PORT);
            socket.send(packet);
    
            byte[] buffer = new byte[512];
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
    
            System.out.println("MainApp.auctions:\n");
            while (true) {
                try {
                    socket.receive(response);
                    String responseMessage = new String(response.getData(), 0, response.getLength());
                    System.out.println(responseMessage);
                } catch (SocketTimeoutException e) {
                    break;
                }
            }
        } catch (SocketTimeoutException e) {
            System.out.println("Nachricht nicht gesendet oder empfangen: Timeout");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void info() {
        int sum = 0;
        for (Bid aBid : MainApp.bids.values()) {
            sum += aBid.bid;
        }
        System.out.println("Balance: " + MainApp.getBalance() + " | Bidding: " + sum + " | Rest: " + (MainApp.getBalance() - sum));
    }
    
    public static void bieten(int price, String username, String filename) {
        int sum = 0;
        for (Bid aBid : MainApp.bids.values()) {
            if(!aBid.fileName.equals(filename)){
                sum += aBid.bid;
            }
        }
        if(price > MainApp.getBalance() || price > MainApp.getBalance() - sum){
            System.out.println("Sie haben nicht genug Geld!!");
            return;
        }
        for (Bid bid : MainApp.bids.values()) {
            if (bid.fileName.equals(filename)) {
                if (price <= bid.bid) {
                    System.out.println("Das Gebot darf nicht niedriger als dein letztes Gebot sein.");
                    return;
                }
            }
        }
        String userIP = findUser(username);
        if (userIP == null) {
            System.out.println("User not found");
            return;
        } else {
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setSoTimeout(10000);
                socket.setBroadcast(true);
                InetAddress broadcastAddress = InetAddress.getByName(MainApp.BROADCAST_ADDRESS);
                String bid = "nachricht:" + System.getenv("USER") + " bietet " + price + " für " + filename + ".";
    
                DatagramPacket packet = new DatagramPacket(bid.getBytes(), bid.length(), broadcastAddress,
                        MainApp.BROADCAST_PORT);
                socket.send(packet);
            } catch (SocketTimeoutException e) {
                System.out.println("Nachricht nicht gesendet oder empfangen: Timeout");
            } catch (IOException e) {
                e.printStackTrace();
            }
    
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setSoTimeout(10000);
                InetAddress userAddress = InetAddress.getByName(userIP);
    
                String bid = "bieten:" + System.getenv("USER") + ";" + price + ";" + filename;
                DatagramPacket packet = new DatagramPacket(bid.getBytes(), bid.length(), userAddress, MainApp.BROADCAST_PORT);
                socket.send(packet);
            } catch (SocketTimeoutException e) {
                System.out.println("Nachricht nicht gesendet oder empfangen: Timeout");
            } catch (IOException e) {
                e.printStackTrace();
            }
    
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setSoTimeout(10000);
                InetAddress broadcastAddress = InetAddress.getByName(MainApp.BROADCAST_ADDRESS);
    
                String bid = "neuGebot:" + System.getenv("USER") + ";" + filename;
                DatagramPacket packet = new DatagramPacket(bid.getBytes(), bid.length(), broadcastAddress, MainApp.BROADCAST_PORT);
                socket.send(packet);
            } catch (SocketTimeoutException e) {
                System.out.println("Nachricht nicht gesendet oder empfangen: Timeout");
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            Bid bidInfo = new Bid(username, price, filename);
            MainApp.bids.put(filename, bidInfo);
        }
    }
    
    public static String findUser(String username) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.setSoTimeout(10000);
    
            String msg = "CHECK_NAME:" + username;
            InetAddress broadcastAddress = InetAddress.getByName(MainApp.BROADCAST_ADDRESS);
            DatagramPacket request = new DatagramPacket(msg.getBytes(), msg.length(), broadcastAddress, MainApp.BROADCAST_PORT);
            socket.send(request);
    
            byte[] buffer = new byte[512];
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
    
            while (true) {
                try {
                    socket.receive(response);
                    String responseAddress = new String(response.getData(), 0, response.getLength()).trim();
                    return responseAddress;
                } catch (SocketTimeoutException e) {
                    break;
                }
            }
        } catch (SocketTimeoutException e) {
            System.out.println("Nachricht nicht gesendet oder empfangen: Timeout");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static void sendFileToWinner(String fileName, String winnerAddress) {
        try (DatagramSocket socket = new DatagramSocket()) {
            try (BufferedReader br = new BufferedReader(new FileReader("dateien/" + fileName))) {
                String line;
                while ((line = br.readLine()) != null) {
                    byte[] data = ("File:" + line).getBytes();
                    DatagramPacket packet = new DatagramPacket(data, data.length,
                            InetAddress.getByName(findUser(winnerAddress)), MainApp.BROADCAST_PORT);
                    socket.send(packet);
                }
                byte[] data = ("File:EOF").getBytes();
                DatagramPacket packet = new DatagramPacket(data, data.length,
                        InetAddress.getByName(findUser(winnerAddress)), MainApp.BROADCAST_PORT);
                socket.send(packet);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        File file = new File("dateien/" + fileName);
        if(file.delete()){
            System.out.println("Datei erfolgreich gesendet!");
        }
    }
    
    public static void sendMoney(DatagramSocket requestSocket, String fileNameWon, int priceWon) throws IOException {
        String money = "Geld:" + priceWon;
        String winningSeller = MainApp.bids.get(fileNameWon).seller;
        InetAddress sellerAddress = InetAddress.getByName(findUser(winningSeller));
        DatagramPacket moneyPacket = new DatagramPacket(money.getBytes(), money.length(),
                sellerAddress,
                MainApp.BROADCAST_PORT);
        requestSocket.send(moneyPacket);
        System.out.println("Geld gesendet: " + priceWon);
        MainApp.setBalance(MainApp.getBalance() - priceWon);;
    }
}

