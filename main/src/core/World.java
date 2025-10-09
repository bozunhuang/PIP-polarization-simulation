package core;

import tileengine.DTile;
import tileengine.TETile;
import tileengine.Tileset;

import java.util.Random;

public class World {
    // Random seed
    private final Random random = new Random(42);

    // World data
    public TETile[][] worldGrid;

    // World parameters
    private static int WIDTH;
    private static int HEIGHT;
    private static int RADIUS;
    private static double ALPHA_PIP;
    private static double ALPHA_ENZYME;
    private static double TIMESTEP;
    private static double PATCH_LENGTH;

    // Enzyme conservation tracking
    private int totalKinases;
    private int totalPhosphatases;
    private int kinasesInSolution;
    private int phosphatasesInSolution;

    // Kinetic parameters (from NetLogo)
    private double k_mkon = 0.3;      // kinase on-rate
    private double k_koff = 0.5;      // kinase off-rate
    private double p_mkon = 0.05;      // phosphatase on-rate
    private double p_koff = 0.2;      // phosphatase off-rate
    private double k_mkcat = 10.0;     // kinase catalytic rate
    private double k_mKm = 2.0;       // kinase Michaelis constant
    private double p_mkcat = 15;     // phosphatase catalytic rate
    private double p_mKm = 0.5;       // phosphatase Michaelis constant

    // Temporary storage for diffusion (avoid in-place updates)
    private double[][] updatedX;
    private int[][] updatedKinaseCount;
    private int[][] updatedPhosphataseCount;

    public World(TETile[][] map, int width, int height, int radius, double alphaPIP, double alphaEnzyme, double timeStep, double patchLength) {
        worldGrid = map;
        WIDTH = width;
        HEIGHT = height;
        RADIUS = radius;
        ALPHA_PIP = alphaPIP;
        ALPHA_ENZYME = alphaEnzyme;
        TIMESTEP = timeStep;
        PATCH_LENGTH = patchLength;
        initialize();
        validateStability();
    }

    private void initialize(){
        updatedX = new double[WIDTH][HEIGHT];
        updatedKinaseCount = new int[WIDTH][HEIGHT];
        updatedPhosphataseCount = new int[WIDTH][HEIGHT];

        // Initialize enzyme pools
        totalKinases = 0;
        totalPhosphatases = 0;
        kinasesInSolution = 0;
        phosphatasesInSolution = 0;
    }

    private void validateStability() {
        // FTCS stability condition: alpha < 0.25 for 2D
        if (ALPHA_PIP >= 0.25) {
            System.err.println("WARNING: Diffusion unstable! ALPHA_PIP = " + ALPHA_PIP + " >= 0.25");
            System.err.println("Reduce timestep or increase patch size.");
        }
    }

//    public void setUpFloor(){
//        int r = RADIUS;
//        for (int x = 0; x < 2 * r + 1; x++) {
//            for (int y = 0; y < 2 * r + 1; y++) {
//                long curr_right = Math.round(Math.sqrt(Math.pow(x + r, 2) + Math.pow(y + r, 2)));
//                long curr_left = Math.round(Math.sqrt(Math.pow(x - r, 2) + Math.pow(y - r, 2)));
//
//                if (r > curr_left && r < curr_right) {
//                    worldGrid[x][y] = new DTile();
//                }
//            }
//        }
//    }
//
//    public void addWalls(){
//        for (int x = 1; x < WIDTH - 1; x++){
//            for (int y = 1; y < HEIGHT - 1; y++){
//                if (worldGrid[x][y] instanceof DTile){
//                    // Check 4-neighbors (not diagonals)
//                    int[] dx = {-1, 1, 0, 0};
//                    int[] dy = {0, 0, -1, 1};
//
//                    for (int i = 0; i < 4; i++){
//                        int nx = x + dx[i];
//                        int ny = y + dy[i];
//                        if (worldGrid[nx][ny] == Tileset.NOTHING){
//                            worldGrid[nx][ny] = Tileset.FLOWER;
//                        }
//                    }
//                }
//            }
//        }
//    }

    public void initializeEnzymes(int initialKinases, int initialPhosphatases) {
        totalKinases = initialKinases;
        totalPhosphatases = initialPhosphatases;
        kinasesInSolution = initialKinases;
        phosphatasesInSolution = initialPhosphatases;
    }

    public void upDateWorld() {
        // Follow NetLogo order: unbind -> bind -> convert -> move
        unbind();
        bind();
        convert();
        move();
    }

