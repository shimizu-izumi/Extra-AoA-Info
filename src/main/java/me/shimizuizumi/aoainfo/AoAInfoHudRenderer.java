package me.shimizuizumi.aoainfo;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.tslat.aoa3.common.registration.item.AoAEnchantments;
import net.tslat.aoa3.common.registration.item.AoAItems;
import net.tslat.aoa3.content.item.lootbox.RuneBox;
import net.tslat.aoa3.content.item.weapon.bow.BaseBow;
import net.tslat.aoa3.content.item.weapon.bow.Slingshot;
import net.tslat.aoa3.content.item.weapon.gun.BaseGun;
import net.tslat.aoa3.content.item.weapon.staff.BaseStaff;
import net.tslat.aoa3.content.item.weapon.thrown.BaseThrownWeapon;
import net.tslat.aoa3.common.registration.item.AoAWeapons;
import net.tslat.aoa3.util.RenderUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class AoAInfoHudRenderer {
    private final Minecraft mc;
    public PoseStack stack = new PoseStack();

    private static final ResourceLocation toastTextures = new ResourceLocation("minecraft", "textures/gui/toasts.png");

    public AoAInfoHudRenderer() {
        this.mc = Minecraft.getInstance();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRenderTick(final TickEvent.RenderTickEvent ev) {
        if (mc.screen == null && !mc.options.hideGui && !mc.player.isSpectator() && AoAInfoConfig.DISPLAY_AMMO_HUD.get() && !mc.player.isCreative()) {
            ItemStack mainHandStack = mc.player.getMainHandItem();
            ItemStack offHandStack = mc.player.getOffhandItem();
            Item mainHandItem = mainHandStack.getItem();
            Item offHandItem = offHandStack.getItem();
            boolean doMainHand = false;
            boolean doOffHand = false;

            if (mainHandStack != ItemStack.EMPTY && ((mainHandItem instanceof BaseGun && !(mainHandItem instanceof BaseThrownWeapon) && mainHandItem instanceof BaseStaff || (mainHandItem instanceof BowItem && mainHandItem != AoAWeapons.SPECTRAL_BOW.get())) && (!AoAEnchantments.BRACE.isPresent() || EnchantmentHelper.getTagEnchantmentLevel(AoAEnchantments.BRACE.get(), mainHandStack) == 0)))
                doMainHand = true;

            if (offHandStack != ItemStack.EMPTY && ((offHandItem instanceof BaseGun && !(offHandItem instanceof BaseThrownWeapon) && offHandItem instanceof BaseStaff || (offHandItem instanceof BowItem && offHandItem != AoAWeapons.SPECTRAL_BOW.get())) && (!AoAEnchantments.BRACE.isPresent() || EnchantmentHelper.getTagEnchantmentLevel(AoAEnchantments.BRACE.get(), offHandStack) > 0)))
                doOffHand = true;

            if (!doMainHand && !doOffHand)
                return;

            RenderSystem.applyModelViewMatrix();
            RenderSystem.disableDepthTest();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

            int yOffset = 0;
            int yHeight = 0;

            mc.getTextureManager().bindForSetup(toastTextures);

            if (doMainHand) {
                if (mainHandItem instanceof BaseStaff) {
                    yHeight = 20 * ((BaseStaff)mainHandItem).getRunes().size();
                }
                else {
                    yHeight = 20;
                }
            }

            if (doOffHand) {
                if (offHandItem instanceof BaseStaff) {
                    yHeight += 20 * ((BaseStaff)offHandItem).getRunes().size() + (doMainHand ? 12 : 0);
                }
                else {
                    yHeight += doMainHand ? 32 : 20;
                }
            }

            RenderUtil.drawColouredBox(stack, 0, 0, 0, 160, 5, 0xa00500);
            RenderUtil.drawColouredBox(stack, 5, 0, 5, 160, 5, 0xa00d00);
            RenderUtil.drawColouredBox(stack, 13 + yHeight, 0, 27, 160, 5, 0xa005ff);

            if (doMainHand) {
                if (mainHandItem instanceof BaseStaff) {
                    yOffset += renderRunesGroup("Main Hand", mainHandStack, ((BaseStaff)mainHandItem).getRunes(), yOffset);
                }
                else if (mainHandItem instanceof Slingshot) {
                    yOffset += renderSlingshotGroup("Main Hand", mainHandStack, yOffset);
                }
                else if (mainHandItem instanceof BaseBow) {
                    yOffset += renderArrowGroup("Main Hand", mainHandStack, yOffset, false);
                }
                else if (mainHandItem instanceof BowItem) {
                    yOffset += renderArrowGroup("Main Hand", mainHandStack, yOffset, true);
                }
                else {
                    yOffset += renderGunGroup("Main Hand", mainHandStack, yOffset);
                }
            }

            if (doOffHand) {
                if (offHandItem instanceof BaseStaff) {
                    renderRunesGroup("Offhand", offHandStack, ((BaseStaff)offHandItem).getRunes(), yOffset);
                }
                else if (offHandItem instanceof Slingshot) {
                    renderSlingshotGroup("Offhand", offHandStack, yOffset);
                }
                else if (offHandItem instanceof BaseBow) {
                    renderArrowGroup("Offhand", offHandStack, yOffset, false);
                }
                else if (mainHandItem instanceof BowItem) {
                    renderArrowGroup("Offhand", offHandStack, yOffset, true);
                }
                else {
                    renderGunGroup("Offhand", offHandStack, yOffset);
                }
            }

            RenderSystem.applyModelViewMatrix();
        }
    }

    public int renderRunesGroup(String title, ItemStack staff, HashMap<RuneBox, Integer> runeMap, int yOffset) {
        RenderUtil.drawCenteredScaledString(stack, mc.font, title, 80, yOffset + 8, 1.0f, Color.WHITE.getRGB(), RenderUtil.StringRenderType.OUTLINED);

        HashMap<RuneBox, Integer> availableRuneMap = new HashMap<RuneBox, Integer>(runeMap.size());

        ItemStack checkStack1;
        ItemStack checkStack2;

        if ((checkStack1 = mc.player.getMainHandItem()).getItem() instanceof RuneBox) {
            if (runeMap.containsKey(checkStack1.getItem()))
                availableRuneMap.compute((RuneBox) checkStack1.getItem(), (key, val) -> val == null ? checkStack1.getCount() : val + checkStack1.getCount());
        }

        if ((checkStack2 = mc.player.getOffhandItem()).getItem() instanceof RuneBox) {
            if (runeMap.containsKey(checkStack2.getItem()))
                availableRuneMap.compute((RuneBox)checkStack2.getItem(), (key, val) -> val == null ? checkStack2.getCount() : val + checkStack2.getCount());
        }

        for (int i = 0; i < mc.player.getInventory().getContainerSize(); ++i) {
            ItemStack stack = mc.player.getInventory().getItem(i);

            if (runeMap.containsKey(stack.getItem()))
                availableRuneMap.compute((RuneBox) stack.getItem(), (key, val) -> val == null ? stack.getCount() : val + stack.getCount());
        }

        int archMage = EnchantmentHelper.getTagEnchantmentLevel(AoAEnchantments.ARCHMAGE.get(), staff);
        boolean nightmareArmour = true;
        int greed = EnchantmentHelper.getTagEnchantmentLevel(AoAEnchantments.GREED.get(), staff);

        for (int i = 0; i < mc.player.getInventory().armor.size(); i++) {
            ItemStack armourItem = mc.player.getInventory().armor.get(i);
        }

        int runeNum = 0;

        for (Map.Entry<RuneBox, Integer> entry : runeMap.entrySet()) {
            int amount = Math.max(1, entry.getValue() + greed * 2 - archMage - (nightmareArmour ? 1 : 0));
            int available = availableRuneMap.getOrDefault(entry.getKey(), 0);

            mc.getItemRenderer().renderGuiItem(new ItemStack(entry.getKey()), 10, yOffset + 18 + runeNum * 20);
            RenderUtil.drawOutlinedText(stack, mc.font, Component.translatable("gui.aoainfo.ammoHud.amount", String.valueOf(amount)) + "      " + Component.translatable("gui.aoainfo.ammoHud.available", String.valueOf(available)), 26, yOffset + 22 + runeNum * 20, available >= amount ? Color.WHITE.getRGB() : Color.RED.getRGB(), 1.0f);
            runeNum++;
        }

        return runeNum * 20 + 12;
    }

    public int renderGunGroup(String title, ItemStack weapon, int yOffset) {
        RenderUtil.drawCenteredScaledString(stack, mc.font, title, 80, yOffset + 8, 1.0f, Color.WHITE.getRGB(), RenderUtil.StringRenderType.OUTLINED);

        Item ammoItem = Items.AIR;
        int ammoCount = 0;
        ItemStack checkStack;
        List<Component> tooltip = new ArrayList<>();

        tooltip.add(Component.literal(""));

        weapon.getItem().appendHoverText(weapon, mc.player.level, tooltip, TooltipFlag.Default.NORMAL);


        for (Component line : tooltip) {
            if (line.contains(Component.literal("Ammo: "))) {
                String ammo = line.getString().substring(line.getString().indexOf(":") + 2);

                switch (ammo) {
                    case "Bullets":
                        ammoItem = AoAItems.LIMONITE_BULLET.get();
                        break;
                    case "Discharge Capsule":
                        ammoItem = AoAItems.DISCHARGE_CAPSULE.get();
                        break;
                    case "Seeds":
                        ammoItem = Items.WHEAT_SEEDS;
                        break;
                    case "Spreadshot":
                        ammoItem = AoAItems.SPREADSHOT.get();
                        break;
                    case "Cannonballs":
                        ammoItem = AoAItems.CANNONBALL.get();
                        break;
                    case "Metal Slug":
                    case "Metal Slugs":
                        ammoItem = AoAItems.METAL_SLUG.get();
                        break;
                    case "Cobblestone":
                        ammoItem = BlockItem.byBlock(Blocks.COBBLESTONE);
                        break;
                    case "Grenades":
                        ammoItem = AoAWeapons.GRENADE.get();
                        break;
                    case "Carrot":
                        ammoItem = Items.CARROT;
                        break;
                    case "Balloon":
                        ammoItem = AoAItems.BALLOON.get();
                        break;
                    case "Chili":
                        ammoItem = AoAItems.CHILLI.get();
                        break;
                    case "Nether Wart":
                        ammoItem = Items.NETHER_WART;
                        break;
                    case "Leather Boots":
                        ammoItem = Items.LEATHER_BOOTS;
                        break;
                    default:
                        ammoItem = Items.AIR;
                        break;
                }
            }
        }

        if ((checkStack = mc.player.getMainHandItem()).getItem() == ammoItem)
            ammoCount += checkStack.getCount();

        if ((checkStack = mc.player.getOffhandItem()).getItem() == ammoItem)
            ammoCount += checkStack.getCount();

        int greed = EnchantmentHelper.getTagEnchantmentLevel(AoAEnchantments.GREED.get(), weapon);

        mc.getItemRenderer().renderGuiItem(new ItemStack(ammoItem), 10, yOffset + 18);
        RenderUtil.drawOutlinedText(stack, mc.font, Component.translatable("gui.aoainfo.ammoHud.amount", String.valueOf(1 + greed)) + "      " + Component.translatable("gui.aoainfo.ammoHud.available", String.valueOf(ammoCount)), 26, yOffset + 22, ammoCount > 0 ? Color.WHITE.getRGB() : Color.RED.getRGB(), 1.0f);

        return 32;
    }

    public int renderArrowGroup(String title, ItemStack weapon, int yOffset, boolean isVanillaBow) {
        RenderUtil.drawCenteredScaledString(stack, mc.font, title, 80, yOffset + 8, 1.0f, Color.WHITE.getRGB(), RenderUtil.StringRenderType.OUTLINED);

        ItemStack ammoItem = ItemStack.EMPTY;
        int ammoCount = 0;
        ItemStack checkStack;

        int greed = EnchantmentHelper.getTagEnchantmentLevel(AoAEnchantments.GREED.get(), weapon);

        mc.getItemRenderer().renderGuiItem(ammoItem, 10, yOffset + 18);
        RenderUtil.drawOutlinedText(stack, mc.font, Component.translatable("gui.aoainfo.ammoHud.amount", String.valueOf(1 + greed)) + "      " + Component.translatable("gui.aoainfo.ammoHud.available", String.valueOf(ammoCount)), 26, yOffset + 22, ammoCount > 0 ? Color.WHITE.getRGB() : Color.RED.getRGB(), 1.0f);

        return 32;
    }

    public int renderSlingshotGroup(String title, ItemStack weapon, int yOffset) {
        RenderUtil.drawCenteredScaledString(stack, mc.font, title, 80, yOffset + 8, 1.0f, Color.WHITE.getRGB(), RenderUtil.StringRenderType.OUTLINED);

        ItemStack ammoItem = ItemStack.EMPTY;
        int ammoCount = 0;
        ItemStack checkStack;

        if ((checkStack = mc.player.getMainHandItem()).getItem() == AoAItems.POP_SHOT.get() || checkStack.getItem() == Items.FLINT) {
            ammoItem = checkStack;
            ammoCount = checkStack.getCount();
        }

        if ((checkStack = mc.player.getOffhandItem()).getItem() == AoAItems.POP_SHOT.get() || checkStack.getItem() == Items.FLINT) {
            if (ammoItem == ItemStack.EMPTY)
                ammoItem = checkStack;

            if (checkStack.getItem() == ammoItem.getItem())
                ammoCount += checkStack.getCount();
        }

        if (ammoItem == ItemStack.EMPTY)
            ammoItem = new ItemStack(AoAItems.POP_SHOT.get());

        int greed = EnchantmentHelper.getTagEnchantmentLevel(AoAEnchantments.GREED.get(), weapon);

        mc.getItemRenderer().renderGuiItem(ammoItem, 10, yOffset + 18);
        RenderUtil.drawOutlinedText(stack, mc.font, Component.translatable("gui.aoainfo.ammoHud.amount", String.valueOf(1 + greed)) + "      " + Component.translatable("gui.aoainfo.ammoHud.available", String.valueOf(ammoCount)), 26, yOffset + 22, ammoCount > 0 ? Color.WHITE.getRGB() : Color.RED.getRGB(), 1.0f);

        return 32;
    }
}
