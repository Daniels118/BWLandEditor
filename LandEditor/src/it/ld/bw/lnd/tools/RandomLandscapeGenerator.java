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
package it.ld.bw.lnd.tools;

import java.util.*;

import it.ld.bw.lnd.model.LH3DLandBlock;
import it.ld.bw.lnd.model.LH3DLandCell;
import it.ld.bw.lnd.model.LH3DLandCell.Sound;
import it.ld.bw.lnd.model.LNDCountry;
import it.ld.bw.lnd.model.LndFile;
import it.ld.bw.lnd.model.Vec3f;
import it.ld.utils.ControlledList;
import it.ld.utils.ControlledList.ListControllerAdapter;
import it.ld.utils.Listeners;
import it.ld.utils.OpenSimplexNoise;
import it.ld.utils.UChangeListener;
import it.ld.utils.UChangeListener.EventType;

import static it.ld.utils.MathUtils.PI2;
import static it.ld.utils.MathUtils.clamp;

public class RandomLandscapeGenerator {
    private Random random;
    private OpenSimplexNoise noise;
    
    private float seedOffsetX;
    private float seedOffsetY;
    private float islandRotation;
    private float islandAspectX;
    private float islandAspectY;
    private float islandThreshold;
    private float warpAmplitude;
    
    private float biomeOffsetX;
    private float biomeOffsetY;
    private float biomeRotation;
    private float biomeWarpAmplitude;
    private float biomeAspectX;
    private float biomeAspectY;
    
    private void init(Params p) {
    	this.random = new Random(p.seed);
        this.noise = new OpenSimplexNoise(p.seed);
        
        Random r = new Random(p.seed ^ 0x9E3779B97F4A7C15L);

        this.seedOffsetX = r.nextFloat() * 100000;
        this.seedOffsetY = r.nextFloat() * 100000;
        this.islandRotation = r.nextFloat() * PI2;
        this.islandAspectX = 0.55f + r.nextFloat() * 0.9f;
        this.islandAspectY = 0.55f + r.nextFloat() * 0.9f;
        this.islandThreshold = 0.25f + r.nextFloat() * 0.18f;
        this.warpAmplitude = 18 + r.nextFloat() * 62;
        
        this.biomeOffsetX = r.nextFloat() * 100000;
        this.biomeOffsetY = r.nextFloat() * 100000;
        this.biomeRotation = r.nextFloat() * PI2;
        this.biomeWarpAmplitude = 20 + r.nextFloat() * 80;
        this.biomeAspectX = 0.65f + r.nextFloat() * 0.7f;
        this.biomeAspectY = 0.65f + r.nextFloat() * 0.7f;
    }
    
    public LndFile generateLand(LndFile refLand, Params p) {
    	init(p);
    	Result r = generate(p);
    	byte[][] hmap = r.heightMap;
    	byte[][] cmap = r.countryMap;
    	byte[][] smap = r.soundMap;
    	final int xCells = hmap.length;
    	final int zCells = hmap[0].length;
    	LndFile dstLand = refLand.clone();
    	dstLand.getLandBlocksForRead().clear();
    	for (int bx = 0; bx < dstLand.getBlocksPerSide(); bx++) {
    		final int basex = bx * LH3DLandBlock.SEG_PER_SIDE;
    		for (int bz = 0; bz < dstLand.getBlocksPerSide(); bz++) {
    			final int basez = bz * LH3DLandBlock.SEG_PER_SIDE;
    			LH3DLandBlock block = dstLand.getBlock(bx, bz);
    			for (int cx = 0; cx < LH3DLandBlock.POINTS_PER_SIDE; cx++) {
    				final int ax = basex + cx;
    				if (ax >= p.width) break;
    				for (int cz = 0; cz < LH3DLandBlock.POINTS_PER_SIDE; cz++) {
    					final int az = basez + cz;
    					if (az >= p.height) break;
		    			int h00 = hmap[ax][az] & 0xFF;
		    			int sound = smap[ax][az];
    					if (h00 != 0 || (sound != 0 && sound != Sound.OCEAN.code)) {
		    				if (block == null) {
								block = dstLand.addBlockAt(bx, bz);
							}
		    				LH3DLandCell cell = block.getCell(cx, cz);
		    				cell.setAltitude(h00);
		    				cell.setWater(h00 <= 1);
		    				cell.setCountry(cmap[ax][az]);
		    				cell.setSound(sound);
		    				
							int h10 = (ax + 1 < xCells) ? hmap[ax + 1][az] : 0;
							int h01 = (az + 1 < zCells) ? hmap[ax][az + 1] : 0;
							int h11 = (ax + 1 < xCells && az + 1 < zCells) ? hmap[ax + 1][az + 1] : 0;
							int dh0 = Math.abs(h11 - h00);
							int dh1 = Math.abs(h01 - h10);
							cell.setSplit(dh0 > dh1);
		    			}
    				}
    			}
        	}
    	}
    	return dstLand;
    }
    