    private void unbind() {
        // Probability of unbinding per timestep
        double k_Poff = k_koff * TIMESTEP;
        double p_Poff = p_koff * TIMESTEP;

        if (k_Poff > 1.0 || p_Poff > 1.0) {
            System.err.println("WARNING: Unbinding probability > 1! Reduce timestep.");
        }

        for (int x = 0; x < WIDTH; x++){
            for (int y = 0; y < HEIGHT; y++){
                if (worldGrid[x][y] instanceof DTile tile) {

                    // Unbind kinases
                    int kinasesToUnbind = 0;
                    for (int k = 0; k < tile.kinaseCount; k++){
                        if (random.nextDouble() < k_Poff){
                            kinasesToUnbind++;
                        }
                    }
                    tile.kinaseCount -= kinasesToUnbind;
                    kinasesInSolution += kinasesToUnbind;

                    // Unbind phosphatases
                    int phosphatasesToUnbind = 0;
                    for (int p = 0; p < tile.pptaseCount; p++){
                        if (random.nextDouble() < p_Poff){
                            phosphatasesToUnbind++;
                        }
                    }
                    tile.pptaseCount -= phosphatasesToUnbind;
                    phosphatasesInSolution += phosphatasesToUnbind;
                }
            }
        }
    }

    private void bind() {
        // Collect all membrane tiles first (like NetLogo's inpatches)
        int membraneCount = 0;
        for (int x = 0; x < WIDTH; x++){
            for (int y = 0; y < HEIGHT; y++){
                if (worldGrid[x][y] instanceof DTile) {
                    membraneCount++;
                }
            }
        }

        if (membraneCount == 0) return;

        // Process binding for each patch
        for (int x = 0; x < WIDTH; x++){
            for (int y = 0; y < HEIGHT; y++){
                if (worldGrid[x][y] instanceof DTile tile) {
                    double patchArea = PATCH_LENGTH * PATCH_LENGTH;

                    // Calculate and store k_Pon (kinase binding probability)
                    // Kinase binding depends on X (binds to PIP2)
                    if (kinasesInSolution > 0) {
                        tile.k_Pon = k_mkon * tile.X * patchArea * TIMESTEP;
                        // Adjust by available fraction (from NetLogo)
                        tile.k_Pon *= (double) kinasesInSolution / totalKinases;

                        if (tile.k_Pon > 1.0) {
                            System.err.println("WARNING: k_Pon = " + tile.k_Pon + " > 1 at (" + x + "," + y + ")");
                        }

                        // Stochastic binding event
                        if (random.nextDouble() < tile.k_Pon) {
                            tile.kinaseCount++;
                            kinasesInSolution--;
                        }
                    }

                    // Calculate and store p_Pon (phosphatase binding probability)
                    // Phosphatase binding depends on (1-X) (binds to PIP1)
                    if (phosphatasesInSolution > 0) {
                        tile.p_Pon = p_mkon * (1.0 - tile.X) * patchArea * TIMESTEP;
                        // Adjust by available fraction (from NetLogo)
                        tile.p_Pon *= (double) phosphatasesInSolution / totalPhosphatases;

                        if (tile.p_Pon > 1.0) {
                            System.err.println("WARNING: p_Pon = " + tile.p_Pon + " > 1 at (" + x + "," + y + ")");
                        }

                        // Stochastic binding event
                        if (random.nextDouble() < tile.p_Pon) {
                            tile.pptaseCount++;
                            phosphatasesInSolution--;
                        }
                    }
                }
            }
        }
    }

    private void convert() {
        for (int x = 0; x < WIDTH; x++){
            for (int y = 0; y < HEIGHT; y++){
                if (worldGrid[x][y] instanceof DTile tile) {
                    double patchArea = PATCH_LENGTH * PATCH_LENGTH;

                    // Calculate enzyme densities (enzymes per unit area)
                    double kinaseDensity = tile.kinaseCount / patchArea;
                    double phosphataseDensity = tile.pptaseCount / patchArea;

                    // Michaelis-Menten kinetics
                    // Kinase converts PIP1->PIP2 (increases X)
                    double kinaseContribution = k_mkcat * kinaseDensity * (1.0 - tile.X)
                            / (k_mKm + (1.0 - tile.X));

                    // Phosphatase converts PIP2->PIP1 (decreases X)
                    double phosphataseContribution = -p_mkcat * phosphataseDensity * tile.X
                            / (p_mKm + tile.X);

                    double dX = (kinaseContribution + phosphataseContribution) * TIMESTEP;

                    tile.X = Math.max(0.0, Math.min(1.0, tile.X + dX));
                }
            }
        }
    }

    private void move() {
        // Move enzymes first, then diffuse PIP
        moveEnzymes();
        diffusePIP();
    }

