package moe.hertz.infinity_fix.mixin;


import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(value = BowItem.class, priority = 1001)
public class BowItemMixin {
    
    private boolean returnedArrow = false;
    
    private static int getArrowChances(Item arrowItem) {
        if (arrowItem instanceof ArrowItem)
            if (arrowItem == Items.ARROW) {
                return 3; // 1/3, 2/3, and 3/3
            } else if (arrowItem == Items.SPECTRAL_ARROW || arrowItem == Items.TIPPED_ARROW) {
                return 4; // 1/4, 2/4, and 3/4
            } else {
                return 3; // Default. Arrow type is unknown.
            }
        else
            return 3; // Not an arrow
    }
    
    @ModifyVariable(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("STORE"),
            index = 10
    )
    boolean shouldReturnArrow(boolean value, ItemStack stack, World world, LivingEntity shooter, int remainingUseTicks) {
        Item arrowItem = shooter.getArrowType(stack).getItem();
    
        PlayerAdvancementTracker age = ((ServerPlayerEntity) shooter).getAdvancementTracker();
    
        int arrowReturnChance = getArrowChances(arrowItem);
        
        int infinityLevel = EnchantmentHelper.getLevel(Enchantments.INFINITY, stack);
        int rnd = shooter.getRandom().nextInt(arrowReturnChance);
        
        returnedArrow = infinityLevel - rnd > 0;
        
        return returnedArrow;
    }
    
    @Redirect(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z")
    )
    boolean updateArrowPickupType(World instance, Entity entity, ItemStack stack, World world, LivingEntity shooter,
                                  int remainingUseTicks) {
        PlayerEntity player = (PlayerEntity) shooter;
        PersistentProjectileEntity persistentProjectileEntity = (PersistentProjectileEntity) entity;
        
        if (returnedArrow || player.getAbilities().creativeMode) {
            persistentProjectileEntity.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
        } else {
            persistentProjectileEntity.pickupType = PersistentProjectileEntity.PickupPermission.ALLOWED;
        }
        
        return world.spawnEntity(persistentProjectileEntity);
    }
}
