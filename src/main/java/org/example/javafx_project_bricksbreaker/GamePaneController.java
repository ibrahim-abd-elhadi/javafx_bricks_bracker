package org.example.javafx_project_bricksbreaker;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static javafx.scene.paint.Color.*;

public class GamePaneController implements Initializable {

    @FXML
    private AnchorPane root;
    @FXML
    private Slider sliderangel;
    @FXML
    private Text ballnum;
    @FXML
    private Text bestScore;
    private List<Text> healthTexts = new ArrayList<>();
    private AnimationTimer gameTimer;
    private double ballRadius = 11.5;
    private Rectangle[] bricks;

    private int[] brickHealth;
    private int ballsFallen = 0;
    private ArrayList<Circle> balls = new ArrayList<>();
    private int maxHealth = 30; // Maximum health of a brick
    private int numBricks = 88; // Total number of bricks
    private int cannonX = 350; // X position of the cannon
    private int cannonY = 720; // Y position of the cannon
    private Rectangle cannon; // Cannon shape
    private int numBallsToLaunch = 44; // Number of balls to launch
    private int ballsLaunched = 0; // Counter for launched balls
    private boolean gameStarted = false;
    private Line alignment;
    private int score = 0;
    @FXML
    private Text scoreText; // For displaying the score on the screen
    private int BestScore = 0;
    private final String SCORE_FILE = "E:\\College\\CSE 1st year\\2nd semester\\Programming\\javafx_bricks_breacker\\src\\main\\resources\\org\\example\\javafx_project_bricksbreaker\\best_score.txt";
    private Media brickHitSound;
    private MediaPlayer brickHitMediaPlayer;



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeBrickHealth();
        createBricks();
        createCannon();
        createAlignment();
        loadBestScore();
        bestScore.setText("Best Score: " + BestScore);

        gameTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateBalls();
                updateBestScore(score);
                ballnum.setText("x"+ (numBallsToLaunch-ballsLaunched)+"");
                for (Rectangle brick : bricks) {
                    if (brick != null && brick.getY() >= 591) {
                        this.stop(); // Stop the game timer
                        Platform.runLater(this::showEndGameDialog); // Show dialog on JavaFX Application Thread
                        break;
                    } else if (areAllBricksInvisible() && (ballsFallen ==numBallsToLaunch)) {
                        this.stop(); // Stop the game timer
                        maxHealth+=20;
                        Platform.runLater(this::showEndlevelupDialog); // Show dialog on JavaFX Application Thread
                        break;

                    }
                }
                if (ballsFallen == numBallsToLaunch && gameStarted) {
                    reloadBalls(); // Reset game to start new launch
                }

            }

            private void showEndGameDialog() {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Game Over");
                alert.setHeaderText("A brick has reached the critical position!");
                alert.setContentText("Choose your option:");

                ButtonType buttonTypeRetry = new ButtonType("Retry");
                ButtonType buttonTypeExit = new ButtonType("Exit");
                alert.getButtonTypes().setAll(buttonTypeRetry, buttonTypeExit);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent()) {
                    if (result.get() == buttonTypeRetry) {
                        maxHealth=30;
                        score = 0;
                        updateScore(0);
                        reset();  // Reset game state and start over
                        gameTimer.start();  // Restart the game timer
                    } else if (result.get() == buttonTypeExit) {
                        System.exit(0);  // Exit the game
                    }
                } else {
                    // Log when no selection is made and the dialog is closed
                    System.out.println("No selection made, dialog closed.");
                }
            }


            private void showEndlevelupDialog() {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("congratulations");
                alert.setHeaderText("you win this level!");
                alert.setContentText("Choose your option:");

                ButtonType buttonTypeRetry = new ButtonType("next level");
                ButtonType buttonTypeExit = new ButtonType("Exit");
                alert.getButtonTypes().setAll(buttonTypeRetry, buttonTypeExit);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent()) {
                    if (result.get() == buttonTypeRetry) {
                        reset();  // Reset game state and start over
                        gameTimer.start();  // Restart the game timer
                    } else if (result.get() == buttonTypeExit) {
                        System.exit(0);  // Exit the game
                    }
                } else {
                    // Log when no selection is made and the dialog is closed
                    System.out.println("No selection made, dialog closed.");
                }
            }

        };
        gameTimer.start();

        sliderangel.valueChangingProperty().addListener((obs, wasChanging, isNowChanging) -> {
            if (wasChanging && !isNowChanging) {
                launchBalls(sliderangel.getValue());
                gameStarted = true;
                sliderangel.setValue(90);
            }
        });
        brickHitSound = new Media(new File("src/main/resources/org/example/javafx_project_bricksbreaker/Sound_Effects/Hit.mp3").toURI().toString());
        brickHitMediaPlayer = new MediaPlayer(brickHitSound);
    }

    private boolean areAllBricksInvisible() {
        for (Rectangle brick : bricks) {
            if (brick != null && brick.isVisible()) {
                return false;  // Return false immediately if any brick is visible
            }
        }
        return true;  // All bricks are invisible
    }

    private void reset() {
        // Reset all game variables and states to their initial values
        ballsFallen = 0;
        balls.clear();
        gameStarted = false;
        healthTexts.clear();
        alignment.setVisible(false);




        root.getChildren().removeIf(node ->
                (node instanceof Rectangle ||
                        (node instanceof Text && node != scoreText && node != ballnum && node != bestScore)));



        // Reset bricks and cannon positions
        initializeBrickHealth();
        createBricks();
        createCannon();
        createAlignment();

        // Start the game timer again
        gameTimer.start();
    }

    private void loadBestScore() {
        File scoreFile = new File(SCORE_FILE);

        if (!scoreFile.exists()) {
            System.out.println("Score file does not exist. Creating a new file...");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(scoreFile))) {
            String line = reader.readLine();
            if (line != null && !line.isEmpty()) {
                BestScore = Integer.parseInt(line.trim());
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error reading best score from file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveBestScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SCORE_FILE))) {
            writer.write(Integer.toString(BestScore));
        } catch (IOException e) {
            // Handle exceptions
            e.printStackTrace();
        }
    }

    public void updateBestScore(int newScore) {
        if (score > BestScore) {
            BestScore = score;
            saveBestScore();
        }
    }



    private void updateScore(int points) {
        score += points;
        scoreText.setText("Score: " + score); // Update the displayed text
    }


    private void initializeBrickHealth() {
        brickHealth = new int[numBricks];
        for (int i = 0; i < numBricks; i++) {
            brickHealth[i] = maxHealth;
        }
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
            if (ballsFallen ==numBallsToLaunch) {
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
        int spacing = 4; // Spacing between bricks
        int numRows = 8; // Number of rows
        int numCols = 11; // Number of columns
        int startX = 50; // Starting X position
        int startY = 80; // Starting Y position

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
                            brick.setStroke(Color.AZURE);
                            brick.setStrokeWidth(3);
                            break;
                        case 1:
                            brick.setFill(Color.VIOLET);
                            brick.setArcWidth(20); // Set the arc width
                            brick.setArcHeight(20); // Set the arc height
                            brick.setStroke(Color.AZURE);
                            brick.setStrokeWidth(3);
                            break;
                        case 2:
                            brick.setFill(Color.RED);
                            brick.setArcWidth(20); // Set the arc width
                            brick.setArcHeight(20); // Set the arc height
                            brick.setStroke(Color.AZURE);
                            brick.setStrokeWidth(3);
                            break;
                        case 3:
                            brick.setFill(Color.BLUEVIOLET);
                            brick.setArcWidth(20); // Set the arc width
                            brick.setArcHeight(20);
                            brick.setStroke(Color.AZURE);
                            brick.setStrokeWidth(3);
                            break;
                    }
                    root.getChildren().add(brick);

                    bricks[index] = brick;

                    // Create text node for displaying health
                    Text healthText = new Text(Integer.toString(brickHealth[index]));
                    healthText.setId(Integer.toString(index));
                    healthText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
                    healthText.setFill(WHITE);
                    healthText.setX(x + brickSize / 2 -7); // Center text horizontally
                    healthText.setY(y + brickSize / 2 + 5); // Center text vertically
                    root.getChildren().add(healthText); // Add text to AnchorPane
                    healthTexts.add(healthText);
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
        cannon.setFill(DARKORANGE);
        cannon.setArcHeight(5);
        cannon.setArcWidth(5);
        root.getChildren().add(cannon);
    }
    private void launchBalls(double angel) {
        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdate = 0; // Track the time of the last ball launch
            private final long interval = 49500000; // 1 second interval between launches
            private final double fixedAngle = Math.toRadians(-angel+180); // Launch angle

            @Override
            public void handle(long now) {
                if (ballsLaunched < numBallsToLaunch && (lastUpdate == 0 || now - lastUpdate >= interval)) {

                    double speed = 13; // Set a constant speed for each ball


                    double vx = Math.cos(fixedAngle) * speed;
                    double vy = -Math.sin(fixedAngle) * speed;

                    Circle newBall = new Circle(cannonX, cannonY - ballRadius / 2, ballRadius, Color.LIGHTPINK);
                    newBall.setUserData(new double[]{vx, vy});
                    newBall.setStroke(RED);
                    newBall.setStrokeWidth(3);
                    root.getChildren().add(newBall);
                    balls.add(newBall);

                    ballsLaunched++;
                    lastUpdate = now;
                }

                if (ballsLaunched >= numBallsToLaunch) {
                    this.stop();
                    sliderangel.setValue(90);
                }
            }
        };
        timer.start();
    }
    private void reloadBalls() {
        // Reset game state here
        balls.clear();        // Clear all balls
        ballsLaunched = 0;    // Reset the number of balls launched
        ballsFallen = 0;      // Reset the number of balls fallen
        gameStarted = false;  // Allow a new game to start
    }

    private void repositionBricks() {
        int deltaY = 30;  // Amount to move bricks down
        for (Rectangle brick : bricks) {
            if (brick != null&& brick.isVisible()) {
                brick.setY(brick.getY() + deltaY);
            }
        }
        for (Text text : healthTexts) {
            text.setY(text.getY() + deltaY);
        }
    }




    private void updateHealthText(int index) {
        // Find the health text node associated with the given brick index
        for (Node node : root.getChildren()) {
            if (node instanceof Text) {
                Text healthText = (Text) node;
                // Check if the ID is numeric and matches the index before updating
                try {
                    int textId = Integer.parseInt(healthText.getId());
                    if (textId == index) {
                        if (brickHealth[index] == 0) healthText.setVisible(false);
                        healthText.setText(Integer.toString(brickHealth[index]));
                        break; // Exit loop once the health text is found and updated
                    }
                } catch (NumberFormatException e) {

                }
            }
        }
    }



    private void checkBrickCollision(Circle ball, double[] velocity) {
        double vx = velocity[0];
        double vy = velocity[1];

        for (int i = 0; i < numBricks; i++) {
            if (brickHealth[i] > 0 && bricks[i] != null && ball.getBoundsInParent().intersects(bricks[i].getBoundsInParent())) {
                brickHealth[i]--;
                playBrickHitSound();  // Play the sound effect
                updateScore(1);
                updateHealthText(i);
                if (brickHealth[i] == 0) {
                    bricks[i].setVisible(false);     // Hide the brick if its health is depleted
                    updateScore(20);
                }

                double ballCenterX = ball.getCenterX();
                double ballCenterY = ball.getCenterY();
                double brickTop = bricks[i].getY();
                double brickBottom = brickTop + bricks[i].getHeight();
                double brickLeft = bricks[i].getX();
                double brickRight = brickLeft + bricks[i].getWidth();

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
    public void createAlignment() {
        alignment = new Line();
        alignment.setStartX(cannonX);
        alignment.setStartY(cannonY);
        alignment.setStroke(VIOLET);
        alignment.setStrokeWidth(9);
        alignment.getStrokeDashArray().addAll(30d, 20d);
        root.getChildren().add(alignment);
        alignment.setVisible(false);

        // Attach a listener to update the alignment line as the slider value changes
        sliderangel.setOnMouseDragged(e-> {
            alignment.setVisible(true);
            updateAlignmentLine(sliderangel.getValue());
        });

        // Listen for when the user stops dragging the slider
        sliderangel.setOnMouseReleased(e->{
            alignment.setVisible(false);

        });

    }



    private void updateAlignmentLine(double angleDegrees) {
        double angleRadians = Math.toRadians(-angleDegrees+180);
        double lineLength = 1000; // Large enough to ensure it reaches out of the game area
        double endX = cannonX + lineLength * Math.cos(angleRadians);
        double endY = cannonY - lineLength * Math.sin(angleRadians);
        alignment.setEndX(endX);
        alignment.setEndY(endY);
        adjustLineEndToBrickCollision();
    }


    private void adjustLineEndToBrickCollision() {
        double closestIntersectionDistance = Double.MAX_VALUE;
        Point2D closestIntersection = null;
        Point2D cannonPoint = new Point2D(cannonX, cannonY);

        for (Rectangle brick : bricks) {
            if (brick != null && brick.isVisible()) {
                List<Point2D> intersections = findLineRectIntersections(cannonX, cannonY, alignment.getEndX(), alignment.getEndY(), brick);
                for (Point2D intersection : intersections) {
                    if (intersection != null) {
                        double distance = cannonPoint.distance(intersection);
                        if (distance < closestIntersectionDistance) {
                            closestIntersectionDistance = distance;
                            closestIntersection = intersection;
                        }
                    }
                }
            }
        }

        if (closestIntersection != null) {
            alignment.setEndX(closestIntersection.getX());
            alignment.setEndY(closestIntersection.getY());
        }

    }


    private List<Point2D> findLineRectIntersections(double x1, double y1, double x2, double y2, Rectangle rect) {
        List<Point2D> intersections = new ArrayList<>();
        // Check each side of the rectangle
        intersections.add(intersectLines(x1, y1, x2, y2, rect.getX(), rect.getY(), rect.getX() + rect.getWidth(), rect.getY())); // Top
        intersections.add(intersectLines(x1, y1, x2, y2, rect.getX(), rect.getY() + rect.getHeight(), rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight())); // Bottom
        intersections.add(intersectLines(x1, y1, x2, y2, rect.getX(), rect.getY(), rect.getX(), rect.getY() + rect.getHeight())); // Left
        intersections.add(intersectLines(x1, y1, x2, y2, rect.getX() + rect.getWidth(), rect.getY(), rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight())); // Right
        return intersections.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }


    private Point2D intersectLines(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        double denom = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (denom == 0) return null; // Lines are parallel, no intersection

        double intersectX = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / denom;
        double intersectY = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / denom;

        // Ensure the intersection point is within the line segment bounds
        if (intersectX < Math.min(x3, x4) || intersectX > Math.max(x3, x4) ||
                intersectY < Math.min(y3, y4) || intersectY > Math.max(y3, y4)) {
            return null;
        }
        return new Point2D(intersectX, intersectY);
    }

    private void playBrickHitSound() {
        // Create a new MediaPlayer instance for each hit sound
        MediaPlayer mediaPlayer = new MediaPlayer(brickHitSound);
        mediaPlayer.play();  // Play the sound effect
        mediaPlayer.setOnEndOfMedia(() -> mediaPlayer.dispose()); // Dispose the MediaPlayer after the sound finishes playing
    }


}

