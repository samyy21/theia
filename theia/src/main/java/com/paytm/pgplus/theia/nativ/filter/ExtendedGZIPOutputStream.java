package com.paytm.pgplus.theia.nativ.filter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

public class ExtendedGZIPOutputStream extends GZIPOutputStream {
    public ExtendedGZIPOutputStream(OutputStream out) throws IOException {
        super(out);
    }

    public void setCompressionLevel(int level) {
        if (level >= -1 && level <= 9) {
            def.setLevel(level);
        } else {
            def.setLevel(Deflater.DEFAULT_COMPRESSION);
        }
    }
}
