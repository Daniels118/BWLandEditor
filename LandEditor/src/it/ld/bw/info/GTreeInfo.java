package it.ld.bw.info;

import java.io.IOException;

import it.ld.utils.EndianDataInputStream;
import it.ld.utils.EndianDataOutputStream;

public class GTreeInfo extends GSingleMapFixedInfo {
	public int growing;
	public int burning;
	public int strength;
	public float defence;
	public float startLife;
	public int growsAfterNumGameTurns;
	public float growthAmount;
	public int burnCheckAroundEvery;
	public int randomBurnChance;
	public float minSize;
	public float maxSize;
	public float rainingAcceleratorMultiplier;
	public float waterSpellAcceleratorMultiplier;
	public int carriedType;
	public short maxNumTreesCanProduce;
	
	@Override
	public void read(EndianDataInputStream str) throws IOException {
		little(str);
		super.read(str);
		growing = str.readInt();
		burning = str.readInt();
		strength = str.readInt();
		defence = str.readFloat();
		startLife = str.readFloat();
		growsAfterNumGameTurns = str.readInt();
		growthAmount = str.readFloat();
		burnCheckAroundEvery = str.readInt();
		randomBurnChance = str.readInt();
		minSize = str.readFloat();
		maxSize = str.readFloat();
		rainingAcceleratorMultiplier = str.readFloat();
		waterSpellAcceleratorMultiplier = str.readFloat();
		carriedType = str.readInt();
		maxNumTreesCanProduce = str.readShort();
	}
	
	@Override
	public void write(EndianDataOutputStream str) throws IOException {
		throw new RuntimeException("Method not implemented");
	}
}
