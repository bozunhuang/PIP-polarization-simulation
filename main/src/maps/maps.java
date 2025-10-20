package maps;

import tileengine.DTile;
import tileengine.TERenderer;
import tileengine.TETile;
import tileengine.Tileset;

public class maps {
    private static final int WIDTH = 101;
    private static final int HEIGHT = 101;
    private static final int RADIUS = 50;

    public static final TETile[][] CIRCLE_MAP = generateCircularMap(new TETile[WIDTH][HEIGHT]);
    public static final TETile[][] NEURITE_MAP = generateNeuriteMap(new TETile[WIDTH][HEIGHT]);

    public static void main(String[] args) {
        TERenderer renderer = new TERenderer();
        renderer.initialize(WIDTH, HEIGHT);
        renderer.renderFrame(NEURITE_MAP);
    }

    private static TETile[][] generateNeuriteMap(TETile[][] map) {
        initialize(map);
        addNeuriteFloor(map);
        addWalls(map);
        return map;
    }

    private static TETile[][] generateCircularMap(TETile[][] map){
        initialize(map);
        addCircularFloor(map);
        addWalls(map);
        return map;
    }

    private static void initialize(TETile[][] map){
        for (int i = 0; i < WIDTH; i++){
            for (int j = 0; j < HEIGHT; j++){
                map[i][j] = Tileset.NOTHING;
            }
        }
    }

    public static void addCircularFloor(TETile[][] map) {
        for (int x = 0; x < 2 * RADIUS + 1; x++) {
            for (int y = 0; y < 2 * RADIUS + 1; y++) {
                long curr_right = Math.round(Math.sqrt(Math.pow(x + RADIUS, 2) + Math.pow(y + RADIUS, 2)));
                long curr_left = Math.round(Math.sqrt(Math.pow(x - RADIUS, 2) + Math.pow(y - RADIUS, 2)));

                if (RADIUS > curr_left && RADIUS < curr_right) {
                    map[x][y] = new DTile();
                }
            }
        }
    }

    public static void addNeuriteFloor(TETile[][] map) {
        int xOffset = 10;
        int yOffset = 10;
        int r1 = RADIUS - xOffset;
        int r2 = yOffset;

        // Draws the big circle
        for (int x = 0; x < 2 * r1 + 1; x++) {
            for (int y = 0; y < 2 * r1 + 1; y++) {
                long curr_right = Math.round(Math.sqrt(Math.pow(x + r1, 2) + Math.pow(y + r1, 2)));
                long curr_left = Math.round(Math.sqrt(Math.pow(x - r1, 2) + Math.pow(y - r1, 2)));

                if (r1 > curr_left && r1 < curr_right) {
                    map[x][y] = new DTile();
                }
            }
        }

        // Draws a rectangular channel
        for (int y = 2 * r1; y > 2 * r1 - 8; y--) {
            for (int x = r1; x < 2 * r1; x++) {
                // Add regular tiles for the overlaps with the big circle
                if (x < 1.5 * r1){
                    map[x][y] = new DTile();
                } else {
                // Add tracker tiles for the extended region
                map[x][y] = new DTile("Dendrite");
                }
            }
        }

        // Draws a small circle
        int initialX = 2 * r1 - 1;
        int finalX = initialX + 2 * r2 + 1;
        int initialY = 2 * r1 - r2 - 4;
        int finalY = initialY + 2 * r2 + 1;

        for (int x = initialX; x < finalX; x++) {
            for (int y = initialY; y < finalY; y++) {
                long curr_right = Math.round(Math.sqrt(Math.pow(x - initialX + r2, 2) + Math.pow(y - initialY + r2, 2)));
                long curr_left = Math.round(Math.sqrt(Math.pow(x - initialX - r2, 2) + Math.pow(y - initialY - r2, 2)));

                if (r2 > curr_left && r2 < curr_right) {
                    map[x][y] = new DTile("Dendrite");
                }
            }
        }
    }

    public static void addWalls(TETile[][] map) {
        for (int x = 1; x < WIDTH - 1; x++) {
            for (int y = 1; y < HEIGHT - 1; y++) {
                if (map[x][y] instanceof DTile) {
                    // Check 4-neighbors (not diagonals)
                    int[] dx = {-1, 1, 0, 0};
                    int[] dy = {0, 0, -1, 1};

                    for (int i = 0; i < 4; i++) {
                        int nx = x + dx[i];
                        int ny = y + dy[i];
                        if (map[nx][ny] == Tileset.NOTHING) {
                            map[nx][ny] = Tileset.WALL;
                        }
                    }
                }
            }
        }
    }
}
