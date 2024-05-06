package org.example.javafx_project_bricksbreaker;
/////////////////////////////////////////////////////////////  the once
import javafx.animation.AnimationTimer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.*;

public class GamePaneController implements Initializable {

    @FXML
    private Rectangle recme;


    @FXML
    private AnchorPane root;
    @FXML
    private Slider sliderangel;


    private Circle ball;
    private int slidersetangel;
    private double ballRadius = 10;
    private double ballSpeedX = 3;
    private double ballSpeedY = 3;
    private List<Map> bricks;
    private int ballsFallen = 0;
    private ArrayList<Circle> balls = new ArrayList<>();
    private final int MAXHEALTH = 30; // Maximum health of a brick
    private final int NUMBRICKS = 88; // Total number of bricks
    private int cannonX = 350; // X position of the cannon
    private int cannonY = 720; // Y position of the cannon
    private Rectangle cannon; // Cannon shape
    private int numBallsToLaunch = 40; // Number of balls to launch
    private int ballsLaunched = 0; // Counter for launched balls

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        createBricks();
        createCannon();
        sliderangel.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                slidersetangel = (int) sliderangel.getValue();
            }
        });
        launchBalls(slidersetangel); // Launch multiple balls from the cannon

        // Animation timer for ball movement and collision detection
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateBalls(); // Update the position and check collision for each ball
            }
        };
        timer.start();
    }


    private void createBall() {
        ball = new Circle(300, 400, ballRadius, Color.LIGHTPINK);
        root.getChildren().add(ball);
    }

    private void moveBall() {
        ball.setCenterX(ball.getCenterX() + ballSpeedX);
        ball.setCenterY(ball.getCenterY() + ballSpeedY);
    }


    private void updateBalls() {
        ArrayList<Circle> ballsToRemove = new ArrayList<>();
        // Count the number of balls that have hit the bottom

        for (Circle ball : balls) {
            double[] velocity = (double[]) ball.getUserData();
            double vx = velocity[0];
            double vy = velocity[1];

            // Update the position of the ball
            ball.setCenterX(ball.getCenterX() + vx);
            ball.setCenterY(ball.getCenterY() + vy);

            // Check for horizontal boundary collisions and reverse direction
            if (ball.getCenterX() <= ballRadius || ball.getCenterX() >= root.getWidth() - ballRadius) {
                velocity[0] *= -1;
            }

            // Check for vertical boundary collisions and reverse direction
            if (ball.getCenterY() <= ballRadius) {
                velocity[1] *= -1;
            }

            // Check if the ball hits the bottom of the screen
            if (ball.getCenterY() >= root.getHeight() - ballRadius) {
                ballsToRemove.add(ball); // Schedule the ball for removal
                ballsFallen++;  // Increment the count of fallen balls
            }
            if (ballsFallen == numBallsToLaunch) {
                repositionBricks();
            }
            // Update the velocity after handling collisions
            ball.setUserData(velocity);

            // Handle collisions with bricks
            checkBrickCollision(ball, velocity);

        }

        // Remove balls that have hit the bottom
        balls.removeAll(ballsToRemove);
        root.getChildren().removeAll(ballsToRemove);

        // If all balls have fallen, reposition the bricks

    }


    private void createBricks() {
        int brickSize = 50; // Size of each brick (width and height)
        int spacing = 5; // Spacing between bricks
        int numRows = 8; // Number of rows
        int numCols = 11; // Number of columns
        int startX = 50; // Starting X position
        int startY = 50; // Starting Y position

        bricks = new ArrayList<>();


        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                if (isBrick(row, col)) {
                    int index = row * numCols + col;

                    int x = startX + col * (brickSize + spacing);
                    int y = startY + row * (brickSize + spacing);

                    Rectangle brick = new Rectangle(x, y, brickSize, brickSize);
                    switch (row % 4) {
                        case 0:
                            brick.setFill(Color.ORANGE);
                            brick.setArcWidth(20); // Set the arc width
                            brick.setArcHeight(20); // Set the arc height
                            break;
                        case 1:
                            brick.setFill(Color.VIOLET);
                            brick.setArcWidth(20); // Set the arc width
                            brick.setArcHeight(20); // Set the arc height
                            break;
                        case 2:
                            brick.setFill(Color.RED);
                            brick.setArcWidth(20); // Set the arc width
                            brick.setArcHeight(20); // Set the arc height
                            break;
                        case 3:
                            brick.setFill(Color.BLUEVIOLET);
                            brick.setArcWidth(20); // Set the arc width
                            brick.setArcHeight(20);
                            break;
                    }

                    // Create text node for displaying health
                    Text healthText = new Text(Integer.toString(MAXHEALTH));
                    healthText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    healthText.setFill(Color.WHITE);
                    healthText.setX(x + (double) brickSize / 2 - 5); // Center text horizontally
                    healthText.setY(y + (double) brickSize / 2 + 5); // Center text vertically
                    root.getChildren().add(brick);
                    root.getChildren().add(healthText); // Add text to AnchorPane

                    Map<String, Object> brickMap = new HashMap<>() {{
                        put("brick", brick);
                        put("text", healthText);
                        put("health", MAXHEALTH);
                        put("id", index);
                    }};
                    bricks.add(brickMap);
                } else {
                    Map<String, Object> brick = new HashMap<>();
                    bricks.add(brick);
                }
            }
        }
    }

    private boolean isBrick(int row, int col) {
        String design = "###  ## ###" +
                "# ## #   ###" +
                "##    #   #" +
                "##     ####" +
                "###  ## ###" +
                "# ## #   ###" +
                "##    #   #" + "##     ####";
        int index = row * 11 + col;
        return design.charAt(index) == '#';
    }

    private void createCannon() {
        // Create cannon shape
        cannon = new Rectangle(cannonX - ballRadius / 2, cannonY, ballRadius, ballRadius * 2);
        cannon.setFill(Color.GRAY);
        root.getChildren().add(cannon);
    }

    private void launchBalls(int angel) {
        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdate = 0; // Track the time of the last ball launch
            private final long interval = 50000000; // 1 second interval between launches
            private final double fixedAngle = Math.toRadians(45); // Launch angle

            @Override
            public void handle(long now) {
                if (ballsLaunched < numBallsToLaunch && (lastUpdate == 0 || now - lastUpdate >= interval)) {
                    double speed = 10; // Set a constant speed for each ball
                    double vx = Math.cos(fixedAngle) * speed;
                    double vy = -Math.sin(fixedAngle) * speed;

                    Circle newBall = new Circle(cannonX, cannonY - ballRadius / 2, ballRadius, Color.LIGHTPINK);
                    newBall.setUserData(new double[]{vx, vy});
                    root.getChildren().add(newBall);
                    balls.add(newBall);

                    ballsLaunched++;
                    lastUpdate = now;
                }

                if (ballsLaunched >= numBallsToLaunch) {
                    this.stop();
                }
            }
        };
        timer.start();
    }

    private void repositionBricks() {
        int deltaY = 20;  // Amount to move bricks down
        for (Map brickMap : bricks) {
            if (brickMap.isEmpty()) continue;
            Rectangle brick = (Rectangle) brickMap.get("brick");
            Text healthText = (Text) brickMap.get("text");
            brick.setY(brick.getY() + deltaY);
            healthText.setY(healthText.getY() + deltaY);
        }
    }




    private void checkBrickCollision(Circle ball, double[] velocity) {
        double vx = velocity[0];
        double vy = velocity[1];

        for (int i = 0; i < NUMBRICKS; i++) {
            if (bricks.get(i).isEmpty()) continue;
            Map brickMap = bricks.get(i);
            Rectangle brick = (Rectangle) brickMap.get("brick");
            int brickHealth = (int) brickMap.get("health");
            Text brickText = (Text) brickMap.get("text");
            if (brickHealth > 0 && ball.getBoundsInParent().intersects(brick.getBoundsInParent())) {
                brickMap.put("health", brickHealth - 1);
                brickText.setText(String.valueOf(brickHealth));
                // casting health again to get updated value
                if ((int) (brickMap.get("health")) == 0) {
                    brick.setVisible(false); // Hide the brick if its health is depleted
                    brickText.setVisible(false); // Hide brick text if health is depleted
                }

                double ballCenterX = ball.getCenterX();
                double ballCenterY = ball.getCenterY();
                double brickTop = brick.getY();
                double brickBottom = brickTop + brick.getHeight();
                double brickLeft = brick.getX();
                double brickRight = brickLeft + brick.getWidth();

                // Check which side of the brick the ball has hit and reverse the appropriate velocity component
                if (ballCenterX > brickLeft && ballCenterX < brickRight) {
                    vy *= -1; // Vertical bounce
                } else {
                    vx *= -1; // Horizontal bounce
                }

                // Update the velocity in the ball's user data
                velocity[0] = vx;
                velocity[1] = vy;
                break; // Break the loop after handling collision to avoid multiple collision responses
            }
        }
    }


}
