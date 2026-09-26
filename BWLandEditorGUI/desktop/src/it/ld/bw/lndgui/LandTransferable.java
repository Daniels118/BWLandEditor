package it.ld.bw.lndgui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import it.ld.bw.lnd.model.LndFile;

public class LandTransferable implements Transferable {
	public static final DataFlavor LAND_FLAVOR = new DataFlavor("application/x-blackandwhite-lnd;class=java.io.InputStream", "Black & White Land file");
	
	private final byte[] bytes;
	
	public LandTransferable(LndFile land) throws Exception {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(20 * 1024 * 1024);) {
			land.write(baos);
			bytes = baos.toByteArray();
		}
	}
	
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { LAND_FLAVOR };
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(LAND_FLAVOR);
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (flavor.equals(LAND_FLAVOR)) {
			return new ByteArrayInputStream(bytes);
		}
		throw new UnsupportedFlavorException(flavor);
	}
}
