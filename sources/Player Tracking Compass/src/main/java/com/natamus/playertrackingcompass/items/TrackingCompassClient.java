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

package com.natamus.playertrackingcompass.items;

import com.natamus.collective.functions.StringFunctions;
import java.util.UUID;
import javax.annotation.Nullable;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemModelsProperties;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TrackingCompassClient implements IItemPropertyGetter {

	@SubscribeEvent
	public static void models(FMLClientSetupEvent e) {
		ItemModelsProperties.registerProperty(CompassVariables.TRACKING_COMPASS, new ResourceLocation("angle"), new TrackingCompassClient());
	}

	private double prevAngle = 0.0D;
	private double prevWobble = 0.0D;
	private long prevWorldTime = 0L;

	/**
	 * Calculates the compass angle from an item stack and an entity/item frame
	 *
	 * @param stack The item stack
	 * @param world The world
	 * @param livingEntity The entity
	 * @return The angle
	 */
	@Override
	public float call(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity livingEntity) {
		boolean isLiving = livingEntity != null;

		if (!isLiving && !stack.isOnItemFrame()) {
			return 0;
		}

		Entity entity = isLiving ? livingEntity : stack.getItemFrame();

		if (world == null) {
			world = (ClientWorld)entity.world;
		}

		if (CompassVariables.trackingTarget == null) {
			return 0;
		}

		double d1 = entity.rotationYaw;
		d1 = MathHelper.positiveModulo(d1 / 360.0D, 1.0D);
		double d2 = this.getSpawnToAngle(world, entity, stack) / (Math.PI * 2D);
		return MathHelper.positiveModulo((float)  (0.5D - (d1 - 0.25D - d2)), 1.0F);
	}

	/**
	 * Gets the facing direction of an item frame in degrees
	 *
	 * @param entity The entity instance of the item frame
	 * @return The angle
	 */
	private double getFrameAngle(ItemFrameEntity entity) {
		return MathHelper.wrapDegrees(180 + entity.getHorizontalFacing().getHorizontalIndex() * 90);
	}

	/**
	 * Gets the angle from an entity to the specified position in radians
	 *
	 * @param pos The position
	 * @param entity The entity
	 * @return The angle
	 */
	private double getPosToAngle(int[] pos, Entity entity) {
		return Math.atan2(pos[2] - entity.getPosZ(), pos[0] - entity.getPosX());
	}

	@OnlyIn(Dist.CLIENT)
	private double getSpawnToAngle(World world, Entity entity, ItemStack stack) {
		return Math.atan2((double) CompassVariables.trackingTarget[2] - entity.getPosZ(),
				(double) CompassVariables.trackingTarget[0] - entity.getPosX());
	}
}
