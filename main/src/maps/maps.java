package maps;

import tileengine.DTile;
import tileengine.TERenderer;
import tileengine.TETile;
import tileengine.Tileset;

public class maps {
    public static final int WIDTH = 101;
    public static final int HEIGHT = 101;
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
        int r2 = 6;
        int center = WIDTH / 2;
        int r1 = RADIUS - r2;
        int smallCenter = WIDTH - r2 - 2;

        // Draws the big circle
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                long dist = Math.round(Math.sqrt(Math.pow(x - center, 2) + Math.pow(y - center, 2)));

                if (dist < r1) {
                    map[x][y] = new DTile();
                }
            }
        }

        // Draws the lobes
        for (int i = 1; i <= 4; i++) {
            // Draws a rectangular channel
            for (int y = center + r1; y > center + r1 - 4; y--) {
                for (int x = center; x < center + r1; x++) {
                    // Add regular tiles for the overlaps with the big circle
                    if (x < 1.5 * r1){
                        map[x][y] = new DTile();
                    } else {
                        // Add tracker tiles for the extended region
                        map[x][y] = new DTile(i);
                    }
                }
            }

            // Draws a small circle
            for (int x = 0; x < WIDTH; x++) {
                for (int y = 0; y < HEIGHT; y++) {
                    long dist = Math.round(Math.sqrt(Math.pow(x - smallCenter, 2) + Math.pow(y - smallCenter, 2)));

                    if (dist < r2) {
                        map[x][y] = new DTile(i);
                    }
                }
            }

            // Rotates map
            TETile[][] map2 = new TETile[WIDTH][HEIGHT];

            for (int x = 0; x < WIDTH; x++) {
                for (int y = 0; y < HEIGHT; y++) {
                    map2[x][y] = map[2 * center - y][x];
                }
            }

            for (int x = 0; x < WIDTH; x++) {
                for (int y = 0; y < HEIGHT; y++) {
                    if (map2[x][y] instanceof DTile) {
                        map[x][y] = new DTile((DTile) map2[x][y]);
                    } else {
                        map[x][y] = Tileset.NOTHING;
                    }
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
