package me.shedaniel.rei.api.common.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Uuids {
    public static final StreamCodec<ByteBuf, UUID> STREAM_CODEC = new StreamCodec<ByteBuf, UUID>() {
        @Override
        public void encode(ByteBuf buf, UUID uuid) {
            buf.writeLong(uuid.getMostSignificantBits());
            buf.writeLong(uuid.getLeastSignificantBits());
        }

        @Override
        public @NotNull UUID decode(ByteBuf buf) {
            var mostSignificantBits = buf.readLong();
            var leastSignificantBits = buf.readLong();
            return new UUID(mostSignificantBits, leastSignificantBits);
        }
    };
}
