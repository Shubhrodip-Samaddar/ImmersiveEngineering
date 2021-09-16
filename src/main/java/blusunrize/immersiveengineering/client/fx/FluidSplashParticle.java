/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.fx;

import blusunrize.immersiveengineering.client.ClientUtils;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleOptions.Deserializer;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author BluSunrize - 21.02.2017
 */
@OnlyIn(Dist.CLIENT)
public class FluidSplashParticle extends TextureSheetParticle
{
	public FluidSplashParticle(Fluid fluid, ClientLevel worldIn, double xCoordIn, double yCoordIn, double zCoordIn,
							   double xSpeedIn, double ySpeedIn, double zSpeedIn)
	{
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);

		this.xd *= 0.3D;
		this.yd = Math.random()*0.2D+0.1D;
		this.zd *= 0.3D;
		this.rCol = 1.0F;
		this.gCol = 1.0F;
		this.bCol = 1.0F;
		this.setSize(0.01F, 0.01F);
		this.gravity = 0.06F;
		this.lifetime = (int)(8.0D/(Math.random()*0.8D+0.2D));
		this.quadSize *= .375f;
		this.setFluidTexture(new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME));
	}

	@Override
	public void tick()
	{
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		this.yd -= this.gravity;
		this.move(this.xd, this.yd, this.zd);
		this.xd *= 0.98;
		this.yd *= 0.98;
		this.zd *= 0.98;

		if(this.lifetime-- <= 0)
			this.remove();

		if(this.onGround)
		{
			if(Math.random() < 0.5D)
				this.remove();
			this.xd *= 0.7;
			this.zd *= 0.7;
		}

		BlockPos blockpos = new BlockPos(this.x, this.y, this.z);
		BlockState iblockstate = this.level.getBlockState(blockpos);
		Material material = iblockstate.getMaterial();

		if(material.isLiquid()||material.isSolid())
		{
			double d0;
			/*TODO if(iblockstate.getBlock() instanceof BlockLiquid)
				d0 = (double)(1.0F-BlockLiquid.getLiquidHeightPercent(iblockstate.getValue(BlockLiquid.LEVEL).intValue()));
			else*/
			d0 = iblockstate.getShape(this.level, blockpos).max(Axis.Y);
			double d1 = (double)Mth.floor(this.y)+d0;
			if(this.y < d1)
				this.remove();
		}
	}

	public void setFluidTexture(FluidStack fluid)
	{
		setSprite(ClientUtils.getSprite(fluid.getFluid().getAttributes().getStillTexture(fluid)));
		int argb = fluid.getFluid().getAttributes().getColor(fluid);
		this.alpha = ((argb >> 24)&255)/255f;
		this.rCol = ((argb >> 16)&255)/255f;
		this.gCol = ((argb >> 8&255))/255f;
		this.bCol = (argb&255)/255f;
	}

	@Nonnull
	@Override
	public ParticleRenderType getRenderType()
	{
		return ParticleRenderType.TERRAIN_SHEET;
	}

	@OnlyIn(Dist.CLIENT)
	public static class Factory implements ParticleProvider<FluidSplashOptions>
	{
		@Nullable
		@Override
		public Particle createParticle(FluidSplashOptions typeIn, @Nonnull ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed)
		{
			return new FluidSplashParticle(typeIn.fluid(), worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
		}
	}

	public static class DataDeserializer implements Deserializer<FluidSplashOptions>
	{
		@Nonnull
		@Override
		public FluidSplashOptions fromCommand(@Nonnull ParticleType<FluidSplashOptions> particleTypeIn, StringReader reader) throws CommandSyntaxException
		{
			String name = reader.getString();
			return new FluidSplashOptions(new ResourceLocation(name));
		}

		@Nonnull
		@Override
		public FluidSplashOptions fromNetwork(@Nonnull ParticleType<FluidSplashOptions> particleTypeIn, FriendlyByteBuf buffer)
		{
			return new FluidSplashOptions(buffer.readResourceLocation());
		}
	}
}