    private Result generate(Params p) {
    	byte[][] soundMap = new byte[p.width][p.height];
    	byte[][] lakeMap = new byte[p.width][p.height];
    	byte[][] land = generateIslandMask(p);
        addSmallIslands(land, p);
        addLakes(land, p, soundMap, lakeMap);
        
        int[][] distToLand = distanceToValue(land, (byte)1);
        int[][] distToWater = distanceToValue(land, (byte)0);
        int[][] distToLake = distanceToValue(lakeMap, (byte)1);
        
        byte[][] heightMap = new byte[p.width][p.height];
        byte[][] countryMap = new byte[p.width][p.height];
        
        for (int x = 0; x < p.width; x++) {
            for (int y = 0; y < p.height; y++) {
                int h;
                if (land[x][y] != 0) {
                	int dWater = distToWater[x][y];
                	
                	if (distToLake[x][y] <= 2) {
        				soundMap[x][y] = (byte)Sound.SLOW_WAVES.code;
                	} else if (distToLake[x][y] > 3) {
                		if (dWater <= 3) {
                			if (distToLake[x][y] <= 3) {
                				soundMap[x][y] = (byte)Sound.SLOW_WAVES.code;
                			} else {
                				soundMap[x][y] = (byte)Sound.COAST.code;
                			}
                    	}
        			}
                	if (dWater == 1) {
                        h = 3;
                        heightMap[x][y] = (byte) h;
                        if (distToLake[x][y] > 1 && p.coastlineCountry != null) {
                        	countryMap[x][y] = (byte)p.coastlineCountry.getIndex();
                        }
                    } else {
                        h = computeLandAltitude(x, y, dWater, p);
                        heightMap[x][y] = (byte) clamp(h, 4, 255);
                    }
                } else {
                	int dist2land = distToLand[x][y];
                    if (dist2land >= 3) {
                    	h = 0;
                    } else if (dist2land == 2) {
                    	h = 1;
                    } else {
                    	h = 2;
                    }
                    heightMap[x][y] = (byte) h;
                    if (distToLake[x][y] > 3) {
	                    if (soundMap[x][y] == 0) {
	                    	if (dist2land < 3) {
	                    		soundMap[x][y] = (byte)Sound.COAST.code;
	                    	} else if (dist2land == 3) {
	                    		soundMap[x][y] = (byte)Sound.SPLASH.code;
	                    	} else {
	                    		soundMap[x][y] = (byte)Sound.OCEAN.code;
	                    	}
	                    }
                    } else if (dist2land >= 1 && dist2land <= 3) {
                    	soundMap[x][y] = (byte)Sound.SLOW_WAVES.code;
                    }
                }
            }
        }
        
        for (int x = 0; x < p.width; x++) {
            for (int y = 0; y < p.height; y++) {
                if (land[x][y] != 0) {
                	int dWater = distToWater[x][y];
                    if (dWater > 1) {
                        chooseCountry(x, y, heightMap, p, countryMap, soundMap, distToWater, distToLake);
                    }
                }
            }
        }
        
        return new Result(heightMap, countryMap, soundMap, lakeMap);
    }
    
    private byte[][] generateIslandMask(Params p) {
    	byte[][] land = new byte[p.width][p.height];
        
        final float halfw = p.width * 0.5f;
        final float halfh = p.height * 0.5f;
        
        float baseRadius = Math.min(p.width, p.height) * 0.5f * p.islandRadius;
        
        float cos = (float)Math.cos(islandRotation);
        float sin = (float)Math.sin(islandRotation);
        
        for (int x = 0; x < p.width; x++) {
            for (int y = 0; y < p.height; y++) {
                float warpX = fractalNoise(x + seedOffsetX, y + seedOffsetY,
                        3,
                        0.5f,
                        2f,
                        0.008f
                ) * warpAmplitude;
                
                float warpY = fractalNoise(x - seedOffsetY, y + seedOffsetX,
                        3,
                        0.5f,
                        2f,
                        0.008f
                ) * warpAmplitude;
                
                float px = x + warpX;
                float py = y + warpY;
                
                float dx = (px - halfw) / baseRadius;
                float dy = (py - halfh) / baseRadius;
                
                //Seed-based rotation
                float rx = dx * cos - dy * sin;
                float ry = dx * sin + dy * cos;
                
                //Seed-based elliptic shape
                rx *= islandAspectX;
                ry *= islandAspectY;
                
                float dist = (float)Math.pow(
                        Math.pow(Math.abs(rx), 2.2f) +
                        Math.pow(Math.abs(ry), 2.2f),
                        1f / 2.2f
                );
                
                float continent = fractalNoise(px + seedOffsetX, py + seedOffsetY,
                        p.coastNoiseOctaves,
                        0.5f,
                        2f,
                        p.coastNoiseFrequency
                );
                
                continent = continent * 0.5f + 0.5f;
                
                float ridge = ridgedNoise(px - seedOffsetY, py + seedOffsetX,
                        4,
                        0.5f,
                        2f,
                        0.018f
                );
                
                float directional = fractalNoise(px + seedOffsetX * 0.37f, py + seedOffsetY * 0.41f,
                        2,
                        0.5f,
                        2f,
                        0.004f
                );
                
                float shape =
                        (1f - dist)
                        + continent * 0.42f
                        + ridge * 0.18f
                        + directional * 0.22f;

                land[x][y] = (byte)(shape > islandThreshold ? 1 : 0);
            }
        }
        
        smoothMask(land, 2);
        return land;
    }
    