    private void moveEnzymes() {
        // Use temporary arrays to avoid conflicts
        for (int x = 0; x < WIDTH; x++){
            for (int y = 0; y < HEIGHT; y++){
                updatedKinaseCount[x][y] = 0;
                updatedPhosphataseCount[x][y] = 0;
            }
        }

        // Copy current counts
        for (int x = 0; x < WIDTH; x++){
            for (int y = 0; y < HEIGHT; y++){
                if (worldGrid[x][y] instanceof DTile tile) {
                    updatedKinaseCount[x][y] = tile.kinaseCount;
                    updatedPhosphataseCount[x][y] = tile.pptaseCount;
                }
            }
        }

        // Probability of staying (from NetLogo)
        double Pstay = 1.0 - (4.0 * ALPHA_ENZYME * TIMESTEP) / (PATCH_LENGTH * PATCH_LENGTH);

        if (Pstay < 0) {
            System.err.println("WARNING: Enzyme diffusion unstable! Pstay = " + Pstay);
            Pstay = 0;
        }

        for (int x = 0; x < WIDTH; x++){
            for (int y = 0; y < HEIGHT; y++){
                if (worldGrid[x][y] instanceof DTile tile) {

                    // Move each kinase
                    for (int k = 0; k < tile.kinaseCount; k++){
                        if (random.nextDouble() >= Pstay) {
                            // Choose random direction
                            int[] newPos = getRandomNeighbor(x, y);
                            if (newPos != null) {
                                updatedKinaseCount[x][y]--;
                                updatedKinaseCount[newPos[0]][newPos[1]]++;
                            }
                        }
                    }

                    // Move each phosphatase
                    for (int p = 0; p < tile.pptaseCount; p++){
                        if (random.nextDouble() >= Pstay) {
                            int[] newPos = getRandomNeighbor(x, y);
                            if (newPos != null) {
                                updatedPhosphataseCount[x][y]--;
                                updatedPhosphataseCount[newPos[0]][newPos[1]]++;
                            }
                        }
                    }
                }
            }
        }
        // Apply updates
        for (int x = 0; x < WIDTH; x++){
            for (int y = 0; y < HEIGHT; y++){
                if (worldGrid[x][y] instanceof DTile tile) {
                    tile.kinaseCount = updatedKinaseCount[x][y];
                    tile.pptaseCount = updatedPhosphataseCount[x][y];
                }
            }
        }
    }

    private int[] getRandomNeighbor(int x, int y) {
        // 4-connectivity only
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        // Count valid neighbors
        int validCount = 0;
        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            if (nx >= 0 && nx < WIDTH && ny >= 0 && ny < HEIGHT
                    && worldGrid[nx][ny] instanceof DTile) {
                validCount++;
            }
        }

        if (validCount == 0) return null;

        // Choose random valid neighbor
        int choice = random.nextInt(validCount);
        int count = 0;
        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            if (nx >= 0 && nx < WIDTH && ny >= 0 && ny < HEIGHT
                    && worldGrid[nx][ny] instanceof DTile) {
                if (count == choice) {
                    return new int[]{nx, ny};
                }
                count++;
            }
        }

        return null;
    }

    private void diffusePIP() {
        // FTCS scheme: x_new = x(1 - 4α·n/4) + Σ(neighbors)·α
        // Must use temporary array to avoid in-place updates

        for (int x = 0; x < WIDTH; x++){
            for (int y = 0; y < HEIGHT; y++){
                if (worldGrid[x][y] instanceof DTile tile) {

                    // Count valid neighbors and sum their X values
                    int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
                    int neighborCount = 0;
                    double neighborSum = 0;

                    for (int[] dir : directions) {
                        int nx = x + dir[0];
                        int ny = y + dir[1];
                        if (nx >= 0 && nx < WIDTH && ny >= 0 && ny < HEIGHT
                                && worldGrid[nx][ny] instanceof DTile) {
                            neighborCount++;
                            neighborSum += ((DTile) worldGrid[nx][ny]).X;
                        }
                    }

                    // FTCS update formula
                    double newX = tile.X * (1.0 - 4.0 * ALPHA_PIP * (neighborCount / 4.0))
                            + neighborSum * ALPHA_PIP;

                    updatedX[x][y] = Math.max(0.0, Math.min(1.0, newX));
                } else {
                    updatedX[x][y] = 0;
                }
            }
        }

        // Apply updates and refresh visuals
        for (int x = 0; x < WIDTH; x++){
            for (int y = 0; y < HEIGHT; y++){
                if (worldGrid[x][y] instanceof DTile tile) {
                    tile.X = updatedX[x][y];
                    tile.upDateTile();
                }
            }
        }
    }

    public int getKinasesInSolution() {return kinasesInSolution;}

    public int getTotalKinases() {return totalKinases;}

    public Object getPhosphatasesInSolution() {return phosphatasesInSolution;}

    public Object getTotalPhosphatases() {return totalPhosphatases;}
}
