package core;

import maps.Maps;
import tileengine.TERenderer;
import tileengine.TETile;

//import static maps.maps.CIRCLE_MAP;

import static maps.Maps.NEURITE_MAP;

/// This class is used for testing one single-set of variables. Use the dataCollection class for large scale data collection.
/// Graphic rendering is enabled in this class for demonstration purposes
/// The variables and constants used in the simulation are:
/// world: An object that contains every tile. All changes and calculations of the simulation happens within the world.
/// WIDTH: The width of world in tiles
/// HEIGHT: The height of world in tiles
/// totalKinases: The total number of kinase of the system. This is a constant.
/// totalPhosphatases: The total number of phosphatase of the system. This is a constant..
///

public class Main {
    private static World world;
//    private static final TETile[][] MAP = CIRCLE_MAP;
    private static final TETile[][] MAP = NEURITE_MAP;
    private static final int WIDTH = Maps.WIDTH;
    private static final int HEIGHT = Maps.HEIGHT;
//    private static final int RADIUS = (WIDTH - 1) / 2;

    // These should be calculated properly, not set arbitrarily
//    private static final double TIMESTEP = 0.01;
//    private static final double PATCH_LENGTH = 0.5;  // micrometers
    // Patch length impacts:
    // 1. Size of the system
    // 2. Patch area
    // 3. Enzyme density per patch
    // 4. Binding probability and enzyme contribution using patch area and enzyme density
//    private static final double D_PIP = 5;  // diffusion coefficient
//    private static final double ALPHA_PIP = D_PIP * TIMESTEP / (PATCH_LENGTH * PATCH_LENGTH); // PIP diffusion factor
//    private static final double ALPHA_ENZYME = 0.2; // Enzyme diffusion factor

    // Frame rate control
    private static final int TARGET_FPS = 120;
    private static final long FRAME_TIME_MS = 1000 / TARGET_FPS;

    public static void main(String[] args) {
        int totalKinases = 50;
        int totalPptases = 50;
        // World parameters
        double timestep = 0.01;      // affects rate of every process
        double patchLength = 0.5;    // higher = more binding, less conversion/diffusion/motion
        double dPip = 2.0;           // diffusion
        double alphaEnzyme = 0.2;    // stochastic motion constant, higher = more motion
        // Enzyme parameters (reference * adjustment)
        double k_mkon = 0.01 * 10;   // kinase on-rate
        double p_mkon = 0.0005 * 10; // phosphatase on-rate
        double k_koff = 4.0 / 10;    // kinase off-rate
        double p_koff = 0.2 / 10;    // phosphatase off-rate
        double k_mkcat = 0.1 * 20;   // kinase catalytic rate
        double p_mkcat = 0.4 * 20;   // phosphatase catalytic rate
        double k_mKm = 0.1;          // kinase Michaelis constant
        double p_mKm = 0.4;          // phosphatase Michaelis constant

        runGame(totalKinases, totalPptases, timestep, patchLength, dPip, alphaEnzyme, k_mkon, k_koff, p_mkon, p_koff, k_mkcat, k_mKm, p_mkcat, p_mKm);
    }

    private static void runGame(int initialKinases, int initialPptases, double timestep, double patchLength,
                                double dPIP, double alphaEnzyme, double k_mkon, double k_koff, double p_mkon,
                                double p_koff, double k_mkcat, double k_mKm, double p_mkcat, double p_mKm){
        double alphaPIP = dPIP * timestep / (patchLength * patchLength);

        System.out.println("Initializing stochastic enzyme simulation...");

        if (alphaPIP >= 0.25) {
            System.err.println("ERROR: Diffusion will be unstable!");
            System.err.println("Reduce TIMESTEP or increase PATCH_LENGTH");
            return;
        }

        world = new World(MAP, WIDTH, HEIGHT, alphaPIP, alphaEnzyme, timestep, patchLength, k_mkon, k_koff, p_mkon, p_koff, k_mkcat, k_mKm, p_mkcat, p_mKm);
        world.initializeEnzymes(initialKinases, initialPptases);

        System.out.println("Initial kinases in solution: " + initialKinases);
        System.out.println("Initial phosphatases in solution: " + initialPptases);
        System.out.println("Starting simulation...");

        TERenderer renderer = new TERenderer();
        renderer.initialize(WIDTH, HEIGHT);

        long frameCount = 0;
        long startTime = System.currentTimeMillis();
        long lastRefresh = System.currentTimeMillis();
        long timeSinceRefresh = 0;

        while (frameCount <= 30000){
//            System.out.println(timeSinceRefresh);
            timeSinceRefresh = System.currentTimeMillis() - lastRefresh;

            world.updateWorld();
//            renderer.renderFrame(world.worldGrid);
            if (frameCount % 10 == 0) {
                renderer.renderFrame(world.worldGrid);
                lastRefresh = System.currentTimeMillis();
            }

            frameCount++;

            // Print status every 100 frames
            if (frameCount % 100 == 0) {
                long elapsed = System.currentTimeMillis() - startTime;
                double fps = frameCount * 1000.0 / elapsed;
                System.out.printf("Frame %d | FPS: %.1f | Kinases(sol/total): %d/%d | Pptases(sol/total): %d/%d" +
                                "%nSystemAvgX: %.2f | Dendrite 1 AvgX: %.2f | All Dendrites AvgX: %.2f | Polarization indices: %.2f, %.2f, %.2f, %.2f, %.2f\n",
                        frameCount, fps,
                        world.getKinasesInSolution(), world.getTotalKinases(), world.getPptasesInSolution(), world.getTotalPptases(),
                        world.getAvgBodyX(), world.getAvgOneNodeX(1), world.getAvgAllNodeX(), world.polarizationIdx()[0],
                        world.polarizationIdx()[1], world.polarizationIdx()[2], world.polarizationIdx()[3], world.polarizationIdx()[4]);
            }

            // Frame rate limiting
//            long frameTime = System.currentTimeMillis() - frameStart;
//            if (frameTime < FRAME_TIME_MS) {
//                try {
//                    Thread.sleep(FRAME_TIME_MS - frameTime);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
        }
        System.out.println("Completed simulation!");
    }

//    private static boolean terminate(double frameCount){
//        if  (frameCount >= 2000){
//            return true;
//        }
//        return world.polarizationIdx() >= 0.8;
//    }
}