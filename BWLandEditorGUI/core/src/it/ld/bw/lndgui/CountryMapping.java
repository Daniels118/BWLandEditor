package it.ld.bw.lndgui;

import it.ld.bw.lnd.model.LNDCountry;

public class CountryMapping {
	public final LNDCountry srcCountry;
	public LNDCountry dstCountry;
	
	public CountryMapping(LNDCountry srcCountry) {
		this.srcCountry = srcCountry;
	}
	
	@Override
	public String toString() {
		return srcCountry + " -> " + dstCountry;
	}
}
