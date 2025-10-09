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
    private static final double TIMESTEP = 0.01;
    private static final double PATCH_LENGTH = 0.5;  // micrometers
    // Patch length impacts:
    // 1. Size of the system
    // 2. Patch area
    // 3. Enzyme density per patch
    // 4. Binding probability and enzyme contribution using patch area and enzyme density
    private static final double D_PIP = 5;  // diffusion coefficient
    private static final double ALPHA_PIP = D_PIP * TIMESTEP / (PATCH_LENGTH * PATCH_LENGTH); // PIP diffusion factor
    private static final double ALPHA_ENZYME = 0.2; // Enzyme diffusion factor

    // Frame rate control
    private static final int TARGET_FPS = 120;
    private static final long FRAME_TIME_MS = 1000 / TARGET_FPS;

    public static void main(String[] args) {
        System.out.println("Initializing stochastic enzyme simulation...");
        System.out.println("ALPHA_PIP = " + ALPHA_PIP);

        if (ALPHA_PIP >= 0.25) {
            System.err.println("ERROR: Diffusion will be unstable!");
            System.err.println("Reduce TIMESTEP or increase PATCH_LENGTH");
            return;
        }

        world = new World(MAP, WIDTH, HEIGHT, RADIUS, ALPHA_PIP, ALPHA_ENZYME, TIMESTEP, PATCH_LENGTH);

        // Initialize enzymes in solution
        int initialKinases = 500;
        int initialPhosphatases = 1000;
        world.initializeEnzymes(initialKinases, initialPhosphatases);

        System.out.println("Initial kinases in solution: " + initialKinases);
        System.out.println("Initial phosphatases in solution: " + initialPhosphatases);
        System.out.println("Starting simulation...");

        runGame();
    }

    private static void runGame(){
        TERenderer renderer = new TERenderer();
        renderer.initialize(WIDTH, HEIGHT);

        long frameCount = 0;
        long startTime = System.currentTimeMillis();

        while (true){
            long frameStart = System.currentTimeMillis();

            world.upDateWorld();
            renderer.renderFrame(world.worldGrid);

            frameCount++;

            // Print status every 100 frames
            if (frameCount % 100 == 0) {
                long elapsed = System.currentTimeMillis() - startTime;
                double fps = frameCount * 1000.0 / elapsed;
                System.out.printf("Frame %d | FPS: %.1f | Kinases(sol/total): %d/%d | Pptases(sol/total): %d/%d%n",
                        frameCount, fps,
                        world.getKinasesInSolution(), world.getTotalKinases(),
                        world.getPhosphatasesInSolution(), world.getTotalPhosphatases());
            }

            // Frame rate limiting
            long frameTime = System.currentTimeMillis() - frameStart;
            if (frameTime < FRAME_TIME_MS) {
                try {
                    Thread.sleep(FRAME_TIME_MS - frameTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}