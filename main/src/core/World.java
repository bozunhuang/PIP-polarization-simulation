package core;

import tileengine.DTile;
import tileengine.TETile;

import java.util.ArrayList;
import java.util.Random;

public class World {
    // Random seed
    private final Random random = new Random();

    // World data
    public TETile[][] worldGrid;

    // World parameters
    private int width;
    private int height;
    private double alphaPIP;
    private double alphaEnzyme;
    private double timestep;
    private double patchLength;

    // Enzyme conservation tracking
    private int totalKinases;
    private int totalPhosphatases;
    private int kinasesInSolution;
    private int phosphatasesInSolution;

    // Kinetic parameters (from NetLogo)
    private final double k_mkon;      // kinase on-rate
    private final double k_koff;      // kinase off-rate
    private final double p_mkon;      // phosphatase on-rate
    private final double p_koff;      // phosphatase off-rate
    private final double k_mkcat;     // kinase catalytic rate
    private final double k_mKm;       // kinase Michaelis constant
    private final double p_mkcat;     // phosphatase catalytic rate
    private final double p_mKm;       // phosphatase Michaelis constant

    // Temporary storage for diffusion (avoid in-place updates)
    private double[][] updatedX;
    private int[][] updatedKinaseCount;
    private int[][] updatedPhosphataseCount;

    public World(TETile[][] map, int width, int height, double alphaPIP, double alphaEnzyme,
                 double timestep, double patchLength, double k_mkon, double k_koff, double p_mkon, double p_koff,
                 double k_mkcat, double k_mKm, double p_mkcat, double p_mKm) {
        worldGrid = map;
        this.width = width;
        this.height = height;
        this.alphaPIP = alphaPIP;
        this.alphaEnzyme = alphaEnzyme;
        this.timestep = timestep;
        this.patchLength = patchLength;

        this.k_mkon = k_mkon;
        this.k_koff = k_koff;
        this.p_mkon = p_mkon;
        this.p_koff = p_koff;
        this.k_mkcat = k_mkcat;
        this.k_mKm = k_mKm;
        this.p_mkcat = p_mkcat;
        this.p_mKm = p_mKm;
        initialize();
        validateStability();
    }

    public World(TETile[][] map, int width, int height, double alphaPIP, ArrayList<Double> params) {
        worldGrid = map;
        this.width = width;
        this.height = height;
        this.alphaPIP = alphaPIP;
        int initialKinases = (int) Math.round(params.get(0));
        int initialPhosphatases = (int) Math.round(params.get(1));

        timestep = params.get(2);
        patchLength = params.get(3);
        alphaEnzyme = params.get(5);

        k_mkon = params.get(6);
        k_koff = params.get(7);
        p_mkon = params.get(8);
        p_koff = params.get(9);
        k_mkcat = params.get(10);
        k_mKm = params.get(11);
        p_mkcat = params.get(12);
        p_mKm = params.get(13);
        initialize();
        initializeEnzymes(initialKinases, initialPhosphatases);
        validateStability();
    }

    private void initialize(){
        updatedX = new double[width][height];
        updatedKinaseCount = new int[width][height];
        updatedPhosphataseCount = new int[width][height];

        // Initialize enzyme pools
        totalKinases = 0;
        totalPhosphatases = 0;
        kinasesInSolution = 0;
        phosphatasesInSolution = 0;
    }

    private void validateStability() {
        // FTCS stability condition: alpha < 0.25 for 2D
        if (alphaPIP >= 0.25) {
            System.err.println("WARNING: Diffusion unstable! ALPHA_PIP = " + alphaPIP + " >= 0.25");
            System.err.println("Reduce timestep or increase patch size.");
        }
    }

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
        double k_Poff = k_koff * timestep;
        double p_Poff = p_koff * timestep;

        if (k_Poff > 1.0 || p_Poff > 1.0) {
            System.err.println("WARNING: Unbinding probability > 1! Reduce timestep.");
        }

