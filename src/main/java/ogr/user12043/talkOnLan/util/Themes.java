package ogr.user12043.talkOnLan.util;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import mdlaf.MaterialLookAndFeel;
import mdlaf.themes.MaterialLiteTheme;
import mdlaf.themes.MaterialOceanicTheme;

import javax.swing.*;
import javax.swing.plaf.basic.BasicLookAndFeel;

/**
 * Created by user12043 on 3.05.2020 - 15:06
 * part of project: talk-onLan
 */
public class Themes {
    public static final UIManager.LookAndFeelInfo[] INSTALLED_LOOK_AND_FEELS = UIManager.getInstalledLookAndFeels();
    public static final String FLAT_INTELLIJ_LAF = "Flat Intellij";
    public static final String FLAT_DARCULA_LAF = "Flat Darcula";
    public static final String FLAT_LIGHT_LAF = "Flat Light";
    public static final String FLAT_DARK_LAF = "Flat Dark";
    public static final String MATERIAL_LITE = "Material Lite";
    public static final String MATERIAL_OCEANIC = "Material Oceanic";
    public static final String DEFAULT_THEME = FLAT_LIGHT_LAF;

    public static final String[] THEMES = new String[]{
            FLAT_INTELLIJ_LAF,
            FLAT_DARCULA_LAF,
            FLAT_LIGHT_LAF,
            FLAT_DARK_LAF,
            MATERIAL_LITE,
            MATERIAL_OCEANIC
    };

    public static BasicLookAndFeel get(String name) {
        switch (name) {
            case FLAT_INTELLIJ_LAF:
                return new FlatIntelliJLaf();
            case FLAT_DARCULA_LAF:
                return new FlatDarculaLaf();
            case FLAT_LIGHT_LAF:
                return new FlatLightLaf();
            case FLAT_DARK_LAF:
                return new FlatDarkLaf();
            case MATERIAL_LITE:
                return new MaterialLookAndFeel(new MaterialLiteTheme());
            case MATERIAL_OCEANIC:
                return new MaterialLookAndFeel(new MaterialOceanicTheme());
        }
        return null;
    }
}
