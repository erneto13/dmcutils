package com.erneto.client.core;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public final class CPEventParser {

    private CPEventParser() {
    }

    public static CPEvent parse(int type, byte[] raw) throws IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(raw));

        return switch (type) {
            case 1 -> new CPEvent.BlockEvent(
                    in.readLong(), in.readUTF(), in.readUTF(), in.readUTF(), in.readInt(),
                    in.readInt(), in.readInt(), in.readInt(), in.readUTF(),
                    in.readBoolean(), in.readBoolean(), in.readBoolean());
            case 2 -> new CPEvent.ContainerEvent(
                    in.readLong(), in.readUTF(), in.readUTF(), in.readInt(),
                    in.readInt(), in.readInt(), in.readInt(), in.readUTF());
            case 3 -> new CPEvent.MessageEvent(
                    in.readLong(), in.readUTF(), in.readUTF(), in.readBoolean());
            case 4 -> new CPEvent.SimpleEvent(
                    in.readLong(), in.readUTF(), in.readUTF());
            default -> throw new IOException("ERROR :: Unknown CoreProtect data type: " + type);
        };
    }
}