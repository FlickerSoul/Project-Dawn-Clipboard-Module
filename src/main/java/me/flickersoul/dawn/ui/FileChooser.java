package me.flickersoul.dawn.ui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

public class FileChooser {
     JFileChooser fileChooser;

    public  File[] returnFilesUsingChooser() {
        JFrame jFrame = new JFrame();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e) {
            e.printStackTrace();
        }
        fileChooser = new JFileChooser();
        // enable multi-selection mode
        fileChooser.setMultiSelectionEnabled(true);
        // set extensions
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Documentation(*.txt)", "txt"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("PDF Files(*.pdf)", "pdf"));
        fileChooser.setAcceptAllFileFilterUsed(true);
        // only select files
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        //open window
        int result = fileChooser.showDialog(jFrame, null);
        File[] tempFiles = null;
        if(result == JFileChooser.APPROVE_OPTION) {
            System.out.println("You chose a file.");
            tempFiles = fileChooser.getSelectedFiles();
        }else if(result == JFileChooser.CANCEL_OPTION){
            System.out.println("You didn't choose any files.");
        }

        jFrame.dispose();

        return tempFiles;
    }
}