    private void smoothMask(byte[][] mask, int iterations) {
        final int w = mask.length;
        final int h = mask[0].length;
        
        for (int it = 0; it < iterations; it++) {
            byte[][] temp = new byte[w][h];
            for (int x = 1; x < w - 1; x++) {
                for (int y = 1; y < h - 1; y++) {
                    int count = 0;
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            if (mask[x + dx][y + dy] != 0) {
                                count++;
                            }
                        }
                    }
                    temp[x][y] = (byte)(count >= 5 ? 1 : 0);
                }
            }
            for (int x = 1; x < w - 1; x++) {
                System.arraycopy(temp[x], 1, mask[x], 1, h - 2);
            }
        }
    }
    
    private float ridgedNoise(float x, float y, int octaves, float persistence, float lacunarity, float frequency) {
        float amplitude = 1f;
        float sum = 0;
        float max = 0;
        for (int i = 0; i < octaves; i++) {
            float n = noise.eval(x * frequency, y * frequency);
            n = 1f - Math.abs(n);
            n *= n;
            sum += n * amplitude;
            max += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }
        return sum / max;
    }
    
    private void addSmallIslands(byte[][] land, Params p) {
    	int minRadius = 4;
    	int maxRadius = 16;
    	int minWH2 = Math.min(p.width, p.height) / 2;
    	float baseRadius = minWH2 * p.islandRadius;
    	for (int i = 0; i < p.smallIslandCount; i++) {
    		float radialPosAngle = random.nextFloat() * PI2;
    		float radialPosDist = baseRadius + maxRadius + random.nextFloat() * (minWH2 - baseRadius - maxRadius);
    		
    		int cx = (int)(p.width / 2 + Math.cos(radialPosAngle) * radialPosDist);
    		int cy = (int)(p.height / 2 + Math.sin(radialPosAngle) * radialPosDist);
    		
        	//int cx = maxRadius + random.nextInt(p.width - maxRadius * 2);
            //int cy = maxRadius + random.nextInt(p.height - maxRadius * 2);
            
            if (land[cx][cy] != 0) continue;
            
            int rx = minRadius + random.nextInt(maxRadius - minRadius);
            int ry = minRadius + random.nextInt(maxRadius - minRadius);
            
            float rotation = random.nextFloat() * PI2;
            float cos = (float)Math.cos(rotation);
            float sin = (float)Math.sin(rotation);

            float lakeOffsetX = random.nextFloat() * 100000;
            float lakeOffsetY = random.nextFloat() * 100000;

            float warpAmp = Math.max(rx, ry) * 0.45f;

            int margin = (int)Math.ceil(Math.max(rx, ry) * 2.2f);

            for (int x = Math.max(1, cx - margin); x < Math.min(p.width - 1, cx + margin); x++) {
                for (int y = Math.max(1, cy - margin); y < Math.min(p.height - 1, cy + margin); y++) {

                    float wx = fractalNoise(x + lakeOffsetX, y + lakeOffsetY,
                            3,
                            0.55f,
                            2f,
                            0.11f
                    ) * warpAmp;

                    float wy = fractalNoise(x - lakeOffsetY, y + lakeOffsetX,
                            3,
                            0.55f,
                            2f,
                            0.11f
                    ) * warpAmp;

                    float px = x + wx;
                    float py = y + wy;

                    float dx = px - cx;
                    float dy = py - cy;

                    float rxp = dx * cos - dy * sin;
                    float ryp = dx * sin + dy * cos;

                    float nx = rxp / rx;
                    float ny = ryp / ry;

                    float dist = (float)Math.sqrt(nx * nx + ny * ny);
                    float angle = (float)Math.atan2(ny, nx);

                    float radialNoise =
                            (float)Math.sin(angle * 3f + lakeOffsetX * 0.001f) * 0.12f +
                            (float)Math.sin(angle * 5f + lakeOffsetY * 0.001f) * 0.08f +
                            fractalNoise(x + lakeOffsetX * 0.37f, y + lakeOffsetY * 0.41f,
                                    3,
                                    0.55f,
                                    2f,
                                    0.18f
                            ) * 0.18f;

                    float threshold = 1f + radialNoise;

                    if (dist < threshold) {
                        land[x][y] = 1;
                    }
                }
            }
        }
    }
    
    private void addLakes(byte[][] land, Params p, byte[][] soundMap, byte[][] lakeMap) {
        for (int i = 0; i < p.lakeAttempts; i++) {
            int cx = random.nextInt(p.width);
            int cy = random.nextInt(p.height);

            if (land[cx][cy] == 0) continue;

            int rx = 4 + random.nextInt(Math.max(1, p.maxLakeRadius - 3));
            int ry = 4 + random.nextInt(Math.max(1, p.maxLakeRadius - 3));

            float rotation = random.nextFloat() * PI2;
            float cos = (float)Math.cos(rotation);
            float sin = (float)Math.sin(rotation);

            float lakeOffsetX = random.nextFloat() * 100_000;
            float lakeOffsetY = random.nextFloat() * 100_000;

            float warpAmp = Math.max(rx, ry) * 0.45f;

            int margin = (int)Math.ceil(Math.max(rx, ry) * 2.2);
            
            final int startX = Math.max(1, cx - margin);
            final int endX = Math.min(p.width - 1, cx + margin);
            final int startY = Math.max(1, cy - margin);
            final int endY = Math.min(p.height - 1, cy + margin);
            boolean intersectOcean = false;

            for (int x = startX; x < endX; x++) {
                for (int y = startY; y < endY; y++) {

                    float wx = fractalNoise(x + lakeOffsetX, y + lakeOffsetY,
                            3,
                            0.55f,
                            2f,
                            0.11f
                    ) * warpAmp;

                    float wy = fractalNoise(x - lakeOffsetY, y + lakeOffsetX,
                            3,
                            0.55f,
                            2f,
                            0.11f
                    ) * warpAmp;

                    float px = x + wx;
                    float py = y + wy;

                    float dx = px - cx;
                    float dy = py - cy;

                    float rxp = dx * cos - dy * sin;
                    float ryp = dx * sin + dy * cos;

                    float nx = rxp / rx;
                    float ny = ryp / ry;

                    float dist = (float)Math.sqrt(nx * nx + ny * ny);
                    float angle = (float)Math.atan2(ny, nx);

                    float radialNoise =
                            (float)Math.sin(angle * 3.0 + lakeOffsetX * 0.001) * 0.12f +
                            (float)Math.sin(angle * 5.0 + lakeOffsetY * 0.001) * 0.08f +
                            fractalNoise(x + lakeOffsetX * 0.37f, y + lakeOffsetY * 0.41f,
                                    3,
                                    0.55f,
                                    2f,
                                    0.18f
                            ) * 0.18f;

                    float threshold = 1f + radialNoise;

                    if (dist < threshold) {
                    	lakeMap[x][y] = 1;
                    	if (land[x][y] == 0 && soundMap[x][y] != Sound.LAKE.code) {
                        	intersectOcean = true;
                        }
                        land[x][y] = 0;
                        soundMap[x][y] = (byte)Sound.LAKE.code;
                    }
                }
            }
            
            if (intersectOcean) {
            	for (int x = startX; x < endX; x++) {
                    for (int y = startY; y < endY; y++) {
                    	if (lakeMap[x][y] != 0) {
                    		soundMap[x][y] = 0;
                    		lakeMap[x][y] = 0;
                    	}
                    }
            	}
            }
        }
    }
    
    private int computeLandAltitude(int x, int y, int distanceToWater, Params p) {
        //distanceToWater: 1 = shoreline, 2+ = dry land
        float coastalFactor = smoothStep(
                0f,
                Math.max(1f, p.coastalPlainWidth),
                Math.max(0f, distanceToWater - 1f)
        );
        
        //Soft base: avoid high cliffs right after the shoreline
        float distanceLift = (float)Math.pow(Math.max(0f, distanceToWater - 1f), 1.08f) * p.inlandRise;
        
        float hills = fractalNoise01(x, y,
                p.hillOctaves,
                0.48f,
                2f,
                p.hillFrequency
        ) * 60f * p.ruggedness;
        
        float ridges = ridgedFractalNoise01(x + 20000, y + 20000,
                p.mountainOctaves,
                0.52f,
                2.12f,
                p.mountainFrequency
        );
        
        float detail = ridgedFractalNoise01(x - 20000, y - 20000,
                p.mountainDetailOctaves,
                0.5f,
                2.2f,
                p.mountainDetailFrequency
        );
        
        float mountainMask = inverseLerp(p.mountainThreshold, 1f, ridges);
        mountainMask = (float)Math.pow(clamp01(mountainMask), p.mountainSharpness);
        
        float mountains = mountainMask
                * (0.65f + detail * 0.55f)
                * p.maxAltitude
                * p.mountainAmount;
        
        //Avoid mountains close to the shoreline
        hills *= 0.25f + coastalFactor * 0.75f;
        mountains *= coastalFactor;
        
        float altitude = 4 + distanceLift + hills + mountains;
        
        if (distanceToWater <= p.coastalPlainWidth + 1) {
            float t = smoothStep(1f, p.coastalPlainWidth + 1f, distanceToWater);
            float localMax = lerp(p.coastalPlainMaxAltitude, p.maxAltitude, t);
            altitude = Math.min(altitude, localMax);
        }
        
        return clamp((int) Math.round(altitude), 4, p.maxAltitude);
    }
    
    private Vec3f tmpNormal = new Vec3f();
    private Vec3f sunDir = new Vec3f(1, -1, 1).nor();
    
    private void chooseCountry(int x, int y, byte[][] heightMap, Params p, byte countryMap[][], byte soundMap[][], int[][] distToWater, int[][] distToLake) {
        int altitude = Byte.toUnsignedInt(heightMap[x][y]);
        int slope = estimateSlope(x, y, heightMap);
        estimateNormal(x, y, heightMap, tmpNormal);
        float shadowed = tmpNormal.dot(sunDir);
        
        float nx = (x / (float)p.width) - 0.5f;
        float ny = (y / (float)p.height) - 0.5f;
        float centerDist = (float)Math.sqrt(nx * nx + ny * ny);
        float continentality = 1f - centerDist * 1.4f;
        continentality = clamp01(continentality);
        
        float[] bc = transformBiomeCoords(x, y, p);
        float bx = bc[0];
        float by = bc[1];
        
        float humidity = fractalNoise01(bx, by,
                p.biomeOctaves,
                0.5f,
                2f,
                p.biomeFrequency
        );
        humidity = smoothStep(0.2f, 0.8f, humidity);
        humidity *= 0.55f + continentality * 0.45f;
        
        float temperature = fractalNoise01(bx + 70000, by - 70000,
                p.biomeOctaves,
                0.5f,
                2f,
                p.biomeFrequency * 0.65f
        );
        temperature -= Math.pow(altitude / 255f * 0.8f + shadowed * 0.2f, 1.35f) * 0.85f;
        temperature = clamp01(temperature);
        
        int distanceFromWater = distToWater[x][y];
        int distanceFromLake = distToLake[x][y];
        
        for (BiomeRule biome : p.biomes) {
            if (matchesBiome(biome, altitude, slope, humidity, temperature, distanceFromWater, distanceFromLake)) {
            	if (biome.country != null) {
            		countryMap[x][y] = (byte) biome.country.getIndex();
            	}
            	if (soundMap[x][y] == 0 && biome.sound != null) {
            		soundMap[x][y] = (byte) biome.sound.code;
            	}
            	return;
            }
        }
        
        if (!p.biomes.isEmpty()) {
        	BiomeRule last = p.biomes.get(p.biomes.size() - 1);
        	if (last.country != null) {
        		countryMap[x][y] = (byte) last.country.getIndex();
        	}
        }
        if (soundMap[x][y] == 0) {
        	soundMap[x][y] = (byte)Sound.BIRDS.code;
        }
    }
    
    private float[] transformBiomeCoords(float x, float y, Params p) {
        float wx = fractalNoise(x + biomeOffsetX, y + biomeOffsetY,
                3,
                0.5f,
                2f,
                p.biomeFrequency * 0.7f
        ) * biomeWarpAmplitude;

        float wy = fractalNoise(x - biomeOffsetY, y + biomeOffsetX,
                3,
                0.5f,
                2f,
                p.biomeFrequency * 0.7f
        ) * biomeWarpAmplitude;

        float px = x + wx;
        float py = y + wy;

        float cx = p.width * 0.5f;
        float cy = p.height * 0.5f;

        float dx = px - cx;
        float dy = py - cy;

        float cos = (float)Math.cos(biomeRotation);
        float sin = (float)Math.sin(biomeRotation);

        float rx = dx * cos - dy * sin;
        float ry = dx * sin + dy * cos;

        rx *= biomeAspectX;
        ry *= biomeAspectY;

        return new float[] {
                rx + cx + biomeOffsetX,
                ry + cy + biomeOffsetY
        };
    }
    
    private boolean matchesBiome(BiomeRule biome, int altitude, int slope, float humidity, float temperature, int distanceFromWater, int distanceFromLake) {
        return altitude >= biome.minAltitude &&
               altitude <= biome.maxAltitude &&
               slope >= biome.minSlope &&
               slope <= biome.maxSlope &&
               humidity >= biome.minHumidity &&
               humidity <= biome.maxHumidity &&
               temperature >= biome.minTemperature &&
               temperature <= biome.maxTemperature &&
               distanceFromWater >= biome.minDistanceFromWater &&
               distanceFromWater <= biome.maxDistanceFromWater &&
               distanceFromLake >= biome.minDistanceFromLake &&
               distanceFromLake <= biome.maxDistanceFromLake;
    }

    private int estimateSlope(int x, int y, byte[][] heightMap) {
        int w = heightMap.length;
        int h = heightMap[0].length;
        int center = Byte.toUnsignedInt(heightMap[x][y]);
        int maxDiff = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int nx = x + dx;
                int ny = y + dy;
                if (nx < 0 || ny < 0 || nx >= w || ny >= h) continue;
                int other = Byte.toUnsignedInt(heightMap[nx][ny]);
                maxDiff = Math.max(maxDiff, Math.abs(center - other));
            }
        }
        return maxDiff;
    }
    
    private Vec3f tmpVec = new Vec3f();
    
    private void estimateNormal(int x, int y, byte[][] heightMap, Vec3f normal) {
        int w = heightMap.length;
        int h = heightMap[0].length;
        if (x <= 1 || x >= w - 1 || y <= 1 || y >= h - 1) {
        	normal.set(0, 1, 0);
        	return;
        }
        int el = Byte.toUnsignedInt(heightMap[x - 1][y]);
        int er = Byte.toUnsignedInt(heightMap[x + 1][y]);
        int eb = Byte.toUnsignedInt(heightMap[x][y - 1]);
        int et = Byte.toUnsignedInt(heightMap[x][y + 1]);
        tmpVec.set(20, er - el, 0);
        normal.set(0, et - eb, 20);
		normal.crs(tmpVec).nor();
    }
    
    private int[][] distanceToValue(byte[][] map, byte target) {
        int w = map.length;
        int h = map[0].length;

        int[][] dist = new int[w][h];
        for (int[] row : dist) Arrays.fill(row, Integer.MAX_VALUE);

        ArrayDeque<int[]> queue = new ArrayDeque<>();

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                if (map[x][y] == target) {
                    dist[x][y] = 0;
                    queue.add(new int[]{x, y});
                }
            }
        }

        int[][] dirs = {
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}
        };

        while (!queue.isEmpty()) {
            int[] c = queue.removeFirst();
            int x = c[0];
            int y = c[1];

            for (int[] d : dirs) {
                int nx = x + d[0];
                int ny = y + d[1];

                if (nx < 0 || ny < 0 || nx >= w || ny >= h) continue;

                if (dist[nx][ny] > dist[x][y] + 1) {
                    dist[nx][ny] = dist[x][y] + 1;
                    queue.addLast(new int[]{nx, ny});
                }
            }
        }

        return dist;
    }
    
    /** Fractal noise in range [-1, +1]. */
    private float fractalNoise(float x, float y, int octaves, float persistence, float lacunarity, float frequency) {
        float amplitude = 1f;
        float max = 0;
        float sum = 0;
        for (int i = 0; i < octaves; i++) {
            float n = noise.eval(x * frequency, y * frequency);
            sum += n * amplitude;
            max += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }
        return sum / max;
    }

    /** Fractal noise normalized in range [0, 1]. */
    public float fractalNoise01(float x, float y, int octaves, float persistence, float lacunarity, float frequency) {
        return (fractalNoise(x, y, octaves, persistence, lacunarity, frequency) + 1f) * 0.5f;
    }

    /** Ridged noise normalized in range [0, 1] */
    public float ridgedFractalNoise01(float x, float y, int octaves, float persistence, float lacunarity, float frequency) {
        float amplitude = 1f;
        float max = 0;
        float sum = 0;
        for (int i = 0; i < octaves; i++) {
            float n = noise.eval(x * frequency, y * frequency); // [-1, +1]
            float ridge = 1f - Math.abs(n);
            ridge *= ridge;
            sum += ridge * amplitude;
            max += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }
        return sum / max;
    }
    
    private float smoothStep(float edge0, float edge1, float x) {
        float t = clamp01((x - edge0) / (edge1 - edge0));
        return t * t * (3f - 2f * t);
    }
    
    private float inverseLerp(float a, float b, float v) {
        if (a == b) return 0;
        return (v - a) / (b - a);
    }
    
    private float lerp(float a, float b, float t) {
        return a + (b - a) * clamp01(t);
    }
    
    private float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
    
    
    public static class Params {
    	public enum Property {
    		WIDTH, HEIGHT, SEED, ISLAND_RADIUS, MOUNTAIN_AMOUNT, RUGGEDNESS, MAX_ALTITUDE,
    		COAST_NOISE_FREQUENCY, COAST_NOISE_OCTAVES,
    		HILL_FREQUENCY, HILL_OCTAVES,
    		MOUNTAIN_FREQUENCY, MOUNTAIN_OCTAVES,
    		MOUNTAIN_DETAIL_FREQUENCY, MOUNTAIN_DETAIL_OCTAVES,
    		BIOME_FREQUENCY, BIOME_OCTAVES,
    		COASTAL_PLAIN_WIDTH, COASTAL_PLAIN_MAX_ALTITUDE,
    		INLAND_RISE, MOUNTAIN_THRESHOLD, MOUNTAIN_SHARPNESS,
    		SMALL_ISLAND_COUNT, LAKE_ATTEMPTS, MAX_LAKE_RADIUS,
    		COASTLINE_COUNTRY, BIOMES
    	}
    	public final Listeners listeners = new Listeners(this);
    	
        private int width = 300;
        private int height = 300;
        private long seed = 0;

        private float islandRadius = 0.45f;
        private float mountainAmount = 0.55f;
        private float ruggedness = 0.35f;
        private int maxAltitude = 255;

        /** Lower frequencies = softer and larger shapes. */
        private float coastNoiseFrequency = 0.03f;
        private int coastNoiseOctaves = 6;

        private float hillFrequency = 0.035f;
        private int hillOctaves = 1;

        private float mountainFrequency = 0.03f;
        private int mountainOctaves = 6;

        /** Ridge noise frquency. Larger values = jagged mountains. */
        private float mountainDetailFrequency = 0.055f;
        private int mountainDetailOctaves = 4;
        
        private float biomeFrequency = 0.02f;
        private int biomeOctaves = 1;
        
        /** Width (in cells) of the low coast zone. */
        private int coastalPlainWidth = 8;
        
        /** Max altitude near to the coast. Avoids vertical walls close to the shoreline. */
        private int coastalPlainMaxAltitude = 15;
        
        /**
         * How fast the ground can raise after the coast zone.
         * Lower values = soften coast; higher values = cliffs closer to the sea.
         */
        private float inlandRise = 1.1f;
        
        /** Higher values = few mountains. */
        private float mountainThreshold = 0.4f;
        
        /** Higher values = sharper mountains. */
        private float mountainSharpness = 1.8f;
        
        private int smallIslandCount = 2;
        private int lakeAttempts = 6;
        private int maxLakeRadius = 12;
        
        private LNDCountry coastlineCountry;
        
        private List<BiomeRule> biomes = new ControlledList<BiomeRule>(new ListControllerAdapter<BiomeRule>() {
        	public void afterAdd(int index, BiomeRule rule) {
        		rule.listeners.add(biomeChangeListener);
        		listeners.notify(EventType.ADD, Property.BIOMES, null, rule, index);
        	};
        	
        	public void afterRemove(int index, BiomeRule rule) {
        		rule.listeners.remove(biomeChangeListener);
        		listeners.notify(EventType.REMOVE, Property.BIOMES, rule, null, index);
        	};
        });
        
        private final UChangeListener biomeChangeListener = new UChangeListener() {
        	@Override
        	public void onChange(UEvent event) {
        		listeners.notify(EventType.CHANGE, Property.BIOMES, null, event.getSource(), -1, event);
        	}
        };
        
        
        public int getWidth() {
        	return width;
        }
        
        public void setWidth(int width) {
        	if (width != this.width) {
        		Object oldValue = this.width;
        		this.width = width;
        		listeners.notify(EventType.CHANGE, Property.WIDTH, oldValue, this.width);
        	}
        }
        
        public int getHeight() {
        	return height;
        }
        
        public void setHeight(int height) {
        	if (height != this.height) {
        		Object oldValue = this.height;
        		this.height = height;
        		listeners.notify(EventType.CHANGE, Property.HEIGHT, oldValue, this.height);
        	}
        }
        
        public long getSeed() {
        	return seed;
        }
        
        public void setSeed(long seed) {
        	if (seed != this.seed) {
        		Object oldValue = this.seed;
        		this.seed = seed;
        		listeners.notify(EventType.CHANGE, Property.SEED, oldValue, this.seed);
        	}
        }
        
        public float getIslandRadius() {
        	return islandRadius;
        }
        
        public void setIslandRadius(int islandRadius) {
        	if (islandRadius != this.islandRadius) {
        		Object oldValue = this.islandRadius;
        		this.islandRadius = islandRadius;
        		listeners.notify(EventType.CHANGE, Property.ISLAND_RADIUS, oldValue, this.islandRadius);
        	}
        }
        
        public float getMountainAmount() {
        	return mountainAmount;
        }
        
        public void setMountainAmount(float mountainAmount) {
        	if (mountainAmount != this.mountainAmount) {
        		Object oldValue = this.mountainAmount;
        		this.mountainAmount = mountainAmount;
        		listeners.notify(EventType.CHANGE, Property.MOUNTAIN_AMOUNT, oldValue, this.mountainAmount);
        	}
        }
        
        public float getRuggedness() {
        	return ruggedness;
        }
        
        public void setRuggedness(float ruggedness) {
        	if (ruggedness != this.ruggedness) {
        		Object oldValue = this.ruggedness;
        		this.ruggedness = ruggedness;
        		listeners.notify(EventType.CHANGE, Property.RUGGEDNESS, oldValue, this.ruggedness);
        	}
        }
        
        public int getMaxAltitude() {
        	return maxAltitude;
        }
        
        public void setMaxAltitude(int maxAltitude) {
        	if (maxAltitude != this.maxAltitude) {
        		Object oldValue = this.maxAltitude;
        		this.maxAltitude = maxAltitude;
        		listeners.notify(EventType.CHANGE, Property.MAX_ALTITUDE, oldValue, this.maxAltitude);
        	}
        }
        
        public float getCoastNoiseFrequency() {
        	return coastNoiseFrequency;
        }
        
        public void setCoastNoiseFrequency(float coastNoiseFrequency) {
        	if (coastNoiseFrequency != this.coastNoiseFrequency) {
        		Object oldValue = this.coastNoiseFrequency;
        		this.coastNoiseFrequency = coastNoiseFrequency;
        		listeners.notify(EventType.CHANGE, Property.COAST_NOISE_FREQUENCY, oldValue, this.coastNoiseFrequency);
        	}
        }
        
        public int getCoastNoiseOctaves() {
        	return coastNoiseOctaves;
        }
        
        public void setCoastNoiseOctaves(int coastNoiseOctaves) {
        	if (coastNoiseOctaves != this.coastNoiseOctaves) {
        		Object oldValue = this.coastNoiseOctaves;
        		this.coastNoiseOctaves = coastNoiseOctaves;
        		listeners.notify(EventType.CHANGE, Property.COAST_NOISE_OCTAVES, oldValue, this.coastNoiseOctaves);
        	}
        }
        
        public float getHillFrequency() {
        	return hillFrequency;
        }
        
        public void setHillFrequency(float hillFrequency) {
        	if (hillFrequency != this.hillFrequency) {
        		Object oldValue = this.hillFrequency;
        		this.hillFrequency = hillFrequency;
        		listeners.notify(EventType.CHANGE, Property.HILL_FREQUENCY, oldValue, this.hillFrequency);
        	}
        }
        
        public int getHillOctaves() {
        	return hillOctaves;
        }
        
        public void setHillOctaves(int hillOctaves) {
        	if (hillOctaves != this.hillOctaves) {
        		Object oldValue = this.hillOctaves;
        		this.hillOctaves = hillOctaves;
        		listeners.notify(EventType.CHANGE, Property.HILL_OCTAVES, oldValue, this.hillOctaves);
        	}
        }
        
        public float getMountainFrequency() {
        	return mountainFrequency;
        }
        
        public void setMountainFrequency(float mountainFrequency) {
        	if (mountainFrequency != this.mountainFrequency) {
        		Object oldValue = this.mountainFrequency;
        		this.mountainFrequency = mountainFrequency;
        		listeners.notify(EventType.CHANGE, Property.MOUNTAIN_FREQUENCY, oldValue, this.mountainFrequency);
        	}
        }
        
        public int getMountainOctaves() {
        	return mountainOctaves;
        }
        
        public void setMountainOctaves(int mountainOctaves) {
        	if (mountainOctaves != this.mountainOctaves) {
        		Object oldValue = this.mountainOctaves;
        		this.mountainOctaves = mountainOctaves;
        		listeners.notify(EventType.CHANGE, Property.MOUNTAIN_OCTAVES, oldValue, this.mountainOctaves);
        	}
        }
        
        public float getMountainDetailFrequency() {
        	return mountainDetailFrequency;
        }
        
        public void setMountainDetailFrequency(float mountainDetailFrequency) {
        	if (mountainDetailFrequency != this.mountainDetailFrequency) {
        		Object oldValue = this.mountainDetailFrequency;
        		this.mountainDetailFrequency = mountainDetailFrequency;
        		listeners.notify(EventType.CHANGE, Property.MOUNTAIN_DETAIL_FREQUENCY, oldValue, this.mountainDetailFrequency);
        	}
        }
        
        public int getMountainDetailOctaves() {
        	return mountainDetailOctaves;
        }
        
        public void setMountainDetailOctaves(int mountainDetailOctaves) {
        	if (mountainDetailOctaves != this.mountainDetailOctaves) {
        		Object oldValue = this.mountainDetailOctaves;
        		this.mountainDetailOctaves = mountainDetailOctaves;
        		listeners.notify(EventType.CHANGE, Property.MOUNTAIN_DETAIL_OCTAVES, oldValue, this.mountainDetailOctaves);
        	}
        }
        
        public float getBiomeFrequency() {
        	return biomeFrequency;
        }
        
        public void setBiomeFrequency(float biomeFrequency) {
        	if (biomeFrequency != this.biomeFrequency) {
        		Object oldValue = this.biomeFrequency;
        		this.biomeFrequency = biomeFrequency;
        		listeners.notify(EventType.CHANGE, Property.BIOME_FREQUENCY, oldValue, this.biomeFrequency);
        	}
        }
        
        public int getBiomeOctaves() {
        	return biomeOctaves;
        }
        
        public void setBiomeOctaves(int biomeOctaves) {
        	if (biomeOctaves != this.biomeOctaves) {
        		Object oldValue = this.biomeOctaves;
        		this.biomeOctaves = biomeOctaves;
        		listeners.notify(EventType.CHANGE, Property.BIOME_OCTAVES, oldValue, this.biomeOctaves);
        	}
        }
        
        public int getCoastalPlainWidth() {
        	return coastalPlainWidth;
        }
        
        public void setCoastalPlainWidth(int coastalPlainWidth) {
        	if (coastalPlainWidth != this.coastalPlainWidth) {
        		Object oldValue = this.coastalPlainWidth;
        		this.coastalPlainWidth = coastalPlainWidth;
        		listeners.notify(EventType.CHANGE, Property.COASTAL_PLAIN_WIDTH, oldValue, this.coastalPlainWidth);
        	}
        }
        
        public int getCoastalPlainMaxAltitude() {
        	return coastalPlainMaxAltitude;
        }
        
        public void setCoastalPlainMaxAltitude(int coastalPlainMaxAltitude) {
        	if (coastalPlainMaxAltitude != this.coastalPlainMaxAltitude) {
        		Object oldValue = this.coastalPlainMaxAltitude;
        		this.coastalPlainMaxAltitude = coastalPlainMaxAltitude;
        		listeners.notify(EventType.CHANGE, Property.COASTAL_PLAIN_MAX_ALTITUDE, oldValue, this.coastalPlainMaxAltitude);
        	}
        }
        
        public float getInlandRise() {
        	return inlandRise;
        }
        
        public void setInlandRise(float inlandRise) {
        	if (inlandRise != this.inlandRise) {
        		Object oldValue = this.inlandRise;
        		this.inlandRise = inlandRise;
        		listeners.notify(EventType.CHANGE, Property.INLAND_RISE, oldValue, this.inlandRise);
        	}
        }
        
        public float getMountainThreshold() {
        	return mountainThreshold;
        }
        
        public void setMountainThreshold(float mountainThreshold) {
        	if (mountainThreshold != this.mountainThreshold) {
        		Object oldValue = this.mountainThreshold;
        		this.mountainThreshold = mountainThreshold;
        		listeners.notify(EventType.CHANGE, Property.MOUNTAIN_THRESHOLD, oldValue, this.mountainThreshold);
        	}
        }
        
        public float getMountainSharpness() {
        	return mountainSharpness;
        }
        
        public void setMountainSharpness(float mountainSharpness) {
        	if (mountainSharpness != this.mountainSharpness) {
        		Object oldValue = this.mountainSharpness;
        		this.mountainSharpness = mountainSharpness;
        		listeners.notify(EventType.CHANGE, Property.MOUNTAIN_SHARPNESS, oldValue, this.mountainSharpness);
        	}
        }
        
        public int getSmallIslandCount() {
        	return smallIslandCount;
        }
        
        public void setSmallIslandCount(int smallIslandCount) {
        	if (smallIslandCount != this.smallIslandCount) {
        		Object oldValue = this.smallIslandCount;
        		this.smallIslandCount = smallIslandCount;
        		listeners.notify(EventType.CHANGE, Property.SMALL_ISLAND_COUNT, oldValue, this.smallIslandCount);
        	}
        }
        
        public int getLakeAttempts() {
        	return lakeAttempts;
        }
        
        public void setLakeAttempts(int lakeAttempts) {
        	if (lakeAttempts != this.lakeAttempts) {
        		Object oldValue = this.lakeAttempts;
        		this.lakeAttempts = lakeAttempts;
        		listeners.notify(EventType.CHANGE, Property.LAKE_ATTEMPTS, oldValue, this.lakeAttempts);
        	}
        }
        
        public int getMaxLakeRadius() {
        	return maxLakeRadius;
        }
        
        public void setMaxLakeRadius(int maxLakeRadius) {
        	if (maxLakeRadius != this.maxLakeRadius) {
        		Object oldValue = this.maxLakeRadius;
        		this.maxLakeRadius = maxLakeRadius;
        		listeners.notify(EventType.CHANGE, Property.MAX_LAKE_RADIUS, oldValue, this.maxLakeRadius);
        	}
        }
        
        public LNDCountry getCoastlineCountry() {
        	return coastlineCountry;
        }
        
        public void setCoastlineCountry(LNDCountry coastlineCountry) {
        	if (coastlineCountry != this.coastlineCountry) {
        		Object oldValue = this.coastlineCountry;
        		this.coastlineCountry = coastlineCountry;
        		listeners.notify(EventType.CHANGE, Property.COASTLINE_COUNTRY, oldValue, this.coastlineCountry);
        	}
        }
        
        public List<BiomeRule> getBiomes() {
        	return biomes;
        }
    }
    
    
    public static class BiomeRule {
    	public enum Property {
    		NAME, COUNTRY, SOUND, MIN_ALTITUDE, MAX_ALTITUDE, MIN_SLOPE, MAX_SLOPE, MIN_HUMIDITY, MAX_HUMIDITY,
    		MIN_TEMPERATURE, MAX_TEMPERATURE, MIN_DIST_FROM_WATER, MAX_DIST_FROM_WATER, MIN_DIST_FROM_LAKE, MAX_DIST_FROM_LAKE
    	}
    	public final Listeners listeners = new Listeners(this);
    	
    	private String name = "";
    	private LNDCountry country;
    	private Sound sound;
        
    	private int minAltitude = 4;
    	private int maxAltitude = 255;
        
    	private int minSlope = 0;
    	private int maxSlope = 255;
        
    	private float minHumidity = 0;
    	private float maxHumidity = 1;
        
    	private float minTemperature = 0;
    	private float maxTemperature = 1;
        
    	private int minDistanceFromWater = 0;
    	private int maxDistanceFromWater = Integer.MAX_VALUE;
        
    	private int minDistanceFromLake = 0;
    	private int maxDistanceFromLake = Integer.MAX_VALUE;
        
        public BiomeRule() {}
        
        public BiomeRule(String name, LNDCountry country, Sound sound) {
            this.name = name;
        	this.country = country;
            this.sound = sound;
        }
        
        public String getName() {
        	return name;
        }
        
        public void setName(String name) {
        	if (!this.name.equals(name)) {
        		Object oldValue = this.name;
        		this.name = name;
        		listeners.notify(EventType.CHANGE, Property.NAME, oldValue, this.name);
        	}
        }
        
        public LNDCountry getCountry() {
        	return country;
        }
        
        public void setCountry(LNDCountry country) {
        	if (country != this.country) {
        		Object oldValue = this.country;
        		this.country = country;
        		listeners.notify(EventType.CHANGE, Property.NAME, oldValue, this.country);
        	}
        }
        
        public Sound getSound() {
        	return sound;
        }
        
        public void setSound(Sound sound) {
        	if (sound != this.sound) {
        		Object oldValue = this.sound;
        		this.sound = sound;
        		listeners.notify(EventType.CHANGE, Property.SOUND, oldValue, this.sound);
        	}
        }
        
        public int getMinAltitude() {
        	return minAltitude;
        }
        
        public void setMinAltitude(int minAltitude) {
        	if (minAltitude != this.minAltitude) {
        		Object oldValue = this.minAltitude;
        		this.minAltitude = minAltitude;
        		listeners.notify(EventType.CHANGE, Property.MIN_ALTITUDE, oldValue, this.minAltitude);
        	}
        }
        
        public int getMaxAltitude() {
        	return maxAltitude;
        }
        
        public void setMaxAltitude(int maxAltitude) {
        	if (maxAltitude != this.maxAltitude) {
        		Object oldValue = this.maxAltitude;
        		this.maxAltitude = maxAltitude;
        		listeners.notify(EventType.CHANGE, Property.MAX_ALTITUDE, oldValue, this.maxAltitude);
        	}
        }
        
        public int getMinSlope() {
        	return minSlope;
        }
        
        public void setMinSlope(int minSlope) {
        	if (minSlope != this.minSlope) {
        		Object oldValue = this.minSlope;
        		this.minSlope = minSlope;
        		listeners.notify(EventType.CHANGE, Property.MIN_SLOPE, oldValue, this.minSlope);
        	}
        }
        
        public int getMaxSlope() {
        	return maxSlope;
        }
        
        public void setMaxSlope(int maxSlope) {
        	if (maxSlope != this.maxSlope) {
        		Object oldValue = this.maxSlope;
        		this.maxSlope = maxSlope;
        		listeners.notify(EventType.CHANGE, Property.MAX_SLOPE, oldValue, this.maxSlope);
        	}
        }
        
        public float getMinHumidity() {
        	return minHumidity;
        }
        
        public void setMinHumidity(float minHumidity) {
        	if (minHumidity != this.minHumidity) {
        		Object oldValue = this.minHumidity;
        		this.minHumidity = minHumidity;
        		listeners.notify(EventType.CHANGE, Property.MIN_HUMIDITY, oldValue, this.minHumidity);
        	}
        }
        
        public float getMaxHumidity() {
        	return maxHumidity;
        }
        
        public void setMaxHumidity(float maxHumidity) {
        	if (maxHumidity != this.maxHumidity) {
        		Object oldValue = this.maxHumidity;
        		this.maxHumidity = maxHumidity;
        		listeners.notify(EventType.CHANGE, Property.MAX_HUMIDITY, oldValue, this.maxHumidity);
        	}
        }
        
        public float getMinTemperature() {
        	return minTemperature;
        }
        
        public void setMinTemperature(float minTemperature) {
        	if (minTemperature != this.minTemperature) {
        		Object oldValue = this.minTemperature;
        		this.minTemperature = minTemperature;
        		listeners.notify(EventType.CHANGE, Property.MIN_TEMPERATURE, oldValue, this.minTemperature);
        	}
        }
        
        public float getMaxTemperature() {
        	return maxTemperature;
        }
        
        public void setMaxTemperature(float maxTemperature) {
        	if (maxTemperature != this.maxTemperature) {
        		Object oldValue = this.maxTemperature;
        		this.maxTemperature = maxTemperature;
        		listeners.notify(EventType.CHANGE, Property.MAX_TEMPERATURE, oldValue, this.maxTemperature);
        	}
        }
        
        public int getMinDistanceFromWater() {
        	return minDistanceFromWater;
        }
        
        public void setMinDistanceFromWater(int minDistanceFromWater) {
        	if (minDistanceFromWater != this.minDistanceFromWater) {
        		Object oldValue = this.minDistanceFromWater;
        		this.minDistanceFromWater = minDistanceFromWater;
        		listeners.notify(EventType.CHANGE, Property.MIN_DIST_FROM_WATER, oldValue, this.minDistanceFromWater);
        	}
        }
        
        public int getMaxDistanceFromWater() {
        	return maxDistanceFromWater;
        }
        
        public void setMaxDistanceFromWater(int maxDistanceFromWater) {
        	if (maxDistanceFromWater != this.maxDistanceFromWater) {
        		Object oldValue = this.maxDistanceFromWater;
        		this.maxDistanceFromWater = maxDistanceFromWater;
        		listeners.notify(EventType.CHANGE, Property.MAX_DIST_FROM_WATER, oldValue, this.maxDistanceFromWater);
        	}
        }
        
        public int getMinDistanceFromLake() {
        	return minDistanceFromLake;
        }
        
        public void setMinDistanceFromLake(int minDistanceFromLake) {
        	if (minDistanceFromLake != this.minDistanceFromLake) {
        		Object oldValue = this.minDistanceFromLake;
        		this.minDistanceFromLake = minDistanceFromLake;
        		listeners.notify(EventType.CHANGE, Property.MIN_DIST_FROM_LAKE, oldValue, this.minDistanceFromLake);
        	}
        }
        
        public int getMaxDistanceFromLake() {
        	return maxDistanceFromLake;
        }
        
        public void setMaxDistanceFromLake(int maxDistanceFromLake) {
        	if (maxDistanceFromLake != this.maxDistanceFromLake) {
        		Object oldValue = this.maxDistanceFromLake;
        		this.maxDistanceFromLake = maxDistanceFromLake;
        		listeners.notify(EventType.CHANGE, Property.MAX_DIST_FROM_LAKE, oldValue, this.maxDistanceFromLake);
        	}
        }
    }
    
    
    private static class Result {
        public final byte[][] heightMap;
        public final byte[][] countryMap;
        public final byte[][] soundMap;
        
        public Result(byte[][] heightMap, byte[][] countryMap, byte[][] soundMap, byte[][] lakeMap) {
            this.heightMap = heightMap;
            this.countryMap = countryMap;
            this.soundMap = soundMap;
        }
    }
}
