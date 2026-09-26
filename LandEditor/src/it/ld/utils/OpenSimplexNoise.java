/* Copyright (c) 2026 Daniele Lombardi / Daniels118
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.ld.utils;

public class OpenSimplexNoise {
    private static final long PRIME_X = 0x5205402B9270C86FL;
    private static final long PRIME_Y = 0x598CD327003817B5L;
    
    private final long seed;
    
    public OpenSimplexNoise(long seed) {
        this.seed = seed;
    }
    
    public float eval(float x, float y) {
        int x0 = fastFloor(x);
        int y0 = fastFloor(y);
        
        float xf = x - x0;
        float yf = y - y0;
        
        float n00 = gradient(x0, y0, xf, yf);
        float n10 = gradient(x0 + 1, y0, xf - 1, yf);
        float n01 = gradient(x0, y0 + 1, xf, yf - 1);
        float n11 = gradient(x0 + 1, y0 + 1, xf - 1, yf - 1);
        
        float u = fade(xf);
        float v = fade(yf);
        
        float x1 = lerp(n00, n10, u);
        float x2 = lerp(n01, n11, u);
        
        return lerp(x1, x2, v);
    }
    
    private float gradient(int x, int y, float dx, float dy) {
        long hash = seed;
        hash ^= PRIME_X * x;
        hash ^= PRIME_Y * y;
        hash *= 0x27d4eb2dL;
        int h = (int)(hash & 7);
        float gx, gy;
        switch (h) {
            case 0: { gx = 1; gy = 1; break;}
            case 1: { gx = -1; gy = 1; break;}
            case 2: { gx = 1; gy = -1; break;}
            case 3: { gx = -1; gy = -1; break;}
            case 4: { gx = 1; gy = 0; break;}
            case 5: { gx = -1; gy = 0; break;}
            case 6: { gx = 0; gy = 1; break;}
            default: { gx = 0; gy = -1; break;}
        }
        return gx * dx + gy * dy;
    }
    
    private float fade(float t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }
    
    private float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }
    
    private int fastFloor(float x) {
        int xi = (int)x;
        return x < xi ? xi - 1 : xi;
    }
}
