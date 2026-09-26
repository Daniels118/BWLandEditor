package it.ld.bw.lndgui.gfx;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Disposable;

import it.ld.bw.lnd.model.LNDMaterial;

import it.ld.libgdx.utils.Utils;

public class Material3D implements Disposable {
	private final LNDMaterial landMaterial;
	private final Pixmap pixmap;
	
	public Material3D(LNDMaterial lndmat) {
		this.landMaterial = lndmat;
		this.pixmap = Utils.toPixmap(lndmat.getIntARGB(), LNDMaterial.width, LNDMaterial.height);
	}
	
	public LNDMaterial getLandMaterial() {
		return landMaterial;
	}
	
	public Pixmap getPixmap() {
		return pixmap;
	}
	
	@Override
	public void dispose() {
		pixmap.dispose();
	}
}
