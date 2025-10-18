package core;

import tileengine.TERenderer;
import tileengine.TETile;

import static maps.maps.CIRCLE_MAP;
import static maps.maps.NEURITE_MAP;

public class Main {
    private static World world;
//    private static final TETile[][] MAP = CIRCLE_MAP;
    private static final TETile[][] MAP = NEURITE_MAP;
    private static final int WIDTH = 101;
    private static final int HEIGHT = 101;
    private static final int RADIUS = (WIDTH - 1) / 2;

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
        int totalKinases = 500;
        int totalPhosphatases = 1000;
        double timestep = 0.01;
        double patchLength = 0.5;
        double dPip = 5;
        double alphaEnzyme = 0.2;
        double k_mkon = 0.3;      // kinase on-rate
        double k_koff = 0.5;      // kinase off-rate
        double p_mkon = 0.05;      // phosphatase on-rate
        double p_koff = 0.2;      // phosphatase off-rate
        double k_mkcat = 10.0;     // kinase catalytic rate
        double k_mKm = 2.0;       // kinase Michaelis constant
        double p_mkcat = 15;     // phosphatase catalytic rate
        double p_mKm = 0.5;

        runGame(totalKinases, totalPhosphatases, timestep, patchLength, dPip, alphaEnzyme, k_mkon, k_koff, p_mkon, p_koff, k_mkcat, k_mKm, p_mkcat, p_mKm);
    }

    private static void runGame(int initialKinases, int initialPhosphatases, double timestep, double patchLength, double dPIP, double alphaEnzyme, double k_mkon, double k_koff, double p_mkon, double p_koff, double k_mkcat, double k_mKm, double p_mkcat, double p_mKm){
        double alphaPIP = dPIP * timestep / (patchLength * patchLength);

        System.out.println("Initializing stochastic enzyme simulation...");
        System.out.println("ALPHA_PIP = " + alphaPIP);

        if (alphaPIP >= 0.25) {
            System.err.println("ERROR: Diffusion will be unstable!");
            System.err.println("Reduce TIMESTEP or increase PATCH_LENGTH");
            return;
        }

        world = new World(MAP, WIDTH, HEIGHT, RADIUS, alphaPIP, alphaEnzyme, timestep, patchLength, k_mkon, k_koff, p_mkon, p_koff, k_mkcat, k_mKm, p_mkcat, p_mKm);
        world.initializeEnzymes(initialKinases, initialPhosphatases);

        System.out.println("Initial kinases in solution: " + initialKinases);
        System.out.println("Initial phosphatases in solution: " + initialPhosphatases);
        System.out.println("Starting simulation...");

        TERenderer renderer = new TERenderer();
        renderer.initialize(WIDTH, HEIGHT);

        long frameCount = 0;
        long startTime = System.currentTimeMillis();

        while (frameCount < 2000){
            long frameStart = System.currentTimeMillis();

            world.upDateWorld();
//            renderer.renderFrame(world.worldGrid);

            frameCount++;

            // Print status every 100 frames
            if (frameCount % 100 == 0) {
                renderer.renderFrame(world.worldGrid);
                long elapsed = System.currentTimeMillis() - startTime;
                double fps = frameCount * 1000.0 / elapsed;
                System.out.printf("Frame %d | FPS: %.1f | Kinases(sol/total): %d/%d | Pptases(sol/total): %d/%d%n",
                        frameCount, fps,
                        world.getKinasesInSolution(), world.getTotalKinases(),
                        world.getPhosphatasesInSolution(), world.getTotalPhosphatases());
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
    }
}