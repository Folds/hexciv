package net.folds.hexciv;

import java.awt.*;

/**
 * Created by jasper on Feb 14, 2014.
 */
public class Loupe {
    WorldMap map;
    int hexWidthInPixels;
    int hexSideInPixels;
    Rectangle margins;

    Loupe(WorldMap map, int hexWidthInPixels, int hexSideInPixels, Rectangle margins) {
        this.map = map;
        this.hexWidthInPixels = hexWidthInPixels;
        this.hexSideInPixels = hexSideInPixels;
        this.margins = margins;
    }

    protected int getLeftMargin() {
        return margins.x;
    }

    protected int getTopMargin() {
        return margins.y;
    }

    protected void setMargins(Rectangle margins) {
        this.margins.x = margins.x;
        this.margins.y = margins.y;
        this.margins.width = margins.width;
        this.margins.height = margins.height;
    }

    protected void setMap(WorldMap map) {
        this.map = map;
    }

    public Directions getCellOffset(double phaseX, double phaseY) {
        int hexSideInPixels = this.hexSideInPixels;
        int hexWidthInPixels = this.hexWidthInPixels;
        if (phaseY < hexSideInPixels / 2) {
            if (phaseX == 0) {
                return Directions.northwest;
            }
            if (phaseX < hexWidthInPixels / 2 - 1) {
                double fromNW =   2 * phaseX * hexSideInPixels + hexSideInPixels
                        + 2 * phaseY * hexWidthInPixels + hexWidthInPixels;
                long NWtoSE = 2 * hexSideInPixels * hexWidthInPixels;
                double fromSE = NWtoSE - fromNW;
                if (fromNW < fromSE) {
                    return Directions.northwest;
                }
                if (fromSE < fromNW) {
                    return Directions.none;
                }
                if (phaseY % 2 == 0) {
                    return Directions.none;
                }
                return Directions.northwest;
            }
            if (phaseX <= hexWidthInPixels / 2) {
                return Directions.none;
            }
            if (phaseX < hexWidthInPixels - 1) {
                double fromNE =  (2 * hexWidthInPixels - 2 * phaseX - 1) * hexSideInPixels
                        + 2 * phaseY * hexWidthInPixels + 2 * hexWidthInPixels;
                long SWtoNE = 2 * hexSideInPixels * hexWidthInPixels;
                double fromSW = SWtoNE - fromNE;
                if (fromNE < fromSW) {
                    return Directions.northeast;
                }
                if (fromSW < fromNE) {
                    return Directions.none;
                }
                if (phaseY % 2 == 0) {
                    return Directions.none;
                }
                return Directions.northeast;
            }
            return Directions.northeast;
        }
        if (phaseY <= hexSideInPixels / 2 + hexSideInPixels - 1) {
            return Directions.none;
        }
        if (phaseY <= 2 * hexSideInPixels - 1) {
            if (phaseX == 0) {
                return Directions.southwest;
            }
            if (phaseX < hexWidthInPixels / 2 - 1) {
                double fromNE =  (hexWidthInPixels - 2 * phaseX - 1) * hexSideInPixels
                        + 2 * phaseY * hexWidthInPixels + 2 * hexWidthInPixels;
                long SWtoNE = 2 * hexSideInPixels * hexWidthInPixels;
                double fromSW = SWtoNE - fromNE;
                if (fromNE < fromSW) {
                    return Directions.none;
                }
                if (fromSW < fromNE) {
                    return Directions.southwest;
                }
                if ((phaseY + 1) % 2 == 0) {
                    return Directions.none;
                }
                return Directions.southwest;
            }
            if (phaseX <= hexWidthInPixels / 2) {
                return Directions.none;
            }
            if (phaseX < hexWidthInPixels - 1) {
                double fromNW =  (2 * phaseX - hexWidthInPixels + 1) * hexSideInPixels
                        + 2 * phaseY * hexWidthInPixels + hexWidthInPixels;
                long NWtoSE = 2 * hexSideInPixels * hexWidthInPixels;
                double fromSE = NWtoSE - fromNW;
                if (fromNW < fromSE) {
                    return Directions.southeast;
                }
                if (fromSE < fromNW) {
                    return Directions.none;
                }
                if ((phaseY + 1) % 2 == 0) {
                    return Directions.none;
                }
                return Directions.southeast;
            }
            return Directions.southeast;
        }
        if (phaseX <= hexWidthInPixels / 2 - 1) {
            return Directions.southwest;
        }
        return Directions.southeast;
    }
}