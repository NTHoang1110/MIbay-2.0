package Application;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        Scene scene = new Scene(root, 600, 400);

        Circle circle = new Circle(20, Color.BLUE);
        circle.setCenterX(50);
        circle.setCenterY(200);
        root.getChildren().add(circle);

        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdate = 0;
            private double speed = 300; // Pixels per second

            @Override
            public void handle(long now) {
                if (lastUpdate > 0) {
                    double deltaTime = (now - lastUpdate) / 1_000_000_000.0; // Convert nanoseconds to seconds
                    circle.setCenterX(circle.getCenterX() + speed * deltaTime);
                }
                lastUpdate = now;

                // Reset position when it reaches the end
                if (circle.getCenterX() > 600) {
                    circle.setCenterX(50);
                }
            }
        };
        timer.start();

        primaryStage.setTitle("AnimationTimer Example");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

