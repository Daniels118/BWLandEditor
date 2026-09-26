package it.ld.bw.lndgui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Scaling;

import it.ld.bw.lndgui.interfaces.FolderChooser;

import javax.swing.SwingUtilities;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.File;

import static org.lwjgl.util.nfd.NativeFileDialog.*;

public class DesktopFolderChooser implements FolderChooser, AutoCloseable {
	public DesktopFolderChooser() {
		NFD_Init();
	}
	
    @Override
    public void chooseFolder(Stage stage, Skin skin, String dialogTitle, FolderChooserCallback callback) {
    	chooseFolder(stage, skin, dialogTitle, null, null);
    }
    
    @Override
    public void chooseFolder(Stage stage, Skin skin, String dialogTitle, File initialDirectory, FolderChooserCallback callback) {
    	final Image modalShadow = (stage != null && skin != null) ? new Image(skin.newDrawable("white", new Color(0f, 0f, 0f, 0.5f))) : null;
    	if (modalShadow != null) {
	    	modalShadow.setScaling(Scaling.fill);
	    	modalShadow.setBounds(0, 0, stage.getWidth(), stage.getHeight());
			stage.addActor(modalShadow);
    	}
    	SwingUtilities.invokeLater(() -> {
            try {
            	File selected = chooseFolder(initialDirectory);
            	if (modalShadow != null) {
            		modalShadow.remove();
            	}
                if (selected != null) {
                    Gdx.app.postRunnable(() -> callback.onFolderChosen(new FileHandle(selected)));
                } else {
                    Gdx.app.postRunnable(callback::onCancellation);
                }
            } catch (Exception e) {
                Gdx.app.postRunnable(() -> callback.onError(e));
            }
        });
    }
    
    private static File chooseFolder(File defaultDir) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer outPath = stack.mallocPointer(1);
            int result = NFD_PickFolder(outPath, defaultDir != null ? defaultDir.toString() : null);
            if (result == NFD_OKAY) {
                long pathPtr = outPath.get(0);
                String path = MemoryUtil.memUTF8(pathPtr);
                NFD_FreePath(pathPtr);
                return new File(path);
            } else if (result == NFD_CANCEL) {
                return null;
            } else {
                throw new RuntimeException("NativeFileDialog error: " + NFD_GetError());
            }
        }
    }
    
    @Override
    public void close() {
    	NFD_Quit();
    }
}