        for (int x = 0; x < width; x++){
            for (int y = 0; y < height; y++){
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
        for (int x = 0; x < width; x++){
            for (int y = 0; y < height; y++){
                if (worldGrid[x][y] instanceof DTile) {
                    membraneCount++;
                }
            }
        }

        if (membraneCount == 0) return;

        // Process binding for each patch
        for (int x = 0; x < width; x++){
            for (int y = 0; y < height; y++){
                if (worldGrid[x][y] instanceof DTile tile) {
                    double patchArea = patchLength * patchLength;

                    // Calculate and store k_Pon (kinase binding probability)
                    // Kinase binding depends on X (binds to PIP2)
                    if (kinasesInSolution > 0) {
                        tile.k_Pon = k_mkon * tile.X * patchArea * timestep;
                        // Adjust by available fraction (from NetLogo)
                        tile.k_Pon *= (double) kinasesInSolution / totalKinases;

                        if (tile.k_Pon > 1.0) {
                            System.err.println("WARNING: k_Pon = " + tile.k_Pon + " > 1 at (" + x + "," + y + ")" + " " + k_mkon + " " +
                                    tile.X + " " + patchArea + " " + timestep + " " + kinasesInSolution + " " + totalKinases);
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
                        tile.p_Pon = p_mkon * (1.0 - tile.X) * patchArea * timestep;
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
        for (int x = 0; x < width; x++){
            for (int y = 0; y < height; y++){
                if (worldGrid[x][y] instanceof DTile tile) {
                    double patchArea = patchLength * patchLength;

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

                    double dX = (kinaseContribution + phosphataseContribution) * timestep;

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
        for (int x = 0; x < width; x++){
            for (int y = 0; y < height; y++){
                if (worldGrid[x][y] instanceof DTile tile) {
                    updatedKinaseCount[x][y] = tile.kinaseCount;
                    updatedPhosphataseCount[x][y] = tile.pptaseCount;
                } else {
                    updatedKinaseCount[x][y] = 0;
                    updatedPhosphataseCount[x][y] = 0;
                }
            }
        }

        // Probability of staying (from NetLogo)
        double Pstay = 1.0 - (4.0 * alphaEnzyme * timestep) / (patchLength * patchLength);

        if (Pstay < 0) {
            System.err.println("WARNING: Enzyme diffusion unstable! Pstay = " + Pstay);
            Pstay = 0;
        }

        for (int x = 0; x < width; x++){
            for (int y = 0; y < height; y++){
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
        for (int x = 0; x < width; x++){
            for (int y = 0; y < height; y++){
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
        int[][] validDirections = {{0, 0}, {0, 0}, {0, 0}, {0, 0}};

        // Count valid neighbors
        int validCount = 0;
        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            if (nx >= 0 && nx < width && ny >= 0 && ny < height
                    && worldGrid[nx][ny] instanceof DTile) {
                validDirections[validCount] = dir;
                validCount++;
            }
        }

        if (validCount == 0) return null;

        // Choose random valid neighbor
        int[] choice = validDirections[random.nextInt(validCount)];
        int nx = x + choice[0];
        int ny = y + choice[1];
        return new int[]{nx, ny};
    }

    private void diffusePIP() {
        // FTCS scheme: x_new = x(1 - 4α·n/4) + Σ(neighbors)·α
        // Must use temporary array to avoid in-place updates
        for (int x = 0; x < width; x++){
            for (int y = 0; y < height; y++){
                if (worldGrid[x][y] instanceof DTile tile) {
                    // Count valid neighbors and sum their X values
                    int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
                    int neighborCount = 0;
                    double neighborSum = 0;

                    for (int[] dir : directions) {
                        int nx = x + dir[0];
                        int ny = y + dir[1];
                        if (nx >= 0 && nx < width && ny >= 0 && ny < height
                                && worldGrid[nx][ny] instanceof DTile neighbor) {
                            neighborCount++;
                            neighborSum += neighbor.X;
                        }
                    }

                    // FTCS update formula
                    double newX = tile.X * (1.0 - 4.0 * alphaPIP * (neighborCount / 4.0))
                            + neighborSum * alphaPIP;
                    updatedX[x][y] = Math.max(0.0, Math.min(1.0, newX));
                } else {
                    updatedX[x][y] = 0;
                }
            }
        }

        // Apply updates and refresh visuals
        for (int x = 0; x < width; x++){
            for (int y = 0; y < height; y++){
                if (worldGrid[x][y] instanceof DTile tile) {
                    tile.X = updatedX[x][y];
                    tile.upDateTile();
                }
            }
        }
    }

    public int getKinasesInSolution() {return kinasesInSolution;}

    public int getTotalKinases() {return totalKinases;}

    public int getPhosphatasesInSolution() {return phosphatasesInSolution;}

    public int getTotalPhosphatases() {return totalPhosphatases;}

    public double getAvgBodyX() {
        double totalSystemX = 0;
        int tileCount = 0;
        for (int x = 0; x < width; x++){
            for (int y = 0; y < height; y++){
                if (worldGrid[x][y] instanceof DTile && ((DTile) worldGrid[x][y]).tracker == 0) {
                    totalSystemX += ((DTile) worldGrid[x][y]).X;
                    tileCount++;
                }
            }
        }
        return totalSystemX / tileCount;
    }

    public double getAvgOneNodeX(int i) {
        double totalDendriteX = 0;
        int tileCount = 0;
        for (int x = 0; x < width; x++){
            for (int y = 0; y < height; y++){
                if (worldGrid[x][y] instanceof DTile && ((DTile) worldGrid[x][y]).tracker == i) {
                    totalDendriteX += ((DTile) worldGrid[x][y]).X;
                    tileCount++;
                }
            }
        }
        return totalDendriteX / tileCount;
    }

    public double getAvgAllNodeX() {
        double totalDendriteX = 0;
        int tileCount = 0;
        for (int x = 0; x < width; x++){
            for (int y = 0; y < height; y++){
                if (worldGrid[x][y] instanceof DTile && ((DTile) worldGrid[x][y]).tracker > 0) {
                    totalDendriteX += ((DTile) worldGrid[x][y]).X;
                    tileCount++;
                }
            }
        }
        return totalDendriteX / tileCount;
    }

    public double[] polarizationIdx() {
        double[] nodes = new double[5];
        for (int i = 1; i <= 4; i++){
            if (getAvgOneNodeX(i) < 0.00000001 || getAvgBodyX() < 0.00000001) {
                nodes[i] = 0.0;
            } else {
                nodes[i] = getAvgOneNodeX(i) / getAvgBodyX();
            }
        }
        nodes[0] = getAvgAllNodeX() / getAvgBodyX();
        return nodes;
    }
}
