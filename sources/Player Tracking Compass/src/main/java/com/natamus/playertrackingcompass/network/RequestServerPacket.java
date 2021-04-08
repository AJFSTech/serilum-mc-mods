/*
 * This is the latest source code of Player Tracking Compass.
 * Minecraft version: 1.16.5, mod version: 1.6.
 *
 * If you'd like access to the source code of previous Minecraft versions or previous mod versions, consider becoming a Github Sponsor or Patron.
 * You'll be added to a private repository which contains all versions' source of Player Tracking Compass ever released, along with some other perks.
 *
 * Github Sponsor link: https://github.com/sponsors/ricksouth
 * Patreon link: https://patreon.com/ricksouth
 *
 * Becoming a Sponsor or Patron allows me to dedicate more time to the development of mods.
 * Thanks for looking at the source code! Hope it's of some use to your project. Happy modding!
 */

package com.natamus.playertrackingcompass.network;

import com.natamus.collective.functions.StringFunctions;
import com.natamus.playertrackingcompass.Main;
import java.util.function.Supplier;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

public class RequestServerPacket {

  public RequestServerPacket() {
  }

  public RequestServerPacket(PacketBuffer buf) {
  }

  public void fromBytes(PacketBuffer buf) {
  }

  public void toBytes(PacketBuffer buf) {
  }

  public void handle(Supplier<NetworkEvent.Context> ctx) {
    ctx.get().enqueueWork(() -> {
      BlockPos targetpos = new BlockPos(0, 0, 0);

      ServerPlayerEntity serverplayer = ctx.get().getSender();
      BlockPos serverplayerpos = serverplayer.getPosition();
      BlockPos comparepp = new BlockPos(serverplayerpos.getX(), serverplayerpos.getY(), serverplayerpos.getZ());

      ServerPlayerEntity closestplayer = null;
      double closestdistance = Double.MAX_VALUE;

      ServerWorld world = serverplayer.getServerWorld();
      for (ServerPlayerEntity oplayer : world.getPlayers()) {
        if (oplayer.getUniqueID().equals(serverplayer.getUniqueID())) {
          continue;
        }
        BlockPos oplayerpos = oplayer.getPosition();
        BlockPos compareop = new BlockPos(oplayerpos.getX(), oplayerpos.getY(), oplayerpos.getZ());

        double distance = comparepp.manhattanDistance(compareop);
        if (distance < closestdistance) {
          closestdistance = distance;
          closestplayer = oplayer;
        }
      }

      if (closestplayer != null) {
        targetpos = closestplayer.getPosition().toImmutable();
        serverplayer.sendStatusMessage(
            new StringTextComponent("Tracking " + closestplayer.getName().getString()).mergeStyle(TextFormatting.GREEN), true);
      } else {
        serverplayer.sendStatusMessage(
            new StringTextComponent("No target found").mergeStyle(TextFormatting.RED), true);
      }
      Main.network.sendTo(new PacketToClientUpdateTarget(targetpos),
          serverplayer.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
    });
    ctx.get().setPacketHandled(true);
  }
}
