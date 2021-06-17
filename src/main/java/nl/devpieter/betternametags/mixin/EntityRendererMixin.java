/*
* #######################################################################
*        ____                  ____     _          __
*       / __ \  ___  _   __   / __ \   (_)  ___   / /_  ___    _____
*      / / / / / _ \| | / /  / /_/ /  / /  / _ \ / __/ / _ \  / ___/
*     / /_/ / /  __/| |/ /  / ____/  / /  /  __// /_  /  __/ / /
*    /_____/  \___/ |___/  /_/      /_/   \___/ \__/  \___/ /_/
*   
*                    This mod was created by DevPieter
*                              © DevPieter
*
*      DevPieter.nl    github.com/DevPieter    twitter.com/DevPieter
* #######################################################################
*/

package nl.devpieter.betternametags.mixin;

import java.text.DecimalFormat;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

	MinecraftClient minecraft = MinecraftClient.getInstance();

	@Inject(method = "renderLabelIfPresent", at = @At("TAIL"))
	protected void renderLabelIfPresent(T entity, Text text, MatrixStack matrices,
			VertexConsumerProvider vertexConsumers, int light, CallbackInfo callbackInfo) {
		if (entity instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) entity;

			DecimalFormat df = new DecimalFormat("#.#");
			Text infoRow1 = new LiteralText(String.format("%s \u00A7a↔ %s", getHealth(player, df),
					df.format(player.distanceTo(minecraft.player))));
			Text infoRow2 = new LiteralText(String.format("\u00A7d%s %s %s", df.format(player.getPos().x),
					df.format(player.getPos().y), df.format(player.getPos().z)));

			double d = minecraft.getEntityRenderDispatcher().getSquaredDistanceToCamera(player);
			if (d <= 4096.0D) {

				boolean isNotSneaky = !player.isSneaky();
				TextRenderer textRenderer = minecraft.getEntityRenderDispatcher().getTextRenderer();

				float backgroundOpacity = minecraft.options.getTextBackgroundOpacity(0.25F);
				int backgroundColor = (int) (backgroundOpacity * 255.0F) << 24;

				float f = player.getHeight() + 0.5F;
				matrices.push();
				matrices.translate(0.0D, (double) f, 0.0D);
				matrices.multiply(minecraft.getEntityRenderDispatcher().getRotation());
				matrices.scale(-0.025F, -0.025F, 0.025F);
				Matrix4f matrix4f = matrices.peek().getModel();

				float infoRow1X = (float) (-textRenderer.getWidth((StringVisitable) infoRow1) / 2);
				float infoRow2X = (float) (-textRenderer.getWidth((StringVisitable) infoRow2) / 2);
				float textX = (float) (-textRenderer.getWidth((StringVisitable) text) / 2);
				int y = -10;

				// Render name tag
				textRenderer.draw(infoRow1, infoRow1X, (float) y - 10, 553648127, false, matrix4f, vertexConsumers, isNotSneaky, backgroundColor, light);
				textRenderer.draw(infoRow2, infoRow2X, (float) y, 553648127, false, matrix4f, vertexConsumers, isNotSneaky, backgroundColor, light);

				if (isNotSneaky) {
					// Render name tag
					textRenderer.draw(infoRow1, infoRow1X, (float) y - 10, -1, false, matrix4f, vertexConsumers, false, 0, light);
					textRenderer.draw(infoRow2, infoRow2X, (float) y, -1, false, matrix4f, vertexConsumers, false, 0, light);

					// Render players head
					Identifier texture = nameToTexture(minecraft, player.getName().asString());
					if (texture != null) {
						RenderSystem.enableDepthTest();
						minecraft.getTextureManager().bindTexture(texture);
						DrawableHelper.drawTexture(matrices, (int) textX - 11, y + 9, 10, 10, 8.0F, 8.0F, 8, 8, 64, 64);
						RenderSystem.enableBlend();
						DrawableHelper.drawTexture(matrices, (int) textX - 11, y + 9, 10, 10, 40.0F, 8.0F, 8, 8, 64, 64);
						RenderSystem.disableBlend();
						RenderSystem.disableDepthTest();
					}
				}
				matrices.pop();
			}
		}
	}

	private static Identifier nameToTexture(MinecraftClient minecraft, String name) {
		if (minecraft.player.world == null)
			return null;

		Identifier foundSkin = null;
		for (PlayerListEntry playerList : minecraft.player.networkHandler.getPlayerList()) {
			if (playerList.getProfile().getName().equalsIgnoreCase(name)) {
				foundSkin = playerList.getSkinTexture();
			}
		}
		return foundSkin;
	}

	private static String getHealth(PlayerEntity player, DecimalFormat df) {
		float Health = player.getHealth() + player.getAbsorptionAmount();
		if (Health > 20)
			return " \u00A76❤" + df.format(Health);
		else if (Health >= 15.0 && Health <= 20.9)
			return " \u00A7a❤" + df.format(Health);
		else if (Health >= 10.0 && Health <= 14.9)
			return " \u00A7e❤" + df.format(Health);
		else if (Health >= 5.0 && Health <= 9.9)
			return " \u00A7c❤" + df.format(Health);
		else if (Health >= 1.0 && Health <= 4.9)
			return " \u00A74❤" + df.format(Health);
		else
			return " \u00A70❤" + df.format(Health);
	}

}
