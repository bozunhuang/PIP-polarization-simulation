package tileengine;

import java.awt.*;

public class DTile extends TETile {
    public int kinaseCount = 0;
    public int pptaseCount = 0;
    public double X = 0.5;  // Fraction of PIP2 (0 = all PIP1, 1 = all PIP2)

    // Binding probabilities (patch-owned variables, like in NetLogo)
    public double k_Pon = 0.0;  // Probability of kinase binding to this patch
    public double p_Pon = 0.0;  // Probability of phosphatase binding to this patch

    public static final char defaultChar = ' ';

    public int tracker = 0;

    public DTile() {
        super(defaultChar, Color.black, Color.blue, 0);
        upDateTile();
    }

    /**
     * Creates a copy of DTile t.
     * @param t tile to copy
     */
    public DTile(DTile t) {
        super(t);
        kinaseCount = t.kinaseCount;
        pptaseCount = t.pptaseCount;
        X = t.X;
        tracker = t.tracker;
        upDateTile();
    }

    public DTile(int tracker) {
        super('#', new Color(((tracker == 1) ? 255 : 0),((tracker == 2) ? 255 : 0),((tracker == 3) ? 255 : 0)), Color.blue, 0);
        this.tracker = tracker;
        upDateTile();
    }


    /**
     * Updates the tile's visual appearance based on X value.
     * Uses same color scheme as NetLogo:
     * - Blue (PIP1) when X = 0
     * - Orange/Yellow (PIP2) when X = 1
     */
    public void upDateTile() {
        assert X >= 0 && X <= 1 : "X out of bounds: " + X;

        // NetLogo color scheme:
        // RGB-pip1 = [0, 100, 255] (blue)
        // RGB-pip2 = [255, 200, 0] (orange)
        // Mix them based on X

        int[] pip1_rgb = {0, 100, 255};
        int[] pip2_rgb = {255, 200, 0};

        int r = (int) (X * pip2_rgb[0] + (1 - X) * pip1_rgb[0]);
        int g = (int) (X * pip2_rgb[1] + (1 - X) * pip1_rgb[1]);
        int b = (int) (X * pip2_rgb[2] + (1 - X) * pip1_rgb[2]);

        // Clamp values to valid range
        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));

        this.backgroundColor = new Color(r, g, b);

        // Optional: Display enzyme information as character
        // Uncomment if you want to see enzyme distribution
        /*
        if (kinaseCount > 0 && pptaseCount > 0) {
            character = 'B';  // Both
        } else if (kinaseCount > 0) {
            character = 'K';
        } else if (pptaseCount > 0) {
            character = 'P';
        } else {
            character = ' ';
        }
        */
    }

    /**
     * Get total enzyme count at this tile
     */
    public int getTotalEnzymeCount() {
        return kinaseCount + pptaseCount;
    }
}