package it.ld.bw.lndgui;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import games.spooky.gdx.nativefilechooser.desktop.DesktopFileChooser;
import it.ld.bw.lhx.LHXParser;
import it.ld.bw.lhx.ParseException;
import it.ld.bw.lhx.Statement;
import it.ld.bw.lnd.model.LndFile;
import it.ld.bw.lndgui.interfaces.ClipboardListener;
import it.ld.bw.lndgui.interfaces.ClipboardListener.DataType;
import it.ld.bw.lndgui.interfaces.FolderChooser;
import it.ld.bw.lndgui.interfaces.ImageWriter;
import it.ld.bw.lndgui.interfaces.OS;

public class DesktopOS implements OS, AutoCloseable {
	private static final float COPY_DELAY = 0.2f;
	private static final int COPY_RETRY = 3;
	
	private DesktopFileChooser fileChooser;
	private DesktopFolderChooser folderChooser;
	private DesktopImageWriter imageWriter;
	
	private ClipboardListener clipboardListener;
	
	public DesktopOS() {
		attachClipboardListener();
	}
	
	private void attachClipboardListener() {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.addFlavorListener(flavorListener);
	}
	
	private final FlavorListener flavorListener = new FlavorListener() {
		private Task copyTask;
		private int retry;
		private Task stringMonitorTask;
		private String oldText;
		
		@Override
		public void flavorsChanged(FlavorEvent event) {
			initTasks();
			if (copyTask.isScheduled()) {
				copyTask.cancel();
			}
			if (stringMonitorTask.isScheduled()) {
				stringMonitorTask.cancel();
			}
			//
			if (clipboardListener == null) return;
			retry = COPY_RETRY;
			Timer.schedule(copyTask, 0.05f, COPY_DELAY);
		}
		
		private void initTasks() {
			if (copyTask == null) {
				copyTask = new Task() {
					@Override
				    public void run() {
						try {
							Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
							Transferable transferable = clipboard.getContents(null);
							if (transferable.isDataFlavorSupported(LandTransferable.LAND_FLAVOR)) {
								LndFile land = new LndFile(true);
								try (InputStream inp = (InputStream)transferable.getTransferData(LandTransferable.LAND_FLAVOR);) {
									land.read(inp);
								}
								clipboardListener.contentsChanged(DataType.LAND, land);
							} else if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
								oldText = (String)transferable.getTransferData(DataFlavor.stringFlavor);
								List<Statement> objects = getClipboardObjects();
								if (objects != null) {
									clipboardListener.contentsChanged(DataType.OBJECTS, objects);
								} else {
									clipboardListener.contentsChanged(null, null);
								}
								//
								if (!stringMonitorTask.isScheduled()) {
									Timer.schedule(stringMonitorTask, 0.3f, 0.3f);
								}
							} else {
								clipboardListener.contentsChanged(null, null);
							}
							copyTask.cancel();
						} catch (IllegalStateException e) {	//May happen if clipboard is accessed too early
							if (--retry <= 0) {
								e.printStackTrace();
								copyTask.cancel();
							}
						} catch (Exception e) {
							e.printStackTrace();
							copyTask.cancel();
						}
				    }
				};
			}
			
			if (stringMonitorTask == null) {
				stringMonitorTask = new Task() {
					@Override
					public void run() {
						try {
							Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
							Transferable transferable = clipboard.getContents(null);
							String text = (String)transferable.getTransferData(DataFlavor.stringFlavor);
							if (!text.equals(oldText)) {
								Timer.schedule(copyTask, 0.05f, COPY_DELAY);
							}
						} catch (UnsupportedFlavorException e) {
							stringMonitorTask.cancel();
						} catch (Exception e) {}
					}
				};
			}
		}
	};
	
	public void setClipboardListener(ClipboardListener listener) {
		this.clipboardListener = listener;
	}
	
	public DataType getClipboardContentType() {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable transferable = clipboard.getContents(null);
		if (transferable.isDataFlavorSupported(LandTransferable.LAND_FLAVOR)) {
			return DataType.LAND;
		} else if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			try {
				String text = (String)transferable.getTransferData(DataFlavor.stringFlavor);
				LHXParser parser = new LHXParser();
				parser.parse(text, false);
				return DataType.OBJECTS;
			} catch (Exception e) {
				//Invalid LHX, nothing to do
			}
		}
		return null;
	}
	
	@Override
	public LndFile getClipboardLand() throws Exception {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable transferable = clipboard.getContents(null);
		if (transferable.isDataFlavorSupported(LandTransferable.LAND_FLAVOR)) {
			LndFile land = new LndFile(true);
			try (InputStream inp = (InputStream)transferable.getTransferData(LandTransferable.LAND_FLAVOR);) {
				land.read(inp);
			}
			return land;
		}
		return null;
	}
	
	@Override
	public List<Statement> getClipboardObjects() throws Exception {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable transferable = clipboard.getContents(null);
		if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			String text = (String)transferable.getTransferData(DataFlavor.stringFlavor);
			LHXParser parser = new LHXParser();
			try {
				return parser.parse(text, false);
			} catch (ParseException e) {
				//Invalid LHX, nothing to do
			}
		}
		return null;
	}
	
	@Override
	public void setClipboardContent(LndFile land) {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		if (land == null) {
			clipboard.setContents(null, null);
		} else {
			try {
				clipboard.setContents(new LandTransferable(land), null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void setClipboardContent(List<Statement> statements) {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		if (statements == null) {
			clipboard.setContents(null, null);
		} else {
			try {
				StringBuilder sb = new StringBuilder(2 * 1024 * 1024);
				for (Statement stmt : statements) {
					sb.append(stmt.toString()).append("\r\n");
				}
				String text = sb.toString();
				clipboard.setContents(new StringSelection(text), null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public NativeFileChooser getFileChooser() {
		if (fileChooser == null) {
			fileChooser = new DesktopFileChooser();
		}
		return fileChooser;
	}
	
	@Override
	public FolderChooser getFolderChooser() {
		if (folderChooser == null) {
			folderChooser = new DesktopFolderChooser();
		}
		return folderChooser;
	}

	@Override
	public ImageWriter getImageWriter() {
		if (imageWriter == null) {
			imageWriter = new DesktopImageWriter();
		}
		return imageWriter;
	}

	@Override
	public void openURL(String url) {
		try {
            URI uri = new URI(url);
            if (uri.getScheme() == null || "file".equalsIgnoreCase(uri.getScheme())) {
            	if (!uri.isAbsolute()) {
            		uri = getAppDir().resolve(url).toAbsolutePath().normalize().toUri();
            	}
            }
			Desktop.getDesktop().browse(uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	@Override
	public Path getAppDir() {
	    Path javaHome = Paths.get(System.getProperty("java.home"));
	    if (Files.exists(javaHome.resolve("jmods"))) {
	        Path parent = javaHome.getParent();
	        if (parent != null && Files.exists(parent.resolve("app"))) {
	            return parent.toAbsolutePath().normalize();
	        }
	    }
	    return Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
	}
	
	@Override
	public void close() {
		if (folderChooser != null) folderChooser.close();
	}
}
