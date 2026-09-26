package it.ld.bw.lndgui.interfaces;

import java.io.File;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public interface FolderChooser {
	void chooseFolder(Stage stage, Skin skin, String dialogTitle, FolderChooserCallback callback);
	void chooseFolder(Stage stage, Skin skin, String dialogTitle, File initialDirectory, FolderChooserCallback callback);
    
    public interface FolderChooserCallback {
        void onFolderChosen(FileHandle folder);
        void onCancellation();
        void onError(Exception exception);
    }
}