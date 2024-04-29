package org.example.javafx_project_bricksbreaker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Level {

    private List<String> levels;

    public Level(String filename) {
        levels = new ArrayList<>();
        loadLevels(filename);
    }

    private void loadLevels(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                levels.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLevel(int levelNumber) {
        if (levelNumber >= 0 && levelNumber < levels.size()) {
            return levels.get(levelNumber);
        }
        return null;
    }
}
