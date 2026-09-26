package it.ld.bw.lndgui.interfaces;

import java.nio.file.Path;
import java.util.List;

import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import it.ld.bw.lhx.Statement;
import it.ld.bw.lnd.model.LndFile;

public interface OS {
	public void setClipboardListener(ClipboardListener listener);
	public ClipboardListener.DataType getClipboardContentType();
	public LndFile getClipboardLand() throws Exception;
	public List<Statement> getClipboardObjects() throws Exception;
	public void setClipboardContent(LndFile land);
	public void setClipboardContent(List<Statement> statements);
	public NativeFileChooser getFileChooser();
	public FolderChooser getFolderChooser();
	public ImageWriter getImageWriter();
	public void openURL(String url);
	public Path getAppDir();
}
