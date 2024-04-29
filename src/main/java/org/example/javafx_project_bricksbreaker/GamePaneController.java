package org.example.javafx_project_bricksbreaker;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class GamePaneController implements Initializable {

    @FXML
    private Rectangle recme;


    @FXML
    private AnchorPane root;

    private Circle ball;
    private double ballRadius = 10;
    private double ballSpeedX = 3;
    private double ballSpeedY = 3;
    private Rectangle[] bricks;
    private int[] brickHealth;
    private int maxHealth = 1; // Maximum health of a brick
    private int numBricks = 88; // Total number of bricks
    private int cannonX = 350; // X position of the cannon
    private int cannonY = 720; // Y position of the cannon
    private Rectangle cannon; // Cannon shape
    private int numBallsToLaunch = 60; // Number of balls to launch
    private int ballsLaunched = 0; // Counter for launched balls

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeBrickHealth(); // Initialize brick health array
        createBall();
        createBricks();
        createCannon();
        // Launch balls from the cannon
        launchBalls();
        // Animation timer for ball movement

        // Animation timer for ball movement and collision detection
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                moveBall();
                checkBallCollision();
            }
        };
        timer.start();
    }

    private void initializeBrickHealth() {
        brickHealth = new int[numBricks];
        for (int i = 0; i < numBricks; i++) {
            brickHealth[i] = maxHealth;
        }
    }

    private void createBall() {
        ball = new Circle(300, 400, ballRadius, Color.LIGHTPINK);
        root.getChildren().add(ball);
    }

    private void moveBall() {
        ball.setCenterX(ball.getCenterX() + ballSpeedX);
        ball.setCenterY(ball.getCenterY() + ballSpeedY);
    }

    private void checkBallCollision() {
        if (ball.getCenterX() <= 0 || ball.getCenterX() >= root.getWidth()) {
            ballSpeedX *= -1;
        }
        if (ball.getCenterY() <= 0 || ball.getCenterY() >= root.getHeight()) {
            ballSpeedY *= -1;
        }
        checkBrickCollision();
    }


    private void createBricks() {
        int brickSize = 50; // Size of each brick (width and height)
        int spacing = 5; // Spacing between bricks
        int numRows = 8; // Number of rows
        int numCols = 11; // Number of columns
        int startX = 50; // Starting X position
        int startY = 50; // Starting Y position

        bricks = new Rectangle[numBricks];

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
                    root.getChildren().add(brick);

                    bricks[index] = brick;

                    /*// Create text node for displaying health
                    Text healthText = new Text(Integer.toString(brickHealth[index]));
                    healthText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    healthText.setFill(Color.WHITE);
                    healthText.setX(x + brickSize / 2 - 5); // Center text horizontally
                    healthText.setY(y + brickSize / 2 + 5); // Center text vertically
                    root.getChildren().add(healthText); // Add text to AnchorPane*/
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
    private void launchBalls() {
        // Launch balls one by one
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (ballsLaunched < numBallsToLaunch) {
                    // Create a new ball and set its position to the cannon
                    Circle newBall = new Circle(cannonX, cannonY - ballRadius / 2, ballRadius, Color.LIGHTPINK);
                    root.getChildren().add(newBall);
                    // Adjust speed and angle of the ball
                    double angle = Math.random() * Math.PI / 4 - Math.PI / 8; // Random angle between -π/8 and π/8
                    double speed = Math.random() * 5 + 5; // Random speed between 5 and 10
                    double vx = Math.sin(angle) * speed;
                    double vy = -Math.cos(angle) * speed;
                    // Animate the ball's movement
                    AnimationTimer ballTimer = new AnimationTimer() {
                        @Override
                        public void handle(long now) {
                            newBall.setCenterX(newBall.getCenterX() + vx);
                            newBall.setCenterY(newBall.getCenterY() + vy);
                            if (newBall.getCenterY() <= 0) {
                                // Remove the ball when it goes out of bounds
                                root.getChildren().remove(newBall);
                                this.stop();
                            }
                        }
                    };
                    ballTimer.start();
                    ballsLaunched++;
                } else {
                    this.stop(); // Stop launching balls when the desired number is reached
                }
            }
        };
        timer.start();
    }

    private void updateHealthText(int index) {
        // Find the health text node associated with the given brick index
        for (Node node : root.getChildren()) {
            if (node instanceof Text) {
                Text healthText = (Text) node;
                if (root.getChildren().indexOf(healthText) % 2 != 0) { // Skip every other node (assume health texts are added after bricks)
                    continue;
                }
                int brickIndex = root.getChildren().indexOf(healthText) / 2; // Calculate the brick index from the health text index
                if (brickIndex == index) {
                    // Update the health text with the new health value
                    healthText.setText(Integer.toString(brickHealth[index]));
                    break; // Exit loop once the health text is found and updated
                }
            }
        }
    }


    private void checkBrickCollision() {
        for (int i = 0; i < numBricks; i++) {
            if (brickHealth[i] > 0 && bricks[i] != null && ball.getBoundsInParent().intersects(bricks[i].getBoundsInParent())) {
                brickHealth[i]--;
                if (brickHealth[i] == 0) {
                    bricks[i].setVisible(false);
                }
                double ballCenterX = ball.getCenterX();
                double ballCenterY = ball.getCenterY();

                double brickTop = bricks[i].getY();
                double brickBottom = brickTop + bricks[i].getHeight();
                double brickLeft = bricks[i].getX();
                double brickRight = brickLeft + bricks[i].getWidth();

                if (ballCenterX > brickLeft && ballCenterX < brickRight) {
                    ballSpeedY *= -1;

                } else if (ballCenterY > brickTop && ballCenterY < brickBottom) {
                    ballSpeedX *= -1;
                }
            }
        }
    }

}
