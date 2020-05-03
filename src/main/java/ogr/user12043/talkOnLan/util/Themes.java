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
import java.util.Map;

/**
 * Created by user12043 on 3.05.2020 - 15:06
 * part of project: talk-onLan
 */
public class Themes {
    public static final String DEFAULT_THEME = "Flat Light";
    public static final UIManager.LookAndFeelInfo[] INSTALLED_LOOK_AND_FEELS = UIManager.getInstalledLookAndFeels();
    private static final FlatIntelliJLaf FLAT_INTELLIJ_LAF = new FlatIntelliJLaf();
    private static final FlatDarculaLaf FLAT_DARCULA_LAF = new FlatDarculaLaf();
    private static final FlatLightLaf FLAT_LIGHT_LAF = new FlatLightLaf();
    private static final FlatDarkLaf FLAT_DARK_LAF = new FlatDarkLaf();
    private static final MaterialLookAndFeel MATERIAL_LITE = new MaterialLookAndFeel(new MaterialLiteTheme());
    private static final MaterialLookAndFeel MATERIAL_OCEANIC = new MaterialLookAndFeel(new MaterialOceanicTheme());

    public static final Map<String, BasicLookAndFeel> THEMES = Map.of("Flat Intellij", FLAT_INTELLIJ_LAF, "Flat Darcula", FLAT_DARCULA_LAF, "Flat Light", FLAT_LIGHT_LAF, "Flat Dark", FLAT_DARK_LAF, "Material Lite", MATERIAL_LITE, "Material Oceanic", MATERIAL_OCEANIC);
}
