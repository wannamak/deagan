module deagan {
    requires java.logging;
    requires com.google.protobuf;

    requires transitive java.desktop;

    exports org.spcgreenville.deagan;
    exports org.spcgreenville.deagan.hourly;
    exports org.spcgreenville.deagan.logical;
    exports org.spcgreenville.deagan.manual;
    exports org.spcgreenville.deagan.midi;
    exports org.spcgreenville.deagan.physical;

}
