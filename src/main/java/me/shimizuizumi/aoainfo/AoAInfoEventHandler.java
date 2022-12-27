package me.shimizuizumi.aoainfo;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.tslat.aoa3.content.item.weapon.blaster.BaseBlaster;
import net.tslat.aoa3.content.item.weapon.bow.BaseBow;
import net.tslat.aoa3.content.item.weapon.greatblade.BaseGreatblade;
import net.tslat.aoa3.content.item.weapon.gun.BaseGun;
import net.tslat.aoa3.content.item.weapon.maul.BaseMaul;
import net.tslat.aoa3.content.item.weapon.shotgun.BaseShotgun;

import java.util.List;

public class AoAInfoEventHandler {
    @SubscribeEvent
    public void onTooltipRender(ItemTooltipEvent ev) {
        if (!AoAInfoConfig.DISPLAY_ADVANCED_TOOLTIPS.get())
            return;

        Item item = ev.getItemStack().getItem();

        List<Component> tooltipLines = ev.getToolTip();

        if (item instanceof SwordItem || item instanceof BaseGreatblade || item instanceof BaseMaul || item instanceof HoeItem) {
            float attackSpeed = 0;
            float damage = 0;
            int lineIndex = 0;

            for (int i = 0; i < tooltipLines.size(); i++) {
                String line = tooltipLines.get(i).getString();

                if (line.endsWith(" Attack Speed")) {
                    String valueSubstring = line.substring(0, line.indexOf("Attack Speed")).replaceAll(" ", "");

                    try {
                        attackSpeed = Float.parseFloat(valueSubstring);
                    }
                    catch (NumberFormatException ex) {}
                }
                else if (line.endsWith(" Attack Damage")) {
                    String valueSubstring = line.substring(0, line.indexOf("Attack Damage")).replaceAll(" ", "");
                    lineIndex = i;
                    try {
                        damage = Float.parseFloat(valueSubstring);
                    }
                    catch (NumberFormatException ex) {}
                }
            }

            String dpsValue = roundToNthDecimalPlace(attackSpeed * damage, 2);

            if (Float.parseFloat(dpsValue) == 0)
                return;

            tooltipLines.add(lineIndex + 1, Component.translatable("gui.aoainfo.tooltips.dps", dpsValue).withStyle(ChatFormatting.GOLD));

            return;
        }
        else if (item instanceof ArmorItem && ev.getEntity() != null) {
            int newArmourValue = 0;
            int oldArmourValue = 0;
            float newToughnessValue = 0f;
            float oldToughnessValue = 0f;

            int armourLineIndex = -1;
            int toughnessLineIndex = -1;

            for (int i = 0; i < tooltipLines.size(); i++) {
                String line = tooltipLines.get(i).getString();

                if (line.endsWith(" Armor Toughness")) {
                    String valueSubstring = ChatFormatting.stripFormatting(line.substring(line.indexOf("+") + 1, line.indexOf("Armor Toughness")).replaceAll(" ", ""));

                    try {
                        newToughnessValue = Float.parseFloat(valueSubstring);
                        toughnessLineIndex = i;
                    }
                    catch (NumberFormatException ex) {}
                }
                else if (line.endsWith(" Armor")) {
                    String valueSubstring = ChatFormatting.stripFormatting(line.substring(line.indexOf("+") + 1, line.indexOf("Armor")).replaceAll(" ", ""));

                    try {
                        newArmourValue = Integer.parseInt(valueSubstring);
                        armourLineIndex = i;
                    }
                    catch (NumberFormatException ex) {}
                }
            }

            if (toughnessLineIndex >= 0) {
                Item compareItem;

                if ((compareItem = ev.getEntity().getItemBySlot(((ArmorItem) item).getSlot()).getItem()) instanceof ArmorItem)
                    oldToughnessValue = ((ArmorItem) compareItem).getToughness();
            }

            if (armourLineIndex >= 0) {
                Item compareItem;
                EquipmentSlot slot;

                if ((compareItem = ev.getEntity().getItemBySlot((slot = ((ArmorItem)item).getSlot())).getItem()) instanceof ArmorItem)
                    oldArmourValue = ((ArmorItem)compareItem).getMaterial().getDefenseForSlot(slot);
            }

            if (newArmourValue - oldArmourValue != 0) {
                int change = newArmourValue - oldArmourValue;

                tooltipLines.set(armourLineIndex, Component.literal(armourLineIndex + (change > 0 ? ChatFormatting.GREEN + " (+" + change + ")" : ChatFormatting.RED + " (" + change + "")));
            }

            if (newToughnessValue - oldToughnessValue != 0) {
                String change = roundToNthDecimalPlace(newToughnessValue - oldToughnessValue, 2);

                tooltipLines.set(toughnessLineIndex, Component.literal(toughnessLineIndex + (newToughnessValue - oldToughnessValue > 0 ? ChatFormatting.GREEN + " (+" + change + ")" : ChatFormatting.RED + " (" + change + ")")));
            }
        }

        if (item.getDescriptionId() == null || !item.getDescriptionId().contains("aoa3"))
            return;

        if (item instanceof BaseShotgun) {
            BaseShotgun shotgun = (BaseShotgun)item;
            float dmg = (float)shotgun.getDamage();
            float firingRate = 20 / (float)shotgun.getFiringDelay();

            for (int i = 0; i < tooltipLines.size(); i++) {
                String line = tooltipLines.get(i).getString();

                if (line.endsWith(" Damage")) {
                    String valueSubstring = ChatFormatting.stripFormatting(line.substring(0, line.indexOf("x")).replaceAll(" ", ""));

                    try {
                        dmg = Float.parseFloat(valueSubstring);
                    }
                    catch (NumberFormatException ex) {}
                }
                else if (line.contains("Firing Rate")) {
                    String valueSubstring = line.substring(line.indexOf(":") + 1, line.indexOf("/sec")).replaceAll(" ", "");

                    try {
                        firingRate = Float.parseFloat(valueSubstring);
                    }
                    catch (NumberFormatException ex) {}

                    String dpsValue = roundToNthDecimalPlace(firingRate * dmg * shotgun.getPelletCount(), 2);

                    if (Float.parseFloat(dpsValue) == 0)
                        return;

                    tooltipLines.set(i, Component.literal(line).append(" ").append(Component.translatable("gui.aoainfo.tooltips.dps", dpsValue)).withStyle(ChatFormatting.GOLD));
                }
            }
        }
        else if (item instanceof BaseGun) {
            BaseGun gun = (BaseGun)item;
            float dmg = (float)gun.getDamage();
            float firingRate = 20 / (float)gun.getFiringDelay();

            for (int i = 0; i < tooltipLines.size(); i++) {
                String line = tooltipLines.get(i).getString();

                if (line.endsWith(" Bullet Damage")) {
                    String valueSubstring = ChatFormatting.stripFormatting(line.substring(0, line.indexOf("Bullet Damage")).replaceAll(" ", ""));

                    try {
                        dmg = Float.parseFloat(valueSubstring);
                    }
                    catch (NumberFormatException ex) {}
                }
                else if (line.contains("Firing Rate")) {
                    String valueSubstring = line.substring(line.indexOf(":") + 1, line.indexOf("/sec")).replaceAll(" ", "");

                    try {
                        firingRate = Float.parseFloat(valueSubstring);
                    }
                    catch (NumberFormatException ex) {}

                    String dpsValue = roundToNthDecimalPlace(firingRate * dmg, 2);

                    if (Float.parseFloat(dpsValue) == 0)
                        return;

                    tooltipLines.set(i, Component.literal(line).append(" ").append(Component.translatable("gui.aoainfo.tooltips.dps", dpsValue)).withStyle(ChatFormatting.GOLD));
                }
            }
        }
        else if (item instanceof BaseBow) {
            BaseBow bow = (BaseBow)item;
            float dmg = (float)bow.getDamage();
            float firingRate = 0;

            for (int i = 0; i < tooltipLines.size(); i++) {
                String line = tooltipLines.get(i).getString();

                if (line.endsWith(" Average Ranged Damage")) {
                    String valueSubstring = ChatFormatting.stripFormatting(line.substring(0, line.indexOf("Average Ranged Damage")).replaceAll(" ", ""));

                    try {
                        dmg = Float.parseFloat(valueSubstring);
                    }
                    catch (NumberFormatException ex) {}
                }
                else if (line.startsWith("Draw Time:")) {
                    String valueSubstring = line.substring(line.indexOf(":") + 1, line.length() - 1).replaceAll(" ", "");

                    try {
                        firingRate = 20 / (20 * Float.parseFloat(valueSubstring));
                    }
                    catch (NumberFormatException ex) {}

                    String dpsValue = roundToNthDecimalPlace(firingRate * dmg, 2);

                    if (Float.parseFloat(dpsValue) == 0)
                        return;

                    tooltipLines.set(i, Component.literal(line).append(" ").append(Component.translatable("gui.aoainfo.tooltips.dps", dpsValue)).withStyle(ChatFormatting.GOLD));
                }
            }
        }
        else if (item instanceof BaseBlaster) {
            BaseBlaster blaster = (BaseBlaster)item;
            float dmg = (float)blaster.getDamage();
            float firingRate = 20 / (float)blaster.getFiringDelay();
            float energyConsumption = 0;

            for (int i = 0; i < tooltipLines.size(); i++) {
                String line = tooltipLines.get(i).getString();

                if (line.endsWith(" Blaster Damage")) {
                    String valueSubstring = ChatFormatting.stripFormatting(line.substring(0, line.indexOf("Blaster Damage")).replaceAll(" ", ""));

                    try {
                        dmg = Float.parseFloat(valueSubstring);
                    }
                    catch (NumberFormatException ex) {}
                }
                else if (line.contains("Firing Rate")) {
                    String valueSubstring = line.substring(line.indexOf(":") + 1, line.indexOf("/sec")).replaceAll(" ", "");

                    try {
                        firingRate = Float.parseFloat(valueSubstring);
                    }
                    catch (NumberFormatException ex) {}

                    String dpsValue = roundToNthDecimalPlace(firingRate * dmg, 2);

                    if (Float.parseFloat(dpsValue) == 0)
                        continue;

                    tooltipLines.set(i, Component.literal(line).append(" ").append(Component.translatable("gui.aoainfo.tooltips.dps", dpsValue)).withStyle(ChatFormatting.GOLD));
                }
                else if (line.endsWith(" Energy")) {
                    String valueSubstring = line.substring(line.indexOf("Consumes") + 8, line.indexOf("Energy")).replaceAll(" ", "");

                    try {
                        energyConsumption = Float.parseFloat(valueSubstring);

                        if (energyConsumption == 0)
                            return;
                    }
                    catch (NumberFormatException ex) {}

                    tooltipLines.set(i, Component.literal(line).append(" ").append(Component.translatable("gui.aoainfo.tooltip.perSec", roundToNthDecimalPlace(energyConsumption * firingRate, 2))));
                }
            }
        }
    }

    public static String roundToNthDecimalPlace(float value, int decimals)
    {
        float val = Math.round(value * (float) Math.pow(10, decimals)) / (float) Math.pow(10, decimals);

        if (((int) val) == val)
        {
            return String.valueOf((int) val);
        }
        return String.valueOf(val);
    }
}
