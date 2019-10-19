module me.flickersoul.dawn {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.media;
    requires java.datatransfer;
    requires java.desktop;
    requires java.sql;
    requires jkeymaster;
    requires org.jsoup;
    requires fastjson;
    requires jdk.jsobject;
    requires commons.dbcp2;
    requires commons.logging;
    requires commons.pool2;
    requires java.management;
    requires jdk.net;
    requires java.base;
    requires java.logging;


    exports me.flickersoul.dawn.ui;
    exports me.flickersoul.dawn.functions;
    opens me.flickersoul.dawn.ui;
    opens me.flickersoul.dawn.functions;

//    exports javafx.graphics to com.sun.javafx.sg.prism;

//    --module-path
//"C:\Program Files\Java\javafx-sdk-11.0.1\lib"
//--add-modules=javafx.controls,javafx.fxml
//--add-exports
//javafx.graphics/com.sun.javafx.sg.prism=ALL-UNNAMED
//--add-exports
//javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED
//--add-exports
//javafx.graphics/com.sun.javafx.util=ALL-UNNAMED
//--add-exports
//javafx.base/com.sun.javafx.logging=ALL-UNNAMED
//--add-exports
//javafx.graphics/com.sun.prism=ALL-UNNAMED
//--add-exports
//javafx.graphics/com.sun.glass.ui=ALL-UNNAMED
//--add-exports
//javafx.graphics/com.sun.javafx.geom.transform=ALL-UNNAMED
//--add-exports
//javafx.graphics/com.sun.javafx.tk=ALL-UNNAMED
//--add-exports
//javafx.graphics/com.sun.glass.utils=ALL-UNNAMED
//--add-exports
//javafx.graphics/com.sun.javafx.font=ALL-UNNAMED
//--add-exports
//javafx.graphics/com.sun.javafx.application=ALL-UNNAMED
//--add-exports
//javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED
//--add-exports
//javafx.graphics/com.sun.javafx.scene.input=ALL-UNNAMED
//--add-exports
//javafx.graphics/com.sun.javafx.geom=ALL-UNNAMED
//--add-exports
//javafx.graphics/com.sun.prism.paint=ALL-UNNAMED
//--add-exports
//javafx.graphics/com.sun.scenario.effect=ALL-UNNAMED
//--add-exports
//javafx.graphics/com.sun.scenario.effect.impl=ALL-UNNAMED
//--add-exports
//javafx.graphics/com.sun.scenario.effect.impl.prism=ALL-UNNAMED
//--add-exports
//javafx.graphics/com.sun.javafx.text=ALL-UNNAMED
//--add-exports
//javafx.media/com.sun.media.jfxmedia.events=ALL-UNNAMED
}